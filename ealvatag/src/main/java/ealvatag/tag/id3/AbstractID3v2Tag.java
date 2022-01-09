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
import static ealvatag.logging.ErrorMessage.MP3_UNABLE_TO_ADJUST_PADDING;
import static ealvatag.utils.Check.CANNOT_BE_NULL;
import static ealvatag.utils.Check.checkArgNotNull;
import static ealvatag.utils.Check.checkVarArg0NotNull;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.io.Files;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.TreeSet;

import ealvatag.audio.Utils;
import ealvatag.audio.exceptions.UnableToCreateFileException;
import ealvatag.audio.exceptions.UnableToModifyFileException;
import ealvatag.audio.exceptions.UnableToRenameFileException;
import ealvatag.audio.mp3.MP3File;
import ealvatag.logging.EalvaTagLog;
import ealvatag.logging.EalvaTagLog.JLogger;
import ealvatag.logging.EalvaTagLog.JLoggers;
import ealvatag.logging.ErrorMessage;
import ealvatag.logging.FileSystemMessage;
import ealvatag.tag.FieldDataInvalidException;
import ealvatag.tag.FieldKey;
import ealvatag.tag.InvalidFrameException;
import ealvatag.tag.Tag;
import ealvatag.tag.TagField;
import ealvatag.tag.TagFieldContainer;
import ealvatag.tag.UnsupportedFieldException;
import ealvatag.tag.datatype.DataTypes;
import ealvatag.tag.datatype.Pair;
import ealvatag.tag.datatype.PairedTextEncodedStringNullTerminated;
import ealvatag.tag.id3.framebody.AbstractArtworkFrameBody;
import ealvatag.tag.id3.framebody.AbstractFrameBodyNumberTotal;
import ealvatag.tag.id3.framebody.AbstractFrameBodyPairs;
import ealvatag.tag.id3.framebody.AbstractFrameBodyTextInfo;
import ealvatag.tag.id3.framebody.FrameBodyAPIC;
import ealvatag.tag.id3.framebody.FrameBodyCOMM;
import ealvatag.tag.id3.framebody.FrameBodyEncrypted;
import ealvatag.tag.id3.framebody.FrameBodyIPLS;
import ealvatag.tag.id3.framebody.FrameBodyPIC;
import ealvatag.tag.id3.framebody.FrameBodyPOPM;
import ealvatag.tag.id3.framebody.FrameBodyTIPL;
import ealvatag.tag.id3.framebody.FrameBodyTMCL;
import ealvatag.tag.id3.framebody.FrameBodyTXXX;
import ealvatag.tag.id3.framebody.FrameBodyUFID;
import ealvatag.tag.id3.framebody.FrameBodyUSLT;
import ealvatag.tag.id3.framebody.FrameBodyUnsupported;
import ealvatag.tag.id3.framebody.FrameBodyWOAR;
import ealvatag.tag.id3.framebody.FrameBodyWXXX;
import ealvatag.tag.id3.valuepair.ID3NumberTotalFields;
import ealvatag.tag.id3.valuepair.StandardIPLSKey;
import ealvatag.tag.images.Artwork;
import ealvatag.tag.images.ArtworkFactory;
import ealvatag.tag.reference.Languages;
import ealvatag.utils.Check;
import okio.Buffer;


/**
 * This is the abstract base class for all ID3v2 tags.
 *
 * @author : Paul Taylor
 * @author : Eric Farng
 * @version $Id$
 */
public abstract class AbstractID3v2Tag extends AbstractID3Tag implements TagFieldContainer {
    //Tag ID as held in file
    public static final byte[] TAG_ID = {'I', 'D', '3'};
    //The tag header is the same for ID3v2 versions
    public static final int TAG_HEADER_LENGTH = 10;
    public static final int FIELD_TAGID_LENGTH = 3;
    public static final int FIELD_TAG_MAJOR_VERSION_POS = 3;
    static final String TYPE_HEADER = "header";
    private static final String TAGID = "ID3";
    //  static final int TAGID_VERSIONS_FLAGS_SIZE_LENGTH = FIELD_TAGID_LENGTH + 7;  // Major, minor, flags 1 byte each, size 4
    private static final int FIELD_TAG_MAJOR_VERSION_LENGTH = 1;
    private static final int FIELD_TAG_MINOR_VERSION_LENGTH = 1;
    private static final int FIELD_TAG_FLAG_LENGTH = 1;
    private static final int FIELD_TAG_SIZE_LENGTH = 4;
    private static final String TYPE_BODY = "body";
    //    protected static final int FIELD_TAGID_POS = 0;
//    protected static final int FIELD_TAG_MINOR_VERSION_POS = 4;
//    protected static final int FIELD_TAG_FLAG_POS = 5;
//    protected static final int FIELD_TAG_SIZE_POS = 6;
    private static final int TAG_SIZE_INCREMENT = 100;
    /**
     * Holds the ids of invalid duplicate frames
     */
    private static final String TYPE_DUPLICATEFRAMEID = "duplicateFrameId";
    /**
     * Holds count the number of bytes used up by invalid duplicate frames
     */
    private static final String TYPE_DUPLICATEBYTES = "duplicateBytes";
    /**
     * Holds count the number bytes used up by empty frames
     */
    private static final String TYPE_EMPTYFRAMEBYTES = "emptyFrameBytes";
    /**
     * Holds the size of the tag as reported by the tag header
     */
    private static final String TYPE_FILEREADSIZE = "fileReadSize";
    /**
     * Holds count of invalid frames, (frames that could not be read)
     */
    private static final String TYPE_INVALIDFRAMES = "invalidFrames";
    private static final JLogger LOG = JLoggers.get(AbstractID3v2Tag.class, EalvaTagLog.MARKER);
    //The max size we try to write in one go to avoid out of memory errors (10mb)
    private static final long MAXIMUM_WRITABLE_CHUNK_SIZE = 10000000;
    /**
     * Map of all frames for this tag
     */
    public HashMap<String, Object> frameMap;
    /**
     * Map of all encrypted frames, these cannot be unencrypted by ealvatag
     */
    HashMap<String, Object> encryptedFrameMap;
    String duplicateFrameId = "";
    int duplicateBytes;
    int emptyFrameBytes;
    int fileReadSize;
    int invalidFrames;
    //Start location of this chunk
    //TODO currently only used by ID3 embedded into Wav/Aiff but shoudl be extended to mp3s
    private Long startLocationInFile;
    //End location of this chunk
    private Long endLocationInFile;

    public static Optional<Id3v2Header> getHeader(Buffer buffer) throws EOFException {
        buffer.require(10);

        for (int i = 0; i < TAG_ID.length; i++) {
            if (buffer.readByte() != TAG_ID[i]) {
                return Optional.absent();
            }
        }

        return Optional.of(new Id3v2Header(buffer.readByte(),
                buffer.readByte(),
                buffer.readByte(),
                ID3SyncSafeInteger.bufferToValue(buffer)));
    }


    /**
     * Determines if file contain an id3 tag and if so positions the file pointer just after the end
     * of the tag.
     * <p>
     * This method is used by non mp3s (such as .ogg and .flac) to determine if they contain an id3 tag
     */
    public static boolean isId3Tag(RandomAccessFile raf) throws IOException {
        if (!isID3V2Header(raf)) {
            return false;
        }
        //So we have a tag
        byte[] tagHeader = new byte[FIELD_TAG_SIZE_LENGTH];
        raf.seek(raf.getFilePointer() + FIELD_TAGID_LENGTH + FIELD_TAG_MAJOR_VERSION_LENGTH +
                FIELD_TAG_MINOR_VERSION_LENGTH + FIELD_TAG_FLAG_LENGTH);
        raf.read(tagHeader);
        ByteBuffer bb = ByteBuffer.wrap(tagHeader);

        int size = ID3SyncSafeInteger.bufferToValue(bb);
        raf.seek(size + TAG_HEADER_LENGTH);
        return true;
    }

    /**
     * True if files has a ID3v2 header
     */
    private static boolean isID3V2Header(RandomAccessFile raf) throws IOException {
        long start = raf.getFilePointer();
        byte[] tagIdentifier = new byte[FIELD_TAGID_LENGTH];
        raf.read(tagIdentifier);
        raf.seek(start);
        return Arrays.equals(tagIdentifier, TAG_ID);
    }

    public static boolean isId3Tag(FileChannel fc) throws IOException {
        if (!isID3V2Header(fc)) {
            return false;
        }
        //So we have a tag
        ByteBuffer bb = ByteBuffer.allocateDirect(FIELD_TAG_SIZE_LENGTH);
        fc.position(
                fc.position() + FIELD_TAGID_LENGTH + FIELD_TAG_MAJOR_VERSION_LENGTH + FIELD_TAG_MINOR_VERSION_LENGTH +
                        FIELD_TAG_FLAG_LENGTH);
        fc.read(bb);
        bb.flip();
        int size = ID3SyncSafeInteger.bufferToValue(bb);
        fc.position(size + TAG_HEADER_LENGTH);
        return true;
    }

    private static boolean isID3V2Header(FileChannel fc) throws IOException {
        long start = fc.position();
        ByteBuffer headerBuffer = Utils.readFileDataIntoBufferBE(fc, FIELD_TAGID_LENGTH);
        fc.position(start);
        String s = Utils.readThreeBytesAsChars(headerBuffer);
        return s.equals(TAGID);
    }

    /**
     * Checks to see if the file contains an ID3tag and if so return its size as reported in
     * the tag header  and return the size of the tag (including header), if no such tag exists return
     * zero.
     */
    static long getV2TagSizeIfExists(File file) throws IOException {
        FileInputStream fis = null;
        FileChannel fc = null;
        ByteBuffer bb;
        try {
            //Files
            fis = new FileInputStream(file);
            fc = fis.getChannel();

            //Read possible Tag header  Byte Buffer
            bb = ByteBuffer.allocate(TAG_HEADER_LENGTH);
            fc.read(bb);
            bb.flip();
            if (bb.limit() < (TAG_HEADER_LENGTH)) {
                return 0;
            }
        } finally {
            if (fc != null) {
                fc.close();
            }

            if (fis != null) {
                fis.close();
            }
        }

        //ID3 identifier
        byte[] tagIdentifier = new byte[FIELD_TAGID_LENGTH];
        bb.get(tagIdentifier, 0, FIELD_TAGID_LENGTH);
        if (!(Arrays.equals(tagIdentifier, TAG_ID))) {
            return 0;
        }

        //Is it valid Major Version
        byte majorVersion = bb.get();
        if ((majorVersion != ID3v22Tag.MAJOR_VERSION) && (majorVersion != ID3v23Tag.MAJOR_VERSION) &&
                (majorVersion != ID3v24Tag.MAJOR_VERSION)) {
            return 0;
        }

        //Skip Minor Version
        bb.get();

        //Skip Flags
        bb.get();

        //Get size as recorded in frame header
        int frameSize = ID3SyncSafeInteger.bufferToValue(bb);

        //addField header size to frame size
        frameSize += TAG_HEADER_LENGTH;
        return frameSize;
    }

//  public static long getV2TagSizeIfExists(FileOperator fileOperator) throws IOException {
//    try (Buffer buffer = new Buffer()) {
//      fileOperator.read(0, buffer, TAG_HEADER_LENGTH);
//
//      for (int i = 0; i < TAG_ID.length; i++) {
//        if (buffer.readByte() != TAG_ID[i]) {
//          return 0;
//        }
//      }
//
//      //Is it valid Major Version
//      byte majorVersion = buffer.readByte();
//      if ((majorVersion != ID3v22Tag.MAJOR_VERSION) && (majorVersion != ID3v23Tag.MAJOR_VERSION) &&
//          (majorVersion != ID3v24Tag.MAJOR_VERSION)) {
//        return 0;
//      }
//
//      //Skip Minor Version
//      buffer.readByte();
//
//      //Skip Flags
//      buffer.readByte();
//
//      //Get size as recorded in frame header
//      int frameSize = ID3SyncSafeInteger.bufferToValue(buffer);
//
//      //addField header size to frame size
//      frameSize += TAG_HEADER_LENGTH;
//      return frameSize;
//    }
//  }

