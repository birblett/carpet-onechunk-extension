package com.birblett.mixin;

import com.birblett.entity_tracker.TrackedEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Entity.class)
public class EntityMixin implements TrackedEntity {

    private EntitySpawnReason reason = null;


    @Override
    public void carpet_onechunk_extension$setSpawnReason(EntitySpawnReason reason) {
        this.reason = reason;
    }

    @Override
    public EntitySpawnReason carpet_onechunk_extension$getSpawnReason() {
        return this.reason;
    }
}
