package com.birblett.entity_tracker;

import com.birblett.CarpetOneChunkExtension;
import com.birblett.CarpetOneChunkSettings;
import com.birblett.mixin.EntitySelectorAccessor;
import com.birblett.util.TextUtils;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.selector.EntitySelectorParser;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.util.Util;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.Nullable;
import org.lwjgl.system.SharedLibraryUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EntityTrackerState extends SavedData {

    private record EntityPredicateContainer(Predicate<Entity> basePredicate, EntitySelectorAccessor accessor, String message) {

        @Nullable private static EntityPredicateContainer fromPair(Pair<String, String> pair) {
            return fromString(pair.getFirst(), pair.getSecond());
        }

        @Nullable private static EntityPredicateContainer fromString(String selector, String message) {
            EntitySelectorParser parser = new EntitySelectorParser(new StringReader(selector), true);
            try {
                EntitySelectorAccessor a = ((EntitySelectorAccessor) parser.parse());
                return (new EntityPredicateContainer(Util.allOf(a.getContextFreePredicates()), a, message));
            } catch (CommandSyntaxException e) {
                return null;
            }
        }

    }

    private final ArrayList<Pair<String, String>> serializablePredicates = new ArrayList<>();
    private final ArrayList<EntityPredicateContainer> computedPredicates = new ArrayList<>();

    private EntityTrackerState() {}

    private EntityTrackerState(List<Pair<String, String>> seralizedPredicates) {
        this.serializablePredicates.addAll(seralizedPredicates);
        this.serializablePredicates.forEach(p -> computedPredicates.add(EntityPredicateContainer.fromPair(p)));
    }

    private List<Pair<String, String>> getTrackerState() {
        return this.serializablePredicates;
    }

    private static final Codec<EntityTrackerState> CODEC =
            Codec.list(Codec.pair(Codec.STRING.fieldOf("predicate").codec(), Codec.STRING.fieldOf("message").codec()))
                    .xmap(
                            EntityTrackerState::new,
                            EntityTrackerState::getTrackerState
                    );

    private static final SavedDataType<EntityTrackerState> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath(CarpetOneChunkExtension.MOD_ID, "entity_tracker"),
            EntityTrackerState::new,
            CODEC,
            null
    );

    public static EntityTrackerState getOrCreate(CommandContext<CommandSourceStack> context) {
        return EntityTrackerState.getOrCreate(context.getSource().getServer());
    }

    public static EntityTrackerState getOrCreate(MinecraftServer server) {
        EntityTrackerState data = server.getDataStorage().computeIfAbsent(TYPE);
        data.setDirty();
        return data;
    }

    private void add(String selector, @Nullable String message) {
        this.serializablePredicates.add(new Pair<>(selector, message));
        this.computedPredicates.add(EntityPredicateContainer.fromString(selector, message));
    }

    private void set(int index, String selector, @Nullable String message) {
        this.serializablePredicates.set(index, new Pair<>(selector, message));
        this.computedPredicates.set(index, EntityPredicateContainer.fromString(selector, message));
    }

    private static final Pattern MESSAGE_REGEX = Pattern.compile("%\\{[a-zA-Z,!_]*}");

    private static MutableComponent finalMessage(Entity e, String s, String condition) {
        if (s.isEmpty()) {
            s = CarpetOneChunkSettings.commandEntityTrackerDefaultMessage;
        }
        BlockPos p = e.blockPosition();
        MutableComponent c = TextUtils.formattable("");
        Matcher matcher = MESSAGE_REGEX.matcher(s);

        int index = 0;
        String modifier = null;

        Style style = Style.EMPTY;

        while (matcher.find()) {
            String current = s.substring(index, matcher.start());
            style = addFormattedText(e, current, condition, p, c, modifier, style);
            modifier = matcher.group(0);
            index = matcher.end();
        }
        if (index <= s.length()) {
            addFormattedText(e, s.substring(index), condition, p, c, modifier, style);
        }

        return c;
    }

    private static Style addFormattedText(Entity e, String current, String condition, BlockPos p, MutableComponent c, String modifier, Style style) {
        MutableComponent m = TextUtils.formattable(current);
        if (modifier != null) {
            String[] modifiers = modifier.replaceAll("^%\\{|}$", "").split(",");
            for (String mod : modifiers) {
                mod = mod.strip();
                switch (mod) {
                    case "type" -> c.append(TextUtils.formattable(e.getType().toShortString()).setStyle(style));
                    case "type_name" -> c.append(TextUtils.translatable(e.getType().getDescriptionId()));
                    case "name" -> c.append(e.getName().copy().setStyle(style));
                    case "x" -> c.append(TextUtils.formattable(String.valueOf(p.getX())).setStyle(style));
                    case "y" -> c.append(TextUtils.formattable(String.valueOf(p.getY())).setStyle(style));
                    case "z" -> c.append(TextUtils.formattable(String.valueOf(p.getZ())).setStyle(style));
                    case "dimension" -> c.append(TextUtils.formattable(e.level().dimension().identifier().toString()).setStyle(style));
                    case "dimension_name" -> c.append(TextUtils.translatable(e.level().dimension().identifier().toLanguageKey()).setStyle(style));
                    case "condition" -> c.append(TextUtils.formattable(condition).setStyle(style));
                    case "b", "!b" -> style = style.withBold(!mod.startsWith("!"));
                    case "i", "!i" -> style = style.withItalic(!mod.startsWith("!"));
                    case "s", "!s" -> style = style.withStrikethrough(!mod.startsWith("!"));
                    default -> {
                        DataResult<TextColor> color = TextColor.parseColor(mod);
                        color.ifSuccess(m::withColor);
                    }
                }
            }
        }
        c.append(m);
        return style;
    }

    @Nullable public MutableComponent getMatchMessage(Entity e) {
        for (int i = 0; i < this.computedPredicates.size(); i++) {
            EntityPredicateContainer container = this.computedPredicates.get(i);

            if (container.basePredicate.test(e)) {
                AABB aabb = container.accessor.getAABB(container.accessor.getRelativePosition().apply(e.position()));
                if (aabb != null) {
                    Predicate<Entity> p = e1 -> aabb.intersects(e1.getBoundingBox());
                    return p.test(e) ? finalMessage(e, container.message, this.serializablePredicates.get(i).getFirst()) : null;
                }
                return finalMessage(e, container.message,  this.serializablePredicates.get(i).getFirst());
            }
        }
        return null;
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("entity_tracker")
                        .requires(s -> s.permissions().hasPermission(Permissions.COMMANDS_ADMIN))
                        .then(Commands.literal("add")
                                .then(Commands.argument("target", EntityArgument.entities())
                                        .then(Commands.argument("message", StringArgumentType.greedyString())
                                                .executes(EntityTrackerState::addTracker))
                                        .executes(EntityTrackerState::addTracker)))
                        .then(Commands.literal("modify")
                                .then(Commands.argument("index", IntegerArgumentType.integer(0))
                                        .then(Commands.literal("selector")
                                                .then(Commands.argument("target", EntityArgument.entities())
                                                        .suggests((context, b)->
                                                                trackerSuggestionProvider(context, b, true))
                                                        .executes(EntityTrackerState::editTracker)))
                                        .then(Commands.literal("message")
                                                .then(Commands.argument("text", StringArgumentType.greedyString())
                                                        .suggests((context, b)->
                                                                trackerSuggestionProvider(context, b, false))
                                                        .executes(EntityTrackerState::editTracker)))))
                        .then(Commands.literal("remove")
                                .then(Commands.argument("index", IntegerArgumentType.integer(0))
                                        .executes(EntityTrackerState::removeTracker)))
                        .then(Commands.literal("list")
                                .executes(EntityTrackerState::list)));
    }

    private static int addTracker(CommandContext<CommandSourceStack> context) {
        String message;
        try {
            message = context.getArgument("message", String.class);
        } catch (Exception _) {
            message = "";
        }
        String selector = context.getInput().substring("entity_tracker add ".length());
        selector = selector.substring(0, selector.length() - message.length());
        EntityTrackerState state = getOrCreate(context);
        state.add(selector, message);
        return list(context);
    }

    private static CompletableFuture<Suggestions> trackerSuggestionProvider(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder, boolean isSelector) {
        EntityTrackerState state = getOrCreate(context);
        int i = context.getArgument("index", Integer.class);
        String suggestion = isSelector ? state.serializablePredicates.get(i).getFirst() : state.serializablePredicates.get(i).getSecond();
        return SharedSuggestionProvider.suggest(List.of(suggestion), builder);
    }

    private static int editTracker(CommandContext<CommandSourceStack> context) {
        int i = context.getArgument("index", Integer.class);
        if (i >= 0) {
            EntityTrackerState state = getOrCreate(context);
            boolean message = true;
            Pair<String, String> p = state.serializablePredicates.get(i);
            try {
                state.set(i, p.getFirst(), context.getArgument("message", String.class));
            } catch (Exception _) {
                message = false;
                String s = context.getInput().substring("entity_tracker modify ".length()).replaceFirst("\\D.*", "");
                s = context.getInput().substring("entity_tracker modify  selector ".length() + s.length());
                state.set(i, s, p.getSecond());
            }
            context.getSource().sendSystemMessage(TextUtils.formattable("Changed " + (message ? "message" : "selector") + " at index "
                    + i + " to \"" + state.serializablePredicates.get(i).getFirst() + "\"").withColor(TextColor.YELLOW));
            return list(context);
        } else {
            context.getSource().sendSystemMessage(TextUtils.formattable("Index " + i + " is out of bounds").withColor(TextColor.RED));
            return 1;
        }
    }

    private static int removeTracker(CommandContext<CommandSourceStack> context) {
        int i = context.getArgument("index", Integer.class);
        EntityTrackerState state = getOrCreate(context);
        if (i >= 0 && i < state.serializablePredicates.size()) {
            context.getSource().sendSystemMessage(TextUtils.formattable("Removed selector \"" + state.serializablePredicates.remove(i).getFirst() + "\"").withColor(TextColor.YELLOW));
            state.computedPredicates.remove(i);
            return 0;
        } else {
            context.getSource().sendSystemMessage(TextUtils.formattable("Index " + i + " is out of bounds").withColor(TextColor.RED));
            return 1;
        }
    }

    private static int list(CommandContext<CommandSourceStack> context) {
        EntityTrackerState state = getOrCreate(context);
        MutableComponent t = TextUtils.formattable("Active trackers:");
        for (int i = 0; i < state.serializablePredicates.size(); ++i) {
            TextColor c = state.computedPredicates.get(i) != null ? TextColor.GREEN : TextColor.RED;
            t.append(TextUtils.formattable("\n " + i + ". ")).withColor(TextColor.WHITE).append(TextUtils.formattable(state.serializablePredicates.get(i).getFirst()).withColor(c));
        }
        context.getSource().sendSystemMessage(t);
        return 0;
    }

}
