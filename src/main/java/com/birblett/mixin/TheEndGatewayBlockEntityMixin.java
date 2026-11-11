package com.birblett.mixin;

import com.birblett.CarpetOneChunkSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.TheEndGatewayBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TheEndGatewayBlockEntity.class)
public class TheEndGatewayBlockEntityMixin {
    @Inject(at = @At("HEAD"), method = "findTallestBlock", cancellable = true)
    private static void findTallestBlockNoBarrier(BlockGetter level, BlockPos around, int dist, boolean allowBedrock, CallbackInfoReturnable<BlockPos> cir) {
        if(!CarpetOneChunkSettings.noBarrierGatewayTeleport) return;

        BlockPos tallest = null;

        for (int xd = -dist; xd <= dist; xd++) {
            for (int zd = -dist; zd <= dist; zd++) {
                if (xd != 0 || zd != 0 || allowBedrock) {
                    for (int y = level.getMaxY(); y > (tallest == null ? level.getMinY() : tallest.getY()); y--) {
                        BlockPos pos = new BlockPos(around.getX() + xd, y, around.getZ() + zd);
                        BlockState state = level.getBlockState(pos);
                        if (state.isCollisionShapeFullBlock(level, pos) && (allowBedrock || !state.is(Blocks.BEDROCK)) && !state.is(Blocks.BARRIER)) {
                            tallest = pos;
                            break;
                        }
                    }
                }
            }
        }

        cir.setReturnValue(tallest == null ? around : tallest);
    }
}

