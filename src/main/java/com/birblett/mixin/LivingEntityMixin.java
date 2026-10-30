package com.birblett.mixin;

import com.birblett.CarpetOneChunkSettings;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gamerules.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {

    public LivingEntityMixin(EntityType<?> type, Level world) {
        super(type, world);
    }

    @Inject(method = "dropExperience", at = @At("HEAD"), cancellable = true)
    private void playerNoXpDrop(ServerLevel world, Entity attacker, CallbackInfo ci) {
        if (CarpetOneChunkSettings.keepInventoryXPDeletion && world.getGameRules().get(GameRules.KEEP_INVENTORY) && ((Entity) this) instanceof ServerPlayer p) {
            p.setExperienceLevels(0);
            p.setExperiencePoints(0);
            ci.cancel();
        }
    }

    @WrapOperation(method = "randomTeleport(DDDZLjava/util/function/Predicate;)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/tags/TagKey;)Z"))
    private boolean noEndermanBarrierTeleport(BlockState state, TagKey<Block> tagKey, Operation<Boolean> original) {
        return CarpetOneChunkSettings.noBarrierTeleport && !state.is(Blocks.BARRIER) && original.call(state, tagKey);
    }
}
