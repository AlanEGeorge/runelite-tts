package com.runelitetts;

import com.runelitetts.engine.AbstractEngine;
import com.runelitetts.engine.GoogleCloudEngine;
import com.runelitetts.engine.MaryTTSEngine;
import com.runelitetts.engine.TTSEngine;
import com.google.inject.Provides;
import javax.inject.Inject;

import com.runelitetts.player.MP3Player;
import com.runelitetts.player.WavPlayer;
import lombok.extern.slf4j.Slf4j;
import marytts.server.Mary;
import net.runelite.api.*;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetID;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.config.Keybind;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.util.HotkeyListener;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;

@Slf4j
@PluginDescriptor(
	name = "Text-to-Speech"
)
public class TTSPlugin extends Plugin
{
	private TTSEngine ttsEngine;
	private TextSanitizer textSanitizer;

	private String lastDialogBoxText = "";
	private String lastPlayerDialogueText = "";
	private String lastNpcDialogueText = "";
	private String lastLevelUpDialogText = "";

	private Widget[] dialogueOptions;

	private static final int WIDGET_CHILD_ID_DIALOG_PLAYER_CLICK_HERE_TO_CONTINUE = 4;
	private static final int WIDGET_CHILD_ID_DIALOG_NPC_CLICK_HERE_TO_CONTINUE = 4;
	private static final int WIDGET_CHILD_ID_DIALOG_PLAYER_NAME = 3;

	private boolean inNpcDialog;

	private HotkeyListener spacebarListener = new HotkeyListener(this::provider )
	{
		@Override
		public void hotkeyPressed()
		{
			log.info("Space bar pressed!");
		}
	};

	private Keybind provider() {
		return new Keybind(KeyEvent.VK_SPACE, InputEvent.SHIFT_DOWN_MASK);
	}

	@Inject
	private Client client;

	@Inject
	private TTSPluginConfig config;

	@Override
	protected void startUp() throws Exception
	{
		inNpcDialog = false;
		textSanitizer = new TextSanitizer();
//		ttsEngine = new TTSEngine(GoogleCloudEngine.class, MP3Player.class);
		ttsEngine = new TTSEngine(MaryTTSEngine.class, WavPlayer.class);

		log.info("TTS started!");
	}

	private boolean widgetExists(int parentId, int childId) {
		return client.getWidget(parentId, childId) != null;
	}

	@Override
	protected void shutDown() throws Exception
	{
		log.info("TTS stopped!");
		ttsEngine.shutdownNow();
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
//		if (gameStateChanged.getGameState() == GameState.LOGGED_IN)
//		{
//			client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Example says " + config.drawDistance(), null);
//		}
	}

