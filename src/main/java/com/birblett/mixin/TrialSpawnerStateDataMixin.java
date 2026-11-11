package com.birblett.mixin;

import java.util.Set;
import java.util.UUID;

import com.birblett.CarpetOneChunkSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawnerStateData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TrialSpawnerStateData.class)
public class TrialSpawnerStateDataMixin {

    @Shadow
    @Final
    Set<UUID> detectedPlayers;

    @Inject(method = "countAdditionalPlayers", at = @At("HEAD"), cancellable = true)
    private void overrideAdditionalPlayers(BlockPos blockPos, CallbackInfoReturnable<Integer> cir) {
        if(CarpetOneChunkSettings.vaultReworkEnabled)
            cir.setReturnValue(0);
    }

    @Inject(method = "haveAllCurrentMobsDied", at = @At("RETURN"))
    private void capRewardsOnTrialComplete(CallbackInfoReturnable<Boolean> cir) {
        if (CarpetOneChunkSettings.vaultReworkEnabled && cir.getReturnValue() && this.detectedPlayers.size() > 1) {
            UUID firstPlayer = this.detectedPlayers.iterator().next();
            this.detectedPlayers.clear();
            this.detectedPlayers.add(firstPlayer);
        }
    }

    @Shadow
    long cooldownEndsAt;

    @Inject(
            method = "isCooldownFinished",
            at = @At("HEAD"),
            cancellable = true
    )
    private void dynamicCooldownCheck(net.minecraft.server.level.ServerLevel level, CallbackInfoReturnable<Boolean> cir) {
        long currentTime = level.getGameTime();

        if (CarpetOneChunkSettings.vaultReworkEnabled) {
            long adjustedEndTick = getAdjustedEndTick();
            cir.setReturnValue(currentTime >= adjustedEndTick);
        } else {
            cir.setReturnValue(currentTime >= this.cooldownEndsAt);
        }
    }

    @Unique
    private long getAdjustedEndTick() {
        long customCooldownTicks = CarpetOneChunkSettings.trialSpawnerGlobalCooldown > 0
                ? CarpetOneChunkSettings.trialSpawnerGlobalCooldown
                : 6000L;

        long expectedStartTick = this.cooldownEndsAt - 36000L;
        return Math.min(this.cooldownEndsAt, expectedStartTick + customCooldownTicks);
    }
}
