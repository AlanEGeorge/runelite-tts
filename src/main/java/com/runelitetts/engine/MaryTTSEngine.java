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

    // The following voices are arranged in best to worst

    // Male, medium pitch voice. Decent all around
//    private static final String voiceName = "cmu-bdl-hsmm";

    // Male, low, slow voice. Sounds decent
//    private static final String voiceName = "cmu-rms-hsmm";

    // Female, poppy voice, sounds pretty decent, but a bit high
    private static final String voiceName = "dfki-prudence-hsmm";
//        String voiceName = "dfki-poppy";

    // Male, low, deep voice, that's kinda hard to hear and the flow is strange
//    private static final String voiceName = "dfki-obadiah";

    // Female, medium pitch voice, pacing is weird
//    private static final String voiceName = "cmu-slt-hsmm";

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

        try {
            // synthesize
            final AudioInputStream audio = localMaryInterface.generateAudio(input);

            // write to output
            final double[] samples = MaryAudioUtils.getSamplesAsDoubleArray(audio);

            // The MaryAudioUtils class does not have a way to generate a .wav file as a native byte array
            // This code block writes the wav to file, reads in the bytes, and then deletes the file
            MaryAudioUtils.writeWavFile(samples, outputFileName, audio.getFormat());
            final byte[] result = Files.readAllBytes(Path.of(outputFileName));

            File file = new File(outputFileName);
            if (!file.delete()) {
                log.error("Failed to delete .wav file: " + outputFileName);
            }

            return result;
        } catch (final Exception ex) {
            throw new IOException("MaryTTS synthesis failed", ex);
        }
    }
}
