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
import static ealvatag.logging.EalvaTagLog.LogLevel.INFO;
import static ealvatag.logging.EalvaTagLog.LogLevel.WARN;
import static ealvatag.tag.id3.ID3SyncSafeInteger.INTEGRAL_SIZE;

import com.google.common.base.Strings;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.NoSuchElementException;

import ealvatag.audio.mp3.MP3File;
import ealvatag.logging.EalvaTagLog;
import ealvatag.logging.EalvaTagLog.JLogger;
import ealvatag.logging.EalvaTagLog.JLoggers;
import ealvatag.logging.ErrorMessage;
import ealvatag.logging.Hex;
import ealvatag.tag.EmptyFrameException;
import ealvatag.tag.InvalidDataTypeException;
import ealvatag.tag.InvalidFrameException;
import ealvatag.tag.InvalidFrameIdentifierException;
import ealvatag.tag.InvalidTagException;
import ealvatag.tag.TagOptionSingleton;
import ealvatag.tag.datatype.Lyrics3Line;
import ealvatag.tag.id3.framebody.AbstractID3v2FrameBody;
import ealvatag.tag.id3.framebody.FrameBodyCOMM;
import ealvatag.tag.id3.framebody.FrameBodyDeprecated;
import ealvatag.tag.id3.framebody.FrameBodySYLT;
import ealvatag.tag.id3.framebody.FrameBodyTALB;
import ealvatag.tag.id3.framebody.FrameBodyTCOM;
import ealvatag.tag.id3.framebody.FrameBodyTIT2;
import ealvatag.tag.id3.framebody.FrameBodyTMOO;
import ealvatag.tag.id3.framebody.FrameBodyTPE1;
import ealvatag.tag.id3.framebody.FrameBodyTXXX;
import ealvatag.tag.id3.framebody.FrameBodyUSLT;
import ealvatag.tag.id3.framebody.FrameBodyUnsupported;
import ealvatag.tag.id3.framebody.ID3v24FrameBody;
import ealvatag.tag.id3.valuepair.TextEncoding;
import ealvatag.tag.lyrics3.FieldFrameBodyAUT;
import ealvatag.tag.lyrics3.FieldFrameBodyEAL;
import ealvatag.tag.lyrics3.FieldFrameBodyEAR;
import ealvatag.tag.lyrics3.FieldFrameBodyETT;
import ealvatag.tag.lyrics3.FieldFrameBodyINF;
import ealvatag.tag.lyrics3.FieldFrameBodyLYR;
import ealvatag.tag.lyrics3.Lyrics3v2Field;
import ealvatag.utils.Characters;
import ealvatag.utils.EqualsUtil;
import ealvatag.utils.StandardCharsets;
import okio.Buffer;

/**
 * Represents an ID3v2.4 frame.
 *
 * @author : Paul Taylor
 * @author : Eric Farng
 * @version $Id$
 */
@SuppressWarnings("Duplicates")
public class ID3v24Frame extends AbstractID3v2Frame {
    private static final int FRAME_DATA_LENGTH_SIZE = 4;
    private static final int FRAME_ID_SIZE = 4;
    private static final int FRAME_FLAGS_SIZE = 2;
    private static final int FRAME_SIZE_SIZE = 4;
    private static final int FRAME_ENCRYPTION_INDICATOR_SIZE = 1;
    private static final int FRAME_GROUPING_INDICATOR_SIZE = 1;
    private static final int FRAME_HEADER_SIZE = FRAME_ID_SIZE + FRAME_SIZE_SIZE + FRAME_FLAGS_SIZE;
    private static final JLogger LOG = JLoggers.get(ID3v24Frame.class, EalvaTagLog.MARKER);
    /**
     * If the frame is encrypted then the encryption method is stored in this byte
     */
    private int encryptionMethod;

    /**
     * If the frame belongs in a group with other frames then the group identifier byte is stored
     */
    private int groupIdentifier;

    public ID3v24Frame() {
    }

    /**
     * Creates a new ID3v2_4Frame of type identifier. An empty
     * body of the correct type will be automatically created.
     * This constructor should be used when wish to create a new
     * frame from scratch using user input
     *
     * @param identifier defines the type of body to be created
     */
    public ID3v24Frame(String identifier) {
        //Super Constructor creates a frame with empty body of type specified
        super(identifier);
        statusFlags = new StatusFlags();
        encodingFlags = new EncodingFlags();

    }

    /**
     * Copy Constructor:Creates a new ID3v24 frame datatype based on another frame.
     */
    public ID3v24Frame(ID3v24Frame frame) {
        super(frame);
        statusFlags = new StatusFlags(frame.getStatusFlags().getOriginalFlags());
        encodingFlags = new EncodingFlags(frame.getEncodingFlags().getFlags());
    }

    /**
     * Partially construct ID3v24 Frame form an IS3v23Frame
     * <p>
     * Used for Special Cases
     */
    protected ID3v24Frame(ID3v23Frame frame, String identifier) throws InvalidFrameException {
        this.identifier = identifier;
        statusFlags = new StatusFlags((ID3v23Frame.StatusFlags) frame.getStatusFlags());
        encodingFlags = new EncodingFlags(frame.getEncodingFlags().getFlags());
    }


    /**
     * Creates a new ID3v24 frame datatype based on another frame of different version
     * Converts the framebody to the equivalent v24 framebody or to UnsupportedFrameBody if identifier
     * is unknown.
     *
     * @param frame to construct a new frame from
     */
    public ID3v24Frame(AbstractID3v2Frame frame) throws InvalidFrameException {
        //Should not be called
        if ((frame instanceof ID3v24Frame)) {
            throw new UnsupportedOperationException("Copy Constructor not called. Please type cast the argument");
        } else if (frame instanceof ID3v23Frame) {
            statusFlags = new StatusFlags((ID3v23Frame.StatusFlags) frame.getStatusFlags());
            encodingFlags = new EncodingFlags(frame.getEncodingFlags().getFlags());
        } else if (frame instanceof ID3v22Frame) {
            statusFlags = new StatusFlags();
            encodingFlags = new EncodingFlags();
        }

        // Convert Identifier. If the id was a known id for the original
        // version we should be able to convert it to an v24 frame, although it may mean minor
        // modification to the data. If it was not recognised originally it should remain
        // unknown.
        if (frame instanceof ID3v23Frame) {
            createV24FrameFromV23Frame((ID3v23Frame) frame);
        } else if (frame instanceof ID3v22Frame) {
            ID3v23Frame v23Frame = new ID3v23Frame(frame);
            createV24FrameFromV23Frame(v23Frame);
        }
        frameBody.setHeader(this);
    }

