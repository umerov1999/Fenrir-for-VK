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
import static ealvatag.utils.Check.AT_LEAST_ONE_REQUIRED;
import static ealvatag.utils.Check.CANNOT_BE_NULL;
import static ealvatag.utils.Check.CANNOT_BE_NULL_OR_EMPTY;
import static ealvatag.utils.Check.checkArgNotNull;
import static ealvatag.utils.Check.checkArgNotNullOrEmpty;
import static ealvatag.utils.Check.checkVarArg0NotNull;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;

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
import ealvatag.tag.id3.framebody.AbstractFrameBodyTextInfo;
import ealvatag.tag.id3.framebody.FrameBodyTCON;
import ealvatag.tag.id3.framebody.FrameBodyTDRC;
import ealvatag.tag.id3.valuepair.ImageFormats;
import okio.Buffer;

/**
 * Represents an ID3v2.2 tag.
 *
 * @author : Paul Taylor
 * @author : Eric Farng
 * @version $Id$
 */
public class ID3v22Tag extends AbstractID3v2Tag {
    public static final byte MAJOR_VERSION = 2;
    /**
     * Bit mask to indicate tag is Unsychronization
     */
    private static final int MASK_V22_UNSYNCHRONIZATION = FileConstants.BIT7;
    /**
     * Bit mask to indicate tag is compressed, although compression is not
     * actually defined in v22 so just ignored
     */
    private static final int MASK_V22_COMPRESSION = FileConstants.BIT6;
    private static final byte RELEASE = 2;
    private static final byte REVISION = 0;
    private static final String TYPE_COMPRESSION = "compression";
    private static final String TYPE_UNSYNCHRONISATION = "unsyncronisation";
    private static final JLogger LOG = JLoggers.get(ID3v22Tag.class, EalvaTagLog.MARKER);
    /**
     * The tag is compressed, although no compression scheme is defined in ID3v22
     */
    protected boolean compression;
    /**
     * If set all frames in the tag uses unsynchronisation
     */
    private boolean unsynchronization;

    /**
     * Creates a new empty ID3v2_2 tag.
     */
    public ID3v22Tag() {
        ensureFrameMapsAndClear();
    }

    /**
     * Copy Constructor, creates a new ID3v2_2 Tag based on another ID3v2_2 Tag
     */
    public ID3v22Tag(ID3v22Tag copyObject) {
        LOG.log(DEBUG, "Creating tag from another tag of same type");
        copyPrimitives(copyObject);
        copyFrames(copyObject);
    }

    /**
     * Constructs a new tag based upon another tag of different version/type
     */
    public ID3v22Tag(BaseID3Tag mp3tag) {
        ensureFrameMapsAndClear();
        LOG.log(DEBUG, "Creating tag from a tag of a different version");
        //Default Superclass constructor does nothing
        if (mp3tag != null) {
            ID3v24Tag convertedTag;
            //Should use the copy constructor instead
            if ((!(mp3tag instanceof ID3v23Tag)) && (mp3tag instanceof ID3v22Tag)) {
                throw new UnsupportedOperationException("Copy Constructor not called. Please type cast the argument");
            }
            //If v2.4 can getFields variables from this
            else if (mp3tag instanceof ID3v24Tag) {
                convertedTag = (ID3v24Tag) mp3tag;
            }
            //Any tag (e.g lyrics3 and idv1.1,idv2.3 can be converted to id32.4 so do that
            //to simplify things
            else {
                convertedTag = new ID3v24Tag(mp3tag);
            }
            setLoggingFilename(convertedTag.loggingFilename);
            //Set the primitive types specific to v2_2.
            copyPrimitives(convertedTag);
            //Set v2.2 Frames
            copyFrames(convertedTag);
            LOG.log(DEBUG, "Created tag from a tag of a different version");
        }
    }

    /**
     * Creates a new ID3v2_2 datatype.
     */
    public ID3v22Tag(ByteBuffer buffer, String loggingFilename) throws TagException {
        setLoggingFilename(loggingFilename);
        read(buffer);
    }

