package com.bsh.engines;

import java.io.IOException;
import java.io.OutputStream;

public interface AbstractEngine {

    /**
     * This abstract method accepts a string input, and creates an outfile file of the speech.
     * This must be implemented by child classes.
     * @param input text input to convert to speech
     * @param outputStream a pointer to the output stream to write the speech to
     */
    void textToSpeech(String input, OutputStream outputStream) throws IOException;

}
