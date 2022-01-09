/*
 * Copyright (c) 2017 Eric A. Snell
 *
 * This file is part of eAlvaTag.
 *
 * eAlvaTag is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * eAlvaTag is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with eAlvaTag.  If not,
 * see <http://www.gnu.org/licenses/>.
 */

package ealvatag.utils;

/**
 * Array utility methods
 * <p>
 * Created by Eric A. Snell on 1/24/17.
 */
public class ArrayUtil {

    public static final byte ZERO = 0;

    public static boolean equals(byte[] a, byte[] b, int length) {
        if (a == null || b == null) {
            return false;
        }

        if (length > a.length || length > b.length) {
            throw new ArrayIndexOutOfBoundsException("a:" + a.length + " b:" + b.length + " length:" + length);
        }

        for (int i = 0; i < length; i++) {
            if (a[i] != b[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Fills array {@code a} with {@code val} up to {@code length}
     *
     * @param a      array to fill
     * @param val    value set into each array element
     * @param length set no more than this many elements
     * @return {@code a[]}
     * @throws ArrayIndexOutOfBoundsException if {@code lenght} > {@code a.length}
     */
    public static byte[] fill(byte[] a, byte val, int length) {
        if (length > a.length) {
            throw new ArrayIndexOutOfBoundsException("a:" + a.length + " length:" + length);
        }
        for (int i = 0; i < length; i++) {
            a[i] = val;
        }
        return a;
    }
}
