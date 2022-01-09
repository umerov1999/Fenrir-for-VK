/*
 * @author : Paul Taylor
 * @author : Eric Farng
 * <p>
 * Version @version:$Id$
 * <p>
 * MusicTag Copyright (C)2003,2004
 * <p>
 * This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public  License as published by the Free Software Foundation; either version 2.1 of the License,
 * or (at your option) any later version.
 * <p>
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public License along with this library; if not,
 * you can get a copy from http://www.opensource.org/licenses/lgpl-license.php or write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 * <p>
 * Description:
 */
package ealvatag.tag.id3;

import static ealvatag.logging.EalvaTagLog.LogLevel.DEBUG;
import static ealvatag.logging.EalvaTagLog.LogLevel.TRACE;
import static ealvatag.utils.Check.CANNOT_BE_NULL;
import static ealvatag.utils.Check.CANNOT_BE_NULL_OR_EMPTY;
import static ealvatag.utils.Check.checkArgNotNull;
import static ealvatag.utils.Check.checkArgNotNullOrEmpty;
import static ealvatag.utils.Check.checkVarArg0NotNull;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;

import ealvatag.audio.io.FileOperator;
import ealvatag.audio.mp3.MP3File;
import ealvatag.logging.EalvaTagLog;
import ealvatag.logging.EalvaTagLog.JLogger;
import ealvatag.logging.EalvaTagLog.JLoggers;
import ealvatag.tag.FieldDataInvalidException;
import ealvatag.tag.FieldKey;
import ealvatag.tag.Key;
import ealvatag.tag.Tag;
import ealvatag.tag.TagField;
import ealvatag.tag.TagFieldContainer;
import ealvatag.tag.TagNotFoundException;
import ealvatag.tag.TagOptionSingleton;
import ealvatag.tag.UnsupportedFieldException;
import ealvatag.tag.images.Artwork;
import ealvatag.tag.reference.GenreTypes;
import ealvatag.utils.StandardCharsets;

/**
 * Represents an ID3v1 tag.
 *
 * @author : Eric Farng
 * @author : Paul Taylor
 */
public class ID3v1Tag extends AbstractID3v1Tag implements TagFieldContainer {
    //For writing output
    static final String TYPE_COMMENT = "comment";
    static final int BYTE_TO_UNSIGNED = 0xff;
    static final int GENRE_UNDEFINED = 0xff;
    static final ImmutableMap<FieldKey, ID3v1FieldKey> tagFieldToID3v11Field;
    private static final int FIELD_COMMENT_LENGTH = 30;
    private static final int FIELD_COMMENT_POS = 97;
    private static final JLogger LOG = JLoggers.get(ID3v1Tag.class, EalvaTagLog.MARKER);
    private static final ImmutableMap<FieldKey, ID3v1FieldKey> tagFieldToID3v1Field;
    private static final byte RELEASE = 1;
    private static final byte MAJOR_VERSION = 0;
    private static final byte REVISION = 0;

    static {
        ImmutableMap.Builder<FieldKey, ID3v1FieldKey> builder = ImmutableMap.builder();
        builder.put(FieldKey.ARTIST, ID3v1FieldKey.ARTIST)
                .put(FieldKey.ALBUM, ID3v1FieldKey.ALBUM)
                .put(FieldKey.TITLE, ID3v1FieldKey.TITLE)
                .put(FieldKey.YEAR, ID3v1FieldKey.YEAR)
                .put(FieldKey.GENRE, ID3v1FieldKey.GENRE)
                .put(FieldKey.COMMENT, ID3v1FieldKey.COMMENT);
        tagFieldToID3v1Field = builder.build();

        tagFieldToID3v11Field = ImmutableMap.<FieldKey, ID3v1FieldKey>builder()
                .putAll(tagFieldToID3v1Field)
                .put(FieldKey.TRACK, ID3v1FieldKey.TRACK)
                .build();

    }

    /**
     *
     */
    protected String album = "";
    /**
     *
     */
    protected String artist = "";
    /**
     *
     */
    protected String comment = "";
    /**
     *
     */
    protected String title = "";
    /**
     *
     */
    protected String year = "";
    /**
     *
     */
    protected byte genre = (byte) -1;

