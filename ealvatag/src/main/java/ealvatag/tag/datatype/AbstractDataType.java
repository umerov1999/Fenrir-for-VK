/*
 * @author : Paul Taylor
 * @author : Eric Farng
 * <p>
 * Version @version:$Id$
 * <p>
 * MusicTag Copyright (C)2003,2004
 * <p>
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

import org.jetbrains.annotations.Nullable;

import java.io.EOFException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

import ealvatag.audio.mp3.MP3File;
import ealvatag.logging.EalvaTagLog;
import ealvatag.logging.EalvaTagLog.JLogger;
import ealvatag.logging.EalvaTagLog.JLoggers;
import ealvatag.tag.InvalidDataTypeException;
import ealvatag.tag.id3.AbstractTagFrameBody;
import okio.Buffer;

/**
 * Represents a field/data type that can be held within a frames body, these map loosely onto
 * Section 4. ID3v2 frame overview at http://www.id3.org/id3v2.4.0-structure.txt
 */
public abstract class AbstractDataType {
    protected static JLogger LOG = JLoggers.get(AbstractDataType.class, EalvaTagLog.MARKER);  // TODO: 3/14/17 make this private!!

    /**
     * Holds the data
     */
    protected @Nullable
    Object value;

    /**
     * Holds the key such as "Text" or "PictureType", the naming of keys are fairly arbitary but are intended
     * to make it easier to for the developer, the keys themseleves are not written to the tag.
     */
    protected String identifier = "";

    /**
     * Holds the calling body, allows an datatype to query other objects in the
     * body such as the Text Encoding of the frame
     */
    protected AbstractTagFrameBody frameBody;

    /**
     * Holds the size of the data in file when read/written
     */
    protected int size;

    /**
     * Construct an abstract datatype identified by identifier and linked to a framebody without setting
     * an initial value.
     *
     * @param identifier to allow retrieval of this datatype by name from framebody
     * @param frameBody  that the dataype is associated with
     */
    protected AbstractDataType(String identifier, AbstractTagFrameBody frameBody) {
        this.identifier = identifier;
        this.frameBody = frameBody;
    }

    /**
     * Construct an abstract datatype identified by identifier and linked to a framebody initilised with a value
     *
     * @param identifier to allow retrieval of this datatype by name from framebody
     * @param frameBody  that the dataype is associated with
     * @param value      of this DataType
     */
    protected AbstractDataType(String identifier, AbstractTagFrameBody frameBody, Object value) {
        this.identifier = identifier;
        this.frameBody = frameBody;
        setValue(value);
    }

    /**
     * This is used by subclasses, to clone the data within the copyObject
     * <p>
     * TODO:It seems to be missing some of the more complex value types.
     *
     * @param copyObject clone type assistance
     */
    public AbstractDataType(AbstractDataType copyObject) {
        // no copy constructor in super class
        identifier = copyObject.identifier;
        if (copyObject.value == null) {
            value = null;
        } else if (copyObject.value instanceof String) {
            value = copyObject.value;
        } else if (copyObject.value instanceof Boolean) {
            value = copyObject.value;
        } else if (copyObject.value instanceof Byte) {
            value = copyObject.value;
        } else if (copyObject.value instanceof Character) {
            value = copyObject.value;
        } else if (copyObject.value instanceof Double) {
            value = copyObject.value;
        } else if (copyObject.value instanceof Float) {
            value = copyObject.value;
        } else if (copyObject.value instanceof Integer) {
            value = copyObject.value;
        } else if (copyObject.value instanceof Long) {
            value = copyObject.value;
        } else if (copyObject.value instanceof Short) {
            value = copyObject.value;
        } else if (copyObject.value instanceof MultipleTextEncodedStringNullTerminated.Values) {
            value = copyObject.value;
        } else if (copyObject.value instanceof PairedTextEncodedStringNullTerminated.ValuePairs) {
            value = copyObject.value;
        } else if (copyObject.value instanceof PartOfSet.PartOfSetValue) {
            value = copyObject.value;
        } else if (copyObject.value instanceof boolean[]) {
            value = ((boolean[]) copyObject.value).clone();
        } else if (copyObject.value instanceof byte[]) {
            value = ((byte[]) copyObject.value).clone();
        } else if (copyObject.value instanceof char[]) {
            value = ((char[]) copyObject.value).clone();
        } else if (copyObject.value instanceof double[]) {
            value = ((double[]) copyObject.value).clone();
        } else if (copyObject.value instanceof float[]) {
            value = ((float[]) copyObject.value).clone();
        } else if (copyObject.value instanceof int[]) {
            value = ((int[]) copyObject.value).clone();
        } else if (copyObject.value instanceof long[]) {
            value = ((long[]) copyObject.value).clone();
        } else if (copyObject.value instanceof short[]) {
            value = ((short[]) copyObject.value).clone();
        } else if (copyObject.value instanceof Object[]) {
            value = ((Object[]) copyObject.value).clone();
        } else if (copyObject.value instanceof ArrayList) {
            value = ((ArrayList) copyObject.value).clone();
        } else if (copyObject.value instanceof LinkedList) {
            value = ((LinkedList) copyObject.value).clone();
        } else {
            throw new UnsupportedOperationException("Unable to create copy of class " + copyObject.getClass());
        }
    }