    /**
     * Copy primitives apply to all tags
     */
    protected void copyPrimitives(AbstractID3v2Tag copyObject) {
        LOG.log(DEBUG, "Copying Primitives");
        //Primitives type variables common to all IDv2 Tags
        duplicateFrameId = copyObject.duplicateFrameId;
        duplicateBytes = copyObject.duplicateBytes;
        emptyFrameBytes = copyObject.emptyFrameBytes;
        fileReadSize = copyObject.fileReadSize;
        invalidFrames = copyObject.invalidFrames;
    }

    /**
     * Copy frames from another tag,
     */
    //TODO Copy Encrypted frames needs implementing
    void copyFrames(AbstractID3v2Tag copyObject) {
        frameMap = new LinkedHashMap<>();
        encryptedFrameMap = new LinkedHashMap<>();

        //Copy Frames that are a valid 2.4 type
        for (Object o1 : copyObject.frameMap.keySet()) {
            String id = (String) o1;
            Object o = copyObject.frameMap.get(id);
            //SingleFrames
            if (o instanceof AbstractID3v2Frame) {
                addFrame((AbstractID3v2Frame) o);
            } else if (o instanceof TyerTdatAggregatedFrame) {
                for (AbstractID3v2Frame next : ((TyerTdatAggregatedFrame) o).getFrames()) {
                    addFrame(next);
                }
            }
            //MultiFrames
            else if (o instanceof ArrayList) {
                for (AbstractID3v2Frame frame : (ArrayList<AbstractID3v2Frame>) o) {
                    addFrame(frame);
                }
            }
        }
    }

    /**
     * Add the frame converted to the correct version
     */
    protected abstract void addFrame(AbstractID3v2Frame frame);

    /**
     * Convert the frame to the correct frame(s)
     */
    protected abstract List<AbstractID3v2Frame> convertFrame(AbstractID3v2Frame frame) throws InvalidFrameException;

    /**
     * Returns the number of bytes which come from duplicate frames
     *
     * @return the number of bytes which come from duplicate frames
     */
    int getDuplicateBytes() {
        return duplicateBytes;
    }

    /**
     * Return the string which holds the ids of all
     * duplicate frames.
     *
     * @return the string which holds the ids of all duplicate frames.
     */
    String getDuplicateFrameId() {
        return duplicateFrameId;
    }

//  /**
//   * Returns the number of bytes which come from empty frames
//   *
//   * @return the number of bytes which come from empty frames
//   */
//  public int getEmptyFrameBytes() {
//    return emptyFrameBytes;
//  }

    /**
     * Return  byte count of invalid frames
     *
     * @return byte count of invalid frames
     */
    public int getInvalidFrames() {
        return invalidFrames;
    }

//  /**
//   * Returns the tag size as reported by the tag header
//   *
//   * @return the tag size as reported by the tag header
//   */
//  public int getFileReadBytes() {
//    return fileReadSize;
//  }

//  /**
//   * Return whether tag has frame with this identifier and a related body. This is required to protect
//   * against circumstances whereby a tag contains a frame with an unsupported body
//   * but happens to have an identifier that is valid for another version of the tag which it has been converted to
//   * <p>
//   * e.g TDRC is an invalid frame in a v23 tag but if somehow a v23tag has been created by another application
//   * with a TDRC frame we construct an UnsupportedFrameBody to hold it, then this library constructs a
//   * v24 tag, it will contain a frame with id TDRC but it will not have the expected frame body it is not really a
//   * TDRC frame.
//   *
//   * @param identifier frameId to lookup
//   *
//   * @return true if tag has frame with this identifier
//   */
//  public boolean hasFrameAndBody(String identifier) {
//    if (hasFrame(identifier)) {
//      Object o = getFrame(identifier);
//      if (o instanceof AbstractID3v2Frame) {
//        return !(((AbstractID3v2Frame)o).getBody() instanceof FrameBodyUnsupported);
//      }
//      return true;
//    }
//    return false;
//  }

    /**
     * Return whether tag has frame with this identifier
     * <p>
     * Warning the match is only done against the identifier so if a tag contains a frame with an unsupported body
     * but happens to have an identifier that is valid for another version of the tag it will return true
     *
     * @param identifier frameId to lookup
     * @return true if tag has frame with this identifier
     */
    public boolean hasFrame(String identifier) {
        return frameMap.containsKey(identifier);
    }

    /**
     * For single frames return the frame in this tag with given identifier if it exists, if multiple frames
     * exist with the same identifier it will return a list containing all the frames with this identifier
     * <p>
     * Warning the match is only done against the identifier so if a tag contains a frame with an unsupported body
     * but happens to have an identifier that is valid for another version of the tag it will be returned.
     *
     * @param identifier is an ID3Frame identifier
     * @return matching frame, or list of matching frames
     */
    //TODO:This method is problematic because sometimes it returns a list and sometimes a frame, we need to
    //replace with two separate methods as in the tag interface.
    public Object getFrame(String identifier) {
        return frameMap.get(identifier);
    }

//  /**
//   * Return whether tag has frame starting with this identifier
//   * <p>
//   * Warning the match is only done against the identifier so if a tag contains a frame with an unsupported body
//   * but happens to have an identifier that is valid for another version of the tag it will return true
//   *
//   * @param identifier start of frameId to lookup
//   *
//   * @return tag has frame starting with this identifier
//   */
//  public boolean hasFrameOfType(String identifier) {
//    for (String key : frameMap.keySet()) {
//      if (key.startsWith(identifier)) {
//        return true;
//      }
//    }
//    return false;
//  }

    /**
     * Return any encrypted frames with this identifier
     * <p>
     * <p>For single frames return the frame in this tag with given identifier if it exists, if multiple frames
     * exist with the same identifier it will return a list containing all the frames with this identifier
     */
    public Object getEncryptedFrame(String identifier) {
        return encryptedFrameMap.get(identifier);
    }

    /**
     * Add a frame to this tag
     *
     * @param frame the frame to add
     *              <p>
     *              <p>
     *              Warning if frame(s) already exists for this identifier that they are overwritten
     */
    //TODO needs to ensure do not addField an invalid frame for this tag
    //TODO what happens if already contains a list with this ID
    public void setFrame(AbstractID3v2Frame frame) {
        frameMap.put(frame.getIdentifier(), frame);
    }

    private List<TagField> getModifiableFieldList(String id) {
        Object o = getFrame(id);
        if (o == null) {
            return Lists.newArrayList();
        } else if (o instanceof List) {
            return (List<TagField>) o;
        } else if (o instanceof AbstractID3v2Frame) {
            return Lists.newArrayList((TagField) o);
        } else {
            throw new RuntimeException("Found entry in frameMap that was not a frame or a list:" + o);
        }
    }

    /**
     * Is this tag empty
     *
     * @see ealvatag.tag.Tag#isEmpty()
     */
    public boolean isEmpty() {
        return frameMap.size() == 0;
    }

    public boolean hasField(FieldKey genericKey) {
        return getFirstField(checkArgNotNull(genericKey, CANNOT_BE_NULL, "genericKey")).isPresent();
    }

    /**
     * Does this tag contain a field with the specified id
     *
     * @see ealvatag.tag.Tag#hasField(java.lang.String)
     */
    public boolean hasField(String id) {
        return hasFrame(id);
    }

    /**
     * Count number of frames/fields in this tag
     */
    public int getFieldCount() {
        Iterator<TagField> it = getFields();
        int count = 0;

        //Done this way because it.hasNext() incorrectly counts empty list
        //whereas it.next() works correctly
        try {
            while (true) {
                TagField next = it.next();
                count++;
            }
        } catch (NoSuchElementException nse) {
            //this is thrown when no more elements
        }
        return count;
    }

    public Tag setField(FieldKey genericKey, String... values) throws IllegalArgumentException,
            UnsupportedFieldException,
            FieldDataInvalidException {
        // create checks params
        TagField tagfield = createField(genericKey, values);
        setField(tagfield);
        return this;
    }

    public Tag addField(FieldKey genericKey, String... values) throws IllegalArgumentException,
            UnsupportedFieldException,
            FieldDataInvalidException {
        // create checks params
        TagField tagfield = createField(genericKey, values);
        addField(tagfield);
        return this;
    }

    public String getFirst(FieldKey genericKey) throws IllegalArgumentException, UnsupportedFieldException {
        return getValue(genericKey, 0).or("");
    }

    /**
     * Retrieve the first value that exists for this identifier
     * <p>
     * If the value is a String it returns that, otherwise returns a summary of the fields information
     */
    public String getFirst(String identifier) throws IllegalArgumentException, UnsupportedFieldException {
        AbstractID3v2Frame frame = (AbstractID3v2Frame) getFirstField(identifier).orNull();
        if (frame == null) {
            return "";
        }
        return getTextValueForFrame(frame);
    }

    private String getTextValueForFrame(AbstractID3v2Frame frame) {
        return frame.getBody().getUserFriendlyValue();
    }

    @Override
    public Optional<String> getValue(FieldKey genericKey) throws IllegalArgumentException {
        return getValue(genericKey, 0);
    }

    @Override
    public Optional<String> getValue(FieldKey genericKey, int index) throws IllegalArgumentException {
        checkArgNotNull(genericKey, CANNOT_BE_NULL, "genericKey");
        //Special case here because the generic key to frameid/subid mapping is identical for trackno versus tracktotal
        //and discno versus disctotal so we have to handle here, also want to ignore index parameter.
        if (ID3NumberTotalFields.isNumber(genericKey) || ID3NumberTotalFields.isTotal(genericKey)) {
            List<TagField> fields = getFields(genericKey);
            if (fields.size() > 0) {
                //Should only be one frame so ignore index value, and we ignore multiple values within the frame
                //it would make no sense if it existed.
                AbstractID3v2Frame frame = (AbstractID3v2Frame) fields.get(0);
                if (ID3NumberTotalFields.isNumber(genericKey)) {
                    return Optional.of(((AbstractFrameBodyNumberTotal) frame.getBody()).getNumberAsText());
                } else if (ID3NumberTotalFields.isTotal(genericKey)) {
                    return Optional.of(((AbstractFrameBodyNumberTotal) frame.getBody()).getTotalAsText());
                }
            } else {
                return Optional.absent();
            }
        } else if (genericKey == FieldKey.RATING) {
            //Special Case, TODO may be possible to put into doGetValueAtIndex but getUserFriendlyValue in POPMGFrameBody
            //is implemented different to what we would need.
            List<TagField> fields = getFields(genericKey);
            if (fields != null && fields.size() > index) {
                AbstractID3v2Frame frame = (AbstractID3v2Frame) fields.get(index);
                return Optional.of(String.valueOf(((FrameBodyPOPM) frame.getBody()).getRating()));
            } else {
                return Optional.absent();
            }
        }

        return doGetFieldValueAtIndex(getFrameAndSubIdFromGenericKey(genericKey), index);
    }

    public String getFieldAt(FieldKey genericKey, int index) throws IllegalArgumentException, UnsupportedFieldException {
        return getValue(genericKey, index).or("");
    }

    public List<String> getAll(FieldKey genericKey) throws IllegalArgumentException, UnsupportedFieldException {
        //Special case here because the generic key to frameid/subid mapping is identical for trackno versus tracktotal
        //and discno versus disctotal so we have to handle here, also want to ignore index parameter.
        List<String> values = new ArrayList<>();
        List<TagField> fields = getFields(genericKey);

        if (ID3NumberTotalFields.isNumber(genericKey)) {
            if (fields != null && fields.size() > 0) {
                AbstractID3v2Frame frame = (AbstractID3v2Frame) fields.get(0);
                values.add(((AbstractFrameBodyNumberTotal) frame.getBody()).getNumberAsText());
            }
            return values;
        } else if (ID3NumberTotalFields.isTotal(genericKey)) {
            if (fields != null && fields.size() > 0) {
                AbstractID3v2Frame frame = (AbstractID3v2Frame) fields.get(0);
                values.add(((AbstractFrameBodyNumberTotal) frame.getBody()).getTotalAsText());
            }
            return values;
        } else if (genericKey == FieldKey.RATING) {
            if (fields != null && fields.size() > 0) {
                AbstractID3v2Frame frame = (AbstractID3v2Frame) fields.get(0);
                values.add(String.valueOf(((FrameBodyPOPM) frame.getBody()).getRating()));
            }
            return values;
        } else {
            return doGetValues(getFrameAndSubIdFromGenericKey(genericKey));
        }
    }

