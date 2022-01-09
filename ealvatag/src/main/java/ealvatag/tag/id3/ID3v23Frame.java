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
import static ealvatag.logging.EalvaTagLog.LogLevel.INFO;
import static ealvatag.logging.EalvaTagLog.LogLevel.WARN;

import com.google.common.base.Strings;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.NoSuchElementException;

import ealvatag.audio.mp3.MP3File;
import ealvatag.logging.EalvaTagLog;
import ealvatag.logging.EalvaTagLog.JLogger;
import ealvatag.logging.EalvaTagLog.JLoggers;
import ealvatag.logging.Hex;
import ealvatag.tag.EmptyFrameException;
import ealvatag.tag.InvalidDataTypeException;
import ealvatag.tag.InvalidFrameException;
import ealvatag.tag.InvalidFrameIdentifierException;
import ealvatag.tag.InvalidTagException;
import ealvatag.tag.id3.framebody.AbstractID3v2FrameBody;
import ealvatag.tag.id3.framebody.FrameBodyDeprecated;
import ealvatag.tag.id3.framebody.FrameBodyUnsupported;
import ealvatag.tag.id3.framebody.ID3v23FrameBody;
import ealvatag.tag.id3.valuepair.TextEncoding;
import ealvatag.utils.Characters;
import ealvatag.utils.EqualsUtil;
import ealvatag.utils.StandardCharsets;
import okio.Buffer;

/**
 * Represents an ID3v2.3 frame.
 *
 * @author : Paul Taylor
 * @author : Eric Farng
 * @version $Id$
 */
@SuppressWarnings("Duplicates")
public class ID3v23Frame extends AbstractID3v2Frame {
    private static final JLogger LOG = JLoggers.get(ID3v23Frame.class, EalvaTagLog.MARKER);

    private static final int FRAME_ID_SIZE = 4;
    private static final int FRAME_FLAGS_SIZE = 2;
    private static final int FRAME_SIZE_SIZE = 4;
    static final int FRAME_HEADER_SIZE = FRAME_ID_SIZE + FRAME_SIZE_SIZE + FRAME_FLAGS_SIZE;
    private static final int FRAME_COMPRESSION_UNCOMPRESSED_SIZE = 4;
    private static final int FRAME_ENCRYPTION_INDICATOR_SIZE = 1;
    private static final int FRAME_GROUPING_INDICATOR_SIZE = 1;
    /**
     * If the frame is encrypted then the encryption method is stored in this byte
     */
    private int encryptionMethod;

    /**
     * If the frame belongs in a group with other frames then the group identifier byte is stored
     */
    private int groupIdentifier;

    /**
     * Creates a new ID3v23 Frame
     */
    public ID3v23Frame() {
    }

    /**
     * Creates a new ID3v23 Frame of type identifier.
     * <p>
     * <p>An empty body of the correct type will be automatically created.
     * This constructor should be used when wish to create a new
     * frame from scratch using user data.
     */
    public ID3v23Frame(String identifier) {
        super(identifier);
        statusFlags = new StatusFlags();
        encodingFlags = new EncodingFlags();
    }

    /**
     * Copy Constructor
     * <p>
     * Creates a new v23 frame  based on another v23 frame
     */
    public ID3v23Frame(ID3v23Frame frame) {
        super(frame);
        statusFlags = new StatusFlags(frame.getStatusFlags().getOriginalFlags());
        encodingFlags = new EncodingFlags(frame.getEncodingFlags().getFlags());
    }

    /**
     * Partially construct ID3v24 Frame form an IS3v23Frame
     * <p>
     * Used for Special Cases
     */
    protected ID3v23Frame(ID3v24Frame frame, String identifier) throws InvalidFrameException {
        this.identifier = identifier;
        statusFlags = new StatusFlags((ID3v24Frame.StatusFlags) frame.getStatusFlags());
        encodingFlags = new EncodingFlags(frame.getEncodingFlags().getFlags());
    }

