package me.qamulex.erl;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RateLimiterMap<K> implements Map<K, RateLimiter> {

    private final RateLimiterBuilder  rateLimiterBuilder;
    private final Map<K, RateLimiter> map;

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }

    @Override
    public Set<Entry<K, RateLimiter>> entrySet() {
        return map.entrySet();
    }

    @Override
    @SuppressWarnings({ "unchecked" })
    public RateLimiter get(Object key) {
        return map.computeIfAbsent(
                (K) key,
                ignored -> rateLimiterBuilder.build()
        );
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public Set<K> keySet() {
        return map.keySet();
    }

    @Override
    public RateLimiter put(K key, RateLimiter value) {
        return map.put(key, value);
    }

    @Override
    public void putAll(Map<? extends K, ? extends RateLimiter> m) {
        throw new UnsupportedOperationException();
    }

    @Override
    public RateLimiter remove(Object key) {
        return map.remove(key);
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public Collection<RateLimiter> values() {
        return map.values();
    }

}
