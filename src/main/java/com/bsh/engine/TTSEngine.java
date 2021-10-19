package com.bsh.engine;

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

    public TTSEngine(Class<T> abstractEngineType) {
        try {
            engineImpl = abstractEngineType.getDeclaredConstructor().newInstance();

            executorService = Executors.newSingleThreadExecutor();
        } catch (Exception ex) {
            throw new RuntimeException("Failed to construct TTSEngine", ex);
        }
    }

    public void shutdown() {
        log.info("TTSEngine shutdown");
        executorService.shutdownNow();
    }

    public void textToSpeechPlayer(final String input) throws IOException {
        final byte[] result = engineImpl.textToSpeechPlayer(input);

        playAudio(input, result);
    }

    public void textToSpeechNpc(final String input) throws IOException {
        final byte[] result = engineImpl.textToSpeechNpc(input);

        playAudio(input, result);
    }

    public void playAudio(final String input, final byte[] mp3Data) throws IOException {
        if (mp3Data == null) {
            throw new IOException("Text conversion result was null");
        }

        InputStream myInputStream = new ByteArrayInputStream(mp3Data);

        // Cancel existing playing audio
        if (audioFuture != null) {
            log.info("Cancelling currently playing audio: " + input);
            audioFuture.cancel(true);
            log.info("Cancelled currently playing audio: " + input);
        }

        audioFuture = executorService.submit(() -> {
            try {
                Player playMP3 = new Player(myInputStream);
                log.info("Playing audio: " + input);
                playMP3.play();
                log.info("Completed playing audio: " + input);
            } catch(Exception ex) {
                log.error("Exception occurred while playing audio", ex);
            }
        });
    }

}
