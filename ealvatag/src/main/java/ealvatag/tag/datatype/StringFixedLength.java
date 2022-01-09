/**
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

import static ealvatag.logging.EalvaTagLog.LogLevel.DEBUG;
import static ealvatag.logging.EalvaTagLog.LogLevel.ERROR;
import static ealvatag.logging.EalvaTagLog.LogLevel.TRACE;
import static ealvatag.logging.EalvaTagLog.LogLevel.WARN;

import java.io.EOFException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;

import ealvatag.tag.InvalidDataTypeException;
import ealvatag.tag.exceptions.IllegalCharsetException;
import ealvatag.tag.id3.AbstractTagFrameBody;
import ealvatag.utils.StandardCharsets;
import okio.Buffer;


/**
 * Represents a fixed length String, whereby the length of the String is known. The String
 * will be encoded based upon the text encoding of the frame that it belongs to.
 */
public class StringFixedLength extends AbstractString {
    /**
     * Creates a new ObjectStringFixedsize datatype.
     *
     * @param identifier
     * @param frameBody
     * @param size
     * @throws IllegalArgumentException
     */
    public StringFixedLength(String identifier, AbstractTagFrameBody frameBody, int size) {
        super(identifier, frameBody);
        if (size < 0) {
            throw new IllegalArgumentException("size is less than zero: " + size);
        }
        setSize(size);
    }

    public StringFixedLength(StringFixedLength copyObject) {
        super(copyObject);
        size = copyObject.size;
    }

    /**
     * @param obj
     * @return if obj is equivalent to this
     */
    public boolean equals(Object obj) {
        if (!(obj instanceof StringFixedLength)) {
            return false;
        }
        StringFixedLength object = (StringFixedLength) obj;
        return size == object.size && super.equals(obj);
    }

    /**
     * Read a string from buffer of fixed size(size has already been set in constructor)
     *
     * @param arr    this is the buffer for the frame
     * @param offset this is where to start reading in the buffer for this field
     */
    public void readByteArray(byte[] arr, int offset) throws InvalidDataTypeException {
        LOG.log(DEBUG, "Reading from array from offset:%s", offset);
        try {
            CharsetDecoder decoder = getTextEncodingCharSet().newDecoder();

            //Decode buffer if runs into problems should through exception which we
            //catch and then set value to empty string.
            LOG.log(TRACE, "Array length is:%s offset is:%s size is:%s", arr.length, offset, size);


            if (arr.length - offset < size) {
                throw new InvalidDataTypeException(
                        "byte array is to small to retrieve string of declared length:" + size);
            }
            value = decoder.decode(ByteBuffer.wrap(arr, offset, size)).toString();
        } catch (CharacterCodingException ce) {
            LOG.log(ERROR, "Character encoding, value:%s", value, ce);
            value = "";
        }
        LOG.log(DEBUG, "Read StringFixedLength:%s", value);
    }

    @Override
    public void read(Buffer buffer, int size) throws EOFException, InvalidDataTypeException {
        try {
            value = buffer.readString(this.size, getTextEncodingCharSet());
        } catch (IllegalCharsetException e) {
            throw new InvalidDataTypeException(e, "Bad charset Id");
        }
    }

    /**
     * Write String into byte array
     * <p>
     * The string will be adjusted to ensure the correct number of bytes are written, If the current value is null
     * or to short the written value will have the 'space' character appended to ensure this. We write this instead of
     * the null character because the null character is likely to confuse the parser into misreading the next field.
     *
     * @return the byte array to be written to the file
     */
    public byte[] writeByteArray() {
        ByteBuffer dataBuffer;
        byte[] data;

        //Create with a series of empty of spaces to try and ensure integrity of field
        if (value == null) {
            LOG.log(WARN, "Value of StringFixedlength Field is null using default value instead");
            data = new byte[size];
            for (int i = 0; i < size; i++) {
                data[i] = ' ';
            }
            return data;
        }

        try {
            Charset charset = getTextEncodingCharSet();
            CharsetEncoder encoder;
            if (StandardCharsets.UTF_16.equals(charset)) {
                //Note remember LE BOM is ff fe but tis is handled by encoder Unicode char is fe ff
                encoder = StandardCharsets.UTF_16LE.newEncoder();
                dataBuffer = encoder.encode(CharBuffer.wrap('\ufeff' + (String) value));
            } else {
                encoder = charset.newEncoder();
                dataBuffer = encoder.encode(CharBuffer.wrap((String) value));
            }
        } catch (CharacterCodingException ce) {
            LOG.log(WARN, "There was a problem writing the following StringFixedlength Field:%s using default value instead", value, ce);
            data = new byte[size];
            for (int i = 0; i < size; i++) {
                data[i] = ' ';
            }
            return data;
        }

        // We must return the defined size.
        // To check now because size is in bytes not chars
        if (dataBuffer != null) {
            //Everything ok
            if (dataBuffer.limit() == size) {
                data = new byte[dataBuffer.limit()];
                dataBuffer.get(data, 0, dataBuffer.limit());
                return data;
            }
            //There is more data available than allowed for this field strip
            else if (dataBuffer.limit() > size) {
                LOG.log(WARN, "There was a problem writing the following StringFixedlength Field:%s when converted to bytes has length of:%s" +
                                " but field was defined with length of:%s too long so stripping extra length",
                        value,
                        dataBuffer.limit(),
                        size);
                data = new byte[size];
                dataBuffer.get(data, 0, size);
                return data;
            }
            //There is not enough data
            else {
                LOG.log(WARN, "There was a problem writing the following StringFixedlength Field:%s when converted to bytes has length of:%s" +
                                " but field was defined with length of:%s too short so padding with spaces to make up extra length",
                        value,
                        dataBuffer.limit(),
                        size);

                data = new byte[size];
                dataBuffer.get(data, 0, dataBuffer.limit());

                for (int i = dataBuffer.limit(); i < size; i++) {
                    data[i] = ' ';
                }
                return data;
            }
        } else {
            LOG.log(WARN, "There was a serious problem writing the following StringFixedlength Field:%s using default value instead", value);
            data = new byte[size];
            for (int i = 0; i < size; i++) {
                data[i] = ' ';
            }
            return data;
        }
    }

}