    /**
     * Creates a new ID3v2_4Frame datatype based on Lyrics3.
     */
    public ID3v24Frame(Lyrics3v2Field field) throws InvalidTagException {
        String id = field.getIdentifier();
        String value;
        switch (id) {
            case "IND":
                throw new InvalidTagException("Cannot create ID3v2.40 frame from Lyrics3 indications field.");
            case "LYR":
                FieldFrameBodyLYR lyric = (FieldFrameBodyLYR) field.getBody();
                Lyrics3Line line;
                Iterator<Lyrics3Line> iterator = lyric.iterator();
                FrameBodySYLT sync;
                FrameBodyUSLT unsync;
                boolean hasTimeStamp = lyric.hasTimeStamp();
                // we'll create only one frame here.
                // if there is any timestamp at all, we will create a sync'ed frame.
                sync = new FrameBodySYLT((byte) 0, "ENG", (byte) 2, (byte) 1, "", new byte[0]);
                unsync = new FrameBodyUSLT((byte) 0, "ENG", "", "");
                while (iterator.hasNext()) {
                    line = iterator.next();
                    if (!hasTimeStamp) {
                        unsync.addLyric(line);
                    }
//                    else {
//                         sync.addLyric(line);
//                    }
                }
                if (hasTimeStamp) {
                    frameBody = sync;
                    frameBody.setHeader(this);
                } else {
                    frameBody = unsync;
                    frameBody.setHeader(this);
                }
                break;
            case "INF":
                value = ((FieldFrameBodyINF) field.getBody()).getAdditionalInformation();
                frameBody = new FrameBodyCOMM((byte) 0, "ENG", "", value);
                frameBody.setHeader(this);
                break;
            case "AUT":
                value = ((FieldFrameBodyAUT) field.getBody()).getAuthor();
                frameBody = new FrameBodyTCOM((byte) 0, value);
                frameBody.setHeader(this);
                break;
            case "EAL":
                value = ((FieldFrameBodyEAL) field.getBody()).getAlbum();
                frameBody = new FrameBodyTALB((byte) 0, value);
                frameBody.setHeader(this);
                break;
            case "EAR":
                value = ((FieldFrameBodyEAR) field.getBody()).getArtist();
                frameBody = new FrameBodyTPE1((byte) 0, value);
                frameBody.setHeader(this);
                break;
            case "ETT":
                value = ((FieldFrameBodyETT) field.getBody()).getTitle();
                frameBody = new FrameBodyTIT2((byte) 0, value);
                frameBody.setHeader(this);
                break;
            case "IMG":
                throw new InvalidTagException("Cannot create ID3v2.40 frame from Lyrics3 image field.");
            default:
                throw new InvalidTagException("Cannot caret ID3v2.40 frame from " + id + " Lyrics3 field");
        }
    }

    public ID3v24Frame(Buffer buffer, String loggingFilename, boolean ignoreArtwork) throws InvalidTagException, IOException {
        setLoggingFilename(loggingFilename);
        read(buffer, ignoreArtwork);
    }

    public ID3v24Frame(ByteBuffer byteBuffer, String loggingFilename)
            throws InvalidFrameException, InvalidDataTypeException {
        setLoggingFilename(loggingFilename);
        read(byteBuffer);
    }

    private void createV24FrameFromV23Frame(ID3v23Frame frame) throws InvalidFrameException {
        // Is it a straight conversion e.g TALB - TALB
        identifier = ID3Tags.convertFrameID23To24(frame.getIdentifier());
        LOG.log(DEBUG, "Creating V24frame from v23:%s:%s", frame.getIdentifier(), identifier == null ? "null" : identifier);


        //We cant convert unsupported bodies properly
        if (frame.getBody() instanceof FrameBodyUnsupported) {
            frameBody = new FrameBodyUnsupported((FrameBodyUnsupported) frame.getBody());
            frameBody.setHeader(this);
            identifier = frame.getIdentifier();
            LOG.log(DEBUG, "V3:UnsupportedBody:Orig id is:%s:New id is:%s", frame.getIdentifier(), identifier == null ? "null" : identifier);
        }//Simple Copy
        else if (identifier != null) {
            //Special Case
            if ((frame.getIdentifier().equals(ID3v23Frames.FRAME_ID_V3_USER_DEFINED_INFO)) &&
                    (((FrameBodyTXXX) frame.getBody()).getDescription().equals(FrameBodyTXXX.MOOD))) {
                frameBody = new FrameBodyTMOO((FrameBodyTXXX) frame.getBody());
                frameBody.setHeader(this);
                identifier = frameBody.getIdentifier();
            } else {
                LOG.log(DEBUG, "V3:Orig id is:%s:New id is:%s", frame.getIdentifier(), identifier);
                frameBody = (AbstractTagFrameBody) ID3Tags.copyObject(frame.getBody());
                frameBody.setHeader(this);
            }
        }
        // Is it a known v3 frame which needs forcing to v4 frame e.g. TYER - TDRC
        else if (ID3Tags.isID3v23FrameIdentifier(frame.getIdentifier())) {
            identifier = ID3Tags.forceFrameID23To24(frame.getIdentifier());
            if (identifier != null) {
                LOG.log(DEBUG, "V3:Orig id is:%s:New id is:%s", frame.getIdentifier(), identifier);
                frameBody = readBody(identifier, (AbstractID3v2FrameBody) frame.getBody());
                frameBody.setHeader(this);
            }
            // No mechanism exists to convert it to a v24 frame, e.g deprecated frame e.g TSIZ, so hold
            // as a deprecated frame consisting of an array of bytes*/
            else {
                frameBody = new FrameBodyDeprecated((AbstractID3v2FrameBody) frame.getBody());
                frameBody.setHeader(this);
                identifier = frame.getIdentifier();
                LOG.log(DEBUG, "V3:Deprecated:Orig id is:%s:New id is:%s", frame.getIdentifier(), identifier);
            }
        }
        // Unknown Frame e.g NCON or TDRL (because TDRL unknown to V23)
        else {
            frameBody = new FrameBodyUnsupported((FrameBodyUnsupported) frame.getBody());
            frameBody.setHeader(this);
            identifier = frame.getIdentifier();
            LOG.log(DEBUG, "V3:Unknown:Orig id is:%s:New id is:%s", frame.getIdentifier(), identifier);
        }
    }

