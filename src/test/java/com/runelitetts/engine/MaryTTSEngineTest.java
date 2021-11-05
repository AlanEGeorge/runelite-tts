package com.runelitetts.engine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import com.runelitetts.engine.AbstractEngine;
import com.runelitetts.engine.MaryTTSEngine;
import com.runelitetts.engine.TTSEngine;
import com.runelitetts.player.WavPlayer;
import marytts.server.Mary;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public class MaryTTSEngineTest {

    private TTSEngine ttsEngine;

    @Before
    public void setUp() throws Exception {
        ttsEngine = new TTSEngine(MaryTTSEngine.class, WavPlayer.class);
    }

    @Test
    public void testMaryTTSEngineAndWavPlayer() throws Exception {
        ttsEngine.textToSpeech(AbstractEngine.SpeechType.PLAYER_MAN, "Dooom! What doom? Doom, it's all around us!", true);
        Thread.sleep(10000);
//        try
//        {
//            Clip clip = AudioSystem.getClip();
//            clip.open(AudioSystem.getAudioInputStream(new File("output.wav")));
//            clip.start();
//            while (!clip.isRunning())
//                Thread.sleep(10);
//            while (clip.isRunning())
//                Thread.sleep(10);
//            clip.close();
//        }
//        catch (Exception exc)
//        {
//            exc.printStackTrace(System.out);
//        }
    }

}
