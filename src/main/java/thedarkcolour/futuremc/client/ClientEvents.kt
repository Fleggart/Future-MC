package thedarkcolour.futuremc.client

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiMerchant
import net.minecraft.client.renderer.block.model.ModelResourceLocation
import net.minecraft.item.Item
import net.minecraftforge.client.event.GuiOpenEvent
import net.minecraftforge.client.event.ModelRegistryEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import thedarkcolour.futuremc.client.gui.GuiVillager
import thedarkcolour.futuremc.config.FConfig
import thedarkcolour.futuremc.container.ContainerVillager

object ClientEvents {
    val models = ArrayList<Triple<Item, Int, String>>()

    @SubscribeEvent
    fun onGuiOpen(event: GuiOpenEvent) {
        if (FConfig.villageAndPillage.newVillagerGui) {
            val gui = event.gui

            if (gui is GuiMerchant && gui !is GuiVillager) {
                event.gui = GuiVillager(ContainerVillager(Minecraft.getMinecraft().player.inventory, gui.merchant, null))
            }
        }
    }

    @SubscribeEvent
    fun onModelRegistry(event: ModelRegistryEvent) {
        for (item in models) {
            ModelLoader.setCustomModelResourceLocation(item.first, item.second, ModelResourceLocation(item.third, "inventory"))
        }
    }
}
