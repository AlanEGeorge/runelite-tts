package com.runelitetts.engine;

import com.runelitetts.player.AbstractPlayer;
import com.runelitetts.player.MP3Player;
//import com.runelitetts.player.WavPlayer;
import com.runelitetts.player.WavPlayer;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.concurrent.*;

@Slf4j
public class TTSEngine<E extends AbstractEngine, P extends AbstractPlayer> {

    private E engineImpl;
    private P playerImpl;

    private ExecutorService executorService;

    ConcurrentHashMap<Long, AbstractPlayer> playerQueue;

    public TTSEngine(Class<E> abstractEngineType, Class<P> abstractPlayerType) {
        try {
            engineImpl = abstractEngineType.getDeclaredConstructor().newInstance();

            executorService = Executors.newCachedThreadPool();

            playerQueue = new ConcurrentHashMap<>();
        } catch (Exception ex) {
            throw new RuntimeException("Failed to construct TTSEngine", ex);
        }
    }

    public void stopAudio() {
        playerQueue.forEach((time, player) -> {
            if (player != null) {
                player.stop();
            } else {
                log.warn("Cannot stop audio: player is null");
            }
        });
        playerQueue.clear();
    }

    public void shutdown() {
        playerQueue.forEach((time, player) -> {
            if (player != null) {
                player.await();
            } else {
                log.warn("Cannot await on audio: player is null");
            }
        });
        playerQueue.clear();
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
        final byte[] result = engineImpl.textToAudio(speechType, input);

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

        AbstractPlayer player = new WavPlayer(audioData);
        log.info("Created new " + player.toString());
        final long currentTime = System.currentTimeMillis();
        playerQueue.put(currentTime, player);
        log.info("Added player to the queue (current size: " + playerQueue.size() + ")");

        executorService.execute(() -> {
            try {
                log.info("Playing audio: " + input);
                player.play();
                log.info("Completed playing audio: " + input);

                playerQueue.remove(currentTime);
                log.info("Removed player from queue (current size: " + playerQueue.size() + ")");
            } catch (Exception ex) {
                log.error("Exception occurred while playing audio", ex);
            }
        });
    }

}
