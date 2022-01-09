/*
 *  MusicTag Copyright (C)2003,2004
 *
 *  This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser
 *  General Public  License as published by the Free Software Foundation; either version 2.1 of the License,
 *  or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 *  the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License along with this library; if not,
 *  you can get a copy from http://www.opensource.org/licenses/lgpl-license.php or write to the Free Software
 *  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 */
package ealvatag.tag.id3;

import static ealvatag.logging.EalvaTagLog.LogLevel.DEBUG;
import static ealvatag.logging.EalvaTagLog.LogLevel.ERROR;
import static ealvatag.logging.EalvaTagLog.LogLevel.TRACE;
import static ealvatag.logging.EalvaTagLog.LogLevel.WARN;
import static ealvatag.logging.ErrorMessage.ID3_EXTENDED_HEADER_SIZE_TOO_SMALL;
import static ealvatag.logging.ErrorMessage.ID3_INVALID_OR_UNKNOWN_FLAG_SET;
import static ealvatag.utils.Check.CANNOT_BE_NULL;
import static ealvatag.utils.Check.checkArgNotNull;
import static ealvatag.utils.Check.checkArgNotNullOrEmpty;
import static ealvatag.utils.Check.checkVarArg0NotNull;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

import ealvatag.audio.mp3.MP3File;
import ealvatag.logging.EalvaTagLog;
import ealvatag.logging.EalvaTagLog.JLogger;
import ealvatag.logging.EalvaTagLog.JLoggers;
import ealvatag.logging.ErrorMessage;
import ealvatag.tag.EmptyFrameException;
import ealvatag.tag.FieldDataInvalidException;
import ealvatag.tag.FieldKey;
import ealvatag.tag.InvalidDataTypeException;
import ealvatag.tag.InvalidFrameException;
import ealvatag.tag.InvalidFrameIdentifierException;
import ealvatag.tag.InvalidTagException;
import ealvatag.tag.Key;
import ealvatag.tag.PaddingException;
import ealvatag.tag.Tag;
import ealvatag.tag.TagException;
import ealvatag.tag.TagField;
import ealvatag.tag.TagNotFoundException;
import ealvatag.tag.TagOptionSingleton;
import ealvatag.tag.UnsupportedFieldException;
import ealvatag.tag.datatype.DataTypes;
import ealvatag.tag.datatype.Pair;
import ealvatag.tag.id3.framebody.AbstractID3v2FrameBody;
import ealvatag.tag.id3.framebody.FrameBodyCOMM;
import ealvatag.tag.id3.framebody.FrameBodyIPLS;
import ealvatag.tag.id3.framebody.FrameBodyTALB;
import ealvatag.tag.id3.framebody.FrameBodyTCON;
import ealvatag.tag.id3.framebody.FrameBodyTDRC;
import ealvatag.tag.id3.framebody.FrameBodyTIPL;
import ealvatag.tag.id3.framebody.FrameBodyTIT2;
import ealvatag.tag.id3.framebody.FrameBodyTMCL;
import ealvatag.tag.id3.framebody.FrameBodyTPE1;
import ealvatag.tag.id3.framebody.FrameBodyTRCK;
import ealvatag.tag.id3.framebody.FrameBodyUnsupported;
import ealvatag.tag.id3.valuepair.MusicianCredits;
import ealvatag.tag.id3.valuepair.StandardIPLSKey;
import ealvatag.tag.lyrics3.AbstractLyrics3;
import ealvatag.tag.lyrics3.Lyrics3v2;
import ealvatag.tag.lyrics3.Lyrics3v2Field;
import ealvatag.tag.reference.GenreTypes;
import ealvatag.utils.Check;
import okio.Buffer;

/**
 * Represents an ID3v2.4 tag.
 *
 * @author : Paul Taylor
 * @author : Eric Farng
 * @version $Id$
 */
public class ID3v24Tag extends AbstractID3v2Tag {
    public static final byte MAJOR_VERSION = 4;
    private static final JLogger LOG = JLoggers.get(ID3v24Tag.class, EalvaTagLog.MARKER);
    private static final String TYPE_FOOTER = "footer";
    private static final String TYPE_IMAGEENCODINGRESTRICTION = "imageEncodingRestriction";
    private static final String TYPE_IMAGESIZERESTRICTION = "imageSizeRestriction";
    private static final String TYPE_TAGRESTRICTION = "tagRestriction";
    private static final String TYPE_TAGSIZERESTRICTION = "tagSizeRestriction";
    private static final String TYPE_TEXTENCODINGRESTRICTION = "textEncodingRestriction";
    private static final String TYPE_TEXTFIELDSIZERESTRICTION = "textFieldSizeRestriction";
    private static final String TYPE_UPDATETAG = "updateTag";
    private static final String TYPE_CRCDATA = "crcdata";
    private static final String TYPE_EXPERIMENTAL = "experimental";
    private static final String TYPE_EXTENDED = "extended";
    private static final String TYPE_PADDINGSIZE = "paddingsize";
    private static final String TYPE_UNSYNCHRONISATION = "unsyncronisation";
    private static final int TAG_EXT_HEADER_LENGTH = 6;
    private static final int TAG_EXT_HEADER_UPDATE_LENGTH = 1;
    private static final int TAG_EXT_HEADER_CRC_LENGTH = 6;
    private static final int TAG_EXT_HEADER_RESTRICTION_LENGTH = 2;
    private static final int TAG_EXT_HEADER_CRC_DATA_LENGTH = 5;
    @SuppressWarnings("FieldCanBeLocal")
    private static final int TAG_EXT_HEADER_RESTRICTION_DATA_LENGTH = 1;
    @SuppressWarnings("FieldCanBeLocal")
    private static final int TAG_EXT_NUMBER_BYTES_DATA_LENGTH = 1;
    /**
     * ID3v2.4 Header bit mask
     */
    private static final int MASK_V24_UNSYNCHRONIZATION = FileConstants.BIT7;
    /**
     * ID3v2.4 Header bit mask
     */
    private static final int MASK_V24_EXTENDED_HEADER = FileConstants.BIT6;
    /**
     * ID3v2.4 Header bit mask
     */
    private static final int MASK_V24_EXPERIMENTAL = FileConstants.BIT5;
    /**
     * ID3v2.4 Header bit mask
     */
    private static final int MASK_V24_FOOTER_PRESENT = FileConstants.BIT4;
    /**
     * ID3v2.4 Extended header bit mask
     */
    private static final int MASK_V24_TAG_UPDATE = FileConstants.BIT6;
    /**
     * ID3v2.4 Extended header bit mask
     */
    private static final int MASK_V24_CRC_DATA_PRESENT = FileConstants.BIT5;
    /**
     * ID3v2.4 Extended header bit mask
     */
    private static final int MASK_V24_TAG_RESTRICTIONS = FileConstants.BIT4;
    /**
     * ID3v2.4 Extended header bit mask
     */
    private static final int MASK_V24_TAG_SIZE_RESTRICTIONS = (byte) FileConstants.BIT7 | FileConstants.BIT6;
    /**
     * ID3v2.4 Extended header bit mask
     */
    private static final int MASK_V24_TEXT_ENCODING_RESTRICTIONS = FileConstants.BIT5;
    /**
     * ID3v2.4 Extended header bit mask
     */
    private static final int MASK_V24_TEXT_FIELD_SIZE_RESTRICTIONS = FileConstants.BIT4 | FileConstants.BIT3;
    /**
     * ID3v2.4 Extended header bit mask
     */
    private static final int MASK_V24_IMAGE_ENCODING = FileConstants.BIT2;

