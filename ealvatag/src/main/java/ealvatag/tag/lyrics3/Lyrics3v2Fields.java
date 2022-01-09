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
package ealvatag.tag.lyrics3;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import ealvatag.tag.reference.SimpleStringStringMap;

public class Lyrics3v2Fields implements SimpleStringStringMap {
    static final String FIELD_V2_INDICATIONS = "IND";
    static final String FIELD_V2_LYRICS_MULTI_LINE_TEXT = "LYR";
    static final String FIELD_V2_ADDITIONAL_MULTI_LINE_TEXT = "INF";
    static final String FIELD_V2_AUTHOR = "AUT";
    static final String FIELD_V2_ALBUM = "EAL";
    static final String FIELD_V2_ARTIST = "EAR";
    static final String FIELD_V2_TRACK = "ETT";
    static final String FIELD_V2_IMAGE = "IMG";
    /**
     * CRLF int set
     */
    private static final byte[] crlfByte = {13, 10};
    /**
     * CRLF int set
     */
    static final String CRLF = new String(crlfByte);
    private static volatile Lyrics3v2Fields instance;
    private final ImmutableMap<String, String> idToValue;


    private Lyrics3v2Fields() {
        ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();
        builder.put(FIELD_V2_INDICATIONS, "Indications field")
                .put(FIELD_V2_LYRICS_MULTI_LINE_TEXT, "Lyrics multi line text")
                .put(FIELD_V2_ADDITIONAL_MULTI_LINE_TEXT, "Additional information multi line text")
                .put(FIELD_V2_AUTHOR, "Lyrics/Music Author name")
                .put(FIELD_V2_ALBUM, "Extended Album name")
                .put(FIELD_V2_ARTIST, "Extended Artist name")
                .put(FIELD_V2_TRACK, "Extended Track Title")
                .put(FIELD_V2_IMAGE, "Link to an image files");

        idToValue = builder.build();
    }

    public static Lyrics3v2Fields getInstanceOf() {
        if (instance == null) {
            synchronized (Lyrics3v2Field.class) {
                if (instance == null) {
                    instance = new Lyrics3v2Fields();
                }
            }
        }
        return instance;
    }

    /**
     * Returns true if the identifier is a valid Lyrics3v2 frame identifier
     *
     * @param identifier string to test
     * @return true if the identifier is a valid Lyrics3v2 frame identifier
     */
    static boolean isLyrics3v2FieldIdentifier(String identifier) {
        return identifier.length() >= 3 && getInstanceOf().containsKey(identifier.substring(0, 3));
    }

    public boolean containsKey(String key) {
        return idToValue.containsKey(key);
    }

    public String getValue(String id) {
        return idToValue.get(id);
    }

    public ImmutableSet<String> getAllKeys() {
        return idToValue.keySet();
    }

}
