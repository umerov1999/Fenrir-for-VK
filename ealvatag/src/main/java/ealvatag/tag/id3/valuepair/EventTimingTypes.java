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
 * Description:
 */
package ealvatag.tag.id3.valuepair;

import static com.google.common.base.Strings.nullToEmpty;

import ealvatag.utils.InclusiveIntegerRange;

public class EventTimingTypes implements SimpleIntStringMap {
    @SuppressWarnings("WeakerAccess")
    public static final InclusiveIntegerRange CORE_TYPES = new InclusiveIntegerRange(0x00, 0x16);
    @SuppressWarnings("WeakerAccess")
    public static final InclusiveIntegerRange NOT_PREDEFINED_SYNC_TYPES = new InclusiveIntegerRange(0xE0, 0xEF);
    @SuppressWarnings("WeakerAccess")
    public static final InclusiveIntegerRange AUDIO_END_TYPES = new InclusiveIntegerRange(0xFD, 0xFE);

    private static volatile EventTimingTypes instance;
    private final String[] coreValues;
    private final String[] syncValues;
    private final String[] audioEndValues;

    private EventTimingTypes() {
        // We only have 3 ranges now, so we'll keep it simple with if statements to find values. No reason to have maps or create Integer
        // instance, etc.
        coreValues = new String[CORE_TYPES.size()];
        coreValues[0x00] = "Padding (has no meaning)";
        coreValues[0x01] = "End of initial silence";
        coreValues[0x02] = "Intro start";
        coreValues[0x03] = "Main part start";
        coreValues[0x04] = "Outro start";
        coreValues[0x05] = "Outro end";
        coreValues[0x06] = "Verse start";
        coreValues[0x07] = "Refrain start";
        coreValues[0x08] = "Interlude start";
        coreValues[0x09] = "Theme start";
        coreValues[0x0A] = "Variation start";
        coreValues[0x0B] = "Key change";
        coreValues[0x0C] = "Time change";
        coreValues[0x0D] = "Momentary unwanted noise (Snap, Crackle & Pop)";
        coreValues[0x0E] = "Sustained noise";
        coreValues[0x0F] = "Sustained noise end";
        coreValues[0x10] = "Intro end";
        coreValues[0x11] = "Main part end";
        coreValues[0x12] = "Verse end";
        coreValues[0x13] = "Refrain end";
        coreValues[0x14] = "Theme end";
        coreValues[0x15] = "Profanity";
        coreValues[0x16] = "Profanity end";

        // 0x17-0xDF  reserved for future use

        syncValues = new String[NOT_PREDEFINED_SYNC_TYPES.size()];
        syncValues[0] = "Not predefined synch 0";
        syncValues[1] = "Not predefined synch 1";
        syncValues[2] = "Not predefined synch 2";
        syncValues[3] = "Not predefined synch 3";
        syncValues[4] = "Not predefined synch 4";
        syncValues[5] = "Not predefined synch 5";
        syncValues[6] = "Not predefined synch 6";
        syncValues[7] = "Not predefined synch 7";
        syncValues[8] = "Not predefined synch 8";
        syncValues[9] = "Not predefined synch 9";
        syncValues[10] = "Not predefined synch A";
        syncValues[11] = "Not predefined synch B";
        syncValues[12] = "Not predefined synch C";
        syncValues[13] = "Not predefined synch D";
        syncValues[14] = "Not predefined synch E";
        syncValues[15] = "Not predefined synch F";

        // 0xF0-0xFC  reserved for future use
        audioEndValues = new String[AUDIO_END_TYPES.size()];
        audioEndValues[0] = "Audio end (start of silence)";
        audioEndValues[1] = "Audio file ends";
    }

    public static EventTimingTypes getInstanceOf() {
        if (instance == null) {
            synchronized (EventTimingTypes.class) {
                if (instance == null) {
                    instance = new EventTimingTypes();
                }
            }
        }
        return instance;
    }

    @Override
    public boolean containsKey(int key) {
        return CORE_TYPES.contains(key) || NOT_PREDEFINED_SYNC_TYPES.contains(key) || AUDIO_END_TYPES.contains(key);
    }

    @Override
    public String getValue(int key) {
        if (CORE_TYPES.contains(key)) {
            return nullToEmpty(coreValues[key - CORE_TYPES.getLowerBounds()]);
        }

        if (NOT_PREDEFINED_SYNC_TYPES.contains(key)) {
            return nullToEmpty(syncValues[key - NOT_PREDEFINED_SYNC_TYPES.getLowerBounds()]);
        }

        if (AUDIO_END_TYPES.contains(key)) {
            return nullToEmpty(audioEndValues[key - AUDIO_END_TYPES.getLowerBounds()]);
        }

        return "";
    }
}
