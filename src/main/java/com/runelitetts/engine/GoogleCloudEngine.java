package com.runelitetts.engine;

import com.google.cloud.texttospeech.v1.AudioConfig;
import com.google.cloud.texttospeech.v1.AudioEncoding;
import com.google.cloud.texttospeech.v1.SsmlVoiceGender;
import com.google.cloud.texttospeech.v1.SynthesisInput;
import com.google.cloud.texttospeech.v1.SynthesizeSpeechResponse;
import com.google.cloud.texttospeech.v1.TextToSpeechClient;
import com.google.cloud.texttospeech.v1.VoiceSelectionParams;
import com.google.protobuf.ByteString;

import lombok.extern.slf4j.Slf4j;

import java.io.*;

@Slf4j
public class GoogleCloudEngine extends AbstractEngine {

    public static final String GOOGLE_ENV_NAME = "GOOGLE_APPLICATION_CREDENTIALS";

    // Google Cloud API
    private final TextToSpeechClient textToSpeechClient;
    private final VoiceSelectionParams npcVoice;
    private final VoiceSelectionParams playerVoice;
    private final AudioConfig audioConfig;

    public GoogleCloudEngine() {
        try {
            textToSpeechClient = TextToSpeechClient.create();

            // Construct the voices for NPCs and players
            npcVoice = VoiceSelectionParams.newBuilder()
                            .setLanguageCode("en-US")
                            .setSsmlGender(SsmlVoiceGender.MALE)
                            .setName("en-US-Wavenet-B")
                            .build();

            playerVoice = VoiceSelectionParams.newBuilder()
                    .setLanguageCode("en-US")
                    .setSsmlGender(SsmlVoiceGender.MALE)
                    .setName("en-US-Wavenet-A")
                    .build();

            // Configure the API to return MP3s
            audioConfig = AudioConfig.newBuilder().setAudioEncoding(AudioEncoding.MP3).build();

        } catch (IOException ex) {
            throw new RuntimeException("Failed to construct GoogleCloudEngine", ex);
        }
    }

    @Override
    public byte[] textToAudio(final SpeechType speechType, final String input) throws IOException {

        // Set the text input to be synthesized
        SynthesisInput synthesisInput = SynthesisInput.newBuilder().setText(input).build();

        SynthesizeSpeechResponse response = null;

        switch (speechType) {
            case PLAYER_MAN:
                response = textToSpeechClient.synthesizeSpeech(synthesisInput, playerVoice, audioConfig);
                break;
            case NPC_MAN:
                response = textToSpeechClient.synthesizeSpeech(synthesisInput, npcVoice, audioConfig);
                break;
            default:
                throw new IOException("Unsupported speech type: " + speechType.toString());
        }

        // Get the audio contents from the response
        ByteString audioContents = response.getAudioContent();

        // Return the Mp3 as bytes
        return audioContents.toByteArray();
    }
}
