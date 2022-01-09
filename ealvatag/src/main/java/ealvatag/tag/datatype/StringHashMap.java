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

import static ealvatag.utils.StandardCharsets.ISO_8859_1;

import com.google.common.base.Strings;

import java.nio.charset.Charset;

import ealvatag.tag.id3.AbstractTagFrameBody;
import ealvatag.tag.reference.Languages;
import ealvatag.tag.reference.SimpleStringStringMap;


/**
 * Represents a String that acts as a key into an enumeration of values. The String will be encoded
 * using the default encoding regardless of what encoding may be specified in the framebody
 */
public class StringHashMap extends StringFixedLength {

    private final SimpleStringStringMap simpleStringStringMap;
    private boolean hasEmptyValue;

    public StringHashMap(String identifier, AbstractTagFrameBody frameBody, int size) {
        super(identifier, frameBody, size);

        if (identifier.equals(DataTypes.OBJ_LANGUAGE)) {
            simpleStringStringMap = Languages.getInstanceOf();
        } else {
            throw new IllegalArgumentException("Hashmap identifier not defined in this class: " + identifier);
        }
    }

    @SuppressWarnings("unused")   // TODO: 1/18/17 Do we need this copy ctor?
    public StringHashMap(StringHashMap copyObject) {
        super(copyObject);
        hasEmptyValue = copyObject.hasEmptyValue;
        simpleStringStringMap = copyObject.simpleStringStringMap;
    }

    public void setValue(Object value) {
        if (value instanceof String) {
            //Issue #273 temporary hack for MM
            if (value.equals("XXX")) {
                this.value = value.toString();
            } else {
                this.value = ((String) value).toLowerCase();
            }
        } else {
            this.value = value;
        }
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof StringHashMap)) {
            return false;
        }

        StringHashMap that = (StringHashMap) obj;

        return hasEmptyValue == that.hasEmptyValue &&
                com.google.common.base.Objects.equal(simpleStringStringMap, that.simpleStringStringMap) &&
                super.equals(obj);
    }

    public String toString() {
        if (value != null) {
            return Strings.nullToEmpty(simpleStringStringMap.getValue(value.toString()));
        }
        return "";
    }

    /**
     * @return the ISO_8859 encoding for Datatypes of this type
     */
    protected Charset getTextEncodingCharSet() {
        return ISO_8859_1;
    }
}