    public void read(Buffer buffer, boolean ignoreArtwork) throws InvalidTagException, IOException {
        long sizeBeforeRead = buffer.size();
        String fileName = loggingFilename;
        try {
            String identifier = readIdentifier(buffer);

            if (!isValidID3v2FrameIdentifier(identifier)) {
                throw new InvalidFrameIdentifierException(fileName + ":" + identifier + ":is not a valid ID3v2.30 frame");
            }

            //Get the frame size, adjusted as necessary
            getFrameSize(buffer);

            //Read the flag bytes
            statusFlags = new StatusFlags(buffer.readByte());
            encodingFlags = new EncodingFlags(buffer.readByte());

            //Read extra bits appended to frame header for various encodings
            //These are not included in header size but are included in frame size but wont be read when we actually
            //try to read the frame body data
            int extraHeaderBytesCount = 0;
            int dataLengthSize = -1;
            if (((EncodingFlags) encodingFlags).isGrouping()) {
                extraHeaderBytesCount = FRAME_GROUPING_INDICATOR_SIZE;
                groupIdentifier = buffer.readByte();
            }

            if (((EncodingFlags) encodingFlags).isEncryption()) {
                //Read the Encryption byte, but do nothing with it
                extraHeaderBytesCount += FRAME_ENCRYPTION_INDICATOR_SIZE;
                encryptionMethod = buffer.readByte();
            }

            if (((EncodingFlags) encodingFlags).isDataLengthIndicator()) {
                //Read the sync safe size field
                dataLengthSize = ID3SyncSafeInteger.bufferToValue(buffer);
                extraHeaderBytesCount += FRAME_DATA_LENGTH_SIZE;
                LOG.log(DEBUG, "%s:Frame Size Is:%s Data length Size%s", fileName, frameSize, dataLengthSize);
            }

            //Work out the real size of the frameBody data
            int realFrameSize = frameSize - extraHeaderBytesCount;

            try {
                if (ignoreArtwork && AbstractID3v2Frame.isArtworkFrameId(identifier)) {
                    buffer.skip(realFrameSize);
                    frameBody = null;
                } else {
                    //Read the body data
                    Buffer frameBodyBuffer = buffer;
                    //Do we need to synchronize the frame body
                    int syncSize = realFrameSize;
                    if (((EncodingFlags) encodingFlags).isUnsynchronised()) {
                        //We only want to synchronize the buffer up to the end of this frame (remember this
                        //buffer contains the remainder of this tag not just this frame)
                        //Create Buffer that only contains the body of this frame rather than the remainder of tag
                        frameBodyBuffer = new Buffer();
                        buffer.read(frameBodyBuffer, realFrameSize);
                        frameBodyBuffer = Id3SynchronizingSink.synchronizeBuffer(frameBodyBuffer);
                        syncSize = (int) frameBodyBuffer.size();
                        LOG.log(DEBUG, "%s:Frame Size After Syncing is:%s", fileName, syncSize);
                    }


                    if (((EncodingFlags) encodingFlags).isCompression()) {
                        frameBodyBuffer = decompressPartOfBuffer(frameBodyBuffer, syncSize, dataLengthSize);
                        if (((EncodingFlags) encodingFlags).isEncryption()) {
                            frameBody = readEncryptedBody(identifier, frameBodyBuffer, dataLengthSize);
                        } else {
                            frameBody = readBody(identifier, frameBodyBuffer, dataLengthSize);
                        }
                    } else if (((EncodingFlags) encodingFlags).isEncryption()) {
                        frameBody = readEncryptedBody(identifier, frameBodyBuffer, syncSize);
                    } else {
                        frameBody = readBody(identifier, frameBodyBuffer, syncSize);
                    }
                    if (!(frameBody instanceof ID3v24FrameBody)) {
                        LOG.log(DEBUG, "%s:Converted frame body with:%s to deprecated framebody", fileName, identifier);
                        frameBody = new FrameBodyDeprecated((AbstractID3v2FrameBody) frameBody);
                    }
                }
            } finally {
                //Update position of main buffer, so no attempt is made to reread these bytes
                long desiredSize = sizeBeforeRead - realFrameSize;
                if (buffer.size() > desiredSize) {
                    buffer.skip(buffer.size() - desiredSize);
                }
            }
        } catch (RuntimeException e) {
            LOG.log(DEBUG, "Unexpected :%s - %s", Strings.nullToEmpty(identifier), fileName, e);
            throw new InvalidFrameException("Buffer:" + buffer.size() + " " + Strings.nullToEmpty(identifier) +
                    " not valid ID3v2.30 frame " + fileName,
                    e);
        }
    }

    private void getFrameSize(Buffer buffer) throws InvalidFrameException, EOFException {
        //Read frame size as syncsafe integer
        frameSize = ID3SyncSafeInteger.peekBufferToValue(buffer);

        if (frameSize < 0) {
            LOG.log(WARN, "%s:Invalid Frame size:%s", loggingFilename, identifier);
            throw new InvalidFrameException(identifier + " is invalid frame");
        } else if (frameSize == 0) {
            LOG.log(WARN, "%s:Empty Frame:%s", loggingFilename, identifier);
            //We dont process this frame or add to frame map because contains no useful information
            //Skip the two flag bytes so in correct position for subsequent frames
            // We peeked the frameSize, so move past it and 2 extra
            for (int i = 0; i < INTEGRAL_SIZE + 2; i++) {
                buffer.readByte();
            }
            throw new EmptyFrameException(identifier + " is empty frame");
        } else if (frameSize > (buffer.size() - FRAME_FLAGS_SIZE)) {
            LOG.log(WARN, "%s:Invalid Frame size larger than size before mp3 audio:%s", loggingFilename, identifier);
            throw new InvalidFrameException(identifier + " is invalid frame");
        }

        long tempSize = buffer.size();
        checkIfFrameSizeThatIsNotSyncSafe(buffer);
        if (buffer.size() != tempSize) {
            throw new RuntimeException("LIBRARY MISCONFIGURATION checkIfFrameSizeThatIsNotSyncSafe moved the buffer pointer");
        }
        // we have peeked everything, move the buffer ahead the frame size integral amount now
        buffer.skip(INTEGRAL_SIZE);

    }

