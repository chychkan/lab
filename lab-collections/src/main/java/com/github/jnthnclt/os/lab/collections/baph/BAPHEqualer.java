package com.github.jnthnclt.os.lab.collections.baph;

/**
 *
 * @author jonathan.colt
 */
public class BAPHEqualer {

    public static final BAPHEqualer SINGLETON = new BAPHEqualer();

    public boolean equals(byte[] a, byte[] b, int bOffset, int bLength) {
        if (a == b) {
            return true;
        }
        if (a == null || b == null) {
            return false;
        }

        if (a.length != bLength) {
            return false;
        }

        for (int i = 0; i < bLength; i++) {
            if (a[i] != b[i + bOffset]) {
                return false;
            }
        }
        return true;
    }

}