    public Tag deleteField(FieldKey genericKey) throws IllegalArgumentException, UnsupportedFieldException {
        checkArgNotNull(genericKey, CANNOT_BE_NULL, "genericKey");
        FrameAndSubId formatKey = getFrameAndSubIdFromGenericKey(genericKey);

        switch (genericKey) {
            case TRACK:
                deleteNumberTotalFrame(formatKey, FieldKey.TRACK, FieldKey.TRACK_TOTAL, true);
                break;
            case TRACK_TOTAL:
                deleteNumberTotalFrame(formatKey, FieldKey.TRACK, FieldKey.TRACK_TOTAL, false);
                break;
            case DISC_NO:
                deleteNumberTotalFrame(formatKey, FieldKey.DISC_NO, FieldKey.DISC_TOTAL, true);
                break;
            case DISC_TOTAL:
                deleteNumberTotalFrame(formatKey, FieldKey.DISC_NO, FieldKey.DISC_TOTAL, false);
                break;
            case MOVEMENT_NO:
                deleteNumberTotalFrame(formatKey, FieldKey.MOVEMENT_NO, FieldKey.MOVEMENT_TOTAL, true);
                break;
            case MOVEMENT_TOTAL:
                deleteNumberTotalFrame(formatKey, FieldKey.MOVEMENT_NO, FieldKey.MOVEMENT_TOTAL, false);
                break;
            default:
                doDeleteTagField(formatKey);
        }
        return this;
    }

    public Tag setArtwork(Artwork artwork) throws IllegalArgumentException, UnsupportedFieldException, FieldDataInvalidException {
        setField(createArtwork(checkArgNotNull(artwork, CANNOT_BE_NULL, "artwork")));
        return this;
    }

    public Tag addArtwork(Artwork artwork) throws IllegalArgumentException, UnsupportedFieldException, FieldDataInvalidException {
        addField(createArtwork(checkArgNotNull(artwork, CANNOT_BE_NULL, "artwork")));
        return this;
    }

    public Optional<Artwork> getFirstArtwork() throws UnsupportedFieldException {
        List<Artwork> artwork = getArtworkList();
        if (artwork.size() > 0) {
            return Optional.of(artwork.get(0));
        }
        return Optional.absent();
    }

    public Tag deleteArtwork() throws UnsupportedFieldException {
        return deleteField(FieldKey.COVER_ART);
    }

    public boolean hasCommonFields() {
        return true;
    }

    /**
     * Return count of fields, this considers a text frame with two null separated values as two fields, if you want
     * a count of frames @see getFrameCount
     *
     * @return count of fields
     */
    public int getFieldCountIncludingSubValues() {
        Iterator<TagField> it = getFields();
        int count = 0;

        //Done this way because it.hasNext() incorrectly counts empty list
        //whereas it.next() works correctly
        try {
            while (true) {
                TagField next = it.next();
                if (next instanceof AbstractID3v2Frame) {
                    AbstractID3v2Frame frame = (AbstractID3v2Frame) next;
                    if ((frame.getBody() instanceof AbstractFrameBodyTextInfo) &&
                            !(frame.getBody() instanceof FrameBodyTXXX)) {
                        AbstractFrameBodyTextInfo frameBody = (AbstractFrameBodyTextInfo) frame.getBody();
                        count += frameBody.getNumberOfValues();
                        continue;
                    }
                }
                count++;
            }
        } catch (NoSuchElementException nse) {
            //this is thrown when no more elements
        }
        return count;
    }

    @Override
    public boolean setEncoding(Charset enc) throws FieldDataInvalidException {
        throw new UnsupportedOperationException("Not Implemented Yet");
    }

    public TagField createField(FieldKey genericKey, String... values) throws IllegalArgumentException,
            UnsupportedFieldException,
            FieldDataInvalidException {
        checkArgNotNull(genericKey, CANNOT_BE_NULL, "genericKey");
        String value = checkVarArg0NotNull(values, Check.AT_LEAST_ONE_REQUIRED, "values");

        FrameAndSubId formatKey = getFrameAndSubIdFromGenericKey(genericKey);

        //FrameAndSubId does not contain enough info for these fields to be able to work out what to update
        //that is why we need the extra processing here instead of doCreateTagField()
        if (ID3NumberTotalFields.isNumber(genericKey)) {
            AbstractID3v2Frame frame = createFrame(formatKey.getFrameId());
            AbstractFrameBodyNumberTotal framebody = (AbstractFrameBodyNumberTotal) frame.getBody();
            framebody.setNumber(value);
            return frame;
        } else if (ID3NumberTotalFields.isTotal(genericKey)) {
            AbstractID3v2Frame frame = createFrame(formatKey.getFrameId());
            AbstractFrameBodyNumberTotal framebody = (AbstractFrameBodyNumberTotal) frame.getBody();
            framebody.setTotal(value);
            return frame;
        } else {
            return doCreateTagField(formatKey, values);
        }
    }

    /**
     * {@inheritDoc}
     * This will return the number of underlying frames of this type, for example if you have added two TCOM field
     * values these will be stored within a single frame so only one field will be returned not two. This can be
     * confusing because getValues() would return two values.
     */
    public ImmutableList<TagField> getFields(FieldKey genericKey)
            throws IllegalArgumentException, UnsupportedFieldException {
        checkArgNotNull(genericKey, CANNOT_BE_NULL, "genericKey");
        FrameAndSubId formatKey = getFrameAndSubIdFromGenericKey(genericKey);

        //Get list of frames that this uses, as we are going to remove entries we don't want take a copy
        ImmutableList<TagField> list = getFields(formatKey.getFrameId());
        ImmutableList.Builder<TagField> filteredList = ImmutableList.builder();
        String subFieldId = formatKey.getSubId();

        if (subFieldId != null) {
            for (TagField tagfield : list) {
                AbstractTagFrameBody next = ((AbstractID3v2Frame) tagfield).getBody();
                if (next instanceof FrameBodyTXXX) {
                    if (((FrameBodyTXXX) next).getDescription().equals(formatKey.getSubId())) {
                        filteredList.add(tagfield);
                    }
                } else if (next instanceof FrameBodyWXXX) {
                    if (((FrameBodyWXXX) next).getDescription().equals(formatKey.getSubId())) {
                        filteredList.add(tagfield);
                    }
                } else if (next instanceof FrameBodyCOMM) {
                    if (((FrameBodyCOMM) next).getDescription().equals(formatKey.getSubId())) {
                        filteredList.add(tagfield);
                    }
                } else if (next instanceof FrameBodyUFID) {
                    if (((FrameBodyUFID) next).getOwner().equals(formatKey.getSubId())) {
                        filteredList.add(tagfield);
                    }
                } else if (next instanceof FrameBodyIPLS) {
                    for (Pair entry : ((FrameBodyIPLS) next).getPairing().getMapping()) {
                        if (entry.getKey().equals(formatKey.getSubId())) {
                            filteredList.add(tagfield);
                        }
                    }
                } else if (next instanceof FrameBodyTIPL) {
                    for (Pair entry : ((FrameBodyTIPL) next).getPairing().getMapping()) {
                        if (entry.getKey().equals(formatKey.getSubId())) {
                            filteredList.add(tagfield);
                        }
                    }
                } else if (next instanceof FrameBodyUnsupported) {
                    return list;
                } else {
                    throw new RuntimeException(
                            "Need to implement getFields(FieldKey genericKey) for:" + next.getClass());
                }
            }
            return filteredList.build();
        } else if (ID3NumberTotalFields.isNumber(genericKey)) {
            for (TagField tagfield : list) {
                AbstractTagFrameBody next = ((AbstractID3v2Frame) tagfield).getBody();
                if (next instanceof AbstractFrameBodyNumberTotal) {
                    if (((AbstractFrameBodyNumberTotal) next).getNumber() != null) {
                        filteredList.add(tagfield);
                    }
                }
            }
            return filteredList.build();
        } else if (ID3NumberTotalFields.isTotal(genericKey)) {
            for (TagField tagfield : list) {
                AbstractTagFrameBody next = ((AbstractID3v2Frame) tagfield).getBody();
                if (next instanceof AbstractFrameBodyNumberTotal) {
                    if (((AbstractFrameBodyNumberTotal) next).getTotal() != null) {
                        filteredList.add(tagfield);
                    }
                }
            }
            return filteredList.build();
        } else {
            return list;
        }
    }

    /**
     * @return iterator of all fields, multiple values for the same Id (e.g multiple TXXX frames) count as separate fields
     */
    public Iterator<TagField> getFields() {
        //Iterator of each different frameId in this tag
        final Iterator<Map.Entry<String, Object>> it = frameMap.entrySet().iterator();

        //Iterator used by hasNext() so doesn't effect next()
        final Iterator<Map.Entry<String, Object>> itHasNext = frameMap.entrySet().iterator();


        return new Iterator<TagField>() {
            Map.Entry<String, Object> latestEntry;

            //this iterates through frames through for a particular frameId
            private Iterator<TagField> fieldsIt;

            //TODO assumes if have entry its valid, but what if empty list but very different to check this
            //without causing a side effect on next() so leaving for now
            public boolean hasNext() {
                //Check Current frameId, does it contain more values
                if (fieldsIt != null) {
                    if (fieldsIt.hasNext()) {
                        return true;
                    }
                }

                //No remaining entries return false
                if (!itHasNext.hasNext()) {
                    return false;
                }

                //Issue #236
                //TODO assumes if have entry its valid, but what if empty list but very different to check this
                //without causing a side effect on next() so leaving for now
                return itHasNext.hasNext();
            }

            public TagField next() {
                //Hasn't been initialized yet
                if (fieldsIt == null) {
                    changeIt();
                }

                if (fieldsIt != null) {
                    //Go to the end of the run
                    if (!fieldsIt.hasNext()) {
                        changeIt();
                    }
                }

                if (fieldsIt == null) {
                    throw new NoSuchElementException();
                }
                return fieldsIt.next();
            }

            private void changeIt() {
                if (!it.hasNext()) {
                    return;
                }

                while (it.hasNext()) {
                    Map.Entry<String, Object> e = it.next();
                    latestEntry = itHasNext.next();
                    if (e.getValue() instanceof List) {
                        List<TagField> l = (List<TagField>) e.getValue();
                        //If list is empty (which it shouldn't be) we skip over this entry
                        if (l.size() != 0) {
                            fieldsIt = l.iterator();
                            break;
                        }
                    } else {
                        //TODO must be a better way
                        List<TagField> l = new ArrayList<>();
                        l.add((TagField) e.getValue());
                        fieldsIt = l.iterator();
                        break;
                    }
                }
            }

            public void remove() {
                fieldsIt.remove();
            }
        };
    }

    public ImmutableList<TagField> getFields(String id) {
        Object o = getFrame(id);
        if (o == null) {
            return ImmutableList.of();
        } else if (o instanceof List) {
            return ImmutableList.copyOf((List<TagField>) o);
        } else if (o instanceof AbstractID3v2Frame) {
            return ImmutableList.of((TagField) o);
        } else {
            throw new RuntimeException("Found entry in frameMap that was not a frame or a list:" + o);
        }
    }

    /**
     * Retrieve the first tag field that exists for this identifier
     *
     * @return tag field or null if doesn't exist
     */
    public Optional<TagField> getFirstField(String identifier) throws IllegalArgumentException, UnsupportedFieldException {
        Object object = getFrame(identifier);
        if (object instanceof List) {
            //noinspection unchecked
            return Optional.of(((List<TagField>) object).get(0));
        } else {
            return Optional.fromNullable((TagField) object);
        }
    }

    public Optional<TagField> getFirstField(FieldKey genericKey)
            throws IllegalArgumentException, UnsupportedFieldException {
        List<TagField> fields = getFields(genericKey);
        if (fields.size() > 0) {
            return Optional.fromNullable(fields.get(0));
        }
        return Optional.absent();
    }

