package thedarkcolour.futuremc.block.villagepillage

import net.minecraft.block.Block
import net.minecraft.block.properties.PropertyBool
import net.minecraft.block.properties.PropertyInteger
import net.minecraft.block.state.BlockStateContainer
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.item.EntityFallingBlock
import net.minecraft.util.BlockRenderLayer
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.BlockPos.MutableBlockPos
import net.minecraft.world.IBlockAccess
import net.minecraft.world.World
import thedarkcolour.core.block.FBlock
import thedarkcolour.futuremc.registry.FBlocks
import java.util.*

class ScaffoldingBlock(properties: Properties) : FBlock(properties) {
    override fun createBlockState(): BlockStateContainer {
        return BlockStateContainer(this, DISTANCE, BOTTOM)
    }

    override fun getBoundingBox(state: IBlockState, worldIn: IBlockAccess, pos: BlockPos): AxisAlignedBB {
        return super.getBoundingBox(state, worldIn, pos)
    }

    // ============================================================
    // ✅ 修复：碰撞箱逻辑
    // - 玩家正在上升（跳跃）→ 空心碰撞箱（不阻挡上升）
    // - 玩家在顶部（不潜行）→ 顶部边框碰撞箱（防止掉落）
    // - 玩家在内部 + 不潜行 → 实心碰撞箱（挂在脚手架上）
    // - 玩家在内部 + 潜行 → 空心碰撞箱（允许穿过中间下降）
    // ============================================================
    override fun addCollisionBoxToList(
        state: IBlockState,
        worldIn: World,
        pos: BlockPos,
        entityBox: AxisAlignedBB,
        collidingBoxes: List<AxisAlignedBB>,
        entityIn: Entity?,
        isActualState: Boolean
    ) {
        if (entityIn !is EntityLivingBase) return

        // ✅ 玩家正在上升（跳跃）→ 使用空心碰撞箱，不阻挡上升
        if (entityIn.motionY > 0.0) {
            for (box in noBottomCollisionBoxes) {
                addCollisionBoxToList(pos, entityBox, collidingBoxes, box)
            }
            return
        }

        // 判断玩家是否在脚手架顶部（站在上面）
        val isOnTop = entityIn.posY > (pos.y + 1.0 - 0.001) && !entityIn.isSneaking

        // 玩家在顶部 → 只提供顶部边框碰撞（防止从边缘掉落）
        if (isOnTop) {
            for (box in noBottomCollisionBoxes) {
                addCollisionBoxToList(pos, entityBox, collidingBoxes, box)
            }
            return
        }

        // 玩家在脚手架内部
        if (entityIn.posY > (pos.y - 0.001)) {
            // 潜行 → 使用空心碰撞箱（中间可穿过，实现下降）
            if (entityIn.isSneaking) {
                for (box in noBottomCollisionBoxes) {
                    addCollisionBoxToList(pos, entityBox, collidingBoxes, box)
                }
            } else {
                // 不潜行 → 使用实心碰撞箱（挂在脚手架上）
                for (box in bottomCollisionBoxes) {
                    addCollisionBoxToList(pos, entityBox, collidingBoxes, box)
                }
            }
        }
    }

    // ============================================================
    // ✅ 玩家在脚手架内部的移动逻辑
    // - 按住跳跃键：持续上升（由 isLadder = true 配合跳跃实现）
    // - 按住潜行键：持续下降（由 onEntityCollision 控制）
    // - 不按任何键：停留在原地（由碰撞箱阻止下落）
    // ============================================================
    override fun onEntityCollision(
        worldIn: World,
        pos: BlockPos,
        state: IBlockState,
        entityIn: Entity
    ) {
        super.onEntityCollision(worldIn, pos, state, entityIn)

        if (entityIn is EntityLivingBase) {
            val box = entityIn.entityBoundingBox
            val minX = pos.x.toDouble()
            val minY = pos.y.toDouble()
            val minZ = pos.z.toDouble()
            val maxX = pos.x + 1.0
            val maxY = pos.y + 1.0
            val maxZ = pos.z + 1.0

            if (box.intersects(minX, minY, minZ, maxX, maxY, maxZ)) {
                when {
                    // 按住潜行 → 持续下降
                    entityIn.isSneaking -> {
                        entityIn.motionY = -0.08
                    }
                    // 不按潜行且正在下落 → 停留在原地（挂在脚手架上）
                    entityIn.motionY < 0.0 -> {
                        entityIn.motionY = 0.0
                    }
                    // 上升由跳跃键 + isLadder = true 控制
                }
            }
        }
    }

    override fun getRenderLayer(): BlockRenderLayer {
        return BlockRenderLayer.CUTOUT
    }

