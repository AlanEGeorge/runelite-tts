package com.runelitetts;

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

    public static String removeFormatting(final String input) {
        String result = input.replaceAll("<br>", "\\\n");
        result = result.replaceAll("</col>", ".");
        result = result.replaceAll("<col=[0-9a-f]+>", "");

        return result;
    }

    public String adjustPronunciations(final String input) {
        // Make a copy of the input, and make it lower case such that case doesn't affect adjustment
        String cleanInput = removeFormatting(input).toLowerCase();

        // Tokenize the string to ensure we're not replacing characters within existing words

        String[] tokens = cleanInput.split(String.format (REGEX_KEEP_DELIMITER, "[\"?!., ]"));

        log.info("Input string: " + input);
        log.info("Tokens: " + Arrays.toString(tokens));
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

        log.info("Result string: " + result);

        return result;
    }

}