    public TagField createCompilationField(boolean value) throws UnsupportedFieldException {
        try {
            return createField(FieldKey.IS_COMPILATION, value ? "1" : "0");
        } catch (FieldDataInvalidException e) {
            throw new RuntimeException(e); // should never happen. We know what valid data is. If thrown, library misconfiguration
        }
    }

    /**
     * Create Frame of correct ID3 version with the specified id
     */
    public abstract AbstractID3v2Frame createFrame(String id);

    /**
     * Create Frame for Id3 Key
     * <p>
     * Only textual data supported at the moment, should only be used with frames that
     * support a simple string argument.
     */
    TagField doCreateTagField(FrameAndSubId formatKey, String... values) throws IllegalArgumentException,
            UnsupportedFieldException,
            FieldDataInvalidException {
        String value = values[0];

        AbstractID3v2Frame frame = createFrame(formatKey.getFrameId());
        if (frame.getBody() instanceof FrameBodyUFID) {
            ((FrameBodyUFID) frame.getBody()).setOwner(formatKey.getSubId());
            ((FrameBodyUFID) frame.getBody()).setUniqueIdentifier(value.getBytes(StandardCharsets.ISO_8859_1));
        } else if (frame.getBody() instanceof FrameBodyTXXX) {
            ((FrameBodyTXXX) frame.getBody()).setDescription(formatKey.getSubId());
            ((FrameBodyTXXX) frame.getBody()).setText(value);
        } else if (frame.getBody() instanceof FrameBodyWXXX) {
            ((FrameBodyWXXX) frame.getBody()).setDescription(formatKey.getSubId());
            ((FrameBodyWXXX) frame.getBody()).setUrlLink(value);
        } else if (frame.getBody() instanceof FrameBodyCOMM) {
            //Set description if set
            if (formatKey.getSubId() != null) {
                ((FrameBodyCOMM) frame.getBody()).setDescription(formatKey.getSubId());
                //Special Handling for Media Monkey Compatability
                if (((FrameBodyCOMM) frame.getBody()).isMediaMonkeyFrame()) {
                    ((FrameBodyCOMM) frame.getBody()).setLanguage(Languages.MEDIA_MONKEY_ID);
                }
            }
            ((FrameBodyCOMM) frame.getBody()).setText(value);
        } else if (frame.getBody() instanceof FrameBodyUSLT) {
            ((FrameBodyUSLT) frame.getBody()).setDescription("");
            ((FrameBodyUSLT) frame.getBody()).setLyric(value);
        } else if (frame.getBody() instanceof FrameBodyWOAR) {
            ((FrameBodyWOAR) frame.getBody()).setUrlLink(value);
        } else if (frame.getBody() instanceof AbstractFrameBodyTextInfo) {
            ((AbstractFrameBodyTextInfo) frame.getBody()).setText(value);
        } else if (frame.getBody() instanceof FrameBodyPOPM) {
            ((FrameBodyPOPM) frame.getBody()).parseString(value);
        } else if (frame.getBody() instanceof FrameBodyIPLS) {
            if (formatKey.getSubId() != null) {
                ((FrameBodyIPLS) (frame.getBody())).addPair(formatKey.getSubId(), value);
            } else {
                if (values.length >= 2) {
                    ((FrameBodyIPLS) (frame.getBody())).addPair(values[0], values[1]);
                } else {
                    ((FrameBodyIPLS) (frame.getBody())).addPair(values[0]);
                }
            }
        } else if (frame.getBody() instanceof FrameBodyTIPL) {
            ((FrameBodyTIPL) (frame.getBody())).addPair(formatKey.getSubId(), value);
        } else if (frame.getBody() instanceof FrameBodyTMCL) {
            if (values.length >= 2) {
                ((FrameBodyTMCL) (frame.getBody())).addPair(values[0], values[1]);
            } else {
                ((FrameBodyTMCL) (frame.getBody())).addPair(values[0]);
            }
        } else if ((frame.getBody() instanceof FrameBodyAPIC) || (frame.getBody() instanceof FrameBodyPIC)) {
            throw new UnsupportedFieldException(ErrorMessage.ARTWORK_CANNOT_BE_CREATED_WITH_THIS_METHOD);
        } else {
            throw new FieldDataInvalidException(
                    "Field with key of:" + formatKey.getFrameId() + ":does not accept cannot parse data:" + value);
        }
        return frame;
    }

    protected abstract FrameAndSubId getFrameAndSubIdFromGenericKey(FieldKey genericKey) throws UnsupportedFieldException;

    public void setField(TagField field) throws FieldDataInvalidException {
        if ((!(field instanceof AbstractID3v2Frame)) && (!(field instanceof AggregatedFrame))) {
            throw new FieldDataInvalidException(
                    "Field " + field + " is not of type AbstractID3v2Frame nor AggregatedFrame");
        }

        if (field instanceof AbstractID3v2Frame) {
            AbstractID3v2Frame newFrame = (AbstractID3v2Frame) field;

            Object obj = frameMap.get(field.getId());


            //If no frame of this type exist or if multiples are not allowed
            if (obj == null) {
                frameMap.put(field.getId(), field);
            }
            //frame of this type already exists
            else if (obj instanceof AbstractID3v2Frame) {
                List<AbstractID3v2Frame> frames = new ArrayList<>();
                frames.add((AbstractID3v2Frame) obj);
                mergeDuplicateFrames(newFrame, frames);
            }
            //Multiple frames of this type already exist
            else if (obj instanceof List) {
                mergeDuplicateFrames(newFrame, (List<AbstractID3v2Frame>) obj);
            }
        } else
        //TODO not handling multiple aggregated frames of same type
        {
            frameMap.put(field.getId(), field);
        }
    }

    /**
     * Add new field
     * <p>
     * There is a special handling if adding another text field of the same type, in this case the value will
     * be appended to the existing field, separated by the null character.
     */
    public void addField(TagField field) throws FieldDataInvalidException {
        if (field == null) {
            return;
        }

        if ((!(field instanceof AbstractID3v2Frame)) && (!(field instanceof AggregatedFrame))) {
            throw new FieldDataInvalidException(
                    "Field " + field + " is not of type AbstractID3v2Frame or AggregatedFrame");
        }

        if (field instanceof AbstractID3v2Frame) {
            AbstractID3v2Frame frame = (AbstractID3v2Frame) field;

            Object o = frameMap.get(field.getId());

            //No frame of this type
            if (o == null) {
                frameMap.put(field.getId(), field);
            }
            //There are already frames of this type, adding another may need to merge
            else if (o instanceof List) {
                List<TagField> list = (List<TagField>) o;
                addNewFrameOrAddField(list, frameMap, null, frame);
            }
            //One frame exists, we are adding another so may need to convert to list
            else {
                AbstractID3v2Frame existingFrame = (AbstractID3v2Frame) o;
                List<TagField> list = new ArrayList<>();
                addNewFrameOrAddField(list, frameMap, existingFrame, frame);
            }
        } else {
            frameMap.put(field.getId(), field);
        }
    }

    /**
     * Handles adding of a new field that's shares a frame with other fields, so modifies the existing frame rather
     * than creating a new frame for these special cases
     */
    private void addNewFrameOrAddField(List<TagField> list,
                                       HashMap frameMap,
                                       AbstractID3v2Frame existingFrame,
                                       AbstractID3v2Frame frame) {
        ArrayList<TagField> mergedList = new ArrayList<>();
        if (existingFrame != null) {
            mergedList.add(existingFrame);
        } else {
            mergedList.addAll(list);
        }

        /*
         * If the frame is a TXXX frame then we add an extra string to the existing frame
         * if same description otherwise we create a new frame
         */
        if (frame.getBody() instanceof FrameBodyTXXX) {
            FrameBodyTXXX frameBody = (FrameBodyTXXX) frame.getBody();
            boolean match = false;
            for (TagField aMergedList : mergedList) {
                FrameBodyTXXX existingFrameBody = (FrameBodyTXXX) ((AbstractID3v2Frame) aMergedList).getBody();
                if (frameBody.getDescription().equals(existingFrameBody.getDescription())) {
                    existingFrameBody.addTextValue(frameBody.getText());
                    match = true;
                    break;
                }
            }
            if (!match) {
                addNewFrameToMap(list, frameMap, existingFrame, frame);
            }
        } else if (frame.getBody() instanceof FrameBodyWXXX) {
            FrameBodyWXXX frameBody = (FrameBodyWXXX) frame.getBody();
            boolean match = false;
            for (TagField aMergedList : mergedList) {
                FrameBodyWXXX existingFrameBody = (FrameBodyWXXX) ((AbstractID3v2Frame) aMergedList).getBody();
                if (frameBody.getDescription().equals(existingFrameBody.getDescription())) {
                    existingFrameBody.addUrlLink(frameBody.getUrlLink());
                    match = true;
                    break;
                }
            }
            if (!match) {
                addNewFrameToMap(list, frameMap, existingFrame, frame);
            }
        } else if (frame.getBody() instanceof AbstractFrameBodyTextInfo) {
            AbstractFrameBodyTextInfo frameBody = (AbstractFrameBodyTextInfo) frame.getBody();
            AbstractFrameBodyTextInfo existingFrameBody = (AbstractFrameBodyTextInfo) existingFrame.getBody();
            existingFrameBody.addTextValue(frameBody.getText());
        } else if (frame.getBody() instanceof AbstractFrameBodyPairs) {
            AbstractFrameBodyPairs frameBody = (AbstractFrameBodyPairs) frame.getBody();
            AbstractFrameBodyPairs existingFrameBody = (AbstractFrameBodyPairs) existingFrame.getBody();
            existingFrameBody.addPair(frameBody.getText());
        } else if (frame.getBody() instanceof AbstractFrameBodyNumberTotal) {
            AbstractFrameBodyNumberTotal frameBody = (AbstractFrameBodyNumberTotal) frame.getBody();
            AbstractFrameBodyNumberTotal existingFrameBody = (AbstractFrameBodyNumberTotal) existingFrame.getBody();

            if (frameBody.getNumber() != null && frameBody.getNumber() > 0) {
                existingFrameBody.setNumber(frameBody.getNumberAsText());
            }

            if (frameBody.getTotal() != null && frameBody.getTotal() > 0) {
                existingFrameBody.setTotal(frameBody.getTotalAsText());
            }
        } else {
            addNewFrameToMap(list, frameMap, existingFrame, frame);
        }
    }

    /**
     * Add another frame to the map
     */
    private void addNewFrameToMap(List<TagField> list,
                                  HashMap frameMap,
                                  AbstractID3v2Frame existingFrame,
                                  AbstractID3v2Frame frame) {
        if (list.size() == 0) {
            list.add(existingFrame);
            list.add(frame);
            frameMap.put(frame.getId(), list);
        } else {
            list.add(frame);
        }
    }

