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
 * Description:
 */
package ealvatag.tag.id3.valuepair;

import com.google.common.base.Strings;

import ealvatag.utils.InclusiveIntegerRange;

public class EventTimingTimestampTypes implements SimpleIntStringMap {
    public static final int TIMESTAMP_KEY_FIELD_SIZE = 1;
    public static final InclusiveIntegerRange EVENT_TIMING_ID_RANGE = new InclusiveIntegerRange(1, 2);

    private static volatile EventTimingTimestampTypes instance;
    private final String[] values;

    private EventTimingTimestampTypes() {
        values = new String[EVENT_TIMING_ID_RANGE.size()];
        values[1 - EVENT_TIMING_ID_RANGE.getLowerBounds()] = "Absolute time using MPEG [MPEG] frames as unit";
        values[2 - EVENT_TIMING_ID_RANGE.getLowerBounds()] = "Absolute time using milliseconds as unit";
    }

    public static EventTimingTimestampTypes getInstanceOf() {
        if (instance == null) {
            synchronized (EventTimingTimestampTypes.class) {
                if (instance == null) {
                    instance = new EventTimingTimestampTypes();
                }
            }
        }
        return instance;
    }

    @Override
    public boolean containsKey(int key) {
        return EVENT_TIMING_ID_RANGE.contains(key);
    }

    @Override
    public String getValue(int key) {
        if (!EVENT_TIMING_ID_RANGE.contains(key)) {
            return "";
        }
        return Strings.nullToEmpty(values[key - EVENT_TIMING_ID_RANGE.getLowerBounds()]);
    }
}
