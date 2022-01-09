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

package ealvatag.tag.id3;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

/**
 * Maps between ID3 versions
 * <p>
 * Created by Eric A. Snell on 1/19/17.
 */
class ID3Mapping {
    //TODO What does CRM Map to ?
    // Force v22 to v23,  Extra fields in v23 version
    private static final ImmutableMap<String, String> forcev22Tov23 =
            ImmutableMap.of(ID3v22Frames.FRAME_ID_V2_ATTACHED_PICTURE,
                    ID3v23Frames.FRAME_ID_V3_ATTACHED_PICTURE);
    private static final ImmutableMap<String, String> forcev23Tov22 =
            ImmutableMap.of(ID3v23Frames.FRAME_ID_V3_ATTACHED_PICTURE,
                    ID3v22Frames.FRAME_ID_V2_ATTACHED_PICTURE);
    // No others exist because most v23 mappings are identical to v24 therefore no mapping required and the ones that
    // are different need to be forced.
    private static final ImmutableMap<String, String> convertv23Tov24 =
            ImmutableMap.of(ID3v23Frames.FRAME_ID_V3_TITLE_SORT_ORDER_MUSICBRAINZ, ID3v24Frames.FRAME_ID_TITLE_SORT_ORDER,
                    ID3v23Frames.FRAME_ID_V3_ARTIST_SORT_ORDER_MUSICBRAINZ, ID3v24Frames.FRAME_ID_ARTIST_SORT_ORDER,
                    ID3v23Frames.FRAME_ID_V3_ALBUM_SORT_ORDER_MUSICBRAINZ, ID3v24Frames.FRAME_ID_ALBUM_SORT_ORDER);
    private static final Map<String, String> convertv24Tov23 = ImmutableMap.of();


    // Define the mapping from v23 to v24 only maps values where
    // the v23 ID is not a v24 ID and where the translation from v23 to v24
    // ID does not affect the framebody.
    //This one way allows us to convert XSOT to TSOT,XSOP to TSOP and XSOA - TSOA but in the other direction gets converted to TSOT,TSOP,TSOA
    // Force v23 to v24 These are deprecated and need to do a forced mapping
    private static final ImmutableMap<String, String> forcev23Tov24 =
            ImmutableMap.<String, String>builder()
                    .put(ID3v23Frames.FRAME_ID_V3_RELATIVE_VOLUME_ADJUSTMENT, ID3v24Frames.FRAME_ID_RELATIVE_VOLUME_ADJUSTMENT2)
                    .put(ID3v23Frames.FRAME_ID_V3_EQUALISATION, ID3v24Frames.FRAME_ID_EQUALISATION2)
                    .put(ID3v23Frames.FRAME_ID_V3_INVOLVED_PEOPLE, ID3v24Frames.FRAME_ID_INVOLVED_PEOPLE)
                    .put(ID3v23Frames.FRAME_ID_V3_TDAT, ID3v24Frames.FRAME_ID_YEAR)
                    .put(ID3v23Frames.FRAME_ID_V3_TIME, ID3v24Frames.FRAME_ID_YEAR)
                    .put(ID3v23Frames.FRAME_ID_V3_TORY, ID3v24Frames.FRAME_ID_ORIGINAL_RELEASE_TIME)
                    .put(ID3v23Frames.FRAME_ID_V3_TRDA, ID3v24Frames.FRAME_ID_YEAR)
                    .put(ID3v23Frames.FRAME_ID_V3_TYER, ID3v24Frames.FRAME_ID_YEAR).build();
    //Note Force v24 to v23, TDRC is a 1M relationship handled specially.
    // @TODO EQUALISATION
    private static final ImmutableMap<String, String> forcev24Tov23 =
            ImmutableMap.<String, String>builder()
                    .put(ID3v24Frames.FRAME_ID_RELATIVE_VOLUME_ADJUSTMENT2, ID3v23Frames.FRAME_ID_V3_RELATIVE_VOLUME_ADJUSTMENT)
                    //Used to be a special frame now a text frame
                    .put(ID3v24Frames.FRAME_ID_INVOLVED_PEOPLE, ID3v23Frames.FRAME_ID_V3_INVOLVED_PEOPLE)
                    //No Mood frame in v23 so use a TXXX frame
                    .put(ID3v24Frames.FRAME_ID_MOOD, ID3v23Frames.FRAME_ID_V3_USER_DEFINED_INFO)
                    //Release time can be mapped to release year (but can only hold year)
                    .put(ID3v24Frames.FRAME_ID_ORIGINAL_RELEASE_TIME, ID3v23Frames.FRAME_ID_V3_TORY).build();
    private static volatile ImmutableMap<String, String> convertv22Tov23;
    private static volatile ImmutableMap<String, String> convertv23Tov22;

