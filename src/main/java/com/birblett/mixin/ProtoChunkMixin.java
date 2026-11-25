package com.birblett.mixin;

import com.birblett.CarpetOneChunkSettings;
import com.birblett.entity_tracker.TrackedEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.chunk.ProtoChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ProtoChunk.class)
public class ProtoChunkMixin {

    @Inject(method = "addEntity(Lnet/minecraft/world/entity/Entity;)V", at = @At("HEAD"), cancellable = true)
    private void removeNonStructureEntities(Entity entity, CallbackInfo ci) {
        if (CarpetOneChunkSettings.generateEmptyChunks && CarpetOneChunkSettings.generateEmptyChunksPreservesEntities && ((TrackedEntity) entity).carpet_onechunk_extension$getSpawnReason() != null && ((TrackedEntity) entity).carpet_onechunk_extension$getSpawnReason() != EntitySpawnReason.STRUCTURE) {
            ci.cancel();
        }
    }

}
