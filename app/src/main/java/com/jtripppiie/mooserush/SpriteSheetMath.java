package com.jtripppiie.mooserush;

final class SpriteSheetMath {
    private static final int SPRITE_EDGE_GUARD_PX = 2;

    private SpriteSheetMath() {
    }

    static int[] trimmedSourceValues(int safeFrame, int frameWidth, int sheetHeight, int[] trim) {
        int left = trim[0] == 0 ? SPRITE_EDGE_GUARD_PX : trim[0];
        int top = trim[1];
        int right = trim[2] >= frameWidth ? frameWidth - SPRITE_EDGE_GUARD_PX : trim[2];
        int bottom = Math.min(trim[3], sheetHeight);
        if (right <= left || bottom <= top) {
            return new int[]{safeFrame * frameWidth, 0, (safeFrame + 1) * frameWidth, sheetHeight};
        }
        return new int[]{safeFrame * frameWidth + left, top, safeFrame * frameWidth + right, bottom};
    }
}
