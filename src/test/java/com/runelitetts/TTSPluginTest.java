package com.runelitetts;

import com.runelitetts.engine.AbstractEngine;
import com.runelitetts.engine.GoogleCloudEngine;
//import com.bsh.engine.MaryTTSEngine;
import com.runelitetts.engine.MaryTTSEngine;
import com.runelitetts.engine.TTSEngine;
import com.runelitetts.player.MP3Player;
import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.File;

public class TTSPluginTest
{

	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(TTSPlugin.class);
		RuneLite.main(args);

//		final TTSEngine googleCloudEngine = new TTSEngine(GoogleCloudEngine.class, MP3Player.class);
//		googleCloudEngine.textToSpeech(AbstractEngine.SpeechType.PLAYER_MAN, "Anyone have 100k", false);
//		Thread.sleep(200);
//		googleCloudEngine.textToSpeech(AbstractEngine.SpeechType.PLAYER_MAN, "Dancing for money", false);
//		Thread.sleep(200);
//		googleCloudEngine.textToSpeech(AbstractEngine.SpeechType.PLAYER_MAN, "I am not familiar with opensource in this area.", false);
////
//		googleCloudEngine.shutdown();

//		final TTSEngine maryTtsEngine = new TTSEngine(MaryTTSEngine.class);
//		maryTtsEngine.textToSpeech(AbstractEngine.SpeechType.NPC_MAN, "Hello, I am an NPC.", false);
//
//		Clip clip = AudioSystem.getClip();
//		clip.open(AudioSystem.getAudioInputStream(new File("output.wav")));
//		clip.start();
//
//		while (!clip.isRunning())
//			Thread.sleep(10);
//		while (clip.isRunning())
//			Thread.sleep(10);
//		clip.close();

//		Thread.sleep(1000);

//		final String exampleDialogue = "The chance to get a higher tier clue from geodes, bottles and nests is lower the higher the tier. But if you chop down higher-level trees you will have a higher chance of receiving a nest which contains a clue. Same with fishing and mining.";
//		final String test = "Hello Amelia, this is a computer!";
//
//		try (OutputStream out = new FileOutputStream("output.mp3")) {
//			GoogleCloudEngine engine = new GoogleCloudEngine();
//			engine.textToSpeech(test, out);
//		}
	}
}