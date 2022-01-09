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
 * Description: Represents a Number of a fixed number of decimal places.
 */
package ealvatag.tag.datatype;

import static ealvatag.logging.EalvaTagLog.LogLevel.DEBUG;

import com.google.common.base.Preconditions;

import java.io.EOFException;

import ealvatag.tag.InvalidDataTypeException;
import ealvatag.tag.id3.AbstractTagFrameBody;
import ealvatag.tag.id3.ID3Tags;
import okio.Buffer;


/**
 * Represents a number held as a fixed number of digits.
 * <p>
 * The bitorder in ID3v2 is most significant bit first (MSB). The byteorder in multibyte numbers is most significant
 * byte first (e.g. $12345678 would be encoded $12 34 56 78), also known as big endian and network byte order.
 * <p>
 * In ID3Specification would be denoted as $xx xx this denotes exactly two bytes required
 */
public class NumberFixedLength extends AbstractDataType {
    /**
     * Creates a new ObjectNumberFixedLength datatype.
     *
     * @param identifier to allow retrieval of this datatype by name from framebody
     * @param frameBody  that the dataype is associated with
     * @param size       the number of significant places that the number is held to
     * @throws IllegalArgumentException if size < 0
     */
    public NumberFixedLength(String identifier, AbstractTagFrameBody frameBody, int size) {
        super(identifier, frameBody);
        Preconditions.checkArgument(size >= 0);
        this.size = size;

    }

    public NumberFixedLength(NumberFixedLength copy) {
        super(copy);
        size = copy.size;
    }

    /**
     * Return size
     *
     * @return the size of this number
     */
    public int getSize() {
        return size;
    }

    /**
     * Set Size in Bytes of this Object
     *
     * @param size in bytes that this number will be held as
     */
    public void setSize(int size) {
        if (size > 0) {
            this.size = size;
        }
    }

    public void setValue(Object value) {
        if (!(value instanceof Number)) {
            throw new IllegalArgumentException("Invalid value type for NumberFixedLength:" + value.getClass());
        }
        super.setValue(value);
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof NumberFixedLength)) {
            return false;
        }
        NumberFixedLength object = (NumberFixedLength) obj;
        return size == object.size && super.equals(obj);
    }

    public void readByteArray(byte[] array, int offset) throws InvalidDataTypeException {
        if (array == null) {
            throw new NullPointerException("Byte array is null");
        }
        if ((offset < 0) || (offset >= array.length)) {
            throw new InvalidDataTypeException("Offset to byte array is out of bounds: offset = " +
                    offset +
                    ", array.length = " +
                    array.length);
        }

        if (offset + size > array.length) {
            throw new InvalidDataTypeException("Offset plus size to byte array is out of bounds: offset = "
                    + offset + ", size = " + size + " + array.length " + array.length);
        }

        long lvalue = 0;
        for (int i = offset; i < (offset + size); i++) {
            lvalue <<= 8;
            lvalue += (array[i] & 0xff);
        }
        value = lvalue;
        LOG.log(DEBUG, "Read NumberFixedlength:" + value);
    }

    @Override
    public void read(Buffer buffer, int size) throws EOFException, InvalidDataTypeException {
        long lvalue = 0;
        for (int i = 0; i < this.size; i++) {
            lvalue <<= 8;
            lvalue += (buffer.readByte() & 0xff);
        }
        value = lvalue;
    }

    /**
     * @return String representation of this datatype
     */
    public String toString() {
        if (value == null) {
            return "";
        } else {
            return value.toString();
        }
    }

    /**
     * Write data to byte array
     *
     * @return the datatype converted to a byte array
     */
    public byte[] writeByteArray() {
        byte[] arr;
        arr = new byte[size];
        if (value != null) {
            //Convert value to long
            long temp = ID3Tags.getWholeNumber(value);

            for (int i = size - 1; i >= 0; i--) {
                arr[i] = (byte) (temp & 0xFF);
                temp >>= 8;
            }
        }
        return arr;
    }
}
