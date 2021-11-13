package com.runelitetts.engine;

import com.runelitetts.player.AbstractPlayer;
//import com.runelitetts.player.WavPlayer;
import com.runelitetts.player.MP3Player;
import com.runelitetts.player.WavPlayer;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.*;

@Slf4j
public class TTSEngine<E extends AbstractEngine, P extends AbstractPlayer> {

    // This is the upper bound for number of concurrent audio channels
    private static final short MAX_AUDIO_CHANNELS = 10;

    private E engineImpl;

    private Class<P> abstractPlayerType;
    private Constructor<P> abstractPlayerConstructor;

    private ExecutorService executorService;

    private ConcurrentHashMap<Long, AbstractPlayer> audioPlayerQueue;

    public TTSEngine(Class<E> abstractEngineType, Class<P> abstractPlayerType) {
        try {
            engineImpl = abstractEngineType.getDeclaredConstructor().newInstance();
            this.abstractPlayerType = abstractPlayerType;
            abstractPlayerConstructor = abstractPlayerType.getDeclaredConstructor(byte[].class);

            executorService = Executors.newCachedThreadPool();

            audioPlayerQueue = new ConcurrentHashMap<>();
        } catch (Exception ex) {
            throw new RuntimeException("Failed to construct TTSEngine", ex);
        }
    }

    public void stopAudio() {
        audioPlayerQueue.forEach((time, player) -> {
            if (player != null) {
                player.stop();
            } else {
                log.warn("Cannot stop audio: player is null");
            }
        });
        audioPlayerQueue.clear();
    }

    public void shutdown() {
        audioPlayerQueue.forEach((time, player) -> {
            if (player != null) {
                player.await();
            } else {
                log.warn("Cannot await on audio: player is null");
            }
        });
        audioPlayerQueue.clear();
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

        // Check to see if we're at maximum audio channels
        if (audioPlayerQueue.size() >= MAX_AUDIO_CHANNELS) {
            return;
        }

        // Cancel existing playing audio
        if (interruptOthers) {
            stopAudio();
        }

        try {
            AbstractPlayer player = abstractPlayerConstructor.newInstance(audioData);
            log.debug("Created new " + player.toString());
            final long currentTime = System.currentTimeMillis();
            audioPlayerQueue.put(currentTime, player);
            log.debug("Added player to the queue (current size: " + audioPlayerQueue.size() + ")");

            executorService.execute(() -> {
                try {
                    log.debug("Playing audio: " + input);
                    player.play();
                    log.debug("Completed playing audio: " + input);

                    audioPlayerQueue.remove(currentTime);
                    log.debug("Removed player from queue (current size: " + audioPlayerQueue.size() + ")");
                } catch (Exception ex) {
                    log.error("Exception occurred while playing audio", ex);
                }
            });
        } catch (InvocationTargetException|InstantiationException|IllegalAccessException ex) {
            throw new IOException("Failed to create AbstractPlayer object from derived type", ex);
        }
    }
}
