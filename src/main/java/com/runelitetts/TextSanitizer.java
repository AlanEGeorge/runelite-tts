package com.runelitetts;

import com.google.api.gax.rpc.InvalidArgumentException;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Properties;

@Slf4j
public class TextSanitizer {

    private static final String PROPS_FILE_NAME="pronunciation.properties";

    // Pattern borrowed from (top answer + comment):
    // https://stackoverflow.com/questions/2206378/how-to-split-a-string-but-also-keep-the-delimiters
    private static final String REGEX_KEEP_DELIMITER = "((?<=%1$s)|(?=%1$s))";

    private HashMap<String, String> literalPronunciationMap;

    public TextSanitizer() throws IOException {
        literalPronunciationMap = new HashMap<>();

        Properties props = new Properties();
        props.load(TextSanitizer.class.getClassLoader().getResourceAsStream(PROPS_FILE_NAME));

        props.forEach((literal, pronunciation) -> {
            literalPronunciationMap.put((String)literal, (String)pronunciation);
            log.debug("Added to pronunciation map: " + literal + " -> " + pronunciation);
        });
    }

    public static String sanitizePlayerName(final String input) {
        if (input == null) {
            throw new IllegalArgumentException("Input was null");
        }

        String result = input;

        // strips off all non-ASCII characters
        result = result.replaceAll("[^\\x00-\\x7F]", " ");

        // erases all the ASCII control characters
        result = result.replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", " ");

        // removes non-printable characters from Unicode
        result = result.replaceAll("\\p{C}", " ");

        return result;
    }

    public static String removeFormatting(final String input) {
        String result = input.replaceAll("<br>", "\\\n");
        result = result.replace("<img=[0-9]+>", ""); // Iron man images
        result = result.replaceAll("</col>", ".");
        result = result.replaceAll("<col=[0-9a-f]+>", "");

        return result;
    }

    public String adjustPronunciations(final String input) {
        // Make a copy of the input, and make it lower case such that case doesn't affect adjustment
        String cleanInput = removeFormatting(input).toLowerCase();

        // Tokenize the string to ensure we're not replacing characters within existing words

        String[] tokens = cleanInput.split(String.format (REGEX_KEEP_DELIMITER, "[\"?!., ]"));

        log.debug("Input string: " + input);
        log.debug("Tokens: " + Arrays.toString(tokens));
        for (int i = 0; i < tokens.length; i++) {
            String token = tokens[i];
            if (literalPronunciationMap.keySet().contains(token)) {
                String replacement = literalPronunciationMap.get(token);
                log.debug("Replaced " + token + " with " + replacement);
                tokens[i] = replacement;
            }
        }

        // Combine tokens into single string
        final String result = String.join("", tokens);

        log.debug("Result string: " + result);

        return result;
    }

}
