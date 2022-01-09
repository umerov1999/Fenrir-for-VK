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
 * You should have received a copy of the GNU Lesser General Public License ainteger with this library; if not, you can get a copy from
 * http://www.opensource.org/licenses/lgpl-license.php or write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
 * Boston, MA 02110-1301 USA
 * <p>
 * Description: Used by Commercial Frame (COMR)
 */
package ealvatag.tag.id3.valuepair;

import com.google.common.base.Strings;

/**
 * Defines how song was purchased used by the COMR frame
 */
public class ReceivedAsTypes implements SimpleIntStringMap {
    public static final int MAX_RECEIVED_AS_ID = 0x08;

    //The number of bytes used to hold the text encoding field size
    public static final int RECEIVED_AS_FIELD_SIZE = 1;

    private static volatile ReceivedAsTypes instance;
    private final String[] values;

    private ReceivedAsTypes() {
        values = new String[MAX_RECEIVED_AS_ID + 1];
        values[0x00] = "Other";
        values[0x01] = "Standard CD album with other songs";
        values[0x02] = "Compressed audio on CD";
        values[0x03] = "File over the Internet";
        values[0x04] = "Stream over the Internet";
        values[0x05] = "As note sheets";
        values[0x06] = "As note sheets in a book with other sheets";
        values[0x07] = "Music on other media";
        values[0x08] = "Non-musical merchandise";
    }

    public static ReceivedAsTypes getInstanceOf() {
        if (instance == null) {
            synchronized (ReceivedAsTypes.class) {
                if (instance == null) {
                    instance = new ReceivedAsTypes();
                }
            }
        }
        return instance;
    }

    @Override
    public boolean containsKey(int key) {
        return key >= 0 && key <= MAX_RECEIVED_AS_ID;
    }

    @Override
    public String getValue(int key) {
        if (!containsKey(key)) {
            return "";
        }
        return Strings.nullToEmpty(values[key]);
    }
}
