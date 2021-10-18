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
        executorService.shutdown();
        log.info("TTSEngine shutdown");
    }

    public void textToSpeech(final String input) throws IOException {
        final byte[] result = engineImpl.textToSpeech(input);

        InputStream myInputStream = new ByteArrayInputStream(result);

        // Cancel existing playing audio
        if (audioFuture != null) {
            log.info("Cancelled currently playing audio: " + input);
            audioFuture.cancel(true);
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
