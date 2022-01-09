/*
 * @author : Paul Taylor
 * @author : Eric Farng
 * <p>
 * Version @version:$Id$
 * <p>
 * MusicTag Copyright (C)2003,2004
 * <p>
 * This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public  License as
 * published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version.
 * <p>
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public License along with this library; if not, you can get a copy from
 * http://www.opensource.org/licenses/lgpl-license.php or write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
 * Boston, MA 02110-1301 USA
 * <p>
 * Description: This class is for a ID3v1.1 Tag
 */
package ealvatag.tag.id3;

import static ealvatag.logging.EalvaTagLog.LogLevel.DEBUG;
import static ealvatag.logging.EalvaTagLog.LogLevel.WARN;
import static ealvatag.utils.Check.CANNOT_BE_NULL;
import static ealvatag.utils.Check.checkArgNotNull;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;

import ealvatag.audio.io.FileOperator;
import ealvatag.audio.mp3.MP3File;
import ealvatag.logging.EalvaTagLog;
import ealvatag.logging.EalvaTagLog.JLogger;
import ealvatag.logging.EalvaTagLog.JLoggers;
import ealvatag.tag.FieldKey;
import ealvatag.tag.Tag;
import ealvatag.tag.TagException;
import ealvatag.tag.TagField;
import ealvatag.tag.TagNotFoundException;
import ealvatag.tag.TagOptionSingleton;
import ealvatag.tag.UnsupportedFieldException;
import ealvatag.tag.id3.framebody.FrameBodyCOMM;
import ealvatag.tag.id3.framebody.FrameBodyTALB;
import ealvatag.tag.id3.framebody.FrameBodyTCON;
import ealvatag.tag.id3.framebody.FrameBodyTDRC;
import ealvatag.tag.id3.framebody.FrameBodyTIT2;
import ealvatag.tag.id3.framebody.FrameBodyTPE1;
import ealvatag.tag.id3.framebody.FrameBodyTRCK;
import ealvatag.tag.reference.GenreTypes;
import ealvatag.utils.StandardCharsets;

/**
 * Represents an ID3v11 tag.
 *
 * @author : Eric Farng
 * @author : Paul Taylor
 */
public class ID3v11Tag extends ID3v1Tag {
    private static final JLogger LOG = JLoggers.get(ID3v11Tag.class, EalvaTagLog.MARKER);

    //For writing output
    private static final String TYPE_TRACK = "track";

    private static final int TRACK_UNDEFINED = 0;
    private static final int TRACK_MAX_VALUE = 255;
    private static final int TRACK_MIN_VALUE = 1;

    private static final int FIELD_COMMENT_LENGTH = 28;
    private static final int FIELD_COMMENT_POS = 97;

    @SuppressWarnings("unused")
    private static final int FIELD_TRACK_INDICATOR_LENGTH = 1;
    private static final int FIELD_TRACK_INDICATOR_POS = 125;

    @SuppressWarnings("unused")
    private static final int FIELD_TRACK_LENGTH = 1;
    private static final int FIELD_TRACK_POS = 126;
    private static final byte RELEASE = 1;
    private static final byte MAJOR_VERSION = 1;
    private static final byte REVISION = 0;
    /**
     * Track is held as a single byte in v1.1
     */
    protected byte track = (byte) TRACK_UNDEFINED;

    /**
     * Creates a new ID3v11 datatype.
     */
    public ID3v11Tag() {

    }

    public ID3v11Tag(ID3v11Tag copyObject) {
        super(copyObject);
        track = copyObject.track;
    }

