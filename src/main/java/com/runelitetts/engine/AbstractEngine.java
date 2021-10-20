package com.runelitetts.engine;

import java.io.IOException;

public abstract class AbstractEngine {

    public enum SpeechType {
        PLAYER_MAN,
        PLAYER_FEMALE,
        NPC_MAN,
        NPC_WOMAN,
        NPC_CHILD,
        NPC_MONSTER
    }

    /**
     * This abstract method accepts a string input, and creates an outfile file of the speech.
     * This must be implemented by child classes.
     * @param input text input to convert to speech
     * @param outputStream a pointer to the output stream to write the speech to
     */
    public abstract byte[] textToMp3Bytes(final SpeechType speechType, final String input) throws IOException;

}
