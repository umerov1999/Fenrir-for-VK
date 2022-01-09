/*
 * @author : Paul Taylor
 * @author : Eric Farng
 * <p>
 * Version @version:$Id$
 * <p>
 * MusicTag Copyright (C)2003,2004
 * <p>
 * This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public  License as
 * published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version.
 * <p>
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public License along with this library; if not, you can get a copy from
 * http://www.opensource.org/licenses/lgpl-license.php or write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
 * Boston, MA 02110-1301 USA
 * <p>
 * Description:
 */
package ealvatag.tag.datatype;

import java.io.EOFException;

import ealvatag.tag.InvalidDataTypeException;
import ealvatag.tag.id3.AbstractTagFrameBody;
import okio.Buffer;

/**
 * Represents a bit flag within a byte
 */
public class BooleanByte extends AbstractDataType {
    private int bitPosition = -1;

    public BooleanByte(String identifier, AbstractTagFrameBody frameBody, int bitPosition) {
        super(identifier, frameBody);
        if ((bitPosition < 0) || (bitPosition > 7)) {
            throw new IndexOutOfBoundsException("Bit position needs to be from 0 - 7 : " + bitPosition);
        }

        this.bitPosition = bitPosition;
    }

    public int getSize() {
        return 1;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof BooleanByte)) {
            return false;
        }

        BooleanByte object = (BooleanByte) obj;

        return bitPosition == object.bitPosition && super.equals(obj);

    }

    public void readByteArray(byte[] arr, int offset) throws InvalidDataTypeException {
        if (arr == null) {
            throw new NullPointerException("Byte array is null");
        }

        if ((offset < 0) || (offset >= arr.length)) {
            throw new IndexOutOfBoundsException("Offset to byte array is out of bounds: offset = " + offset + ", array.length = " + arr.length);
        }

        byte newValue = arr[offset];

        newValue >>= bitPosition;
        newValue &= 0x1;
        value = newValue == 1;
    }

    @Override
    public void read(Buffer buffer, int size) throws EOFException, InvalidDataTypeException {
        byte newValue = buffer.readByte();
        newValue >>= bitPosition;
        newValue &= 0x1;
        value = newValue == 1;
    }

    public String toString() {
        return "" + value;
    }

    public byte[] writeByteArray() {
        byte[] retValue;

        retValue = new byte[1];

        if (value != null) {
            retValue[0] = (byte) ((Boolean) value ? 1 : 0);
            retValue[0] <<= bitPosition;
        }

        return retValue;
    }
}
