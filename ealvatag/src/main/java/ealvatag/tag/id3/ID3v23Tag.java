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
 *  you can getFields a copy from http://www.opensource.org/licenses/lgpl-license.php or write to the Free Software
 *  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 */
package ealvatag.tag.id3;

import static ealvatag.logging.EalvaTagLog.LogLevel.DEBUG;
import static ealvatag.logging.EalvaTagLog.LogLevel.ERROR;
import static ealvatag.logging.EalvaTagLog.LogLevel.TRACE;
import static ealvatag.logging.EalvaTagLog.LogLevel.WARN;
import static ealvatag.utils.Check.CANNOT_BE_NULL;
import static ealvatag.utils.Check.CANNOT_BE_NULL_OR_EMPTY;
import static ealvatag.utils.Check.checkArgNotNull;
import static ealvatag.utils.Check.checkArgNotNullOrEmpty;
import static ealvatag.utils.Check.checkVarArg0NotNull;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
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
import ealvatag.tag.InvalidTagHeaderException;
import ealvatag.tag.Key;
import ealvatag.tag.PaddingException;
import ealvatag.tag.Tag;
import ealvatag.tag.TagException;
import ealvatag.tag.TagField;
import ealvatag.tag.TagNotFoundException;
import ealvatag.tag.TagOptionSingleton;
import ealvatag.tag.TagTextField;
import ealvatag.tag.UnsupportedFieldException;
import ealvatag.tag.datatype.Pair;
import ealvatag.tag.datatype.PairedTextEncodedStringNullTerminated;
import ealvatag.tag.id3.framebody.AbstractFrameBodyTextInfo;
import ealvatag.tag.id3.framebody.FrameBodyIPLS;
import ealvatag.tag.id3.framebody.FrameBodyTCON;
import ealvatag.tag.id3.framebody.FrameBodyTDAT;
import ealvatag.tag.id3.framebody.FrameBodyTDRC;
import ealvatag.tag.id3.framebody.FrameBodyTIME;
import ealvatag.tag.id3.framebody.FrameBodyTIPL;
import ealvatag.tag.id3.framebody.FrameBodyTMCL;
import ealvatag.tag.id3.framebody.FrameBodyTYER;
import okio.Buffer;

/**
 * Represents an ID3v2.3 tag.
 *
 * @author : Paul Taylor
 * @author : Eric Farng
 * @version $Id$
 */
public class ID3v23Tag extends AbstractID3v2Tag {
    public static final byte MAJOR_VERSION = 3;
    /**
     * ID3v2.3 Header bit mask
     */
    private static final int MASK_V23_UNSYNCHRONIZATION = FileConstants.BIT7;
    /**
     * ID3v2.3 Header bit mask
     */
    private static final int MASK_V23_EXTENDED_HEADER = FileConstants.BIT6;
    /**
     * ID3v2.3 Header bit mask
     */
    private static final int MASK_V23_EXPERIMENTAL = FileConstants.BIT5;
    /**
     * ID3v2.3 Extended Header bit mask
     */
    private static final int MASK_V23_CRC_DATA_PRESENT = FileConstants.BIT7;
    /**
     * ID3v2.3 RBUF frame bit mask
     */
//  public static final int MASK_V23_EMBEDDED_INFO_FLAG = FileConstants.BIT1;
    private static final byte RELEASE = 2;
    private static final byte REVISION = 0;
    private static final String TYPE_CRCDATA = "crcdata";
    private static final String TYPE_EXPERIMENTAL = "experimental";
    private static final String TYPE_EXTENDED = "extended";
    private static final String TYPE_PADDINGSIZE = "paddingsize";
    private static final String TYPE_UNSYNCHRONISATION = "unsyncronisation";
    private static final JLogger LOG = JLoggers.get(ID3v23Tag.class, EalvaTagLog.MARKER);
    private static final int TAG_EXT_HEADER_LENGTH = 10;
    private static final int TAG_EXT_HEADER_CRC_LENGTH = 4;
    private static final int FIELD_TAG_EXT_SIZE_LENGTH = 4;
    private static final int TAG_EXT_HEADER_DATA_LENGTH = TAG_EXT_HEADER_LENGTH - FIELD_TAG_EXT_SIZE_LENGTH;
    /**
     * Contains extended header
     */
    protected boolean extended;
    /**
     * The tag is compressed
     */
    protected boolean compression;
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
     * Crcdata Checksum in extended header
     */
    private int crc32;
    /**
     * Tag padding
     */
    private int paddingSize;


    /**
     * Creates a new empty ID3v2_3 datatype.
     */
    public ID3v23Tag() {
        ensureFrameMapsAndClear();
    }

    /**
     * Copy Constructor, creates a new ID3v2_3 Tag based on another ID3v2_3 Tag
     */
    public ID3v23Tag(ID3v23Tag copyObject) {
        LOG.log(DEBUG, "Creating tag from another tag of same type");
        copyPrimitives(copyObject);
        copyFrames(copyObject);

    }

    /**
     * Constructs a new tag based upon another tag of different version/type
     */
    public ID3v23Tag(BaseID3Tag mp3tag) {
        LOG.log(DEBUG, "Creating tag from a tag of a different version");
        ensureFrameMapsAndClear();

        if (mp3tag != null) {
            ID3v24Tag convertedTag;
            //Should use simpler copy constructor
            if (mp3tag instanceof ID3v23Tag) {
                throw new UnsupportedOperationException("Copy Constructor not called. Please type cast the argument");
            }
            if (mp3tag instanceof ID3v24Tag) {
                convertedTag = (ID3v24Tag) mp3tag;
            }
            //All tags types can be converted to v2.4 so do this to simplify things
            else {
                convertedTag = new ID3v24Tag(mp3tag);
            }
            setLoggingFilename(convertedTag.loggingFilename);
            //Copy Primitives
            copyPrimitives(convertedTag);
            //Copy Frames
            copyFrames(convertedTag);
            LOG.log(DEBUG, "Created tag from a tag of a different version");
        }
    }

