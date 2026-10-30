package com.birblett.mixin;

import com.birblett.CarpetOneChunkSettings;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Zombie.class)
public class ZombieMixin {

    @WrapOperation(method = "populateDefaultEquipmentSlots", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/zombie/Zombie;setItemSlot(Lnet/minecraft/world/entity/EquipmentSlot;Lnet/minecraft/world/item/ItemStack;)V"))
    private void replaceSpearEquip(Zombie entity, EquipmentSlot equipmentSlot, ItemStack itemStack, Operation<Void> original) {
        original.call(entity, equipmentSlot,
                switch(CarpetOneChunkSettings.spearSpawnOverride) {
                    case "sword" -> Items.IRON_SWORD.getDefaultInstance();
                    case "shovel" -> Items.IRON_SHOVEL.getDefaultInstance();
                    default -> itemStack;
                });
    }

}