    override fun getStateForPlacement(
        worldIn: World, pos: BlockPos, facing: EnumFacing, hitX: Float,
        hitY: Float, hitZ: Float, meta: Int, placer: EntityLivingBase
    ): IBlockState {
        val i = getHorizontalDistance(worldIn, pos)
        return this.defaultState.withProperty(DISTANCE, i).withProperty(BOTTOM, hasBottom(worldIn, pos, i))
    }

    private fun hasBottom(worldIn: World, pos: BlockPos, i: Int): Boolean {
        return i > 0 && worldIn.getBlockState(pos.down()).block != this
    }

    override fun onBlockAdded(worldIn: World, pos: BlockPos, state: IBlockState) {
        if (!worldIn.isRemote) {
            worldIn.scheduleUpdate(pos, this, 1)
        }
    }

    override fun neighborChanged(state: IBlockState, worldIn: World, pos: BlockPos, blockIn: Block, fromPos: BlockPos) {
        if (!worldIn.isRemote) {
            worldIn.scheduleUpdate(pos, this, 1)
        }
    }

    override fun updateTick(worldIn: World, pos: BlockPos, state: IBlockState, rand: Random) {
        val i = getHorizontalDistance(worldIn, pos)
        val blockstate = state.withProperty(DISTANCE, i).withProperty(BOTTOM, hasBottom(worldIn, pos, i))
        if (blockstate.getValue(DISTANCE) == 7) {
            if (state.getValue(DISTANCE) == 7) {
                worldIn.spawnEntity(
                    EntityFallingBlock(
                        worldIn,
                        pos.x.toDouble() + 0.5,
                        pos.y.toDouble(),
                        pos.z.toDouble() + 0.5,
                        state
                    )
                )
            } else {
                worldIn.destroyBlock(pos, true)
            }
        } else if (state != blockstate) {
            worldIn.setBlockState(pos, blockstate, 3)
        }
    }

    override fun canPlaceBlockAt(worldIn: World, pos: BlockPos): Boolean {
        return getHorizontalDistance(worldIn, pos) < 7
    }

    override fun isLadder(state: IBlockState, world: IBlockAccess, pos: BlockPos, entity: EntityLivingBase): Boolean {
        return true  // 允许玩家攀爬（跳跃上升）
    }

    override fun getStateFromMeta(meta: Int): IBlockState {
        val bottom = meta > 7
        val distance = if (bottom) meta - 8 else meta
        return defaultState.withProperty(BOTTOM, bottom).withProperty(DISTANCE, distance)
    }

    override fun getMetaFromState(state: IBlockState): Int {
        return if (state.getValue(BOTTOM)) state.getValue(DISTANCE) + 8 else state.getValue(DISTANCE)
    }

    override fun isFullBlock(state: IBlockState): Boolean {
        return false
    }

    override fun isFullCube(state: IBlockState): Boolean {
        return false
    }

    override fun isOpaqueCube(state: IBlockState): Boolean {
        return false
    }

    companion object {
        private val DISTANCE: PropertyInteger = PropertyInteger.create("distance", 0, 7)
        private val BOTTOM: PropertyBool = PropertyBool.create("bottom")

        private var bottomCollisionBoxes = arrayOf(
            cube(0.0, 0.0, 0.0, 16.0, 2.0, 16.0),
            cube(0.0, 0.0, 0.0, 2.0, 2.0, 16.0),
            cube(14.0, 0.0, 0.0, 16.0, 2.0, 16.0),
            cube(0.0, 0.0, 14.0, 16.0, 2.0, 16.0),
            cube(0.0, 0.0, 0.0, 16.0, 2.0, 2.0)
        )
        private var noBottomCollisionBoxes = arrayOf(
            cube(0.0, 14.0, 0.0, 16.0, 16.0, 16.0),
            cube(0.0, 0.0, 0.0, 2.0, 16.0, 2.0),
            cube(14.0, 0.0, 0.0, 16.0, 16.0, 2.0),
            cube(0.0, 0.0, 14.0, 2.0, 16.0, 16.0),
            cube(14.0, 0.0, 14.0, 16.0, 16.0, 16.0)
        )

        fun getHorizontalDistance(worldIn: World, pos: BlockPos): Int {
            val blockPos = MutableBlockPos(pos).move(EnumFacing.DOWN)
            val blockstate = worldIn.getBlockState(blockPos)
            var i = 7
            if (blockstate.block == FBlocks.SCAFFOLDING) {
                i = blockstate.getValue(DISTANCE)
            } else if (blockstate.isSideSolid(worldIn, blockPos, EnumFacing.UP)) {
                return 0
            }
            for (direction in EnumFacing.Plane.HORIZONTAL) {
                val blockstate1 = worldIn.getBlockState(blockPos.setPos(pos).move(direction))
                if (blockstate1.block == FBlocks.SCAFFOLDING) {
                    i = i.coerceAtMost(blockstate1.getValue(DISTANCE) + 1)
                    if (i == 1) {
                        break
                    }
                }
            }
            return i
        }
    }
}
