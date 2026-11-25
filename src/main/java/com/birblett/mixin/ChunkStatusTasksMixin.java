package com.birblett.mixin;

import com.birblett.CarpetOneChunkSettings;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.*;
import net.minecraft.world.level.chunk.status.ChunkStatusTasks;
import net.minecraft.world.level.levelgen.Heightmap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ChunkStatusTasks.class)
public class ChunkStatusTasksMixin {

    @ModifyExpressionValue(method = "lambda$full$0(Lnet/minecraft/world/level/chunk/ChunkAccess;Lnet/minecraft/world/level/chunk/status/WorldGenContext;Lnet/minecraft/server/level/GenerationChunkHolder;)Lnet/minecraft/world/level/chunk/ChunkAccess;", at = @At(value = "NEW", target = "net/minecraft/world/level/chunk/LevelChunk"))
    private static LevelChunk replaceChunk(LevelChunk chunk) {
        if (CarpetOneChunkSettings.generateEmptyChunks) {
            LevelChunkSection[] sections = chunk.getSections();
            for (int i = 0; i < sections.length; i++) {
                sections[i] = new LevelChunkSection(new PalettedContainer<>(Blocks.AIR.defaultBlockState(), Strategy.createForBlockStates(Block.BLOCK_STATE_REGISTRY)), sections[i].getBiomes());
            }
            chunk.clearAllBlockEntities();
            chunk.setHeightmap(Heightmap.Types.MOTION_BLOCKING, new long[37]);
        }
        return chunk;
    }

}
