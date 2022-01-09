/*
 * Copyright (c) 2017 Eric A. Snell
 *
 * This file is part of eAlvaTag.
 *
 * eAlvaTag is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * eAlvaTag is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with eAlvaTag.  If not,
 * see <http://www.gnu.org/licenses/>.
 */

package ealvatag.tag.id3.valuepair;

/**
 * Simple read-only map type interface
 * <p>
 * Created by Eric A. Snell on 1/18/17.
 */
public interface SimpleIntStringMap {

    /**
     * Query if the map contains the given integer key
     *
     * @param key key to query
     * @return true if key is contained in the map, else false
     */
    boolean containsKey(int key);

    /**
     * Returns the value associated with the key
     *
     * @param key key to query
     * @return the value or empty string if key not found
     */
    String getValue(int key);
}