    /*
     * TODO: 2/23/17
     * ID3v2.4 Header Footer are the same as the header flags. WHY?!?! move the
     * flags from their position in 2.3??????????
     */

//  /**
//   * ID3v2.4 Header Footer bit mask
//   */
//  public static final int MASK_V24_TAG_ALTER_PRESERVATION = FileConstants.BIT6;
//
//  /**
//   * ID3v2.4 Header Footer bit mask
//   */
//  public static final int MASK_V24_FILE_ALTER_PRESERVATION = FileConstants.BIT5;
//
//  /**
//   * ID3v2.4 Header Footer bit mask
//   */
//  public static final int MASK_V24_READ_ONLY = FileConstants.BIT4;
//
//  /**
//   * ID3v2.4 Header Footer bit mask
//   */
//  public static final int MASK_V24_GROUPING_IDENTITY = FileConstants.BIT6;
//
//  /**
//   * ID3v2.4 Header Footer bit mask
//   */
//  public static final int MASK_V24_COMPRESSION = FileConstants.BIT4;
//
//  /**
//   * ID3v2.4 Header Footer bit mask
//   */
//  public static final int MASK_V24_ENCRYPTION = FileConstants.BIT3;
//
//  /**
//   * ID3v2.4 Header Footer bit mask
//   */
//  public static final int MASK_V24_FRAME_UNSYNCHRONIZATION = FileConstants.BIT2;
//
//  /**
//   * ID3v2.4 Header Footer bit mask
//   */
//  public static final int MASK_V24_DATA_LENGTH_INDICATOR = FileConstants.BIT1;
    /**
     * ID3v2.4 Extended header bit mask
     */
    private static final int MASK_V24_IMAGE_SIZE_RESTRICTIONS = FileConstants.BIT2 | FileConstants.BIT1;
    private static final byte RELEASE = 2;
    private static final byte REVISION = 0;
    /**
     * Tag padding
     */
    @SuppressWarnings("FieldCanBeLocal")
    private final int paddingSize = 0;
    /**
     * Contains extended header
     */
    protected boolean extended;
    /**
     * All frames in the tag uses unsynchronisation
     */
    boolean unsynchronization;
    /**
     * CRC Checksum calculated
     */
    private boolean crcDataFlag;
    /**
     * Experiemntal tag
     */
    private boolean experimental;
    /**
     * CRC Checksum
     */
    private int crcData;
    /**
     * Contains a footer
     */
    private boolean footer;
    /**
     * Tag is an update
     */
    private boolean updateTag;
    /**
     * Tag has restrictions
     */
    private boolean tagRestriction;
    /**
     * If Set Image encoding restrictions
     * <p>
     * 0   No restrictions
     * 1   Images are encoded only with PNG [PNG] or JPEG [JFIF].
     */
    private byte imageEncodingRestriction;
    /**
     * If set Image size restrictions
     * <p>
     * 00  No restrictions
     * 01  All images are 256x256 pixels or smaller.
     * 10  All images are 64x64 pixels or smaller.
     * 11  All images are exactly 64x64 pixels, unless required
     * otherwise.
     */
    private byte imageSizeRestriction;
    /**
     * If set then Tag Size Restrictions
     * <p>
     * 00   No more than 128 frames and 1 MB total tag size.
     * 01   No more than 64 frames and 128 KB total tag size.
     * 10   No more than 32 frames and 40 KB total tag size.
     * 11   No more than 32 frames and 4 KB total tag size.
     */
    private byte tagSizeRestriction;
    /**
     * If set Text encoding restrictions
     * <p>
     * 0    No restrictions
     * 1    Strings are only encoded with ISO-8859-1 [ISO-8859-1] or
     * UTF-8 [UTF-8].
     */
    private byte textEncodingRestriction;
    /**
     * If set Text fields size restrictions
     * <p>
     * 00   No restrictions
     * 01   No string is longer than 1024 characters.
     * 10   No string is longer than 128 characters.
     * 11   No string is longer than 30 characters.
     * <p>
     * Note that nothing is said about how many bytes is used to
     * represent those characters, since it is encoding dependent. If a
     * text frame consists of more than one string, the sum of the
     * strings is restricted as stated.
     */
    private byte textFieldSizeRestriction;

    /**
     * Creates a new empty ID3v2_4 data type.
     */
    public ID3v24Tag() {
        ensureFrameMapsAndClear();
    }

    /**
     * Copy Constructor, creates a new ID3v2_4 Tag based on another ID3v2_4 Tag
     */
    public ID3v24Tag(ID3v24Tag copyObject) {
        LOG.log(DEBUG, "Creating tag from another tag of same type");
        copyPrimitives(copyObject);
        copyFrames(copyObject);
    }