    /**
     * Add frame taking into account existing frames of the same type
     */
    private void mergeDuplicateFrames(AbstractID3v2Frame newFrame, List<AbstractID3v2Frame> frames) {
        for (ListIterator<AbstractID3v2Frame> li = frames.listIterator(); li.hasNext(); ) {
            AbstractID3v2Frame nextFrame = li.next();

            if (newFrame.getBody() instanceof FrameBodyTXXX) {
                //Value with matching key exists so replace
                if (((FrameBodyTXXX) newFrame.getBody()).getDescription()
                        .equals(((FrameBodyTXXX) nextFrame.getBody()).getDescription())) {
                    li.set(newFrame);
                    frameMap.put(newFrame.getId(), frames);
                    return;
                }
            } else if (newFrame.getBody() instanceof FrameBodyWXXX) {
                //Value with matching key exists so replace
                if (((FrameBodyWXXX) newFrame.getBody()).getDescription()
                        .equals(((FrameBodyWXXX) nextFrame.getBody()).getDescription())) {
                    li.set(newFrame);
                    frameMap.put(newFrame.getId(), frames);
                    return;
                }
            } else if (newFrame.getBody() instanceof FrameBodyCOMM) {
                if (((FrameBodyCOMM) newFrame.getBody()).getDescription()
                        .equals(((FrameBodyCOMM) nextFrame.getBody()).getDescription())) {
                    li.set(newFrame);
                    frameMap.put(newFrame.getId(), frames);
                    return;
                }
            } else if (newFrame.getBody() instanceof FrameBodyUFID) {
                if (((FrameBodyUFID) newFrame.getBody()).getOwner()
                        .equals(((FrameBodyUFID) nextFrame.getBody()).getOwner())) {
                    li.set(newFrame);
                    frameMap.put(newFrame.getId(), frames);
                    return;
                }
            } else if (newFrame.getBody() instanceof FrameBodyUSLT) {
                if (((FrameBodyUSLT) newFrame.getBody()).getDescription()
                        .equals(((FrameBodyUSLT) nextFrame.getBody()).getDescription())) {
                    li.set(newFrame);
                    frameMap.put(newFrame.getId(), frames);
                    return;
                }
            } else if (newFrame.getBody() instanceof FrameBodyPOPM) {
                if (((FrameBodyPOPM) newFrame.getBody()).getEmailToUser()
                        .equals(((FrameBodyPOPM) nextFrame.getBody()).getEmailToUser())) {
                    li.set(newFrame);
                    frameMap.put(newFrame.getId(), frames);
                    return;
                }
            }
            //e.g TRCK, TPOS, MVNM
            else if (newFrame.getBody() instanceof AbstractFrameBodyNumberTotal) {
                mergeNumberTotalFrames(newFrame, nextFrame);
                return;
            }
            //e.g TIPL IPLS, TMCL
            else if (newFrame.getBody() instanceof AbstractFrameBodyPairs) {
                AbstractFrameBodyPairs frameBody = (AbstractFrameBodyPairs) newFrame.getBody();
                AbstractFrameBodyPairs existingFrameBody = (AbstractFrameBodyPairs) nextFrame.getBody();
                existingFrameBody.addPair(frameBody.getText());
                return;
            }
        }

        if (!getID3Frames().isMultipleAllowed(newFrame.getId())) {
            frameMap.put(newFrame.getId(), newFrame);
        } else {
            //No match found so addField new one
            frames.add(newFrame);
            frameMap.put(newFrame.getId(), frames);
        }
    }

    protected abstract ID3Frames getID3Frames();

    /**
     * All Number/Count frames  are treated the same (TCK, TPOS, MVNM)
     */
    private void mergeNumberTotalFrames(AbstractID3v2Frame newFrame, AbstractID3v2Frame nextFrame) {
        AbstractFrameBodyNumberTotal newBody = (AbstractFrameBodyNumberTotal) newFrame.getBody();
        AbstractFrameBodyNumberTotal oldBody = (AbstractFrameBodyNumberTotal) nextFrame.getBody();

        if (newBody.getNumber() != null && newBody.getNumber() > 0) {
            oldBody.setNumber(newBody.getNumberAsText());
        }

        if (newBody.getTotal() != null && newBody.getTotal() > 0) {
            oldBody.setTotal(newBody.getTotalAsText());
        }
    }

    /**
     * Used for setting multiple frames for a single frame Identifier
     * <p>
     * Warning if frame(s) already exists for this identifier they are overwritten
     * TODO needs to ensure do not add an invalid frame for this tag
     */
    public void setFrame(String identifier, List<AbstractID3v2Frame> multiFrame) {
        LOG.log(TRACE, "Adding %s frames for %s", multiFrame.size(), identifier);
        frameMap.put(identifier, multiFrame);
    }

    /**
     * Return all frames which start with the identifier, this
     * can be more than one which is useful if trying to retrieve
     * similar frames e.g TIT1,TIT2,TIT3 ... and don't know exactly
     * which ones there are.
     * <p>
     * Warning the match is only done against the identifier so if a tag contains a frame with an unsupported body
     * but happens to have an identifier that is valid for another version of the tag it will be returned.
     *
     * @return an iterator of all the frames starting with a particular identifier
     */
    public Iterator getFrameOfType(String identifier) {
        Iterator<String> iterator = frameMap.keySet().iterator();
        HashSet<Object> result = new HashSet<>();
        String key;
        while (iterator.hasNext()) {
            key = iterator.next();
            if (key.startsWith(identifier)) {
                Object o = frameMap.get(key);
                if (o instanceof List) {
                    for (Object next : (List) o) {
                        result.add(next);
                    }
                } else {
                    result.add(o);
                }
            }
        }
        return result.iterator();
    }

    /**
     * Remove frame(s) with this identifier from tag
     *
     * @param identifier frameId to look for
     */
    public void removeFrame(String identifier) {
        LOG.log(TRACE, "Removing frame with identifier:%s", identifier);
        frameMap.remove(identifier);
    }

//  /**
//   * Remove all frame(s) which have an unsupported body, in other words
//   * remove all frames that are not part of the standard frameSet for
//   * this tag
//   */
//  public void removeUnsupportedFrames() {
//    for (Iterator i = iterator(); i.hasNext(); ) {
//      Object o = i.next();
//      if (o instanceof AbstractID3v2Frame) {
//        if (((AbstractID3v2Frame)o).getBody() instanceof FrameBodyUnsupported) {
//          LOG.log(LogLevel.TRACE, "Removing frame %s", ((AbstractID3v2Frame)o).getIdentifier());
//          i.remove();
//        }
//      }
//    }
//  }

    /**
     * Remove any frames starting with this identifier from tag
     *
     * @param identifier start of frameId to look for
     */
    void removeFrameOfType(String identifier) {
        //First fine matching keys
        HashSet<String> result;
        result = new HashSet<>();
        for (Object match : frameMap.keySet()) {
            String key = (String) match;
            if (key.startsWith(identifier)) {
                result.add(key);
            }
        }
        //Then deleteField outside of loop to prevent concurrent modification exception if there are two keys
        //with the same id
        for (String match : result) {
            LOG.log(TRACE, "Removing frame with identifier:%s because starts with:%s", match, identifier);
            frameMap.remove(match);
        }
    }

    /**
     * Write tag to file.
     *
     * @return new audioStartByte - different only if the audio content had to be moved
     */
    public abstract long write(File file, long audioStartByte) throws IOException;

    /**
     * Write tag to output stream
     */
    public void write(OutputStream outputStream) throws IOException {
        write(Channels.newChannel(outputStream), 0);
    }

    /**
     * Write tag to channel.
     */
    public void write(WritableByteChannel channel, int currentTagSize) throws IOException {
    }

    /**
     * Write tag to output stream
     */
    public void write(OutputStream outputStream, int currentTagSize) throws IOException {
        write(Channels.newChannel(outputStream), currentTagSize);
    }

    /**
     * Does a tag of the correct version exist in this file.
     *
     * @param byteBuffer to search through
     * @return true if tag exists.
     */
    public boolean seek(ByteBuffer byteBuffer) {
        byteBuffer.rewind();
        LOG.log(DEBUG, "ByteBuffer pos:%s:limit%s:cap", byteBuffer.position(), byteBuffer.limit(), byteBuffer.capacity());

        byte[] tagIdentifier = new byte[FIELD_TAGID_LENGTH];
        byteBuffer.get(tagIdentifier, 0, FIELD_TAGID_LENGTH);
        if (!(Arrays.equals(tagIdentifier, TAG_ID))) {
            return false;
        }
        //Major Version
        if (byteBuffer.get() != getMajorVersion()) {
            return false;
        }
        //Minor Version
        return byteBuffer.get() == getRevision();
    }

    public boolean seek(Buffer buffer) throws EOFException {
        byte[] tagIdentifier = new byte[FIELD_TAGID_LENGTH];
        buffer.read(tagIdentifier, 0, FIELD_TAGID_LENGTH);
        if (!(Arrays.equals(tagIdentifier, TAG_ID))) {
            return false;
        }
        //Major Version
        if (buffer.readByte() != getMajorVersion()) {
            return false;
        }
        //Minor Version
        return buffer.readByte() == getRevision();
    }

    /**
     * Write paddings byte to the channel
     */
    void writePadding(WritableByteChannel channel, int padding) throws IOException {
        if (padding > 0) {
            channel.write(ByteBuffer.wrap(new byte[padding]));
        }
    }

    /**
     * Write tag to file.
     */
    public void write(RandomAccessFile file) throws IOException {
        // TODO: 2/23/17 shouldn't we throw here?
    }

    /**
     * This method determines the total tag size taking into account
     * the preferredSize and the min size required for new tag. For mp3
     * preferred size is the location of the audio, for other formats
     * preferred size is the size of the existing tag
     */
    int calculateTagSize(int tagSize, int preferredSize) {
        // We can fit in the tag so no adjustments required
        if (tagSize <= preferredSize) {
            return preferredSize;
        }
        // There is not enough room as we need to move the audio file we might as well increase it more than necessary for future changes
        return tagSize + TAG_SIZE_INCREMENT;
    }

    /**
     * Delete Tag
     *
     * @param file to delete the tag from
     * @throws IOException if problem accessing the file
     */
    //TODO should clear all data and preferably recover lost space and go upto end of mp3s
    public void delete(RandomAccessFile file) throws IOException {
        // this works by just erasing the "ID3" tag at the beginning
        // of the file
        byte[] buffer = new byte[FIELD_TAGID_LENGTH];
        //Read into Byte Buffer
        FileChannel fc = file.getChannel();
        fc.position();
        ByteBuffer byteBuffer = ByteBuffer.allocate(TAG_HEADER_LENGTH);
        fc.read(byteBuffer, 0);
        byteBuffer.flip();
        if (seek(byteBuffer)) {
            file.seek(0L);
            file.write(buffer);
        }
    }

    /**
     * Write the data from the buffer to the file
     */
    void writeBufferToFile(File file,
                           ByteBuffer headerBuffer,
                           byte[] bodyByteBuffer,
                           int padding,
                           int sizeIncPadding,
                           long audioStartLocation) throws IOException {
        FileChannel fc = null;
        FileLock fileLock = null;

        //We need to adjust location of audio file if true
        if (sizeIncPadding > audioStartLocation) {
            LOG.log(TRACE, "Adjusting Padding");
            adjustPadding(file, sizeIncPadding, audioStartLocation);
        }

        try {
            fc = new RandomAccessFile(file, "rw").getChannel();
            fileLock = getFileLockForWriting(fc, file.getPath());
            fc.write(headerBuffer);
            fc.write(ByteBuffer.wrap(bodyByteBuffer));
            fc.write(ByteBuffer.wrap(new byte[padding]));
        } catch (FileNotFoundException fe) {
            LOG.log(ERROR, loggingFilename + fe.getMessage(), fe);
            if (fe.getMessage().contains(FileSystemMessage.ACCESS_IS_DENIED.getMsg()) ||
                    fe.getMessage().contains(FileSystemMessage.PERMISSION_DENIED.getMsg())) {
                LOG.log(ERROR, ErrorMessage.GENERAL_WRITE_FAILED_TO_OPEN_FILE_FOR_EDITING, file);
                throw new UnableToModifyFileException(ErrorMessage.GENERAL_WRITE_FAILED_TO_OPEN_FILE_FOR_EDITING, file);
            } else {
                LOG.log(ERROR, ErrorMessage.GENERAL_WRITE_FAILED_TO_OPEN_FILE_FOR_EDITING, file);
                throw new UnableToCreateFileException(String.format(Locale.getDefault(),
                        ErrorMessage.GENERAL_WRITE_FAILED_TO_OPEN_FILE_FOR_EDITING,
                        file));
            }
        } catch (IOException ioe) {
            LOG.log(ERROR, loggingFilename + ioe.getMessage(), ioe);
            if (ioe.getMessage().equals(FileSystemMessage.ACCESS_IS_DENIED.getMsg())) {
                LOG.log(ERROR, ErrorMessage.GENERAL_WRITE_FAILED_TO_OPEN_FILE_FOR_EDITING, file.getParentFile());
                throw new UnableToModifyFileException(ErrorMessage.GENERAL_WRITE_FAILED_TO_OPEN_FILE_FOR_EDITING, file.getParentFile());
            } else {
                LOG.log(ERROR, ErrorMessage.GENERAL_WRITE_FAILED_TO_OPEN_FILE_FOR_EDITING, file.getParentFile());
                throw new UnableToCreateFileException(ErrorMessage.GENERAL_WRITE_FAILED_TO_OPEN_FILE_FOR_EDITING, file.getParentFile());
            }
        } finally {
            if (fc != null) {
                if (fileLock != null) {
                    fileLock.release();
                }
                fc.close();
            }
        }
    }

