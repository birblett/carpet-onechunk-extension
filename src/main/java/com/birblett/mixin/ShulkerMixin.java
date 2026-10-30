package com.birblett.mixin;

import com.birblett.CarpetOneChunkSettings;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Shulker.class)
public class ShulkerMixin {

    @WrapOperation(method = "canStayAt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;loadedAndEntityCanStandOnFace(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/core/Direction;)Z"))
    private boolean noShulkerBarrierTeleport(Level world, BlockPos pos, Entity entity, Direction direction, Operation<Boolean> original) {
        if (CarpetOneChunkSettings.noBarrierTeleport) {
            if (!world.isInValidBounds(pos)) {
                return false;
            } else {
                ChunkAccess chunk = world.getChunk(SectionPos.blockToSectionCoord(pos.getX()), SectionPos.blockToSectionCoord(pos.getZ()), ChunkStatus.FULL, false);
                if (chunk == null) {
                    return false;
                }
                BlockState state = chunk.getBlockState(pos);
                return !state.is(Blocks.BARRIER) && state.entityCanStandOnFace(world, pos, entity, direction);
            }
        } else {
            return original.call(world, pos, entity, direction);
        }
    }

}