    /**
     * Creates a new ID3v2_4 datatype based on another (non 2.4) tag
     */
    public ID3v24Tag(BaseID3Tag mp3tag) {
        LOG.log(DEBUG, "Creating tag from a tag of a different version");
        ensureFrameMapsAndClear();

        if (mp3tag != null) {
            //Should use simpler copy constructor
            if ((mp3tag instanceof ID3v24Tag)) {
                throw new UnsupportedOperationException("Copy Constructor not called. Please type cast the argument");
            }
            /* If we get a tag, we want to convert to id3v2_4
             * both id3v1 and lyrics3 convert to this type
             * id3v1 needs to convert to id3v2_4 before converting to lyrics3
             */
            else if (mp3tag instanceof AbstractID3v2Tag) {
                setLoggingFilename(((AbstractID3v2Tag) mp3tag).loggingFilename);
                copyPrimitives((AbstractID3v2Tag) mp3tag);
                copyFrames((AbstractID3v2Tag) mp3tag);
            }
            //IDv1
            else if (mp3tag instanceof ID3v1Tag) {
                // convert id3v1 tags.
                ID3v1Tag id3tag = (ID3v1Tag) mp3tag;
                ID3v24Frame newFrame;
                AbstractID3v2FrameBody newBody;
                if (id3tag.title.length() > 0) {
                    newBody = new FrameBodyTIT2((byte) 0, id3tag.title);
                    newFrame = new ID3v24Frame(ID3v24Frames.FRAME_ID_TITLE);
                    newFrame.setBody(newBody);
                    frameMap.put(newFrame.getIdentifier(), newFrame);
                }
                if (id3tag.artist.length() > 0) {
                    newBody = new FrameBodyTPE1((byte) 0, id3tag.artist);
                    newFrame = new ID3v24Frame(ID3v24Frames.FRAME_ID_ARTIST);
                    newFrame.setBody(newBody);
                    frameMap.put(newFrame.getIdentifier(), newFrame);
                }
                if (id3tag.album.length() > 0) {
                    newBody = new FrameBodyTALB((byte) 0, id3tag.album);
                    newFrame = new ID3v24Frame(ID3v24Frames.FRAME_ID_ALBUM);
                    newFrame.setBody(newBody);
                    frameMap.put(newFrame.getIdentifier(), newFrame);
                }
                if (id3tag.year.length() > 0) {
                    newBody = new FrameBodyTDRC((byte) 0, id3tag.year);
                    newFrame = new ID3v24Frame(ID3v24Frames.FRAME_ID_YEAR);
                    newFrame.setBody(newBody);
                    frameMap.put(newFrame.getIdentifier(), newFrame);
                }
                if (id3tag.comment.length() > 0) {
                    newBody = new FrameBodyCOMM((byte) 0, "ENG", "", id3tag.comment);
                    newFrame = new ID3v24Frame(ID3v24Frames.FRAME_ID_COMMENT);
                    newFrame.setBody(newBody);
                    frameMap.put(newFrame.getIdentifier(), newFrame);
                }
                if (((id3tag.genre & ID3v1Tag.BYTE_TO_UNSIGNED) >= 0) &&
                        ((id3tag.genre & ID3v1Tag.BYTE_TO_UNSIGNED) != ID3v1Tag.BYTE_TO_UNSIGNED)) {
                    Integer genreId = id3tag.genre & ID3v1Tag.BYTE_TO_UNSIGNED;
                    String genre = "(" + genreId + ") " + GenreTypes.getInstanceOf().getValue(genreId);

                    newBody = new FrameBodyTCON((byte) 0, genre);
                    newFrame = new ID3v24Frame(ID3v24Frames.FRAME_ID_GENRE);
                    newFrame.setBody(newBody);
                    frameMap.put(newFrame.getIdentifier(), newFrame);
                }
                if (mp3tag instanceof ID3v11Tag) {
                    ID3v11Tag id3tag2 = (ID3v11Tag) mp3tag;
                    if (id3tag2.track > 0) {
                        newBody = new FrameBodyTRCK((byte) 0, Byte.toString(id3tag2.track));
                        newFrame = new ID3v24Frame(ID3v24Frames.FRAME_ID_TRACK);
                        newFrame.setBody(newBody);
                        frameMap.put(newFrame.getIdentifier(), newFrame);
                    }
                }
            }
            //Lyrics 3
            else if (mp3tag instanceof AbstractLyrics3) {
                //Put the conversion stuff in the individual frame code.
                Lyrics3v2 lyric;
                if (mp3tag instanceof Lyrics3v2) {
                    lyric = new Lyrics3v2((Lyrics3v2) mp3tag);
                } else {
                    lyric = new Lyrics3v2(mp3tag);
                }
                Iterator<Lyrics3v2Field> iterator = lyric.iterator();
                Lyrics3v2Field field;
                ID3v24Frame newFrame;
                while (iterator.hasNext()) {
                    try {
                        field = iterator.next();
                        newFrame = new ID3v24Frame(field);
                        frameMap.put(newFrame.getIdentifier(), newFrame);
                    } catch (InvalidTagException ex) {
                        LOG.log(WARN, "Unable to convert Lyrics3 to v24 Frame:Frame Identifier");
                    }
                }
            }
        }
    }


    /**
     * Creates a new ID3v2_4 datatype.
     */
    public ID3v24Tag(ByteBuffer buffer, String loggingFilename) throws TagException {
        ensureFrameMapsAndClear();
        setLoggingFilename(loggingFilename);
        read(buffer);
    }

    public ID3v24Tag(Buffer buffer,
                     Id3v2Header header,
                     String loggingFilename,
                     boolean ignoreArtwork) throws TagException {
        ensureFrameMapsAndClear();
        setLoggingFilename(loggingFilename);
        read(buffer, header, ignoreArtwork);
    }

    /**
     * Retrieve the Release
     */
    public byte getRelease() {
        return RELEASE;
    }

    /**
     * Retrieve the Major Version
     */
    public byte getMajorVersion() {
        return MAJOR_VERSION;
    }

    /**
     * Retrieve the Revision
     */
    public byte getRevision() {
        return REVISION;
    }

    /**
     * Copy primitives applicable to v2.4, this is used when cloning a v2.4 datatype
     * and other objects such as v2.3 so need to check instanceof
     */
    protected void copyPrimitives(AbstractID3v2Tag copyObj) {
        LOG.log(DEBUG, "Copying primitives");
        super.copyPrimitives(copyObj);

        if (copyObj instanceof ID3v24Tag) {
            ID3v24Tag copyObject = (ID3v24Tag) copyObj;
            footer = copyObject.footer;
            tagRestriction = copyObject.tagRestriction;
            updateTag = copyObject.updateTag;
            imageEncodingRestriction = copyObject.imageEncodingRestriction;
            imageSizeRestriction = copyObject.imageSizeRestriction;
            tagSizeRestriction = copyObject.tagSizeRestriction;
            textEncodingRestriction = copyObject.textEncodingRestriction;
            textFieldSizeRestriction = copyObject.textFieldSizeRestriction;
        }
    }

