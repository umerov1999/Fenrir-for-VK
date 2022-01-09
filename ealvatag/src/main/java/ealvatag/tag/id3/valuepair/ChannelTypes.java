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
 * You should have received a copy of the GNU Lesser General Public License with this library; if not, you can get a copy from
 * http://www.opensource.org/licenses/lgpl-license.php or write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
 * Boston, MA 02110-1301 USA
 * <p>
 * Description: Channel type used by
 */
package ealvatag.tag.id3.valuepair;

import com.google.common.base.Strings;

public class ChannelTypes implements SimpleIntStringMap {
    @SuppressWarnings("WeakerAccess")
    public static final int MAX_CHANNEL_ID = 0x08;

    private static volatile ChannelTypes instance;

    private final String[] values;

    private ChannelTypes() {
        values = new String[MAX_CHANNEL_ID + 1];
        values[0x00] = "Other";
        values[0x01] = "Master volume";
        values[0x02] = "Front right";
        values[0x03] = "Front left";
        values[0x04] = "Back right";
        values[0x05] = "Back left";
        values[0x06] = "Front centre";
        values[0x07] = "Back centre";
        values[0x08] = "Subwoofer";
    }

    public static ChannelTypes getInstanceOf() {
        if (instance == null) {
            synchronized (ChannelTypes.class) {
                if (instance == null) {
                    instance = new ChannelTypes();
                }
            }
        }
        return instance;
    }

    @Override
    public boolean containsKey(int key) {
        return key >= 0 && key <= MAX_CHANNEL_ID;
    }

    @Override
    public String getValue(int key) {
        if (!containsKey(key)) {
            return "";
        }
        return Strings.nullToEmpty(values[key]);
    }
}
