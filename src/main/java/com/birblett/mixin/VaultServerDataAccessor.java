package com.birblett.mixin;

import net.minecraft.world.level.block.entity.vault.VaultServerData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Set;
import java.util.UUID;

@Mixin(VaultServerData.class)
public interface VaultServerDataAccessor {
    @Invoker("getRewardedPlayers")
    Set<UUID> callGetRewardedPlayers();
}
