package com.jtripppiie.mooserush;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

public class ReusableObjectPoolTest {
    @Test
    public void releasedObjectIsReused() {
        ReusableObjectPool<Object> pool = new ReusableObjectPool<>(2, Object::new);
        Object first = pool.acquire();
        pool.release(first);
        assertSame(first, pool.acquire());
        assertEquals(1, pool.createdCount());
    }

    @Test
    public void releaseAllClearsActiveCollectionAndHonorsCap() {
        ReusableObjectPool<Object> pool = new ReusableObjectPool<>(2, Object::new);
        List<Object> active = new ArrayList<>();
        active.add(new Object());
        active.add(new Object());
        active.add(new Object());
        pool.releaseAll(active);
        assertEquals(0, active.size());
        assertEquals(2, pool.availableCount());
    }
}
