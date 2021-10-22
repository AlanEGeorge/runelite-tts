package com.runelitetts.engine;

import com.runelitetts.player.AbstractPlayer;
import com.runelitetts.player.MP3Player;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Slf4j
public class TTSEngine<E extends AbstractEngine, P extends AbstractPlayer> {

    private E engineImpl;
    private P playerImpl;

    private ExecutorService executorService;
    ConcurrentHashMap<Long, Player> playerMap;

    AbstractPlayer player;

    public TTSEngine(Class<E> abstractEngineType, Class<P> abstractPlayerType) {
        try {
            engineImpl = abstractEngineType.getDeclaredConstructor().newInstance();

            executorService = Executors.newSingleThreadExecutor();
        } catch (Exception ex) {
            throw new RuntimeException("Failed to construct TTSEngine", ex);
        }
    }

    public void stopAudio() {
        if (player != null) {
            player.stop();
        } else {
            log.warn("Cannot stop audio: player is null");
        }
    }

    public void shutdown() {
        player.await();
        stopAudio();
        executorService.shutdown();
        log.info("TTSEngine shutdown");
    }

    public void shutdownNow() {
        stopAudio();
        executorService.shutdownNow();
        log.info("TTSEngine shutdown");
    }

    public void textToSpeech(final AbstractEngine.SpeechType speechType, final String input, final boolean interruptOthers) throws IOException {
        final byte[] result = engineImpl.textToMp3Bytes(speechType, input);

        playAudio(input, result, interruptOthers);
    }

    public void playAudio(final String input, final byte[] audioData, final boolean interruptOthers) throws IOException {
        if (audioData == null) {
            throw new IOException("Text conversion result was null");
        }

        // Cancel existing playing audio
        if (interruptOthers) {
            stopAudio();
        }

        player = new MP3Player(audioData);

        executorService.execute(() -> {
            try {
                log.info("Playing audio: " + input);
                player.play(audioData);
                log.info("Completed playing audio: " + input);
            } catch (Exception ex) {
                log.error("Exception occurred while playing audio", ex);
            }
        });
    }

}