    /**
     * Is this tag equivalent to another
     *
     * @param obj to test for equivalence
     * @return true if they are equivalent
     */
    public boolean equals(Object obj) {
        if (!(obj instanceof AbstractID3v2Tag)) {
            return false;
        }
        AbstractID3v2Tag object = (AbstractID3v2Tag) obj;
        return frameMap.equals(object.frameMap) && super.equals(obj);
    }

    /**
     * Get file lock for writing too file
     * <p>
     * TODO:this appears to have little effect on Windows Vista
     *
     * @return lock or null if locking is not supported
     * @throws IOException                                    if unable to get lock because already locked by another program
     * @throws java.nio.channels.OverlappingFileLockException if already locked by another thread in the same VM, we dont catch this because
     *                                                        indicates a programming error
     */
    private FileLock getFileLockForWriting(FileChannel fileChannel, String filePath) throws IOException {
        LOG.log(TRACE, "locking fileChannel for %s", filePath);
        FileLock fileLock;
        try {
            fileLock = fileChannel.tryLock();
        }
        //Assumes locking is not supported on this platform so just returns null
        catch (IOException | Error exception) {  //catching Error - #129 Workaround for https://bugs.openjdk.java.net/browse/JDK-8025619
            return null;
        }

        //Couldn't getFields lock because file is already locked by another application
        if (fileLock == null) {
            throw new IOException(String.format(Locale.getDefault(), ErrorMessage.GENERAL_WRITE_FAILED_FILE_LOCKED, filePath));
        }
        return fileLock;
    }

    /**
     * Return the frames in the order they were added
     *
     * @return and iterator of the frmaes/list of multi value frames
     */
    public Iterator iterator() {
        return frameMap.values().iterator();
    }

    /**
     * Adjust the length of the  padding at the beginning of the MP3 file, this is only called when there is currently
     * not enough space before the start of the audio to write the tag.
     * <p>
     * A new file will be created with enough size to fit the <code>ID3v2</code> tag.
     * The old file will be deleted, and the new file renamed.
     *
     * @param paddingSize This is total size required to store tag before audio
     * @param audioStart  beginning of the audio data
     * @param file        The file to adjust the padding length of
     * @throws FileNotFoundException if the file exists but is a directory rather than a regular file or cannot be opened for any other
     *                               reason
     * @throws IOException           on any I/O error
     */
    private void adjustPadding(File file, int paddingSize, long audioStart) throws IOException {
        LOG.log(DEBUG, "Need to move audio file to accommodate tag");
        FileChannel fcIn = null;
        FileChannel fcOut;

        //Create buffer holds the necessary padding
        ByteBuffer paddingBuffer = ByteBuffer.wrap(new byte[paddingSize]);

        //Create Temporary File and write channel, make sure it is locked
        File paddedFile;

        try {
            paddedFile = File.createTempFile(Utils.getBaseFilenameForTempFile(file), ".new", file.getParentFile());
            LOG.log(TRACE, "Created temp file:%s for %s", paddedFile, file);
        }
        //Vista:Can occur if have Write permission on folder this file would be created in Denied
        catch (IOException ioe) {
            if (ioe.getMessage().equals(FileSystemMessage.ACCESS_IS_DENIED.getMsg())) {
                LOG.log(ERROR,
                        ErrorMessage.GENERAL_WRITE_FAILED_TO_CREATE_TEMPORARY_FILE_IN_FOLDER,
                        file.getName(),
                        file.getParentFile(),
                        ioe);
                throw new UnableToCreateFileException(ioe,
                        ErrorMessage.GENERAL_WRITE_FAILED_TO_CREATE_TEMPORARY_FILE_IN_FOLDER,
                        file,
                        file.getParentFile());
            } else {
                LOG.log(ERROR,
                        ErrorMessage.GENERAL_WRITE_FAILED_TO_CREATE_TEMPORARY_FILE_IN_FOLDER,
                        file.getName(),
                        file.getParentFile(),
                        ioe);
                throw new UnableToCreateFileException(ioe,
                        ErrorMessage.GENERAL_WRITE_FAILED_TO_CREATE_TEMPORARY_FILE_IN_FOLDER,
                        file,
                        file.getParentFile());
            }
        }

        try {
            fcOut = new FileOutputStream(paddedFile).getChannel();
        }
        //Vista:Can occur if have special permission Create Folder/Append Data denied
        catch (FileNotFoundException ioe) {
            LOG.log(ERROR, ErrorMessage.GENERAL_WRITE_FAILED_TO_MODIFY_TEMPORARY_FILE_IN_FOLDER, file, file.getParentFile(), ioe);
            throw new UnableToModifyFileException(ErrorMessage.GENERAL_WRITE_FAILED_TO_MODIFY_TEMPORARY_FILE_IN_FOLDER,
                    file.getName(),
                    file.getParentFile());
        }

        try {
            //Create read channel from original file
            //TODO lock so cant be modified by anything else whilst reading from it ?
            fcIn = new FileInputStream(file).getChannel();

            //Write padding to new file (this is where the tag will be written to later)
            long written = fcOut.write(paddingBuffer);

            //Write rest of file starting from audio
            LOG.log(DEBUG, "Copying: %s bytes", file.length() - audioStart);

            //If the amount to be copied is very large we split into 10MB lumps to try and avoid
            //out of memory errors
            long audiolength = file.length() - audioStart;
            if (audiolength <= MAXIMUM_WRITABLE_CHUNK_SIZE) {
                fcIn.position(audioStart);
                long written2 = fcOut.transferFrom(fcIn, paddingSize, audiolength);
                LOG.log(DEBUG, "Written padding:%s Data%s", written, written2);
                if (written2 != audiolength) {
                    throw new RuntimeException(String.format(Locale.getDefault(), MP3_UNABLE_TO_ADJUST_PADDING, audiolength, written2));
                }
            } else {
                long noOfChunks = audiolength / MAXIMUM_WRITABLE_CHUNK_SIZE;
                long lastChunkSize = audiolength % MAXIMUM_WRITABLE_CHUNK_SIZE;
                long written2 = 0;
                for (int i = 0; i < noOfChunks; i++) {
                    written2 += fcIn.transferTo(audioStart + (i * MAXIMUM_WRITABLE_CHUNK_SIZE),
                            MAXIMUM_WRITABLE_CHUNK_SIZE,
                            fcOut);
                }
                written2 +=
                        fcIn.transferTo(audioStart + (noOfChunks * MAXIMUM_WRITABLE_CHUNK_SIZE), lastChunkSize, fcOut);
                LOG.log(DEBUG, "Written padding:%s Data:%s", written, written2);
                if (written2 != audiolength) {
                    throw new RuntimeException(String.format(Locale.getDefault(), MP3_UNABLE_TO_ADJUST_PADDING, audiolength, written2));
                }
            }

            //Store original modification time
            long lastModified = file.lastModified();

            //Close Channels and locks
            if (fcIn != null) {
                if (fcIn.isOpen()) {
                    fcIn.close();
                }
            }

            if (fcOut != null) {
                if (fcOut.isOpen()) {
                    fcOut.close();
                }
            }

            //Replace file with paddedFile
            replaceFile(file, paddedFile);

            //Update modification time
            //TODO is this the right file ?
            paddedFile.setLastModified(lastModified);
        } catch (UnableToRenameFileException ure) {
            paddedFile.delete();
            throw ure;
        } finally {
            try {
                //Whatever happens ensure all locks and channels are closed/released
                if (fcIn != null) {
                    if (fcIn.isOpen()) {
                        fcIn.close();
                    }
                }

                if (fcOut != null) {
                    if (fcOut.isOpen()) {
                        fcOut.close();
                    }
                }
            } catch (Exception e) {
                LOG.log(WARN, "Problem closing channels and locks", e);
            }
        }
    }

    /**
     * Replace originalFile with the contents of newFile
     * <p>
     * Both files must exist in the same folder so that there are no problems with filesystem mount points
     */
    private void replaceFile(File originalFile, File newFile) throws IOException {
        boolean renameOriginalResult;
        //Rename Original File to make a backup in case problem with new file
        File originalFileBackup = new File(originalFile.getAbsoluteFile().getParentFile().getPath(),
                Files.getNameWithoutExtension(originalFile.getPath()) + ".old");
        //If already exists modify the suffix
        int count = 1;
        while (originalFileBackup.exists()) {
            originalFileBackup = new File(originalFile.getAbsoluteFile().getParentFile().getPath(),
                    Files.getNameWithoutExtension(originalFile.getPath()) + ".old" + count);
            count++;
        }

        renameOriginalResult = originalFile.renameTo(originalFileBackup);
        if (!renameOriginalResult) {
            LOG.log(WARN, ErrorMessage.GENERAL_WRITE_FAILED_TO_RENAME_ORIGINAL_FILE_TO_BACKUP, originalFile, originalFileBackup);
            newFile.delete();
            throw new UnableToRenameFileException(ErrorMessage.GENERAL_WRITE_FAILED_TO_RENAME_ORIGINAL_FILE_TO_BACKUP,
                    originalFile,
                    originalFileBackup);
        }

        //Rename new Temporary file to the final file
        boolean renameResult = newFile.renameTo(originalFile);
        if (!renameResult) {
            //Renamed failed so lets do some checks rename the backup back to the original file
            //New File doesnt exist
            if (!newFile.exists()) {
                LOG.log(WARN, ErrorMessage.GENERAL_WRITE_FAILED_NEW_FILE_DOESNT_EXIST, newFile);
            }

            //Rename the backup back to the original
            renameOriginalResult = originalFileBackup.renameTo(originalFile);
            if (!renameOriginalResult) {
                //TODO now if this happens we are left with testfile.old instead of testfile.mp3
                LOG.log(WARN, ErrorMessage.GENERAL_WRITE_FAILED_TO_RENAME_ORIGINAL_BACKUP_TO_ORIGINAL, originalFileBackup, originalFile);
            }


            LOG.log(WARN, ErrorMessage.GENERAL_WRITE_FAILED_TO_RENAME_TO_ORIGINAL_FILE, originalFile, newFile);
            newFile.delete();
            throw new UnableToRenameFileException(ErrorMessage.GENERAL_WRITE_FAILED_TO_RENAME_TO_ORIGINAL_FILE, originalFile, newFile);
        } else {
            //Rename was okay so we can now deleteField the backup of the original
            boolean deleteResult = originalFileBackup.delete();
            if (!deleteResult) {
                //Not a disaster but can't deleteField the backup so make a warning
                LOG.log(WARN, ErrorMessage.GENERAL_WRITE_WARNING_UNABLE_TO_DELETE_BACKUP_FILE, originalFileBackup);
            }
        }
    }

    /**
     * Copy frame into map, whilst accounting for multiple frame of same type which can occur even if there were
     * not frames of the same type in the original tag
     */
    final void copyFrameIntoMap(String id, AbstractID3v2Frame newFrame) {
        if (frameMap.containsKey(newFrame.getIdentifier())) {
            Object o = frameMap.get(newFrame.getIdentifier());
            if (o instanceof AbstractID3v2Frame) {
                processDuplicateFrame(newFrame, (AbstractID3v2Frame) o);
            } else if (o instanceof AggregatedFrame) {
                LOG.log(ERROR, "Duplicated Aggregate Frame, ignoring:%s", id);
            } else if (o instanceof List) {
                List<AbstractID3v2Frame> list = (List) o;
                list.add(newFrame);
            } else {
                LOG.log(ERROR, "Unknown frame class:discarding:%s", o.getClass());
            }
        } else {
            frameMap.put(newFrame.getIdentifier(), newFrame);
        }
    }

    /**
     * If frame already exists default behaviour is to just add another one, but can be overrridden if
     * special handling required
     */
    protected void processDuplicateFrame(AbstractID3v2Frame newFrame, AbstractID3v2Frame existingFrame) {
        List<AbstractID3v2Frame> list = new ArrayList<>();
        list.add(existingFrame);
        list.add(newFrame);
        frameMap.put(newFrame.getIdentifier(), list);
    }

