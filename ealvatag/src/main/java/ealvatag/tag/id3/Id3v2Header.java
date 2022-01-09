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

/**
 * The first part of the ID3v2 tag is the 10 byte tag header, laid out
 * as follows:
 * <p>
 * ID3v2/file identifier      "ID3"
 * ID3v2 version              $04 00
 * ID3v2 flags                %abcd0000
 * ID3v2 size             4 * %0xxxxxxx
 * <p>
 * The first three bytes of the tag are always "ID3", to indicate that
 * this is an ID3v2 tag, directly followed by the two version bytes. The
 * first byte of ID3v2 version is its major version, while the second
 * byte is its revision number. In this case this is ID3v2.4.0. All
 * revisions are backwards compatible while major versions are not. If
 * software with ID3v2.4.0 and below support should encounter version
 * five or higher it should simply ignore the whole tag. Version or
 * revision will never be $FF.
 * <p>
 * The version is followed by the ID3v2 flags field, which is version specific.
 * <p>
 * The ID3v2 tag size is stored as a 32 bit synchsafe integer (section
 * 6.2), making a total of 28 effective bits (representing up to 256MB).
 * <p>
 * The ID3v2 tag size is the sum of the byte length of the extended
 * header, the padding and the frames after unsynchronisation. If a
 * footer is present this equals to ('total size' - 20) bytes, otherwise
 * ('total size' - 10) bytes.
 */
public class Id3v2Header {
    private final byte majorVersion;
    private final byte revision;
    private final byte flags;
    private final int tagSize;

    public Id3v2Header(byte majorVersion, byte revision, byte flags, int tagSize) {
        this.majorVersion = majorVersion;
        this.revision = revision;
        this.flags = flags;
        this.tagSize = tagSize;
    }

    public byte getMajorVersion() {
        return majorVersion;
    }

    public byte getRevision() {
        return revision;
    }

    public byte getFlags() {
        return flags;
    }

    public int getTagSize() {
        return tagSize;
    }
}