    public ID3v23Tag(ByteBuffer buffer, String loggingFilename) throws TagException {
        setLoggingFilename(loggingFilename);
        read(buffer);
    }

    public ID3v23Tag(Buffer buffer, Id3v2Header header, String loggingFilename, boolean ignoreArtwork) throws TagException {
        setLoggingFilename(loggingFilename);
        read(buffer, header, ignoreArtwork);
    }

    public int getCrc32() {
        return crc32;
    }

    /**
     * Copy primitives applicable to v2.3
     */
    protected void copyPrimitives(AbstractID3v2Tag copyObj) {
        LOG.log(DEBUG, "Copying primitives");
        super.copyPrimitives(copyObj);

        if (copyObj instanceof ID3v23Tag) {
            ID3v23Tag copyObject = (ID3v23Tag) copyObj;
            crcDataFlag = copyObject.crcDataFlag;
            experimental = copyObject.experimental;
            extended = copyObject.extended;
            crc32 = copyObject.crc32;
            paddingSize = copyObject.paddingSize;
        }
    }

    @Override
    public void addFrame(AbstractID3v2Frame frame) {
        try {
            if (frame instanceof ID3v23Frame) {
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

    @Override
    protected List<AbstractID3v2Frame> convertFrame(AbstractID3v2Frame frame) throws InvalidFrameException {
        List<AbstractID3v2Frame> frames = new ArrayList<>();
        if ((frame.getIdentifier().equals(ID3v24Frames.FRAME_ID_YEAR)) && (frame.getBody() instanceof FrameBodyTDRC)) {
            //TODO will overwrite any existing TYER or TIME frame, do we ever want multiples of these
            FrameBodyTDRC tmpBody = (FrameBodyTDRC) frame.getBody();
            tmpBody.findMatchingMaskAndExtractV3Values();
            ID3v23Frame newFrame;
            if (!tmpBody.getYear().equals("")) {
                newFrame = new ID3v23Frame(ID3v23Frames.FRAME_ID_V3_TYER);
                ((FrameBodyTYER) newFrame.getBody()).setText(tmpBody.getYear());
                frames.add(newFrame);
            }
            if (!tmpBody.getDate().equals("")) {
                newFrame = new ID3v23Frame(ID3v23Frames.FRAME_ID_V3_TDAT);
                ((FrameBodyTDAT) newFrame.getBody()).setText(tmpBody.getDate());
                ((FrameBodyTDAT) newFrame.getBody()).setMonthOnly(tmpBody.isMonthOnly());
                frames.add(newFrame);
            }
            if (!tmpBody.getTime().equals("")) {
                newFrame = new ID3v23Frame(ID3v23Frames.FRAME_ID_V3_TIME);
                ((FrameBodyTIME) newFrame.getBody()).setText(tmpBody.getTime());
                ((FrameBodyTIME) newFrame.getBody()).setHoursOnly(tmpBody.isHoursOnly());
                frames.add(newFrame);
            }
        }
        //If at later stage we have multiple IPLS frames we have to merge
        else if ((frame.getIdentifier().equals(ID3v24Frames.FRAME_ID_INVOLVED_PEOPLE)) &&
                (frame.getBody() instanceof FrameBodyTIPL)) {
            List<Pair> pairs = ((FrameBodyTIPL) frame.getBody()).getPairing().getMapping();
            AbstractID3v2Frame ipls = new ID3v23Frame((ID3v24Frame) frame, ID3v23Frames.FRAME_ID_V3_INVOLVED_PEOPLE);
            FrameBodyIPLS iplsBody = new FrameBodyIPLS(frame.getBody().getTextEncoding(), pairs);
            ipls.setBody(iplsBody);
            frames.add(ipls);
        } else if ((frame.getIdentifier().equals(ID3v24Frames.FRAME_ID_MUSICIAN_CREDITS)) &&
                (frame.getBody() instanceof FrameBodyTMCL)) {
            List<Pair> pairs = ((FrameBodyTMCL) frame.getBody()).getPairing().getMapping();
            AbstractID3v2Frame ipls = new ID3v23Frame((ID3v24Frame) frame, ID3v23Frames.FRAME_ID_V3_INVOLVED_PEOPLE);
            FrameBodyIPLS iplsBody = new FrameBodyIPLS(frame.getBody().getTextEncoding(), pairs);
            ipls.setBody(iplsBody);
            frames.add(ipls);
        } else {
            frames.add(new ID3v23Frame(frame));
        }
        return frames;
    }

    protected ID3Frames getID3Frames() {
        return ID3v23Frames.getInstanceOf();
    }

    @Override
    public int getFieldCount(Key genericKey) throws IllegalArgumentException, UnsupportedFieldException {
        return getFields(genericKey.name()).size();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Overridden because YEAR key can be served by TDAT, TYER or special aggreagted frame
     */
    @Override
    public ImmutableList<TagField> getFields(FieldKey genericKey)
            throws IllegalArgumentException, UnsupportedFieldException {
        checkArgNotNull(genericKey, CANNOT_BE_NULL, "genericKey");
        if (genericKey == FieldKey.YEAR) {
            AggregatedFrame af = (AggregatedFrame) getFrame(TyerTdatAggregatedFrame.ID_TYER_TDAT);
            if (af != null) {
                return ImmutableList.<TagField>of(af);
            } else {
                return super.getFields(genericKey);
            }
        } else {
            return super.getFields(genericKey);
        }
    }

    /**
     * {@inheritDoc}
     * Overridden because GENRE can need converting of data to ID3v23 format and
     * YEAR key is specially processed by getFields() for ID3
     */
    @Override
    public List<String> getAll(FieldKey genericKey) throws IllegalArgumentException, UnsupportedFieldException {
        if (genericKey == FieldKey.GENRE) {
            List<TagField> fields = getFields(genericKey);
            List<String> convertedGenres = new ArrayList<>();
            if (fields != null && fields.size() > 0) {
                AbstractID3v2Frame frame = (AbstractID3v2Frame) fields.get(0);
                FrameBodyTCON body = (FrameBodyTCON) frame.getBody();

                for (String next : body.getValues()) {
                    convertedGenres.add(FrameBodyTCON.convertID3v23GenreToGeneric(next));
                }
            }
            return convertedGenres;
        } else if (genericKey == FieldKey.YEAR) {
            List<TagField> fields = getFields(genericKey);
            List<String> results = new ArrayList<>();
            if (fields != null && fields.size() > 0) {
                for (TagField next : fields) {
                    if (next instanceof TagTextField) {
                        results.add(((TagTextField) next).getContent());
                    }
                }
            }
            return results;
        } else {
            return super.getAll(genericKey);
        }
    }

    @Override
    public Optional<String> getValue(FieldKey genericKey, int index) throws IllegalArgumentException {
        checkArgNotNull(genericKey, CANNOT_BE_NULL, "genericKey");
        if (genericKey == FieldKey.YEAR) {
            AggregatedFrame af = (AggregatedFrame) getFrame(TyerTdatAggregatedFrame.ID_TYER_TDAT);
            if (af != null) {
                return Optional.of(af.getContent());
            } else {
                return super.getValue(genericKey, index);
            }
        } else if (genericKey == FieldKey.GENRE) {
            List<TagField> fields = getFields(genericKey);
            if (fields != null && fields.size() > 0) {
                AbstractID3v2Frame frame = (AbstractID3v2Frame) fields.get(0);
                FrameBodyTCON body = (FrameBodyTCON) frame.getBody();
                return Optional.of(FrameBodyTCON.convertID3v23GenreToGeneric(body.getValues().get(index)));
            }
            return Optional.absent();
        } else {
            return super.getValue(genericKey, index);
        }
    }

    /**
     * {@inheritDoc}
     * Overridden to allow special handling for mapping YEAR to TYER and TDAT Frames
     */
    @Override
    public TagField createField(FieldKey genericKey, String... values) throws IllegalArgumentException,
            UnsupportedFieldException,
            FieldDataInvalidException {
        checkArgNotNull(genericKey);
        String value = checkVarArg0NotNull(values);
        if (genericKey == FieldKey.GENRE) {
            FrameAndSubId formatKey = getFrameAndSubIdFromGenericKey(genericKey);
            AbstractID3v2Frame frame = createFrame(formatKey.getFrameId());
            FrameBodyTCON framebody = (FrameBodyTCON) frame.getBody();
            framebody.setV23Format();

            if (TagOptionSingleton.getInstance().isWriteMp3GenresAsText()) {
                framebody.setText(value);
            } else {
                framebody.setText(FrameBodyTCON.convertGenericToID3v23Genre(value));
            }
            return frame;
        } else if (genericKey == FieldKey.YEAR) {
            if (value.length() == 1) {
                AbstractID3v2Frame tyer = createFrame(ID3v23Frames.FRAME_ID_V3_TYER);
                ((AbstractFrameBodyTextInfo) tyer.getBody()).setText("000" + value);
                return tyer;
            } else if (value.length() == 2) {
                AbstractID3v2Frame tyer = createFrame(ID3v23Frames.FRAME_ID_V3_TYER);
                ((AbstractFrameBodyTextInfo) tyer.getBody()).setText("00" + value);
                return tyer;
            } else if (value.length() == 3) {
                AbstractID3v2Frame tyer = createFrame(ID3v23Frames.FRAME_ID_V3_TYER);
                ((AbstractFrameBodyTextInfo) tyer.getBody()).setText("0" + value);
                return tyer;
            } else if (value.length() == 4) {
                AbstractID3v2Frame tyer = createFrame(ID3v23Frames.FRAME_ID_V3_TYER);
                ((AbstractFrameBodyTextInfo) tyer.getBody()).setText(value);
                return tyer;
            } else if (value.length() > 4) {
                AbstractID3v2Frame tyer = createFrame(ID3v23Frames.FRAME_ID_V3_TYER);
                ((AbstractFrameBodyTextInfo) tyer.getBody()).setText(value.substring(0, 4));

                if (value.length() >= 10) {
                    //Have a full yyyy-mm-dd value that needs storing in two frames in ID3
                    String month = value.substring(5, 7);
                    String day = value.substring(8, 10);
                    AbstractID3v2Frame tdat = createFrame(ID3v23Frames.FRAME_ID_V3_TDAT);
                    ((AbstractFrameBodyTextInfo) tdat.getBody()).setText(day + month);

                    TyerTdatAggregatedFrame ag = new TyerTdatAggregatedFrame();
                    ag.addFrame(tyer);
                    ag.addFrame(tdat);
                    return ag;
                } else if (value.length() >= 7) {
                    //TDAT frame requires both month and day so if we only have the month we just have to make
                    //the day up
                    String month = value.substring(5, 7);
                    String day = "01";
                    AbstractID3v2Frame tdat = createFrame(ID3v23Frames.FRAME_ID_V3_TDAT);
                    ((AbstractFrameBodyTextInfo) tdat.getBody()).setText(day + month);

                    TyerTdatAggregatedFrame ag = new TyerTdatAggregatedFrame();
                    ag.addFrame(tyer);
                    ag.addFrame(tdat);
                    return ag;
                } else {
                    //We only have year data
                    return tyer;
                }
            } else {
                return null;
            }
        } else {
            return super.createField(genericKey, values);
        }
    }

    /**
     * Write tag to file
     * <p>
     * TODO:we currently never write the Extended header , but if we did the size calculation in this
     * method would be slightly incorrect
     *
     * @param file The file to write to
     * @throws IOException if write error
     */
    public long write(File file, long audioStartLocation) throws IOException {
        setLoggingFilename(file.getName());
        LOG.log(DEBUG, "Writing tag to file:%s", loggingFilename);

        //Write Body Buffer
        byte[] bodyByteBuffer = writeFramesToBuffer().toByteArray();
        LOG.log(DEBUG, "%s:bodybytebuffer:sizebeforeunsynchronisation:%s", loggingFilename, bodyByteBuffer.length);

        // Unsynchronize if option enabled and unsync required
        unsynchronization = TagOptionSingleton.getInstance().isUnsyncTags() &&
                ID3Unsynchronization.requiresUnsynchronization(bodyByteBuffer);
        if (isUnsynchronized()) {
            bodyByteBuffer = ID3Unsynchronization.unsynchronize(bodyByteBuffer);
            LOG.log(DEBUG, "%s:bodybytebuffer:sizeafterunsynchronisation:%s", loggingFilename, bodyByteBuffer.length);
        }

        int sizeIncPadding = calculateTagSize(bodyByteBuffer.length + TAG_HEADER_LENGTH, (int) audioStartLocation);
        int padding = sizeIncPadding - (bodyByteBuffer.length + TAG_HEADER_LENGTH);
        LOG.log(DEBUG, "%s:Current audiostart:%s", loggingFilename, audioStartLocation);
        LOG.log(DEBUG, "%s:Size including padding:%s", loggingFilename, sizeIncPadding);
        LOG.log(DEBUG, "%s:Padding:%s", loggingFilename, padding);

        ByteBuffer headerBuffer = writeHeaderToBuffer(padding, bodyByteBuffer.length);
        writeBufferToFile(file, headerBuffer, bodyByteBuffer, padding, sizeIncPadding, audioStartLocation);
        return sizeIncPadding;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(WritableByteChannel channel, int currentTagSize) throws IOException {
        LOG.log(DEBUG, loggingFilename + ":Writing tag to channel");

        byte[] bodyByteBuffer = writeFramesToBuffer().toByteArray();
        LOG.log(DEBUG, "%s:bodybytebuffer:sizebeforeunsynchronisation:%s", loggingFilename, bodyByteBuffer.length);

        // Unsynchronize if option enabled and unsync required
        unsynchronization = TagOptionSingleton.getInstance().isUnsyncTags() &&
                ID3Unsynchronization.requiresUnsynchronization(bodyByteBuffer);
        if (isUnsynchronized()) {
            bodyByteBuffer = ID3Unsynchronization.unsynchronize(bodyByteBuffer);
            LOG.log(DEBUG, "%s:bodybytebuffer:sizeafterunsynchronisation:%s", loggingFilename, bodyByteBuffer.length);
        }

        int padding = 0;
        if (currentTagSize > 0) {
            int sizeIncPadding = calculateTagSize(bodyByteBuffer.length + TAG_HEADER_LENGTH, currentTagSize);
            padding = sizeIncPadding - (bodyByteBuffer.length + TAG_HEADER_LENGTH);
            LOG.log(DEBUG, "%s:Padding:%s", loggingFilename, padding);
        }
        ByteBuffer headerBuffer = writeHeaderToBuffer(padding, bodyByteBuffer.length);

        channel.write(headerBuffer);
        channel.write(ByteBuffer.wrap(bodyByteBuffer));
        writePadding(channel, padding);
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof ID3v23Tag)) {
            return false;
        }
        ID3v23Tag object = (ID3v23Tag) obj;
        return crc32 == object.crc32 &&
                crcDataFlag == object.crcDataFlag &&
                experimental == object.experimental &&
                extended == object.extended &&
                paddingSize == object.paddingSize &&
                super.equals(obj);
    }

    /**
     * Override to merge TIPL/TMCL into single IPLS frame
     */
    @Override
    protected void processDuplicateFrame(AbstractID3v2Frame newFrame, AbstractID3v2Frame existingFrame) {
        //We dont add this new frame we just add the contents to existing frame
        if (newFrame.getIdentifier().equals(ID3v23Frames.FRAME_ID_V3_INVOLVED_PEOPLE)) {
            PairedTextEncodedStringNullTerminated.ValuePairs oldVps =
                    ((FrameBodyIPLS) (existingFrame).getBody()).getPairing();
            PairedTextEncodedStringNullTerminated.ValuePairs newVps = ((FrameBodyIPLS) newFrame.getBody()).getPairing();
            for (Pair next : newVps.getMapping()) {
                oldVps.add(next);
            }
        } else {
            List<AbstractID3v2Frame> list = new ArrayList<>();
            list.add(existingFrame);
            list.add(newFrame);
            frameMap.put(newFrame.getIdentifier(), list);
        }
    }

    protected void loadFrameIntoMap(String frameId, AbstractID3v2Frame next) {
        if (next.getBody() instanceof FrameBodyTCON) {
            ((FrameBodyTCON) next.getBody()).setV23Format();
        }
        super.loadFrameIntoMap(frameId, next);
    }

    protected void loadFrameIntoSpecifiedMap(HashMap<String, Object> map, String frameId, AbstractID3v2Frame frame) {
        if (!(frameId.equals(ID3v23Frames.FRAME_ID_V3_TYER)) && !(frameId.equals(ID3v23Frames.FRAME_ID_V3_TDAT))) {
            super.loadFrameIntoSpecifiedMap(map, frameId, frame);
            return;
        }

        if (frameId.equals(ID3v23Frames.FRAME_ID_V3_TDAT)) {
            if (frame.getContent().length() == 0) {
                //Discard not useful to complicate by trying to map it
                LOG.log(WARN, "TDAT is empty so just ignoring");
                return;
            }
        }
        if (map.containsKey(frameId) || map.containsKey(TyerTdatAggregatedFrame.ID_TYER_TDAT)) {
            //If we have multiple duplicate frames in a tag separate them with semicolons
            if (duplicateFrameId.length() > 0) {
                duplicateFrameId += ";";
            }
            duplicateFrameId += frameId;
            duplicateBytes += frame.getSize();
        } else if (frameId.equals(ID3v23Frames.FRAME_ID_V3_TYER)) {
            if (map.containsKey(ID3v23Frames.FRAME_ID_V3_TDAT)) {
                TyerTdatAggregatedFrame ag = new TyerTdatAggregatedFrame();
                ag.addFrame(frame);
                ag.addFrame((AbstractID3v2Frame) map.get(ID3v23Frames.FRAME_ID_V3_TDAT));
                map.remove(ID3v23Frames.FRAME_ID_V3_TDAT);
                map.put(TyerTdatAggregatedFrame.ID_TYER_TDAT, ag);
            } else {
                map.put(ID3v23Frames.FRAME_ID_V3_TYER, frame);
            }
        } else if (frameId.equals(ID3v23Frames.FRAME_ID_V3_TDAT)) {
            if (map.containsKey(ID3v23Frames.FRAME_ID_V3_TYER)) {
                TyerTdatAggregatedFrame ag = new TyerTdatAggregatedFrame();
                ag.addFrame((AbstractID3v2Frame) map.get(ID3v23Frames.FRAME_ID_V3_TYER));
                ag.addFrame(frame);
                map.remove(ID3v23Frames.FRAME_ID_V3_TYER);
                map.put(TyerTdatAggregatedFrame.ID_TYER_TDAT, ag);
            } else {
                map.put(ID3v23Frames.FRAME_ID_V3_TDAT, frame);
            }
        }

    }

    /**
     * Return frame size based upon the sizes of the tags rather than the physical
     * no of bytes between start of ID3Tag and start of Audio Data.
     * <p>
     * TODO this is incorrect, because of subclasses
     *
     * @return size of tag
     */
    public int getSize() {
        int size = TAG_HEADER_LENGTH;
        if (extended) {
            size += TAG_EXT_HEADER_LENGTH;
            if (crcDataFlag) {
                size += TAG_EXT_HEADER_CRC_LENGTH;
            }
        }
        size += super.getSize();
        return size;
    }

    /**
     * @return comparator used to order frames in preferred order for writing to file so that most important frames are written first.
     */
    public Comparator<String> getPreferredFrameOrderComparator() {
        return ID3v23PreferredFrameOrderComparator.getInstanceof();
    }

    /**
     * For representing the MP3File in an XML Format
     */
    public void createStructure() {

        MP3File.getStructureFormatter().openHeadingElement(TYPE_TAG, getIdentifier());

        createStructureHeader();

        //Header
        MP3File.getStructureFormatter().openHeadingElement(TYPE_HEADER, "");
        MP3File.getStructureFormatter().addElement(TYPE_UNSYNCHRONISATION, isUnsynchronized());
        MP3File.getStructureFormatter().addElement(TYPE_EXTENDED, extended);
        MP3File.getStructureFormatter().addElement(TYPE_EXPERIMENTAL, experimental);
        MP3File.getStructureFormatter().addElement(TYPE_CRCDATA, crc32);
        MP3File.getStructureFormatter().addElement(TYPE_PADDINGSIZE, paddingSize);
        MP3File.getStructureFormatter().closeHeadingElement(TYPE_HEADER);
        //Body
        createStructureBody();
        MP3File.getStructureFormatter().closeHeadingElement(TYPE_TAG);
    }

    public ID3v23Frame createFrame(String id) {
        return new ID3v23Frame(id);
    }

    protected FrameAndSubId getFrameAndSubIdFromGenericKey(FieldKey genericKey) throws UnsupportedFieldException {
        ID3v23FieldKey id3v23FieldKey = ID3v23Frames.getInstanceOf().getId3KeyFromGenericKey(genericKey);
        if (id3v23FieldKey == null) {
            throw new UnsupportedFieldException(genericKey.name());
        }
        return new FrameAndSubId(genericKey, id3v23FieldKey.getFrameId(), id3v23FieldKey.getSubId());
    }

    /**
     * @return textual tag identifier
     */
    public String getIdentifier() {
        return "ID3v2.30";
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

    private void readHeaderFlags(byte flags) throws TagException {
        //Allowable Flags
        unsynchronization = (flags & MASK_V23_UNSYNCHRONIZATION) != 0;
        extended = (flags & MASK_V23_EXTENDED_HEADER) != 0;
        experimental = (flags & MASK_V23_EXPERIMENTAL) != 0;

        //Not allowable/Unknown Flags
        if ((flags & FileConstants.BIT4) != 0) {
            LOG.log(WARN, ErrorMessage.ID3_INVALID_OR_UNKNOWN_FLAG_SET, loggingFilename, FileConstants.BIT4);
        }

        if ((flags & FileConstants.BIT3) != 0) {
            LOG.log(WARN, ErrorMessage.ID3_INVALID_OR_UNKNOWN_FLAG_SET, loggingFilename, FileConstants.BIT3);
        }

        if ((flags & FileConstants.BIT2) != 0) {
            LOG.log(WARN, ErrorMessage.ID3_INVALID_OR_UNKNOWN_FLAG_SET, loggingFilename, FileConstants.BIT2);
        }

        if ((flags & FileConstants.BIT1) != 0) {
            LOG.log(WARN, ErrorMessage.ID3_INVALID_OR_UNKNOWN_FLAG_SET, loggingFilename, FileConstants.BIT1);
        }

        if ((flags & FileConstants.BIT0) != 0) {
            LOG.log(WARN, ErrorMessage.ID3_INVALID_OR_UNKNOWN_FLAG_SET, loggingFilename, FileConstants.BIT0);
        }

        if (isUnsynchronized()) {
            LOG.log(DEBUG, ErrorMessage.ID3_TAG_UNSYNCHRONIZED, loggingFilename);
        }

        if (extended) {
            LOG.log(DEBUG, ErrorMessage.ID3_TAG_EXTENDED, loggingFilename);
        }

        if (experimental) {
            LOG.log(DEBUG, ErrorMessage.ID3_TAG_EXPERIMENTAL, loggingFilename);
        }
    }

    /**
     * Read the optional extended header
     */
    private void readExtendedHeader(ByteBuffer buffer) {
        // Int is 4 bytes.
        int extendedHeaderSize = buffer.getInt();
        // Extended header without CRC Data
        if (extendedHeaderSize == TAG_EXT_HEADER_DATA_LENGTH) {
            //Flag should not be setField , if is log a warning
            byte extFlag = buffer.get();
            crcDataFlag = (extFlag & MASK_V23_CRC_DATA_PRESENT) != 0;
            if (crcDataFlag) {
                LOG.log(WARN, ErrorMessage.ID3_TAG_CRC_FLAG_SET_INCORRECTLY, loggingFilename);
            }
            //2nd Flag Byte (not used)
            buffer.get();

            //Take padding and ext header size off the size to be read
            paddingSize = buffer.getInt();
            if (paddingSize > 0) {
                LOG.log(DEBUG, ErrorMessage.ID3_TAG_PADDING_SIZE, loggingFilename, paddingSize);
            }
        } else if (extendedHeaderSize == TAG_EXT_HEADER_DATA_LENGTH + TAG_EXT_HEADER_CRC_LENGTH) {
            LOG.log(DEBUG, ErrorMessage.ID3_TAG_CRC, loggingFilename);

            //Flag should be setField, if nor just act as if it is
            byte extFlag = buffer.get();
            crcDataFlag = (extFlag & MASK_V23_CRC_DATA_PRESENT) != 0;
            if (!crcDataFlag) {
                LOG.log(WARN, ErrorMessage.ID3_TAG_CRC_FLAG_SET_INCORRECTLY, loggingFilename);
            }
            //2nd Flag Byte (not used)
            buffer.get();
            //Take padding size of size to be read
            paddingSize = buffer.getInt();
            if (paddingSize > 0) {
                LOG.log(DEBUG, ErrorMessage.ID3_TAG_PADDING_SIZE, loggingFilename, paddingSize);
            }
            //CRC Data
            crc32 = buffer.getInt();
            LOG.log(DEBUG, ErrorMessage.ID3_TAG_CRC_SIZE, loggingFilename, crc32);
        }
        //Extended header size is only allowed to be six or ten bytes so this is invalid but instead
        //of giving up lets guess its six bytes and carry on and see if we can read file ok
        else {
            LOG.log(WARN, ErrorMessage.ID3_EXTENDED_HEADER_SIZE_INVALID, loggingFilename, extendedHeaderSize);
            buffer.position(buffer.position() - FIELD_TAG_EXT_SIZE_LENGTH);
        }
    }

    private int readExtendedHeader(Buffer buffer) throws InvalidTagHeaderException {
        try {
            // Int is 4 bytes.
            int extendedHeaderSize = buffer.readInt();
            // Extended header without CRC Data
            if (extendedHeaderSize == TAG_EXT_HEADER_DATA_LENGTH) {
                //Flag should not be setField , if is log a warning
                byte extFlag = buffer.readByte();
                crcDataFlag = (extFlag & MASK_V23_CRC_DATA_PRESENT) != 0;
                if (crcDataFlag) {
                    LOG.log(WARN, ErrorMessage.ID3_TAG_CRC_FLAG_SET_INCORRECTLY, loggingFilename);
                }
                //2nd Flag Byte (not used)
                buffer.readByte();

                //Take padding and ext header size off the size to be read
                paddingSize = buffer.readInt();
                if (paddingSize > 0) {
                    LOG.log(DEBUG, ErrorMessage.ID3_TAG_PADDING_SIZE, loggingFilename, paddingSize);
                }
            } else if (extendedHeaderSize == TAG_EXT_HEADER_DATA_LENGTH + TAG_EXT_HEADER_CRC_LENGTH) {
                LOG.log(DEBUG, ErrorMessage.ID3_TAG_CRC, loggingFilename);

                //Flag should be setField, if nor just act as if it is
                byte extFlag = buffer.readByte();
                crcDataFlag = (extFlag & MASK_V23_CRC_DATA_PRESENT) != 0;
                if (!crcDataFlag) {
                    LOG.log(WARN, ErrorMessage.ID3_TAG_CRC_FLAG_SET_INCORRECTLY, loggingFilename);
                }
                //2nd Flag Byte (not used)
                buffer.readByte();
                //Take padding size of size to be read
                paddingSize = buffer.readInt();
                if (paddingSize > 0) {
                    LOG.log(DEBUG, ErrorMessage.ID3_TAG_PADDING_SIZE, loggingFilename, paddingSize);
                }
                //CRC Data
                crc32 = buffer.readInt();
                LOG.log(DEBUG, ErrorMessage.ID3_TAG_CRC_SIZE, loggingFilename, crc32);
            } else {
                //Extended header size is only allowed to be six or ten bytes so this is invalid but instead
                //of giving up lets guess its six bytes and carry on and see if we can read file ok
                LOG.log(WARN, ErrorMessage.ID3_EXTENDED_HEADER_SIZE_INVALID, loggingFilename, extendedHeaderSize);
                throw new InvalidTagHeaderException(String.format(Locale.getDefault(),
                        ErrorMessage.ID3_EXTENDED_HEADER_SIZE_INVALID,
                        loggingFilename,
                        extendedHeaderSize));
            }
            return extendedHeaderSize;
        } catch (EOFException e) {
            throw new InvalidTagHeaderException(e);
        }
    }

    public void read(ByteBuffer buffer) throws TagException {
        int size;
        if (!seek(buffer)) {
            throw new TagNotFoundException(getIdentifier() + " tag not found");
        }
        LOG.log(DEBUG, "%s:Reading ID3v23 tag", loggingFilename);

        readHeaderFlags(buffer.get());

        // Read the size, this is size of tag not including the tag header
        size = ID3SyncSafeInteger.bufferToValue(buffer);
        LOG.log(DEBUG, ErrorMessage.ID_TAG_SIZE, loggingFilename, size);

        //Extended Header
        if (extended) {
            readExtendedHeader(buffer);
        }

        //Slice Buffer, so position markers tally with size (i.e do not include tagHeader)
        ByteBuffer bufferWithoutHeader = buffer.slice();
        //We need to synchronize the buffer
        if (isUnsynchronized()) {
            bufferWithoutHeader = ID3Unsynchronization.synchronize(bufferWithoutHeader);
        }

        readFrames(bufferWithoutHeader, size);
        LOG.log(DEBUG, "%s:Loaded Frames,there are:%s", loggingFilename, frameMap.keySet().size());
    }

    private void read(Buffer buffer, Id3v2Header header, boolean ignoreArtwork) throws TagException {
        try {
            readHeaderFlags(header.getFlags());

            int size = header.getTagSize();
            LOG.log(DEBUG, ErrorMessage.ID_TAG_SIZE, loggingFilename, size);

            if (extended) {
                readExtendedHeader(buffer);
            }

            Buffer bufferWithoutHeader = buffer;
            //We need to synchronize the buffer
            if (isUnsynchronized()) {
                bufferWithoutHeader = Id3SynchronizingSink.synchronizeBuffer(buffer);
            }

            readFrames(bufferWithoutHeader, size, ignoreArtwork);
            LOG.log(DEBUG, "%s:Loaded Frames,there are:%s", loggingFilename, frameMap.keySet().size());
        } catch (IOException e) {
            throw new TagNotFoundException(getIdentifier() + " error reading tag", e);
        }
    }

    /**
     * Read the frames
     * <p>
     * Read from byteBuffer up to size
     */
    private void readFrames(ByteBuffer byteBuffer, int size) {
        //Now start looking for frames
        ID3v23Frame next;
        ensureFrameMapsAndClear();


        //Read the size from the Tag Header
        fileReadSize = size;
        LOG.log(TRACE, "%s:Start of frame body at:%s,frames data size is:", loggingFilename, byteBuffer.position(), size);

        // Read the frames until got to up to the size as specified in header or until
        // we hit an invalid frame identifier or padding
        while (byteBuffer.position() < size) {
            String id;
            try {
                //Read Frame
                int posBeforeRead = byteBuffer.position();
                LOG.log(DEBUG, loggingFilename + ":Looking for next frame at:" + posBeforeRead);
                next = new ID3v23Frame(byteBuffer, loggingFilename);
                id = next.getIdentifier();
                LOG.log(DEBUG, loggingFilename + ":Found " + id + " at frame at:" + posBeforeRead);
                loadFrameIntoMap(id, next);
            }
            //Found Padding, no more frames
            catch (PaddingException ex) {
                LOG.log(DEBUG, loggingFilename + ":Found padding starting at:" + byteBuffer.position());
                break;
            }
            //Found Empty Frame, log it - empty frames should not exist
            catch (EmptyFrameException ex) {
                LOG.log(WARN, loggingFilename + ":Empty Frame:" + ex.getMessage());
                emptyFrameBytes += ID3v23Frame.FRAME_HEADER_SIZE;
            } catch (InvalidFrameIdentifierException ifie) {
                LOG.log(WARN, loggingFilename + ":Invalid Frame Identifier:" + ifie.getMessage());
                invalidFrames++;
                //Don't try and find any more frames
                break;
            }
            //Problem trying to find frame, often just occurs because frameHeader includes padding
            //and we have reached padding
            catch (InvalidFrameException ife) {
                LOG.log(WARN, loggingFilename + ":Invalid Frame:" + ife.getMessage());
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
        LOG.log(TRACE, "Frame data is size:%s", size);

        // Read the frames until got to up to the size as specified in header or until
        // we hit an invalid frame identifier or padding
        while (buffer.size() > 0) {
            try {
                ID3v23Frame next = new ID3v23Frame(buffer, loggingFilename, ignoreArtwork);
                if (next.isArtworkFrame() && ignoreArtwork) {
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
            } catch (InvalidDataTypeException idete) {
                //Failed reading frame but may just have invalid data but correct length so lets carry on
                //in case we can read the next frame
                LOG.log(WARN, "%s:Corrupt Frame", loggingFilename, idete);
                invalidFrames++;
            } catch (IOException e) {
                LOG.log(WARN, "Unexpectedly reached end of frame" + e);
                invalidFrames++;
            } catch (@SuppressWarnings("TryWithIdenticalCatches") InvalidTagException e) {  // TODO: 1/25/17 get exceptions straightened out
                LOG.log(WARN, "%s:Corrupt Frame", loggingFilename, e);
                invalidFrames++;
            }
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
     * @param padding is the size of the padding portion of the tag
     * @param size    is the size of the body data
     * @return ByteBuffer
     */
    private ByteBuffer writeHeaderToBuffer(int padding, int size) throws IOException {
        // Flags,currently we never calculate the CRC
        // and if we dont calculate them cant keep orig values. Tags are not
        // experimental and we never createField extended header to keep things simple.
        extended = false;
        experimental = false;
        crcDataFlag = false;

        // Create Header Buffer,allocate maximum possible size for the header
        ByteBuffer headerBuffer = ByteBuffer.
                allocate(TAG_HEADER_LENGTH + TAG_EXT_HEADER_LENGTH +
                        TAG_EXT_HEADER_CRC_LENGTH);

        //TAGID
        headerBuffer.put(TAG_ID);

        //Major Version
        headerBuffer.put(getMajorVersion());

        //Minor Version
        headerBuffer.put(getRevision());

        //Flags
        byte flagsByte = 0;
        if (isUnsynchronized()) {
            flagsByte |= MASK_V23_UNSYNCHRONIZATION;
        }
        if (extended) {
            flagsByte |= MASK_V23_EXTENDED_HEADER;
        }
        if (experimental) {
            flagsByte |= MASK_V23_EXPERIMENTAL;
        }
        headerBuffer.put(flagsByte);

        //Additional Header Size,(for completeness we never actually write the extended header)
        int additionalHeaderSize = 0;
        if (extended) {
            additionalHeaderSize += TAG_EXT_HEADER_LENGTH;
            if (crcDataFlag) {
                additionalHeaderSize += TAG_EXT_HEADER_CRC_LENGTH;
            }
        }

        //Size As Recorded in Header, don't include the main header length
        headerBuffer.put(ID3SyncSafeInteger.valueToBuffer(padding + size + additionalHeaderSize));

        //Write Extended Header
        if (extended) {
            byte extFlagsByte1 = 0;
            byte extFlagsByte2 = 0;

            //Contains CRCData
            if (crcDataFlag) {
                headerBuffer.putInt(TAG_EXT_HEADER_DATA_LENGTH + TAG_EXT_HEADER_CRC_LENGTH);
                extFlagsByte1 |= MASK_V23_CRC_DATA_PRESENT;
                headerBuffer.put(extFlagsByte1);
                headerBuffer.put(extFlagsByte2);
                headerBuffer.putInt(paddingSize);
                headerBuffer.putInt(crc32);
            }
            //Just extended Header
            else {
                headerBuffer.putInt(TAG_EXT_HEADER_DATA_LENGTH);
                headerBuffer.put(extFlagsByte1);
                headerBuffer.put(extFlagsByte2);
                //Newly Calculated Padding As Recorded in Extended Header
                headerBuffer.putInt(padding);
            }
        }

        headerBuffer.flip();
        return headerBuffer;
    }

    boolean isUnsynchronized() {
        return unsynchronization;
    }

    public TagField createField(ID3v23FieldKey id3Key, String value) throws IllegalArgumentException, FieldDataInvalidException {
        checkArgNotNull(id3Key);
        checkArgNotNullOrEmpty(value);
        return doCreateTagField(new FrameAndSubId(null, id3Key.getFrameId(), id3Key.getSubId()), value);
    }

    public String getFirst(ID3v23FieldKey id3v23FieldKey) throws IllegalArgumentException, UnsupportedFieldException {
        checkArgNotNull(id3v23FieldKey);
        FieldKey genericKey = ID3v23Frames.getInstanceOf().getGenericKeyFromId3(id3v23FieldKey);
        return genericKey != null
                ? getFirst(genericKey)
                : doGetValueAtIndex(new FrameAndSubId(null, id3v23FieldKey.getFrameId(), id3v23FieldKey.getSubId()), 0);
    }

    public void deleteField(ID3v23FieldKey id3v23FieldKey) throws IllegalArgumentException {
        checkArgNotNull(id3v23FieldKey);
        doDeleteTagField(new FrameAndSubId(null, id3v23FieldKey.getFrameId(), id3v23FieldKey.getSubId()));
    }

    public Tag deleteField(String id) throws IllegalArgumentException, UnsupportedFieldException {
        checkArgNotNullOrEmpty(id, CANNOT_BE_NULL_OR_EMPTY, "id");
        doDeleteTagField(new FrameAndSubId(null, id, null));
        return this;
    }

    @Override
    public ImmutableSet<FieldKey> getSupportedFields() {
        return ID3v23Frames.getInstanceOf().getSupportedFields();
    }

    public int getPaddingSize() {
        return paddingSize;
    }

}
