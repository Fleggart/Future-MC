package thedarkcolour.futuremc.item

import net.minecraft.block.Block
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

/**
 * 脚手架物品
 * 改进：引入视角判断，免去原版频繁按潜行键向上搭建的繁琐操作。
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

        // 确定初始判定位置
        val clickedState = worldIn.getBlockState(placementPos)
        if (!clickedState.block.isReplaceable(worldIn, placementPos) && clickedState.block != this.block) {
            placementPos = placementPos.offset(facing)
        } else {
            if (clickedState.block != this.block && ScaffoldingBlock.getHorizontalDistance(worldIn, placementPos) == 7) {
                return EnumActionResult.FAIL
            }

            // 根据玩家视角或按键决定延伸方向
            val direction = when {
                // 视角偏高/偏低时：向上搭建
                player.rotationPitch > 60f || player.rotationPitch < -60f -> EnumFacing.UP
                // 水平或潜行时：朝玩家面对的方向水平延伸
                else -> player.horizontalFacing
            }

            // 沿选定方向寻找最近的可替换方块（最多延伸7格）
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
                if (direction.axis.isHorizontal) i++
            }

            if (!found) return EnumActionResult.FAIL
        }

        // 执行方块放置与音效播放
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
    ): Boolean = true

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
