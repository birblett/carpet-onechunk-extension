package com.birblett;

import carpet.CarpetExtension;
import carpet.CarpetServer;
import com.birblett.entity_tracker.EntityTrackerState;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.api.ModInitializer;

import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;

public class CarpetOneChunkExtension implements ModInitializer, CarpetExtension {
	public static final String MOD_ID = "carpet-onechunk-extension";

	@Override
	public void onInitialize() {
		CarpetServer.manageExtension(new CarpetOneChunkExtension());
	}

	@Override
	public void onGameStarted() {
		CarpetServer.settingsManager.parseSettingsClass(CarpetOneChunkSettings.class);
	}

	@Override
	public void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher, final CommandBuildContext commandRegistryAccess) {
		EntityTrackerState.register(dispatcher);
	}

	@Override
	public Map<String, String> canHasTranslations(String lang) {
		InputStream langFile = CarpetOneChunkExtension.class.getClassLoader()
				.getResourceAsStream("assets/carpet-onechunk-extension/lang/%s.json".formatted(lang));
		if (langFile == null) {
			return Collections.emptyMap();
		}
		String jsonData;
		try {
			jsonData = IOUtils.toString(langFile, StandardCharsets.UTF_8);
		} catch (IOException e) {
			return Collections.emptyMap();
		}
		Gson gson = new GsonBuilder().setLenient().create();
		return gson.fromJson(jsonData, new TypeToken<Map<String, String>>() {}.getType());

	}

}