    /**
     * Copy the frame
     * <p>
     * If the frame is already an ID3v24 frame we can add as is, if not we need to convert
     * to id3v24 frame(s)
     */
    @Override
    public void addFrame(AbstractID3v2Frame frame) {
        try {
            if (frame instanceof ID3v24Frame) {
                copyFrameIntoMap(frame.getIdentifier(), frame);
            } else {
                List<AbstractID3v2Frame> frames = convertFrame(frame);
                for (AbstractID3v2Frame next : frames) {
                    copyFrameIntoMap(next.getIdentifier(), next);
                }
            }
        } catch (InvalidFrameException ife) {
            LOG.log(ERROR, "Unable to convert frame:%s", frame.getIdentifier());
        }
    }

    /**
     * Convert frame into ID3v24 frame(s)
     */
    @Override
    protected List<AbstractID3v2Frame> convertFrame(AbstractID3v2Frame frame) throws InvalidFrameException {
        List<AbstractID3v2Frame> frames = new ArrayList<>();
        if (frame instanceof ID3v22Frame && frame.getIdentifier().equals(ID3v22Frames.FRAME_ID_V2_IPLS)) {
            frame = new ID3v23Frame(frame);
        }

        //This frame may need splitting and converting into two frames depending on its content
        if (frame instanceof ID3v23Frame && frame.getIdentifier().equals(ID3v23Frames.FRAME_ID_V3_INVOLVED_PEOPLE)) {
            List<Pair> pairs = ((FrameBodyIPLS) frame.getBody()).getPairing().getMapping();
            List<Pair> pairsTipl = new ArrayList<>();
            List<Pair> pairsTmcl = new ArrayList<>();

            for (Pair next : pairs) {
                if (StandardIPLSKey.isKey(next.getKey())) {
                    pairsTipl.add(next);
                } else if (MusicianCredits.isKey(next.getKey())) {
                    pairsTmcl.add(next);
                } else {
                    pairsTipl.add(next);
                }
            }
            AbstractID3v2Frame tipl = new ID3v24Frame((ID3v23Frame) frame, ID3v24Frames.FRAME_ID_INVOLVED_PEOPLE);
            FrameBodyTIPL tiplBody = new FrameBodyTIPL(frame.getBody().getTextEncoding(), pairsTipl);
            tipl.setBody(tiplBody);
            frames.add(tipl);

            AbstractID3v2Frame tmcl = new ID3v24Frame((ID3v23Frame) frame, ID3v24Frames.FRAME_ID_MUSICIAN_CREDITS);
            FrameBodyTMCL tmclBody = new FrameBodyTMCL(frame.getBody().getTextEncoding(), pairsTmcl);
            tmcl.setBody(tmclBody);
            frames.add(tmcl);
        } else {
            frames.add(new ID3v24Frame(frame));
        }
        return frames;
    }

    /**
     * Two different frames both converted to TDRCFrames, now if this is the case one of them
     * may have actually have been created as a FrameUnsupportedBody because TDRC is only
     * supported in ID3v24, but is often created in v23 tags as well together with the valid TYER
     * frame OR it might be that we have two v23 frames that map to TDRC such as TYER,TIME or TDAT
     */
    @Override
    protected void processDuplicateFrame(AbstractID3v2Frame newFrame, AbstractID3v2Frame existingFrame) {
        //We dont add this new frame we just add the contents to existing frame
        //
        if (newFrame.getBody() instanceof FrameBodyTDRC) {
            if (existingFrame.getBody() instanceof FrameBodyTDRC) {
                FrameBodyTDRC body = (FrameBodyTDRC) existingFrame.getBody();
                FrameBodyTDRC newBody = (FrameBodyTDRC) newFrame.getBody();

                //#304:Check for NullPointer, just ignore this frame
                if (newBody.getOriginalID() == null) {
                    return;
                }
                //Just add the data to the frame
                switch (newBody.getOriginalID()) {
                    case ID3v23Frames.FRAME_ID_V3_TYER:
                        body.setYear(newBody.getYear());
                        break;
                    case ID3v23Frames.FRAME_ID_V3_TDAT:
                        body.setDate(newBody.getDate());
                        body.setMonthOnly(newBody.isMonthOnly());
                        break;
                    case ID3v23Frames.FRAME_ID_V3_TIME:
                        body.setTime(newBody.getTime());
                        body.setHoursOnly(newBody.isHoursOnly());
                        break;
                }
                body.setObjectValue(DataTypes.OBJ_TEXT, body.getFormattedText());
            }
            // The first frame was a TDRC frame that was not really allowed, this new frame was probably a
            // valid frame such as TYER which has been converted to TDRC, replace the firstframe with this frame
            else if (existingFrame.getBody() instanceof FrameBodyUnsupported) {
                frameMap.put(newFrame.getIdentifier(), newFrame);
            } else {
                //we just lose this frame, we have already got one with the correct id.
                LOG.log(WARN, "Found duplicate TDRC frame in invalid situation,discarding:%s", newFrame.getIdentifier());
            }
        } else {
            List<AbstractID3v2Frame> list = new ArrayList<>();
            list.add(existingFrame);
            list.add(newFrame);
            frameMap.put(newFrame.getIdentifier(), list);
        }
    }

    /**
     * @return identifier
     */
    public String getIdentifier() {
        return "ID3v2.40";
    }

    /**
     * Return tag size based upon the sizes of the frames rather than the physical
     * no of bytes between start of ID3Tag and start of Audio Data.
     *
     * @return size
     */
    public int getSize() {
        int size = TAG_HEADER_LENGTH;
        if (extended) {
            size += TAG_EXT_HEADER_LENGTH;
            if (updateTag) {
                size += TAG_EXT_HEADER_UPDATE_LENGTH;
            }
            if (crcDataFlag) {
                size += TAG_EXT_HEADER_CRC_LENGTH;
            }
            if (tagRestriction) {
                size += TAG_EXT_HEADER_RESTRICTION_LENGTH;
            }
        }
        size += super.getSize();
        LOG.log(DEBUG, "Tag Size is %s", size);
        return size;
    }

    /**
     * @return equality
     */
    public boolean equals(Object obj) {
        if (!(obj instanceof ID3v24Tag)) {
            return false;
        }
        ID3v24Tag object = (ID3v24Tag) obj;
        return footer == object.footer &&
                imageEncodingRestriction == object.imageEncodingRestriction &&
                imageSizeRestriction == object.imageSizeRestriction &&
                tagRestriction == object.tagRestriction &&
                tagSizeRestriction == object.tagSizeRestriction &&
                textEncodingRestriction == object.textEncodingRestriction &&
                textFieldSizeRestriction == object.textFieldSizeRestriction &&
                updateTag == object.updateTag &&
                super.equals(obj);
    }

