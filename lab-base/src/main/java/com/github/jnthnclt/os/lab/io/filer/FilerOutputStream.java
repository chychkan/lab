package com.github.jnthnclt.os.lab.io.filer;

import java.io.IOException;
import java.io.OutputStream;

/**
 *
 * @author jonathan.colt
 */
public class FilerOutputStream implements IWriteableBytes {

    private final OutputStream outputStream;

    public FilerOutputStream(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    public void write(byte b) throws IOException {
        outputStream.write(b);
    }

    @Override
    public void write(byte[] b, int _offset, int _len) throws IOException {
        outputStream.write(b, _offset, _len);
    }

    @Override
    public void flush(boolean fsync) throws IOException {
        outputStream.flush();
    }

    @Override
    public void close() throws IOException {
        outputStream.close();
    }

    @Override
    public Object lock() {
        return outputStream;
    }

    @Override
    public void seek(long position) throws IOException {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public long length() throws IOException {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public long getFilePointer() throws IOException {
        throw new UnsupportedOperationException("Not supported.");
    }

}