    /**
     * Creates a new ID3v11 datatype from a non v11 tag
     *
     * @param mp3tag the base ID3 tag
     * @throws UnsupportedOperationException if copy ctor not called and should type cast the arg
     */
    public ID3v11Tag(BaseID3Tag mp3tag) {
        if (mp3tag != null) {
            if (mp3tag instanceof ID3v1Tag) {
                if (mp3tag instanceof ID3v11Tag) {
                    throw new UnsupportedOperationException("Copy Constructor not called. Please type cast the argument");
                }
                // id3v1_1 objects are also id3v1 objects
                ID3v1Tag id3old = (ID3v1Tag) mp3tag;
                title = id3old.title;
                artist = id3old.artist;
                album = id3old.album;
                comment = id3old.comment;
                year = id3old.year;
                genre = id3old.genre;
            } else {
                ID3v24Tag id3tag;
                // first change the tag to ID3v2_4 tag if not one already
                if (!(mp3tag instanceof ID3v24Tag)) {
                    id3tag = new ID3v24Tag(mp3tag);
                } else {
                    id3tag = (ID3v24Tag) mp3tag;
                }
                ID3v24Frame frame;
                String text;
                if (id3tag.hasFrame(ID3v24Frames.FRAME_ID_TITLE)) {
                    frame = (ID3v24Frame) id3tag.getFrame(ID3v24Frames.FRAME_ID_TITLE);
                    text = ((FrameBodyTIT2) frame.getBody()).getText();
                    title = ID3Tags.truncate(text, FIELD_TITLE_LENGTH);
                }
                if (id3tag.hasFrame(ID3v24Frames.FRAME_ID_ARTIST)) {
                    frame = (ID3v24Frame) id3tag.getFrame(ID3v24Frames.FRAME_ID_ARTIST);
                    text = ((FrameBodyTPE1) frame.getBody()).getText();
                    artist = ID3Tags.truncate(text, FIELD_ARTIST_LENGTH);
                }
                if (id3tag.hasFrame(ID3v24Frames.FRAME_ID_ALBUM)) {
                    frame = (ID3v24Frame) id3tag.getFrame(ID3v24Frames.FRAME_ID_ALBUM);
                    text = ((FrameBodyTALB) frame.getBody()).getText();
                    album = ID3Tags.truncate(text, FIELD_ALBUM_LENGTH);
                }
                if (id3tag.hasFrame(ID3v24Frames.FRAME_ID_YEAR)) {
                    frame = (ID3v24Frame) id3tag.getFrame(ID3v24Frames.FRAME_ID_YEAR);
                    text = ((FrameBodyTDRC) frame.getBody()).getText();
                    year = ID3Tags.truncate(text, FIELD_YEAR_LENGTH);
                }

                if (id3tag.hasFrame(ID3v24Frames.FRAME_ID_COMMENT)) {
                    Iterator iterator = id3tag.getFrameOfType(ID3v24Frames.FRAME_ID_COMMENT);
                    text = "";
                    while (iterator.hasNext()) {
                        frame = (ID3v24Frame) iterator.next();
                        text += (((FrameBodyCOMM) frame.getBody()).getText() + " ");
                    }
                    comment = ID3Tags.truncate(text, FIELD_COMMENT_LENGTH);
                }
                if (id3tag.hasFrame(ID3v24Frames.FRAME_ID_GENRE)) {
                    frame = (ID3v24Frame) id3tag.getFrame(ID3v24Frames.FRAME_ID_GENRE);
                    text = ((FrameBodyTCON) frame.getBody()).getText();
                    try {
                        genre = (byte) ID3Tags.findNumber(text);
                    } catch (TagException ex) {
                        Integer genreId = GenreTypes.getInstanceOf().getIdForValue(text);
                        if (null != genreId) {
                            genre = genreId.byteValue();
                        } else {
                            LOG.log(WARN, "%s:Unable to convert TCON frame to format suitable for v11 tag", loggingFilename, ex);
                            genre = (byte) ID3v1Tag.GENRE_UNDEFINED;
                        }
                    }
                }
                if (id3tag.hasFrame(ID3v24Frames.FRAME_ID_TRACK)) {
                    frame = (ID3v24Frame) id3tag.getFrame(ID3v24Frames.FRAME_ID_TRACK);
                    track = (byte) ((FrameBodyTRCK) frame.getBody()).getTrackNo().intValue();
                }
            }
        }
    }

