package com.bsh;

import java.io.FileOutputStream;
import java.io.OutputStream;

import com.bsh.engines.GoogleCloudEngine;

public class TTSPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(TTSPlugin.class);
		RuneLite.main(args);

//		final String exampleDialogue = "The chance to get a higher tier clue from geodes, bottles and nests is lower the higher the tier. But if you chop down higher-level trees you will have a higher chance of receiving a nest which contains a clue. Same with fishing and mining.";
//
//		try (OutputStream out = new FileOutputStream("output.mp3")) {
//			GoogleCloudEngine engine = new GoogleCloudEngine();
//			engine.textToSpeech(exampleDialogue, out);
//		}
	}
}