    /**
     * Add frame to the frame map
     */
    protected void loadFrameIntoMap(String frameId, AbstractID3v2Frame next) {
        if (next.getBody() instanceof FrameBodyEncrypted) {
            loadFrameIntoSpecifiedMap(encryptedFrameMap, frameId, next);
        } else {
            loadFrameIntoSpecifiedMap(frameMap, frameId, next);
        }
    }

    //TODO is this a special field?

    /**
     * Decides what to with the frame that has just been read from file.
     * If the frame is an allowable duplicate frame and is a duplicate we add all
     * frames into an ArrayList and add the ArrayList to the HashMap. if not allowed
     * to be duplicate we store the number of bytes in the duplicateBytes variable and discard
     * the frame itself.
     */
    protected void loadFrameIntoSpecifiedMap(HashMap<String, Object> map, String frameId, AbstractID3v2Frame next) {
        if ((ID3v24Frames.getInstanceOf().isMultipleAllowed(frameId)) ||
                (ID3v23Frames.getInstanceOf().isMultipleAllowed(frameId)) ||
                (ID3v22Frames.getInstanceOf().isMultipleAllowed(frameId))) {
            //If a frame already exists of this type
            if (map.containsKey(frameId)) {
                Object o = map.get(frameId);
                if (o instanceof ArrayList) {
                    @SuppressWarnings("unchecked") ArrayList<AbstractID3v2Frame> multiValues = (ArrayList<AbstractID3v2Frame>) o;
                    multiValues.add(next);
                    LOG.log(DEBUG, "Adding Multi Frame(1) %s", frameId);
                } else {
                    ArrayList<AbstractID3v2Frame> multiValues = new ArrayList<>();
                    multiValues.add((AbstractID3v2Frame) o);
                    multiValues.add(next);
                    map.put(frameId, multiValues);
                    LOG.log(DEBUG, "Adding Multi Frame(2) %s", frameId);
                }
            } else {
                LOG.log(DEBUG, "Adding Multi FrameList(3) %s", frameId);
                map.put(frameId, next);
            }
        }
        //If duplicate frame just stores the name of the frame and the number of bytes the frame contains
        else if (map.containsKey(frameId)) {
            LOG.log(WARN, "Ignoring Duplicate Frame %s", frameId);
            //If we have multiple duplicate frames in a tag separate them with semicolons
            if (duplicateFrameId.length() > 0) {
                duplicateFrameId += ";";
            }
            duplicateFrameId += frameId;
            duplicateBytes += ((AbstractID3v2Frame) frameMap.get(frameId)).getSize();
        } else {
            LOG.log(DEBUG, "Adding Frame %s", frameId);
            map.put(frameId, next);
        }
    }

    /**
     * Return tag size based upon the sizes of the tags rather than the physical
     * no of bytes between start of ID3Tag and start of Audio Data.Should be extended
     * by subclasses to include header.
     *
     * @return size of the tag
     */
    public int getSize() {
        int size = 0;
        Iterator iterator = frameMap.values().iterator();
        AbstractID3v2Frame frame;
        while (iterator.hasNext()) {
            Object o = iterator.next();
            if (o instanceof AbstractID3v2Frame) {
                frame = (AbstractID3v2Frame) o;
                size += frame.getSize();
            } else if (o instanceof AggregatedFrame) {
                AggregatedFrame af = (AggregatedFrame) o;
                for (AbstractID3v2Frame next : af.frames) {
                    size += next.getSize();
                }
            } else if (o instanceof List) {
                ArrayList<AbstractID3v2Frame> multiFrames = (ArrayList<AbstractID3v2Frame>) o;
                for (ListIterator<AbstractID3v2Frame> li = multiFrames.listIterator(); li.hasNext(); ) {
                    frame = li.next();
                    size += frame.getSize();
                }
            }
        }
        return size;
    }

    /**
     * Write all the frames to the byteArrayOutputStream
     * <p>
     * <p>Currently Write all frames, defaults to the order in which they were loaded, newly
     * created frames will be at end of tag.
     *
     * @return ByteBuffer Contains all the frames written within the tag ready for writing to file
     * @throws IOException if write error
     */
    ByteArrayOutputStream writeFramesToBuffer() throws IOException {
        ByteArrayOutputStream bodyBuffer = new ByteArrayOutputStream();
        writeFramesToBufferStream(frameMap, bodyBuffer);
        writeFramesToBufferStream(encryptedFrameMap, bodyBuffer);
        return bodyBuffer;
    }

    /**
     * Write frames in map to bodyBuffer
     *
     * @throws IOException if write error
     */
    private void writeFramesToBufferStream(Map<String, Object> map, ByteArrayOutputStream bodyBuffer) throws IOException {
        //Sort keys into Preferred Order
        TreeSet<String> sortedWriteOrder = new TreeSet<>(getPreferredFrameOrderComparator());
        sortedWriteOrder.addAll(map.keySet());

        AbstractID3v2Frame frame;
        for (String id : sortedWriteOrder) {
            Object o = map.get(id);
            if (o instanceof AbstractID3v2Frame) {
                frame = (AbstractID3v2Frame) o;
                frame.setLoggingFilename(loggingFilename);
                frame.write(bodyBuffer);
            } else if (o instanceof AggregatedFrame) {
                AggregatedFrame ag = (AggregatedFrame) o;
                for (AbstractID3v2Frame next : ag.getFrames()) {
                    next.setLoggingFilename(loggingFilename);
                    next.write(bodyBuffer);
                }
            } else {
                List<AbstractID3v2Frame> multiFrames = (List<AbstractID3v2Frame>) o;
                for (AbstractID3v2Frame nextFrame : multiFrames) {
                    nextFrame.setLoggingFilename(loggingFilename);
                    nextFrame.write(bodyBuffer);
                }
            }
        }
    }

    /**
     * @return comparator used to order frames in preferred order for writing to file so that most important frames are written first.
     */
    public abstract Comparator<String> getPreferredFrameOrderComparator();

    public void createStructure() {
        createStructureHeader();
        createStructureBody();
    }

    void createStructureHeader() {
        MP3File.getStructureFormatter().addElement(TYPE_DUPLICATEBYTES, duplicateBytes);
        MP3File.getStructureFormatter().addElement(TYPE_DUPLICATEFRAMEID, duplicateFrameId);
        MP3File.getStructureFormatter().addElement(TYPE_EMPTYFRAMEBYTES, emptyFrameBytes);
        MP3File.getStructureFormatter().addElement(TYPE_FILEREADSIZE, fileReadSize);
        MP3File.getStructureFormatter().addElement(TYPE_INVALIDFRAMES, invalidFrames);
    }


    void createStructureBody() {
        MP3File.getStructureFormatter().openHeadingElement(TYPE_BODY, "");

        AbstractID3v2Frame frame;
        for (Object o : frameMap.values()) {
            if (o instanceof AbstractID3v2Frame) {
                frame = (AbstractID3v2Frame) o;
                frame.createStructure();
            } else {
                ArrayList<AbstractID3v2Frame> multiFrames = (ArrayList<AbstractID3v2Frame>) o;
                for (ListIterator<AbstractID3v2Frame> li = multiFrames.listIterator(); li.hasNext(); ) {
                    frame = li.next();
                    frame.createStructure();
                }
            }
        }
        MP3File.getStructureFormatter().closeHeadingElement(TYPE_BODY);
    }

    /**
     * Create a list of values for this (sub)frame
     * <p>
     * This method  does all the complex stuff of splitting multiple values in one frame into separate values.
     *
     * @param formatKey frame and sub-id
     * @return the list of values
     */
    private List<String> doGetValues(FrameAndSubId formatKey) {
        List<String> values = new ArrayList<>();

        if (formatKey.getSubId() != null) {
            //Get list of frames that this uses
            List<TagField> list = getFields(formatKey.getFrameId());
            for (TagField aList : list) {
                AbstractTagFrameBody next = ((AbstractID3v2Frame) aList).getBody();

                if (next instanceof FrameBodyTXXX) {
                    if (((FrameBodyTXXX) next).getDescription().equals(formatKey.getSubId())) {
                        values.addAll((((FrameBodyTXXX) next).getValues()));
                    }
                } else if (next instanceof FrameBodyWXXX) {
                    if (((FrameBodyWXXX) next).getDescription().equals(formatKey.getSubId())) {
                        values.addAll((((FrameBodyWXXX) next).getUrlLinks()));
                    }
                } else if (next instanceof FrameBodyCOMM) {
                    if (((FrameBodyCOMM) next).getDescription().equals(formatKey.getSubId())) {
                        values.addAll((((FrameBodyCOMM) next).getValues()));
                    }
                } else if (next instanceof FrameBodyUFID) {
                    if (((FrameBodyUFID) next).getOwner().equals(formatKey.getSubId())) {
                        if (((FrameBodyUFID) next).getUniqueIdentifier() != null) {
                            values.add(new String(((FrameBodyUFID) next).getUniqueIdentifier()));
                        }
                    }
                } else if (next instanceof AbstractFrameBodyPairs) {
                    for (Pair entry : ((AbstractFrameBodyPairs) next).getPairing().getMapping()) {
                        if (entry.getKey().equals(formatKey.getSubId())) {
                            if (entry.getValue() != null) {
                                values.add(entry.getValue());
                            }
                        }
                    }
                } else {
                    throw new UnsupportedFieldException("Need to implement getFields(FieldKey genericKey) for:" + next.getClass());
                }
            }
        }
        //Special handling for paired fields with no defined key
        else if ((formatKey.getGenericKey() != null) &&
                ((formatKey.getGenericKey() == FieldKey.PERFORMER) ||
                        (formatKey.getGenericKey() == FieldKey.INVOLVED_PERSON))
        ) {
            List<TagField> list = getFields(formatKey.getFrameId());
            for (TagField aList : list) {
                AbstractTagFrameBody next = ((AbstractID3v2Frame) aList).getBody();
                if (next instanceof AbstractFrameBodyPairs) {
                    for (Pair entry : ((AbstractFrameBodyPairs) next).getPairing().getMapping()) {
                        if (!StandardIPLSKey.isKey(entry.getKey())) {
                            if (!entry.getValue().isEmpty()) {
                                if (!entry.getKey().isEmpty()) {
                                    values.add(entry.getPairValue());
                                } else {
                                    values.add(entry.getValue());
                                }
                            }
                        }
                    }
                }
            }
        }
        //Simple 1 to 1 mapping
        else {
            List<TagField> list = getFields(formatKey.getFrameId());
            for (TagField next : list) {
                AbstractID3v2Frame frame = (AbstractID3v2Frame) next;
                if (frame != null) {
                    if (frame.getBody() instanceof AbstractFrameBodyTextInfo) {
                        AbstractFrameBodyTextInfo fb = (AbstractFrameBodyTextInfo) frame.getBody();
                        values.addAll(fb.getValues());
                    } else {
                        values.add(getTextValueForFrame(frame));
                    }
                }
            }
        }
        return values;
    }

    /**
     * Get the value at the index, we massage the values so that the index as used in the generic interface rather
     * than simply taking the frame index. For example if two composers have been added then then they can be retrieved
     * individually using index=0, index=1 despite the fact that both internally will be stored in a single TCOM frame.
     *
     * @param formatKey frame and sub id
     * @param index     the index specified by the user
     */
    String doGetValueAtIndex(FrameAndSubId formatKey, int index) {
        List<String> values = doGetValues(formatKey);
        if (values.size() > index) {
            return values.get(index);
        }
        return "";
    }