    public ID3v11Tag(FileOperator fileOperator, String loggingFilename) throws TagNotFoundException, IOException {
        FileChannel fc = fileOperator.getFileChannel();
        setLoggingFilename(loggingFilename);
        ByteBuffer byteBuffer = ByteBuffer.allocate(TAG_LENGTH);

        fc.position(fc.size() - TAG_LENGTH);

        fc.read(byteBuffer);
        byteBuffer.flip();
        read(byteBuffer);

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

    public int getFieldCount() {
        return 7;
    }

    /**
     * Set Comment
     */
    public void setComment(String comment) {
        checkArgNotNull(comment);
        this.comment = ID3Tags.truncate(comment, FIELD_COMMENT_LENGTH);
    }

    /**
     * Get Comment
     *
     * @return comment
     */
    public String getFirstComment() {
        return comment;
    }

    /**
     * Return the track number as a String.
     *
     * @return track
     */

    public String getFirstTrack() {
        return String.valueOf(track & BYTE_TO_UNSIGNED);
    }

    @SuppressWarnings("unused")
    public void addTrack(String track) {
        setTrack(track);
    }

    public ImmutableList<TagField> getTrack() {
        String firstTrack = getFirst(FieldKey.TRACK);
        if (firstTrack.length() > 0) {
            return ImmutableList.<TagField>of(new ID3v1TagField(ID3v1FieldKey.TRACK.name(), firstTrack));
        } else {
            return ImmutableList.of();
        }
    }

    /**
     * Set the track, v11 stores track numbers in a single byte value so can only
     * handle a simple number in the range 0-255.
     */

    public void setTrack(String trackValue) {
        int trackAsInt;
        //Try and convert String representation of track into an integer
        try {
            trackAsInt = Integer.parseInt(trackValue);
        } catch (NumberFormatException e) {
            trackAsInt = 0;
        }

        //This value cannot be held in v1_1
        if ((trackAsInt > TRACK_MAX_VALUE) || (trackAsInt < TRACK_MIN_VALUE)) {
            track = (byte) TRACK_UNDEFINED;
        } else {
            track = (byte) Integer.parseInt(trackValue);
        }
    }

    public void setField(TagField field) {
        FieldKey genericKey = FieldKey.valueOf(field.getId());
        if (genericKey == FieldKey.TRACK) {
            setTrack(field.toString());
        } else {
            super.setField(field);
        }
    }


    public ImmutableList<TagField> getFields(FieldKey genericKey)
            throws IllegalArgumentException, UnsupportedFieldException {
        if (genericKey == FieldKey.TRACK) {
            return getTrack();
        } else {
            return super.getFields(genericKey);
        }
    }

    @Override
    public ImmutableSet<FieldKey> getSupportedFields() {
        return tagFieldToID3v11Field.keySet();
    }

    public String getFirst(FieldKey genericKey) throws IllegalArgumentException, UnsupportedFieldException {
        switch (genericKey) {
            case ARTIST:
                return getFirstArtist();

            case ALBUM:
                return getFirstAlbum();

            case TITLE:
                return getFirstTitle();

            case GENRE:
                return getFirstGenre();

            case YEAR:
                return getFirstYear();

            case TRACK:
                return getFirstTrack();

            case COMMENT:
                return getFirstComment();

            default:
                return "";
        }
    }

    public Optional<TagField> getFirstField(String id) throws IllegalArgumentException, UnsupportedFieldException {
        List<TagField> results;

        if (FieldKey.TRACK.name().equals(id)) {
            results = getTrack();
            if (results != null) {
                if (results.size() > 0) {
                    return Optional.fromNullable(results.get(0));
                }
            }
            return Optional.absent();
        } else {
            return super.getFirstField(id);
        }
    }

    public boolean isEmpty() {
        return track <= 0 && super.isEmpty();
    }

    public Tag deleteField(FieldKey genericKey) throws IllegalArgumentException, UnsupportedFieldException {
        checkArgNotNull(genericKey, CANNOT_BE_NULL, "genericKey");
        if (genericKey == FieldKey.TRACK) {
            track = 0;
        } else {
            super.deleteField(genericKey);
        }
        return this;
    }

    @Override
    protected ImmutableMap<FieldKey, ID3v1FieldKey> getFieldMap() {
        return tagFieldToID3v11Field;
    }

    /**
     * Compares Object with this only returns true if both v1_1 tags with all
     * fields set to same value
     *
     * @param obj Comparing Object
     */
    public boolean equals(Object obj) {
        if (!(obj instanceof ID3v11Tag)) {
            return false;
        }
        ID3v11Tag object = (ID3v11Tag) obj;
        return track == object.track && super.equals(obj);
    }


    /**
     * Find identifier within byteBuffer to indicate that a v11 tag exists within the buffer
     *
     * @return true if find header for v11 tag within buffer
     */
    public boolean seek(ByteBuffer byteBuffer) {
        byte[] buffer = new byte[FIELD_TAGID_LENGTH];
        // read the TAG value
        byteBuffer.get(buffer, 0, FIELD_TAGID_LENGTH);
        if (!(Arrays.equals(buffer, TAG_ID))) {
            return false;
        }

        // Check for the empty byte before the TRACK
        byteBuffer.position(FIELD_TRACK_INDICATOR_POS);
        if (byteBuffer.get() != END_OF_FIELD) {
            return false;
        }
        //Now check for TRACK if the next byte is also null byte then not v1.1
        //tag, however this means cannot have v1_1 tag with track setField to zero/undefined
        //because on next read will be v1 tag.
        return byteBuffer.get() != END_OF_FIELD;
    }

    /**
     * Read in a tag from the ByteBuffer
     *
     * @param byteBuffer from where to read in a tag
     * @throws TagNotFoundException if unable to read a tag in the byteBuffer
     */
    public void read(ByteBuffer byteBuffer) throws TagNotFoundException {
        if (!seek(byteBuffer)) {
            throw new TagNotFoundException("ID3v1 tag not found");
        }
        LOG.log(DEBUG, "Reading v1.1 tag");

        //Do single file read of data to cut down on file reads
        byte[] dataBuffer = new byte[TAG_LENGTH];
        byteBuffer.position(0);
        byteBuffer.get(dataBuffer, 0, TAG_LENGTH);
        title = new String(dataBuffer, FIELD_TITLE_POS, FIELD_TITLE_LENGTH, StandardCharsets.ISO_8859_1).trim();
        Matcher m = AbstractID3v1Tag.endofStringPattern.matcher(title);
        if (m.find()) {
            title = title.substring(0, m.start());
        }
        artist = new String(dataBuffer, FIELD_ARTIST_POS, FIELD_ARTIST_LENGTH, StandardCharsets.ISO_8859_1).trim();
        m = AbstractID3v1Tag.endofStringPattern.matcher(artist);
        if (m.find()) {
            artist = artist.substring(0, m.start());
        }
        album = new String(dataBuffer, FIELD_ALBUM_POS, FIELD_ALBUM_LENGTH, StandardCharsets.ISO_8859_1).trim();
        m = AbstractID3v1Tag.endofStringPattern.matcher(album);
        if (m.find()) {
            album = album.substring(0, m.start());
        }
        year = new String(dataBuffer, FIELD_YEAR_POS, FIELD_YEAR_LENGTH, StandardCharsets.ISO_8859_1).trim();
        m = AbstractID3v1Tag.endofStringPattern.matcher(year);
        if (m.find()) {
            year = year.substring(0, m.start());
        }
        comment = new String(dataBuffer, FIELD_COMMENT_POS, FIELD_COMMENT_LENGTH, StandardCharsets.ISO_8859_1).trim();
        m = AbstractID3v1Tag.endofStringPattern.matcher(comment);
        if (m.find()) {
            comment = comment.substring(0, m.start());
        }
        track = dataBuffer[FIELD_TRACK_POS];
        genre = dataBuffer[FIELD_GENRE_POS];
    }


    /**
     * Write this representation of tag to the file indicated
     *
     * @param file that this tag should be written to
     * @throws IOException thrown if there were problems writing to the file
     */
    public void write(RandomAccessFile file) throws IOException {
        LOG.log(DEBUG, "Saving ID3v11 tag to file");
        byte[] buffer = new byte[TAG_LENGTH];
        int i;
        String str;
        delete(file);
        file.seek(file.length());
        System.arraycopy(TAG_ID, FIELD_TAGID_POS, buffer, FIELD_TAGID_POS, TAG_ID.length);
        int offset = FIELD_TITLE_POS;
        if (TagOptionSingleton.getInstance().isId3v1SaveTitle()) {
            str = ID3Tags.truncate(title, FIELD_TITLE_LENGTH);
            for (i = 0; i < str.length(); i++) {
                buffer[i + offset] = (byte) str.charAt(i);
            }
        }
        offset = FIELD_ARTIST_POS;
        if (TagOptionSingleton.getInstance().isId3v1SaveArtist()) {
            str = ID3Tags.truncate(artist, FIELD_ARTIST_LENGTH);
            for (i = 0; i < str.length(); i++) {
                buffer[i + offset] = (byte) str.charAt(i);
            }
        }
        offset = FIELD_ALBUM_POS;
        if (TagOptionSingleton.getInstance().isId3v1SaveAlbum()) {
            str = ID3Tags.truncate(album, FIELD_ALBUM_LENGTH);
            for (i = 0; i < str.length(); i++) {
                buffer[i + offset] = (byte) str.charAt(i);
            }
        }
        offset = FIELD_YEAR_POS;
        if (TagOptionSingleton.getInstance().isId3v1SaveYear()) {
            str = ID3Tags.truncate(year, FIELD_YEAR_LENGTH);
            for (i = 0; i < str.length(); i++) {
                buffer[i + offset] = (byte) str.charAt(i);
            }
        }
        offset = FIELD_COMMENT_POS;
        if (TagOptionSingleton.getInstance().isId3v1SaveComment()) {
            str = ID3Tags.truncate(comment, FIELD_COMMENT_LENGTH);
            for (i = 0; i < str.length(); i++) {
                buffer[i + offset] = (byte) str.charAt(i);
            }
        }
        offset = FIELD_TRACK_POS;
        buffer[offset] = track; // skip one byte extra blank for 1.1 definition
        offset = FIELD_GENRE_POS;
        if (TagOptionSingleton.getInstance().isId3v1SaveGenre()) {
            buffer[offset] = genre;
        }
        file.write(buffer);

        LOG.log(DEBUG, "Saved ID3v11 tag to file");
    }


    public void createStructure() {
        MP3File.getStructureFormatter().openHeadingElement(TYPE_TAG, getIdentifier());
        //Header
        MP3File.getStructureFormatter().addElement(TYPE_TITLE, title);
        MP3File.getStructureFormatter().addElement(TYPE_ARTIST, artist);
        MP3File.getStructureFormatter().addElement(TYPE_ALBUM, album);
        MP3File.getStructureFormatter().addElement(TYPE_YEAR, year);
        MP3File.getStructureFormatter().addElement(TYPE_COMMENT, comment);
        MP3File.getStructureFormatter().addElement(TYPE_TRACK, track);
        MP3File.getStructureFormatter().addElement(TYPE_GENRE, genre);
        MP3File.getStructureFormatter().closeHeadingElement(TYPE_TAG);

    }
}
