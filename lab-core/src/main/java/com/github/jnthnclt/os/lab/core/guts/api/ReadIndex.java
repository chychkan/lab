package com.github.jnthnclt.os.lab.core.guts.api;

import com.github.jnthnclt.os.lab.base.BolBuffer;

/**
 *
 * @author jonathan.colt
 */
public interface ReadIndex {

    void release();

    Scanner rangeScan(boolean hashIndexEnabled, boolean pointFrom, byte[] from, byte[] to, BolBuffer entryBuffer, BolBuffer entryKeyBuffer) throws Exception;

    Scanner rowScan(BolBuffer entryBuffer, BolBuffer entryKeyBuffer) throws Exception;

    BolBuffer pointScan(boolean hashIndexEnabled, byte[] key) throws Exception;

    long count() throws Exception;

}
