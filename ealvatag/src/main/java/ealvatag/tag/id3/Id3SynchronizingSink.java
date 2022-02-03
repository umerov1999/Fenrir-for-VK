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

package ealvatag.tag.id3;

import androidx.annotation.NonNull;

import java.io.IOException;

import okio.Buffer;
import okio.BufferedSink;
import okio.ForwardingSink;

/**
 * Synchronize an array of bytes in an ID3 buffer
 * <p>
 * Any patterns of the form $FF $00 should be replaced by $FF
 * <p>
 * Created by Eric A. Snell on 1/24/17.
 */
@SuppressWarnings("WeakerAccess")
public class Id3SynchronizingSink extends ForwardingSink {
    // keep as bytes to not worry with casting
    private static final byte FF = (byte) 0xFF;
    private static final byte ZERO = (byte) 0x00;
    private final BufferedSink sink;
    private boolean lastByteWasFF;

    /**
     * This is a {@link ForwardingSink} which transforms the stream of bytes substituting every occurrence of [0xFF, 0x00] with [0xFF]. This
     * is synchronizing an ID3 tag
     *
     * @param delegate the {@link BufferedSink} the transformed bytes written to
     */
    public Id3SynchronizingSink(BufferedSink delegate) {
        super(delegate);
        sink = delegate;
    }

    public static Buffer synchronizeBuffer(Buffer buffer) throws IOException {
        Buffer syncBuffer = new Buffer();
        Id3SynchronizingSink sink = new Id3SynchronizingSink(syncBuffer);
        buffer.readAll(sink);
        return syncBuffer;
    }

    @Override
    public void write(@NonNull Buffer source, long byteCount) throws IOException {
        for (int i = 0; i < byteCount; i++) {
            byte current = source.readByte();
            if (lastByteWasFF) {
                sink.writeByte(FF);
                lastByteWasFF = false;
                if (ZERO != current) {
                    if (FF != current) {
                        lastByteWasFF = false;
                        sink.writeByte(current);
                    } else {
                        lastByteWasFF = true;
                    }
                }
            } else {
                if (FF == current) {
                    lastByteWasFF = true;
                } else {
                    sink.writeByte(current);
                }
            }
        }
    }

    @Override
    public void flush() throws IOException {
        if (lastByteWasFF) {
            sink.writeByte(0xFF);
            lastByteWasFF = false;
        }
        sink.flush();
    }

    @Override
    public void close() throws IOException {
        flush();
    }
}