    /**
     * Creates a new ID3v23Frame  based on another frame of a different version.
     */
    public ID3v23Frame(AbstractID3v2Frame frame) throws InvalidFrameException {
        LOG.log(DEBUG, "Creating frame from a frame of a different version");
        if (frame instanceof ID3v23Frame) {
            throw new UnsupportedOperationException("Copy Constructor not called. Please type cast the argument");
        } else if (frame instanceof ID3v22Frame) {
            statusFlags = new StatusFlags();
            encodingFlags = new EncodingFlags();
        } else if (frame instanceof ID3v24Frame) {
            statusFlags = new StatusFlags((ID3v24Frame.StatusFlags) frame.getStatusFlags());
            encodingFlags = new EncodingFlags(frame.getEncodingFlags().getFlags());
        }

        if (frame instanceof ID3v24Frame) {
            //Unknown Frame e.g NCON, also protects when known id but has unsupported frame body
            if (frame.getBody() instanceof FrameBodyUnsupported) {
                frameBody = new FrameBodyUnsupported((FrameBodyUnsupported) frame.getBody());
                frameBody.setHeader(this);
                identifier = frame.getIdentifier();
                LOG.log(DEBUG, "UNKNOWN:Orig id is:%s:New id is:%s", frame.getIdentifier(), identifier);
                return;
            }
            // Deprecated frame for v24
            else if (frame.getBody() instanceof FrameBodyDeprecated) {
                //Was it valid for this tag version, if so try and reconstruct
                if (ID3Tags.isID3v23FrameIdentifier(frame.getIdentifier())) {
                    frameBody = ((FrameBodyDeprecated) frame.getBody()).getOriginalFrameBody();
                    frameBody.setHeader(this);
                    frameBody.setTextEncoding(ID3TextEncodingConversion.getTextEncoding(this, frameBody.getTextEncoding()));
                    identifier = frame.getIdentifier();
                    LOG.log(DEBUG, "DEPRECATED:Orig id is:%s:New id is:%s", frame.getIdentifier(), identifier);
                }
                //or was it still deprecated, if so leave as is
                else {
                    frameBody = new FrameBodyDeprecated((FrameBodyDeprecated) frame.getBody());
                    frameBody.setHeader(this);
                    frameBody.setTextEncoding(ID3TextEncodingConversion.getTextEncoding(this, frameBody.getTextEncoding()));

                    identifier = frame.getIdentifier();
                    LOG.log(DEBUG, "DEPRECATED:Orig id is:%s:New id is:%s", frame.getIdentifier(), identifier);
                    return;
                }
            } else if (ID3Tags.isID3v24FrameIdentifier(frame.getIdentifier())) {
                LOG.log(DEBUG, "isID3v24FrameIdentifier");
                //Version between v4 and v3
                identifier = ID3Tags.convertFrameID24To23(frame.getIdentifier());
                if (identifier != null) {
                    LOG.log(DEBUG, "V4:Orig id is:%s:New id is:%s", frame.getIdentifier(), identifier);
                    frameBody = (AbstractTagFrameBody) ID3Tags.copyObject(frame.getBody());
                    frameBody.setHeader(this);
                    frameBody.setTextEncoding(ID3TextEncodingConversion.getTextEncoding(this, frameBody.getTextEncoding()));
                    return;
                } else {
                    //Is it a known v4 frame which needs forcing to v3 frame e.g. TDRC - TYER,TDAT
                    identifier = ID3Tags.forceFrameID24To23(frame.getIdentifier());
                    if (identifier != null) {
                        LOG.log(DEBUG, "V4:Orig id is:%s:New id is:%s", frame.getIdentifier(), identifier);
                        frameBody = readBody(identifier, (AbstractID3v2FrameBody) frame.getBody());
                        frameBody.setHeader(this);
                        frameBody.setTextEncoding(ID3TextEncodingConversion.getTextEncoding(this, frameBody.getTextEncoding()));
                        return;
                    }
                    //It is a v24 frame that is not known and cannot be forced in v23 e.g TDRL,in which case
                    //we convert to a frameBody unsupported by writing contents as a byte array and feeding
                    //it into FrameBodyUnsupported
                    else {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        ((AbstractID3v2FrameBody) frame.getBody()).write(baos);

                        identifier = frame.getIdentifier();
                        frameBody = new FrameBodyUnsupported(identifier, baos.toByteArray());
                        frameBody.setHeader(this);
                        LOG.log(DEBUG, "V4:Orig id is:%s:New Id Unsupported is:%s", frame.getIdentifier(), identifier);
                        return;
                    }
                }
            }
            // Unable to find a suitable frameBody, this should not happen
            else {
                LOG.log(ERROR, "Orig id is:%sUnable to create Frame Body", frame.getIdentifier());
                throw new InvalidFrameException("Orig id is:" + frame.getIdentifier() + "Unable to create Frame Body");
            }
        } else if (frame instanceof ID3v22Frame) {
            if (ID3Tags.isID3v22FrameIdentifier(frame.getIdentifier())) {
                identifier = ID3Tags.convertFrameID22To23(frame.getIdentifier());
                if (identifier != null) {
                    LOG.log(DEBUG, "V3:Orig id is:%s:New id is:%s", frame.getIdentifier(), identifier);
                    frameBody = (AbstractTagFrameBody) ID3Tags.copyObject(frame.getBody());
                    frameBody.setHeader(this);
                    return;
                }
                //Is it a known v2 frame which needs forcing to v23 frame e.g PIC - APIC
                else if (ID3Tags.isID3v22FrameIdentifier(frame.getIdentifier())) {
                    //Force v2 to v3
                    identifier = ID3Tags.forceFrameID22To23(frame.getIdentifier());
                    if (identifier != null) {
                        LOG.log(DEBUG, "V22Orig id is:%s New id is:%s", frame.getIdentifier(), identifier);
                        frameBody = readBody(identifier, (AbstractID3v2FrameBody) frame.getBody());
                        frameBody.setHeader(this);
                        return;
                    }
                    //No mechanism exists to convert it to a v23 frame
                    else {
                        frameBody = new FrameBodyDeprecated((AbstractID3v2FrameBody) frame.getBody());
                        frameBody.setHeader(this);
                        identifier = frame.getIdentifier();
                        LOG.log(DEBUG, "Deprecated:V22:orig id id is:%s:New id is:%s", frame.getIdentifier(), identifier);
                        return;
                    }
                }
            }
            // Unknown Frame e.g NCON
            else {
                frameBody = new FrameBodyUnsupported((FrameBodyUnsupported) frame.getBody());
                frameBody.setHeader(this);
                identifier = frame.getIdentifier();
                LOG.log(DEBUG, "UNKNOWN:Orig id is:%s:New id is:%s", frame.getIdentifier(), identifier);
                return;
            }
        }

        LOG.log(WARN, "Frame is unknown version:%s", frame.getClass());
    }

