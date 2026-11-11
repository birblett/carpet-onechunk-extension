package com.birblett.mixin;

import com.birblett.entity_tracker.EntityTrackerState;
import com.birblett.entity_tracker.TrackedEntity;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerLevel.class)
public class ServerLevelMixin {

    @Inject(method = "addFreshEntity", at = @At("HEAD"))
    private void entitySpawnHook(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (entity != null && entity.level() instanceof ServerLevel level &&
                ((TrackedEntity) entity).carpet_onechunk_extension$getSpawnReason() instanceof EntitySpawnReason reason &&
                reason != EntitySpawnReason.LOAD &&
                reason != EntitySpawnReason.DIMENSION_TRAVEL &&
                EntityTrackerState.getOrCreate(level.getServer()).getMatchMessage(entity) instanceof MutableComponent message) {
            CommandSourceStack source = level.getServer().createCommandSourceStack();
            source.getServer().getPlayerList().broadcastChatMessage(PlayerChatMessage.system("").withUnsignedContent(message),
                    source, ChatType.bind(ChatType.SAY_COMMAND, source));
        }
    }

}
