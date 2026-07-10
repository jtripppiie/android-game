package com.jtripppiie.mooserush;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;

/*
 * SpriteFrameCropper finds the useful pixels inside a sprite sheet frame.
 *
 * Why this matters: many generated sprite sheets have empty transparent padding
 * or accidental neighboring-frame pixels. Tight crops make the runner and stage
 * creatures look centered and prevent little visual artifacts from flashing.
 */
final class SpriteFrameCropper {
    // Pixels with alpha above this count as visible art.
    private static final int ALPHA_THRESHOLD = 12;
    // Add a little room around detected art so feet/hands are not shaved off.
    private static final int CROP_PAD_PX = 4;
    private static final int MIN_COMPONENT_PIXELS = 80;
    private static final int MAX_COMPONENTS = 96;

    private SpriteFrameCropper() {
    }

    static Rect[] computeFrameCrops(Bitmap sheet, int frames) {
        return computeFrameCrops(sheet, frames, 0);
    }

    static Rect[] computeFrameCrops(Bitmap sheet, int frames, int seamGuardPx) {
        return computeFrameCrops(sheet, frames, seamGuardPx, false);
    }

    static Rect[] computeMainFrameCrops(Bitmap sheet, int frames, int seamGuardPx) {
        return computeFrameCrops(sheet, frames, seamGuardPx, true);
    }

    /**
     * Tight bounding box of every visible pixel inside each uniform frame cell.
     * Unlike the connected-component crops, this keeps detached content such as
     * lifted running feet, and it never trims across a cell seam because the scan
     * is confined to each cell. Frames are authored to stay inside their cells, so
     * the returned rects are complete and free of neighbouring-frame bleed.
     */
    static Rect[] computeCellContentCrops(Bitmap sheet, int frames, int padPx) {
        Rect[] crops = new Rect[Math.max(0, frames)];
        if (sheet == null || frames <= 0 || sheet.getWidth() <= 0 || sheet.getHeight() <= 0) {
            return crops;
        }
        int height = sheet.getHeight();
        for (int frame = 0; frame < frames; frame++) {
            int rawLeft = Math.round(sheet.getWidth() * (frame / (float) frames));
            int rawRight = Math.round(sheet.getWidth() * ((frame + 1) / (float) frames));
            int minX = rawRight;
            int minY = height;
            int maxX = rawLeft - 1;
            int maxY = -1;
            for (int y = 0; y < height; y++) {
                for (int x = rawLeft; x < rawRight; x++) {
                    if (!isVisible(sheet, x, y)) {
                        continue;
                    }
                    if (x < minX) minX = x;
                    if (x > maxX) maxX = x;
                    if (y < minY) minY = y;
                    if (y > maxY) maxY = y;
                }
            }
            if (maxX < minX || maxY < minY) {
                crops[frame] = new Rect(rawLeft, 0, rawRight, height);
            } else {
                crops[frame] = new Rect(
                        Math.max(rawLeft, minX - padPx),
                        Math.max(0, minY - padPx),
                        Math.min(rawRight, maxX + 1 + padPx),
                        Math.min(height, maxY + 1 + padPx)
                );
            }
        }
        return crops;
    }

    private static Rect[] computeFrameCrops(Bitmap sheet, int frames, int seamGuardPx, boolean mainComponentOnly) {
        /*
         * Divide the sheet into equal frame cells, crop each cell, then clamp the
         * crop away from seams so one frame does not steal pixels from another.
         */
        Rect[] crops = new Rect[frames];
        if (sheet == null || frames <= 0 || sheet.getWidth() <= 0 || sheet.getHeight() <= 0) {
            return crops;
        }
        for (int frame = 0; frame < frames; frame++) {
            int rawLeft = Math.round(sheet.getWidth() * (frame / (float) frames));
            int rawRight = Math.round(sheet.getWidth() * ((frame + 1) / (float) frames));
            crops[frame] = componentBounds(sheet, rawLeft, rawRight, mainComponentOnly);
            if (crops[frame] == null) {
                crops[frame] = new Rect(rawLeft, 0, rawRight, sheet.getHeight());
            }
            clampInternalSeams(crops[frame], rawLeft, rawRight, frame, frames, seamGuardPx);
        }
        return crops;
    }

