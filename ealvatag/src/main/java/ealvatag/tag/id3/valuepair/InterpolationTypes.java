/*
 * @author : Paul Taylor
 *
 * Version @version:$Id$
 *
 * Jaudiotagger Copyright (C)2004,2005
 *
 * This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public  License as published by the Free Software Foundation; either version 2.1 of the License,
 * or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this library; if not,
 * you can get a copy from http://www.opensource.org/licenses/lgpl-license.php or write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 *
 * Description:
 */
package ealvatag.tag.id3.valuepair;

import com.google.common.base.Strings;

public class InterpolationTypes implements SimpleIntStringMap {
    @SuppressWarnings("WeakerAccess")
    public static final int MAX_INTERPOLATION_ID = 1;

    private static volatile InterpolationTypes instance;
    private final String[] values;

    private InterpolationTypes() {
        values = new String[MAX_INTERPOLATION_ID + 1];
        values[0] = "Band";
        values[1] = "Linear";
    }

    public static InterpolationTypes getInstanceOf() {
        if (instance == null) {
            synchronized (InterpolationTypes.class) {
                if (instance == null) {
                    instance = new InterpolationTypes();
                }
            }
        }
        return instance;
    }

    @Override
    public boolean containsKey(int key) {
        return key >= 0 && key <= MAX_INTERPOLATION_ID;
    }

    @Override
    public String getValue(int key) {
        if (!containsKey(key)) {
            return "";
        }
        return Strings.nullToEmpty(values[key]);
    }
}