    private void checkIfFrameSizeThatIsNotSyncSafe(Buffer buffer) throws InvalidFrameException {
        if (frameSize > ID3SyncSafeInteger.MAX_SAFE_SIZE) {
            // the buffer is still positioned directly before the frame size

            //Read as nonsync safe integer
            int nonSyncSafeFrameSize = peekBufferInt(buffer);

            //Is the frame size syncsafe, should always be BUT some encoders such as Itunes do not do it properly
            //so do an easy check now.
            boolean isNotSyncSafe = ID3SyncSafeInteger.peekIsBufferNotSyncSafe(buffer);

            if (isNotSyncSafe) {
                LOG.log(WARN, "%s:Frame size is NOT stored as a sync safe integer:%s", loggingFilename, identifier);

                //This will return a larger frame size so need to check against buffer size if too large then we are
                //buggered , give up
                if (nonSyncSafeFrameSize > (buffer.size() + INTEGRAL_SIZE)) {   // remember, we're keeping the buffer just before
                    // the frame size
                    LOG.log(WARN, "%s:Invalid Frame size larger than size before mp3 audio:%s", loggingFilename, identifier);
                    throw new InvalidFrameException(identifier + " is invalid frame");
                } else {
                    frameSize = nonSyncSafeFrameSize;
                }
            } else {
                //appears to be sync safe but lets look at the bytes just after the reported end of this
                //frame to see if find a valid frame header

                //Read the Frame Identifier
                byte[] readAheadbuffer = new byte[getFrameIdSize()];
                // we're just before the frame size, we need ahead look 4 bytes for the size, 2 flag bytes, and the frame size
                int peekFrameHeaderPosition = INTEGRAL_SIZE + getFrameFlagsSize() + frameSize;

                if (buffer.size() - peekFrameHeaderPosition >= getFrameIdSize()) {
                    for (int i = 0, size = readAheadbuffer.length; i < size; i++) {
                        readAheadbuffer[i] = buffer.getByte(peekFrameHeaderPosition + i);
                    }

                    String readAheadIdentifier = new String(readAheadbuffer);
                    if (isValidID3v2FrameIdentifier(readAheadIdentifier)) {
                        //Everything ok, so continue
                    } else if (ID3SyncSafeInteger.isBufferEmpty(readAheadbuffer)) {
                        //no data found so assume entered padding in which case assume it is last
                        //frame and we are ok
                    }
                    //haven't found identifier so maybe not syncsafe or maybe there are no more frames, just padding
                    else {
                        //Ok lets try using a non-syncsafe integer

                        //size returned will be larger so is it valid
                        if (nonSyncSafeFrameSize > buffer.size() + INTEGRAL_SIZE) {
                            //invalid so assume syncsafe
                        } else {
                            readAheadbuffer = new byte[getFrameIdSize()];
                            peekFrameHeaderPosition = INTEGRAL_SIZE + getFrameFlagsSize() + nonSyncSafeFrameSize;

                            if (buffer.size() - peekFrameHeaderPosition >= getFrameIdSize()) {
                                for (int i = 0, size = readAheadbuffer.length; i < size; i++) {
                                    readAheadbuffer[i] = buffer.getByte(peekFrameHeaderPosition + i);
                                }

                                readAheadIdentifier = new String(readAheadbuffer);

                                //ok found a valid identifier using non-syncsafe so assume non-syncsafe size
                                //and continue
                                if (isValidID3v2FrameIdentifier(readAheadIdentifier)) {
                                    frameSize = nonSyncSafeFrameSize;
                                    LOG.log(WARN, "%s:Assuming frame size is NOT stored as a sync safe integer:%s", loggingFilename, identifier);
                                }
                                //no data found so assume entered padding in which case assume it is last
                                //frame and we are ok whereas we didn't hit padding when using syncsafe integer
                                //or we wouldn't have got to this point. So assume syncsafe integer ended within
                                //the frame data whereas this has reached end of frames.
                                else if (ID3SyncSafeInteger.isBufferEmpty(readAheadbuffer)) {
                                    frameSize = nonSyncSafeFrameSize;
                                    LOG.log(WARN, "%s:Assuming frame size is NOT stored as a sync safe integer:%s", loggingFilename, identifier);
                                }
                                //invalid so assume syncsafe as that is is the standard
                            } else {
                                //If the unsync framesize matches exactly the remaining bytes then assume it has the
                                //correct size for the last frame
                                if (buffer.size() + INTEGRAL_SIZE == 0) {
                                    frameSize = nonSyncSafeFrameSize;
                                }
                                //Inconclusive stick with syncsafe
                            }
                        }
                    }
                }//There is no padding or framedata we are at end so assume syncsafe
            }
        }
    }

    protected int getFrameIdSize() {
        return FRAME_ID_SIZE;
    }

    protected int getFrameSizeSize() {
        return FRAME_SIZE_SIZE;
    }

    protected int getFrameHeaderSize() {
        return FRAME_HEADER_SIZE;
    }

