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

package ealvatag.tag.id3.framebody;

import com.google.common.collect.ImmutableMap;

import ealvatag.tag.InvalidTagException;
import ealvatag.tag.id3.ID3v22Frames;
import ealvatag.tag.id3.ID3v23Frames;
import ealvatag.tag.id3.ID3v24Frames;
import ealvatag.tag.id3.ID3v2ChapterFrames;
import okio.Buffer;

/**
 * This contains all the factories for Id3v2 frames
 * <p>
 * Created by Eric A. Snell on 1/25/17.
 */
public class Id3FrameBodyFactories implements Id3FrameBodyFactory {
    private static volatile Id3FrameBodyFactory instance;
    private final ImmutableMap<String, Id3FrameBodyFactory> factoryMap;

    private Id3FrameBodyFactories() {
        factoryMap = ImmutableMap.<String, Id3FrameBodyFactory>builder()
                .put(ID3v24Frames.FRAME_ID_AUDIO_ENCRYPTION, (frameId, buffer, frameSize) -> new FrameBodyAENC(buffer, frameSize))
                .put(ID3v24Frames.FRAME_ID_ATTACHED_PICTURE, (frameId, buffer, frameSize) -> new FrameBodyAPIC(buffer, frameSize))
                .put(ID3v24Frames.FRAME_ID_AUDIO_SEEK_POINT_INDEX, (frameId, buffer, frameSize) -> new FrameBodyASPI(buffer, frameSize))
                .put(ID3v2ChapterFrames.FRAME_ID_CHAPTER, (frameId, buffer, frameSize) -> new FrameBodyCHAP(buffer, frameSize))
                .put(ID3v24Frames.FRAME_ID_COMMENT, (frameId, buffer, frameSize) -> new FrameBodyCOMM(buffer, frameSize))
                .put(ID3v24Frames.FRAME_ID_COMMERCIAL_FRAME, (frameId, buffer, frameSize) -> new FrameBodyCOMR(buffer, frameSize))
                .put(ID3v22Frames.FRAME_ID_V2_ENCRYPTED_FRAME, (frameId, buffer, frameSize) -> new FrameBodyCRM(buffer, frameSize))
                .put(ID3v2ChapterFrames.FRAME_ID_TABLE_OF_CONTENT, (frameId, buffer, frameSize) -> new FrameBodyCTOC(buffer, frameSize))
                .put(ID3v24Frames.FRAME_ID_ENCRYPTION, (frameId, buffer, frameSize) -> new FrameBodyENCR(buffer, frameSize))
                .put(ID3v24Frames.FRAME_ID_EQUALISATION2, (frameId, buffer, frameSize) -> new FrameBodyEQU2(buffer, frameSize))
                .put(ID3v23Frames.FRAME_ID_V3_EQUALISATION, (frameId, buffer, frameSize) -> new FrameBodyEQUA(buffer, frameSize))
                .put(ID3v24Frames.FRAME_ID_EVENT_TIMING_CODES, (frameId, buffer, frameSize) -> new FrameBodyETCO(buffer, frameSize))
                .put(ID3v24Frames.FRAME_ID_GENERAL_ENCAPS_OBJECT, (frameId, buffer, frameSize) -> new FrameBodyGEOB(buffer, frameSize))
                .put(ID3v24Frames.FRAME_ID_GROUP_ID_REG, (frameId, buffer, frameSize) -> new FrameBodyGRID(buffer, frameSize))
                .put(ID3v24Frames.FRAME_ID_ITUNES_GROUPING, (frameId, buffer, frameSize) -> new FrameBodyGRP1(buffer, frameSize))
                .put(ID3v23Frames.FRAME_ID_V3_INVOLVED_PEOPLE, (frameId, buffer, frameSize) -> new FrameBodyIPLS(buffer, frameSize))
                .put(ID3v24Frames.FRAME_ID_LINKED_INFO, (frameId, buffer, frameSize) -> new FrameBodyLINK(buffer, frameSize))
                .put(ID3v24Frames.FRAME_ID_MUSIC_CD_ID, (frameId, buffer, frameSize) -> new FrameBodyMCDI(buffer, frameSize))
                .put(ID3v24Frames.FRAME_ID_MPEG_LOCATION_LOOKUP_TABLE, (frameId, buffer, frameSize) -> new FrameBodyMLLT(buffer, frameSize))
                .put(ID3v24Frames.FRAME_ID_MOVEMENT_NO, (frameId, buffer, frameSize) -> new FrameBodyMVIN(buffer, frameSize))
                .put(ID3v24Frames.FRAME_ID_MOVEMENT, (frameId, buffer, frameSize) -> new FrameBodyMVNM(buffer, frameSize))
                .put(ID3v24Frames.FRAME_ID_OWNERSHIP, (frameId, buffer, frameSize) -> new FrameBodyOWNE(buffer, frameSize))
                .put(ID3v24Frames.FRAME_ID_PLAY_COUNTER, (frameId, buffer, frameSize) -> new FrameBodyPCNT(buffer, frameSize))
                .put(ID3v22Frames.FRAME_ID_V2_ATTACHED_PICTURE, (frameId, buffer, frameSize) -> new FrameBodyPIC(buffer, frameSize))
                .put(ID3v24Frames.FRAME_ID_POPULARIMETER, (frameId, buffer, frameSize) -> new FrameBodyPOPM(buffer, frameSize))
                .put(ID3v24Frames.FRAME_ID_POSITION_SYNC, (frameId, buffer, frameSize) -> new FrameBodyPOSS(buffer, frameSize))
                .put(ID3v24Frames.FRAME_ID_PRIVATE, (frameId, buffer, frameSize) -> new FrameBodyPRIV(buffer, frameSize))
                .put(ID3v24Frames.FRAME_ID_RECOMMENDED_BUFFER_SIZE, (frameId, buffer, frameSize) -> new FrameBodyRBUF(buffer, frameSize))
                .put(ID3v24Frames.FRAME_ID_RELATIVE_VOLUME_ADJUSTMENT2, (frameId, buffer, frameSize) -> new FrameBodyRVA2(buffer, frameSize))
                .put(ID3v23Frames.FRAME_ID_V3_RELATIVE_VOLUME_ADJUSTMENT, (frameId, buffer, frameSize) -> new FrameBodyRVAD(buffer, frameSize))
                .put(ID3v24Frames.FRAME_ID_REVERB, (frameId, buffer, frameSize) -> new FrameBodyRVRB(buffer, frameSize))
                .put(ID3v24Frames.FRAME_ID_SEEK, (frameId, buffer, frameSize) -> new FrameBodySEEK(buffer, frameSize))
                .put(ID3v24Frames.FRAME_ID_SIGNATURE, (frameId, buffer, frameSize) -> new FrameBodySIGN(buffer, frameSize))
                .put(ID3v24Frames.FRAME_ID_SYNC_LYRIC, (frameId, buffer, frameSize) -> new FrameBodySYLT(buffer, frameSize))
                .put(ID3v24Frames.FRAME_ID_SYNC_TEMPO, (frameId, buffer, frameSize) -> new FrameBodySYTC(buffer, frameSize))
                .put(ID3v24Frames.FRAME_ID_BPM, (frameId, buffer, frameSize) -> new FrameBodyTBPM(buffer, frameSize))
                .put(ID3v24Frames.FRAME_ID_IS_COMPILATION, (frameId, buffer, frameSize) -> new FrameBodyTCMP(buffer, frameSize))
                .put(ID3v24Frames.FRAME_ID_COMPOSER, (frameId, buffer, frameSize) -> new FrameBodyTCOM(buffer, frameSize))
                .put(ID3v24Frames.FRAME_ID_GENRE, (frameId, buffer, frameSize) -> new FrameBodyTCON(buffer, frameSize))
                .put(ID3v24Frames.FRAME_ID_COPYRIGHTINFO, (frameId, buffer, frameSize) -> new FrameBodyTCOP(buffer, frameSize))
                .put(ID3v23Frames.FRAME_ID_V3_TDAT, (frameId, buffer, frameSize) -> new FrameBodyTDAT(buffer, frameSize))
                .put(ID3v24Frames.FRAME_ID_ENCODING_TIME, (frameId, buffer, frameSize) -> new FrameBodyTDEN(buffer, frameSize))
                .put(ID3v24Frames.FRAME_ID_PLAYLIST_DELAY, (frameId, buffer, frameSize) -> new FrameBodyTDLY(buffer, frameSize))
                .put(ID3v24Frames.FRAME_ID_ORIGINAL_RELEASE_TIME, (frameId, buffer, frameSize) -> new FrameBodyTDOR(buffer, frameSize))
                .put(ID3v24Frames.FRAME_ID_YEAR, (frameId, buffer, frameSize) -> new FrameBodyTDRC(buffer, frameSize))
                .put(ID3v24Frames.FRAME_ID_RELEASE_TIME, (frameId, buffer, frameSize) -> new FrameBodyTDRL(buffer, frameSize))
                .put(ID3v24Frames.FRAME_ID_TAGGING_TIME, (frameId, buffer, frameSize) -> new FrameBodyTDTG(buffer, frameSize))
                .put(ID3v24Frames.FRAME_ID_ENCODEDBY, (frameId, buffer, frameSize) -> new FrameBodyTENC(buffer, frameSize))
                .put(ID3v24Frames.FRAME_ID_LYRICIST, (frameId, buffer, frameSize) -> new FrameBodyTEXT(buffer, frameSize))
                .put(ID3v24Frames.FRAME_ID_FILE_TYPE, (frameId, buffer, frameSize) -> new FrameBodyTFLT(buffer, frameSize))
                .put(ID3v23Frames.FRAME_ID_V3_TIME, (frameId, buffer, frameSize) -> new FrameBodyTIME(buffer, frameSize))
                .put(ID3v24Frames.FRAME_ID_INVOLVED_PEOPLE, (frameId, buffer, frameSize) -> new FrameBodyTIPL(buffer, frameSize))
                .put(ID3v24Frames.FRAME_ID_CONTENT_GROUP_DESC, (frameId, buffer, frameSize) -> new FrameBodyTIT1(buffer, frameSize))
                .put(ID3v24Frames.FRAME_ID_TITLE, (frameId, buffer, frameSize) -> new FrameBodyTIT2(buffer, frameSize))
                .put(ID3v24Frames.FRAME_ID_TITLE_REFINEMENT, (frameId, buffer, frameSize) -> new FrameBodyTIT3(buffer, frameSize))
                .put(ID3v24Frames.FRAME_ID_INITIAL_KEY, (frameId, buffer, frameSize) -> new FrameBodyTKEY(buffer, frameSize))
                .put(ID3v24Frames.FRAME_ID_LANGUAGE, (frameId, buffer, frameSize) -> new FrameBodyTLAN(buffer, frameSize))
                .put(ID3v24Frames.FRAME_ID_LENGTH, (frameId, buffer, frameSize) -> new FrameBodyTLEN(buffer, frameSize))
                .put(ID3v24Frames.FRAME_ID_MUSICIAN_CREDITS, (frameId, buffer, frameSize) -> new FrameBodyTMCL(buffer, frameSize))
                .put(ID3v24Frames.FRAME_ID_MEDIA_TYPE, (frameId, buffer, frameSize) -> new FrameBodyTMED(buffer, frameSize))
                .put(ID3v24Frames.FRAME_ID_MOOD, (frameId, buffer, frameSize) -> new FrameBodyTMOO(buffer, frameSize))
                .put(ID3v24Frames.FRAME_ID_ORIG_TITLE, (frameId, buffer, frameSize) -> new FrameBodyTOAL(buffer, frameSize))
                .put(ID3v24Frames.FRAME_ID_ORIG_FILENAME, (frameId, buffer, frameSize) -> new FrameBodyTOFN(buffer, frameSize))
                .put(ID3v24Frames.FRAME_ID_ORIG_LYRICIST, (frameId, buffer, frameSize) -> new FrameBodyTOLY(buffer, frameSize))
                .put(ID3v24Frames.FRAME_ID_ORIGARTIST, (frameId, buffer, frameSize) -> new FrameBodyTOPE(buffer, frameSize))
                .put(ID3v23Frames.FRAME_ID_V3_TORY, (frameId, buffer, frameSize) -> new FrameBodyTORY(buffer, frameSize))
                .put(ID3v24Frames.FRAME_ID_FILE_OWNER, (frameId, buffer, frameSize) -> new FrameBodyTOWN(buffer, frameSize))
                .put(ID3v24Frames.FRAME_ID_ARTIST, (frameId, buffer, frameSize) -> new FrameBodyTPE1(buffer, frameSize))
                .put(ID3v24Frames.FRAME_ID_ACCOMPANIMENT, (frameId, buffer, frameSize) -> new FrameBodyTPE2(buffer, frameSize))
                .put(ID3v24Frames.FRAME_ID_CONDUCTOR, (frameId, buffer, frameSize) -> new FrameBodyTPE3(buffer, frameSize))
                .put(ID3v24Frames.FRAME_ID_REMIXED, (frameId, buffer, frameSize) -> new FrameBodyTPE4(buffer, frameSize))
                .put(ID3v24Frames.FRAME_ID_SET, (frameId, buffer, frameSize) -> new FrameBodyTPOS(buffer, frameSize))
                .put(ID3v24Frames.FRAME_ID_PRODUCED_NOTICE, (frameId, buffer, frameSize) -> new FrameBodyTPRO(buffer, frameSize))
                .put(ID3v24Frames.FRAME_ID_PUBLISHER, (frameId, buffer, frameSize) -> new FrameBodyTPUB(buffer, frameSize))
                .put(ID3v24Frames.FRAME_ID_TRACK, (frameId, buffer, frameSize) -> new FrameBodyTRCK(buffer, frameSize))
                .put(ID3v23Frames.FRAME_ID_V3_TRDA, (frameId, buffer, frameSize) -> new FrameBodyTRDA(buffer, frameSize))
                .put(ID3v24Frames.FRAME_ID_RADIO_NAME, (frameId, buffer, frameSize) -> new FrameBodyTRSN(buffer, frameSize))
                .put(ID3v24Frames.FRAME_ID_RADIO_OWNER, (frameId, buffer, frameSize) -> new FrameBodyTRSO(buffer, frameSize))
                .put(ID3v23Frames.FRAME_ID_V3_TSIZ, (frameId, buffer, frameSize) -> new FrameBodyTSIZ(buffer, frameSize))
                .put(ID3v24Frames.FRAME_ID_ALBUM_ARTIST_SORT_ORDER_ITUNES, (frameId, buffer, frameSize) -> new FrameBodyTSO2(buffer, frameSize))
                .put(ID3v24Frames.FRAME_ID_ALBUM_SORT_ORDER, (frameId, buffer, frameSize) -> new FrameBodyTSOA(buffer, frameSize))
                .put(ID3v24Frames.FRAME_ID_COMPOSER_SORT_ORDER_ITUNES, (frameId, buffer, frameSize) -> new FrameBodyTSOC(buffer, frameSize))
                .put(ID3v24Frames.FRAME_ID_ARTIST_SORT_ORDER, (frameId, buffer, frameSize) -> new FrameBodyTSOP(buffer, frameSize))
                .put(ID3v24Frames.FRAME_ID_TITLE_SORT_ORDER, (frameId, buffer, frameSize) -> new FrameBodyTSOT(buffer, frameSize))
                .put(ID3v24Frames.FRAME_ID_ISRC, (frameId, buffer, frameSize) -> new FrameBodyTSRC(buffer, frameSize))
                .put(ID3v24Frames.FRAME_ID_HW_SW_SETTINGS, (frameId, buffer, frameSize) -> new FrameBodyTSSE(buffer, frameSize))
                .put(ID3v24Frames.FRAME_ID_SET_SUBTITLE, (frameId, buffer, frameSize) -> new FrameBodyTSST(buffer, frameSize))
                .put(ID3v24Frames.FRAME_ID_USER_DEFINED_INFO, (frameId, buffer, frameSize) -> new FrameBodyTXXX(buffer, frameSize))
                .put(ID3v23Frames.FRAME_ID_V3_TYER, (frameId, buffer, frameSize) -> new FrameBodyTYER(buffer, frameSize))
                .put(ID3v24Frames.FRAME_ID_UNIQUE_FILE_ID, (frameId, buffer, frameSize) -> new FrameBodyUFID(buffer, frameSize))
                .put(ID3v24Frames.FRAME_ID_TERMS_OF_USE, (frameId, buffer, frameSize) -> new FrameBodyUSER(buffer, frameSize))
                .put(ID3v24Frames.FRAME_ID_UNSYNC_LYRICS, (frameId, buffer, frameSize) -> new FrameBodyUSLT(buffer, frameSize))
                .put(ID3v24Frames.FRAME_ID_URL_COMMERCIAL, (frameId, buffer, frameSize) -> new FrameBodyWCOM(buffer, frameSize))
                .put(ID3v24Frames.FRAME_ID_URL_COPYRIGHT, (frameId, buffer, frameSize) -> new FrameBodyWCOP(buffer, frameSize))
                .put(ID3v24Frames.FRAME_ID_URL_FILE_WEB, (frameId, buffer, frameSize) -> new FrameBodyWOAF(buffer, frameSize))
                .put(ID3v24Frames.FRAME_ID_URL_ARTIST_WEB, (frameId, buffer, frameSize) -> new FrameBodyWOAR(buffer, frameSize))
                .put(ID3v24Frames.FRAME_ID_URL_SOURCE_WEB, (frameId, buffer, frameSize) -> new FrameBodyWOAS(buffer, frameSize))
                .put(ID3v24Frames.FRAME_ID_URL_OFFICIAL_RADIO, (frameId, buffer, frameSize) -> new FrameBodyWORS(buffer, frameSize))
                .put(ID3v24Frames.FRAME_ID_URL_PAYMENT, (frameId, buffer, frameSize) -> new FrameBodyWPAY(buffer, frameSize))
                .put(ID3v24Frames.FRAME_ID_URL_PUBLISHERS, (frameId, buffer, frameSize) -> new FrameBodyWPUB(buffer, frameSize))
                .put(ID3v24Frames.FRAME_ID_USER_DEFINED_URL, (frameId, buffer, frameSize) -> new FrameBodyWXXX(buffer, frameSize))
                .put(ID3v23Frames.FRAME_ID_V3_ALBUM_SORT_ORDER_MUSICBRAINZ, (frameId, buffer, frameSize) -> new FrameBodyXSOA(buffer, frameSize))
                .put(ID3v23Frames.FRAME_ID_V3_ARTIST_SORT_ORDER_MUSICBRAINZ, (frameId, buffer, frameSize) -> new FrameBodyXSOP(buffer, frameSize))
                .put(ID3v23Frames.FRAME_ID_V3_TITLE_SORT_ORDER_MUSICBRAINZ, (frameId, buffer, frameSize) -> new FrameBodyXSOT(buffer, frameSize))


                .put(ID3v24Frames.FRAME_ID_ALBUM, (frameId, buffer, frameSize) -> new FrameBodyTALB(buffer, frameSize))
                .build();
    }

    public static Id3FrameBodyFactory instance() {
        if (instance == null) {
            synchronized (Id3FrameBodyFactories.class) {
                if (instance == null) {
                    instance = new Id3FrameBodyFactories();
                }
            }
        }
        return instance;
    }

    @Override
    public AbstractID3v2FrameBody make(String frameId,
                                       Buffer buffer,
                                       int frameSize) throws FrameIdentifierException, InvalidTagException {
        Id3FrameBodyFactory id3FrameBodyFactory = factoryMap.get(frameId);
        if (id3FrameBodyFactory == null) {
            throw new FrameIdentifierException(frameId);
        }
        return id3FrameBodyFactory.make(frameId, buffer, frameSize);
    }
}
