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
 * FragmentBody contains the data for a fragment. ID3v2 tags have frames bodys. Lyrics3 tags have fields bodys ID3v1 tags do not have
 * fragments bodys. Fragment Bodies consist of a number of MP3Objects held in an objectList Methods are additionally defined here to
 * restrieve and set these objects. We also specify methods for getting/setting the text encoding of textual data. Fragment bodies should
 * not be concerned about their parent fragment. For example most ID3v2 frames can be applied to ID3v2tags of different versions. The frame
 * header will need modification based on the frame version but this should have no effect on the frame body.
 */
package ealvatag.tag.id3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import ealvatag.tag.datatype.AbstractDataType;
import ealvatag.tag.datatype.DataTypes;
import ealvatag.tag.id3.valuepair.TextEncoding;

/**
 * A frame body contains the data content for a frame
 */
public abstract class AbstractTagFrameBody extends AbstractTagItem {
    // set to default ArrayList.DEFAULT_CAPACITY, which happens to match our max.
    private final ArrayList<AbstractDataType> dataTypeList = new ArrayList<>(10);
    // 10 is currently our max size and one 1 has that many. 5 would be the max if not for that outlier. So knowing that current HashMaps
    // will take 5 to the next power of 2 (8), I'll choose 5. That one case of 10 will cause a map resize, but I'll save a lot of space as
    // average waste will be approximately 3 and not 11.
    private final HashMap<String, AbstractDataType> dataTypeMap = new HashMap<>(5);
    /**
     * Reference to the header associated with this frame body, a framebody can be created without a header
     * but one it is associated with a header this should be set. It is principally useful for the framebody to know
     * its header, because this will specify its tag version and some framebodies behave slighly different
     * between tag versions.
     */
    private AbstractTagFrame header;

    /**
     * Creates a new framebody, at this point the bodys
     * ObjectList is setup which defines what datatypes are expected in body
     */
    protected AbstractTagFrameBody() {
        setupObjectList();
    }

    /**
     * Copy Constructor for fragment body. Copies all objects in the
     * Object Iterator with data.
     */
    protected AbstractTagFrameBody(AbstractTagFrameBody copyObject) {
        ArrayList<AbstractDataType> copyObjectList = copyObject.dataTypeList;
        for (int i = 0, size = copyObjectList.size(); i < size; i++) {
            AbstractDataType newObject = (AbstractDataType) ID3Tags.copyObject(copyObjectList.get(i));
            newObject.setBody(this);
            addDataType(newObject);
        }
    }

    public void createStructure() {
    }

    /**
     * List of data types that make up this particular frame body.
     */
    protected List<AbstractDataType> getDataTypeList() {
        return dataTypeList;
    }

    protected void addDataType(AbstractDataType dataType) {
        dataTypeList.add(dataType);
        dataTypeMap.put(dataType.getIdentifier(), dataType);
    }

    private boolean containsDataType(AbstractDataType dataType) {
        return dataTypeMap.containsKey(dataType.getIdentifier());
    }

    /**
     * Return the Text Encoding
     *
     * @return the text encoding used by this framebody
     */
    public final byte getTextEncoding() {
        AbstractDataType o = getObject(DataTypes.OBJ_TEXT_ENCODING);

        if (o != null) {
            Long encoding = (Long) (o.getValue());
            return encoding.byteValue();
        } else {
            return TextEncoding.ISO_8859_1;
        }
    }

    /**
     * Set the Text Encoding to use for this frame body
     *
     * @param textEncoding to use for this frame body
     */
    public final void setTextEncoding(byte textEncoding) {
        //Number HashMap actually converts this byte to a long
        setObjectValue(DataTypes.OBJ_TEXT_ENCODING, textEncoding);
    }

    /**
     * @return the text value that the user would expect to see for this framebody type, this should be overridden for all frame-bodies
     */
    public String getUserFriendlyValue() {
        return toString();
    }