    /**
     * Get the framebody associated with this datatype
     *
     * @return the framebody that this datatype is associated with
     */
    public AbstractTagFrameBody getBody() {
        return frameBody;
    }

    /**
     * Set the framebody that this datatype is associated with
     */
    public void setBody(AbstractTagFrameBody frameBody) {
        this.frameBody = frameBody;
    }

    /**
     * Return the key as declared by the frame bodies datatype list
     *
     * @return the key used to reference this datatype from a framebody
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Get value held by this Object
     *
     * @return value held by this Object
     */
    public Object getValue() {
        return value;
    }

    /**
     * Set the value held by this datatype, this is used typically used when the
     * user wants to modify the value in an existing frame.
     */
    public void setValue(Object value) {
        this.value = value;
    }

    /**
     * Simplified wrapper for reading bytes from file into Object.
     * Used for reading Strings, this class should be overridden
     * for non String Objects
     *
     * @param array byte array to read from
     * @throws InvalidDataTypeException of the data cannot be parsed
     * @see #read(Buffer, int)
     * @deprecated Just don't. Please. Seriously, stop. Read from {@link Buffer}
     */
    final public void readByteArray(byte[] array) throws InvalidDataTypeException {
        readByteArray(array, 0);
    }

    /**
     * This defines the size in bytes of the datatype being
     * held when read/written to file.
     *
     * @return the size in bytes of the datatype
     */
    abstract public int getSize();

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof AbstractDataType)) {
            return false;
        }
        AbstractDataType object = (AbstractDataType) obj;
        if (!identifier.equals(object.identifier)) {
            return false;
        }
        if ((value == null) && (object.value == null)) {
            return true;
        } else if ((value == null) || (object.value == null)) {
            return false;
        }
        // boolean[]
        if (value instanceof boolean[] && object.value instanceof boolean[]) {
            return Arrays.equals((boolean[]) value, (boolean[]) object.value);
            // byte[]
        } else if (value instanceof byte[] && object.value instanceof byte[]) {
            return Arrays.equals((byte[]) value, (byte[]) object.value);
            // char[]
        } else if (value instanceof char[] && object.value instanceof char[]) {
            return Arrays.equals((char[]) value, (char[]) object.value);
            // double[]
        } else if (value instanceof double[] && object.value instanceof double[]) {
            return Arrays.equals((double[]) value, (double[]) object.value);
            // float[]
        } else if (value instanceof float[] && object.value instanceof float[]) {
            return Arrays.equals((float[]) value, (float[]) object.value);
            // int[]
        } else if (value instanceof int[] && object.value instanceof int[]) {
            return Arrays.equals((int[]) value, (int[]) object.value);
            // long[]
        } else if (value instanceof long[] && object.value instanceof long[]) {
            return Arrays.equals((long[]) value, (long[]) object.value);
            // Object[]
        } else if (value instanceof Object[] && object.value instanceof Object[]) {
            return Arrays.equals((Object[]) value, (Object[]) object.value);
            // short[]
        } else if (value instanceof short[] && object.value instanceof short[]) {
            return Arrays.equals((short[]) value, (short[]) object.value);
        } else return value.equals(object.value);
    }

    /**
     * This is the starting point for reading bytes from the file into the ID3 datatype
     * starting at offset.
     *
     * @param array  array to read from
     * @param offset offset to start from
     * @throws InvalidDataTypeException if can't be parsed
     * @throws IllegalArgumentException if array is null or offset is not inside the bounds of the array
     * @see #read(Buffer, int)
     * @deprecated New philosophy, stop creating so many temporary byte[] AND let's use {@link Buffer} and cut the GC a break
     */
    public abstract void readByteArray(byte[] array, int offset) throws InvalidDataTypeException, IllegalArgumentException;

    /**
     * Read the data for this type from the {@link Buffer}
     *
     * @param buffer read here
     * @param size   the remaining size to read for this tag. Do not read past this size (I'm talking to you null terminated strings)
     * @throws EOFException             if data is unavailable in the buffer
     * @throws InvalidDataTypeException if error parsing this data type
     */
    public abstract void read(Buffer buffer, int size) throws EOFException, InvalidDataTypeException;

    /**
     * Starting point write ID3 Datatype back to array of bytes.
     * This class must be overridden.
     *
     * @return the array of bytes representing this datatype that should be written to file
     */
    public abstract byte[] writeByteArray();

    /**
     * Return String Representation of Datatype     *
     */
    public void createStructure() {
        MP3File.getStructureFormatter().addElement(identifier, getValue().toString());
    }

}