    public static String convertV22ToV23(String v22) {
        return getConvertv22Tov23().get(v22);
    }

    public static String convertV23ToV22(String v23) {
        return getConvertv23Tov22().get(v23);
    }

    public static String forceV22ToV23(String v22) {
        return getForcev22Tov23().get(v22);
    }

    public static String forceV23ToV22(String v23) {
        return getForcev23Tov22().get(v23);
    }

    public static String convertV23ToV24(String v23) {
        return getConvertv23Tov24().get(v23);
    }

    public static String convertV24ToV23(String v24) {
        return getConvertv24Tov23().get(v24);
    }

    public static String forceV23ToV24(String v23) {
        return getForcev23Tov24().get(v23);
    }

    public static String forceV24ToV23(String v24) {
        return getForcev24Tov23().get(v24);
    }

    private static ImmutableMap<String, String> getConvertv22Tov23() {
        if (convertv22Tov23 == null) {
            synchronized (ID3Mapping.class) {
                if (convertv22Tov23 == null) {
                    convertv22Tov23 = makeConvertV22ToV23();
                }
            }
        }
        return convertv22Tov23;
    }

    private static ImmutableMap<String, String> makeConvertV22ToV23() {
        ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();
        builder.put(ID3v22Frames.FRAME_ID_V2_ACCOMPANIMENT, ID3v23Frames.FRAME_ID_V3_ACCOMPANIMENT)
                .put(ID3v22Frames.FRAME_ID_V2_ALBUM, ID3v23Frames.FRAME_ID_V3_ALBUM)
                .put(ID3v22Frames.FRAME_ID_V2_ARTIST, ID3v23Frames.FRAME_ID_V3_ARTIST)
                .put(ID3v22Frames.FRAME_ID_V2_AUDIO_ENCRYPTION, ID3v23Frames.FRAME_ID_V3_AUDIO_ENCRYPTION)
                .put(ID3v22Frames.FRAME_ID_V2_BPM, ID3v23Frames.FRAME_ID_V3_BPM)
                .put(ID3v22Frames.FRAME_ID_V2_COMMENT, ID3v23Frames.FRAME_ID_V3_COMMENT)
//        .put(ID3v22Frames.FRAME_ID_V2_COMMENT, ID3v23Frames.FRAME_ID_V3_COMMENT)  Dup?? missing one?
                .put(ID3v22Frames.FRAME_ID_V2_COMPOSER, ID3v23Frames.FRAME_ID_V3_COMPOSER)
                .put(ID3v22Frames.FRAME_ID_V2_CONDUCTOR, ID3v23Frames.FRAME_ID_V3_CONDUCTOR)
                .put(ID3v22Frames.FRAME_ID_V2_CONTENT_GROUP_DESC, ID3v23Frames.FRAME_ID_V3_CONTENT_GROUP_DESC)
                .put(ID3v22Frames.FRAME_ID_V2_COPYRIGHTINFO, ID3v23Frames.FRAME_ID_V3_COPYRIGHTINFO)
                .put(ID3v22Frames.FRAME_ID_V2_ENCODEDBY, ID3v23Frames.FRAME_ID_V3_ENCODEDBY)
                .put(ID3v22Frames.FRAME_ID_V2_EQUALISATION, ID3v23Frames.FRAME_ID_V3_EQUALISATION)
                .put(ID3v22Frames.FRAME_ID_V2_EVENT_TIMING_CODES, ID3v23Frames.FRAME_ID_V3_EVENT_TIMING_CODES)
                .put(ID3v22Frames.FRAME_ID_V2_FILE_TYPE, ID3v23Frames.FRAME_ID_V3_FILE_TYPE)
                .put(ID3v22Frames.FRAME_ID_V2_GENERAL_ENCAPS_OBJECT, ID3v23Frames.FRAME_ID_V3_GENERAL_ENCAPS_OBJECT)
                .put(ID3v22Frames.FRAME_ID_V2_GENRE, ID3v23Frames.FRAME_ID_V3_GENRE)
                .put(ID3v22Frames.FRAME_ID_V2_HW_SW_SETTINGS, ID3v23Frames.FRAME_ID_V3_HW_SW_SETTINGS)
                .put(ID3v22Frames.FRAME_ID_V2_INITIAL_KEY, ID3v23Frames.FRAME_ID_V3_INITIAL_KEY)
                .put(ID3v22Frames.FRAME_ID_V2_IPLS, ID3v23Frames.FRAME_ID_V3_INVOLVED_PEOPLE)
                .put(ID3v22Frames.FRAME_ID_V2_ISRC, ID3v23Frames.FRAME_ID_V3_ISRC)
                .put(ID3v22Frames.FRAME_ID_V2_ITUNES_GROUPING, ID3v23Frames.FRAME_ID_V3_ITUNES_GROUPING)
                .put(ID3v22Frames.FRAME_ID_V2_LANGUAGE, ID3v23Frames.FRAME_ID_V3_LANGUAGE)
                .put(ID3v22Frames.FRAME_ID_V2_LENGTH, ID3v23Frames.FRAME_ID_V3_LENGTH)
                .put(ID3v22Frames.FRAME_ID_V2_LINKED_INFO, ID3v23Frames.FRAME_ID_V3_LINKED_INFO)
                .put(ID3v22Frames.FRAME_ID_V2_LYRICIST, ID3v23Frames.FRAME_ID_V3_LYRICIST)
                .put(ID3v22Frames.FRAME_ID_V2_MEDIA_TYPE, ID3v23Frames.FRAME_ID_V3_MEDIA_TYPE)
                .put(ID3v22Frames.FRAME_ID_V2_MOVEMENT, ID3v23Frames.FRAME_ID_V3_MOVEMENT)
                .put(ID3v22Frames.FRAME_ID_V2_MOVEMENT_NO, ID3v23Frames.FRAME_ID_V3_MOVEMENT_NO)
                .put(ID3v22Frames.FRAME_ID_V2_MPEG_LOCATION_LOOKUP_TABLE, ID3v23Frames.FRAME_ID_V3_MPEG_LOCATION_LOOKUP_TABLE)
                .put(ID3v22Frames.FRAME_ID_V2_MUSIC_CD_ID, ID3v23Frames.FRAME_ID_V3_MUSIC_CD_ID)
                .put(ID3v22Frames.FRAME_ID_V2_ORIGARTIST, ID3v23Frames.FRAME_ID_V3_ORIGARTIST)
                .put(ID3v22Frames.FRAME_ID_V2_ORIG_FILENAME, ID3v23Frames.FRAME_ID_V3_ORIG_FILENAME)
                .put(ID3v22Frames.FRAME_ID_V2_ORIG_LYRICIST, ID3v23Frames.FRAME_ID_V3_ORIG_LYRICIST)
                .put(ID3v22Frames.FRAME_ID_V2_ORIG_TITLE, ID3v23Frames.FRAME_ID_V3_ORIG_TITLE)
                .put(ID3v22Frames.FRAME_ID_V2_PLAYLIST_DELAY, ID3v23Frames.FRAME_ID_V3_PLAYLIST_DELAY)
                .put(ID3v22Frames.FRAME_ID_V2_PLAY_COUNTER, ID3v23Frames.FRAME_ID_V3_PLAY_COUNTER)
//        .put(ID3v22Frames.FRAME_ID_V2_PLAY_COUNTER, ID3v23Frames.FRAME_ID_V3_PLAY_COUNTER)  Dup??? Indicates missing one?
                .put(ID3v22Frames.FRAME_ID_V2_POPULARIMETER, ID3v23Frames.FRAME_ID_V3_POPULARIMETER)
                .put(ID3v22Frames.FRAME_ID_V2_PUBLISHER, ID3v23Frames.FRAME_ID_V3_PUBLISHER)
                .put(ID3v22Frames.FRAME_ID_V2_RECOMMENDED_BUFFER_SIZE, ID3v23Frames.FRAME_ID_V3_RECOMMENDED_BUFFER_SIZE)
//        .put(ID3v22Frames.FRAME_ID_V2_RECOMMENDED_BUFFER_SIZE, ID3v23Frames.FRAME_ID_V3_RECOMMENDED_BUFFER_SIZE) Dup?? What's up?
                .put(ID3v22Frames.FRAME_ID_V2_RELATIVE_VOLUME_ADJUSTMENT, ID3v23Frames.FRAME_ID_V3_RELATIVE_VOLUME_ADJUSTMENT)
                .put(ID3v22Frames.FRAME_ID_V2_REMIXED, ID3v23Frames.FRAME_ID_V3_REMIXED)
                .put(ID3v22Frames.FRAME_ID_V2_REVERB, ID3v23Frames.FRAME_ID_V3_REVERB)
                .put(ID3v22Frames.FRAME_ID_V2_SET, ID3v23Frames.FRAME_ID_V3_SET)
                .put(ID3v22Frames.FRAME_ID_V2_SET_SUBTITLE, ID3v23Frames.FRAME_ID_V3_SET_SUBTITLE)
                .put(ID3v22Frames.FRAME_ID_V2_SYNC_LYRIC, ID3v23Frames.FRAME_ID_V3_SYNC_LYRIC)
                .put(ID3v22Frames.FRAME_ID_V2_SYNC_TEMPO, ID3v23Frames.FRAME_ID_V3_SYNC_TEMPO)
                .put(ID3v22Frames.FRAME_ID_V2_TDAT, ID3v23Frames.FRAME_ID_V3_TDAT)
                .put(ID3v22Frames.FRAME_ID_V2_TIME, ID3v23Frames.FRAME_ID_V3_TIME)
                .put(ID3v22Frames.FRAME_ID_V2_TITLE_REFINEMENT, ID3v23Frames.FRAME_ID_V3_TITLE_REFINEMENT)
                .put(ID3v22Frames.FRAME_ID_V2_TORY, ID3v23Frames.FRAME_ID_V3_TORY)
                .put(ID3v22Frames.FRAME_ID_V2_TRACK, ID3v23Frames.FRAME_ID_V3_TRACK)
                .put(ID3v22Frames.FRAME_ID_V2_TRDA, ID3v23Frames.FRAME_ID_V3_TRDA)
                .put(ID3v22Frames.FRAME_ID_V2_TSIZ, ID3v23Frames.FRAME_ID_V3_TSIZ)
                .put(ID3v22Frames.FRAME_ID_V2_TYER, ID3v23Frames.FRAME_ID_V3_TYER)
                .put(ID3v22Frames.FRAME_ID_V2_UNIQUE_FILE_ID, ID3v23Frames.FRAME_ID_V3_UNIQUE_FILE_ID)
//        .put(ID3v22Frames.FRAME_ID_V2_UNIQUE_FILE_ID, ID3v23Frames.FRAME_ID_V3_UNIQUE_FILE_ID) YAD - Yet another dup
                .put(ID3v22Frames.FRAME_ID_V2_UNSYNC_LYRICS, ID3v23Frames.FRAME_ID_V3_UNSYNC_LYRICS)
                .put(ID3v22Frames.FRAME_ID_V2_URL_ARTIST_WEB, ID3v23Frames.FRAME_ID_V3_URL_ARTIST_WEB)
                .put(ID3v22Frames.FRAME_ID_V2_URL_COMMERCIAL, ID3v23Frames.FRAME_ID_V3_URL_COMMERCIAL)
                .put(ID3v22Frames.FRAME_ID_V2_URL_COPYRIGHT, ID3v23Frames.FRAME_ID_V3_URL_COPYRIGHT)
                .put(ID3v22Frames.FRAME_ID_V2_URL_FILE_WEB, ID3v23Frames.FRAME_ID_V3_URL_FILE_WEB)
                .put(ID3v22Frames.FRAME_ID_V2_URL_OFFICIAL_RADIO, ID3v23Frames.FRAME_ID_V3_URL_OFFICIAL_RADIO)
                .put(ID3v22Frames.FRAME_ID_V2_URL_PAYMENT, ID3v23Frames.FRAME_ID_V3_URL_PAYMENT)
                .put(ID3v22Frames.FRAME_ID_V2_URL_PUBLISHERS, ID3v23Frames.FRAME_ID_V3_URL_PUBLISHERS)
                .put(ID3v22Frames.FRAME_ID_V2_URL_SOURCE_WEB, ID3v23Frames.FRAME_ID_V3_URL_SOURCE_WEB)
                .put(ID3v22Frames.FRAME_ID_V2_USER_DEFINED_INFO, ID3v23Frames.FRAME_ID_V3_USER_DEFINED_INFO)
                .put(ID3v22Frames.FRAME_ID_V2_USER_DEFINED_URL, ID3v23Frames.FRAME_ID_V3_USER_DEFINED_URL)
                .put(ID3v22Frames.FRAME_ID_V2_TITLE, ID3v23Frames.FRAME_ID_V3_TITLE)
                .put(ID3v22Frames.FRAME_ID_V2_IS_COMPILATION, ID3v23Frames.FRAME_ID_V3_IS_COMPILATION)
                .put(ID3v22Frames.FRAME_ID_V2_TITLE_SORT_ORDER_ITUNES, ID3v23Frames.FRAME_ID_V3_TITLE_SORT_ORDER_ITUNES)
                .put(ID3v22Frames.FRAME_ID_V2_ARTIST_SORT_ORDER_ITUNES, ID3v23Frames.FRAME_ID_V3_ARTIST_SORT_ORDER_ITUNES)
                .put(ID3v22Frames.FRAME_ID_V2_ALBUM_SORT_ORDER_ITUNES, ID3v23Frames.FRAME_ID_V3_ALBUM_SORT_ORDER_ITUNES)
                .put(ID3v22Frames.FRAME_ID_V2_ALBUM_ARTIST_SORT_ORDER_ITUNES,
                        ID3v23Frames.FRAME_ID_V3_ALBUM_ARTIST_SORT_ORDER_ITUNES)
                .put(ID3v22Frames.FRAME_ID_V2_COMPOSER_SORT_ORDER_ITUNES, ID3v23Frames.FRAME_ID_V3_COMPOSER_SORT_ORDER_ITUNES);
        return builder.build();
    }

