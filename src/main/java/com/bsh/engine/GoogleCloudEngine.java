package com.bsh.engine;

import com.google.cloud.texttospeech.v1.AudioConfig;
import com.google.cloud.texttospeech.v1.AudioEncoding;
import com.google.cloud.texttospeech.v1.SsmlVoiceGender;
import com.google.cloud.texttospeech.v1.SynthesisInput;
import com.google.cloud.texttospeech.v1.SynthesizeSpeechResponse;
import com.google.cloud.texttospeech.v1.TextToSpeechClient;
import com.google.cloud.texttospeech.v1.VoiceSelectionParams;
import com.google.protobuf.ByteString;

import javazoom.jl.player.*;
import lombok.extern.slf4j.Slf4j;

import java.io.*;

@Slf4j
public class GoogleCloudEngine implements AbstractEngine {

    public static final String GOOGLE_ENV_NAME = "GOOGLE_APPLICATION_CREDENTIALS";

    public GoogleCloudEngine() {

    }

    public boolean envExists(String name) {
        return System.getenv(name) != null;
    }

    private void performChecks() throws IOException {

        if (!envExists(GOOGLE_ENV_NAME)) {
            throw new IOException("Failed to find environment variable: " + GOOGLE_ENV_NAME);
        }

        final String envFile = System.getenv(GOOGLE_ENV_NAME);
        File file = new File(envFile);
        if (!file.exists()) {
            throw new IOException("Failed to find auth file at: " + envFile);
        }
    }
    @Override
    public byte[] textToSpeechPlayer(String input) throws IOException {
        performChecks();

        try (TextToSpeechClient textToSpeechClient = TextToSpeechClient.create()) {
            // Set the text input to be synthesized
            SynthesisInput synthesisInput = SynthesisInput.newBuilder().setText(input).build();

            // Build the voice request, select the language code ("en-US") and the ssml voice gender
            // ("neutral")
            VoiceSelectionParams voice =
                    VoiceSelectionParams.newBuilder()
                            .setLanguageCode("en-US")
                            .setSsmlGender(SsmlVoiceGender.MALE)
                            .setName("en-US-Wavenet-B")
                            .build();

            // Select the type of audio file you want returned
            AudioConfig audioConfig =
                    AudioConfig.newBuilder().setAudioEncoding(AudioEncoding.MP3).build();

            // Perform the text-to-speech request on the text input with the selected voice parameters and
            // audio file type
            SynthesizeSpeechResponse response =
                    textToSpeechClient.synthesizeSpeech(synthesisInput, voice, audioConfig);

            // Get the audio contents from the response
            ByteString audioContents = response.getAudioContent();

            // Return the Mp3 as bytes
            return audioContents.toByteArray();
        }
    }

    @Override
    public byte[] textToSpeechNpc(String input) throws IOException {
        performChecks();

        try (TextToSpeechClient textToSpeechClient = TextToSpeechClient.create()) {
            // Set the text input to be synthesized
            SynthesisInput synthesisInput = SynthesisInput.newBuilder().setText(input).build();

            // Build the voice request, select the language code ("en-US") and the ssml voice gender
            // ("neutral")
            VoiceSelectionParams voice =
                    VoiceSelectionParams.newBuilder()
                            .setLanguageCode("en-US")
                            .setSsmlGender(SsmlVoiceGender.MALE)
                            .setName("en-US-Wavenet-A")
                            .build();

            // Select the type of audio file you want returned
            AudioConfig audioConfig =
                    AudioConfig.newBuilder().setAudioEncoding(AudioEncoding.MP3).build();

            // Perform the text-to-speech request on the text input with the selected voice parameters and
            // audio file type
            SynthesizeSpeechResponse response =
                    textToSpeechClient.synthesizeSpeech(synthesisInput, voice, audioConfig);

            // Get the audio contents from the response
            ByteString audioContents = response.getAudioContent();

            // Return the Mp3 as bytes
            return audioContents.toByteArray();
        }

    }
}
