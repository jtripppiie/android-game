package com.jtripppiie.mooserush;

/*
 * SpriteSheetMath converts a frame number plus trim values into a source rect.
 *
 * Source rect = the pixels we copy from the sprite sheet.
 * Destination rect = where those pixels get drawn on the Canvas.
 */
final class SpriteSheetMath {
    // Edge guards avoid sampling neighboring frames at the sheet borders.
    private static final int SPRITE_EDGE_GUARD_PX = 14;
    // Trim inset shaves off a few extra transparent/artifact pixels.
    private static final int SPRITE_TRIM_INSET_PX = 6;

    private SpriteSheetMath() {
    }

    static int[] trimmedSourceValues(int safeFrame, int frameWidth, int sheetHeight, int[] trim) {
        /*
         * trim is {left, top, right, bottom} inside one frame. We add safeFrame *
         * frameWidth at the end to move from "inside the frame" coordinates to
         * "inside the whole sprite sheet" coordinates.
         */
        int left = (trim[0] == 0 ? SPRITE_EDGE_GUARD_PX : trim[0]) + SPRITE_TRIM_INSET_PX;
        int top = trim[1] + SPRITE_TRIM_INSET_PX;
        int right = (trim[2] >= frameWidth ? frameWidth - SPRITE_EDGE_GUARD_PX : trim[2]) - SPRITE_TRIM_INSET_PX;
        int bottom = Math.min(trim[3], sheetHeight) - SPRITE_TRIM_INSET_PX;
        if (right <= left || bottom <= top) {
            return new int[]{safeFrame * frameWidth, 0, (safeFrame + 1) * frameWidth, sheetHeight};
        }
        return new int[]{safeFrame * frameWidth + left, top, safeFrame * frameWidth + right, bottom};
    }
}
