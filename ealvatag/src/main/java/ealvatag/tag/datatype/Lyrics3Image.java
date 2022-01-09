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

import java.io.EOFException;
import java.util.Arrays;

import ealvatag.tag.InvalidDataTypeException;
import ealvatag.tag.id3.AbstractTagFrameBody;
import ealvatag.utils.StandardCharsets;
import okio.Buffer;

public class Lyrics3Image extends AbstractDataType {
    /**
     *
     */
    private Lyrics3TimeStamp time;

    /**
     *
     */
    private String description = "";

    /**
     *
     */
    private String filename = "";

    /**
     * Creates a new ObjectLyrics3Image datatype.
     *
     * @param identifier
     * @param frameBody
     */
    public Lyrics3Image(String identifier, AbstractTagFrameBody frameBody) {
        super(identifier, frameBody);
    }

    public Lyrics3Image(Lyrics3Image copy) {
        super(copy);
        time = new Lyrics3TimeStamp(copy.time);
        description = copy.description;
        filename = copy.filename;
    }

    /**
     * @return
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return
     */
    public String getFilename() {
        return filename;
    }

    /**
     * @param filename
     */
    public void setFilename(String filename) {
        this.filename = filename;
    }

    /**
     * @return
     */
    public int getSize() {
        int size;

        size = filename.length() + 2 + description.length() + 2;

        if (time != null) {
            size += time.getSize();
        }

        return size;
    }

    /**
     * @return
     */
    public Lyrics3TimeStamp getTimeStamp() {
        return time;
    }

    /**
     * @param time
     */
    public void setTimeStamp(Lyrics3TimeStamp time) {
        this.time = time;
    }

    /**
     * @param obj
     * @return
     */
    public boolean equals(Object obj) {
        if (!(obj instanceof Lyrics3Image)) {
            return false;
        }

        Lyrics3Image object = (Lyrics3Image) obj;

        if (!description.equals(object.description)) {
            return false;
        }

        if (!filename.equals(object.filename)) {
            return false;
        }

        if (time == null) {
            if (object.time != null) {
                return false;
            }
        } else {
            if (!time.equals(object.time)) {
                return false;
            }
        }

        return super.equals(obj);
    }

    /**
     * @param imageString
     * @param offset
     * @throws NullPointerException
     * @throws IndexOutOfBoundsException
     */
    public void readString(String imageString, int offset) {
        if (imageString == null) {
            throw new NullPointerException("Image string is null");
        }

        if ((offset < 0) || (offset >= imageString.length())) {
            throw new IndexOutOfBoundsException("Offset to image string is out of bounds: offset = " +
                    offset +
                    ", string.length()" +
                    imageString.length());
        }

        if (imageString != null) {
            String timestamp;
            int delim;

            delim = imageString.indexOf("||", offset);
            filename = imageString.substring(offset, delim);

            offset = delim + 2;
            delim = imageString.indexOf("||", offset);
            description = imageString.substring(offset, delim);

            offset = delim + 2;
            timestamp = imageString.substring(offset);

            if (timestamp.length() == 7) {
                time = new Lyrics3TimeStamp("Time Stamp");
                time.readString();
            }
        }
    }

    /**
     * @return
     */
    public String toString() {
        String str;
        str = "filename = " + filename + ", description = " + description;

        if (time != null) {
            str += (", timestamp = " + time);
        }

        return str + "\n";
    }

    /**
     * @return
     */
    public String writeString() {
        String str;

        if (filename == null) {
            str = "||";
        } else {
            str = filename + "||";
        }

        if (description == null) {
            str += "||";
        } else {
            str += (description + "||");
        }

        if (time != null) {
            str += time.writeString();
        }

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
