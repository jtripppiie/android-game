package com.jtripppiie.mooserush;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class DebugReviewNotesTest {
    @Test
    public void entryKeepsPriorityContextAndItemIds() {
        String entry = DebugReviewNotes.formatEntry("2026-07-15 12:30:00",
                "stage=Bear Country | visible=BER-AN02, BER-PF03",
                " animal arrives too early ", true);
        assertTrue(entry.contains("[PRIORITY]"));
        assertTrue(entry.contains("BER-AN02, BER-PF03"));
        assertTrue(entry.contains("animal arrives too early"));
    }
}
