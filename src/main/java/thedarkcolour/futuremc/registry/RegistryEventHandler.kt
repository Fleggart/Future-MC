package thedarkcolour.futuremc.registry

import net.minecraft.block.Block
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.item.Item
import net.minecraft.item.crafting.IRecipe
import net.minecraft.util.ResourceLocation
import net.minecraft.util.SoundEvent
import net.minecraftforge.client.event.TextureStitchEvent
import net.minecraftforge.event.RegistryEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.registry.EntityEntry
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import net.minecraftforge.registries.IForgeRegistryModifiable
import thedarkcolour.core.util.runOnClient
import thedarkcolour.futuremc.FutureMC
import thedarkcolour.futuremc.block.villagepillage.CampfireBlock
import thedarkcolour.futuremc.client.particle.CampfireParticle
import thedarkcolour.futuremc.client.particle.SoulFlameParticle
import thedarkcolour.futuremc.compat.checkBetterWithMods
import thedarkcolour.futuremc.config.FConfig.useVanillaCreativeTabs
import thedarkcolour.futuremc.item.ItemGroup

object RegistryEventHandler {
    @SubscribeEvent
    fun onBlockRegistry(event: RegistryEvent.Register<Block>) {
        FutureMC.GROUP = if (useVanillaCreativeTabs) CreativeTabs.MISC else ItemGroup

        FBlocks.registerBlocks(event.registry)

        checkBetterWithMods()?.addHeatSource(1, FBlocks.CAMPFIRE.blockState.validStates.filter { state ->
            state.getValue(CampfireBlock.LIT)
        })
    }

    @SubscribeEvent
    fun onItemRegistry(event: RegistryEvent.Register<Item>) {
        FItems.registerItems(event.registry)
    }

    @SubscribeEvent
    fun onEntityRegistry(event: RegistryEvent.Register<EntityEntry>) {
        FEntities.registerEntities()
        runOnClient { FEntities.registerEntityRenderers() }
    }

    @SubscribeEvent
    fun registerRecipes(event: RegistryEvent.Register<IRecipe>) {
        FRecipes.registerRecipes(event.registry as IForgeRegistryModifiable<IRecipe>)
    }

    @SubscribeEvent
    fun registerSounds(event: RegistryEvent.Register<SoundEvent>) {
        FSounds.registerSounds(event.registry)
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    fun onTextureStitchEventPre(event: TextureStitchEvent.Pre) {
        CampfireParticle.textures = arrayOf(
            ResourceLocation(FutureMC.ID, "particles/big_smoke_0"),
            ResourceLocation(FutureMC.ID, "particles/big_smoke_1"),
            ResourceLocation(FutureMC.ID, "particles/big_smoke_2"),
            ResourceLocation(FutureMC.ID, "particles/big_smoke_3"),
            ResourceLocation(FutureMC.ID, "particles/big_smoke_4"),
            ResourceLocation(FutureMC.ID, "particles/big_smoke_5"),
            ResourceLocation(FutureMC.ID, "particles/big_smoke_6"),
            ResourceLocation(FutureMC.ID, "particles/big_smoke_7"),
            ResourceLocation(FutureMC.ID, "particles/big_smoke_8"),
            ResourceLocation(FutureMC.ID, "particles/big_smoke_9"),
            ResourceLocation(FutureMC.ID, "particles/big_smoke_10"),
            ResourceLocation(FutureMC.ID, "particles/big_smoke_11")
        ).map { event.map.registerSprite(it) }.toTypedArray()

        SoulFlameParticle.texture = event.map.registerSprite(ResourceLocation(FutureMC.ID, "particles/soul_fire_flame"))
    }
}
