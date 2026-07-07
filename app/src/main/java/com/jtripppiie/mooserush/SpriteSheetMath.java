package com.jtripppiie.mooserush;

final class SpriteSheetMath {
    private static final int SPRITE_EDGE_GUARD_PX = 14;
    private static final int SPRITE_TRIM_INSET_PX = 6;

    private SpriteSheetMath() {
    }

    static int[] trimmedSourceValues(int safeFrame, int frameWidth, int sheetHeight, int[] trim) {
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
