package com.runelitetts.engine;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Slf4j
public class TTSEngine<T extends AbstractEngine> {

    private T engineImpl;
    private ExecutorService executorService;
    private Future<?> audioFuture;

    private Player mp3Player;

    public TTSEngine(Class<T> abstractEngineType) {
        try {
            engineImpl = abstractEngineType.getDeclaredConstructor().newInstance();

            executorService = Executors.newSingleThreadExecutor();
        } catch (Exception ex) {
            throw new RuntimeException("Failed to construct TTSEngine", ex);
        }
    }

    public void stopAudio() {
        if (mp3Player != null && !mp3Player.isComplete()) {
            mp3Player.close();
            log.info("Stopped currently playing audio");

            mp3Player.isComplete();
        }
    }

    public void shutdown() {
        log.info("TTSEngine shutdown");
        try {
            audioFuture.get();
        } catch (Exception ex) {
            log.warn("Received exception while cancelling audio", ex);
        }
        executorService.shutdownNow();
    }

    public void textToSpeech(final AbstractEngine.SpeechType speechType, final String input, final boolean cancelOthers) throws IOException {
        final byte[] result = engineImpl.textToMp3Bytes(speechType, input);

        playAudio(input, result, cancelOthers);
    }

    public void playAudio(final String input, final byte[] mp3Data, final boolean cancelOthers) throws IOException {
        if (mp3Data == null) {
            throw new IOException("Text conversion result was null");
        }

        InputStream myInputStream = new ByteArrayInputStream(mp3Data);

        // Cancel existing playing audio
        if (cancelOthers) {
            stopAudio();
        }

        try {
            mp3Player = new Player(myInputStream);

            audioFuture = executorService.submit(() -> {
                try {
                    log.info("Playing audio: " + input);
                    mp3Player.play();
                    log.info("Completed playing audio: " + input);
                } catch (Exception ex) {
                    log.error("Exception occurred while playing audio", ex);
                }
            });

        } catch (JavaLayerException ex) {
            throw new IOException("Failed to play MP3", ex);
        }
    }

}