	/**
	 * This subscription is used to cancel audio that's currently playing when the user clicks the "continue" blue text.
	 * This does NOT handle spacebar hotkey skipping.
	 * @param event
	 */
	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked event) {
		final int groupId = WidgetInfo.TO_GROUP(event.getWidgetId());
		final int childId = WidgetInfo.TO_CHILD(event.getWidgetId());

		// Stop playing audio if the user clicks continue on a dialog option and the NPC is speaking
		if (groupId == WidgetID.DIALOG_NPC_GROUP_ID && childId == WIDGET_CHILD_ID_DIALOG_NPC_CLICK_HERE_TO_CONTINUE) {
			ttsEngine.stopAudio();
		// Stop playing audio if the user clicks continue on a dialog option and the player is speaking
		} else if (groupId == WidgetID.DIALOG_PLAYER_GROUP_ID && childId == WIDGET_CHILD_ID_DIALOG_PLAYER_CLICK_HERE_TO_CONTINUE) {
			ttsEngine.stopAudio();
		}
	}

	/**
	 * Convert chat messages into speech.
	 * @param event
	 */
	@Subscribe
	public void onChatMessage(ChatMessage event) {

//		log.info("My username: " + client.getUsername());

		// Play other player's chat
		if (config.speakPlayerPublicMessages() && event.getType() == ChatMessageType.PUBLICCHAT && event.getName() != client.getUsername()) {
			final String playerName = event.getName();
			final String playerMessage = event.getMessage();

			log.info(playerName + ": " + playerMessage);

			// Prepend player name if enabled
			String message = "";
			if (config.speakNamePlayerPublicMessages()) {
				message = playerName + " says: " + playerMessage;
			} else {
				message = playerMessage;
			}

			try {
				ttsEngine.textToSpeech(AbstractEngine.SpeechType.NPC_MAN, textSanitizer.adjustPronunciations(message), false);
			} catch (IOException ex) {
				log.error("Failed to play player dialog", ex);
			}
		}
	}

	@Subscribe
	public void onGameTick(GameTick tick) {

		handleNpcToPlayerDialog();

		handlePlayerToNpcDialog();

		// Disabling this section--way too buggy with how widget text windows are done in OSRS
		if (false) {
			// Sprite dialog text
			//		handleDialogSpriteText();
			speakWidgetText(11, 2);
			speakWidgetText(229, 1);

			// Top-level dialog
			speakWidgetText(193, 2);

			// Level up-dialog
			if (widgetExists(223, 0)) {
				final String levelUpSkillText = client.getWidget(233, 1).getText();
				final String levelUpLevelText = client.getWidget(233, 2).getText();
				final String levelUpDialog = levelUpSkillText + " " + levelUpLevelText;

				if (levelUpDialog != lastLevelUpDialogText) {
					lastLevelUpDialogText = levelUpDialog;
					try {
						ttsEngine.textToSpeech(AbstractEngine.SpeechType.NPC_MAN, levelUpDialog, true);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}

			// Top-level "Click to continue" button
			// 193.0[2]
			if (widgetExists(193, 0)) {
				Widget widget = client.getWidget(193, 0);
				if (widget.getChild(2) != null) {
					log.info("193.0[2] - Top-level dialog active...not playing other text");
				}
			} else if (widgetExists(229, 2)) {
				log.info("229.2 - Top-level dialog active...not playing other text");
			} else {
				// Chatbox Dialog Text (e.g. tutorial island or questing)
				speakWidgetText(263, 0, 0);
				speakWidgetText(263, 1, 0);
			}

		}

	}

	@Provides
	TTSPluginConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(TTSPluginConfig.class);
	}

	/**
	 * This method checks to see if the NPC dialog widget is active, and converts the dialog string into audio.
	 *
	 * It caches the string to avoid replaying audio, but also strips <br> tags from the text to avoid speaking them.
	 */
	private void handleNpcToPlayerDialog() {
		final Widget npcDialogueTextWidget = client.getWidget(WidgetInfo.DIALOG_NPC_TEXT);

		// Convert the NPC dialog into speech only if we haven't seen it before
		if (npcDialogueTextWidget != null && npcDialogueTextWidget.getText() != lastNpcDialogueText) {
			final String npcText = npcDialogueTextWidget.getText();
			lastNpcDialogueText = npcText;

			try {
				ttsEngine.textToSpeech(AbstractEngine.SpeechType.NPC_MAN, textSanitizer.adjustPronunciations(npcText), true);
			} catch (IOException e) {
				e.printStackTrace();
			}

			// Strip the line breaks and log for debugging purposes
			final String npcName = client.getWidget(WidgetInfo.DIALOG_NPC_NAME).getText();
			log.debug(npcName + ": " + textSanitizer.removeFormatting(npcText));
		}
	}

	/**
	 * This method checks to see if the player dialog widget is active, and converts the dialog string into audio.
	 *
	 * It caches the string to avoid replaying audio, but also strips <br> tags from the text to avoid speaking them.
	 */
	private void handlePlayerToNpcDialog() {
		// This should be in WidgetInfo under DialogPlayer, but isn't currently.
		final Widget playerDialogueTextWidget = client.getWidget(WidgetInfo.DIALOG_PLAYER_TEXT);

		if (playerDialogueTextWidget != null && playerDialogueTextWidget.getText() != lastPlayerDialogueText) {
			final String playerText = playerDialogueTextWidget.getText();
			lastPlayerDialogueText = playerText;

			final String strippedPlayerText = textSanitizer.removeFormatting(playerText);

			log.info("Player: " + strippedPlayerText);

			try {
				ttsEngine.textToSpeech(AbstractEngine.SpeechType.PLAYER_MAN, textSanitizer.adjustPronunciations(playerText), true);
			} catch (IOException ex) {
				throw new RuntimeException("Failed to convert player-to-npc dialog", ex);
			}
		}

		final Widget playerDialogueOptionsWidget = client.getWidget(WidgetID.DIALOG_OPTION_GROUP_ID, 1);
		if (playerDialogueOptionsWidget != null && playerDialogueOptionsWidget.getChildren() != dialogueOptions) {
			dialogueOptions = playerDialogueOptionsWidget.getChildren();
			for (int i = 1; i < dialogueOptions.length - 2; i++) {
				System.out.println(dialogueOptions[i].getText());
			}
		}
	}

//	private void handleDialogSpriteText() {
//		final Widget dialogSpriteTextWidget = client.getWidget(WidgetInfo.DIALOG_SPRITE_TEXT);
//
//		if (dialogSpriteTextWidget != null && dialogSpriteTextWidget.getText() != lastSpriteDialogText) {
//			final String dialogSpriteText = dialogSpriteTextWidget.getText();
//			lastSpriteDialogText = dialogSpriteText;
//
//			log.info("Sprite dialog: " + dialogSpriteText);
//
//			try {
//				ttsEngine.textToSpeech(AbstractEngine.SpeechType.PLAYER_MAN, textSanitizer.removeFormatting(lastSpriteDialogText), true);
//			} catch (IOException ex) {
//				throw new RuntimeException("Failed to convert player-to-npc dialog", ex);
//			}
//		}
//	}

	private void speakFromWidget(AbstractEngine.SpeechType speechType, final String input) {
		if (lastDialogBoxText != input) {
			lastDialogBoxText = input;

			try {
				ttsEngine.textToSpeech(speechType, TextSanitizer.removeFormatting(input), true);
			} catch (IOException ex) {
				throw new RuntimeException("Failed to speak dialog", ex);
			}
		}
	}

	private void speakWidgetText(final Widget widget) {
		if (widget != null) {
			final String widgetText = widget.getText();
			speakFromWidget(AbstractEngine.SpeechType.NPC_MAN, widgetText);
		};
	}

	private void speakWidgetText(final int parentId, final int childId) {
		log.debug("Speaking widget text: " + parentId + "." + childId);
		speakWidgetText(client.getWidget(parentId, childId));
	}

	private void speakWidgetText(final int parentId, final int childId, final int index) {
		log.debug("Speaking widget text: " + parentId + "." + childId + "[" + index + "]");
		Widget widget = client.getWidget(parentId, childId);
		if (widget != null) {
			speakWidgetText(widget.getChild(index));
		}
	}
}
