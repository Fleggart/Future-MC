下面是合并后的单文件版本（两个类放在同一个 Kotlin 文件里，保留原逻辑与依赖）：

package thedarkcolour.futuremc.block.villagepillage

import net.minecraft.block.Block
import net.minecraft.block.material.Material
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.util.EnumActionResult
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.SoundCategory
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import thedarkcolour.core.item.ModeledItemBlock
import thedarkcolour.futuremc.block.villagepillage.ScaffoldingBlock
import thedarkcolour.futuremc.registry.FBlocks


// ===============================
// Scaffolding Block
// ===============================
class ScaffoldingBlock : Block(Material.WOOD) {

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

                if (entityIn.isSneaking) {
                    entityIn.motionY = -0.08
                } else if (entityIn.motionY < 0.0) {
                    entityIn.motionY = 0.0
                }
            }
        }
    }
}


// ===============================
// Scaffolding Item
// ===============================
class ScaffoldingItem : ModeledItemBlock(FBlocks.SCAFFOLDING) {

    override fun onItemUse(
        player: EntityPlayer,
        worldIn: World,
        pos: BlockPos,
        hand: EnumHand,
        facing: EnumFacing,
        hitX: Float,
        hitY: Float,
        hitZ: Float
    ): EnumActionResult {

        var placementPos = pos

        val clickedState = worldIn.getBlockState(placementPos)

        if (!clickedState.block.isReplaceable(worldIn, placementPos)
            && clickedState.block != this.block
        ) {
            placementPos = placementPos.offset(facing)
        } else {

            if (clickedState.block != this.block
                && ScaffoldingBlock.getHorizontalDistance(worldIn, placementPos) == 7
            ) {
                return EnumActionResult.FAIL
            }

            val direction = when {
                player.rotationPitch > 60f || player.rotationPitch < -60f -> EnumFacing.UP
                player.isSneaking -> player.horizontalFacing
                else -> player.horizontalFacing
            }

            var i = 0
            val cursor = BlockPos.MutableBlockPos(placementPos).move(direction)
            var found = false

            while (i < 7) {
                val state = worldIn.getBlockState(cursor)

                if (state.block != this.block) {
                    if (state.block.isReplaceable(worldIn, cursor)) {
                        placementPos = cursor
                        found = true
                    }
                    break
                }

                cursor.move(direction)

                if (direction.axis.isHorizontal) {
                    i++
                }
            }

            if (!found) return EnumActionResult.FAIL
        }

        val stack = player.getHeldItem(hand)

        if (!stack.isEmpty && player.canPlayerEdit(placementPos, facing, stack)) {

            val meta = this.getMetadata(stack.metadata)

            val state = this.block.getStateForPlacement(
                worldIn,
                placementPos,
                facing,
                hitX,
                hitY,
                hitZ,
                meta,
                player,
                hand
            )

            if (placeBlockAt(stack, player, worldIn, placementPos, facing, hitX, hitY, hitZ, state)) {

                val placedState = worldIn.getBlockState(placementPos)
                val soundType = placedState.block.getSoundType(placedState, worldIn, placementPos, player)

                worldIn.playSound(
                    player,
                    placementPos,
                    soundType.placeSound,
                    SoundCategory.BLOCKS,
                    (soundType.volume + 1.0f) / 2.0f,
                    soundType.pitch * 0.8f
                )

                stack.shrink(1)
            }

            return EnumActionResult.SUCCESS
        }

        return EnumActionResult.FAIL
    }

    override fun canPlaceBlockOnSide(
        worldIn: World,
        pos: BlockPos,
        side: EnumFacing,
        player: EntityPlayer,
        stack: ItemStack
    ): Boolean {
        return true
    }
}
