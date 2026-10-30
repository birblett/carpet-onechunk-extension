package com.birblett.mixin;

import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

@Mixin(EntitySelector.class)
public interface EntitySelectorAccessor {

    @Accessor("contextFreePredicates")
    List<Predicate<Entity>> getContextFreePredicates();

    @Accessor("position")
    Function<Vec3, Vec3> getRelativePosition();

    @Invoker("getPredicate")
    Predicate<Entity> getEntityPredicate(Vec3 pos, @Nullable AABB absoluteAabb, @Nullable FeatureFlagSet enabledFeatures);

    @Invoker("getAbsoluteAabb")
    AABB getAABB(Vec3 pos);

}
