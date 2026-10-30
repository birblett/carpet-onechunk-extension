package com.birblett.entity_tracker;

import net.minecraft.world.entity.EntitySpawnReason;

public interface TrackedEntity {

    void carpet_onechunk_extension$setSpawnReason(EntitySpawnReason reason);
    EntitySpawnReason carpet_onechunk_extension$getSpawnReason();

}