    public ID3v22Tag(Buffer buffer, Id3v2Header header, String loggingFilename, boolean ignoreArtwork) throws TagException {
        setLoggingFilename(loggingFilename);
        read(buffer, header, ignoreArtwork);
    }

    /**
     * Copy primitives applicable to v2.2
     */
    protected void copyPrimitives(AbstractID3v2Tag copyObj) {
        LOG.log(DEBUG, "Copying primitives");
        super.copyPrimitives(copyObj);

        //Set the primitive types specific to v2_2.
        if (copyObj instanceof ID3v22Tag) {
            ID3v22Tag copyObject = (ID3v22Tag) copyObj;
            compression = copyObject.compression;
            unsynchronization = copyObject.unsynchronization;
        } else if (copyObj instanceof ID3v23Tag) {
            ID3v23Tag copyObject = (ID3v23Tag) copyObj;
            compression = copyObject.compression;
            unsynchronization = copyObject.unsynchronization;
        } else if (copyObj instanceof ID3v24Tag) {
            ID3v24Tag copyObject = (ID3v24Tag) copyObj;
            compression = false;
            unsynchronization = copyObject.unsynchronization;
        }
    }

    @Override
    public void addFrame(AbstractID3v2Frame frame) {
        try {
            if (frame instanceof ID3v22Frame) {
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
            FrameBodyTDRC tmpBody = (FrameBodyTDRC) frame.getBody();
            ID3v22Frame newFrame;
            if (tmpBody.getYear().length() != 0) {
                //Create Year frame (v2.2 id,but uses v2.3 body)
                newFrame = new ID3v22Frame(ID3v22Frames.FRAME_ID_V2_TYER);
                ((AbstractFrameBodyTextInfo) newFrame.getBody()).setText(tmpBody.getYear());
                frames.add(newFrame);
            }
            if (tmpBody.getTime().length() != 0) {
                //Create Time frame (v2.2 id,but uses v2.3 body)
                newFrame = new ID3v22Frame(ID3v22Frames.FRAME_ID_V2_TIME);
                ((AbstractFrameBodyTextInfo) newFrame.getBody()).setText(tmpBody.getTime());
                frames.add(newFrame);
            }
        } else {
            frames.add(new ID3v22Frame(frame));
        }
        return frames;
    }

    protected ID3Frames getID3Frames() {
        return ID3v22Frames.getInstanceOf();
    }

    public List<String> getAll(FieldKey genericKey) throws IllegalArgumentException, UnsupportedFieldException {
        if (genericKey == FieldKey.GENRE) {
            List<TagField> fields = getFields(genericKey);
            List<String> convertedGenres = new ArrayList<>();
            if (fields != null && fields.size() > 0) {
                AbstractID3v2Frame frame = (AbstractID3v2Frame) fields.get(0);
                FrameBodyTCON body = (FrameBodyTCON) frame.getBody();

                for (String next : body.getValues()) {
                    convertedGenres.add(FrameBodyTCON.convertID3v22GenreToGeneric(next));
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
                return Optional.of(FrameBodyTCON.convertID3v22GenreToGeneric(body.getValues().get(index)));
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

    public TagField createField(FieldKey genericKey, String... values) throws IllegalArgumentException,
            UnsupportedFieldException,
            FieldDataInvalidException {
        checkArgNotNull(genericKey, CANNOT_BE_NULL, "generickey");
        if (genericKey == FieldKey.GENRE) {
            String value = checkVarArg0NotNull(values, AT_LEAST_ONE_REQUIRED, "values");
            FrameAndSubId formatKey = getFrameAndSubIdFromGenericKey(genericKey);
            AbstractID3v2Frame frame = createFrame(formatKey.getFrameId());
            FrameBodyTCON framebody = (FrameBodyTCON) frame.getBody();
            framebody.setV23Format();
            framebody.setText(FrameBodyTCON.convertGenericToID3v22Genre(value));
            return frame;
        } else {
            return super.createField(genericKey, values);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long write(File file, long audioStartLocation) throws IOException {
        setLoggingFilename(file.getName());
        LOG.log(DEBUG, "Writing tag to file:%s", loggingFilename);

        // Write Body Buffer
        byte[] bodyByteBuffer = writeFramesToBuffer().toByteArray();

        // Unsynchronize if option enabled and unsync required
        unsynchronization = TagOptionSingleton.getInstance().isUnsyncTags() &&
                ID3Unsynchronization.requiresUnsynchronization(bodyByteBuffer);
        if (isUnsynchronization()) {
            bodyByteBuffer = ID3Unsynchronization.unsynchronize(bodyByteBuffer);
            LOG.log(DEBUG, "%s:bodybytebuffer:sizeafterunsynchronisation:%d", loggingFilename, bodyByteBuffer.length);
        }

        int sizeIncPadding = calculateTagSize(bodyByteBuffer.length + TAG_HEADER_LENGTH, (int) audioStartLocation);
        int padding = sizeIncPadding - (bodyByteBuffer.length + TAG_HEADER_LENGTH);
        LOG.log(DEBUG, "%s:Current audiostart:%d", loggingFilename, audioStartLocation);
        LOG.log(DEBUG, "%s:Size including padding:%d", loggingFilename, sizeIncPadding);
        LOG.log(DEBUG, "%s:Padding:%d", loggingFilename, padding);

        ByteBuffer headerBuffer = writeHeaderToBuffer(padding, bodyByteBuffer.length);
        writeBufferToFile(file, headerBuffer, bodyByteBuffer, padding, sizeIncPadding, audioStartLocation);
        return sizeIncPadding;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(WritableByteChannel channel, int currentTagSize) throws IOException {
        LOG.log(DEBUG, "%s Writing tag to channel", loggingFilename);

        byte[] bodyByteBuffer = writeFramesToBuffer().toByteArray();
        LOG.log(DEBUG, "%s:bodybytebuffer:sizebeforeunsynchronisation:%d", loggingFilename, bodyByteBuffer.length);

        //Unsynchronize if option enabled and unsync required
        unsynchronization = TagOptionSingleton.getInstance().isUnsyncTags() &&
                ID3Unsynchronization.requiresUnsynchronization(bodyByteBuffer);
        if (isUnsynchronization()) {
            bodyByteBuffer = ID3Unsynchronization.unsynchronize(bodyByteBuffer);
            LOG.log(DEBUG, "%s:bodybytebuffer:sizeafterunsynchronisation:%d", loggingFilename, bodyByteBuffer.length);
        }

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

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ID3v22Tag)) {
            return false;
        }
        ID3v22Tag object = (ID3v22Tag) obj;
        return compression == object.compression && unsynchronization == object.unsynchronization && super.equals(obj);
    }

    protected void loadFrameIntoMap(String frameId, AbstractID3v2Frame next) {
        if (next.getBody() instanceof FrameBodyTCON) {
            ((FrameBodyTCON) next.getBody()).setV23Format();
        }
        super.loadFrameIntoMap(frameId, next);
    }

    /**
     * Return frame size based upon the sizes of the frames rather than the size
     * including padding recorded in the tag header
     *
     * @return size
     */
    public int getSize() {
        int size = TAG_HEADER_LENGTH;
        size += super.getSize();
        return size;
    }

    /**
     * @return comparator used to order frames in preffrred order for writing to file so that most important frames are written first.
     */
    public Comparator<String> getPreferredFrameOrderComparator() {
        return ID3v22PreferredFrameOrderComparator.getInstanceof();
    }

    public void createStructure() {
        MP3File.getStructureFormatter().openHeadingElement(TYPE_TAG, getIdentifier());

        createStructureHeader();

        //Header
        MP3File.getStructureFormatter().openHeadingElement(TYPE_HEADER, "");
        MP3File.getStructureFormatter().addElement(TYPE_COMPRESSION, compression);
        MP3File.getStructureFormatter().addElement(TYPE_UNSYNCHRONISATION, unsynchronization);
        MP3File.getStructureFormatter().closeHeadingElement(TYPE_HEADER);
        //Body
        createStructureBody();

        MP3File.getStructureFormatter().closeHeadingElement(TYPE_TAG);
    }

    /**
     * Create Frame
     *
     * @param id frameid
     */
    public ID3v22Frame createFrame(String id) {
        return new ID3v22Frame(id);
    }

    protected FrameAndSubId getFrameAndSubIdFromGenericKey(FieldKey genericKey) throws UnsupportedFieldException {
        ID3v22FieldKey id3v22FieldKey = ID3v22Frames.getInstanceOf().getId3KeyFromGenericKey(genericKey);
        if (id3v22FieldKey == null) {
            throw new UnsupportedFieldException(genericKey.name());
        }
        return new FrameAndSubId(genericKey, id3v22FieldKey.getFrameId(), id3v22FieldKey.getSubId());
    }

    /**
     * @return an identifier of the tag type
     */
    public String getIdentifier() {
        return "ID3v2_2.20";
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
        unsynchronization = (flags & MASK_V22_UNSYNCHRONIZATION) != 0;
        compression = (flags & MASK_V22_COMPRESSION) != 0;

        if (unsynchronization) {
            LOG.log(DEBUG, ErrorMessage.ID3_TAG_UNSYNCHRONIZED, loggingFilename);
        }

        if (compression) {
            LOG.log(DEBUG, ErrorMessage.ID3_TAG_COMPRESSED, loggingFilename);
        }

        //Not allowable/Unknown Flags
        if ((flags & FileConstants.BIT5) != 0) {
            LOG.log(WARN, ErrorMessage.ID3_INVALID_OR_UNKNOWN_FLAG_SET, loggingFilename, FileConstants.BIT5);
        }
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
            LOG.log(WARN, ErrorMessage.ID3_INVALID_OR_UNKNOWN_FLAG_SET, loggingFilename, FileConstants.BIT3);
        }
    }

    @Override
    public void read(ByteBuffer byteBuffer) throws TagException {
        int size;
        if (!seek(byteBuffer)) {
            throw new TagNotFoundException("ID3v2.20 tag not found");
        }
        LOG.log(DEBUG, "%s:Reading tag from file", loggingFilename);

        //Read the flags
        readHeaderFlags(byteBuffer.get());

        // Read the size
        size = ID3SyncSafeInteger.bufferToValue(byteBuffer);

        //Slice Buffer, so position markers tally with size (i.e do not include tagheader)
        ByteBuffer bufferWithoutHeader = byteBuffer.slice();

        //We need to synchronize the buffer
        if (unsynchronization) {
            bufferWithoutHeader = ID3Unsynchronization.synchronize(bufferWithoutHeader);
        }
        readFrames(bufferWithoutHeader, size);
        LOG.log(DEBUG, "%s:Loaded Frames,there are:%s", loggingFilename, frameMap.keySet().size());
    }

    public void read(Buffer buffer, Id3v2Header header, boolean ignoreArtwork) throws TagException {
        try {
            readHeaderFlags(header.getFlags());

            int size = header.getTagSize();

            Buffer bufferWithoutHeader = buffer;
            //We need to synchronize the buffer
            if (unsynchronization) {
                bufferWithoutHeader = Id3SynchronizingSink.synchronizeBuffer(buffer);
            }

            readFrames(bufferWithoutHeader, size, ignoreArtwork);
            LOG.log(DEBUG, "%s:Loaded Frames,there are:%s", loggingFilename, frameMap.keySet().size());
        } catch (IOException e) {
            throw new TagNotFoundException(getIdentifier() + " error reading tag", e);
        }
    }

    private void readFrames(Buffer buffer, int size, boolean ignoreArtwork) {
        ensureFrameMapsAndClear();
        fileReadSize = size;
        LOG.log(TRACE, "Frame data is size:%s", size);

        // Read the frames until got to up to the size as specified in header or until
        // we hit an invalid frame identifier or padding
        while (buffer.size() > 0) {
            String logName = loggingFilename;
            try {
                ID3v22Frame next = new ID3v22Frame(buffer, logName, ignoreArtwork);
                if (next.isArtworkFrame() && ignoreArtwork) {
                    setReadOnly();
                } else {
                    loadFrameIntoMap(next.getIdentifier(), next);
                }
            } catch (PaddingException ex) {
                //Found Padding, no more frames
                LOG.log(DEBUG, "Found padding with %s remaining. %s", buffer.size(), logName);
                break;
            } catch (EmptyFrameException ex) {
                //Found Empty Frame, log it - empty frames should not exist
                LOG.log(WARN, "%s:Empty Frame", logName, ex);
                emptyFrameBytes += ID3v23Frame.FRAME_HEADER_SIZE;
            } catch (InvalidFrameIdentifierException ifie) {
                LOG.log(WARN, "%s:Invalid Frame Identifier", logName, ifie);
                invalidFrames++;
                //Don't try and find any more frames
                break;
            } catch (InvalidFrameException ife) {
                //Problem trying to find frame, often just occurs because frameHeader includes padding
                //and we have reached padding
                LOG.log(WARN, "%s:Invalid Frame:", logName, ife);
                invalidFrames++;
                //Don't try and find any more frames
                break;
            } catch (InvalidDataTypeException idete) {
                //Failed reading frame but may just have invalid data but correct length so lets carry on
                //in case we can read the next frame
                LOG.log(WARN, "%s:Corrupt Frame", logName, idete);
                invalidFrames++;
            } catch (IOException e) {
                LOG.log(WARN, "%s:Unexpectedly reached end of frame", logName, e);
                invalidFrames++;
            } catch (@SuppressWarnings("TryWithIdenticalCatches") InvalidTagException e) {  // TODO: 1/25/17 get exceptions straightened out
                LOG.log(WARN, "%s:Corrupt Frame", logName, e);
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

    private void readFrames(ByteBuffer byteBuffer, int size) {
        //Now start looking for frames
        ID3v22Frame next;
        ensureFrameMapsAndClear();

        //Read the size from the Tag Header
        fileReadSize = size;
        LOG.log(TRACE, "%s:Start of frame body at:%s,frames sizes and padding is:%s", loggingFilename, byteBuffer.position(), size);
        /* todo not done yet. Read the first Frame, there seems to be quite a
         ** common case of extra data being between the tag header and the first
         ** frame so should we allow for this when reading first frame, but not subsequent frames
         */
        // Read the frames until got to upto the size as specified in header
        while (byteBuffer.position() < size) {
            try {
                //Read Frame
                LOG.log(TRACE, "%s:looking for next frame at:%s", loggingFilename, byteBuffer.position());
                next = new ID3v22Frame(byteBuffer, loggingFilename);
                String id = next.getIdentifier();
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
                emptyFrameBytes += ID3v22Frame.FRAME_HEADER_SIZE;
            } catch (InvalidFrameIdentifierException ifie) {
                LOG.log(DEBUG, "%s:Invalid Frame Identifier ", loggingFilename, ifie);
                invalidFrames++;
                //Dont try and find any more frames
                break;
            }
            //Problem trying to find frame
            catch (InvalidFrameException ife) {
                LOG.log(WARN, "%s:Invalid Frame:", loggingFilename, ife);
                invalidFrames++;
                //Dont try and find any more frames
                break;
            }
            //Failed reading frame but may just have invalid data but correct length so lets carry on
            //in case we can read the next frame
            catch (InvalidDataTypeException idete) {
                LOG.log(WARN, "%s:Corrupt Frame", loggingFilename, idete);
                invalidFrames++;
            }
        }
    }

//  /**
//   * This is used when we need to translate a single frame into multiple frames,
//   * currently required for TDRC frames.
//   *
//   */
//  //TODO will overwrite any existing TYER or TIME frame, do we ever want multiples of these
//  protected void translateFrame(AbstractID3v2Frame frame) {
//    FrameBodyTDRC tmpBody = (FrameBodyTDRC)frame.getBody();
//    ID3v22Frame newFrame;
//    if (tmpBody.getYear().length() != 0) {
//      //Create Year frame (v2.2 id,but uses v2.3 body)
//      newFrame = new ID3v22Frame(ID3v22Frames.FRAME_ID_V2_TYER);
//      ((AbstractFrameBodyTextInfo)newFrame.getBody()).setText(tmpBody.getYear());
//      frameMap.put(newFrame.getIdentifier(), newFrame);
//    }
//    if (tmpBody.getTime().length() != 0) {
//      //Create Time frame (v2.2 id,but uses v2.3 body)
//      newFrame = new ID3v22Frame(ID3v22Frames.FRAME_ID_V2_TIME);
//      ((AbstractFrameBodyTextInfo)newFrame.getBody()).setText(tmpBody.getTime());
//      frameMap.put(newFrame.getIdentifier(), newFrame);
//    }
//  }

    private ByteBuffer writeHeaderToBuffer(int padding, int size) throws IOException {
        compression = false;

        //Create Header Buffer
        ByteBuffer headerBuffer = ByteBuffer.allocate(TAG_HEADER_LENGTH);

        //TAGID
        headerBuffer.put(TAG_ID);
        //Major Version
        headerBuffer.put(getMajorVersion());
        //Minor Version
        headerBuffer.put(getRevision());

        //Flags
        byte flags = (byte) 0;
        if (unsynchronization) {
            flags |= (byte) MASK_V22_UNSYNCHRONIZATION;
        }
        if (compression) {
            flags |= (byte) MASK_V22_COMPRESSION;
        }

        headerBuffer.put(flags);

        //Size As Recorded in Header, don't include the main header length
        headerBuffer.put(ID3SyncSafeInteger.valueToBuffer(padding + size));
        headerBuffer.flip();
        return headerBuffer;
    }

    /**
     * @return is tag unsynchronized
     */
    public boolean isUnsynchronization() {
        return unsynchronization;
    }

    /**
     * @return is tag compressed
     */
    public boolean isCompression() {
        return compression;
    }

    /**
     * Create Frame for Id3 Key
     * <p>
     * Only textual data supported at the moment, should only be used with frames that
     * support a simple string argument.
     */
    public TagField createField(ID3v22FieldKey id3Key, String value) throws IllegalArgumentException, FieldDataInvalidException {
        checkArgNotNull(id3Key);
        checkArgNotNullOrEmpty(value);
        return doCreateTagField(new FrameAndSubId(null, id3Key.getFrameId(), id3Key.getSubId()), value);
    }

    public String getFirst(ID3v22FieldKey id3v22FieldKey) throws IllegalArgumentException, UnsupportedFieldException {
        checkArgNotNull(id3v22FieldKey);
        FieldKey genericKey = ID3v22Frames.getInstanceOf().getGenericKeyFromId3(id3v22FieldKey);
        if (genericKey != null) {
            return getFirst(genericKey);
        } else {
            FrameAndSubId frameAndSubId = new FrameAndSubId(null, id3v22FieldKey.getFrameId(), id3v22FieldKey.getSubId());
            return doGetValueAtIndex(frameAndSubId, 0);
        }
    }

    public void deleteField(ID3v22FieldKey id3v22FieldKey) throws IllegalArgumentException {
        checkArgNotNull(id3v22FieldKey);
        doDeleteTagField(new FrameAndSubId(null, id3v22FieldKey.getFrameId(), id3v22FieldKey.getSubId()));
    }

    /**
     * Delete fields with this (frame) id
     */
    public Tag deleteField(String id) throws IllegalArgumentException, UnsupportedFieldException {
        checkArgNotNullOrEmpty(id, CANNOT_BE_NULL_OR_EMPTY, "id");
        doDeleteTagField(new FrameAndSubId(null, id, null));
        return this;
    }

    @Override
    public ImmutableSet<FieldKey> getSupportedFields() {
        return ID3v22Frames.getInstanceOf().getSupportedFields();
    }

    @Override
    protected String getArtworkMimeTypeDataType() {
        return DataTypes.OBJ_IMAGE_FORMAT;
    }

    @Override
    protected String getArtworkMimeType(String mimeType) {
        return ImageFormats.getFormatForMimeType(mimeType);
    }
}