    private void readHeaderFlags(byte flags) throws TagException {
        unsynchronization = (flags & MASK_V24_UNSYNCHRONIZATION) != 0;
        extended = (flags & MASK_V24_EXTENDED_HEADER) != 0;
        experimental = (flags & MASK_V24_EXPERIMENTAL) != 0;
        footer = (flags & MASK_V24_FOOTER_PRESENT) != 0;

        //Not allowable/Unknown Flags
        if ((flags & FileConstants.BIT3) != 0) {
            LOG.log(WARN, ID3_INVALID_OR_UNKNOWN_FLAG_SET, loggingFilename, FileConstants.BIT3);
        }

        if ((flags & FileConstants.BIT2) != 0) {
            LOG.log(WARN, ID3_INVALID_OR_UNKNOWN_FLAG_SET, loggingFilename, FileConstants.BIT2);
        }

        if ((flags & FileConstants.BIT1) != 0) {
            LOG.log(WARN, ID3_INVALID_OR_UNKNOWN_FLAG_SET, loggingFilename, FileConstants.BIT1);
        }

        if ((flags & FileConstants.BIT0) != 0) {
            LOG.log(WARN, ID3_INVALID_OR_UNKNOWN_FLAG_SET, loggingFilename, FileConstants.BIT0);
        }


        if (isUnsynchronization()) {
            LOG.log(DEBUG, ErrorMessage.ID3_TAG_UNSYNCHRONIZED, loggingFilename);
        }

        if (extended) {
            LOG.log(DEBUG, ErrorMessage.ID3_TAG_EXTENDED, loggingFilename);
        }

        if (experimental) {
            LOG.log(DEBUG, ErrorMessage.ID3_TAG_EXPERIMENTAL, loggingFilename);
        }

        if (footer) {
            LOG.log(WARN, ErrorMessage.ID3_TAG_FOOTER, loggingFilename);
        }
    }

    /**
     * Read the optional extended header
     */
    private void readExtendedHeader(ByteBuffer byteBuffer) throws InvalidTagException {
        byte[] buffer;

        // int is 4 bytes.
        int extendedHeaderSize = byteBuffer.getInt();

        // the extended header must be at least 6 bytes
        if (extendedHeaderSize <= TAG_EXT_HEADER_LENGTH) {
            throw new InvalidTagException(String.format(Locale.getDefault(),
                    ID3_EXTENDED_HEADER_SIZE_TOO_SMALL,
                    loggingFilename,
                    extendedHeaderSize));
        }

        //Number of bytes
        byteBuffer.get();

        // Read the extended flag bytes
        byte extFlag = byteBuffer.get();
        updateTag = (extFlag & MASK_V24_TAG_UPDATE) != 0;
        crcDataFlag = (extFlag & MASK_V24_CRC_DATA_PRESENT) != 0;
        tagRestriction = (extFlag & MASK_V24_TAG_RESTRICTIONS) != 0;

        // read the length byte if the flag is set
        // this tag should always be zero but just in case
        // read this information.
        if (updateTag) {
            byteBuffer.get();
        }

        //CRC-32
        if (crcDataFlag) {
            // the CRC has a variable length
            byteBuffer.get();
            buffer = new byte[TAG_EXT_HEADER_CRC_DATA_LENGTH];
            byteBuffer.get(buffer, 0, TAG_EXT_HEADER_CRC_DATA_LENGTH);
            crcData = 0;
            for (int i = 0; i < TAG_EXT_HEADER_CRC_DATA_LENGTH; i++) {
                crcData <<= 8;
                crcData += buffer[i];
            }
        }

        //Tag Restriction
        if (tagRestriction) {
            byteBuffer.get();
            buffer = new byte[1];
            byteBuffer.get(buffer, 0, 1);
            tagSizeRestriction = (byte) ((buffer[0] & MASK_V24_TAG_SIZE_RESTRICTIONS) >> 6);
            textEncodingRestriction = (byte) ((buffer[0] & MASK_V24_TEXT_ENCODING_RESTRICTIONS) >> 5);
            textFieldSizeRestriction = (byte) ((buffer[0] & MASK_V24_TEXT_FIELD_SIZE_RESTRICTIONS) >> 3);
            imageEncodingRestriction = (byte) ((buffer[0] & MASK_V24_IMAGE_ENCODING) >> 2);
            imageSizeRestriction = (byte) (buffer[0] & MASK_V24_IMAGE_SIZE_RESTRICTIONS);
        }
    }

    private int readExtendedHeader(Buffer buffer) throws InvalidTagException {
        try {
            int extendedHeaderSize = buffer.readInt();

            // the extended header must be at least 6 bytes
            if (extendedHeaderSize <= TAG_EXT_HEADER_LENGTH) {
                throw new InvalidTagException(String.format(Locale.getDefault(),
                        ID3_EXTENDED_HEADER_SIZE_TOO_SMALL,
                        loggingFilename,
                        extendedHeaderSize));
            }

            //Number of bytes
            buffer.readByte();

            // Read the extended flag bytes
            byte extFlag = buffer.readByte();
            updateTag = (extFlag & MASK_V24_TAG_UPDATE) != 0;
            crcDataFlag = (extFlag & MASK_V24_CRC_DATA_PRESENT) != 0;
            tagRestriction = (extFlag & MASK_V24_TAG_RESTRICTIONS) != 0;

            // read the length byte if the flag is set
            // this tag should always be zero but just in case
            // read this information.
            if (updateTag) {
                buffer.readByte();
            }

            //CRC-32
            if (crcDataFlag) {
                // the CRC has a variable length
                buffer.readByte();
                crcData = 0;
                for (int i = 0; i < TAG_EXT_HEADER_CRC_DATA_LENGTH; i++) {
                    crcData <<= 8;
                    crcData += buffer.readByte();
                }
            }

            //Tag Restriction
            if (tagRestriction) {
                buffer.readByte();
                byte flags = buffer.readByte();
                tagSizeRestriction = (byte) ((flags & MASK_V24_TAG_SIZE_RESTRICTIONS) >> 6);
                textEncodingRestriction = (byte) ((flags & MASK_V24_TEXT_ENCODING_RESTRICTIONS) >> 5);
                textFieldSizeRestriction = (byte) ((flags & MASK_V24_TEXT_FIELD_SIZE_RESTRICTIONS) >> 3);
                imageEncodingRestriction = (byte) ((flags & MASK_V24_IMAGE_ENCODING) >> 2);
                imageSizeRestriction = (byte) (flags & MASK_V24_IMAGE_SIZE_RESTRICTIONS);
            }
            return extendedHeaderSize;
        } catch (EOFException e) {
            throw new InvalidTagException(e);
        }
    }

