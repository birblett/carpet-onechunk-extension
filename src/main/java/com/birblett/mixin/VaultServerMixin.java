package com.birblett.mixin;

import com.birblett.CarpetOneChunkSettings;
import com.birblett.util.VaultCooldownHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.vault.VaultConfig;
import net.minecraft.world.level.block.entity.vault.VaultServerData;
import net.minecraft.world.level.block.entity.vault.VaultSharedData;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.world.level.block.entity.vault.VaultBlockEntity$Server")
public class VaultServerMixin {

    // 5 minutes
    @Inject(
            method = "tryInsertKey",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void onTryInsertKey(
            ServerLevel serverLevel,
            BlockPos blockPos,
            BlockState blockState,
            VaultConfig vaultConfig,
            VaultServerData vaultServerData,
            VaultSharedData vaultSharedData,
            Player player,
            ItemStack itemStack,
            CallbackInfo ci
    ) {
        if(!CarpetOneChunkSettings.vaultReworkEnabled) return;

        BlockEntity blockEntity = serverLevel.getBlockEntity(blockPos);
        if (blockEntity instanceof VaultCooldownHolder vault) {
            long currentTime = serverLevel.getGameTime();
            long cooldownEnd = vault.getCustomCooldownEnd();

            if (currentTime < cooldownEnd) {
                ci.cancel();
            }
        }
    }

    @Inject(
            method = "tryInsertKey",
            at = @At("TAIL")
    )
    private static void onTryInsertKeyTail(
            ServerLevel serverLevel,
            BlockPos blockPos,
            BlockState blockState,
            VaultConfig vaultConfig,
            VaultServerData vaultServerData,
            VaultSharedData vaultSharedData,
            Player player,
            ItemStack itemStack,
            CallbackInfo ci
    ) {
        if(!CarpetOneChunkSettings.vaultReworkEnabled)
            return;

        BlockEntity blockEntity = serverLevel.getBlockEntity(blockPos);
        if (blockEntity instanceof VaultCooldownHolder vault) {
            if (vault.getCustomCooldownEnd() == 0L) {
                vault.setCustomCooldownEnd(serverLevel.getGameTime() + CarpetOneChunkSettings.vaultGlobalCooldown);
                blockEntity.setChanged(); // marks block entity dirty so NBT saves properly
            }
        }
    }

    @Inject(
            method = "tick",
            at = @At("HEAD")
    )
    private static void onServerTick(
            ServerLevel serverLevel, BlockPos blockPos, BlockState blockState, VaultConfig vaultConfig, VaultServerData vaultServerData, VaultSharedData vaultSharedData, CallbackInfo ci
    ) {
        if(!CarpetOneChunkSettings.vaultReworkEnabled)
            return;

        BlockEntity blockEntity = serverLevel.getBlockEntity(blockPos);
        if (blockEntity instanceof VaultCooldownHolder vault) {
            long currentTime = serverLevel.getGameTime();
            long cooldownEnd = vault.getCustomCooldownEnd();
            var rewardedPlayers = ((VaultServerDataAccessor) (Object) vaultServerData).callGetRewardedPlayers();

            if (cooldownEnd == 0L && !rewardedPlayers.isEmpty()) {
                vault.setCustomCooldownEnd(currentTime + CarpetOneChunkSettings.vaultGlobalCooldown);
                blockEntity.setChanged();
                return;
            }

            if (cooldownEnd > 0L && currentTime >= cooldownEnd) {
                if (!rewardedPlayers.isEmpty()) {
                    rewardedPlayers.clear();
                    blockEntity.setChanged();
                }
                vault.setCustomCooldownEnd(0L);
            }
        }
    }
}