package thedarkcolour.futuremc.item

import net.minecraft.block.Block
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.util.EnumActionResult
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.SoundCategory
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import thedarkcolour.core.item.ModeledItemBlock
import thedarkcolour.futuremc.block.villagepillage.ScaffoldingBlock
import thedarkcolour.futuremc.registry.FBlocks

/**
 * 脚手架物品
 * 
 * 放置逻辑（相较于原版 1.14+ 的改进）：
 * - 视角朝上（>60°）或朝下（<-60°）→ 向上搭
 * - 视角水平 → 水平延伸
 * - 潜行 + 视角水平 → 水平延伸（精细控制）
 * 
 * 这样玩家不需要一直按着潜行键来向上搭，
 * 只需要控制视角方向即可。
 */
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

        // 确定放置位置
        val clickedState = worldIn.getBlockState(placementPos)
        if (!clickedState.block.isReplaceable(worldIn, placementPos) && clickedState.block != this.block) {
            // 如果点击的是不可替换方块，在点击面偏移
            placementPos = placementPos.offset(facing)
        } else {
            // 如果点击的是脚手架或可替换方块
            if (clickedState.block != this.block && ScaffoldingBlock.getHorizontalDistance(worldIn, placementPos) == 7) {
                return EnumActionResult.FAIL
            }

            // ============================================================
            // ✅ 改进：根据视角角度决定放置方向
            // 不再需要强制按潜行才能向上搭
            // ============================================================
            val direction = when {
                // 视角朝上或朝下 → 向上搭（建造高空/地面脚手架）
                player.rotationPitch > 60f || player.rotationPitch < -60f -> EnumFacing.UP

                // 潜行时 → 水平延伸（用于精细搭建，可精确控制位置）
                player.isSneaking -> player.horizontalFacing

                // 默认 → 水平延伸（视角平时自动水平）
                else -> player.horizontalFacing
            }

            // 沿着方向寻找可放置位置（最多7格）
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

            if (!found) {
                return EnumActionResult.FAIL
            }
        }

        // 放置脚手架
        val stack = player.getHeldItem(hand)
        if (!stack.isEmpty && player.canPlayerEdit(placementPos, facing, stack)) {
            val meta = this.getMetadata(stack.metadata)
            val state = this.block.getStateForPlacement(
                worldIn, placementPos, facing, hitX, hitY, hitZ, meta, player, hand
            )

            if (placeBlockAt(stack, player, worldIn, placementPos, facing, hitX, hitY, hitZ, state)) {
                val placedState = worldIn.getBlockState(placementPos)
                val soundType = placedState.block.getSoundType(placedState, worldIn, placementPos, player)
                worldIn.playSound(
                    player,
                    placementPos,
                    soundType.placeSound,
                    SoundCategory.BLOCKS,
                    (soundType.getVolume() + 1.0f) / 2.0f,
                    soundType.getPitch() * 0.8f
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

    /**
     * 检查方块是否可以放置
     * 已移除无用的 worldBorder 注释代码
     */
    private fun canPlaceIgnoreBlockCheck(
        level: World,
        blockIn: Block,
        pos: BlockPos,
        skipCollisionCheck: Boolean,
        sidePlacedOn: EnumFacing,
        placer: EntityPlayer?
    ): Boolean {
        val state = level.getBlockState(pos)
        val bounds = if (skipCollisionCheck) null else this.block.defaultState.getCollisionBoundingBox(level, pos)

        if (bounds != null && !level.checkNoEntityCollision(bounds.offset(pos), placer)) {
            return false
        }

        return state.block.isReplaceable(level, pos)
    }
}
