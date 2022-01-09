/*
 * @author : Paul Taylor
 * <p>
 * Version @version:$Id$
 * <p>
 * Jaudiotagger Copyright (C)2004,2005
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
 * Description: Valid Picture Types in ID3
 */
package ealvatag.tag.reference;

import com.google.common.base.Strings;

import ealvatag.tag.id3.valuepair.SimpleIntStringMap;

/**
 * Pictures types for Attached Pictures
 * <p>
 * <P>Note this list is used by APIC and PIC frames within ID3v2. It is also used by Flac format Picture blocks
 * and WMA Picture fields.
 */
public class PictureTypes implements SimpleIntStringMap {
    @SuppressWarnings("WeakerAccess")
    public static final int MAX_PICTURE_TYPE_ID = 20;
    public static final int PICTURE_TYPE_FIELD_SIZE = 1;
    @SuppressWarnings("unused")
    public static final String DEFAULT_VALUE = "Cover (front)";
    public static final Integer DEFAULT_ID = 3;

    private static volatile PictureTypes instance;
    private final String[] values;

    private PictureTypes() {
        values = new String[MAX_PICTURE_TYPE_ID + 1];
        values[0] = "Other";
        values[1] = "32x32 pixels 'file icon' (PNG only)";
        values[2] = "Other file icon";
        values[3] = "Cover (front)";
        values[4] = "Cover (back)";
        values[5] = "Leaflet page";
        values[6] = "Media (e.g. label side of CD)";
        values[7] = "Lead artist/lead performer/soloist";
        values[8] = "Artist/performer";
        values[9] = "Conductor";
        values[10] = "Band/Orchestra";
        values[11] = "Composer";
        values[12] = "Lyricist/text writer";
        values[13] = "Recording Location";
        values[14] = "During recording";
        values[15] = "During performance";
        values[16] = "Movie/video screen capture";
        values[17] = "A bright coloured fish";
        values[18] = "Illustration";
        values[19] = "Band/artist logotype";
        values[20] = "Publisher/Studio logotype";
    }

    public static PictureTypes getInstanceOf() {
        if (instance == null) {
            synchronized (PictureTypes.class) {
                if (instance == null) {
                    instance = new PictureTypes();
                }
            }
        }
        return instance;
    }

    @Override
    public boolean containsKey(int key) {
        return key >= 0 && key <= MAX_PICTURE_TYPE_ID;
    }

    @Override
    public String getValue(int key) {
        if (!containsKey(key)) {
            return "";
        }
        return Strings.nullToEmpty(values[key]);
    }

    public int getSize() {
        return values.length;
    }
}
