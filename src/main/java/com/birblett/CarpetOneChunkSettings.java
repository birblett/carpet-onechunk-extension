package com.birblett;

import carpet.api.settings.Rule;

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
            options = { "none", "sword", "shovel" },
            categories = {FEATURE, ONECHUNK}
    )
    public static String spearSpawnOverride = "none";

    @Rule(
            options = { "0", "3", "5", "7" },
            categories = {FEATURE, ONECHUNK},
            strict = false
    )
    public static int vaultGlobalCooldown = 0;

    @Rule(
            categories = {COMMAND, FEATURE, ONECHUNK}
    )
    public static String commandEntityTracker = "ops";

    @Rule(
            categories = {FEATURE, ONECHUNK}
    )
    public static String commandEntityTrackerDefaultMessage = "%{b,type_name,!b} spawned at (%{i,x,!i}, %{i,y,!i}, %{i,z,!i}) in %{b,dimension_name,!b}";

}