    /**
     * Write the frame. Writes the frame header but writing the data is delegated to the
     * frame body.
     */
    public void write(ByteArrayOutputStream tagBuffer) {
        boolean unsynchronization;

        LOG.log(DEBUG, "Writing frame to file:" + getIdentifier());

        //This is where we will write header, move position to where we can
        //write bodybuffer
        ByteBuffer headerBuffer = ByteBuffer.allocate(FRAME_HEADER_SIZE);

        //Write Frame Body Data to a new stream
        ByteArrayOutputStream bodyOutputStream = new ByteArrayOutputStream();
        ((AbstractID3v2FrameBody) frameBody).write(bodyOutputStream);

        //Does it need unsynchronizing, and are we allowing unsychronizing
        byte[] bodyBuffer = bodyOutputStream.toByteArray();
        unsynchronization = TagOptionSingleton.getInstance().isUnsyncTags() &&
                ID3Unsynchronization.requiresUnsynchronization(bodyBuffer);
        if (unsynchronization) {
            bodyBuffer = ID3Unsynchronization.unsynchronize(bodyBuffer);
            LOG.log(DEBUG, "bodybytebuffer:sizeafterunsynchronisation:" + bodyBuffer.length);
        }

        //Write Frame Header
        //Write Frame ID, the identifier must be 4 bytes bytes long it may not be
        //because converted an unknown v2.2 id (only 3 bytes long)
        if (getIdentifier().length() == 3) {
            identifier = identifier + ' ';
        }
        headerBuffer.put(getIdentifier().getBytes(StandardCharsets.ISO_8859_1), 0, FRAME_ID_SIZE);

        //Write Frame Size based on size of body buffer (if it has been unsynced then it size
        //will have increased accordingly
        int size = bodyBuffer.length;
        LOG.log(INFO, "Frame Size Is:" + size);
        headerBuffer.put(ID3SyncSafeInteger.valueToBuffer(size));

        //Write the Flags
        //Status Flags:leave as they were when we read
        headerBuffer.put(statusFlags.getWriteFlags());

        //Remove any non standard flags
        ((ID3v24Frame.EncodingFlags) encodingFlags).unsetNonStandardFlags();

        //Encoding we only support unsynchronization
        if (unsynchronization) {
            ((ID3v24Frame.EncodingFlags) encodingFlags).setUnsynchronised();
        } else {
            ((ID3v24Frame.EncodingFlags) encodingFlags).unsetUnsynchronised();
        }
        //These are not currently supported on write
        ((ID3v24Frame.EncodingFlags) encodingFlags).unsetCompression();
        ((ID3v24Frame.EncodingFlags) encodingFlags).unsetDataLengthIndicator();
        headerBuffer.put(encodingFlags.getFlags());

        try {
            //Add header to the Byte Array Output Stream
            tagBuffer.write(headerBuffer.array());

            if (((EncodingFlags) encodingFlags).isEncryption()) {
                tagBuffer.write(encryptionMethod);
            }

            if (((EncodingFlags) encodingFlags).isGrouping()) {
                tagBuffer.write(groupIdentifier);
            }

            //Add bodybuffer to the Byte Array Output Stream
            tagBuffer.write(bodyBuffer);
        } catch (IOException ioe) {
            //This could never happen coz not writing to file, so convert to RuntimeException
            throw new RuntimeException(ioe);
        }
    }

    /**
     * Get Status Flags Object
     */
    public AbstractID3v2Frame.StatusFlags getStatusFlags() {
        return statusFlags;
    }

    /**
     * Get Encoding Flags Object
     */
    public AbstractID3v2Frame.EncodingFlags getEncodingFlags() {
        return encodingFlags;
    }

