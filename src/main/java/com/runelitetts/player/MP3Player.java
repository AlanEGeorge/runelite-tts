package com.runelitetts.player;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

@Slf4j
public class MP3Player extends AbstractPlayer {

    private Player mp3Player;

    public MP3Player(final byte[] audioData) {
        try {
            InputStream myInputStream = new ByteArrayInputStream(audioData);
            mp3Player = new Player(myInputStream);
        } catch (JavaLayerException ex) {
            throw new RuntimeException("Failed to play MP3", ex);
        }
    }

    @Override
    public void play(final byte[] audioData) throws IOException {
        try {
            mp3Player.play();
        } catch (JavaLayerException ex) {
            throw new IOException("Failed to play MP3", ex);
        }
    }

    @Override
    public void await() {
        while (!mp3Player.isComplete());
    }

    @Override
    public void stop() {
        if (!mp3Player.isComplete()) {
            log.info("Stopping currently playing audio...");
            mp3Player.close();
            log.info("Stopped currently playing audio");
        }
    }
}
