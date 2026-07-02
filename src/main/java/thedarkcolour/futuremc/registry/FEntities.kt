package thedarkcolour.futuremc.registry

import net.minecraft.entity.Entity
import net.minecraft.entity.EnumCreatureType
import net.minecraft.init.Biomes
import net.minecraft.util.ResourceLocation
import net.minecraftforge.fml.common.registry.EntityRegistry
import thedarkcolour.core.util.registerEntity
import thedarkcolour.core.util.registerEntityModel
import thedarkcolour.futuremc.FutureMC
import thedarkcolour.futuremc.config.FConfig
import thedarkcolour.futuremc.entity.bee.BeeRenderer
import thedarkcolour.futuremc.entity.bee.EntityBee
import thedarkcolour.futuremc.entity.panda.EntityPanda
import thedarkcolour.futuremc.entity.panda.RenderPanda

object FEntities {
    fun registerEntities() {
        if (FConfig.villageAndPillage.panda && FConfig.villageAndPillage.bamboo.enabled) {
            registerEntity("panda", EntityPanda::class.java, 36, 3, 15198183, 1776418)
            EntityRegistry.addSpawn(EntityPanda::class.java, 14, 1, 2, EnumCreatureType.CREATURE, Biomes.JUNGLE, Biomes.JUNGLE_HILLS, Biomes.MUTATED_JUNGLE)
        }
        if (FConfig.buzzyBees.bee.enabled) {
            registerEntity("bee", EntityBee::class.java, 32, 4, 16770398, 2500144)
        }
    }

    fun registerEntityRenderers() {
        if (FConfig.villageAndPillage.panda && FConfig.villageAndPillage.bamboo.enabled) {
            registerEntityModel { RenderPanda(it) }
        }
        if (FConfig.buzzyBees.bee.enabled) {
            registerEntityModel { BeeRenderer(it) }
        }
    }

    fun registerEntity(name: String, entity: Class<out Entity>, trackingRange: Int, id: Int) {
        EntityRegistry.registerModEntity(ResourceLocation(FutureMC.ID, name), entity, name, id, FutureMC, trackingRange, 1, true)
    }
}