    /**
     * Creates a new ID3v1 datatype.
     */
    public ID3v1Tag() {
    }

    public ID3v1Tag(ID3v1Tag copyObject) {
        album = copyObject.album;
        artist = copyObject.artist;
        comment = copyObject.comment;
        title = copyObject.title;
        year = copyObject.year;
        genre = copyObject.genre;
    }

    public ID3v1Tag(BaseID3Tag mp3tag) {
        if (mp3tag != null) {
            ID3v11Tag convertedTag;
            if (mp3tag instanceof ID3v1Tag) {
                throw new UnsupportedOperationException("Copy Constructor not called. Please type cast the argument");
            }
            convertedTag = new ID3v11Tag(mp3tag);
            album = convertedTag.album;
            artist = convertedTag.artist;
            comment = convertedTag.comment;
            title = convertedTag.title;
            year = convertedTag.year;
            genre = convertedTag.genre;
        }
    }

    public ID3v1Tag(FileOperator fileOperator, String loggingFilename) throws TagNotFoundException, IOException {
        FileChannel fileChannel = fileOperator.getFileChannel();
        setLoggingFilename(loggingFilename);
        ByteBuffer byteBuffer;

        fileChannel.position(fileChannel.size() - TAG_LENGTH);
        byteBuffer = ByteBuffer.allocate(TAG_LENGTH);
        fileChannel.read(byteBuffer);
        byteBuffer.flip();
        read(byteBuffer);
    }

    public void read(ByteBuffer byteBuffer) throws TagNotFoundException {
        if (!seek(byteBuffer)) {
            throw new TagNotFoundException(loggingFilename + ":" + "ID3v1 tag not found");
        }
        LOG.log(DEBUG, "%s:Reading v1 tag", loggingFilename);
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
        LOG.log(TRACE, "%s:Orig Album is:%s", loggingFilename, comment);
        if (m.find()) {
            album = album.substring(0, m.start());
            LOG.log(TRACE, "%s:Album is:%s", loggingFilename, album);
        }
        year = new String(dataBuffer, FIELD_YEAR_POS, FIELD_YEAR_LENGTH, StandardCharsets.ISO_8859_1).trim();
        m = AbstractID3v1Tag.endofStringPattern.matcher(year);
        if (m.find()) {
            year = year.substring(0, m.start());
        }
        comment = new String(dataBuffer, FIELD_COMMENT_POS, FIELD_COMMENT_LENGTH, StandardCharsets.ISO_8859_1).trim();
        m = AbstractID3v1Tag.endofStringPattern.matcher(comment);
        LOG.log(TRACE, "%s:Orig Comment is:%s", loggingFilename, comment);
        if (m.find()) {
            comment = comment.substring(0, m.start());
            LOG.log(TRACE, "%s:Comment is:%s", loggingFilename, comment);
        }
        genre = dataBuffer[FIELD_GENRE_POS];

    }

    /**
     * Does a tag of this version exist within the byteBuffer
     *
     * @return whether tag exists within the byteBuffer
     */
    public boolean seek(ByteBuffer byteBuffer) {
        byte[] buffer = new byte[FIELD_TAGID_LENGTH];
        // read the TAG value
        byteBuffer.get(buffer, 0, FIELD_TAGID_LENGTH);
        return (Arrays.equals(buffer, TAG_ID));
    }

    /**
     * Write this tag to the file, replacing any tag previously existing
     */
    public void write(RandomAccessFile file) throws IOException {
        LOG.log(DEBUG, "Saving ID3v1 tag to file");
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
            str = ID3Tags.truncate(year, AbstractID3v1Tag.FIELD_YEAR_LENGTH);
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
        offset = FIELD_GENRE_POS;
        if (TagOptionSingleton.getInstance().isId3v1SaveGenre()) {
            buffer[offset] = genre;
        }
        file.write(buffer);
        LOG.log(DEBUG, "Saved ID3v1 tag to file");
    }

