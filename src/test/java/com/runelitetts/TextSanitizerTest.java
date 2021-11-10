package com.runelitetts;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Before;
import org.junit.Test;

public class TextSanitizerTest {

    private TextSanitizer textSanitizer;

    @Before
    public void setUp() throws Exception {
        textSanitizer = new TextSanitizer();
    }

    @Test
    public void testSanitizePlayerName() {
        final String input = "this�contains�spaces";
        final String result = textSanitizer.sanitizePlayerName(input);

//        assertFalse(result.contains("�"));
        assertEquals("sniff pantsu", result);
    }

    @Test
    public void testRemoveFormatting() {
        final String input = "<col=0000ff>This is a header</col><br>This would be on a new line.";
        final String result = TextSanitizer.removeFormatting(input);

        assertFalse(result.contains("<br>"));
        assertFalse(result.contains("<col=0000ff>"));
        assertFalse(result.contains("</col>"));
        assertEquals("This is a header.\nThis would be on a new line.", result);
    }

    @Test
    public void testAdjustPronunciations_basic() {
        String input = "fr w/e np thx";
        String result = textSanitizer.adjustPronunciations(input);

        assertFalse(result.contains("<br>"));
        assertEquals("for-real whatever no-problem thanks", result);
    }

    @Test
    public void testAdjustPronunciations_substrings() {
        // Don't replace the terms within valid words
        String input = "french w/east unpleasant guthx";
        String result = textSanitizer.adjustPronunciations(input);
        assertEquals(input, result);
    }

    @Test
    public void testAdjustPronunciations_spaces() {
        String input = "idk, idc";
        String result = textSanitizer.adjustPronunciations(input);
        assertEquals("i don't know, i don't care", result);
    }

}