    /**
     * This method calls <code>toString</code> for all it's objects and appends
     * them without any newline characters.
     *
     * @return brief description string
     */
    public String getBriefDescription() {
        int size = dataTypeList.size();
        StringBuilder builder = new StringBuilder(256);  // during general testing this seems to be a big enough byte array
        for (int i = 0; i < size; i++) {
            AbstractDataType object = dataTypeList.get(i);
            if ((object.toString() != null) && (object.toString().length() > 0)) {
                builder.append(object.getIdentifier())
                        .append("=\"")
                        .append(object)
                        .append("\"; ");
            }
        }
        return builder.toString();
    }


    /**
     * This method calls <code>toString</code> for all it's objects and appends
     * them. It contains new line characters and is more suited for display
     * purposes
     *
     * @return formatted description string
     */
    @SuppressWarnings("unused")                     // TODO: 2/18/17 Is this method needed?
    public final String getLongDescription() {
        String str = "";
        for (AbstractDataType object : dataTypeList) {
            if ((object.toString() != null) && (object.toString().length() > 0)) {
                str += (object.getIdentifier() + " = " + object + "\n");
            }
        }
        return str;
    }

    /**
     * Sets all objects of identifier type to value defined by <code>obj</code> argument.
     *
     * @param identifier <code>MP3Object</code> identifier
     * @param value      new datatype value
     */
    public final void setObjectValue(String identifier, Object value) {
        AbstractDataType abstractDataType = dataTypeMap.get(identifier);
        if (abstractDataType != null) {
            abstractDataType.setValue(value);
        }
    }

    /**
     * Returns the value of the datatype with the specified
     * <code>identifier</code>
     *
     * @return the value of the dattype with the specified <code>identifier</code>
     */
    public final Object getObjectValue(String identifier) {
        return getObject(identifier).getValue();
    }

    /**
     * Returns the datatype with the specified
     * <code>identifier</code>
     *
     * @return the datatype with the specified <code>identifier</code>
     */
    public final AbstractDataType getObject(String identifier) {
        return dataTypeMap.get(identifier);
    }

    /**
     * Returns the size in bytes of this fragmentbody
     *
     * @return estimated size in bytes of this datatype
     */
    public int getSize() {
        int frameBodySize = 0;
        for (int i = 0, size = dataTypeList.size(); i < size; i++) {
            frameBodySize += dataTypeList.get(i).getSize();
        }
        return frameBodySize;
    }

    /**
     * Returns true if this instance and its entire DataType
     * array list is a subset of the argument. This class is a subset if it is
     * the same class as the argument.
     *
     * @param obj datatype to determine subset of
     * @return true if this instance and its entire datatype array list is a subset of the argument.
     */
    public boolean isSubsetOf(Object obj) {
        if (!(obj instanceof AbstractTagFrameBody)) {
            return false;
        }
        AbstractTagFrameBody possibleSuperset = (AbstractTagFrameBody) obj;
        for (int i = 0, size = dataTypeList.size(); i < size; i++) {
            if (!possibleSuperset.containsDataType(dataTypeList.get(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns true if this datatype and its entire DataType array
     * list equals the argument. This datatype is equal to the argument if they
     * are the same class.
     *
     * @param obj datatype to determine equality of
     * @return true if this datatype and its entire <code>MP3Object</code> array list equals the argument.
     */
    public boolean equals(Object obj) {
        if (!(obj instanceof AbstractTagFrameBody)) {
            return false;
        }
        AbstractTagFrameBody object = (AbstractTagFrameBody) obj;
        return dataTypeList.equals(object.dataTypeList);
    }

    /**
     * Returns an iterator of the DataType list.
     *
     * @return iterator of the DataType list.
     */
    public Iterator iterator() {
        return dataTypeList.iterator();
    }


    /**
     * Return brief description of FrameBody
     *
     * @return brief description of FrameBody
     */
    public String toString() {
        return getBriefDescription();
    }


    /**
     * Create the list of Datatypes that this body
     * expects in the correct order This method needs to be implemented by concrete subclasses
     */
    protected abstract void setupObjectList();

    /**
     * Get Reference to header
     */
    public AbstractTagFrame getHeader() {
        return header;
    }

    /**
     * Set header
     */
    public void setHeader(AbstractTagFrame header) {
        this.header = header;
    }

}
