package com.bsh;

import com.bsh.engine.GoogleCloudEngine;
import com.bsh.engine.MaryTTSEngine;
import com.bsh.engine.TTSEngine;

public class TTSPluginTest
{

	public static void main(String[] args) throws Exception
	{
		final TTSEngine googleCloudEngine = new TTSEngine(GoogleCloudEngine.class);
		googleCloudEngine.textToSpeech("Hello from Google");
		googleCloudEngine.textToSpeech("This should interrupt");

		googleCloudEngine.shutdown();

		final TTSEngine maryTtsEngine = new TTSEngine(MaryTTSEngine.class);
		maryTtsEngine.textToSpeech("Hello from MaryTTS");

//		ExternalPluginManager.loadBuiltin(TTSPlugin.class);
//		RuneLite.main(args);

//		final String exampleDialogue = "The chance to get a higher tier clue from geodes, bottles and nests is lower the higher the tier. But if you chop down higher-level trees you will have a higher chance of receiving a nest which contains a clue. Same with fishing and mining.";
//		final String test = "Hello Amelia, this is a computer!";
//
//		try (OutputStream out = new FileOutputStream("output.mp3")) {
//			GoogleCloudEngine engine = new GoogleCloudEngine();
//			engine.textToSpeech(test, out);
//		}
	}
}