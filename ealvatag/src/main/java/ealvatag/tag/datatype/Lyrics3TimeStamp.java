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
import java.util.Arrays;

import ealvatag.tag.InvalidDataTypeException;
import ealvatag.tag.id3.AbstractTagFrameBody;
import ealvatag.utils.StandardCharsets;
import okio.Buffer;

public class Lyrics3TimeStamp extends AbstractDataType {
    /**
     *
     */
    private long minute;

    /**
     *
     */
    private long second;

    public Lyrics3TimeStamp(String identifier, AbstractTagFrameBody frameBody) {
        super(identifier, frameBody);
    }

    Lyrics3TimeStamp(String identifier) {
        super(identifier, null);
    }

    Lyrics3TimeStamp(Lyrics3TimeStamp copy) {
        super(copy);
        minute = copy.minute;
        second = copy.second;
    }

    // TODO: 3/14/17  implement?
    public void readString() {
    }

    @SuppressWarnings("unused")
    public long getMinute() {
        return minute;
    }

    @SuppressWarnings("unused")
    public void setMinute(long minute) {
        this.minute = minute;
    }

    public long getSecond() {
        return second;
    }

    public void setSecond(long second) {
        this.second = second;
    }

    public int getSize() {
        return 7;
    }

    @SuppressWarnings("unused")
    public void setTimeStamp(long timeStamp, byte timeStampFormat) {
        // TODO: 3/14/17  convert both types of formats
        timeStamp = timeStamp / 1000;
        minute = timeStamp / 60;
        second = timeStamp % 60;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof Lyrics3TimeStamp)) {
            return false;
        }

        Lyrics3TimeStamp object = (Lyrics3TimeStamp) obj;

        return minute == object.minute && second == object.second && super.equals(obj);

    }

    public void readString(String timeStamp, int offset) {
        if (timeStamp == null) {
            throw new NullPointerException("Image is null");
        }

        if ((offset < 0) || (offset >= timeStamp.length())) {
            throw new IndexOutOfBoundsException("Offset to timeStamp is out of bounds: offset = " +
                    offset +
                    ", timeStamp.length()" +
                    timeStamp.length());
        }

        timeStamp = timeStamp.substring(offset);

        if (timeStamp.length() == 7) {
            minute = Integer.parseInt(timeStamp.substring(1, 3));
            second = Integer.parseInt(timeStamp.substring(4, 6));
        } else {
            minute = 0;
            second = 0;
        }
    }

    public String toString() {
        return writeString();
    }

    String writeString() {
        String str;
        str = "[";

        if (minute < 0) {
            str += "00";
        } else {
            if (minute < 10) {
                str += '0';
            }

            str += Long.toString(minute);
        }

        str += ':';

        if (second < 0) {
            str += "00";
        } else {
            if (second < 10) {
                str += '0';
            }

            str += Long.toString(second);
        }

        str += ']';

        return str;
    }

    public void readByteArray(byte[] arr, int offset) throws InvalidDataTypeException {
        readString(Arrays.toString(arr), offset);
    }

    @Override
    public void read(Buffer buffer, int size) throws EOFException, InvalidDataTypeException {
        readString(Arrays.toString(buffer.readByteArray()), 0);
    }

    public byte[] writeByteArray() {
        return writeString().getBytes(StandardCharsets.ISO_8859_1);
    }

}
