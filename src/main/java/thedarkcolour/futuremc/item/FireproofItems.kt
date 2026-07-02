package thedarkcolour.futuremc.item

import com.google.common.collect.HashMultimap
import com.google.common.collect.Multimap
import net.minecraft.block.Block
import net.minecraft.entity.FireproofItemLogic
import net.minecraft.entity.SharedMonsterAttributes
import net.minecraft.entity.ai.attributes.AttributeModifier
import net.minecraft.entity.item.EntityItem
import net.minecraft.inventory.EntityEquipmentSlot
import net.minecraft.item.*
import thedarkcolour.core.item.ModeledItem
import thedarkcolour.core.item.ModeledItemBlock
import thedarkcolour.core.util.setItemModel
import thedarkcolour.core.util.setItemName
import java.util.UUID

/**
 * 防火物品块 - 掉入岩浆或火中时不会被烧毁
 */
class FireproofItemBlock(block: Block) : ModeledItemBlock(block) {
    override fun onEntityItemUpdate(entity: EntityItem): Boolean {
        FireproofItemLogic.update(entity)
        return true
    }
}

/**
 * 防火物品 - 掉入岩浆或火中时不会被烧毁
 */
class FireproofItem(regName: String) : ModeledItem(regName) {
    override fun onEntityItemUpdate(entity: EntityItem): Boolean {
        FireproofItemLogic.update(entity)
        return true
    }
}

/**
 * 防火斧头
 */
class FireproofAxeItem(
    regName: String,
    material: ToolMaterial,
    damage: Float,
    speed: Float
) : ItemAxe(material, damage, speed) {
    init {
        setItemName(this, regName)
        setItemModel(this, 0)
    }

    override fun onEntityItemUpdate(entity: EntityItem): Boolean {
        FireproofItemLogic.update(entity)
        return true
    }
}

/**
 * 防火锄头
 */
class FireproofHoeItem(regName: String, material: ToolMaterial) : ItemHoe(material) {
    init {
        setItemName(this, regName)
        setItemModel(this, 0)
    }

    override fun onEntityItemUpdate(entity: EntityItem): Boolean {
        FireproofItemLogic.update(entity)
        return true
    }
}

/**
 * 防火镐
 */
class FireproofPickaxeItem(regName: String, material: ToolMaterial) : ItemPickaxe(material) {
    init {
        setItemName(this, regName)
        setItemModel(this, 0)
    }

    override fun onEntityItemUpdate(entity: EntityItem): Boolean {
        FireproofItemLogic.update(entity)
        return true
    }
}

/**
 * 防火锹
 */
class FireproofShovelItem(regName: String, material: ToolMaterial) : ItemSpade(material) {
    init {
        setItemName(this, regName)
        setItemModel(this, 0)
    }

    override fun onEntityItemUpdate(entity: EntityItem): Boolean {
        FireproofItemLogic.update(entity)
        return true
    }
}

/**
 * 防火剑
 */
class FireproofSwordItem(regName: String, material: ToolMaterial) : ItemSword(material) {
    init {
        setItemName(this, regName)
        setItemModel(this, 0)
    }

    override fun onEntityItemUpdate(entity: EntityItem): Boolean {
        FireproofItemLogic.update(entity)
        return true
    }
}

/**
 * 下界合金盔甲
 * 
 * 相较于普通盔甲，下界合金盔甲额外提供：
 * - 击退抗性 +0.1（每件）
 * - 防火（物品形态掉入岩浆不会被烧毁）
 */
open class NetheriteArmorItem(
    regName: String,
    materialIn: ArmorMaterial,
    equipmentSlotIn: EntityEquipmentSlot
) : ItemArmor(materialIn, 0, equipmentSlotIn) {

    companion object {
        /**
         * 专门用于下界合金盔甲击退抗性的唯一标识符
         * 注意：这个 UUID 不能与任何其他修饰符重复
         */
        private val KNOCKBACK_RESISTANCE_UUID = UUID.fromString("d5c8e8e0-9e3a-4b5f-9e8a-1b2c3d4e5f6a")
    }

    init {
        setItemName(this, regName)
        setItemModel(this, 0)
    }

    override fun getItemAttributeModifiers(slot: EntityEquipmentSlot): Multimap<String, AttributeModifier> {
        val map = super.getItemAttributeModifiers(slot)

        if (slot == armorType) {
            // ✅ 使用独立的 UUID，不再复用 ARMOR_MODIFIERS 的 UUID
            // 这样不会与护甲值、韧性等属性产生冲突
            map.put(
                SharedMonsterAttributes.KNOCKBACK_RESISTANCE.name,
                AttributeModifier(
                    KNOCKBACK_RESISTANCE_UUID,  // 独立的 UUID
                    "Netherite armor knockback resistance",
                    0.1,  // 每件提供 0.1 击退抗性（全套 0.4）
                    0     // 0 = 直接加成
                )
            )
        }

        return map
    }

    override fun onEntityItemUpdate(entity: EntityItem): Boolean {
        FireproofItemLogic.update(entity)
        return true
    }
}
