package com.birblett.mixin;

import carpet.CarpetSettings;
import com.birblett.CarpetOneChunkSettings;
import com.birblett.entity_tracker.EntityTrackerState;
import com.birblett.entity_tracker.TrackedEntity;
import com.birblett.util.WebhookUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerLevel.class)
public class ServerLevelMixin {

    @Unique
    private String surroundWith(String s, String surround) {
        return surround + s + surround;
    }

    @Unique
    private String deformatted(MutableComponent message) {
        StringBuilder s = new StringBuilder();
        for (Component c : message.getSiblings()) {
            String tmp = c.getString();
            Style style = c.getStyle();
            if (style.isItalic()) {
                tmp = surroundWith(tmp, "*");
            }
            if (style.isBold()) {
                tmp = surroundWith(tmp, "**");
            }
            if (style.isUnderlined()) {
                tmp = surroundWith(tmp, "__");
            }
            if (style.isStrikethrough()) {
                tmp = surroundWith(tmp, "~~");
            }
            s.append(tmp);
        }
        return s.toString();
    }

    @Inject(method = "addFreshEntity", at = @At("HEAD"))
    private void entitySpawnHook(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (entity != null && entity.level() instanceof ServerLevel level &&
                ((TrackedEntity) entity).carpet_onechunk_extension$getSpawnReason() instanceof EntitySpawnReason reason &&
                reason != EntitySpawnReason.LOAD &&
                reason != EntitySpawnReason.DIMENSION_TRAVEL &&
                EntityTrackerState.getOrCreate(level.getServer()).getMatchMessage(entity) instanceof MutableComponent message) {
            CommandSourceStack source = level.getServer().createCommandSourceStack();
            source.getServer().getPlayerList().broadcastSystemMessage(message, false);
            String s = deformatted(message);
            CarpetSettings.LOG.info(s);
            if (!CarpetOneChunkSettings.entityTrackerWebhook.equals("none")) {
                WebhookUtils.sendDiscordMessage(CarpetOneChunkSettings.entityTrackerWebhook, s);
            }
        }
    }

}
