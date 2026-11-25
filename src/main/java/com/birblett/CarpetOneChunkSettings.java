package com.birblett;

import carpet.CarpetSettings;
import carpet.api.settings.CarpetRule;
import carpet.api.settings.Rule;
import carpet.api.settings.Validator;
import carpet.api.settings.Validators;
import carpet.utils.CommandHelper;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.permissions.LevelBasedPermissionSet;
import net.minecraft.server.permissions.PermissionCheck;

import java.net.MalformedURLException;
import java.net.URI;

import static carpet.api.settings.RuleCategory.COMMAND;
import static carpet.api.settings.RuleCategory.FEATURE;

public class CarpetOneChunkSettings {

    private static final String ONECHUNK = "onechunk";

    @Rule(
            categories = {FEATURE, ONECHUNK}
    )
    public static boolean forceSmallMap = false;

    @Rule(
            categories = {FEATURE, ONECHUNK}
    )
    public static boolean generateEmptyChunks = true;

    @Rule(
            categories = {FEATURE, ONECHUNK}
    )
    public static boolean generateEmptyChunksPreservesEntities = true;

    @Rule(
            categories = {FEATURE, ONECHUNK}
    )
    public static boolean keepInventoryXPDeletion = false;

    @Rule(
            categories = {FEATURE, ONECHUNK}
    )
    public static boolean mapGlassAlwaysTransparent = false;

    @Rule(
            categories = {FEATURE, ONECHUNK}
    )
    public static boolean noBarrierTeleport = false;

    @Rule(
            categories = {FEATURE, ONECHUNK}
    )
    public static boolean noBarrierSpread = false;

    @Rule(
            categories = {FEATURE, ONECHUNK}
    )
    public static boolean noEndPillarGeneration = false;

    @Rule(
            categories = {FEATURE, ONECHUNK}
    )
    public static boolean noBarrierGatewayTeleport = false;

    @Rule(
            options = { "none", "sword", "shovel" },
            categories = {FEATURE, ONECHUNK}
    )
    public static String spearSpawnOverride = "none";

    @Rule(
            categories = {FEATURE, ONECHUNK},
            strict = false
    )
    // 5mins * 60seconds/min * 20ticks/sec = 6000ticks
    public static int vaultGlobalCooldown = 6000;

    @Rule(
            categories = {FEATURE, ONECHUNK},
            strict = false
    )
    // 5mins * 60seconds/min * 20ticks/sec = 6000ticks
    public static int trialSpawnerGlobalCooldown = 6000;

    @Rule(
            categories = {FEATURE, ONECHUNK}
    )
    public static boolean vaultReworkEnabled = false;

    private static class PermissionConverter extends Validator<String> {
        @Override public String validate(CommandSourceStack source, CarpetRule<String> currentRule, String newValue, String string) {
            PermissionCheck permissionLevel = switch (newValue) {
                case "0", "false" -> Commands.LEVEL_ALL;
                case "1" -> Commands.LEVEL_MODERATORS;
                case "2", "true", "ops" -> Commands.LEVEL_GAMEMASTERS;
                case "3" -> Commands.LEVEL_ADMINS;
                case "4" -> Commands.LEVEL_OWNERS;
                default -> throw new IllegalArgumentException(); // already checked by previous validator
            };
            if (source != null && !permissionLevel.check(source.permissions()))
                return null;
            CarpetSettings.runPermissionLevel = LevelBasedPermissionSet.GAMEMASTER;
            if (source != null) {
                CommandHelper.notifyPlayersCommandsChanged(source.getServer());
                CarpetSettings.runPermissionLevel = source.permissions();
            }
            return newValue;
        }
        @Override
        public String description() { return "When changing the rule, you must at least have the permission level you are trying to give it";}
    }
    @Rule(
            validators = {Validators.CommandLevel.class, PermissionConverter.class},
            categories = {COMMAND, FEATURE, ONECHUNK}
    )
    public static String commandEntityTracker = "ops";

    public static class validatorEntityTrackerWebhook extends Validator<String> {

        @Override
        public String validate(CommandSourceStack source, CarpetRule<String> currentRule, String newValue, String string) {
            if ("none".equals(newValue)) {
                return "none";
            } else {
                try {
                    URI.create(newValue).toURL();
                    return newValue;
                } catch (MalformedURLException | IllegalArgumentException e) {
                    return null;
                }
            }
        }

        @Override
        public String description() { return "Webhook must be a valid url or \"none\".";}

    }
    @Rule(
            validators = validatorEntityTrackerWebhook.class,
            categories = {FEATURE, ONECHUNK}
    )
    public static String entityTrackerWebhook = "none";

    @Rule(
            categories = {FEATURE, ONECHUNK}
    )
    public static String commandEntityTrackerDefaultMessage = "%{b,type_name,!b} spawned at (%{i,x,!i}, %{i,y,!i}, %{i,z,!i}) in %{b,dimension_name,!b}";

}
