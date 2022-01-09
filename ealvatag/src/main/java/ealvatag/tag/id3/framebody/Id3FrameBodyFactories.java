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
                .put(ID3v24Frames.FRAME_ID_AUDIO_ENCRYPTION, new Id3FrameBodyFactory() {
                    @Override
                    public AbstractID3v2FrameBody make(String frameId, Buffer buffer, int frameSize)
                            throws FrameIdentifierException, InvalidTagException {
                        return new FrameBodyAENC(buffer, frameSize);
                    }
                })
                .put(ID3v24Frames.FRAME_ID_ATTACHED_PICTURE, new Id3FrameBodyFactory() {
                    @Override
                    public AbstractID3v2FrameBody make(String frameId, Buffer buffer, int frameSize)
                            throws FrameIdentifierException, InvalidTagException {
                        return new FrameBodyAPIC(buffer, frameSize);
                    }
                })
                .put(ID3v24Frames.FRAME_ID_AUDIO_SEEK_POINT_INDEX, new Id3FrameBodyFactory() {
                    @Override
                    public AbstractID3v2FrameBody make(String frameId, Buffer buffer, int frameSize)
                            throws FrameIdentifierException, InvalidTagException {
                        return new FrameBodyASPI(buffer, frameSize);
                    }
                })
                .put(ID3v2ChapterFrames.FRAME_ID_CHAPTER, new Id3FrameBodyFactory() {
                    @Override
                    public AbstractID3v2FrameBody make(String frameId, Buffer buffer, int frameSize)
                            throws FrameIdentifierException, InvalidTagException {
                        return new FrameBodyCHAP(buffer, frameSize);
                    }
                })
                .put(ID3v24Frames.FRAME_ID_COMMENT, new Id3FrameBodyFactory() {
                    @Override
                    public AbstractID3v2FrameBody make(String frameId, Buffer buffer, int frameSize)
                            throws FrameIdentifierException, InvalidTagException {
                        return new FrameBodyCOMM(buffer, frameSize);
                    }
                })
                .put(ID3v24Frames.FRAME_ID_COMMERCIAL_FRAME, new Id3FrameBodyFactory() {
                    @Override
                    public AbstractID3v2FrameBody make(String frameId, Buffer buffer, int frameSize)
                            throws FrameIdentifierException, InvalidTagException {
                        return new FrameBodyCOMR(buffer, frameSize);
                    }
                })
                .put(ID3v22Frames.FRAME_ID_V2_ENCRYPTED_FRAME, new Id3FrameBodyFactory() {
                    @Override
                    public AbstractID3v2FrameBody make(String frameId, Buffer buffer, int frameSize)
                            throws FrameIdentifierException, InvalidTagException {
                        return new FrameBodyCRM(buffer, frameSize);
                    }
                })
                .put(ID3v2ChapterFrames.FRAME_ID_TABLE_OF_CONTENT, new Id3FrameBodyFactory() {
                    @Override
                    public AbstractID3v2FrameBody make(String frameId, Buffer buffer, int frameSize)
                            throws FrameIdentifierException, InvalidTagException {
                        return new FrameBodyCTOC(buffer, frameSize);
                    }
                })
                .put(ID3v24Frames.FRAME_ID_ENCRYPTION, new Id3FrameBodyFactory() {
                    @Override
                    public AbstractID3v2FrameBody make(String frameId, Buffer buffer, int frameSize)
                            throws FrameIdentifierException, InvalidTagException {
                        return new FrameBodyENCR(buffer, frameSize);
                    }
                })
                .put(ID3v24Frames.FRAME_ID_EQUALISATION2, new Id3FrameBodyFactory() {
                    @Override
                    public AbstractID3v2FrameBody make(String frameId, Buffer buffer, int frameSize)
                            throws FrameIdentifierException, InvalidTagException {
                        return new FrameBodyEQU2(buffer, frameSize);
                    }
                })
                .put(ID3v23Frames.FRAME_ID_V3_EQUALISATION, new Id3FrameBodyFactory() {
                    @Override
                    public AbstractID3v2FrameBody make(String frameId, Buffer buffer, int frameSize)
                            throws FrameIdentifierException, InvalidTagException {
                        return new FrameBodyEQUA(buffer, frameSize);
                    }
                })
                .put(ID3v24Frames.FRAME_ID_EVENT_TIMING_CODES, new Id3FrameBodyFactory() {
                    @Override
                    public AbstractID3v2FrameBody make(String frameId, Buffer buffer, int frameSize)
                            throws FrameIdentifierException, InvalidTagException {
                        return new FrameBodyETCO(buffer, frameSize);
                    }
                })
                .put(ID3v24Frames.FRAME_ID_GENERAL_ENCAPS_OBJECT, new Id3FrameBodyFactory() {
                    @Override
                    public AbstractID3v2FrameBody make(String frameId, Buffer buffer, int frameSize)
                            throws FrameIdentifierException, InvalidTagException {
                        return new FrameBodyGEOB(buffer, frameSize);
                    }
                })
                .put(ID3v24Frames.FRAME_ID_GROUP_ID_REG, new Id3FrameBodyFactory() {
                    @Override
                    public AbstractID3v2FrameBody make(String frameId, Buffer buffer, int frameSize)
                            throws FrameIdentifierException, InvalidTagException {
                        return new FrameBodyGRID(buffer, frameSize);
                    }
                })
                .put(ID3v24Frames.FRAME_ID_ITUNES_GROUPING, new Id3FrameBodyFactory() {
                    @Override
                    public AbstractID3v2FrameBody make(String frameId, Buffer buffer, int frameSize)
                            throws FrameIdentifierException, InvalidTagException {
                        return new FrameBodyGRP1(buffer, frameSize);
                    }
                })
                .put(ID3v23Frames.FRAME_ID_V3_INVOLVED_PEOPLE, new Id3FrameBodyFactory() {
                    @Override
                    public AbstractID3v2FrameBody make(String frameId, Buffer buffer, int frameSize)
                            throws FrameIdentifierException, InvalidTagException {
                        return new FrameBodyIPLS(buffer, frameSize);
                    }
                })
                .put(ID3v24Frames.FRAME_ID_LINKED_INFO, new Id3FrameBodyFactory() {
                    @Override
                    public AbstractID3v2FrameBody make(String frameId, Buffer buffer, int frameSize)
                            throws FrameIdentifierException, InvalidTagException {
                        return new FrameBodyLINK(buffer, frameSize);
                    }
                })
                .put(ID3v24Frames.FRAME_ID_MUSIC_CD_ID, new Id3FrameBodyFactory() {
                    @Override
                    public AbstractID3v2FrameBody make(String frameId, Buffer buffer, int frameSize)
                            throws FrameIdentifierException, InvalidTagException {
                        return new FrameBodyMCDI(buffer, frameSize);
                    }
                })
                .put(ID3v24Frames.FRAME_ID_MPEG_LOCATION_LOOKUP_TABLE, new Id3FrameBodyFactory() {
                    @Override
                    public AbstractID3v2FrameBody make(String frameId, Buffer buffer, int frameSize)
                            throws FrameIdentifierException, InvalidTagException {
                        return new FrameBodyMLLT(buffer, frameSize);
                    }
                })
                .put(ID3v24Frames.FRAME_ID_MOVEMENT_NO, new Id3FrameBodyFactory() {
                    @Override
                    public AbstractID3v2FrameBody make(String frameId, Buffer buffer, int frameSize)
                            throws FrameIdentifierException, InvalidTagException {
                        return new FrameBodyMVIN(buffer, frameSize);
                    }
                })
                .put(ID3v24Frames.FRAME_ID_MOVEMENT, new Id3FrameBodyFactory() {
                    @Override
                    public AbstractID3v2FrameBody make(String frameId, Buffer buffer, int frameSize)
                            throws FrameIdentifierException, InvalidTagException {
                        return new FrameBodyMVNM(buffer, frameSize);
                    }
                })
                .put(ID3v24Frames.FRAME_ID_OWNERSHIP, new Id3FrameBodyFactory() {
                    @Override
                    public AbstractID3v2FrameBody make(String frameId, Buffer buffer, int frameSize)
                            throws FrameIdentifierException, InvalidTagException {
                        return new FrameBodyOWNE(buffer, frameSize);
                    }
                })
                .put(ID3v24Frames.FRAME_ID_PLAY_COUNTER, new Id3FrameBodyFactory() {
                    @Override
                    public AbstractID3v2FrameBody make(String frameId, Buffer buffer, int frameSize)
                            throws FrameIdentifierException, InvalidTagException {
                        return new FrameBodyPCNT(buffer, frameSize);
                    }
                })
                .put(ID3v22Frames.FRAME_ID_V2_ATTACHED_PICTURE, new Id3FrameBodyFactory() {
                    @Override
                    public AbstractID3v2FrameBody make(String frameId, Buffer buffer, int frameSize)
                            throws FrameIdentifierException, InvalidTagException {
                        return new FrameBodyPIC(buffer, frameSize);
                    }
                })
                .put(ID3v24Frames.FRAME_ID_POPULARIMETER, new Id3FrameBodyFactory() {
                    @Override
                    public AbstractID3v2FrameBody make(String frameId, Buffer buffer, int frameSize)
                            throws FrameIdentifierException, InvalidTagException {
                        return new FrameBodyPOPM(buffer, frameSize);
                    }
                })
                .put(ID3v24Frames.FRAME_ID_POSITION_SYNC, new Id3FrameBodyFactory() {
                    @Override
                    public AbstractID3v2FrameBody make(String frameId, Buffer buffer, int frameSize)
                            throws FrameIdentifierException, InvalidTagException {
                        return new FrameBodyPOSS(buffer, frameSize);
                    }
                })
                .put(ID3v24Frames.FRAME_ID_PRIVATE, new Id3FrameBodyFactory() {
                    @Override
                    public AbstractID3v2FrameBody make(String frameId, Buffer buffer, int frameSize)
                            throws FrameIdentifierException, InvalidTagException {
                        return new FrameBodyPRIV(buffer, frameSize);
                    }
                })
                .put(ID3v24Frames.FRAME_ID_RECOMMENDED_BUFFER_SIZE, new Id3FrameBodyFactory() {
                    @Override
                    public AbstractID3v2FrameBody make(String frameId, Buffer buffer, int frameSize)
                            throws FrameIdentifierException, InvalidTagException {
                        return new FrameBodyRBUF(buffer, frameSize);
                    }
                })
                .put(ID3v24Frames.FRAME_ID_RELATIVE_VOLUME_ADJUSTMENT2, new Id3FrameBodyFactory() {
                    @Override
                    public AbstractID3v2FrameBody make(String frameId, Buffer buffer, int frameSize)
                            throws FrameIdentifierException, InvalidTagException {
                        return new FrameBodyRVA2(buffer, frameSize);
                    }
                })
                .put(ID3v23Frames.FRAME_ID_V3_RELATIVE_VOLUME_ADJUSTMENT, new Id3FrameBodyFactory() {
                    @Override
                    public AbstractID3v2FrameBody make(String frameId, Buffer buffer, int frameSize)
                            throws FrameIdentifierException, InvalidTagException {
                        return new FrameBodyRVAD(buffer, frameSize);
                    }
                })
                .put(ID3v24Frames.FRAME_ID_REVERB, new Id3FrameBodyFactory() {
                    @Override
                    public AbstractID3v2FrameBody make(String frameId, Buffer buffer, int frameSize)
                            throws FrameIdentifierException, InvalidTagException {
                        return new FrameBodyRVRB(buffer, frameSize);
                    }
                })
                .put(ID3v24Frames.FRAME_ID_SEEK, new Id3FrameBodyFactory() {
                    @Override
                    public AbstractID3v2FrameBody make(String frameId, Buffer buffer, int frameSize)
                            throws FrameIdentifierException, InvalidTagException {
                        return new FrameBodySEEK(buffer, frameSize);
                    }
                })
                .put(ID3v24Frames.FRAME_ID_SIGNATURE, new Id3FrameBodyFactory() {
                    @Override
                    public AbstractID3v2FrameBody make(String frameId, Buffer buffer, int frameSize)
                            throws FrameIdentifierException, InvalidTagException {
                        return new FrameBodySIGN(buffer, frameSize);
                    }
                })
                .put(ID3v24Frames.FRAME_ID_SYNC_LYRIC, new Id3FrameBodyFactory() {
                    @Override
                    public AbstractID3v2FrameBody make(String frameId, Buffer buffer, int frameSize)
                            throws FrameIdentifierException, InvalidTagException {
                        return new FrameBodySYLT(buffer, frameSize);
                    }
                })
                .put(ID3v24Frames.FRAME_ID_SYNC_TEMPO, new Id3FrameBodyFactory() {
                    @Override
                    public AbstractID3v2FrameBody make(String frameId, Buffer buffer, int frameSize)
                            throws FrameIdentifierException, InvalidTagException {
                        return new FrameBodySYTC(buffer, frameSize);
                    }
                })
                .put(ID3v24Frames.FRAME_ID_BPM, new Id3FrameBodyFactory() {
                    @Override
                    public AbstractID3v2FrameBody make(String frameId, Buffer buffer, int frameSize)
                            throws FrameIdentifierException, InvalidTagException {
                        return new FrameBodyTBPM(buffer, frameSize);
                    }
                })
                .put(ID3v24Frames.FRAME_ID_IS_COMPILATION, new Id3FrameBodyFactory() {
                    @Override
                    public AbstractID3v2FrameBody make(String frameId, Buffer buffer, int frameSize)
                            throws FrameIdentifierException, InvalidTagException {
                        return new FrameBodyTCMP(buffer, frameSize);
                    }
                })
                .put(ID3v24Frames.FRAME_ID_COMPOSER, new Id3FrameBodyFactory() {
                    @Override
                    public AbstractID3v2FrameBody make(String frameId, Buffer buffer, int frameSize)
                            throws FrameIdentifierException, InvalidTagException {
                        return new FrameBodyTCOM(buffer, frameSize);
                    }
                })
                .put(ID3v24Frames.FRAME_ID_GENRE, new Id3FrameBodyFactory() {
                    @Override
                    public AbstractID3v2FrameBody make(String frameId, Buffer buffer, int frameSize)
                            throws FrameIdentifierException, InvalidTagException {
                        return new FrameBodyTCON(buffer, frameSize);
                    }
                })
                .put(ID3v24Frames.FRAME_ID_COPYRIGHTINFO, new Id3FrameBodyFactory() {
                    @Override
                    public AbstractID3v2FrameBody make(String frameId, Buffer buffer, int frameSize)
                            throws FrameIdentifierException, InvalidTagException {
                        return new FrameBodyTCOP(buffer, frameSize);
                    }
                })
                .put(ID3v23Frames.FRAME_ID_V3_TDAT, new Id3FrameBodyFactory() {
                    @Override
                    public AbstractID3v2FrameBody make(String frameId, Buffer buffer, int frameSize)
                            throws FrameIdentifierException, InvalidTagException {
                        return new FrameBodyTDAT(buffer, frameSize);
                    }
                })
                .put(ID3v24Frames.FRAME_ID_ENCODING_TIME, new Id3FrameBodyFactory() {
                    @Override
                    public AbstractID3v2FrameBody make(String frameId, Buffer buffer, int frameSize)
                            throws FrameIdentifierException, InvalidTagException {
                        return new FrameBodyTDEN(buffer, frameSize);
                    }
                })
                .put(ID3v24Frames.FRAME_ID_PLAYLIST_DELAY, new Id3FrameBodyFactory() {
                    @Override
                    public AbstractID3v2FrameBody make(String frameId, Buffer buffer, int frameSize)
                            throws FrameIdentifierException, InvalidTagException {
                        return new FrameBodyTDLY(buffer, frameSize);
                    }
                })
                .put(ID3v24Frames.FRAME_ID_ORIGINAL_RELEASE_TIME, new Id3FrameBodyFactory() {
                    @Override
                    public AbstractID3v2FrameBody make(String frameId, Buffer buffer, int frameSize)
                            throws FrameIdentifierException, InvalidTagException {
                        return new FrameBodyTDOR(buffer, frameSize);
                    }
                })
                .put(ID3v24Frames.FRAME_ID_YEAR, new Id3FrameBodyFactory() {
                    @Override
                    public AbstractID3v2FrameBody make(String frameId, Buffer buffer, int frameSize)
                            throws FrameIdentifierException, InvalidTagException {
                        return new FrameBodyTDRC(buffer, frameSize);
                    }
                })
                .put(ID3v24Frames.FRAME_ID_RELEASE_TIME, new Id3FrameBodyFactory() {
                    @Override
                    public AbstractID3v2FrameBody make(String frameId, Buffer buffer, int frameSize)
                            throws FrameIdentifierException, InvalidTagException {
                        return new FrameBodyTDRL(buffer, frameSize);
                    }
                })
                .put(ID3v24Frames.FRAME_ID_TAGGING_TIME, new Id3FrameBodyFactory() {
                    @Override
                    public AbstractID3v2FrameBody make(String frameId, Buffer buffer, int frameSize)
                            throws FrameIdentifierException, InvalidTagException {
                        return new FrameBodyTDTG(buffer, frameSize);
                    }
                })
                .put(ID3v24Frames.FRAME_ID_ENCODEDBY, new Id3FrameBodyFactory() {
                    @Override
                    public AbstractID3v2FrameBody make(String frameId, Buffer buffer, int frameSize)
                            throws FrameIdentifierException, InvalidTagException {
                        return new FrameBodyTENC(buffer, frameSize);
                    }
                })
                .put(ID3v24Frames.FRAME_ID_LYRICIST, new Id3FrameBodyFactory() {
                    @Override
                    public AbstractID3v2FrameBody make(String frameId, Buffer buffer, int frameSize)
                            throws FrameIdentifierException, InvalidTagException {
                        return new FrameBodyTEXT(buffer, frameSize);
                    }
                })
                .put(ID3v24Frames.FRAME_ID_FILE_TYPE, new Id3FrameBodyFactory() {
                    @Override
                    public AbstractID3v2FrameBody make(String frameId, Buffer buffer, int frameSize)
                            throws FrameIdentifierException, InvalidTagException {
                        return new FrameBodyTFLT(buffer, frameSize);
                    }
                })
                .put(ID3v23Frames.FRAME_ID_V3_TIME, new Id3FrameBodyFactory() {
                    @Override
                    public AbstractID3v2FrameBody make(String frameId, Buffer buffer, int frameSize)
                            throws FrameIdentifierException, InvalidTagException {
                        return new FrameBodyTIME(buffer, frameSize);
                    }
                })
                .put(ID3v24Frames.FRAME_ID_INVOLVED_PEOPLE, new Id3FrameBodyFactory() {
                    @Override
                    public AbstractID3v2FrameBody make(String frameId, Buffer buffer, int frameSize)
                            throws FrameIdentifierException, InvalidTagException {
                        return new FrameBodyTIPL(buffer, frameSize);
                    }
                })
                .put(ID3v24Frames.FRAME_ID_CONTENT_GROUP_DESC, new Id3FrameBodyFactory() {
                    @Override
                    public AbstractID3v2FrameBody make(String frameId, Buffer buffer, int frameSize)
                            throws FrameIdentifierException, InvalidTagException {
                        return new FrameBodyTIT1(buffer, frameSize);
                    }
                })
                .put(ID3v24Frames.FRAME_ID_TITLE, new Id3FrameBodyFactory() {
                    @Override
                    public AbstractID3v2FrameBody make(String frameId, Buffer buffer, int frameSize)
                            throws FrameIdentifierException, InvalidTagException {
                        return new FrameBodyTIT2(buffer, frameSize);
                    }
                })
                .put(ID3v24Frames.FRAME_ID_TITLE_REFINEMENT, new Id3FrameBodyFactory() {
                    @Override
                    public AbstractID3v2FrameBody make(String frameId, Buffer buffer, int frameSize)
                            throws FrameIdentifierException, InvalidTagException {
                        return new FrameBodyTIT3(buffer, frameSize);
                    }
                })
                .put(ID3v24Frames.FRAME_ID_INITIAL_KEY, new Id3FrameBodyFactory() {
                    @Override
                    public AbstractID3v2FrameBody make(String frameId, Buffer buffer, int frameSize)
                            throws FrameIdentifierException, InvalidTagException {
                        return new FrameBodyTKEY(buffer, frameSize);
                    }
                })
                .put(ID3v24Frames.FRAME_ID_LANGUAGE, new Id3FrameBodyFactory() {
                    @Override
                    public AbstractID3v2FrameBody make(String frameId, Buffer buffer, int frameSize)
                            throws FrameIdentifierException, InvalidTagException {
                        return new FrameBodyTLAN(buffer, frameSize);
                    }
                })
                .put(ID3v24Frames.FRAME_ID_LENGTH, new Id3FrameBodyFactory() {
                    @Override
                    public AbstractID3v2FrameBody make(String frameId, Buffer buffer, int frameSize)
                            throws FrameIdentifierException, InvalidTagException {
                        return new FrameBodyTLEN(buffer, frameSize);
                    }
                })
                .put(ID3v24Frames.FRAME_ID_MUSICIAN_CREDITS, new Id3FrameBodyFactory() {
                    @Override
                    public AbstractID3v2FrameBody make(String frameId, Buffer buffer, int frameSize)
                            throws FrameIdentifierException, InvalidTagException {
                        return new FrameBodyTMCL(buffer, frameSize);
                    }
                })
                .put(ID3v24Frames.FRAME_ID_MEDIA_TYPE, new Id3FrameBodyFactory() {
                    @Override
                    public AbstractID3v2FrameBody make(String frameId, Buffer buffer, int frameSize)
                            throws FrameIdentifierException, InvalidTagException {
                        return new FrameBodyTMED(buffer, frameSize);
                    }
                })
                .put(ID3v24Frames.FRAME_ID_MOOD, new Id3FrameBodyFactory() {
                    @Override
                    public AbstractID3v2FrameBody make(String frameId, Buffer buffer, int frameSize)
                            throws FrameIdentifierException, InvalidTagException {
                        return new FrameBodyTMOO(buffer, frameSize);
                    }
                })
                .put(ID3v24Frames.FRAME_ID_ORIG_TITLE, new Id3FrameBodyFactory() {
                    @Override
                    public AbstractID3v2FrameBody make(String frameId, Buffer buffer, int frameSize)
                            throws FrameIdentifierException, InvalidTagException {
                        return new FrameBodyTOAL(buffer, frameSize);
                    }
                })
                .put(ID3v24Frames.FRAME_ID_ORIG_FILENAME, new Id3FrameBodyFactory() {
                    @Override
                    public AbstractID3v2FrameBody make(String frameId, Buffer buffer, int frameSize)
                            throws FrameIdentifierException, InvalidTagException {
                        return new FrameBodyTOFN(buffer, frameSize);
                    }
                })
                .put(ID3v24Frames.FRAME_ID_ORIG_LYRICIST, new Id3FrameBodyFactory() {
                    @Override
                    public AbstractID3v2FrameBody make(String frameId, Buffer buffer, int frameSize)
                            throws FrameIdentifierException, InvalidTagException {
                        return new FrameBodyTOLY(buffer, frameSize);
                    }
                })
                .put(ID3v24Frames.FRAME_ID_ORIGARTIST, new Id3FrameBodyFactory() {
                    @Override
                    public AbstractID3v2FrameBody make(String frameId, Buffer buffer, int frameSize)
                            throws FrameIdentifierException, InvalidTagException {
                        return new FrameBodyTOPE(buffer, frameSize);
                    }
                })
                .put(ID3v23Frames.FRAME_ID_V3_TORY, new Id3FrameBodyFactory() {
                    @Override
                    public AbstractID3v2FrameBody make(String frameId, Buffer buffer, int frameSize)
                            throws FrameIdentifierException, InvalidTagException {
                        return new FrameBodyTORY(buffer, frameSize);
                    }
                })
                .put(ID3v24Frames.FRAME_ID_FILE_OWNER, new Id3FrameBodyFactory() {
                    @Override
                    public AbstractID3v2FrameBody make(String frameId, Buffer buffer, int frameSize)
                            throws FrameIdentifierException, InvalidTagException {
                        return new FrameBodyTOWN(buffer, frameSize);
                    }
                })
                .put(ID3v24Frames.FRAME_ID_ARTIST, new Id3FrameBodyFactory() {
                    @Override
                    public AbstractID3v2FrameBody make(String frameId, Buffer buffer, int frameSize)
                            throws FrameIdentifierException, InvalidTagException {
                        return new FrameBodyTPE1(buffer, frameSize);
                    }
                })
                .put(ID3v24Frames.FRAME_ID_ACCOMPANIMENT, new Id3FrameBodyFactory() {
                    @Override
                    public AbstractID3v2FrameBody make(String frameId, Buffer buffer, int frameSize)
                            throws FrameIdentifierException, InvalidTagException {
                        return new FrameBodyTPE2(buffer, frameSize);
                    }
                })
                .put(ID3v24Frames.FRAME_ID_CONDUCTOR, new Id3FrameBodyFactory() {
                    @Override
                    public AbstractID3v2FrameBody make(String frameId, Buffer buffer, int frameSize)
                            throws FrameIdentifierException, InvalidTagException {
                        return new FrameBodyTPE3(buffer, frameSize);
                    }
                })
                .put(ID3v24Frames.FRAME_ID_REMIXED, new Id3FrameBodyFactory() {
                    @Override
                    public AbstractID3v2FrameBody make(String frameId, Buffer buffer, int frameSize)
                            throws FrameIdentifierException, InvalidTagException {
                        return new FrameBodyTPE4(buffer, frameSize);
                    }
                })
                .put(ID3v24Frames.FRAME_ID_SET, new Id3FrameBodyFactory() {
                    @Override
                    public AbstractID3v2FrameBody make(String frameId, Buffer buffer, int frameSize)
                            throws FrameIdentifierException, InvalidTagException {
                        return new FrameBodyTPOS(buffer, frameSize);
                    }
                })
                .put(ID3v24Frames.FRAME_ID_PRODUCED_NOTICE, new Id3FrameBodyFactory() {
                    @Override
                    public AbstractID3v2FrameBody make(String frameId, Buffer buffer, int frameSize)
                            throws FrameIdentifierException, InvalidTagException {
                        return new FrameBodyTPRO(buffer, frameSize);
                    }
                })
                .put(ID3v24Frames.FRAME_ID_PUBLISHER, new Id3FrameBodyFactory() {
                    @Override
                    public AbstractID3v2FrameBody make(String frameId, Buffer buffer, int frameSize)
                            throws FrameIdentifierException, InvalidTagException {
                        return new FrameBodyTPUB(buffer, frameSize);
                    }
                })
                .put(ID3v24Frames.FRAME_ID_TRACK, new Id3FrameBodyFactory() {
                    @Override
                    public AbstractID3v2FrameBody make(String frameId, Buffer buffer, int frameSize)
                            throws FrameIdentifierException, InvalidTagException {
                        return new FrameBodyTRCK(buffer, frameSize);
                    }
                })
                .put(ID3v23Frames.FRAME_ID_V3_TRDA, new Id3FrameBodyFactory() {
                    @Override
                    public AbstractID3v2FrameBody make(String frameId, Buffer buffer, int frameSize)
                            throws FrameIdentifierException, InvalidTagException {
                        return new FrameBodyTRDA(buffer, frameSize);
                    }
                })
                .put(ID3v24Frames.FRAME_ID_RADIO_NAME, new Id3FrameBodyFactory() {
                    @Override
                    public AbstractID3v2FrameBody make(String frameId, Buffer buffer, int frameSize)
                            throws FrameIdentifierException, InvalidTagException {
                        return new FrameBodyTRSN(buffer, frameSize);
                    }
                })
                .put(ID3v24Frames.FRAME_ID_RADIO_OWNER, new Id3FrameBodyFactory() {
                    @Override
                    public AbstractID3v2FrameBody make(String frameId, Buffer buffer, int frameSize)
                            throws FrameIdentifierException, InvalidTagException {
                        return new FrameBodyTRSO(buffer, frameSize);
                    }
                })
                .put(ID3v23Frames.FRAME_ID_V3_TSIZ, new Id3FrameBodyFactory() {
                    @Override
                    public AbstractID3v2FrameBody make(String frameId, Buffer buffer, int frameSize)
                            throws FrameIdentifierException, InvalidTagException {
                        return new FrameBodyTSIZ(buffer, frameSize);
                    }
                })
                .put(ID3v24Frames.FRAME_ID_ALBUM_ARTIST_SORT_ORDER_ITUNES, new Id3FrameBodyFactory() {
                    @Override
                    public AbstractID3v2FrameBody make(String frameId, Buffer buffer, int frameSize)
                            throws FrameIdentifierException, InvalidTagException {
                        return new FrameBodyTSO2(buffer, frameSize);
                    }
                })
                .put(ID3v24Frames.FRAME_ID_ALBUM_SORT_ORDER, new Id3FrameBodyFactory() {
                    @Override
                    public AbstractID3v2FrameBody make(String frameId, Buffer buffer, int frameSize)
                            throws FrameIdentifierException, InvalidTagException {
                        return new FrameBodyTSOA(buffer, frameSize);
                    }
                })
                .put(ID3v24Frames.FRAME_ID_COMPOSER_SORT_ORDER_ITUNES, new Id3FrameBodyFactory() {
                    @Override
                    public AbstractID3v2FrameBody make(String frameId, Buffer buffer, int frameSize)
                            throws FrameIdentifierException, InvalidTagException {
                        return new FrameBodyTSOC(buffer, frameSize);
                    }
                })
                .put(ID3v24Frames.FRAME_ID_ARTIST_SORT_ORDER, new Id3FrameBodyFactory() {
                    @Override
                    public AbstractID3v2FrameBody make(String frameId, Buffer buffer, int frameSize)
                            throws FrameIdentifierException, InvalidTagException {
                        return new FrameBodyTSOP(buffer, frameSize);
                    }
                })
                .put(ID3v24Frames.FRAME_ID_TITLE_SORT_ORDER, new Id3FrameBodyFactory() {
                    @Override
                    public AbstractID3v2FrameBody make(String frameId, Buffer buffer, int frameSize)
                            throws FrameIdentifierException, InvalidTagException {
                        return new FrameBodyTSOT(buffer, frameSize);
                    }
                })
                .put(ID3v24Frames.FRAME_ID_ISRC, new Id3FrameBodyFactory() {
                    @Override
                    public AbstractID3v2FrameBody make(String frameId, Buffer buffer, int frameSize)
                            throws FrameIdentifierException, InvalidTagException {
                        return new FrameBodyTSRC(buffer, frameSize);
                    }
                })
                .put(ID3v24Frames.FRAME_ID_HW_SW_SETTINGS, new Id3FrameBodyFactory() {
                    @Override
                    public AbstractID3v2FrameBody make(String frameId, Buffer buffer, int frameSize)
                            throws FrameIdentifierException, InvalidTagException {
                        return new FrameBodyTSSE(buffer, frameSize);
                    }
                })
                .put(ID3v24Frames.FRAME_ID_SET_SUBTITLE, new Id3FrameBodyFactory() {
                    @Override
                    public AbstractID3v2FrameBody make(String frameId, Buffer buffer, int frameSize)
                            throws FrameIdentifierException, InvalidTagException {
                        return new FrameBodyTSST(buffer, frameSize);
                    }
                })
                .put(ID3v24Frames.FRAME_ID_USER_DEFINED_INFO, new Id3FrameBodyFactory() {
                    @Override
                    public AbstractID3v2FrameBody make(String frameId, Buffer buffer, int frameSize)
                            throws FrameIdentifierException, InvalidTagException {
                        return new FrameBodyTXXX(buffer, frameSize);
                    }
                })
                .put(ID3v23Frames.FRAME_ID_V3_TYER, new Id3FrameBodyFactory() {
                    @Override
                    public AbstractID3v2FrameBody make(String frameId, Buffer buffer, int frameSize)
                            throws FrameIdentifierException, InvalidTagException {
                        return new FrameBodyTYER(buffer, frameSize);
                    }
                })
                .put(ID3v24Frames.FRAME_ID_UNIQUE_FILE_ID, new Id3FrameBodyFactory() {
                    @Override
                    public AbstractID3v2FrameBody make(String frameId, Buffer buffer, int frameSize)
                            throws FrameIdentifierException, InvalidTagException {
                        return new FrameBodyUFID(buffer, frameSize);
                    }
                })
                .put(ID3v24Frames.FRAME_ID_TERMS_OF_USE, new Id3FrameBodyFactory() {
                    @Override
                    public AbstractID3v2FrameBody make(String frameId, Buffer buffer, int frameSize)
                            throws FrameIdentifierException, InvalidTagException {
                        return new FrameBodyUSER(buffer, frameSize);
                    }
                })
                .put(ID3v24Frames.FRAME_ID_UNSYNC_LYRICS, new Id3FrameBodyFactory() {
                    @Override
                    public AbstractID3v2FrameBody make(String frameId, Buffer buffer, int frameSize)
                            throws FrameIdentifierException, InvalidTagException {
                        return new FrameBodyUSLT(buffer, frameSize);
                    }
                })
                .put(ID3v24Frames.FRAME_ID_URL_COMMERCIAL, new Id3FrameBodyFactory() {
                    @Override
                    public AbstractID3v2FrameBody make(String frameId, Buffer buffer, int frameSize)
                            throws FrameIdentifierException, InvalidTagException {
                        return new FrameBodyWCOM(buffer, frameSize);
                    }
                })
                .put(ID3v24Frames.FRAME_ID_URL_COPYRIGHT, new Id3FrameBodyFactory() {
                    @Override
                    public AbstractID3v2FrameBody make(String frameId, Buffer buffer, int frameSize)
                            throws FrameIdentifierException, InvalidTagException {
                        return new FrameBodyWCOP(buffer, frameSize);
                    }
                })
                .put(ID3v24Frames.FRAME_ID_URL_FILE_WEB, new Id3FrameBodyFactory() {
                    @Override
                    public AbstractID3v2FrameBody make(String frameId, Buffer buffer, int frameSize)
                            throws FrameIdentifierException, InvalidTagException {
                        return new FrameBodyWOAF(buffer, frameSize);
                    }
                })
                .put(ID3v24Frames.FRAME_ID_URL_ARTIST_WEB, new Id3FrameBodyFactory() {
                    @Override
                    public AbstractID3v2FrameBody make(String frameId, Buffer buffer, int frameSize)
                            throws FrameIdentifierException, InvalidTagException {
                        return new FrameBodyWOAR(buffer, frameSize);
                    }
                })
                .put(ID3v24Frames.FRAME_ID_URL_SOURCE_WEB, new Id3FrameBodyFactory() {
                    @Override
                    public AbstractID3v2FrameBody make(String frameId, Buffer buffer, int frameSize)
                            throws FrameIdentifierException, InvalidTagException {
                        return new FrameBodyWOAS(buffer, frameSize);
                    }
                })
                .put(ID3v24Frames.FRAME_ID_URL_OFFICIAL_RADIO, new Id3FrameBodyFactory() {
                    @Override
                    public AbstractID3v2FrameBody make(String frameId, Buffer buffer, int frameSize)
                            throws FrameIdentifierException, InvalidTagException {
                        return new FrameBodyWORS(buffer, frameSize);
                    }
                })
                .put(ID3v24Frames.FRAME_ID_URL_PAYMENT, new Id3FrameBodyFactory() {
                    @Override
                    public AbstractID3v2FrameBody make(String frameId, Buffer buffer, int frameSize)
                            throws FrameIdentifierException, InvalidTagException {
                        return new FrameBodyWPAY(buffer, frameSize);
                    }
                })
                .put(ID3v24Frames.FRAME_ID_URL_PUBLISHERS, new Id3FrameBodyFactory() {
                    @Override
                    public AbstractID3v2FrameBody make(String frameId, Buffer buffer, int frameSize)
                            throws FrameIdentifierException, InvalidTagException {
                        return new FrameBodyWPUB(buffer, frameSize);
                    }
                })
                .put(ID3v24Frames.FRAME_ID_USER_DEFINED_URL, new Id3FrameBodyFactory() {
                    @Override
                    public AbstractID3v2FrameBody make(String frameId, Buffer buffer, int frameSize)
                            throws FrameIdentifierException, InvalidTagException {
                        return new FrameBodyWXXX(buffer, frameSize);
                    }
                })
                .put(ID3v23Frames.FRAME_ID_V3_ALBUM_SORT_ORDER_MUSICBRAINZ, new Id3FrameBodyFactory() {
                    @Override
                    public AbstractID3v2FrameBody make(String frameId, Buffer buffer, int frameSize)
                            throws FrameIdentifierException, InvalidTagException {
                        return new FrameBodyXSOA(buffer, frameSize);
                    }
                })
                .put(ID3v23Frames.FRAME_ID_V3_ARTIST_SORT_ORDER_MUSICBRAINZ, new Id3FrameBodyFactory() {
                    @Override
                    public AbstractID3v2FrameBody make(String frameId, Buffer buffer, int frameSize)
                            throws FrameIdentifierException, InvalidTagException {
                        return new FrameBodyXSOP(buffer, frameSize);
                    }
                })
                .put(ID3v23Frames.FRAME_ID_V3_TITLE_SORT_ORDER_MUSICBRAINZ, new Id3FrameBodyFactory() {
                    @Override
                    public AbstractID3v2FrameBody make(String frameId, Buffer buffer, int frameSize)
                            throws FrameIdentifierException, InvalidTagException {
                        return new FrameBodyXSOT(buffer, frameSize);
                    }
                })


                .put(ID3v24Frames.FRAME_ID_ALBUM, new Id3FrameBodyFactory() {
                    @Override
                    public AbstractID3v2FrameBody make(String frameId,
                                                       Buffer buffer,
                                                       int frameSize) throws InvalidTagException {
                        return new FrameBodyTALB(buffer, frameSize);
                    }
                })
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
