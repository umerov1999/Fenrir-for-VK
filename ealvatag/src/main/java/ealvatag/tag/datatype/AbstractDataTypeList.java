/*
 *  This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser
 *  General Public  License as published by the Free Software Foundation; either version 2.1 of the License,
 *  or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 *  the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License along with this library; if not,
 *  you can get a copy from http://www.opensource.org/licenses/lgpl-license.php or write to the Free Software
 *  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 *
 * Description:
 *
 */
package ealvatag.tag.datatype;

import static ealvatag.logging.EalvaTagLog.LogLevel.DEBUG;

import java.io.EOFException;
import java.util.ArrayList;
import java.util.List;

import ealvatag.tag.InvalidDataTypeException;
import ealvatag.tag.id3.AbstractTagFrameBody;
import okio.Buffer;

/**
 * Represents a list of {@link Cloneable}(!!) {@link AbstractDataType}s, continuing until the end of the buffer.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 * @version $Id:$
 */
public abstract class AbstractDataTypeList<T extends AbstractDataType> extends AbstractDataType {

    public AbstractDataTypeList(String identifier, AbstractTagFrameBody frameBody) {
        super(identifier, frameBody);
        setValue(new ArrayList<T>());
    }

    /**
     * Copy constructor.
     * By convention, subclasses <em>must</em> implement a constructor, accepting an argument of their own class type
     * and call this constructor for {@link ealvatag.tag.id3.ID3Tags#copyObject(Object)} to work.
     * A parametrized {@code AbstractDataTypeList} is not sufficient.
     *
     * @param copy instance
     */
    protected AbstractDataTypeList(AbstractDataTypeList<T> copy) {
        super(copy);
    }

    public List<T> getValue() {
        return (List<T>) super.getValue();
    }

    public void setValue(List<T> list) {
        super.setValue(list == null ? new ArrayList<T>() : new ArrayList<T>(list));
    }

    /**
     * Return the size in byte of this datatype list.
     *
     * @return the size in bytes
     */
    public int getSize() {
        int size = 0;
        for (T t : getValue()) {
            size += t.getSize();
        }
        return size;
    }

    /**
     * Reads list of {@link EventTimingCode}s from buffer starting at the given offset.
     *
     * @param buffer buffer
     * @param offset initial offset into the buffer
     */
    public void readByteArray(byte[] buffer, int offset) throws InvalidDataTypeException {
        if (buffer == null) {
            throw new NullPointerException("Byte array is null");
        }

        if (offset < 0) {
            throw new IndexOutOfBoundsException(
                    "Offset to byte array is out of bounds: offset = " + offset + ", array.length = " + buffer.length);
        }

        // no events
        if (offset >= buffer.length) {
            getValue().clear();
            return;
        }
        for (int currentOffset = offset; currentOffset < buffer.length; ) {
            T data = createListElement();
            data.readByteArray(buffer, currentOffset);
            data.setBody(frameBody);
            getValue().add(data);
            currentOffset += data.getSize();
        }
    }

    @Override
    public void read(Buffer buffer, int size) throws EOFException, InvalidDataTypeException {
        int bufferSize = (int) buffer.size();
        if (bufferSize == 0) {
            getValue().clear();
            return;
        }
        for (int i = 0, readSize = Math.min(size, bufferSize); i < readSize; i++) {
            T data = createListElement();
            data.read(buffer, size);
            data.setBody(frameBody);
            getValue().add(data);
            readSize -= data.getSize();
        }
    }

    /**
     * Factory method that creates new elements for this list.
     * Called from {@link #readByteArray(byte[], int)}.
     *
     * @return new list element
     */
    protected abstract T createListElement();

    /**
     * Write contents to a byte array.
     *
     * @return a byte array that that contains the data that should be persisted to file
     */
    public byte[] writeByteArray() {
        LOG.log(DEBUG, "Writing DataTypeList %s", getIdentifier());
        byte[] buffer = new byte[getSize()];
        int offset = 0;
        for (AbstractDataType data : getValue()) {
            byte[] bytes = data.writeByteArray();
            System.arraycopy(bytes, 0, buffer, offset, bytes.length);
            offset += bytes.length;
        }

        return buffer;
    }

    @Override
    public int hashCode() {
        return getValue() != null ? getValue().hashCode() : 0;
    }

    @Override
    public String toString() {
        return getValue() != null ? getValue().toString() : "%s";

    }
}
