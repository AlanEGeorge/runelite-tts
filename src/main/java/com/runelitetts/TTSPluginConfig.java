package com.runelitetts;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup("Text-to-Speech")
public interface TTSPluginConfig extends Config {
	/*====== General settings ======*/

	@ConfigSection(
			name = "Player",
			description = "Player text-to-speech settings",
			position = 0,
			closedByDefault = false
	)
	String playerSettings = "playerSettings";

	@ConfigItem(
			keyName = "speakOtherPlayerMessages",
			name = "Speak Other Player Messages",
			description = "If enabled, speaks other player messages.",
			position = 1,
			section = playerSettings
	)
	default boolean speakOtherPlayerMessages() {
		return false;
	}
}
