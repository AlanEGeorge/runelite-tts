package com.runelitetts;

import groovy.util.logging.Slf4j;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.Properties;

@Slf4j
public class TextSanitizer {

    private static final String PROPS_FILE_NAME="pronunciation.properties";

    private static HashMap<String, String> pronunciationMap = new HashMap<>();

    static {
        try {
            Properties props = new Properties();
            props.load(TextSanitizer.class.getClassLoader().getResourceAsStream(PROPS_FILE_NAME));

            props.forEach((key, value) -> {
                // Load each key and value into the map, inserting a preceding and trailing space to avoid substring issues
                // Example: slanty would be "slant thankyou" since the "ty" is a substring

                // TODO: better find and replace mechanism
                pronunciationMap.put((String)key, (String)value);
                System.out.println("Added to pronunciation map: " + key + " -> " + value);
            });
        } catch (IOException ex) {
            throw new UncheckedIOException("Failed to load file: " + PROPS_FILE_NAME, ex);
        }
    }

    public static String removeFormatting(final String input) {
        return input.replace("<br>", " ");
    }

    public static String adjustPronunciations(final String input) {
        // Make a copy of the input
        String result = removeFormatting(input).toLowerCase();

        for (String key : pronunciationMap.keySet()) {
            result = result.replace(key, pronunciationMap.get(key));
        }

        return result;
    }

}