    private static ImmutableMap<String, String> getConvertv23Tov22() {
        if (convertv23Tov22 == null) {
            synchronized (ID3Mapping.class) {
                if (convertv23Tov22 == null) {
                    convertv23Tov22 = makeConvertV23ToV22();
                }
            }
        }
        return convertv23Tov22;
    }

    private static ImmutableMap<String, String> makeConvertV23ToV22() {
        ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();
        ImmutableMap<String, String> convertv22Tov23 = getConvertv22Tov23();
        for (Map.Entry<String, String> entry : convertv22Tov23.entrySet()) {
            builder.put(entry.getValue(), entry.getKey());
        }
        //This one way translation allows us to convert XSOT to TST, but in the other direction gets converted to TSOT
        builder.put(ID3v23Frames.FRAME_ID_V3_TITLE_SORT_ORDER_MUSICBRAINZ, ID3v22Frames.FRAME_ID_V2_TITLE_SORT_ORDER_ITUNES)
                .put(ID3v23Frames.FRAME_ID_V3_ARTIST_SORT_ORDER_MUSICBRAINZ, ID3v22Frames.FRAME_ID_V2_ARTIST_SORT_ORDER_ITUNES)
                .put(ID3v23Frames.FRAME_ID_V3_ALBUM_SORT_ORDER_MUSICBRAINZ, ID3v22Frames.FRAME_ID_V2_ALBUM_SORT_ORDER_ITUNES);
        return builder.build();
    }

    private static Map<String, String> getForcev22Tov23() {
        return forcev22Tov23;
    }

    private static Map<String, String> getForcev23Tov22() {
        return forcev23Tov22;
    }

    private static Map<String, String> getConvertv23Tov24() {
        return convertv23Tov24;
    }

    private static Map<String, String> getConvertv24Tov23() {
        return convertv24Tov23;
    }

    private static Map<String, String> getForcev23Tov24() {
        return forcev23Tov24;
    }

    private static Map<String, String> getForcev24Tov23() {
        return forcev24Tov23;
    }
}
