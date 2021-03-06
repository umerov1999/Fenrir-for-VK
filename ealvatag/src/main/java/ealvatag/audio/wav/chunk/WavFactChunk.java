/*
 * Entagged Audio Tag library
 * Copyright (c) 2003-2005 Rapha�l Slinckx <raphael@slinckx.net>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package ealvatag.audio.wav.chunk;

import java.io.IOException;
import java.nio.ByteBuffer;

import ealvatag.audio.GenericAudioHeader;
import ealvatag.audio.Utils;
import ealvatag.audio.iff.Chunk;
import ealvatag.audio.iff.ChunkHeader;

/**
 * Reads the fact header, this contains the information required for constructing Audio header
 * <p>
 * 0 - 3   uint   totalNoSamples (Per channel ?)
 */
public class WavFactChunk extends Chunk {
    private final boolean isValid = false;

    private final GenericAudioHeader info;

    public WavFactChunk(ByteBuffer chunkData, ChunkHeader hdr, GenericAudioHeader info) throws IOException {
        super(chunkData, hdr);
        this.info = info;
    }

    public boolean readChunk() throws IOException {
        info.setNoOfSamples(Utils.convertUnsignedIntToLong(chunkData.getInt()));
        return true;
    }


    public String toString() {
        String out = "Fact Chunk:\n";
        out += "Is valid?: " + isValid;
        return out;
    }
}
