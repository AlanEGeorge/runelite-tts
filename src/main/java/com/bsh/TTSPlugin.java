package com.bsh;

import com.bsh.engines.GoogleCloudEngine;
import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Player;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetID;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

import java.io.FileInputStream;
import java.io.IOException;

@Slf4j
@PluginDescriptor(
	name = "Text-to-Speech"
)
public class TTSPlugin extends Plugin
{
	private String lastNpcDialogueText = null;
	private String lastPlayerDialogueText = null;
	private Widget[] dialogueOptions;

	@Inject
	private Client client;

	@Inject
	private TTSPluginConfig config;

	@Override
	protected void startUp() throws Exception
	{
		log.info("Example started!");
	}

	@Override
	protected void shutDown() throws Exception
	{
		log.info("Example stopped!");
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		if (gameStateChanged.getGameState() == GameState.LOGGED_IN)
		{
			client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Example says " + config.greeting(), null);
		}
	}

	@Subscribe
	public void onGameTick(GameTick tick) {
		Widget npcDialogueTextWidget = client.getWidget(WidgetInfo.DIALOG_NPC_TEXT);

		if (npcDialogueTextWidget != null && npcDialogueTextWidget.getText() != lastNpcDialogueText) {
			String npcText = npcDialogueTextWidget.getText();
			lastNpcDialogueText = npcText;

			// Strip the line break
			String strippedNpcText = npcText.replace("<br>", " ");

			String npcName = client.getWidget(WidgetInfo.DIALOG_NPC_NAME).getText();

			final GoogleCloudEngine engine = new GoogleCloudEngine();
			try {
				engine.textToSpeech(strippedNpcText);
			} catch (IOException e) {
				e.printStackTrace();
			}

			System.out.println(npcName + ": " + strippedNpcText);
		}

		// This should be in WidgetInfo under DialogPlayer, but isn't currently.
		Widget playerDialogueTextWidget = client.getWidget(WidgetInfo.DIALOG_PLAYER_TEXT);

		if (playerDialogueTextWidget != null && playerDialogueTextWidget.getText() != lastPlayerDialogueText) {
			String playerText = playerDialogueTextWidget.getText();
			lastPlayerDialogueText = playerText;

			System.out.println("Player: " + playerText);
		}
//
//		Widget playerDialogueOptionsWidget = client.getWidget(WidgetID.DIALOG_OPTION_GROUP_ID, 1);
//		if (playerDialogueOptionsWidget != null && playerDialogueOptionsWidget.getChildren() != dialogueOptions) {
//			dialogueOptions = playerDialogueOptionsWidget.getChildren();
//			for (int i = 1; i < dialogueOptions.length - 2; i++) {
//				System.out.println(dialogueOptions[i].getText());
//			}
//		}
	}

	@Provides
	TTSPluginConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(TTSPluginConfig.class);
	}
}
