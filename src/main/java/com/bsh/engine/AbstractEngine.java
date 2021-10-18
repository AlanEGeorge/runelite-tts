package com.bsh.engine;

import java.io.IOException;

public interface AbstractEngine {

    /**
     * This abstract method accepts a string input, and creates an outfile file of the speech.
     * This must be implemented by child classes.
     * @param input text input to convert to speech
     * @param outputStream a pointer to the output stream to write the speech to
     */
    byte[] textToSpeech(String input) throws IOException;

}
