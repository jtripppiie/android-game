package com.jtripppiie.mooserush;

/** Pure formatting rules for the persistent in-game review notebook. */
final class DebugReviewNotes {
    private DebugReviewNotes() {
    }

    static String formatEntry(String timestamp, String context, String note, boolean priority) {
        String safeTimestamp = timestamp == null ? "unknown time" : timestamp.trim();
        String safeContext = context == null ? "context unavailable" : context.trim();
        String safeNote = note == null ? "" : note.trim();
        return "--- " + safeTimestamp + (priority ? " [PRIORITY]" : " [NOTE]") + " ---\n"
                + safeContext + "\n" + safeNote + "\n\n";
    }
}
