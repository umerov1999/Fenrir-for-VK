package dev.ragnarok.fenrir.util;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;


public class InstancesCounter {

    private final Map<Class<?>, AtomicInteger> map;

    public InstancesCounter() {
        map = new HashMap<>();
    }

    public int incrementAndGet(Class<?> c) {
        AtomicInteger counter = map.get(c);
        if (counter == null) {
            counter = new AtomicInteger();
            map.put(c, counter);
        }

        return counter.incrementAndGet();
    }

    public void fireExists(Class<?> c, int id) {
        AtomicInteger counter = map.get(c);

        if (counter == null) {
            counter = new AtomicInteger();
            map.put(c, counter);
        }

        if (counter.get() < id) {
            counter.set(id);
        }
    }
}