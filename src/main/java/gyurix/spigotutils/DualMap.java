package gyurix.spigotutils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class DualMap<K, V> implements Map<K, V> {
    final HashMap<K, V> keys = new HashMap<>();
    final HashMap<V, K> values = new HashMap<>();

    public void clear() {
        keys.clear();
        values.clear();
    }

    public boolean containsKey(Object key) {
        return keys.containsKey(key);
    }

    public boolean containsValue(Object value) {
        return values.containsKey(value);
    }

    public Set<Entry<K, V>> entrySet() {
        return keys.entrySet();
    }

    public V get(Object key) {
        return keys.get(key);
    }

    public K getKey(V value) {
        return values.get(value);
    }

    public boolean isEmpty() {
        return keys.isEmpty();
    }

    public Set<K> keySet() {
        return keys.keySet();
    }

    public V put(K key, V value) {
        keys.remove(values.get(value));
        V o = keys.put(key, value);
        values.put(value, key);
        return o;
    }

    public void putAll(Map m) {
        keys.putAll(m);
        putAllValue(m);
    }

    private void putAllValue(Map<K, V> m) {
        for (Entry<K, V> e : m.entrySet()) {
            values.put(e.getValue(), e.getKey());
        }
    }

    public V remove(Object key) {
        V o = keys.remove(key);
        values.remove(o);
        return o;
    }

    public K removeValue(Object value) {
        K key = values.remove(value);
        keys.remove(key);
        return key;
    }

    public int size() {
        return keys.size();
    }

    public Collection<V> values() {
        return values.keySet();
    }
}