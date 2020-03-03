package com.github.jnthnclt.os.lab.io.filer;

import com.google.common.math.IntMath;

import java.io.IOException;

/**
 * All of the methods are intentionally left unsynchronized. Up to caller to do the right thing using the Object returned by lock()
 */
public class HeapFiler implements IFilerBytes {

    private byte[] bytes = new byte[0];
    private long fp = 0;
    private long maxLength = 0;

    public HeapFiler() {
    }

    public HeapFiler(int size) {
        bytes = new byte[size];
        maxLength = 0;
    }

    private HeapFiler(byte[] _bytes, int _maxLength) {
        bytes = _bytes;
        maxLength = _maxLength;
    }

    public static HeapFiler fromBytes(byte[] _bytes, int length) {
        return new HeapFiler(_bytes, length);
    }

    public HeapFiler createReadOnlyClone() {
        HeapFiler heapFiler = new HeapFiler();
        heapFiler.bytes = bytes;
        heapFiler.maxLength = maxLength;
        return heapFiler;
    }

    public byte[] getBytes() {
        if (maxLength == bytes.length) {
            return bytes;
        } else {
            return trim(bytes, (int) maxLength);
        }
    }

    public byte[] copyUsedBytes() {
        return trim(bytes, (int) maxLength);
    }

    public byte[] leakBytes() {
        return bytes;
    }

    public void reset() {
        fp = 0;
        maxLength = 0;
    }

    public void reset(int _maxLength) {
        fp = 0;
        maxLength = _maxLength;
    }

    @Override
    public Object lock() {
        return this;
    }

    @Override
    public int read() throws IOException {
        if (fp + 1 > maxLength) {
            return -1;
        }
        int b = bytes[(int) fp] & 0xFF;
        fp++;
        return b;
    }

    @Override
    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    @Override
    public int read(byte b[], int _offset, int _len) throws IOException {
        if (fp > maxLength) {
            return -1;
        }
        int len = _len;
        if (fp + len > maxLength) {
            len = (int) (maxLength - fp);
        }
        try {
            System.arraycopy(bytes, (int) fp, b, _offset, len);
        } catch (Exception x) {
            System.out.println("Error:" + bytes.length + " srcPos:" + fp + " " + b.length + " dstPos:" + _offset + " legnth:" + len);
            throw x;
        }
        fp += len;
        return len;
    }

    @Override
    public void write(byte b) throws IOException {
        if (fp + 1 > bytes.length) {
            bytes = grow(bytes, Math.max((int) ((fp + 1) - bytes.length), bytes.length));
        }
        bytes[(int)fp] = b;
        fp += 1;
        maxLength = Math.max(maxLength, fp);
    }

    @Override
    public void write(byte _b[], int _offset, int _len) throws IOException {
        if (_b == null) {
            return;
        }
        if (fp + _len > bytes.length) {
            bytes = grow(bytes, Math.max((int) ((fp + _len) - bytes.length), bytes.length));
        }
        System.arraycopy(_b, _offset, bytes, (int) fp, _len);
        fp += _len;
        maxLength = Math.max(maxLength, fp);
    }

    @Override
    public long getFilePointer() throws IOException {
        return fp;
    }

    @Override
    public void seek(long _position) throws IOException {
        fp = _position;
        maxLength = Math.max(maxLength, fp);
    }

    @Override
    public long skip(long _position) throws IOException {
        fp += _position;
        maxLength = Math.max(maxLength, fp);
        return fp;
    }

    @Override
    public void setLength(long len) throws IOException {
        if (len < 0) {
            throw new IOException();
        }
        byte[] newBytes = new byte[(int) len];
        System.arraycopy(bytes, 0, newBytes, 0, Math.min(bytes.length, newBytes.length));
        fp = (int) len;
        bytes = newBytes;
        maxLength = len;
    }

    @Override
    public long length() throws IOException {
        return maxLength;
    }

    @Override
    public void eof() throws IOException {
        bytes = trim(bytes, (int) fp);
        maxLength = fp;
    }

    @Override
    public void close() throws IOException {
    }

    @Override
    public void flush(boolean fsync) throws IOException {
    }

    private static byte[] trim(byte[] src, int count) {
        byte[] newSrc = new byte[count];
        System.arraycopy(src, 0, newSrc, 0, count);
        return newSrc;
    }

    static final public byte[] grow(byte[] src, int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("amount must be greater than zero");
        }
        if (src == null) {
            return new byte[amount];
        }
        byte[] newSrc = new byte[IntMath.checkedAdd(src.length, amount)];
        System.arraycopy(src, 0, newSrc, 0, src.length);
        return newSrc;
    }
}
