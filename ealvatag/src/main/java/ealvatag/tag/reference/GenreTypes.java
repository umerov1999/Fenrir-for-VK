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
package ealvatag.tag.reference;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSortedSet;

import java.util.TreeMap;

import ealvatag.tag.id3.valuepair.SimpleIntStringMap;

/**
 * Genre list
 * <p>
 * <p>This is the IDv1 list with additional values as defined by Winamp, this list is also used in Mp4
 * files, note iTunes doesn't understand genres above MAX_STANDARD_GENRE_ID, Winamp does.
 */
public class GenreTypes implements SimpleIntStringMap {
    @SuppressWarnings("WeakerAccess")
    public static final int MAX_STANDARD_GENRE_ID = 125;
    @SuppressWarnings("WeakerAccess")
    public static final int MAX_GENRE_ID = 191;
    private static volatile GenreTypes instance;
    private final String[] values;  // ids are contiguous, so we'll just keep them in an array
    private TreeMap<String, Integer> valueToId; // one TreeMap with case insensitive ordering so we don't need another map

    private GenreTypes() {
        values = new String[MAX_GENRE_ID + 1];
        values[0] = "Blues";
        values[1] = "Classic Rock";
        values[2] = "Country";
        values[3] = "Dance";
        values[4] = "Disco";
        values[5] = "Funk";
        values[6] = "Grunge";
        values[7] = "Hip-Hop";
        values[8] = "Jazz";
        values[9] = "Metal";
        values[10] = "New Age";
        values[11] = "Oldies";
        values[12] = "Other";
        values[13] = "Pop";
        values[14] = "R&B";
        values[15] = "Rap";
        values[16] = "Reggae";
        values[17] = "Rock";
        values[18] = "Techno";
        values[19] = "Industrial";
        values[20] = "Alternative";
        values[21] = "Ska";
        values[22] = "Death Metal";
        values[23] = "Pranks";
        values[24] = "Soundtrack";
        values[25] = "Euro-Techno";
        values[26] = "Ambient";
        values[27] = "Trip-Hop";
        values[28] = "Vocal";
        values[29] = "Jazz+Funk";
        values[30] = "Fusion";
        values[31] = "Trance";
        values[32] = "Classical";
        values[33] = "Instrumental";
        values[34] = "Acid";
        values[35] = "House";
        values[36] = "Game";
        values[37] = "Sound Clip";
        values[38] = "Gospel";
        values[39] = "Noise";
        values[40] = "AlternRock";
        values[41] = "Bass";
        values[42] = "Soul";
        values[43] = "Punk";
        values[44] = "Space";
        values[45] = "Meditative";
        values[46] = "Instrumental Pop";
        values[47] = "Instrumental Rock";
        values[48] = "Ethnic";
        values[49] = "Gothic";
        values[50] = "Darkwave";
        values[51] = "Techno-Industrial";
        values[52] = "Electronic";
        values[53] = "Pop-Folk";
        values[54] = "Eurodance";
        values[55] = "Dream";
        values[56] = "Southern Rock";
        values[57] = "Comedy";
        values[58] = "Cult";
        values[59] = "Gangsta";
        values[60] = "Top 40";
        values[61] = "Christian Rap";
        values[62] = "Pop/Funk";
        values[63] = "Jungle";
        values[64] = "Native American";
        values[65] = "Cabaret";
        values[66] = "New Wave";
        values[67] = "Psychadelic";
        values[68] = "Rave";
        values[69] = "Showtunes";
        values[70] = "Trailer";
        values[71] = "Lo-Fi";
        values[72] = "Tribal";
        values[73] = "Acid Punk";
        values[74] = "Acid Jazz";
        values[75] = "Polka";
        values[76] = "Retro";
        values[77] = "Musical";
        values[78] = "Rock & Roll";
        values[79] = "Hard Rock";
        values[80] = "Folk";
        values[81] = "Folk-Rock";
        values[82] = "National Folk";
        values[83] = "Swing";
        values[84] = "Fast Fusion";
        values[85] = "Bebob";
        values[86] = "Latin";
        values[87] = "Revival";
        values[88] = "Celtic";
        values[89] = "Bluegrass";
        values[90] = "Avantgarde";
        values[91] = "Gothic Rock";
        values[92] = "Progressive Rock";
        values[93] = "Psychedelic Rock";
        values[94] = "Symphonic Rock";
        values[95] = "Slow Rock";
        values[96] = "Big Band";
        values[97] = "Chorus";
        values[98] = "Easy Listening";
        values[99] = "Acoustic";
        values[100] = "Humour";
        values[101] = "Speech";
        values[102] = "Chanson";
        values[103] = "Opera";
        values[104] = "Chamber Music";
        values[105] = "Sonata";
        values[106] = "Symphony";
        values[107] = "Booty Bass";
        values[108] = "Primus";
        values[109] = "Porn Groove";
        values[110] = "Satire";
        values[111] = "Slow Jam";
        values[112] = "Club";
        values[113] = "Tango";
        values[114] = "Samba";
        values[115] = "Folklore";
        values[116] = "Ballad";
        values[117] = "Power Ballad";
        values[118] = "Rhythmic Soul";
        values[119] = "Freestyle";
        values[120] = "Duet";
        values[121] = "Punk Rock";
        values[122] = "Drum Solo";
        values[123] = "Acapella";
        values[124] = "Euro-House";
        values[125] = "Dance Hall";
        values[126] = "Goa";
        values[127] = "Drum & Bass";
        values[128] = "Club-House";
        values[129] = "Hardcore";
        values[130] = "Terror";
        values[131] = "Indie";
        values[132] = "BritPop";
        values[133] = "Negerpunk"; // to say the least - this name is problematic
        values[134] = "Polsk Punk";
        values[135] = "Beat";
        values[136] = "Christian Gangsta Rap";
        values[137] = "Heavy Metal";
        values[138] = "Black Metal";
        values[139] = "Crossover";
        values[140] = "Contemporary Christian";
        values[141] = "Christian Rock";
        values[142] = "Merengue";
        values[143] = "Salsa";
        values[144] = "Thrash Metal";
        values[145] = "Anime";
        values[146] = "JPop";
        values[147] = "SynthPop";

        // additional Winamp 5.6 values taken from http://en.wikipedia.org/wiki/ID3#Winamp_Extensions
        values[148] = "Abstract";
        values[149] = "Art Rock";
        values[150] = "Baroque";
        values[151] = "Bhangra";
        values[152] = "Big Beat";
        values[153] = "Breakbeat";
        values[154] = "Chillout";
        values[155] = "Downtempo";
        values[156] = "Dub";
        values[157] = "EBM";
        values[158] = "Eclectic";
        values[159] = "Electro";
        values[160] = "Electroclash";
        values[161] = "Emo";
        values[162] = "Experimental";
        values[163] = "Garage";
        values[164] = "Global";
        values[165] = "IDM";
        values[166] = "Illbient";
        values[167] = "Industro-Goth";
        values[168] = "Jam Band";
        values[169] = "Krautrock";
        values[170] = "Leftfield";
        values[171] = "Lounge";
        values[172] = "Math Rock";
        values[173] = "New Romantic";
        values[174] = "Nu-Breakz";
        values[175] = "Post-Punk";
        values[176] = "Post-Rock";
        values[177] = "Psytrance";
        values[178] = "Shoegaze";
        values[179] = "Space Rock";
        values[180] = "Trop Rock";
        values[181] = "World Music";
        values[182] = "Neoclassical";
        values[183] = "Audiobook";
        values[184] = "Audio Theatre";
        values[185] = "Neue Deutsche Welle";
        values[186] = "Podcast";
        values[187] = "Indie Rock";
        values[188] = "G-Funk";
        values[189] = "Dubstep";
        values[190] = "Garage Rock";
        values[191] = "Psybient";
    }

