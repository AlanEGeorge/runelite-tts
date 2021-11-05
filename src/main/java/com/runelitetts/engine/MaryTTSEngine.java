package com.runelitetts.engine;

import com.google.common.io.ByteSource;
import lombok.extern.slf4j.Slf4j;
import marytts.LocalMaryInterface;
import marytts.exceptions.MaryConfigurationException;
import marytts.exceptions.SynthesisException;
import marytts.util.data.audio.MaryAudioUtils;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
public class MaryTTSEngine extends AbstractEngine {

    private static final String NAME = "txt2wav";
    private static final String IN_OPT = "input";
    private static final String OUT_OPT = "output";
    private static final String VOICE_OPT = "voice";

//    private static final String voiceName = "dfki-prudence-hsmm";
//        String voiceName = "cmu-slt-hsmm";
//        String voiceName = "cmu-bdl-hsmm";
//        String voiceName = "cmu-rms-hsmm";
    private static final String voiceName = "dfki-obadiah";
//        String voiceName = "dfki-poppy";

    private LocalMaryInterface localMaryInterface;

    public MaryTTSEngine() {
        try {
            localMaryInterface = new LocalMaryInterface();
            log.info("MaryTTS available voices: " + localMaryInterface.getAvailableVoices());

            // Set voice / language
            localMaryInterface.setVoice(voiceName);
            log.info("Set voice to: " + voiceName);
        } catch (MaryConfigurationException ex) {
            throw new RuntimeException("Could not initialize MaryTTS interface", ex);
        }
    }

    @Override
    public byte[] textToAudio(final SpeechType speechType, final String input) throws IOException {

        // Create unique file name
        final String outputFileName = "runelite-tts-" + System.currentTimeMillis() + ".wav";

        // synthesize
        AudioInputStream audio = null;
        try {
            audio = localMaryInterface.generateAudio(input);

            // write to output
            final double[] samples = MaryAudioUtils.getSamplesAsDoubleArray(audio);

            MaryAudioUtils.writeWavFile(samples, outputFileName, audio.getFormat());
            log.info("Created file: " + outputFileName);

            final byte[] result = Files.readAllBytes(Path.of(outputFileName));
            log.info("Read file of size: " + result.length);

            File file = new File(outputFileName);
            if (!file.delete()) {
                log.error("Failed to delete file: " + outputFileName);
            }

            return result;
        } catch (final Exception ex) {
            throw new IOException("MaryTTS synthesis failed", ex);
        }
    }
}
