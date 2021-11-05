package com.runelitetts.player;

import com.google.common.io.ByteSource;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;
import lombok.extern.slf4j.Slf4j;
import marytts.LocalMaryInterface;
import marytts.exceptions.MaryConfigurationException;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

@Slf4j
public class WavPlayer extends AbstractPlayer {

    private Clip clip;
    private byte[] test;

    public WavPlayer(final byte[] audioData) {
        log.info("in constructor");
        try {
            final InputStream inputStream = ByteSource.wrap(audioData).openStream();
            clip = AudioSystem.getClip();
            clip.open(AudioSystem.getAudioInputStream(inputStream));
        } catch (UnsupportedAudioFileException|LineUnavailableException|IOException ex) {
            log.error("Failed to create WavPlayer: " + ex.getMessage());
            throw new RuntimeException("Failed to create WavPlayer", ex);
        }
    }

    @Override
    public void play() throws IOException {
        log.info("in play()");
        clip.start();
    }

    @Override
    public void await() {
        log.info("in await()");
        while(clip.isRunning());
    }

    @Override
    public void stop() {
        log.info("in stop()");
        clip.close();
    }
}
