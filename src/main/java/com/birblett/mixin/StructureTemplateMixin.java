package com.birblett.mixin;

import carpet.CarpetSettings;
import com.birblett.CarpetOneChunkSettings;
import com.birblett.entity_tracker.EntityTrackerState;
import com.birblett.util.TextUtils;
import com.birblett.util.WebhookUtils;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnRequest;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.storage.ValueInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

@Mixin(StructureTemplate.class)
public class StructureTemplateMixin {

    @WrapOperation(method = "createEntityIgnoreException", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/EntityType;create(Lnet/minecraft/world/level/storage/ValueInput;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/EntitySpawnRequest;)Ljava/util/Optional;"))
    private static Optional<Entity> getEntity(ValueInput input, Level level, EntitySpawnRequest request, Operation<Optional<Entity>> original) {
        Optional<Entity> e = original.call(input, level, request);
        e.ifPresent(entity -> {
            if (EntityTrackerState.getOrCreate(level.getServer()).getMatchMessage(entity) instanceof MutableComponent message) {
                CommandSourceStack source = level.getServer().createCommandSourceStack();
                source.getServer().getPlayerList().broadcastSystemMessage(message, false);
                String s = TextUtils.deformatted(message);
                CarpetSettings.LOG.info(s);
                if (!CarpetOneChunkSettings.entityTrackerWebhook.equals("none")) {
                    WebhookUtils.sendDiscordMessage(CarpetOneChunkSettings.entityTrackerWebhook, s);
                }
            }
        });
        return e;
    }

}