    public void read(ByteBuffer byteBuffer) throws TagException {
        int size;
        if (!seek(byteBuffer)) {
            throw new TagNotFoundException(loggingFilename + ":" + getIdentifier() + " tag not found");
        }
        LOG.log(DEBUG, "%s:Reading ID3v24 tag", loggingFilename);
        readHeaderFlags(byteBuffer.get());

        // Read the size, this is size of tag apart from tag header
        size = ID3SyncSafeInteger.bufferToValue(byteBuffer);
        LOG.log(DEBUG, "%s:Reading tag from file size set in header is %s", loggingFilename, size);

        if (extended) {
            readExtendedHeader(byteBuffer);
        }

        //Note if there was an extended header the size value has padding taken
        //off so we dont search it.
        readFrames(byteBuffer, size);
    }

    public void read(Buffer buffer, Id3v2Header header, boolean ignoreArtwork) throws TagException {
        readHeaderFlags(header.getFlags());

        //Extended Header
        if (extended) {
            readExtendedHeader(buffer);
        }

        readFrames(buffer, header.getTagSize(), ignoreArtwork);
        LOG.log(DEBUG, "%s:Loaded Frames,there are:%s", loggingFilename, frameMap.keySet().size());
    }

    private void readFrames(ByteBuffer byteBuffer, int size) {
        LOG.log(TRACE, "%s:Start of frame body at %s", loggingFilename, byteBuffer.position());
        //Now start looking for frames
        ID3v24Frame next;
        ensureFrameMapsAndClear();

        //Read the size from the Tag Header
        fileReadSize = size;
        // Read the frames until got to up to the size as specified in header
        LOG.log(TRACE, "%s:Start of frame body at:%s, frames data size is:%s", loggingFilename, byteBuffer.position(), size);
        while (byteBuffer.position() <= size) {
            String id;
            try {
                //Read Frame
                LOG.log(TRACE, "%s:looking for next frame at:%s", loggingFilename, byteBuffer.position());
                next = new ID3v24Frame(byteBuffer, loggingFilename);
                id = next.getIdentifier();
                loadFrameIntoMap(id, next);
            }
            //Found Padding, no more frames
            catch (PaddingException ex) {
                LOG.log(DEBUG, "%s:Found padding starting at:%s", loggingFilename, byteBuffer.position());
                break;
            }
            //Found Empty Frame
            catch (EmptyFrameException ex) {
                LOG.log(WARN, "%s:Empty Frame", loggingFilename, ex);
                emptyFrameBytes += TAG_HEADER_LENGTH;
            } catch (InvalidFrameIdentifierException ifie) {
                LOG.log(DEBUG, "%s:Invalid Frame Identifier", loggingFilename, ifie);
                invalidFrames++;
                //Don't try and find any more frames
                break;
            }
            //Problem trying to find frame
            catch (InvalidFrameException ife) {
                LOG.log(WARN, "%s:Invalid Frame:", loggingFilename, ife);
                invalidFrames++;
                //Don't try and find any more frames
                break;
            }
            //Failed reading frame but may just have invalid data but correct length so lets carry on
            //in case we can read the next frame
            catch (InvalidDataTypeException idete) {
                LOG.log(WARN, loggingFilename + ":Corrupt Frame:" + idete.getMessage());
                invalidFrames++;
            }
        }
    }

    private void readFrames(Buffer buffer, int size, boolean ignoreArtwork) {
        ensureFrameMapsAndClear();
        fileReadSize = size;
        while (buffer.size() > 0) {
            try {
                ID3v24Frame next = new ID3v24Frame(buffer, loggingFilename, ignoreArtwork);
                if (ignoreArtwork && next.isArtworkFrame()) {
                    setReadOnly();
                } else {
                    loadFrameIntoMap(next.getIdentifier(), next);
                }
            } catch (PaddingException ex) {
                //Found Padding, no more frames
                LOG.log(DEBUG, "Found padding with %s remaining. %s", buffer.size(), loggingFilename);
                break;
            } catch (EmptyFrameException ex) {
                //Found Empty Frame, log it - empty frames should not exist
                LOG.log(WARN, "%s:Empty Frame", loggingFilename, ex);
                emptyFrameBytes += ID3v23Frame.FRAME_HEADER_SIZE;
            } catch (InvalidFrameIdentifierException ifie) {
                LOG.log(WARN, "%s:Invalid Frame Identifier", loggingFilename, ifie);
                invalidFrames++;
                //Don't try and find any more frames
                break;
            } catch (InvalidFrameException ife) {
                //Problem trying to find frame, often just occurs because frameHeader includes padding
                //and we have reached padding
                LOG.log(WARN, "%s:Invalid Frame", loggingFilename, ife);
                invalidFrames++;
                //Don't try and find any more frames
                break;
            } catch (InvalidTagException idete) {
                //Failed reading frame but may just have invalid data but correct length so lets carry on
                //in case we can read the next frame
                LOG.log(WARN, "%s:Corrupt Frame", loggingFilename, idete);
                invalidFrames++;
            } catch (IOException e) {
                LOG.log(WARN, "Unexpectedly reached end of frame", e);
                invalidFrames++;
            } // TODO: 1/25/17 get exceptions straightened out

        }
    }

    private void ensureFrameMapsAndClear() {
        if (frameMap == null) {
            frameMap = new LinkedHashMap<>();
        }
        if (encryptedFrameMap == null) {
            encryptedFrameMap = new LinkedHashMap<>();
        }

        frameMap.clear();
        encryptedFrameMap.clear();
    }

