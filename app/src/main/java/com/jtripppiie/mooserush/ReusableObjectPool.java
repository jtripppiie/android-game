package com.jtripppiie.mooserush;

import java.util.ArrayDeque;
import java.util.Collection;

/**
 * Small allocation-free pool for frequently recycled gameplay objects.
 * Objects are reset by their owner when acquired; the pool only owns storage.
 */
final class ReusableObjectPool<T> {
    interface Factory<T> {
        T create();
    }

    private final ArrayDeque<T> available;
    private final Factory<T> factory;
    private final int maxSize;
    private int createdCount;

    ReusableObjectPool(int maxSize, Factory<T> factory) {
        if (maxSize <= 0 || factory == null) {
            throw new IllegalArgumentException("Pool needs a positive size and factory.");
        }
        this.maxSize = maxSize;
        this.factory = factory;
        this.available = new ArrayDeque<>(maxSize);
    }

    T acquire() {
        T item = available.pollLast();
        if (item != null) {
            return item;
        }
        createdCount++;
        return factory.create();
    }

    void release(T item) {
        if (item != null && available.size() < maxSize) {
            available.addLast(item);
        }
    }

    void releaseAll(Collection<T> activeItems) {
        for (T item : activeItems) {
            release(item);
        }
        activeItems.clear();
    }

    int availableCount() {
        return available.size();
    }

    int createdCount() {
        return createdCount;
    }
}
