package com.birblett.mixin;

import com.birblett.CarpetOneChunkSettings;
import com.birblett.util.VaultCooldownHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.vault.VaultBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VaultBlockEntity.class)
public abstract class VaultBlockEntityMixin extends BlockEntity implements VaultCooldownHolder {

    @Unique
    private long customCooldownEndTick = 0L;

    public VaultBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public long getCustomCooldownEnd() {
        return this.customCooldownEndTick;
    }

    @Override
    public void setCustomCooldownEnd(long tick) {
        this.customCooldownEndTick = tick;
    }

    @Inject(method = "saveAdditional", at = @At("TAIL"))
    protected void injectSaveAdditional(ValueOutput output, CallbackInfo ci) {
        if(CarpetOneChunkSettings.vaultReworkEnabled)
            output.putLong("CustomCooldownEnd", this.customCooldownEndTick);
    }

    @Inject(method = "loadAdditional", at = @At("TAIL"))
    protected void injectLoadAdditional(ValueInput input, CallbackInfo ci) {
        if(CarpetOneChunkSettings.vaultReworkEnabled)
            this.customCooldownEndTick = input.getLongOr("CustomCooldownEnd", 0L);
    }
}