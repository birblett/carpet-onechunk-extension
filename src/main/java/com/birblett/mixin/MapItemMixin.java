package com.birblett.mixin;

import com.birblett.CarpetOneChunkSettings;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.MapColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MapItem.class)
public class MapItemMixin {

    @WrapOperation(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/material/MapColor;getPackedId(Lnet/minecraft/world/level/material/MapColor$Brightness;)B"))
    private byte modifyColor(MapColor instance, MapColor.Brightness brightness, Operation<Byte> original, @Local(name = "imgY") int imgY, @Local(name = "averagingAreaMinX") int averagingAreaMinX, @Local(name = "averagingAreaMinZ") int averagingAreaMinZ, @Local(name = "blockPos") BlockPos.MutableBlockPos blockPos, @Local(argsOnly = true) Level level) {
        if (CarpetOneChunkSettings.forceSmallMap) {
            byte color;
            ChunkAccess chunk = level.getChunk(blockPos);
            blockPos.set(averagingAreaMinX, 0, averagingAreaMinZ);
            blockPos.setY(level.getHeight(Heightmap.Types.MOTION_BLOCKING, blockPos) - 1);
            if (CarpetOneChunkSettings.mapGlassAlwaysTransparent && chunk.getBlockState(blockPos).is(Blocks.GLASS)) {
                color = MapColor.NONE.getPackedId(brightness);
            } else {
                if (imgY == 56) {
                    blockPos.set(averagingAreaMinX, 0, averagingAreaMinZ - 1);
                    chunk = level.getChunk(blockPos);
                    int columnY = chunk.getHeight(Heightmap.Types.WORLD_SURFACE, blockPos.getX(), blockPos.getZ()) + 1;
                    if (columnY > level.getMinY()) {
                        do {
                            blockPos.setY(--columnY);
                        } while (chunk.getBlockState(blockPos).getMapColor(level, blockPos) == MapColor.NONE && columnY > level.getMinY());
                    }
                    if (columnY <= level.getMinY()) {
                        brightness = MapColor.Brightness.NORMAL;
                    }
                }
                color = original.call(instance, brightness);
            }
            return color;
        }
        return original.call(instance, brightness);
    }

}