    /**
     * @return the maximum genreId that is part of the official Standard, genres above this were added by Winamp later.
     */
    public static int getMaxStandardGenreId() {
        return MAX_STANDARD_GENRE_ID;
    }

    public static int getMaxGenreId() {
        return MAX_GENRE_ID;
    }

    public static GenreTypes getInstanceOf() {
        if (instance == null) {
            synchronized (GenreTypes.class) {
                if (instance == null) {
                    instance = new GenreTypes();
                }
            }
        }
        return instance;
    }

    public void iterateValues(ValuesIterator iterator) {
        int length = values.length;
        if (iterator.begin(length)) {
            for (String value : values) {
                if (!iterator.value(value)) {
                    break;
                }
            }
            iterator.end();
        }
    }

    /**
     * Get Id for Value. This is not case sensitive. Value may be exactly the value or the value in any character case.
     *
     * @param value genre value
     * @return the id for the genre value
     */
    public Integer getIdForValue(String value) {
        return getValueToIdMap().get(value);
    }

    /**
     * Currently this is only used for testing and in another unused class. We'll construct a set on the fly
     *
     * @return the set of all genre values
     */
    public ImmutableSortedSet<String> getSortedValueSet() {
        return ImmutableSortedSet.copyOf(values);
    }

    @Override
    public boolean containsKey(int key) {
        return key >= 0 && key < values.length;
    }

    @Override
    public String getValue(int id) {
        if (!containsKey(id)) {
            return "";
        }
        return Strings.nullToEmpty(values[id]);
    }

    public Optional<String> getOptionalValue(int id) {
        if (!containsKey(id)) {
            return Optional.absent();
        }
        return Optional.fromNullable(values[id]);
    }

    private TreeMap<String, Integer> getValueToIdMap() {
        if (valueToId == null) {
            synchronized (this) {
                if (valueToId == null) {
                    valueToId = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
                    for (int i = 0, size = values.length; i < size; i++) {
                        String value = Strings.nullToEmpty(values[i]);
                        valueToId.put(value, i);
                    }
                }
            }
        }
        return valueToId;
    }

    public interface ValuesIterator {
        boolean begin(int count);

        boolean value(String value);

        void end();
    }

}
