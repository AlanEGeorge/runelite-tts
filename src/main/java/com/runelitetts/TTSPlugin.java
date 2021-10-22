package com.runelitetts;

import com.runelitetts.engine.AbstractEngine;
import com.runelitetts.engine.GoogleCloudEngine;
import com.runelitetts.engine.TTSEngine;
import com.google.inject.Provides;
import javax.inject.Inject;

import com.runelitetts.player.MP3Player;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
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
import net.runelite.client.util.Text;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
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

	private final TTSEngine ttsEngine = new TTSEngine(GoogleCloudEngine.class, MP3Player.class);

	private static final int WIDGET_CHILD_ID_DIALOG_PLAYER_CLICK_HERE_TO_CONTINUE = 4;
	private static final int WIDGET_CHILD_ID_DIALOG_NPC_CLICK_HERE_TO_CONTINUE = 4;
	private static final int WIDGET_CHILD_ID_DIALOG_PLAYER_NAME = 3;

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

	@Subscribe
	public void onGameTick(GameTick tick) {

		handleNpcToPlayerDialog();

		handlePlayerToNpcDialog();
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
	void handleNpcToPlayerDialog() {
		final Widget npcDialogueTextWidget = client.getWidget(WidgetInfo.DIALOG_NPC_TEXT);

		// Convert the NPC dialog into speech only if we haven't seen it before
		if (npcDialogueTextWidget != null && npcDialogueTextWidget.getText() != lastNpcDialogueText) {
			final String npcText = npcDialogueTextWidget.getText();
			lastNpcDialogueText = npcText;

			// Strip the line break
			String strippedNpcText = TextSanitizer.removeFormatting(npcText);

			String npcName = client.getWidget(WidgetInfo.DIALOG_NPC_NAME).getText();
			try {
				ttsEngine.textToSpeech(AbstractEngine.SpeechType.NPC_MAN, TextSanitizer.adjustPronunciations(npcText), true);
			} catch (IOException e) {
				e.printStackTrace();
			}

			log.info(npcName + ": " + strippedNpcText);
		}
	}

	/**
	 * This method checks to see if the player dialog widget is active, and converts the dialog string into audio.
	 *
	 * It caches the string to avoid replaying audio, but also strips <br> tags from the text to avoid speaking them.
	 */
	void handlePlayerToNpcDialog() {
		// This should be in WidgetInfo under DialogPlayer, but isn't currently.
		final Widget playerDialogueTextWidget = client.getWidget(WidgetInfo.DIALOG_PLAYER_TEXT);

		if (playerDialogueTextWidget != null && playerDialogueTextWidget.getText() != lastPlayerDialogueText) {
			final String playerText = playerDialogueTextWidget.getText();
			lastPlayerDialogueText = playerText;

			final String strippedPlayerText = TextSanitizer.removeFormatting(playerText);

			log.info("Player: " + strippedPlayerText);

			try {
				ttsEngine.textToSpeech(AbstractEngine.SpeechType.PLAYER_MAN, TextSanitizer.adjustPronunciations(playerText), true);
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
}
