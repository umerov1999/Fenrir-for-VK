/**
 * This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public  License as published by the Free Software Foundation; either version 2.1 of the License,
 * or (at your option) any later version.
 * <p>
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public License along with this library; if not,
 * you can get a copy from http://www.opensource.org/licenses/lgpl-license.php or write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 */
package ealvatag.tag.datatype;

import java.io.EOFException;

import ealvatag.tag.InvalidDataTypeException;
import ealvatag.tag.id3.AbstractTagFrameBody;
import ealvatag.tag.id3.ID3Tags;
import okio.Buffer;

/**
 * Represents a {@link ealvatag.tag.id3.framebody.FrameBodySYTC} tempo code.
 * <p>
 * The tempo is in BPM described with one or two bytes. If the
 * first byte has the value $FF, one more byte follows, which is added
 * to the first giving a range from 2 - 510 BPM, since $00 and $01 is
 * reserved. $00 is used to describe a beat-free time period, which is
 * not the same as a music-free time period. $01 is used to indicate one
 * single beat-stroke followed by a beat-free period.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 * @version $Id:$
 */
public class TempoCode extends AbstractDataType {
    private static final int MINIMUM_NO_OF_DIGITS = 1;
    private static final int MAXIMUM_NO_OF_DIGITS = 2;

    public TempoCode(TempoCode copy) {
        super(copy);
    }

    public TempoCode(String identifier, AbstractTagFrameBody frameBody) {
        super(identifier, frameBody, 0);
    }

    public TempoCode(String identifier, AbstractTagFrameBody frameBody, Object value) {
        super(identifier, frameBody, value);
    }


    @Override
    public int getSize() {
        if (value == null) {
            return 0;
        } else {
            return ID3Tags.getWholeNumber(value) < 0xFF ? MINIMUM_NO_OF_DIGITS : MAXIMUM_NO_OF_DIGITS;
        }
    }

    @Override
    public boolean equals(Object that) {
        return that instanceof TempoCode && super.equals(that);
    }

    @Override
    public void readByteArray(byte[] arr, int offset) throws InvalidDataTypeException {
        if (arr == null) {
            throw new NullPointerException("Byte array is null");
        }
        if (offset < 0) {
            throw new IllegalArgumentException("negative offset into an array offset:" + offset);
        }
        if (offset >= arr.length) {
            throw new InvalidDataTypeException("Offset to byte array is out of bounds: offset = " +
                    offset +
                    ", array.length = " +
                    arr.length);
        }

        long lvalue = 0;
        lvalue += (arr[offset] & 0xff);
        if (lvalue == 0xFF) {
            lvalue += (arr[offset + 1] & 0xff);
        }
        value = lvalue;
    }

    @Override
    public void read(Buffer buffer, int size) throws EOFException, InvalidDataTypeException {
        long lvalue = 0;
        lvalue += (buffer.readByte() & 0xff);
        if (lvalue == 0xFF) {
            lvalue += (buffer.readByte() & 0xff);
        }
        value = lvalue;
    }

    @Override
    public byte[] writeByteArray() {
        int size = getSize();
        byte[] arr = new byte[size];
        long temp = ID3Tags.getWholeNumber(value);
        int offset = 0;
        if (temp >= 0xFF) {
            arr[offset] = (byte) 0xFF;
            offset++;
            temp -= 0xFF;
        }
        arr[offset] = (byte) (temp & 0xFF);
        return arr;
    }

    @Override
    public String toString() {
        return value == null ? "" : value.toString();
    }

}
