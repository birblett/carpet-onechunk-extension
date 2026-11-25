package com.birblett.mixin;

import com.birblett.CarpetOneChunkSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.*;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.blending.BlendingData;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelChunk.class)
public abstract class LevelChunkMixin extends ChunkAccess {

    @Shadow
    public abstract void clearAllBlockEntities();

    public LevelChunkMixin(ChunkPos chunkPos, UpgradeData upgradeData, LevelHeightAccessor levelHeightAccessor, PalettedContainerFactory containerFactory, long inhabitedTime, LevelChunkSection @Nullable [] sections, @Nullable BlendingData blendingData) {
        super(chunkPos, upgradeData, levelHeightAccessor, containerFactory, inhabitedTime, sections, blendingData);
    }

    @Inject(method = "runPostLoad", at = @At("TAIL"))
    private void removeChunkInPost(CallbackInfo ci) {
        if (CarpetOneChunkSettings.generateEmptyChunks) {
            LevelChunkSection[] sections = this.getSections();
            for (int i = 0; i < sections.length; i++) {
                sections[i] = new LevelChunkSection(new PalettedContainer<>(Blocks.AIR.defaultBlockState(), Strategy.createForBlockStates(Block.BLOCK_STATE_REGISTRY)), sections[i].getBiomes());
            }
            this.pendingBlockEntities.clear();
            this.clearAllBlockEntities();
            this.setHeightmap(Heightmap.Types.MOTION_BLOCKING, new long[37]);
        }
    }

}
