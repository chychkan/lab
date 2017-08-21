package com.github.jnthnclt.os.lab.collections.bah;

import java.util.Arrays;

/**
 *
 * @author jonathan.colt
 */
public class BAHLinkedMapState<V> implements BAHState<V> {

    public static final byte[] NIL = new byte[0];

    private final long capacity;
    private final boolean hasValues;
    private final byte[] nilKey;

    private final byte[][] keys;
    private long firstKeyIndex = -1;
    private final int[] priorKeyIndex;
    private final int[] nextKeyIndex;
    private long lastKeyIndex = -1;
    private final Object[] values;
    private int count;

    public BAHLinkedMapState(long capacity, boolean hasValues, byte[] nilKey) {
        this.count = 0;
        this.capacity = capacity;
        this.hasValues = hasValues;
        this.nilKey = nilKey;

        this.keys = new byte[(int) capacity][];
        this.firstKeyIndex = -1;
        this.priorKeyIndex = new int[(int) capacity];
        Arrays.fill(priorKeyIndex, -1);
        this.nextKeyIndex = new int[(int) capacity];
        Arrays.fill(nextKeyIndex, -1);
        this.lastKeyIndex = -1;
        this.values = (hasValues) ? new Object[(int) capacity] : null;
    }

    @Override
    public BAHState<V> allocate(long capacity) {
        return new BAHLinkedMapState<>(capacity, hasValues, nilKey);
    }


    @Override
    public void clear(long i) {
        keys[(int) i] = null;
        values[(int) i] = null;
    }

    @Override
    public byte[] skipped() {
        return nilKey;
    }

    @Override
    public long first() {
        return firstKeyIndex;
    }

    @Override
    public long size() {
        return count;
    }

    @Override
    public void update(long i, byte[] key, V value) {
        keys[(int) i] = key;
        if (hasValues) {
            values[(int) i] = value;
        }
    }

    @Override
    public void link(long i, byte[] key, V value) {
        if (firstKeyIndex == -1) {
            firstKeyIndex = i;
        }
        priorKeyIndex[(int) i] = (int)lastKeyIndex;
        if (lastKeyIndex != -1) {
            nextKeyIndex[(int) lastKeyIndex] = (int)i;
        }
        nextKeyIndex[(int) i] = -1;
        lastKeyIndex = i;

        keys[(int) i] = key;
        if (hasValues) {
            values[(int) i] = value;
        }
        count++;
    }

    @Override
    public void remove(long i, byte[] key, V value) {
        if (i == firstKeyIndex) {
            firstKeyIndex = nextKeyIndex[(int) i];
        }

        if (i == lastKeyIndex) {
            lastKeyIndex = priorKeyIndex[(int) i];
        }

        long prior = priorKeyIndex[(int) i];
        long next = nextKeyIndex[(int) i];

        if (prior != -1) {
            nextKeyIndex[(int) prior] = (int)next;
        }
        if (next != -1) {
            priorKeyIndex[(int) next] = (int)prior;
        }

        nextKeyIndex[(int) i] = -1;
        priorKeyIndex[(int) i] = -1;

        keys[(int) i] = key;
        if (hasValues) {
            values[(int) i] = value;
        }
        count--;
    }

    @Override
    public long next(long i) {
        return nextKeyIndex[(int) i];
    }

    @Override
    public long capacity() {
        return capacity;
    }

    @Override
    public byte[] key(long i) {
        return keys[(int) i];
    }

    @Override
    @SuppressWarnings("unchecked")
    public V value(long i) {
        return (V) (hasValues ? values[(int) i] : keys[(int) i]);
    }

}
