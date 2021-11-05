package com.runelitetts;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup("Text-to-Speech")
public interface TTSPluginConfig extends Config {

	/*====== Public settings ======*/
	@ConfigSection(
			name = "Public",
			description = "Player text-to-speech settings",
			position = 0,
			closedByDefault = false
	)
	String publicSettings = "publicSettings";

	@ConfigItem(
			keyName = "speakPlayerPublicMessages",
			name = "Speak Player Messages",
			description = "If enabled, speaks player messages in public chat.",
			position = 1,
			section = publicSettings
	)
	default boolean speakPlayerPublicMessages() {
		return false;
	}

	@ConfigItem(
			keyName = "speakNamePlayerPublicMessages",
			name = "Speak Player's Name Before Messages",
			description = "If enabled, speaks player's name before their messages in public chat.",
			position = 2,
			section = publicSettings
	)
	default boolean speakNamePlayerPublicMessages() {
		return false;
	}

	@ConfigItem(
			keyName = "speakMyPublicMessages",
			name = "Speak My Messages",
			description = "If enabled, speaks my messages in public chat.",
			position = 3,
			section = publicSettings
	)
	default boolean speakMyPublicMessages() {
		return false;
	}

	/*====== NPC settings ======*/
	@ConfigSection(
			name = "NPC",
			description = "NPC text-to-speech settings",
			position = 4,
			closedByDefault = false
	)
	String npcSettings = "npcSettings";

	@ConfigItem(
			keyName = "speakNpcToPlayerDialog",
			name = "Speak NPC Dialog",
			description = "If enabled, speaks NPC dialog in interaction windows.",
			position = 5,
			section = npcSettings
	)
	default boolean speakNpcToPlayerDialog() {
		return false;
	}
}