    /**
     * @return true if this and obj are equivalent
     */
    public boolean equals(Object obj) {
        if (!(obj instanceof ID3v1Tag)) {
            return false;
        }
        ID3v1Tag object = (ID3v1Tag) obj;

        return album.equals(object.album) &&
                artist.equals(object.artist) &&
                comment.equals(object.comment) &&
                genre == object.genre &&
                title.equals(object.title) &&
                year.equals(object.year) &&
                super.equals(obj);
    }

    /**
     * @return an iterator to iterate through the fields of the tag
     */
    public Iterator iterator() {
        return new ID3v1Iterator(this);
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

//  protected List<TagField> returnFieldToList(ID3v1TagField field) {
//    List<TagField> fields = new ArrayList<TagField>();
//    fields.add(field);
//    return fields;
//  }

    /**
     * @return album within list or empty if does not exist
     */
    public ImmutableList<TagField> getAlbum() {
        String firstAlbum = getFirstAlbum();
        if (firstAlbum.length() > 0) {
            return ImmutableList.<TagField>of(new ID3v1TagField(ID3v1FieldKey.ALBUM.name(), firstAlbum));
        } else {
            return ImmutableList.of();
        }
    }

    /**
     * Set Album
     */
    public void setAlbum(String album) {
        checkArgNotNull(album);
        this.album = ID3Tags.truncate(album, FIELD_ALBUM_LENGTH);
    }

    /**
     * @return Artist within list or empty if does not exist
     */
    public ImmutableList<TagField> getArtist() {
        String firstArtist = getFirstArtist();
        if (firstArtist.length() > 0) {
            return ImmutableList.<TagField>of(new ID3v1TagField(ID3v1FieldKey.ARTIST.name(), firstArtist));
        } else {
            return ImmutableList.of();
        }
    }

    /**
     * Set Artist
     */
    public void setArtist(String artist) {
        checkArgNotNull(artist);
        this.artist = ID3Tags.truncate(artist, FIELD_ARTIST_LENGTH);
    }

    /**
     * @return comment within list or empty if does not exist
     */
    public ImmutableList<TagField> getComment() {
        if (getFirstComment().length() > 0) {
            return ImmutableList.<TagField>of(new ID3v1TagField(ID3v1FieldKey.COMMENT.name(), getFirstComment()));
        } else {
            return ImmutableList.of();
        }
    }

    /**
     * Set Comment
     *
     * @throws IllegalArgumentException if comment null
     */
    public void setComment(String comment) {
        checkArgNotNull(comment);
        this.comment = ID3Tags.truncate(comment, FIELD_COMMENT_LENGTH);
    }

    /**
     * Get Genre field
     * <p>
     * <p>Only a single genre is available in ID3v1
     */
    public ImmutableList<TagField> getGenre() {
        String firstGenre = getFirst(FieldKey.GENRE);
        if (firstGenre.length() > 0) {
            return ImmutableList.<TagField>of(new ID3v1TagField(ID3v1FieldKey.GENRE.name(), firstGenre));
        } else {
            return ImmutableList.of();
        }
    }

    /**
     * Sets the genreID,
     * <p>
     * <p>ID3v1 only supports genres defined in a predefined list
     * so if unable to find value in list set 255, which seems to be the value
     * winamp uses for undefined.
     */
    public void setGenre(String genreVal) {
        checkArgNotNull(genreVal);
        Integer genreID = GenreTypes.getInstanceOf().getIdForValue(genreVal);
        if (genreID != null) {
            genre = genreID.byteValue();
        } else {
            genre = (byte) GENRE_UNDEFINED;
        }
    }

    /**
     * Get title field
     * <p>
     * <p>Only a single title is available in ID3v1
     */
    public ImmutableList<TagField> getTitle() {
        String firstTitle = getFirst(FieldKey.TITLE);
        if (firstTitle.length() > 0) {
            return ImmutableList.<TagField>of(new ID3v1TagField(ID3v1FieldKey.TITLE.name(), firstTitle));
        } else {
            return ImmutableList.of();
        }
    }

    /**
     * Set Title
     */
    public void setTitle(String title) {
        checkArgNotNull(title);
        this.title = ID3Tags.truncate(title, FIELD_TITLE_LENGTH);
    }

    /**
     * Get year field
     * <p>
     * <p>Only a single year is available in ID3v1
     */
    public ImmutableList<TagField> getYear() {
        if (getFirst(FieldKey.YEAR).length() > 0) {
            return ImmutableList.<TagField>of(new ID3v1TagField(ID3v1FieldKey.YEAR.name(), getFirst(FieldKey.YEAR)));
        } else {
            return ImmutableList.of();
        }
    }

    /**
     * Set year
     */
    public void setYear(String year) {
        this.year = ID3Tags.truncate(year, FIELD_YEAR_LENGTH);
    }

    public String getFirstTrack() {
        throw new UnsupportedFieldException(FieldKey.TRACK.name());
    }

    public ImmutableList<TagField> getTrack() {
        throw new UnsupportedFieldException(FieldKey.TRACK.name());
    }

    public Charset getEncoding() {
        return StandardCharsets.ISO_8859_1;
    }

//  /**
//   * The m parameter is effectively ignored
//   *
//   */
//  public String getSubValue(FieldKey id, int n, int m) {
//    return getFieldAt(id, n);
//  }

    /**
     * Create structured representation of this item.
     */
    public void createStructure() {
        MP3File.getStructureFormatter().openHeadingElement(TYPE_TAG, getIdentifier());
        //Header
        MP3File.getStructureFormatter().addElement(TYPE_TITLE, title);
        MP3File.getStructureFormatter().addElement(TYPE_ARTIST, artist);
        MP3File.getStructureFormatter().addElement(TYPE_ALBUM, album);
        MP3File.getStructureFormatter().addElement(TYPE_YEAR, year);
        MP3File.getStructureFormatter().addElement(TYPE_COMMENT, comment);
        MP3File.getStructureFormatter().addElement(TYPE_GENRE, genre);
        MP3File.getStructureFormatter().closeHeadingElement(TYPE_TAG);
    }

    @Override
    public ImmutableSet<FieldKey> getSupportedFields() {
        return tagFieldToID3v1Field.keySet();
    }

    public boolean isEmpty() {
        return !(getFirst(FieldKey.TITLE).length() > 0 ||
                getFirstArtist().length() > 0 ||
                getFirstAlbum().length() > 0 ||
                getFirst(FieldKey.GENRE).length() > 0 ||
                getFirst(FieldKey.YEAR).length() > 0 ||
                getFirstComment().length() > 0);
    }

    public boolean hasField(FieldKey genericKey) {
        return getFirst(genericKey).length() > 0;
    }

    public boolean hasField(String id) {
        try {
            FieldKey key = FieldKey.valueOf(id.toUpperCase());
            return hasField(key);
        } catch (java.lang.IllegalArgumentException iae) {
            return false;
        }
    }

    @Override
    public int getFieldCount(Key genericKey) throws IllegalArgumentException, UnsupportedFieldException {
        return getFields(FieldKey.valueOf(genericKey.name())).size();
    }

    public int getFieldCount() {
        return 6;
    }

    public Tag setField(FieldKey genericKey, String... values) throws IllegalArgumentException,
            UnsupportedFieldException,
            FieldDataInvalidException {
        // create field checks parameters
        TagField tagfield = createField(genericKey, values);
        setField(tagfield);
        return this;
    }

    public void setField(TagField field) {
        FieldKey genericKey = FieldKey.valueOf(field.getId());
        switch (genericKey) {
            case ARTIST:
                setArtist(field.toString());
                break;

            case ALBUM:
                setAlbum(field.toString());
                break;

            case TITLE:
                setTitle(field.toString());
                break;

            case GENRE:
                setGenre(field.toString());
                break;

            case YEAR:
                setYear(field.toString());
                break;

            case COMMENT:
                setComment(field.toString());
                break;
        }
    }

    public void addField(TagField field) {
        //TODO
    }

    public Tag addField(FieldKey genericKey, String... values) throws IllegalArgumentException,
            UnsupportedFieldException,
            FieldDataInvalidException {
        // parameters checked later in chain // TODO: 1/20/17 refactor into public and private methods
        setField(genericKey, values);
        return this;
    }

    /**
     * Retrieve the first value that exists for this generic key
     */
    public String getFirst(FieldKey genericKey) throws IllegalArgumentException, UnsupportedFieldException {
        if (!getSupportedFields().contains(genericKey)) {
            throw new UnsupportedFieldException(genericKey.toString());
        }
        return getValue(genericKey).or("");
    }

    /**
     * Retrieve the first value that exists for this key id
     */
    public String getFirst(String genericKey) throws IllegalArgumentException, UnsupportedFieldException {
        FieldKey matchingKey = FieldKey.valueOf(genericKey);
        return getValue(matchingKey).or("");
    }

    public Optional<String> getValue(FieldKey genericKey) throws IllegalArgumentException {
        switch (genericKey) {
            case ARTIST:
                return Optional.of(getFirstArtist());
            case ALBUM:
                return Optional.of(getFirstAlbum());
            case TITLE:
                return Optional.of(getFirstTitle());
            case GENRE:
                return getOptionalFirstGenre();
            case YEAR:
                return Optional.of(getFirstYear());
            case COMMENT:
                return Optional.of(getFirstComment());
            default:
                return Optional.absent();
        }
    }

    @Override
    public Optional<String> getValue(FieldKey genericKey, int index) throws IllegalArgumentException {
        checkArgNotNull(genericKey);
        if (index > 0) {
            return Optional.absent();
        }
        return getValue(genericKey);
    }

    public String getFieldAt(FieldKey genericKey, int index) throws IllegalArgumentException, UnsupportedFieldException {
        return getValue(genericKey, index).or("");
    }

    public List<String> getAll(FieldKey genericKey) throws IllegalArgumentException, UnsupportedFieldException {
        return Collections.singletonList(getFirst(genericKey.name()));
    }

    public Tag deleteField(FieldKey genericKey) throws IllegalArgumentException, UnsupportedFieldException {
        checkArgNotNull(genericKey, CANNOT_BE_NULL, "genericKey");
        switch (genericKey) {
            case ARTIST:
                setArtist("");
                break;
            case ALBUM:
                setAlbum("");
                break;
            case TITLE:
                setTitle("");
                break;
            case GENRE:
                setGenre("");
                break;
            case YEAR:
                setYear("");
                break;
            case COMMENT:
                setComment("");
                break;
            default:
                throw new UnsupportedFieldException(genericKey.name());
        }
        return this;
    }

    public Tag deleteField(String id) throws IllegalArgumentException, UnsupportedFieldException {
        FieldKey key = FieldKey.valueOf(checkArgNotNullOrEmpty(id, CANNOT_BE_NULL_OR_EMPTY, "id")); // could throw IllegalArgumentException
        deleteField(key);
        return this;
    }

    public Tag setArtwork(Artwork artwork) throws IllegalArgumentException, UnsupportedFieldException, FieldDataInvalidException {
        throw new UnsupportedFieldException(FieldKey.COVER_ART.name());
    }

    public Tag addArtwork(Artwork artwork) throws IllegalArgumentException, UnsupportedFieldException, FieldDataInvalidException {
        throw new UnsupportedFieldException(FieldKey.COVER_ART.name());
    }

    public Optional<Artwork> getFirstArtwork() throws UnsupportedFieldException {
        throw new UnsupportedFieldException(FieldKey.COVER_ART.name());
    }

    public List<Artwork> getArtworkList() throws UnsupportedFieldException {
        return Collections.emptyList();
    }

    public Tag deleteArtwork() throws UnsupportedFieldException {
        throw new UnsupportedFieldException(FieldKey.COVER_ART.name());
    }

    public boolean hasCommonFields() {
        //TODO
        return true;
    }

    public int getFieldCountIncludingSubValues() {
        return getFieldCount();
    }

    @Override
    public boolean setEncoding(Charset encoding) {
        return true;
    }

    /**
     * Create Tag Field using generic key
     */
    public TagField createField(FieldKey genericKey, String... values) throws IllegalArgumentException,
            UnsupportedFieldException,
            FieldDataInvalidException {
        ID3v1FieldKey idv1FieldKey = getFieldMap().get(checkArgNotNull(genericKey, CANNOT_BE_NULL, "genericKey"));
        if (idv1FieldKey == null) {
            throw new UnsupportedFieldException(genericKey.name());
        }
        return new ID3v1TagField(idv1FieldKey.name(), checkVarArg0NotNull(values));
    }

    protected ImmutableMap<FieldKey, ID3v1FieldKey> getFieldMap() {
        return tagFieldToID3v1Field;
    }

    public TagField createArtwork(Artwork artwork) throws UnsupportedFieldException, FieldDataInvalidException {
        throw new UnsupportedFieldException(FieldKey.COVER_ART.name());
    }

    /**
     * Returns a {@linkplain List list} of {@link TagField} objects whose &quot;{@linkplain TagField#getId() id}&quot;
     * is the specified one.<br>
     *
     * @param genericKey The generic field key
     * @return A list of {@link TagField} objects with the given &quot;id&quot;.
     */
    public ImmutableList<TagField> getFields(FieldKey genericKey)
            throws IllegalArgumentException, UnsupportedFieldException {
        switch (genericKey) {
            case ARTIST:
                return getArtist();

            case ALBUM:
                return getAlbum();

            case TITLE:
                return getTitle();

            case GENRE:
                return getGenre();

            case YEAR:
                return getYear();

            case COMMENT:
                return getComment();

            default:
                return ImmutableList.of();
        }
    }

    public Iterator<TagField> getFields() {
        throw new UnsupportedOperationException("TODO:Not done yet");
    }

    public ImmutableList<TagField> getFields(String id) {
        try {
            return getFields(FieldKey.valueOf(id));
        } catch (NullPointerException | IllegalArgumentException ignored) {
        }
        return ImmutableList.of();
    }

    public Optional<TagField> getFirstField(String id) throws IllegalArgumentException, UnsupportedFieldException {
        List<TagField> results = null;

        if (FieldKey.ARTIST.name().equals(id)) {
            results = getArtist();
        } else if (FieldKey.ALBUM.name().equals(id)) {
            results = getAlbum();
        } else if (FieldKey.TITLE.name().equals(id)) {
            results = getTitle();
        } else if (FieldKey.GENRE.name().equals(id)) {
            results = getGenre();
        } else if (FieldKey.YEAR.name().equals(id)) {
            results = getYear();
        } else if (FieldKey.COMMENT.name().equals(id)) {
            results = getComment();
        }

        if (results != null) {
            if (results.size() > 0) {
                return Optional.fromNullable(results.get(0));
            }
        }
        return Optional.absent();
    }

    public Optional<TagField> getFirstField(FieldKey genericKey)
            throws IllegalArgumentException, UnsupportedFieldException {
        List<TagField> l = getFields(genericKey);
        return (l.size() != 0) ? Optional.fromNullable(l.get(0)) : Optional.<TagField>absent();
    }

    public TagField createCompilationField(boolean value) throws UnsupportedFieldException {
        throw new UnsupportedFieldException(FieldKey.IS_COMPILATION.name());
    }

    /**
     * Get Album
     *
     * @return album
     */
    String getFirstAlbum() {
        return album;
    }

    /**
     * Get Artist
     *
     * @return artist
     */
    String getFirstArtist() {
        return artist;
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
     * Get Genre
     *
     * @return genre or empty string if not valid
     */
    String getFirstGenre() {
        Integer genreId = genre & BYTE_TO_UNSIGNED;
        String genreValue = GenreTypes.getInstanceOf().getValue(genreId);
        if (genreValue == null) {
            return "";
        } else {
            return genreValue;
        }
    }

    private Optional<String> getOptionalFirstGenre() {
        return GenreTypes.getInstanceOf().getOptionalValue(genre & BYTE_TO_UNSIGNED);
    }


    /**
     * Get title
     *
     * @return Title
     */
    String getFirstTitle() {
        return title;
    }

    /**
     * Get year
     *
     * @return year
     */
    String getFirstYear() {
        return year;
    }

}
