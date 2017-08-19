package com.jivesoftware.os.jive.utils.collections.oh;

/**
 *
 * @author jonathan.colt
 */
public class OHMapState<K,V> implements OHState<K,V> {

    private final long capacity;
    private final boolean hasValues;
    private final K nilKey;
    private final Object[] keys;
    private final Object[] values;
    private int count;

    public OHMapState(long capacity, boolean hasValues, K nilKey) {
        this.count = 0;
        this.capacity = capacity;
        this.hasValues = hasValues;
        this.nilKey = nilKey;

        this.keys = new Object[(int) capacity];
        this.values = (hasValues) ? new Object[(int) capacity] : null;
    }

    @Override
    public OHState<K,V> allocate(long capacity) {
        return new OHMapState<>(capacity, hasValues, nilKey);
    }

    @Override
    public K skipped() {
        return nilKey;
    }

    @Override
    public long first() {
        return 0;
    }

    @Override
    public long size() {
        return count;
    }

    @Override
    public void update(long i, K key, V value) {
        keys[(int) i] = key;
        if (hasValues) {
            values[(int) i] = value;
        }
    }

    @Override
    public void link(long i, K key, V value) {
        keys[(int) i] = key;
        if (hasValues) {
            values[(int) i] = value;
        }
        count++;
    }

    @Override
    public void clear(long i) {
        keys[(int) i] = null;
        if (hasValues) {
            values[(int) i] = null;
        }
    }

    @Override
    public void remove(long i, K key, V value) {
        keys[(int) i] = key;
        if (hasValues) {
            values[(int) i] = value;
        }
        count--;
    }

    @Override
    public long next(long i) {
        return (i >= capacity - 1) ? -1 : i + 1;
    }

    @Override
    public long capacity() {
        return capacity;
    }

    @Override
    public K key(long i) {
        return (K) keys[(int) i];
    }

    @Override
    public V value(long i) {
        return (V) (hasValues ? values[(int) i] : keys[(int) i]);
    }

}
