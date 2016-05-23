package gyurix.spigotutils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class DualMap<K, V> implements Map<K, V> {
    final HashMap<K, V> keys = new HashMap<>();
    final HashMap<V, K> values = new HashMap<>();

    public void clear() {
        this.keys.clear();
        this.values.clear();
    }

    public boolean containsKey(Object key) {
        return this.keys.containsKey(key);
    }

    public boolean containsValue(Object value) {
        return this.values.containsKey(value);
    }

    public Set<Map.Entry<K, V>> entrySet() {
        return this.keys.entrySet();
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
        this.keys.remove(this.values.get(value));
        V o = this.keys.put(key, value);
        this.values.put(value, key);
        return o;
    }

    public void putAll(Map m) {
        this.keys.putAll(m);
        putAllValue(m);
    }

    private void putAllValue(Map<K, V> m) {
        for (Map.Entry<K, V> e : m.entrySet()) {
            this.values.put(e.getValue(), e.getKey());
        }
    }

    public V remove(Object key) {
        V o = this.keys.remove(key);
        this.values.remove(o);
        return o;
    }

    public K removeValue(Object value) {
        K key = this.values.remove(value);
        this.keys.remove(key);
        return key;
    }

    public int size() {
        return this.keys.size();
    }

    public Collection<V> values() {
        return this.values.keySet();
    }
}