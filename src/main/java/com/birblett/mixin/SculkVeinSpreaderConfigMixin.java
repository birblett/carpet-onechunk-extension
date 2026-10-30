package com.birblett.mixin;

import com.birblett.CarpetOneChunkSettings;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SculkVeinBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SculkVeinBlock.SculkVeinSpreaderConfig.class)
public class SculkVeinSpreaderConfigMixin {

    @WrapOperation(method = "stateCanBeReplaced", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;is(Ljava/lang/Object;)Z"))
    private boolean noSculkBarrierSpread(BlockState state, Object block, Operation<Boolean> original) {
        return CarpetOneChunkSettings.noBarrierSpread && state.is(Blocks.BARRIER) || original.call(state, block);
    }

}