    /**
     * Return String Representation of body
     */
    public void createStructure() {
        MP3File.getStructureFormatter().openHeadingElement(TYPE_FRAME, getIdentifier());
        MP3File.getStructureFormatter().addElement(TYPE_FRAME_SIZE, frameSize);
        statusFlags.createStructure();
        encodingFlags.createStructure();
        frameBody.createStructure();
        MP3File.getStructureFormatter().closeHeadingElement(TYPE_FRAME);
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof ID3v24Frame)) {
            return false;
        }
        ID3v24Frame that = (ID3v24Frame) obj;


        return EqualsUtil.areEqual(statusFlags, that.statusFlags) &&
                EqualsUtil.areEqual(encodingFlags, that.encodingFlags) && super.equals(that);
    }

    private int getFrameFlagsSize() {
        return FRAME_FLAGS_SIZE;
    }

    private int peekBufferInt(Buffer buffer) {
        return (buffer.getByte(0) & 0xff) << 24
                | (buffer.getByte(1) & 0xff) << 16
                | (buffer.getByte(2) & 0xff) << 8
                | (buffer.getByte(3) & 0xff);
    }

    /**
     * Does the frame identifier meet the syntax for a idv3v2 frame identifier.
     * must start with a capital letter and only contain capital letters and numbers
     * <p>
     * Must be 4 characters which match [A-Z][0-9A-Z]{3}
     *
     * @param identifier to be checked
     * @return whether the identifier is valid
     */
    private boolean isValidID3v2FrameIdentifier(String identifier) {
        return identifier.length() >= 4 &&
                Characters.isUpperCaseEnglish(identifier.charAt(0)) &&
                Characters.isUpperCaseEnglishOrDigit(identifier.charAt(1)) &&
                Characters.isUpperCaseEnglishOrDigit(identifier.charAt(2)) &&
                Characters.isUpperCaseEnglishOrDigit(identifier.charAt(3));
    }

    /**
     * Return size of frame
     *
     * @return int frame size
     */
    public int getSize() {
        return frameBody.getSize() + FRAME_HEADER_SIZE;
    }

    public void read(ByteBuffer byteBuffer) throws InvalidFrameException, InvalidDataTypeException {
        String identifier = readIdentifier(byteBuffer);

        //Is this a valid identifier?
        if (!isValidID3v2FrameIdentifier(identifier)) {
            //If not valid move file pointer back to one byte after
            //the original check so can try again.
            LOG.log(DEBUG, loggingFilename + ":" + "Invalid identifier:" + identifier);
            byteBuffer.position(byteBuffer.position() - (getFrameIdSize() - 1));
            throw new InvalidFrameIdentifierException(
                    loggingFilename + ":" + identifier + ":is not a valid ID3v2.30 frame");
        }

        //Get the frame size, adjusted as necessary
        getFrameSize(byteBuffer);

        //Read the flag bytes
        statusFlags = new StatusFlags(byteBuffer.get());
        encodingFlags = new EncodingFlags(byteBuffer.get());

        //Read extra bits appended to frame header for various encodings
        //These are not included in header size but are included in frame size but wont be read when we actually
        //try to read the frame body data
        int extraHeaderBytesCount = 0;
        int dataLengthSize = -1;
        if (((EncodingFlags) encodingFlags).isGrouping()) {
            extraHeaderBytesCount = FRAME_GROUPING_INDICATOR_SIZE;
            groupIdentifier = byteBuffer.get();
        }

        if (((EncodingFlags) encodingFlags).isEncryption()) {
            //Read the Encryption byte, but do nothing with it
            extraHeaderBytesCount += FRAME_ENCRYPTION_INDICATOR_SIZE;
            encryptionMethod = byteBuffer.get();
        }

        if (((EncodingFlags) encodingFlags).isDataLengthIndicator()) {
            //Read the sync safe size field
            dataLengthSize = ID3SyncSafeInteger.bufferToValue(byteBuffer);
            extraHeaderBytesCount += FRAME_DATA_LENGTH_SIZE;
            LOG.log(DEBUG, loggingFilename + ":" + "Frame Size Is:" + frameSize + " Data Length Size:" + dataLengthSize);
        }

        //Work out the real size of the frameBody data
        int realFrameSize = frameSize - extraHeaderBytesCount;

        //Create Buffer that only contains the body of this frame rather than the remainder of tag
        ByteBuffer frameBodyBuffer = byteBuffer.slice();
        frameBodyBuffer.limit(realFrameSize);

        //Do we need to synchronize the frame body
        int syncSize = realFrameSize;
        if (((EncodingFlags) encodingFlags).isUnsynchronised()) {
            //We only want to synchronize the buffer up to the end of this frame (remember this
            //buffer contains the remainder of this tag not just this frame), and we cannot just
            //create a new buffer because when this method returns the position of the buffer is used
            //to look for the next frame, so we need to modify the buffer. The action of synchronizing causes
            //bytes to be dropped so the existing buffer is large enough to hold the modifications
            frameBodyBuffer = ID3Unsynchronization.synchronize(frameBodyBuffer);
            syncSize = frameBodyBuffer.limit();
            LOG.log(DEBUG, loggingFilename + ":" + "Frame Size After Syncing is:" + syncSize);
        }

        //Read the body data
        try {
            if (((EncodingFlags) encodingFlags).isCompression()) {
                frameBodyBuffer = ID3Compression.uncompress(identifier,
                        loggingFilename,
                        byteBuffer,
                        dataLengthSize,
                        realFrameSize);
                if (((EncodingFlags) encodingFlags).isEncryption()) {
                    frameBody = readEncryptedBody(identifier, frameBodyBuffer, dataLengthSize);
                } else {
                    frameBody = readBody(identifier, frameBodyBuffer, dataLengthSize);
                }
            } else if (((EncodingFlags) encodingFlags).isEncryption()) {
                frameBodyBuffer = byteBuffer.slice();
                frameBodyBuffer.limit(realFrameSize);
                frameBody = readEncryptedBody(identifier, byteBuffer, frameSize);
            } else {
                frameBody = readBody(identifier, frameBodyBuffer, syncSize);
            }
            if (!(frameBody instanceof ID3v24FrameBody)) {
                LOG.log(DEBUG, loggingFilename + ":" + "Converted frame body with:" + identifier +
                        " to deprecated framebody");
                frameBody = new FrameBodyDeprecated((AbstractID3v2FrameBody) frameBody);
            }
        } finally {
            //Update position of main buffer, so no attempt is made to reread these bytes
            byteBuffer.position(byteBuffer.position() + realFrameSize);
        }
    }

    /**
     * Read the frame size form the header, check okay , if not try to fix
     * or just throw exception
     */
    private void getFrameSize(ByteBuffer byteBuffer)
            throws InvalidFrameException {
        //Read frame size as syncsafe integer
        frameSize = ID3SyncSafeInteger.bufferToValue(byteBuffer);

        if (frameSize < 0) {
            LOG.log(WARN, loggingFilename + ":" + "Invalid Frame size:" + identifier);
            throw new InvalidFrameException(identifier + " is invalid frame");
        } else if (frameSize == 0) {
            LOG.log(WARN, loggingFilename + ":" + "Empty Frame:" + identifier);
            //We dont process this frame or add to framemap becuase contains no useful information
            //Skip the two flag bytes so in correct position for subsequent frames
            byteBuffer.get();
            byteBuffer.get();
            throw new EmptyFrameException(identifier + " is empty frame");
        } else if (frameSize > (byteBuffer.remaining() - FRAME_FLAGS_SIZE)) {
            LOG.log(WARN, loggingFilename + ":" + "Invalid Frame size larger than size before mp3 audio:" + identifier);
            throw new InvalidFrameException(identifier + " is invalid frame");
        }

        checkIfFrameSizeThatIsNotSyncSafe(byteBuffer);
    }

    /**
     * If frame is greater than certain size it will be decoded differently if unsynchronized to if synchronized
     * Frames with certain byte sequences should be unsynchronized but sometimes editors do not
     * unsynchronize them so this method checks both cases and goes with the option that fits best with the data
     */
    private void checkIfFrameSizeThatIsNotSyncSafe(ByteBuffer byteBuffer)
            throws InvalidFrameException {
        if (frameSize > ID3SyncSafeInteger.MAX_SAFE_SIZE) {
            //Set Just after size field this is where we want to be when we leave this if statement
            int currentPosition = byteBuffer.position();

            //Read as nonsync safe integer
            byteBuffer.position(currentPosition - getFrameIdSize());
            int nonSyncSafeFrameSize = byteBuffer.getInt();

            //Is the frame size syncsafe, should always be BUT some encoders such as Itunes do not do it properly
            //so do an easy check now.
            byteBuffer.position(currentPosition - getFrameIdSize());
            boolean isNotSyncSafe = ID3SyncSafeInteger.isBufferNotSyncSafe(byteBuffer);

            //not relative so need to move position
            byteBuffer.position(currentPosition);

            if (isNotSyncSafe) {
                LOG.log(WARN, loggingFilename + ":" + "Frame size is NOT stored as a sync safe integer:" + identifier);

                //This will return a larger frame size so need to check against buffer size if too large then we are
                //buggered , give up
                if (nonSyncSafeFrameSize > (byteBuffer.remaining() - -getFrameFlagsSize())) {
                    LOG.log(WARN, loggingFilename + ":" + "Invalid Frame size larger than size before mp3 audio:" +
                            identifier);
                    throw new InvalidFrameException(identifier + " is invalid frame");
                } else {
                    frameSize = nonSyncSafeFrameSize;
                }
            } else {
                //appears to be sync safe but lets look at the bytes just after the reported end of this
                //frame to see if find a valid frame header

                //Read the Frame Identifier
                byte[] readAheadbuffer = new byte[getFrameIdSize()];
                byteBuffer.position(currentPosition + frameSize + getFrameFlagsSize());

                if (byteBuffer.remaining() < getFrameIdSize()) {
                    //There is no padding or framedata we are at end so assume syncsafe
                    //reset position to just after framesize
                    byteBuffer.position(currentPosition);
                } else {
                    byteBuffer.get(readAheadbuffer, 0, getFrameIdSize());

                    //reset position to just after framesize
                    byteBuffer.position(currentPosition);

                    String readAheadIdentifier = new String(readAheadbuffer);
                    if (isValidID3v2FrameIdentifier(readAheadIdentifier)) {
                        //Everything ok, so continue
                    } else if (ID3SyncSafeInteger.isBufferEmpty(readAheadbuffer)) {
                        //no data found so assume entered padding in which case assume it is last
                        //frame and we are ok
                    }
                    //haven't found identifier so maybe not syncsafe or maybe there are no more frames, just padding
                    else {
                        //Ok lets try using a non-syncsafe integer

                        //size returned will be larger so is it valid
                        if (nonSyncSafeFrameSize > byteBuffer.remaining() - getFrameFlagsSize()) {
                            //invalid so assume syncsafe
                            byteBuffer.position(currentPosition);
                        } else {
                            readAheadbuffer = new byte[getFrameIdSize()];
                            byteBuffer.position(currentPosition + nonSyncSafeFrameSize + getFrameFlagsSize());

                            if (byteBuffer.remaining() >= getFrameIdSize()) {
                                byteBuffer.get(readAheadbuffer, 0, getFrameIdSize());
                                readAheadIdentifier = new String(readAheadbuffer);

                                //reset position to just after framesize
                                byteBuffer.position(currentPosition);

                                //ok found a valid identifier using non-syncsafe so assume non-syncsafe size
                                //and continue
                                if (isValidID3v2FrameIdentifier(readAheadIdentifier)) {
                                    frameSize = nonSyncSafeFrameSize;
                                    LOG.log(WARN, loggingFilename + ":" +
                                            "Assuming frame size is NOT stored as a sync safe integer:" +
                                            identifier);
                                }
                                //no data found so assume entered padding in which case assume it is last
                                //frame and we are ok whereas we didn't hit padding when using syncsafe integer
                                //or we wouldn't have got to this point. So assume syncsafe integer ended within
                                //the frame data whereas this has reached end of frames.
                                else if (ID3SyncSafeInteger.isBufferEmpty(readAheadbuffer)) {
                                    frameSize = nonSyncSafeFrameSize;
                                    LOG.log(WARN, loggingFilename + ":" +
                                            "Assuming frame size is NOT stored as a sync safe integer:" +
                                            identifier);
                                }
                                //invalid so assume syncsafe as that is is the standard
                            } else {
                                //reset position to just after framesize
                                byteBuffer.position(currentPosition);

                                //If the unsync framesize matches exactly the remaining bytes then assume it has the
                                //correct size for the last frame
                                if (byteBuffer.remaining() == 0) {
                                    frameSize = nonSyncSafeFrameSize;
                                }
                                //Inconclusive stick with syncsafe
                            }
                        }
                    }
                }
            }
        }
    }

