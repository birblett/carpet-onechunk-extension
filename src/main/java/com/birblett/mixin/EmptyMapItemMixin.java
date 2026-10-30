package com.birblett.mixin;

import com.birblett.CarpetOneChunkSettings;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.EmptyMapItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(EmptyMapItem.class)
public class EmptyMapItemMixin {

    @ModifyArg(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/MapItem;create(Lnet/minecraft/server/level/ServerLevel;IIBZZ)Lnet/minecraft/world/item/ItemStack;"))
    private byte funnyMagicNumber(byte b, @Local(argsOnly = true) Player user) {
        return CarpetOneChunkSettings.forceSmallMap && user.isShiftKeyDown() ? 127 : b;
    }

}