    /**
     * Write the ID3 header to the ByteBuffer.
     * <p>
     * TODO Calculate the CYC Data Check
     * TODO Reintroduce Extended Header
     *
     * @param padding is the size of the padding
     * @param size    is the size of the body data
     * @return ByteBuffer
     */
    private ByteBuffer writeHeaderToBuffer(int padding, int size) {
        //This would only be set if every frame in tag has been unsynchronized, I only unsychronize frames
        //that need it, in any case I have been advised not to set it even then.
        unsynchronization = false;

        // Flags,currently we never calculate the CRC
        // and if we dont calculate them cant keep orig values. Tags are not
        // experimental and we never create extended header to keep things simple.
        extended = false;
        experimental = false;
        footer = false;

        // Create Header Buffer,allocate maximum possible size for the header
        ByteBuffer headerBuffer = ByteBuffer.allocate(TAG_HEADER_LENGTH);
        //TAGID
        headerBuffer.put(TAG_ID);

        //Major Version
        headerBuffer.put(getMajorVersion());

        //Minor Version
        headerBuffer.put(getRevision());

        //Flags
        byte flagsByte = 0;
        if (isUnsynchronization()) {
            flagsByte |= MASK_V24_UNSYNCHRONIZATION;
        }
        if (extended) {
            flagsByte |= MASK_V24_EXTENDED_HEADER;
        }
        if (experimental) {
            flagsByte |= MASK_V24_EXPERIMENTAL;
        }
        if (footer) {
            flagsByte |= MASK_V24_FOOTER_PRESENT;
        }
        headerBuffer.put(flagsByte);

        //Size As Recorded in Header, don't include the main header length
        //Additional Header Size,(for completeness we never actually write the extended header, or footer)
        int additionalHeaderSize = 0;
        if (extended) {
            additionalHeaderSize += TAG_EXT_HEADER_LENGTH;
            if (updateTag) {
                additionalHeaderSize += TAG_EXT_HEADER_UPDATE_LENGTH;
            }
            if (crcDataFlag) {
                additionalHeaderSize += TAG_EXT_HEADER_CRC_LENGTH;
            }
            if (tagRestriction) {
                additionalHeaderSize += TAG_EXT_HEADER_RESTRICTION_LENGTH;
            }
        }

        //Size As Recorded in Header, don't include the main header length
        headerBuffer.put(ID3SyncSafeInteger.valueToBuffer(padding + size + additionalHeaderSize));

        //Write Extended Header
        ByteBuffer extHeaderBuffer = null;
        if (extended) {
            //Write Extended Header Size
            int extendedSize = TAG_EXT_HEADER_LENGTH;
            if (updateTag) {
                extendedSize += TAG_EXT_HEADER_UPDATE_LENGTH;
            }
            if (crcDataFlag) {
                extendedSize += TAG_EXT_HEADER_CRC_LENGTH;
            }
            if (tagRestriction) {
                extendedSize += TAG_EXT_HEADER_RESTRICTION_LENGTH;
            }
            extHeaderBuffer = ByteBuffer.allocate(extendedSize);
            extHeaderBuffer.putInt(extendedSize);
            //Write Number of flags Byte
            extHeaderBuffer.put((byte) TAG_EXT_NUMBER_BYTES_DATA_LENGTH);
            //Write Extended Flags
            byte extFlag = 0;
            if (updateTag) {
                extFlag |= MASK_V24_TAG_UPDATE;
            }
            if (crcDataFlag) {
                extFlag |= MASK_V24_CRC_DATA_PRESENT;
            }
            if (tagRestriction) {
                extFlag |= MASK_V24_TAG_RESTRICTIONS;
            }
            extHeaderBuffer.put(extFlag);
            //Write Update Data
            if (updateTag) {
                extHeaderBuffer.put((byte) 0);
            }
            //Write CRC Data
            if (crcDataFlag) {
                extHeaderBuffer.put((byte) TAG_EXT_HEADER_CRC_DATA_LENGTH);
                extHeaderBuffer.put((byte) 0);
                extHeaderBuffer.putInt(crcData);
            }
            //Write Tag Restriction
            if (tagRestriction) {
                extHeaderBuffer.put((byte) TAG_EXT_HEADER_RESTRICTION_DATA_LENGTH);
                //todo not currently setting restrictions
                extHeaderBuffer.put((byte) 0);
            }
        }

        if (extHeaderBuffer != null) {
            extHeaderBuffer.flip();
            headerBuffer.put(extHeaderBuffer);
        }

        headerBuffer.flip();
        return headerBuffer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long write(File file, long audioStartLocation) throws IOException {
        setLoggingFilename(file.getName());
        LOG.log(DEBUG, "Writing tag to file:%s", loggingFilename);

        //Write Body Buffer
        byte[] bodyByteBuffer = writeFramesToBuffer().toByteArray();

        //Calculate Tag Size including Padding
        int sizeIncPadding = calculateTagSize(bodyByteBuffer.length + TAG_HEADER_LENGTH, (int) audioStartLocation);

        //Calculate padding bytes required
        int padding = sizeIncPadding - (bodyByteBuffer.length + TAG_HEADER_LENGTH);

        ByteBuffer headerBuffer = writeHeaderToBuffer(padding, bodyByteBuffer.length);
        writeBufferToFile(file, headerBuffer, bodyByteBuffer, padding, sizeIncPadding, audioStartLocation);
        return sizeIncPadding;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(WritableByteChannel channel, int currentTagSize) throws IOException {
        LOG.log(ERROR, "Writing tag to channel");

        byte[] bodyByteBuffer = writeFramesToBuffer().toByteArray();


        int padding = 0;
        if (currentTagSize > 0) {
            int sizeIncPadding = calculateTagSize(bodyByteBuffer.length + TAG_HEADER_LENGTH, currentTagSize);
            padding = sizeIncPadding - (bodyByteBuffer.length + TAG_HEADER_LENGTH);
        }
        ByteBuffer headerBuffer = writeHeaderToBuffer(padding, bodyByteBuffer.length);

        channel.write(headerBuffer);
        channel.write(ByteBuffer.wrap(bodyByteBuffer));
        writePadding(channel, padding);
    }

    /**
     * Display the tag in an XMLFormat
     */
    public void createStructure() {
        MP3File.getStructureFormatter().openHeadingElement(TYPE_TAG, getIdentifier());

        createStructureHeader();

        //Header
        MP3File.getStructureFormatter().openHeadingElement(TYPE_HEADER, "");
        MP3File.getStructureFormatter().addElement(TYPE_UNSYNCHRONISATION, isUnsynchronization());
        MP3File.getStructureFormatter().addElement(TYPE_CRCDATA, crcData);
        MP3File.getStructureFormatter().addElement(TYPE_EXPERIMENTAL, experimental);
        MP3File.getStructureFormatter().addElement(TYPE_EXTENDED, extended);
        MP3File.getStructureFormatter().addElement(TYPE_PADDINGSIZE, paddingSize);
        MP3File.getStructureFormatter().addElement(TYPE_FOOTER, footer);
        MP3File.getStructureFormatter().addElement(TYPE_IMAGEENCODINGRESTRICTION, paddingSize);
        MP3File.getStructureFormatter().addElement(TYPE_IMAGESIZERESTRICTION, imageSizeRestriction);
        MP3File.getStructureFormatter().addElement(TYPE_TAGRESTRICTION, tagRestriction);
        MP3File.getStructureFormatter().addElement(TYPE_TAGSIZERESTRICTION, tagSizeRestriction);
        MP3File.getStructureFormatter().addElement(TYPE_TEXTFIELDSIZERESTRICTION, textFieldSizeRestriction);
        MP3File.getStructureFormatter().addElement(TYPE_TEXTENCODINGRESTRICTION, textEncodingRestriction);
        MP3File.getStructureFormatter().addElement(TYPE_UPDATETAG, updateTag);
        MP3File.getStructureFormatter().closeHeadingElement(TYPE_HEADER);

        //Body
        createStructureBody();

        MP3File.getStructureFormatter().closeHeadingElement(TYPE_TAG);
    }

    /**
     * Are all frame swithin this tag unsynchronized
     * <p>
     * <p>Because synchronization occurs at the frame level it is not normally desirable to unsynchronize all frames
     * and hence this flag is not normally set.
     *
     * @return are all frames within the tag unsynchronized
     */
    boolean isUnsynchronization() {
        return unsynchronization;
    }

    public ID3v24Frame createFrame(String id) {
        return new ID3v24Frame(id);
    }


    public TagField createField(ID3v24FieldKey id3Key, String value) throws IllegalArgumentException, FieldDataInvalidException {
        checkArgNotNull(id3Key);
        return doCreateTagField(new FrameAndSubId(null, id3Key.getFrameId(), id3Key.getSubId()), checkArgNotNullOrEmpty(value));
    }

    public String getFirst(ID3v24FieldKey id3v24FieldKey) throws IllegalArgumentException {
        checkArgNotNull(id3v24FieldKey);
        FieldKey genericKey = ID3v24Frames.getInstanceOf().getGenericKeyFromId3(id3v24FieldKey);
        return genericKey != null
                ? getFirst(genericKey)
                : doGetValueAtIndex(new FrameAndSubId(null, id3v24FieldKey.getFrameId(), id3v24FieldKey.getSubId()), 0);
    }


    public void deleteField(ID3v24FieldKey id3v24FieldKey) throws IllegalArgumentException {
        checkArgNotNull(id3v24FieldKey);
        doDeleteTagField(new FrameAndSubId(null, id3v24FieldKey.getFrameId(), id3v24FieldKey.getSubId()));
    }

    public Tag deleteField(String id) throws IllegalArgumentException, UnsupportedFieldException {
        checkArgNotNullOrEmpty(id, Check.CANNOT_BE_NULL_OR_EMPTY, "id");
        doDeleteTagField(new FrameAndSubId(null, id, null));
        return this;
    }

    protected FrameAndSubId getFrameAndSubIdFromGenericKey(FieldKey genericKey) throws UnsupportedFieldException {
        ID3v24FieldKey id3v24FieldKey = ID3v24Frames.getInstanceOf().getId3KeyFromGenericKey(genericKey);
        if (id3v24FieldKey == null) {
            throw new UnsupportedFieldException(genericKey.name());
        }
        return new FrameAndSubId(genericKey, id3v24FieldKey.getFrameId(), id3v24FieldKey.getSubId());
    }

    protected ID3Frames getID3Frames() {
        return ID3v24Frames.getInstanceOf();
    }

    /**
     * @return comparator used to order frames in preferred order for writing to file so that most important frames are written first.
     */
    public Comparator<String> getPreferredFrameOrderComparator() {
        return ID3v24PreferredFrameOrderComparator.getInstanceof();
    }

    /**
     * {@inheritDoc}
     * <p>
     * {@link FieldKey#GENRE} has special processing in this method
     */
    public TagField createField(FieldKey genericKey, String... values) throws IllegalArgumentException,
            UnsupportedFieldException,
            FieldDataInvalidException {
        checkArgNotNull(genericKey, CANNOT_BE_NULL, "genericKey");
        if (genericKey == FieldKey.GENRE) {
            String value = checkVarArg0NotNull(values, Check.AT_LEAST_ONE_REQUIRED, "value");

            FrameAndSubId formatKey = getFrameAndSubIdFromGenericKey(genericKey);
            AbstractID3v2Frame frame = createFrame(formatKey.getFrameId());
            FrameBodyTCON framebody = (FrameBodyTCON) frame.getBody();

            if (TagOptionSingleton.getInstance().isWriteMp3GenresAsText()) {
                framebody.setText(value);
            } else {
                framebody.setText(FrameBodyTCON.convertGenericToID3v24Genre(value));
            }
            return frame;
        } else {
            return super.createField(genericKey, values);
        }
    }

    @Override
    public ImmutableSet<FieldKey> getSupportedFields() {
        return ID3v24Frames.getInstanceOf().getSupportedFields();
    }

    public List<String> getAll(FieldKey genericKey) throws IllegalArgumentException, UnsupportedFieldException {
        if (genericKey == FieldKey.GENRE) {
            List<TagField> fields = getFields(genericKey);
            List<String> convertedGenres = new ArrayList<>();
            if (fields != null && fields.size() > 0) {
                AbstractID3v2Frame frame = (AbstractID3v2Frame) fields.get(0);
                FrameBodyTCON body = (FrameBodyTCON) frame.getBody();

                for (String next : body.getValues()) {
                    convertedGenres.add(FrameBodyTCON.convertID3v24GenreToGeneric(next));
                }
            }
            return convertedGenres;
        } else {
            return super.getAll(genericKey);
        }
    }

    @Override
    public Optional<String> getValue(FieldKey genericKey, int index) throws IllegalArgumentException {
        checkArgNotNull(genericKey, CANNOT_BE_NULL, "genericKey");
        if (genericKey == FieldKey.GENRE) {
            List<TagField> fields = getFields(genericKey);
            if (fields != null && fields.size() > 0) {
                AbstractID3v2Frame frame = (AbstractID3v2Frame) fields.get(0);
                FrameBodyTCON body = (FrameBodyTCON) frame.getBody();
                return Optional.of(FrameBodyTCON.convertID3v24GenreToGeneric(body.getValues().get(index)));
            }
            return Optional.absent();
        } else {
            return super.getValue(genericKey, index);
        }
    }

    @Override
    public int getFieldCount(Key genericKey) throws IllegalArgumentException, UnsupportedFieldException {
        return getFields(genericKey.name()).size();
    }
}