//    public int getEncryptionMethod() {
//        return encryptionMethod;
//    }
//
//    public int getGroupIdentifier() {
//        return groupIdentifier;
//    }

    /**
     * @return true if considered a common frame
     */
    public boolean isBinary() {
        return ID3v24Frames.getInstanceOf().isBinary(getId());
    }

    /**
     * @return true if considered a common frame
     */
    public boolean isCommon() {
        return ID3v24Frames.getInstanceOf().isCommon(getId());
    }

    /**
     * Sets the charset encoding used by the field.
     *
     * @param encoding charset.
     */
    public void setEncoding(Charset encoding) {
        try {
            byte encodingId = TextEncoding.getInstanceOf().getIdForCharset(encoding);
            if (encodingId < 4) {
                getBody().setTextEncoding(encodingId);
            }
        } catch (NoSuchElementException ignored) {
        }
    }

    /**
     * Member Class This represents a frame headers Status Flags
     * Make adjustments if necessary based on frame type and specification.
     */
    public class StatusFlags extends AbstractID3v2Frame.StatusFlags {
        static final String TYPE_TAGALTERPRESERVATION = "typeTagAlterPreservation";
        static final String TYPE_FILEALTERPRESERVATION = "typeFileAlterPreservation";
        static final String TYPE_READONLY = "typeReadOnly";


        /**
         * Discard frame if tag altered
         */
        static final int MASK_TAG_ALTER_PRESERVATION = FileConstants.BIT6;

        /**
         * Discard frame if audio part of file altered
         */
        static final int MASK_FILE_ALTER_PRESERVATION = FileConstants.BIT5;

        /**
         * Frame tagged as read only
         */
        static final int MASK_READ_ONLY = FileConstants.BIT4;

        /**
         * Use this when creating a frame from scratch
         */
        StatusFlags() {
        }

        /**
         * Use this constructor when reading from file or from another v4 frame
         */
        StatusFlags(byte flags) {
            originalFlags = flags;
            writeFlags = flags;
            modifyFlags();
        }

        /**
         * Use this constructor when convert a v23 frame
         */
        StatusFlags(ID3v23Frame.StatusFlags statusFlags) {
            originalFlags = convertV3ToV4Flags(statusFlags.getOriginalFlags());
            writeFlags = originalFlags;
            modifyFlags();
        }

        /**
         * Makes modifications to flags based on specification and frameid
         */
        void modifyFlags() {
            String str = getIdentifier();
            if (ID3v24Frames.getInstanceOf().isDiscardIfFileAltered(str)) {
                writeFlags |= (byte) MASK_FILE_ALTER_PRESERVATION;
                writeFlags &= (byte) ~MASK_TAG_ALTER_PRESERVATION;
            } else {
                writeFlags &= (byte) ~MASK_FILE_ALTER_PRESERVATION;
                writeFlags &= (byte) ~MASK_TAG_ALTER_PRESERVATION;
            }
        }

        /**
         * Convert V3 Flags to equivalent V4 Flags
         */
        private byte convertV3ToV4Flags(byte v3Flag) {
            byte v4Flag = (byte) 0;
            if ((v3Flag & ID3v23Frame.StatusFlags.MASK_FILE_ALTER_PRESERVATION) != 0) {
                v4Flag |= (byte) MASK_FILE_ALTER_PRESERVATION;
            }
            if ((v3Flag & ID3v23Frame.StatusFlags.MASK_TAG_ALTER_PRESERVATION) != 0) {
                v4Flag |= (byte) MASK_TAG_ALTER_PRESERVATION;
            }
            return v4Flag;
        }

        public void createStructure() {
            MP3File.getStructureFormatter().openHeadingElement(TYPE_FLAGS, "");
            MP3File.getStructureFormatter()
                    .addElement(TYPE_TAGALTERPRESERVATION, originalFlags & MASK_TAG_ALTER_PRESERVATION);
            MP3File.getStructureFormatter()
                    .addElement(TYPE_FILEALTERPRESERVATION, originalFlags & MASK_FILE_ALTER_PRESERVATION);
            MP3File.getStructureFormatter().addElement(TYPE_READONLY, originalFlags & MASK_READ_ONLY);
            MP3File.getStructureFormatter().closeHeadingElement(TYPE_FLAGS);
        }


    }

    /**
     * This represents a frame headers Encoding Flags
     */
    class EncodingFlags extends AbstractID3v2Frame.EncodingFlags {
        static final String TYPE_COMPRESSION = "compression";
        static final String TYPE_ENCRYPTION = "encryption";
        static final String TYPE_GROUPIDENTITY = "groupidentity";
        static final String TYPE_FRAMEUNSYNCHRONIZATION = "frameUnsynchronisation";
        static final String TYPE_DATALENGTHINDICATOR = "dataLengthIndicator";

        /**
         * Frame is part of a group
         */
        static final int MASK_GROUPING_IDENTITY = FileConstants.BIT6;

        /**
         * Frame is compressed
         */
        static final int MASK_COMPRESSION = FileConstants.BIT3;

        /**
         * Frame is encrypted
         */
        static final int MASK_ENCRYPTION = FileConstants.BIT2;

        /**
         * Unsynchronisation
         */
        static final int MASK_FRAME_UNSYNCHRONIZATION = FileConstants.BIT1;

        /**
         * Length
         */
        static final int MASK_DATA_LENGTH_INDICATOR = FileConstants.BIT0;

        /**
         * Use this when creating a frame from scratch
         */
        EncodingFlags() {
        }

        /**
         * Use this when creating a frame from existing flags in another v4 frame
         */
        EncodingFlags(byte flags) {
            super(flags);
            logEnabledFlags();
        }

        void logEnabledFlags() {
            if (isNonStandardFlags()) {
                LOG.log(WARN, "%s:%s Unknown Encoding Flags:%s", loggingFilename, identifier, Hex.asHex(flags));
            }
            if (isCompression()) {
                LOG.log(WARN, ErrorMessage.MP3_FRAME_IS_COMPRESSED, loggingFilename, identifier);
            }

            if (isEncryption()) {
                LOG.log(WARN, ErrorMessage.MP3_FRAME_IS_ENCRYPTED, loggingFilename, identifier);
            }

            if (isGrouping()) {
                LOG.log(DEBUG, ErrorMessage.MP3_FRAME_IS_GROUPED, loggingFilename, identifier);
            }

            if (isUnsynchronised()) {
                LOG.log(DEBUG, ErrorMessage.MP3_FRAME_IS_UNSYNCHRONISED, loggingFilename, identifier);
            }

            if (isDataLengthIndicator()) {
                LOG.log(DEBUG, ErrorMessage.MP3_FRAME_IS_DATA_LENGTH_INDICATOR, loggingFilename, identifier);
            }
        }

        public boolean isCompression() {
            return (flags & MASK_COMPRESSION) > 0;
        }

        public boolean isEncryption() {
            return (flags & MASK_ENCRYPTION) > 0;
        }

        public boolean isGrouping() {
            return (flags & MASK_GROUPING_IDENTITY) > 0;
        }

        boolean isUnsynchronised() {
            return (flags & MASK_FRAME_UNSYNCHRONIZATION) > 0;
        }

        boolean isDataLengthIndicator() {
            return (flags & MASK_DATA_LENGTH_INDICATOR) > 0;
        }

        boolean isNonStandardFlags() {
            return ((flags & FileConstants.BIT7) > 0) ||
                    ((flags & FileConstants.BIT5) > 0) ||
                    ((flags & FileConstants.BIT4) > 0);
        }

        public byte getFlags() {
            return flags;
        }

        public void createStructure() {
            MP3File.getStructureFormatter().openHeadingElement(TYPE_FLAGS, "");
            MP3File.getStructureFormatter().addElement(TYPE_COMPRESSION, flags & MASK_COMPRESSION);
            MP3File.getStructureFormatter().addElement(TYPE_ENCRYPTION, flags & MASK_ENCRYPTION);
            MP3File.getStructureFormatter().addElement(TYPE_GROUPIDENTITY, flags & MASK_GROUPING_IDENTITY);
            MP3File.getStructureFormatter()
                    .addElement(TYPE_FRAMEUNSYNCHRONIZATION, flags & MASK_FRAME_UNSYNCHRONIZATION);
            MP3File.getStructureFormatter().addElement(TYPE_DATALENGTHINDICATOR, flags & MASK_DATA_LENGTH_INDICATOR);
            MP3File.getStructureFormatter().closeHeadingElement(TYPE_FLAGS);
        }

//        public void setCompression() {
//            flags |= MASK_COMPRESSION;
//        }
//
//        public void setEncryption() {
//            flags |= MASK_ENCRYPTION;
//        }
//
//        public void setGrouping() {
//            flags |= MASK_GROUPING_IDENTITY;
//        }

        void setUnsynchronised() {
            flags |= MASK_FRAME_UNSYNCHRONIZATION;
        }

//        public void setDataLengthIndicator() {
//            flags |= MASK_DATA_LENGTH_INDICATOR;
//        }

        void unsetCompression() {
            flags &= (byte) ~MASK_COMPRESSION;
        }

//        public void unsetEncryption() {
//            flags &= (byte)~MASK_ENCRYPTION;
//        }
//
//        public void unsetGrouping() {
//            flags &= (byte)~MASK_GROUPING_IDENTITY;
//        }

        void unsetUnsynchronised() {
            flags &= (byte) ~MASK_FRAME_UNSYNCHRONIZATION;
        }

        void unsetDataLengthIndicator() {
            flags &= (byte) ~MASK_DATA_LENGTH_INDICATOR;
        }

        void unsetNonStandardFlags() {
            if (isNonStandardFlags()) {
                LOG.log(WARN, "%s:%s:Unsetting Unknown Encoding Flags:%s", loggingFilename, getIdentifier(), Hex.asHex(flags));
                flags &= (byte) ~FileConstants.BIT7;
                flags &= (byte) ~FileConstants.BIT5;
                flags &= (byte) ~FileConstants.BIT4;
            }
        }
    }
}