    /**
     * Get the value at the index, we massage the values so that the index as used in the generic interface rather
     * than simply taking the frame index. For example if two composers have been added then then they can be retrieved
     * individually using index=0, index=1 despite the fact that both internally will be stored in a single TCOM frame.
     *
     * @param formatKey frame and sub id
     * @param index     the index specified by the user
     */
    private Optional<String> doGetFieldValueAtIndex(FrameAndSubId formatKey, int index) {
        List<String> values = doGetValues(formatKey);
        if (values.size() > index) {
            return Optional.of(values.get(index));
        }
        return Optional.absent();
    }

//  /**
//   * Create a link to artwork, this is not recommended because the link may be broken if the mp3 or image
//   * file is moved
//   *
//   * @param url specifies the link, it could be a local file or could be a full url
//   */
//  public TagField createLinkedArtworkField(String url) {
//    AbstractID3v2Frame frame = createFrame(getFrameAndSubIdFromGenericKey(FieldKey.COVER_ART).getFrameId());
//    if (frame.getBody() instanceof FrameBodyAPIC) {
//      FrameBodyAPIC body = (FrameBodyAPIC)frame.getBody();
//      body.setObjectValue(DataTypes.OBJ_PICTURE_DATA, url.getBytes(StandardCharsets.ISO_8859_1));
//      body.setObjectValue(DataTypes.OBJ_PICTURE_TYPE, PictureTypes.DEFAULT_ID);
//      body.setObjectValue(DataTypes.OBJ_MIME_TYPE, FrameBodyAPIC.IMAGE_IS_URL);
//      body.setObjectValue(DataTypes.OBJ_DESCRIPTION, "");
//    } else if (frame.getBody() instanceof FrameBodyPIC) {
//      FrameBodyPIC body = (FrameBodyPIC)frame.getBody();
//      body.setObjectValue(DataTypes.OBJ_PICTURE_DATA, url.getBytes(StandardCharsets.ISO_8859_1));
//      body.setObjectValue(DataTypes.OBJ_PICTURE_TYPE, PictureTypes.DEFAULT_ID);
//      body.setObjectValue(DataTypes.OBJ_IMAGE_FORMAT, FrameBodyAPIC.IMAGE_IS_URL);
//      body.setObjectValue(DataTypes.OBJ_DESCRIPTION, "");
//    }
//    return frame;
//  }

    /**
     * Some frames are used to store a number/total value, we have to consider both values when requested to delete a
     * key relating to one of them
     */
    private void deleteNumberTotalFrame(FrameAndSubId formatKey,
                                        FieldKey numberFieldKey,
                                        FieldKey totalFieldKey,
                                        boolean deleteNumberFieldKey) {
        if (deleteNumberFieldKey) {
            String total = getFirst(totalFieldKey);
            if (total.length() == 0) {
                doDeleteTagField(formatKey);
            } else {
                AbstractID3v2Frame frame = (AbstractID3v2Frame) getFrame(formatKey.getFrameId());
                AbstractFrameBodyNumberTotal frameBody = (AbstractFrameBodyNumberTotal) frame.getBody();
                frameBody.setNumber(0);
            }
        } else {
            String number = getFirst(numberFieldKey);
            if (number.length() == 0) {
                doDeleteTagField(formatKey);
            } else {
                AbstractID3v2Frame frame = (AbstractID3v2Frame) getFrame(formatKey.getFrameId());
                AbstractFrameBodyNumberTotal frameBody = (AbstractFrameBodyNumberTotal) frame.getBody();
                frameBody.setTotal(0);
            }
        }
    }

    void doDeleteTagField(FrameAndSubId formatKey) throws UnsupportedFieldException {
        if (formatKey.getSubId() != null) {
            //Get list of frames that this uses
            List<TagField> list = getModifiableFieldList(formatKey.getFrameId());
            ListIterator<TagField> li = list.listIterator();
            while (li.hasNext()) {
                AbstractTagFrameBody next = ((AbstractID3v2Frame) li.next()).getBody();
                if (next instanceof FrameBodyTXXX) {
                    if (((FrameBodyTXXX) next).getDescription().equals(formatKey.getSubId())) {
                        if (list.size() == 1) {
                            removeFrame(formatKey.getFrameId());
                        } else {
                            li.remove();
                        }
                    }
                } else if (next instanceof FrameBodyCOMM) {
                    if (((FrameBodyCOMM) next).getDescription().equals(formatKey.getSubId())) {
                        if (list.size() == 1) {
                            removeFrame(formatKey.getFrameId());
                        } else {
                            li.remove();
                        }
                    }
                } else if (next instanceof FrameBodyWXXX) {
                    if (((FrameBodyWXXX) next).getDescription().equals(formatKey.getSubId())) {
                        if (list.size() == 1) {
                            removeFrame(formatKey.getFrameId());
                        } else {
                            li.remove();
                        }
                    }
                } else if (next instanceof FrameBodyUFID) {
                    if (((FrameBodyUFID) next).getOwner().equals(formatKey.getSubId())) {
                        if (list.size() == 1) {
                            removeFrame(formatKey.getFrameId());
                        } else {
                            li.remove();
                        }
                    }
                }
                //A single TIPL frame is used for multiple fields, so we just delete the matching pairs rather than
                //deleting the frame itself unless now empty
                else if (next instanceof FrameBodyTIPL) {
                    PairedTextEncodedStringNullTerminated.ValuePairs pairs = ((FrameBodyTIPL) next).getPairing();
                    ListIterator<Pair> pairIterator = pairs.getMapping().listIterator();
                    while (pairIterator.hasNext()) {
                        Pair nextPair = pairIterator.next();
                        if (nextPair.getKey().equals(formatKey.getSubId())) {
                            pairIterator.remove();
                        }
                    }
                    if (pairs.getMapping().size() == 0) {
                        removeFrame(formatKey.getFrameId());
                    }
                }
                //A single IPLS frame is used for multiple fields, so we just delete the matching pairs rather than
                //deleting the frame itself unless now empty
                else if (next instanceof FrameBodyIPLS) {
                    PairedTextEncodedStringNullTerminated.ValuePairs pairs = ((FrameBodyIPLS) next).getPairing();
                    ListIterator<Pair> pairIterator = pairs.getMapping().listIterator();
                    while (pairIterator.hasNext()) {
                        Pair nextPair = pairIterator.next();
                        if (nextPair.getKey().equals(formatKey.getSubId())) {
                            pairIterator.remove();
                        }
                    }

                    if (pairs.getMapping().size() == 0) {
                        removeFrame(formatKey.getFrameId());
                    }
                } else {
                    throw new UnsupportedFieldException("Need to implement getFields(FieldKey genericKey) for:" + next.getClass());
                }
            }
        } else if ((formatKey.getGenericKey() != null) &&
                ((formatKey.getGenericKey() == FieldKey.PERFORMER) ||
                        (formatKey.getGenericKey() == FieldKey.INVOLVED_PERSON))
        ) {
            List<TagField> list = getFields(formatKey.getFrameId());
            for (TagField aList : list) {
                AbstractTagFrameBody next = ((AbstractID3v2Frame) aList).getBody();
                if (next instanceof AbstractFrameBodyPairs) {
                    PairedTextEncodedStringNullTerminated.ValuePairs pairs =
                            ((AbstractFrameBodyPairs) next).getPairing();
                    ListIterator<Pair> pairIterator = pairs.getMapping().listIterator();
                    while (pairIterator.hasNext()) {
                        Pair nextPair = pairIterator.next();
                        if (!StandardIPLSKey.isKey(nextPair.getKey())) {
                            pairIterator.remove();
                        }
                    }

                    if (pairs.getMapping().size() == 0) {
                        removeFrame(formatKey.getFrameId());
                    }
                }
            }
        }
        //Simple 1 to 1 mapping
        else if (formatKey.getSubId() == null) {
            removeFrame(formatKey.getFrameId());
        }
    }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder();
        out.append("Tag content:\n");
        Iterator<TagField> it = getFields();
        while (it.hasNext()) {
            TagField field = it.next();
            out.append("\t");
            out.append(field.getId());
            out.append(":");
            out.append(field);
            out.append("\n");
        }

        return out.toString();
    }

    public Long getStartLocationInFile() {
        return startLocationInFile;
    }

    public void setStartLocationInFile(long startLocationInFile) {
        this.startLocationInFile = startLocationInFile;
    }

    public Long getEndLocationInFile() {
        return endLocationInFile;
    }

    public void setEndLocationInFile(long endLocationInFile) {
        this.endLocationInFile = endLocationInFile;
    }

    public List<Artwork> getArtworkList() throws UnsupportedFieldException {
        List<TagField> coverartList = getFields(FieldKey.COVER_ART);
        List<Artwork> artworkList = new ArrayList<>(coverartList.size());

        for (TagField next : coverartList) {
            AbstractArtworkFrameBody coverArt = (AbstractArtworkFrameBody) ((AbstractID3v2Frame) next).getBody();
            Artwork artwork = ArtworkFactory.getNew();
            artwork.setMimeType(coverArt.getMimeType());
            artwork.setPictureType(coverArt.getPictureType());
            if (coverArt.isImageUrl()) {
                artwork.setLinked(true);
                artwork.setImageUrl(coverArt.getImageUrl());
            } else {
                artwork.setBinaryData(coverArt.getImageData());
            }
            artworkList.add(artwork);
        }
        return artworkList;
    }

    public TagField createArtwork(Artwork artwork) throws UnsupportedFieldException, FieldDataInvalidException {
        AbstractID3v2Frame frame = createFrame(getFrameAndSubIdFromGenericKey(FieldKey.COVER_ART).getFrameId());
        AbstractTagFrameBody body = frame.getBody();
        if (!artwork.isLinked()) {
            body.setObjectValue(DataTypes.OBJ_PICTURE_DATA, artwork.getBinaryData());
            body.setObjectValue(DataTypes.OBJ_PICTURE_TYPE, artwork.getPictureType());
            body.setObjectValue(getArtworkMimeTypeDataType(), getArtworkMimeType(artwork.getMimeType()));
            body.setObjectValue(DataTypes.OBJ_DESCRIPTION, "");
            return frame;
        } else {
            body.setObjectValue(DataTypes.OBJ_PICTURE_DATA, artwork.getImageUrl().getBytes(StandardCharsets.ISO_8859_1));
            body.setObjectValue(DataTypes.OBJ_PICTURE_TYPE, artwork.getPictureType());
            body.setObjectValue(getArtworkMimeTypeDataType(), FrameBodyAPIC.IMAGE_IS_URL);
            body.setObjectValue(DataTypes.OBJ_DESCRIPTION, "");
            return frame;
        }
    }

    /**
     * Get the {@link DataTypes} type for artwork. Defaults to {@link DataTypes#OBJ_MIME_TYPE} but V22 overrides this
     *
     * @return the DataTypes mime type for the tag version
     */
    protected String getArtworkMimeTypeDataType() {
        return DataTypes.OBJ_MIME_TYPE;
    }

//  /**
//   * Create an artwork field for either a FrameBodyPIC or FrameBodyAPIC.
//   *
//   * @param data     the raw image data
//   * @param mimeType image mime type
//   *
//   * @return an artwork TagField
//   *
//   * @see PictureTypes
//   */
//  public TagField createArtworkField(byte[] data, String mimeType) {
//    AbstractID3v2Frame frame = createFrame(getFrameAndSubIdFromGenericKey(FieldKey.COVER_ART).getFrameId());
//    AbstractTagFrameBody body = frame.getBody();
//    body.setObjectValue(DataTypes.OBJ_PICTURE_DATA, data);
//    body.setObjectValue(DataTypes.OBJ_PICTURE_TYPE, PictureTypes.DEFAULT_ID);
//    body.setObjectValue(getArtworkMimeTypeDataType(), getArtworkMimeType(mimeType));
//    body.setObjectValue(DataTypes.OBJ_DESCRIPTION, "");
//    return frame;
//  }

    /**
     * Be default simple returns the same mime type. Subclasses can map this to another values. V22 overrides this to map the value
     *
     * @param mimeType original mime type
     * @return same mime type or a possible mapping
     */
    protected String getArtworkMimeType(String mimeType) {
        return mimeType;
    }

    /**
     * This class had to be created to minimize the duplicate code in concrete subclasses
     * of this class. It is required in some cases when using the fieldKey enums because enums
     * cannot be sub classed. We want to use enums instead of regular classes because they are
     * much easier for end users to  to use.
     */
    class FrameAndSubId {
        private final FieldKey genericKey;
        private final String frameId;
        private final String subId;

        FrameAndSubId(FieldKey genericKey, String frameId, String subId) {
            this.genericKey = genericKey;
            this.frameId = frameId;
            this.subId = subId;
        }

        public FieldKey getGenericKey() {
            return genericKey;
        }

        public String getFrameId() {
            return frameId;
        }

        String getSubId() {
            return subId;
        }

    }


}