    /**
     * Creates a new ID3v23Frame dataType by reading from byteBuffer.
     *
     * @param byteBuffer to read from
     */
    public ID3v23Frame(ByteBuffer byteBuffer, String loggingFilename) throws InvalidFrameException, InvalidDataTypeException {
        setLoggingFilename(loggingFilename);
        read(byteBuffer);
    }

    public ID3v23Frame(Buffer buffer, String loggingFilename, boolean ignoreArtwork)
            throws InvalidTagException, IOException {
        setLoggingFilename(loggingFilename);
        read(buffer, ignoreArtwork);
    }

    /**
     * Creates a new ID3v23Frame dataType by reading from byteBuffer.
     *
     * @param byteBuffer to read from
     * @deprecated use {@link #ID3v23Frame(ByteBuffer, String)} instead
     */
    public ID3v23Frame(ByteBuffer byteBuffer) throws InvalidFrameException, InvalidDataTypeException {
        this(byteBuffer, "");
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
     * Return size of frame
     *
     * @return int frame size
     */
    public int getSize() {
        return frameBody.getSize() + FRAME_HEADER_SIZE;
    }

    /**
     * Compare for equality
     * To be deemed equal obj must be a IDv23Frame with the same identifier
     * and the same flags.
     * containing the same body,dataType list ectera.
     * equals() method is made up from all the various components
     *
     * @return if true if this object is equivalent to obj
     */
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof ID3v23Frame)) {
            return false;
        }
        ID3v23Frame that = (ID3v23Frame) obj;


        return
                EqualsUtil.areEqual(statusFlags, that.statusFlags) &&
                        EqualsUtil.areEqual(encodingFlags, that.encodingFlags) &&
                        super.equals(that);

    }


    /**
     * Read the frame from a byteBuffer
     *
     * @param byteBuffer buffer to read from
     */
    public void read(ByteBuffer byteBuffer) throws InvalidFrameException, InvalidDataTypeException {
        String identifier = readIdentifier(byteBuffer);
        if (!isValidID3v2FrameIdentifier(identifier)) {
            LOG.log(DEBUG, "%s:Invalid identifier%s:", loggingFilename, identifier);
            byteBuffer.position(byteBuffer.position() - (getFrameIdSize() - 1));
            throw new InvalidFrameIdentifierException(loggingFilename + ":" + identifier + ":is not a valid ID3v2.30 frame");
        }
        //Read the size field (as Big Endian Int - byte buffers always initialised to Big Endian order)
        frameSize = byteBuffer.getInt();
        if (frameSize < 0) {
            LOG.log(WARN, "%s:Invalid Frame Size:%s:%s", loggingFilename, frameSize, identifier);
            throw new InvalidFrameException(identifier + " is invalid frame:" + frameSize);
        } else if (frameSize == 0) {
            LOG.log(WARN, "%s:Empty Frame Size:%s", loggingFilename, identifier);
            //We don't process this frame or add to frameMap because contains no useful information
            //Skip the two flag bytes so in correct position for subsequent frames
            byteBuffer.get();
            byteBuffer.get();
            throw new EmptyFrameException(identifier + " is empty frame");
        } else if (frameSize > byteBuffer.remaining()) {
            LOG.log(WARN, "%s:Invalid Frame size of %s larger than size of%s  before mp3 audio:%s",
                    loggingFilename,
                    frameSize,
                    byteBuffer.remaining(),
                    identifier);
            throw new InvalidFrameException(identifier +
                    " is invalid frame:" +
                    frameSize +
                    " larger than size of" +
                    byteBuffer.remaining() +
                    " before mp3 audio:" +
                    identifier);
        }

        //Read the flag bytes
        statusFlags = new StatusFlags(byteBuffer.get());
        encodingFlags = new EncodingFlags(byteBuffer.get());
        String id;

        //If this identifier is a valid v24 identifier or easily converted to v24
        id = ID3Tags.convertFrameID23To24(identifier);

        // Cant easily be converted to v24 but is it a valid v23 identifier
        if (id == null) {
            // It is a valid v23 identifier so should be able to find a
            //  frame body for it.
            if (ID3Tags.isID3v23FrameIdentifier(identifier)) {
                id = identifier;
            }
            // Unknown so will be created as FrameBodyUnsupported
            else {
                id = UNSUPPORTED_ID;
            }
        }
        LOG.log(INFO, loggingFilename + ":Identifier was:" + identifier + " reading using:" + id + "with frame size:" + frameSize);

        //Read extra bits appended to frame header for various encodings
        //These are not included in header size but are included in frame size but won't be read when we actually
        //try to read the frame body data
        int extraHeaderBytesCount = 0;
        int decompressedFrameSize = -1;

        if (((EncodingFlags) encodingFlags).isCompression()) {
            //Read the Decompressed Size
            decompressedFrameSize = byteBuffer.getInt();
            extraHeaderBytesCount = FRAME_COMPRESSION_UNCOMPRESSED_SIZE;
            LOG.log(INFO, loggingFilename + ":Decompressed frame size is:" + decompressedFrameSize);
        }

        if (((EncodingFlags) encodingFlags).isEncryption()) {
            //Consume the encryption byte
            extraHeaderBytesCount += FRAME_ENCRYPTION_INDICATOR_SIZE;
            encryptionMethod = byteBuffer.get();
        }

        if (((EncodingFlags) encodingFlags).isGrouping()) {
            //Read the Grouping byte, but do nothing with it
            extraHeaderBytesCount += FRAME_GROUPING_INDICATOR_SIZE;
            groupIdentifier = byteBuffer.get();
        }

        if (((EncodingFlags) encodingFlags).isNonStandardFlags()) {
            //Probably corrupt so treat as a standard frame
            LOG.log(ERROR, loggingFilename + ":InvalidEncodingFlags:" + Hex.asHex(encodingFlags.getFlags()));
        }

        if (((EncodingFlags) encodingFlags).isCompression()) {
            if (decompressedFrameSize > (100 * frameSize)) {
                throw new InvalidFrameException(identifier +
                        " is invalid frame, frame size " +
                        frameSize +
                        " cannot be:" +
                        decompressedFrameSize +
                        " when uncompressed");
            }
        }

        //Work out the real size of the frameBody data
        int realFrameSize = frameSize - extraHeaderBytesCount;

        if (realFrameSize <= 0) {
            throw new InvalidFrameException(identifier + " is invalid frame, realframeSize is:" + realFrameSize);
        }

        ByteBuffer frameBodyBuffer;
        //Read the body data
        try {
            if (((EncodingFlags) encodingFlags).isCompression()) {
                frameBodyBuffer =
                        ID3Compression.uncompress(identifier, loggingFilename, byteBuffer, decompressedFrameSize, realFrameSize);
                if (((EncodingFlags) encodingFlags).isEncryption()) {
                    frameBody = readEncryptedBody(id, frameBodyBuffer, decompressedFrameSize);
                } else {
                    frameBody = readBody(id, frameBodyBuffer, decompressedFrameSize);
                }
            } else if (((EncodingFlags) encodingFlags).isEncryption()) {
                frameBodyBuffer = byteBuffer.slice();
                frameBodyBuffer.limit(frameSize);
                frameBody = readEncryptedBody(identifier, frameBodyBuffer, frameSize);
            } else {
                //Create Buffer that only contains the body of this frame rather than the remainder of tag
                frameBodyBuffer = byteBuffer.slice();
                frameBodyBuffer.limit(realFrameSize);
                frameBody = readBody(id, frameBodyBuffer, realFrameSize);
            }
            //TODO code seems to assume that if the frame created is not a v23FrameBody
            //it should be deprecated, but what about if somehow a V24Frame has been put into a V23 Tag, shouldn't
            //it then be created as FrameBodyUnsupported
            if (!(frameBody instanceof ID3v23FrameBody)) {
                LOG.log(DEBUG, "%s:Converted frameBody with:%s  to deprecated frameBody", loggingFilename, identifier);
                frameBody = new FrameBodyDeprecated((AbstractID3v2FrameBody) frameBody);
            }
        } finally {
            //Update position of main buffer, so no attempt is made to reread these bytes
            byteBuffer.position(byteBuffer.position() + realFrameSize);
        }
    }

    private void read(Buffer buffer, boolean ignoreArtwork) throws InvalidTagException, IOException {
        String fileName = loggingFilename;
        try {
            String identifier = readIdentifier(buffer);
            if (!isValidID3v2FrameIdentifier(identifier)) {
                LOG.log(DEBUG, "Invalid identifier:%s - %s", identifier, fileName);
                throw new InvalidFrameIdentifierException(fileName + ":" + identifier + ":is not a valid ID3v2.30 frame");
            }
            frameSize = buffer.readInt();
            if (frameSize < 0) {
                LOG.log(WARN, "Invalid Frame Size:%s Id:%s - %s", frameSize, identifier, fileName);
                throw new InvalidFrameException(identifier + " is invalid frame:" + frameSize);
            } else if (frameSize == 0) {
                LOG.log(WARN, "%s:Empty Frame Size:%s", fileName, identifier);
                //We don't process this frame or add to frameMap because contains no useful information
                //Skip the two flag bytes so in correct position for subsequent frames
                buffer.readByte();
                buffer.readByte();
                throw new EmptyFrameException(identifier + " is empty frame");
            } else if (frameSize > buffer.size()) {
                LOG.log(WARN, "Invalid Frame size of %s larger than size of %s before mp3 audio %s - %s",
                        frameSize,
                        buffer.size(),
                        identifier,
                        fileName);
                throw new InvalidFrameException(identifier +
                        " is invalid frame:" +
                        frameSize +
                        " larger than size of" +
                        buffer.size() +
                        " before mp3 audio:" +
                        identifier);
            }

            //Read the flag bytes
            statusFlags = new StatusFlags(buffer.readByte());
            encodingFlags = new EncodingFlags(buffer.readByte());

            //If this identifier is a valid v24 identifier or easily converted to v24
            String frameId = ID3Tags.convertFrameID23To24(identifier);

            // Cant easily be converted to v24 but is it a valid v23 identifier
            if (frameId == null) {
                // It is a valid v23 identifier so should be able to find a
                //  frame body for it.
                if (ID3Tags.isID3v23FrameIdentifier(identifier)) {
                    frameId = identifier;
                }
                // Unknown so will be created as FrameBodyUnsupported
                else {
                    frameId = UNSUPPORTED_ID;
                }
            }

            //Read extra bits appended to frame header for various encodings
            //These are not included in header size but are included in frame size but won't be read when we actually
            //try to read the frame body data
            int extraHeaderBytesCount = 0;
            int decompressedFrameSize = -1;

            if (((EncodingFlags) encodingFlags).isCompression()) {
                //Read the Decompressed Size
                decompressedFrameSize = buffer.readInt();
                extraHeaderBytesCount = FRAME_COMPRESSION_UNCOMPRESSED_SIZE;
            }

            if (((EncodingFlags) encodingFlags).isEncryption()) {
                //Consume the encryption byte
                extraHeaderBytesCount += FRAME_ENCRYPTION_INDICATOR_SIZE;
                encryptionMethod = buffer.readByte();
            }

            if (((EncodingFlags) encodingFlags).isGrouping()) {
                //Read the Grouping byte, but do nothing with it
                extraHeaderBytesCount += FRAME_GROUPING_INDICATOR_SIZE;
                groupIdentifier = buffer.readByte();
            }

            if (((EncodingFlags) encodingFlags).isNonStandardFlags()) {
                //Probably corrupt so treat as a standard frame
                LOG.log(ERROR, "%s:InvalidEncodingFlags:%s", fileName, Hex.asHex(encodingFlags.getFlags()));
            }

            if (((EncodingFlags) encodingFlags).isCompression()) {
                if (decompressedFrameSize > (100 * frameSize)) {
                    throw new InvalidFrameException(identifier +
                            " is invalid frame, frame size " +
                            frameSize +
                            " cannot be:" +
                            decompressedFrameSize +
                            " when uncompressed");
                }
            }

            //Work out the real size of the frameBody data
            int realFrameSize = frameSize - extraHeaderBytesCount;

            if (realFrameSize <= 0) {
                throw new InvalidFrameException(identifier + " is invalid frame, realframeSize is:" + realFrameSize);
            }

            if (ignoreArtwork && AbstractID3v2Frame.isArtworkFrameId(frameId)) {
                buffer.skip(realFrameSize);
                frameBody = null;
            } else {
                //Read the body data
                if (((EncodingFlags) encodingFlags).isCompression()) {
                    Buffer decompressBuffer = AbstractID3v2Frame.decompressPartOfBuffer(buffer, realFrameSize, decompressedFrameSize);
                    if (((EncodingFlags) encodingFlags).isEncryption()) {
                        frameBody = readEncryptedBody(frameId, decompressBuffer, decompressedFrameSize);
                    } else {
                        frameBody = readBody(frameId, decompressBuffer, decompressedFrameSize);
                    }
                } else if (((EncodingFlags) encodingFlags).isEncryption()) {
                    frameBody = readEncryptedBody(identifier, buffer, frameSize);
                } else {
                    frameBody = readBody(frameId, buffer, realFrameSize);
                }
                //TODO code seems to assume that if the frame created is not a v23FrameBody
                //it should be deprecated, but what about if somehow a V24Frame has been put into a V23 Tag, shouldn't
                //it then be created as FrameBodyUnsupported
                if (!(frameBody instanceof ID3v23FrameBody)) {
                    LOG.log(DEBUG, "%s:Converted frameBody with:%s to deprecated frameBody", fileName, identifier);
                    frameBody = new FrameBodyDeprecated((AbstractID3v2FrameBody) frameBody);
                }
            }
        } catch (RuntimeException e) {
            LOG.log(DEBUG, "Unexpected :%s - %s", Strings.nullToEmpty(identifier), fileName, e);
            throw new InvalidFrameException("Buffer:" + buffer.size() + " " + Strings.nullToEmpty(identifier) +
                    " not valid ID3v2.30 frame " + fileName,
                    e);
        }
    }

    /**
     * Write the frame to bufferOutputStream
     */
    public void write(ByteArrayOutputStream tagBuffer) {
        LOG.log(DEBUG, "Writing frame to buffer:%s", getIdentifier());
        //This is where we will write header, move position to where we can
        //write body
        ByteBuffer headerBuffer = ByteBuffer.allocate(FRAME_HEADER_SIZE);

        //Write Frame Body Data
        ByteArrayOutputStream bodyOutputStream = new ByteArrayOutputStream();
        ((AbstractID3v2FrameBody) frameBody).write(bodyOutputStream);
        //Write Frame Header write Frame ID
        if (getIdentifier().length() == 3) {
            identifier = identifier + ' ';
        }
        headerBuffer.put(getIdentifier().getBytes(StandardCharsets.ISO_8859_1), 0, FRAME_ID_SIZE);
        //Write Frame Size
        int size = frameBody.getSize();
        LOG.log(INFO, "Frame Size Is:" + size);
        headerBuffer.putInt(frameBody.getSize());

        //Write the Flags
        //Status Flags:leave as they were when we read
        headerBuffer.put(statusFlags.getWriteFlags());

        //Remove any non standard flags
        ((EncodingFlags) encodingFlags).unsetNonStandardFlags();

        //Unset Compression flag if previously set because we uncompress previously compressed frames on write.
        ((EncodingFlags) encodingFlags).unsetCompression();
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

            //Add body to the Byte Array Output Stream
            tagBuffer.write(bodyOutputStream.toByteArray());
        } catch (IOException ioe) {
            //This could never happen coz not writing to file, so convert to RuntimeException
            throw new RuntimeException(ioe);
        }


    }

    public AbstractID3v2Frame.StatusFlags getStatusFlags() {
        return statusFlags;
    }

    public AbstractID3v2Frame.EncodingFlags getEncodingFlags() {
        return encodingFlags;
    }

    public int getEncryptionMethod() {
        return encryptionMethod;
    }

    public int getGroupIdentifier() {
        return groupIdentifier;
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

    /**
     * @return true if considered a common frame
     */
    public boolean isCommon() {
        return ID3v23Frames.getInstanceOf().isCommon(getId());
    }

    /**
     * @return true if considered a common frame
     */
    public boolean isBinary() {
        return ID3v23Frames.getInstanceOf().isBinary(getId());
    }

    /**
     * Sets the charset encoding used by the field.
     *
     * @param encoding charset.
     */
    public void setEncoding(Charset encoding) {
        try {
            byte encodingId = TextEncoding.getInstanceOf().getIdForCharset(encoding);
            if (encodingId < 2) {
                getBody().setTextEncoding(encodingId);
            }
        } catch (NoSuchElementException ignored) {
        }
    }

    /**
     * This represents a frame headers Status Flags
     * Make adjustments if necessary based on frame type and specification.
     */
    class StatusFlags extends AbstractID3v2Frame.StatusFlags {
        static final String TYPE_TAGALTERPRESERVATION = "typeTagAlterPreservation";
        static final String TYPE_FILEALTERPRESERVATION = "typeFileAlterPreservation";
        static final String TYPE_READONLY = "typeReadOnly";

        /**
         * Discard frame if tag altered
         */
        static final int MASK_TAG_ALTER_PRESERVATION = FileConstants.BIT7;

        /**
         * Discard frame if audio file part altered
         */
        static final int MASK_FILE_ALTER_PRESERVATION = FileConstants.BIT6;

        /**
         * Frame tagged as read only
         */
        static final int MASK_READ_ONLY = FileConstants.BIT5;

        StatusFlags() {
            originalFlags = (byte) 0;
            writeFlags = (byte) 0;
        }

        StatusFlags(byte flags) {
            originalFlags = flags;
            writeFlags = flags;
            modifyFlags();
        }


        /**
         * Use this constructor when convert a v24 frame
         */
        StatusFlags(ID3v24Frame.StatusFlags statusFlags) {
            originalFlags = convertV4ToV3Flags(statusFlags.getOriginalFlags());
            writeFlags = originalFlags;
            modifyFlags();
        }

        private byte convertV4ToV3Flags(byte v4Flag) {
            byte v3Flag = (byte) 0;
            if ((v4Flag & ID3v24Frame.StatusFlags.MASK_FILE_ALTER_PRESERVATION) != 0) {
                v3Flag |= (byte) MASK_FILE_ALTER_PRESERVATION;
            }
            if ((v4Flag & ID3v24Frame.StatusFlags.MASK_TAG_ALTER_PRESERVATION) != 0) {
                v3Flag |= (byte) MASK_TAG_ALTER_PRESERVATION;
            }
            return v3Flag;
        }

        void modifyFlags() {
            String str = getIdentifier();
            if (ID3v23Frames.getInstanceOf().isDiscardIfFileAltered(str)) {
                writeFlags |= (byte) MASK_FILE_ALTER_PRESERVATION;
                writeFlags &= (byte) ~MASK_TAG_ALTER_PRESERVATION;
            } else {
                writeFlags &= (byte) ~MASK_FILE_ALTER_PRESERVATION;
                writeFlags &= (byte) ~MASK_TAG_ALTER_PRESERVATION;
            }
        }

        public void createStructure() {
            MP3File.getStructureFormatter().openHeadingElement(TYPE_FLAGS, "");
            MP3File.getStructureFormatter().addElement(TYPE_TAGALTERPRESERVATION, originalFlags & MASK_TAG_ALTER_PRESERVATION);
            MP3File.getStructureFormatter().addElement(TYPE_FILEALTERPRESERVATION, originalFlags & MASK_FILE_ALTER_PRESERVATION);
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

        /**
         * Frame is compressed
         */
        static final int MASK_COMPRESSION = FileConstants.BIT7;

        /**
         * Frame is encrypted
         */
        static final int MASK_ENCRYPTION = FileConstants.BIT6;

        /**
         * Frame is part of a group
         */
        static final int MASK_GROUPING_IDENTITY = FileConstants.BIT5;

        EncodingFlags() {
        }

        EncodingFlags(byte flags) {
            super(flags);
            logEnabledFlags();
        }

        public void setCompression() {
            flags |= MASK_COMPRESSION;
        }

        public void setEncryption() {
            flags |= MASK_ENCRYPTION;
        }

        public void setGrouping() {
            flags |= MASK_GROUPING_IDENTITY;
        }

        void unsetCompression() {
            flags &= (byte) ~MASK_COMPRESSION;
        }

        public void unsetEncryption() {
            flags &= (byte) ~MASK_ENCRYPTION;
        }

        public void unsetGrouping() {
            flags &= (byte) ~MASK_GROUPING_IDENTITY;
        }

        boolean isNonStandardFlags() {
            return ((flags & FileConstants.BIT4) > 0) ||
                    ((flags & FileConstants.BIT3) > 0) ||
                    ((flags & FileConstants.BIT2) > 0) ||
                    ((flags & FileConstants.BIT1) > 0) ||
                    ((flags & FileConstants.BIT0) > 0);

        }

        void unsetNonStandardFlags() {
            if (isNonStandardFlags()) {
                LOG.log(WARN, "%s:%s:Unsetting Unknown Encoding Flags:%s", loggingFilename, getIdentifier(), Hex.asHex(flags));
                flags &= (byte) ~FileConstants.BIT4;
                flags &= (byte) ~FileConstants.BIT3;
                flags &= (byte) ~FileConstants.BIT2;
                flags &= (byte) ~FileConstants.BIT1;
                flags &= (byte) ~FileConstants.BIT0;
            }
        }


        void logEnabledFlags() {
            if (isNonStandardFlags()) {
                LOG.log(WARN, "%s:%s:Unknown Encoding Flags:%s", loggingFilename, identifier, Hex.asHex(flags));
            }
            if (isCompression()) {
                LOG.log(WARN, "%s:%s is compressed", loggingFilename, identifier);
            }

            if (isEncryption()) {
                LOG.log(WARN, "%s:%s is encrypted", loggingFilename, identifier);
            }

            if (isGrouping()) {
                LOG.log(WARN, "%s:%s is grouped", loggingFilename, identifier);
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


        public void createStructure() {
            MP3File.getStructureFormatter().openHeadingElement(TYPE_FLAGS, "");
            MP3File.getStructureFormatter().addElement(TYPE_COMPRESSION, flags & MASK_COMPRESSION);
            MP3File.getStructureFormatter().addElement(TYPE_ENCRYPTION, flags & MASK_ENCRYPTION);
            MP3File.getStructureFormatter().addElement(TYPE_GROUPIDENTITY, flags & MASK_GROUPING_IDENTITY);
            MP3File.getStructureFormatter().closeHeadingElement(TYPE_FLAGS);
        }
    }
}
