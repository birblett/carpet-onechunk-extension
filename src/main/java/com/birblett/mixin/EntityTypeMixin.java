package com.birblett.mixin;

import com.birblett.entity_tracker.TrackedEntity;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EntityType.class)
public class EntityTypeMixin {

    @ModifyExpressionValue(method = "create(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/EntitySpawnReason;)Lnet/minecraft/world/entity/Entity;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/EntityType;create(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/EntitySpawnRequest;)Lnet/minecraft/world/entity/Entity;"))
    private Entity spawnHook(@Nullable Entity original, @Local(argsOnly = true) EntitySpawnReason reason) {
        if (original != null) {
            ((TrackedEntity) original).carpet_onechunk_extension$setSpawnReason(reason);
        }
        return original;
    }

}
