package com.birblett.mixin;

import com.birblett.CarpetOneChunkSettings;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(MapItemSavedData.class)
public class MapItemSavedDataMixin {

    @Shadow public byte[] colors;
    @Shadow private void setColorsDirty(int x, int y) {
        throw new AssertionError();
    }

    @Inject(method = "updateColor", at = @At("HEAD"), cancellable = true)
    private void updateColorScaled(int x, int z, byte color, CallbackInfoReturnable<Boolean> cir) {
        if (CarpetOneChunkSettings.forceSmallMap) {
            if (x > 55 && x < 72 && z > 55 && z < 72) {
                byte b = this.colors[(x - 56) * 8 + (z - 56) * 1024];
                if (b != color) {
                    ((MapItemSavedData) (Object) this).setColor(x, z, color);
                    cir.setReturnValue(true);
                    return;
                }
            }
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "setColor", at = @At("HEAD"), cancellable = true)
    private void setColorScaled(int x, int z, byte color, CallbackInfo ci) {
        if (CarpetOneChunkSettings.forceSmallMap) {
            if (x > 55 && x < 72 && z > 55 && z < 72) {
                x -= 56;
                z -= 56;
                for (int sx = x * 8; sx < x * 8 + 8; ++sx) {
                    for (int sz = z * 8; sz < z * 8 + 8; ++sz) {
                        this.colors[sx + sz * 128] = color;
                        this.setColorsDirty(sx, sz);
                    }
                }
            }
            ci.cancel();
        }
    }

    @Inject(method = "addDecoration", at = @At("HEAD"), cancellable = true)
    private void cancelDecor(Holder<MapDecorationType> type, @Nullable LevelAccessor world, String key, double x, double z, double rotation, @Nullable Component text, CallbackInfo ci) {
        if (CarpetOneChunkSettings.forceSmallMap) {
            ci.cancel();
        }
    }

    @ModifyArgs(method = "Lnet/minecraft/world/level/saveddata/maps/MapItemSavedData;createFresh(DDBZZLnet/minecraft/resources/ResourceKey;)Lnet/minecraft/world/level/saveddata/maps/MapItemSavedData;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/saveddata/maps/MapItemSavedData;<init>(IIBZZZLnet/minecraft/resources/ResourceKey;)V"))
    private static void createFreshCenter(Args args, @Local(argsOnly = true, ordinal = 0) double centerX, @Local(argsOnly = true, ordinal = 1) double centerZ, @Local(argsOnly = true) byte scale) {
        if (CarpetOneChunkSettings.forceSmallMap) {
            if (scale == 127) {
                args.set(0, Mth.floor(centerX));
                args.set(1, Mth.floor(centerZ));
            } else {
                args.set(0, Mth.floor(centerX / 16) * 16 + 8);
                args.set(1, Mth.floor(centerZ / 16) * 16 + 8);
            }
        }
        args.set(2, scale == 127 ? 0 : scale);
    }

}