    private static void clampInternalSeams(Rect crop, int rawLeft, int rawRight, int frame, int frames, int seamGuardPx) {
        if (crop == null || seamGuardPx <= 0) {
            return;
        }

        int left = crop.left;
        int right = crop.right;
        if (frame > 0) {
            left = Math.max(left, rawLeft + seamGuardPx);
        }
        if (frame < frames - 1) {
            right = Math.min(right, rawRight - seamGuardPx);
        }
        if (right > left) {
            crop.set(left, crop.top, right, crop.bottom);
        }
    }

    private static Rect componentBounds(Bitmap sheet, int rawLeft, int rawRight, boolean mainComponentOnly) {
        /*
         * Connected-component search means:
         * find blobs of visible pixels that touch each other, measure each blob,
         * and keep the useful one. This is how we can ignore tiny artifacts.
         */
        int width = rawRight - rawLeft;
        int height = sheet.getHeight();
        if (width <= 0 || height <= 0) {
            return null;
        }

        boolean[] visited = new boolean[width * height];
        int[] queueX = new int[width * height];
        int[] queueY = new int[width * height];
        Rect[] bounds = new Rect[MAX_COMPONENTS];
        int[] counts = new int[MAX_COMPONENTS];
        boolean[] touchesEdge = new boolean[MAX_COMPONENTS];
        int componentCount = 0;
        int bestCount = 0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int index = y * width + x;
                if (visited[index] || !isVisible(sheet, rawLeft + x, y)) {
                    visited[index] = true;
                    continue;
                }

                int head = 0;
                int tail = 0;
                int count = 0;
                int minX = x;
                int maxX = x;
                int minY = y;
                int maxY = y;
                queueX[tail] = x;
                queueY[tail] = y;
                tail++;
                visited[index] = true;

                while (head < tail) {
                    int currentX = queueX[head];
                    int currentY = queueY[head];
                    head++;
                    count++;
                    minX = Math.min(minX, currentX);
                    maxX = Math.max(maxX, currentX);
                    minY = Math.min(minY, currentY);
                    maxY = Math.max(maxY, currentY);

                    tail = visitNeighbor(sheet, rawLeft, width, height, currentX - 1, currentY, visited, queueX, queueY, tail);
                    tail = visitNeighbor(sheet, rawLeft, width, height, currentX + 1, currentY, visited, queueX, queueY, tail);
                    tail = visitNeighbor(sheet, rawLeft, width, height, currentX, currentY - 1, visited, queueX, queueY, tail);
                    tail = visitNeighbor(sheet, rawLeft, width, height, currentX, currentY + 1, visited, queueX, queueY, tail);
                }

                if (componentCount < MAX_COMPONENTS) {
                    bounds[componentCount] = new Rect(
                            rawLeft + Math.max(0, minX - CROP_PAD_PX),
                            Math.max(0, minY - CROP_PAD_PX),
                            rawLeft + Math.min(width, maxX + 1 + CROP_PAD_PX),
                            Math.min(height, maxY + 1 + CROP_PAD_PX)
                    );
                    counts[componentCount] = count;
                    touchesEdge[componentCount] = minX <= 1 || maxX >= width - 2;
                    componentCount++;
                }
                bestCount = Math.max(bestCount, count);
            }
        }

        Rect union = null;
        int bestIndex = -1;
        for (int i = 0; i < componentCount; i++) {
            if (bestIndex < 0 || counts[i] > counts[bestIndex]) {
                bestIndex = i;
            }
        }
        if (mainComponentOnly && bestIndex >= 0) {
            return new Rect(bounds[bestIndex]);
        }

        for (int i = 0; i < componentCount; i++) {
            if (counts[i] < MIN_COMPONENT_PIXELS) {
                continue;
            }
            if (touchesEdge[i] && counts[i] < bestCount * 0.35f) {
                continue;
            }
            if (union == null) {
                union = new Rect(bounds[i]);
            } else {
                union.union(bounds[i]);
            }
        }
        return union;
    }

    private static int visitNeighbor(Bitmap sheet, int rawLeft, int width, int height, int x, int y,
                                     boolean[] visited, int[] queueX, int[] queueY, int tail) {
        if (x < 0 || y < 0 || x >= width || y >= height) {
            return tail;
        }
        int index = y * width + x;
        if (visited[index]) {
            return tail;
        }
        visited[index] = true;
        if (!isVisible(sheet, rawLeft + x, y)) {
            return tail;
        }
        queueX[tail] = x;
        queueY[tail] = y;
        return tail + 1;
    }

    private static boolean isVisible(Bitmap sheet, int x, int y) {
        return Color.alpha(sheet.getPixel(x, y)) > ALPHA_THRESHOLD;
    }
}
