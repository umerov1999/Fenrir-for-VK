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

import java.io.IOException;
import java.nio.charset.Charset;

import okio.Buffer;
import okio.BufferedSource;

/**
 * Utility okio methods
 * <p>
 * Created by Eric A. Snell on 2/4/17.
 */
public final class Buffers {
    private Buffers() {
    }

    public static Buffer makeBufferFrom(BufferedSource source, long byteCount) throws IOException {
        Buffer buffer = new Buffer();
        source.readFully(buffer, byteCount);
        return buffer;
    }

    public static String peekString(BufferedSource bufferedSource,
                                    int offset,
                                    int count,
                                    Charset charset) throws IOException {
        bufferedSource.require(offset + count);
        byte[] bytes = new byte[count];
        Buffer buffer = bufferedSource.buffer();
        for (int i = 0; i < count; i++) {
            bytes[i] = buffer.getByte(offset + i);
        }
        return new String(bytes, charset);
    }

    /**
     * Big-endian - everything should assume BE and only specify LE when necessary (which I cannot find as of yet)
     *
     * @return 3 bytes properly masked and shifted to form an int
     * @throws IOException if a read error occurs
     */
    public static int read3ByteInt(BufferedSource source) throws IOException {
        return (source.readByte() & 0xff) << 16
                | (source.readByte() & 0xff) << 8
                | (source.readByte() & 0xff);

    }

    public static int peek3ByteInt(BufferedSource bufferedSource, int offset) throws IOException {
        bufferedSource.require(offset + 3);
        Buffer buffer = bufferedSource.buffer();
        return (buffer.getByte(offset) & 0xff) << 16
                | (buffer.getByte(offset + 1) & 0xff) << 8
                | (buffer.getByte(offset + 2) & 0xff);
    }

}
