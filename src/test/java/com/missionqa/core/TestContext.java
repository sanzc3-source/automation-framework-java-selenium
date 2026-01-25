package com.missionqa.core;

import java.util.HashMap;
import java.util.Map;

public class TestContext {
    private final Map<String, Object> data = new HashMap<>();

    public void set(String key, Object value) {
        data.put(key, value);
    }

    // 1-arg getter (what your ApiSteps is calling)
    public Object get(String key) {
        return data.get(key);
    }

    // Typed getter (still available if you want it)
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> clazz) {
        return (T) data.get(key);
    }
}
