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
 * Description: Options that are used for every datatype and class in this library.
 */
package ealvatag.tag;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import ealvatag.tag.id3.AbstractID3v2Tag;
import ealvatag.tag.id3.framebody.AbstractID3v2FrameBody;
import ealvatag.tag.id3.framebody.FrameBodyCOMM;
import ealvatag.tag.id3.framebody.FrameBodyTIPL;
import ealvatag.tag.id3.framebody.ID3v24FrameBody;
import ealvatag.tag.id3.valuepair.TextEncoding;
import ealvatag.tag.lyrics3.Lyrics3v2Fields;
import ealvatag.tag.options.PadNumberOption;
import ealvatag.tag.reference.GenreTypes;
import ealvatag.tag.reference.ID3V2Version;
import ealvatag.tag.reference.Languages;
import ealvatag.utils.Check;

@SuppressWarnings("unused")
public class TagOptionSingleton {

    private static final ConcurrentMap<String, TagOptionSingleton> tagOptionTable = new ConcurrentHashMap<>();
    private static final String DEFAULT = "default";
    private static final Lock tagOptionTableLock = new ReentrantLock();
    private static String defaultOptions = DEFAULT;
    private boolean isWriteWavForTwonky;
    private HashMap<Class<? extends ID3v24FrameBody>, LinkedList<String>> keywordMap = new HashMap<>();

    /**
     * Map of lyric ID's to Boolean objects if we should or should not save the
     * specific lyrics3 field. Defaults to true.
     */
    private HashMap<String, Boolean> lyrics3SaveFieldMap = new HashMap<>();
    /**
     * parenthesis map stuff
     */
    private HashMap<String, String> parenthesisMap = new HashMap<>();
    /**
     * <code>HashMap</code> listing words to be replaced if found
     */
    private HashMap<String, String> replaceWordMap = new HashMap<>();
    /**
     * default language for any ID3v2 tags frames which require it. This string
     * is in the [ISO-639-2] ISO/FDIS 639-2 definition
     */
    private String language = "eng";
    /**
     *
     */
    private boolean filenameTagSave;
    /**
     * if we should save any fields of the ID3v1 tag or not. Defaults to true.
     */
    private boolean id3v1Save = true;
    /**
     * if we should save the album field of the ID3v1 tag or not. Defaults to
     * true.
     */
    private boolean id3v1SaveAlbum = true;
    /**
     * if we should save the artist field of the ID3v1 tag or not. Defaults to
     * true.
     */
    private boolean id3v1SaveArtist = true;
    /**
     * if we should save the comment field of the ID3v1 tag or not. Defaults to
     * true.
     */
    private boolean id3v1SaveComment = true;
    /**
     * if we should save the genre field of the ID3v1 tag or not. Defaults to
     * true.
     */
    private boolean id3v1SaveGenre = true;
    /**
     * if we should save the title field of the ID3v1 tag or not. Defaults to
     * true.
     */
    private boolean id3v1SaveTitle = true;
    /**
     * if we should save the track field of the ID3v1 tag or not. Defaults to
     * true.
     */
    private boolean id3v1SaveTrack = true;
    /**
     * if we should save the year field of the ID3v1 tag or not. Defaults to
     * true.
     */
    private boolean id3v1SaveYear = true;
    /**
     * When adjusting the ID3v2 padding, if should we copy the current ID3v2
     * tag to the new MP3 file. Defaults to true.
     */
    private boolean id3v2PaddingCopyTag = true;
    /**
     * When adjusting the ID3v2 padding, if we should shorten the length of the
     * ID3v2 tag padding. Defaults to false.
     */
    private boolean id3v2PaddingWillShorten;
    /**
     * if we should save any fields of the ID3v2 tag or not. Defaults to true.
     */
    private boolean id3v2Save = true;
    /**
     * if we should keep an empty Lyrics3 field while we're reading. This is
     * different from a string of white space. Defaults to false.
     */
    private boolean lyrics3KeepEmptyFieldIfRead;
    /**
     * if we should save any fields of the Lyrics3 tag or not. Defaults to
     * true.
     */
    private boolean lyrics3Save = true;
    /**
     * if we should save empty Lyrics3 field or not. Defaults to false.
     * <p>
     * todo I don't think this is implemented yet.
     */
    private boolean lyrics3SaveEmptyField;
    /**
     *
     */
    private boolean originalSavedAfterAdjustingID3v2Padding = true;
    /**
     * default time stamp format for any ID3v2 tag frames which require it.
     */
    private byte timeStampFormat = 2;
    /**
     * number of frames to sync when trying to find the start of the MP3 frame
     * data. The start of the MP3 frame data is the start of the music and is
     * different from the ID3v2 frame data.
     */
    private int numberMP3SyncFrame = 3;
    /**
     * Unsynchronize tags/frames this is rarely required these days and can cause more
     * problems than it solves
     */
    private boolean unsyncTags;
    /**
     * iTunes needlessly writes null terminators at the end for TextEncodedStringSizeTerminated values,
     * if this option is enabled these characters are removed
     */
    private boolean removeTrailingTerminatorOnWrite = true;
    /**
     * This is the default text encoding to use for new v23 frames, when unicode is required
     * UTF16 will always be used because that is the only valid option for v23.
     */
    private byte id3v23DefaultTextEncoding = TextEncoding.ISO_8859_1;
    /**
     * This is the default text encoding to use for new v24 frames, it defaults to simple ISO8859
     * but by changing this value you could always used UTF8 for example whether you needed to or not
     */
    private byte id3v24DefaultTextEncoding = TextEncoding.ISO_8859_1;
    /**
     * This is text encoding to use for new v24 frames when unicode is required, it defaults to UTF16 just
     * because this encoding is understand by all ID3 versions
     */
    private byte id3v24UnicodeTextEncoding = TextEncoding.UTF_16;
    /**
     * When writing frames if this is set to true then the frame will be written
     * using the defaults disregarding the text encoding originally used to create
     * the frame.
     */
    private boolean resetTextEncodingForExistingFrames;
    /**
     * Some formats impose maxmimum lengths for fields , if the text provided is longer
     * than the formats allows it will truncate and write a warning, if this is not set
     * it will throw an exception
     */
    private boolean truncateTextWithoutErrors;
    /**
     * Frames such as TRCK and TPOS sometimes pad single digit numbers to aid sorting
     * <p>
     * Currently only applies to ID3 files
     */
    private boolean padNumbers;
    /**
     * Number of padding zeroes digits 1- 9, numbers larger than nine will be padded accordingly based on the value.
     * Only has any effect if padNumbers is set to true
     * <p>
     * Currently only applies to ID3 files
     */
    private PadNumberOption padNumberTotalLength = PadNumberOption.PAD_ONE_ZERO;
    /**
     * There are a couple of problems with the Java implementation on Google Android, enabling this value
     * switches on Google workarounds
     */
    private boolean isAndroid;
    private boolean isAPICDescriptionITunesCompatible;
    /**
     * When you specify a field should be stored as UTF16 in ID3 this means write with BOM indicating whether
     * written as Little Endian or Big Endian, its defaults to little Endian
     */
    private boolean isEncodeUTF16BomAsLittleEndian = true;
    /**
     * When this is set and using the generic interface ealvatag will make some adjustments
     * when saving field so they work best with the specified Tagger
     */
    //TODO Not Actually Used yet, originally intended for dealing with ratings and genres
    private int playerCompatability = -1;
    /**
     * max size of data to copy when copying audiodata from one file to , default to 4mb
     */
    private long writeChunkSize = (4 * 1024 * 1024);
    private boolean isWriteMp4GenresAsText;
    private boolean isWriteMp3GenresAsText;
    private ID3V2Version id3v2Version = ID3V2Version.ID3_V24;
    /**
     * Whether Files.isWritable should be used to check if a file can be written. In some
     * cases, isWritable can return false negatives.
     */
    private boolean checkIsWritable;
    /**
     * Preserve file identity if possible
     */
    private boolean preserveFileIdentity = true;

    /**
     * Should the entire moov box be immediately read into memory to minimize IO. Can very large (I've seen 500K or more) but improves
     * performance.
     */
    private boolean readAheadMp4 = true;

    /**
     * Creates a new TagOptions data type. All Options are set to their default
     * values
     */
    private TagOptionSingleton() {
        setToDefault();
    }

    /**
     * Default based on user option
     *
     * @return the ID3v2 tag type specified by {@link #setID3V2Version(ID3V2Version)}
     */
    public static AbstractID3v2Tag createDefaultID3Tag() {
        return getInstance().getID3V2Version().makeTag();
    }

    public static TagOptionSingleton getInstance() {
        return getInstance(defaultOptions);
    }

    public static TagOptionSingleton getInstance(String instanceKey) {
        tagOptionTableLock.lock();
        try {
            TagOptionSingleton tagOptions = tagOptionTable.get(instanceKey);

            if (tagOptions == null) {
                tagOptions = new TagOptionSingleton();
                tagOptionTable.put(instanceKey, tagOptions);
            }

            return tagOptions;
        } finally {
            tagOptionTableLock.unlock();
        }
    }

    public static String getInstanceKey() {
        return defaultOptions;
    }

    public void setInstanceKey(String instanceKey) {
        defaultOptions = instanceKey;
    }

    public boolean isFilenameTagSave() {
        return filenameTagSave;
    }

    public void setFilenameTagSave(boolean filenameTagSave) {
        this.filenameTagSave = filenameTagSave;
    }

    public ID3V2Version getID3V2Version() {
        return id3v2Version;
    }

    public void setID3V2Version(ID3V2Version id3v2Version) {
        Check.checkArgNotNull(id3v2Version);
        this.id3v2Version = id3v2Version;
    }

    public boolean isId3v1Save() {
        return id3v1Save;
    }

    public void setId3v1Save(boolean id3v1Save) {
        this.id3v1Save = id3v1Save;
    }

    public boolean isId3v1SaveAlbum() {
        return id3v1SaveAlbum;
    }

    public void setId3v1SaveAlbum(boolean id3v1SaveAlbum) {
        this.id3v1SaveAlbum = id3v1SaveAlbum;
    }

    public boolean isId3v1SaveArtist() {
        return id3v1SaveArtist;
    }

    public void setId3v1SaveArtist(boolean id3v1SaveArtist) {
        this.id3v1SaveArtist = id3v1SaveArtist;
    }

    public boolean isId3v1SaveComment() {
        return id3v1SaveComment;
    }

    public void setId3v1SaveComment(boolean id3v1SaveComment) {
        this.id3v1SaveComment = id3v1SaveComment;
    }

    public boolean isId3v1SaveGenre() {
        return id3v1SaveGenre;
    }

    public void setId3v1SaveGenre(boolean id3v1SaveGenre) {
        this.id3v1SaveGenre = id3v1SaveGenre;
    }

    public boolean isId3v1SaveTitle() {
        return id3v1SaveTitle;
    }

    public void setId3v1SaveTitle(boolean id3v1SaveTitle) {
        this.id3v1SaveTitle = id3v1SaveTitle;
    }

    public boolean isId3v1SaveTrack() {
        return id3v1SaveTrack;
    }

    public void setId3v1SaveTrack(boolean id3v1SaveTrack) {
        this.id3v1SaveTrack = id3v1SaveTrack;
    }

    public boolean isId3v1SaveYear() {
        return id3v1SaveYear;
    }

    public void setId3v1SaveYear(boolean id3v1SaveYear) {
        this.id3v1SaveYear = id3v1SaveYear;
    }

    public boolean isId3v2PaddingCopyTag() {
        return id3v2PaddingCopyTag;
    }

    public void setId3v2PaddingCopyTag(boolean id3v2PaddingCopyTag) {
        this.id3v2PaddingCopyTag = id3v2PaddingCopyTag;
    }

    public boolean isId3v2PaddingWillShorten() {
        return id3v2PaddingWillShorten;
    }

    public void setId3v2PaddingWillShorten(boolean id3v2PaddingWillShorten) {
        this.id3v2PaddingWillShorten = id3v2PaddingWillShorten;
    }

    public boolean isId3v2Save() {
        return id3v2Save;
    }

    public void setId3v2Save(boolean id3v2Save) {
        this.id3v2Save = id3v2Save;
    }

    /**
     * If this true, the entire moov box is read into buffers before parsing all it's contents. This may be very large (>500K) depending
     * on the file metadata, number of tracks, etc.
     * <p>
     * <b>Default is true</b>
     *
     * @return true if mp4 parsing should aggressively cache file contents
     */
    public boolean shouldReadAheadMp4() {
        return readAheadMp4;
    }

    /**
     * Determines if a large initial read is performed of the entire moov box. If set to true it could increase performance. Settings to
     * false <b>may</b> decrease heap footprint during processing, depending on many factors.
     * <p>
     * <b>Default is true</b>
     *
     * @param readAheadMp4 if true mp4 parsing aggressively caches file contents
     * @see #shouldReadAheadMp4()
     */
    public void setReadAheadMp4(boolean readAheadMp4) {
        this.readAheadMp4 = readAheadMp4;
    }

    public Iterator<Class<? extends ID3v24FrameBody>> getKeywordIterator() {
        return keywordMap.keySet().iterator();
    }

    public Iterator<String> getKeywordListIterator(Class<? extends ID3v24FrameBody> id3v2_4FrameBody) {
        return keywordMap.get(id3v2_4FrameBody).iterator();
    }

    /**
     * Returns the default language for any ID3v2 tag frames which require it.
     *
     * @return language ID, [ISO-639-2] ISO/FDIS 639-2 definition
     */
    public String getLanguage() {
        return language;
    }

    /**
     * Sets the default language for any ID3v2 tag frames which require it.
     * While the value will already exist when reading from a file, this value
     * will be used when a new ID3v2 Frame is created from scratch.
     *
     * @param lang language ID, [ISO-639-2] ISO/FDIS 639-2 definition
     */
    public void setLanguage(String lang) {
        if (Languages.getInstanceOf().containsKey(lang)) {
            language = lang;
        }
    }

    public boolean isLyrics3KeepEmptyFieldIfRead() {
        return lyrics3KeepEmptyFieldIfRead;
    }

    public void setLyrics3KeepEmptyFieldIfRead(boolean lyrics3KeepEmptyFieldIfRead) {
        this.lyrics3KeepEmptyFieldIfRead = lyrics3KeepEmptyFieldIfRead;
    }

    public boolean isLyrics3Save() {
        return lyrics3Save;
    }

    public void setLyrics3Save(boolean lyrics3Save) {
        this.lyrics3Save = lyrics3Save;
    }

    public boolean isLyrics3SaveEmptyField() {
        return lyrics3SaveEmptyField;
    }

    public void setLyrics3SaveEmptyField(boolean lyrics3SaveEmptyField) {
        this.lyrics3SaveEmptyField = lyrics3SaveEmptyField;
    }

    /**
     * Sets if we should save the Lyrics3 field. Defaults to true.
     *
     * @param id   Lyrics3 id string
     * @param save true if you want to save this specific Lyrics3 field.
     */
    public void setLyrics3SaveField(String id, boolean save) {
        lyrics3SaveFieldMap.put(id, save);
    }

    /**
     * Returns true if we should save the Lyrics3 field asked for in the
     * argument. Defaults to true.
     *
     * @param id Lyrics3 id string
     * @return true if we should save the Lyrics3 field.
     */
    public boolean getLyrics3SaveField(String id) {
        return lyrics3SaveFieldMap.get(id);
    }

    public HashMap<String, Boolean> getLyrics3SaveFieldMap() {
        return lyrics3SaveFieldMap;
    }

    public String getNewReplaceWord(String oldWord) {
        return replaceWordMap.get(oldWord);
    }

    /**
     * Returns the number of MP3 frames to sync when trying to find the start
     * of the MP3 frame data. The start of the MP3 frame data is the start of
     * the music and is different from the ID3v2 frame data. WinAmp 2.8 seems
     * to sync 3 frames. Default is 5.
     *
     * @return number of MP3 frames to sync
     */
    public int getNumberMP3SyncFrame() {
        return numberMP3SyncFrame;
    }

    /**
     * Sets the number of MP3 frames to sync when trying to find the start of
     * the MP3 frame data. The start of the MP3 frame data is the start of the
     * music and is different from the ID3v2 frame data. WinAmp 2.8 seems to
     * sync 3 frames. Default is 5.
     *
     * @param numberMP3SyncFrame number of MP3 frames to sync
     */
    public void setNumberMP3SyncFrame(int numberMP3SyncFrame) {
        this.numberMP3SyncFrame = numberMP3SyncFrame;
    }

    public Iterator<String> getOldReplaceWordIterator() {
        return replaceWordMap.keySet().iterator();
    }

    public boolean isOpenParenthesis(String open) {
        return parenthesisMap.containsKey(open);
    }

    public Iterator<String> getOpenParenthesisIterator() {
        return parenthesisMap.keySet().iterator();
    }

    public boolean isOriginalSavedAfterAdjustingID3v2Padding() {
        return originalSavedAfterAdjustingID3v2Padding;
    }

    public void setOriginalSavedAfterAdjustingID3v2Padding(boolean originalSavedAfterAdjustingID3v2Padding) {
        this.originalSavedAfterAdjustingID3v2Padding = originalSavedAfterAdjustingID3v2Padding;
    }

    /**
     * Returns the default time stamp format for ID3v2 tags which require it.
     * <p>
     * <p>
     * $01  Absolute time, 32 bit sized, using MPEG frames as unit<br>
     * $02  Absolute time, 32 bit sized, using milliseconds as unit<br>
     *
     * @return the default time stamp format
     */
    public byte getTimeStampFormat() {
        return timeStampFormat;
    }

    /**
     * Sets the default time stamp format for ID3v2 tags which require it.
     * While the value will already exist when reading from a file, this value
     * will be used when a new ID3v2 Frame is created from scratch.
     * <p>
     * <p>
     * $01  Absolute time, 32 bit sized, using MPEG frames as unit<br>
     * $02  Absolute time, 32 bit sized, using milliseconds as unit<br>
     *
     * @param tsf the new default time stamp format
     */
    public void setTimeStampFormat(byte tsf) {
        if ((tsf == 1) || (tsf == 2)) {
            timeStampFormat = tsf;
        }
    }

    /**
     *
     */
    public void setToDefault() {
        isWriteWavForTwonky = false;
        keywordMap = new HashMap<>();
        filenameTagSave = false;
        id3v1Save = true;
        id3v1SaveAlbum = true;
        id3v1SaveArtist = true;
        id3v1SaveComment = true;
        id3v1SaveGenre = true;
        id3v1SaveTitle = true;
        id3v1SaveTrack = true;
        id3v1SaveYear = true;
        id3v2PaddingCopyTag = true;
        id3v2PaddingWillShorten = false;
        id3v2Save = true;
        language = "eng";
        lyrics3KeepEmptyFieldIfRead = false;
        lyrics3Save = true;
        lyrics3SaveEmptyField = false;
        lyrics3SaveFieldMap = new HashMap<>();
        numberMP3SyncFrame = 3;
        parenthesisMap = new HashMap<>();
        replaceWordMap = new HashMap<>();
        timeStampFormat = 2;
        unsyncTags = false;
        removeTrailingTerminatorOnWrite = true;
        id3v23DefaultTextEncoding = TextEncoding.ISO_8859_1;
        id3v24DefaultTextEncoding = TextEncoding.ISO_8859_1;
        id3v24UnicodeTextEncoding = TextEncoding.UTF_16;
        resetTextEncodingForExistingFrames = false;
        truncateTextWithoutErrors = false;
        padNumbers = false;
        isAPICDescriptionITunesCompatible = false;
        isAndroid = false;
        isEncodeUTF16BomAsLittleEndian = true;
        writeChunkSize = 5000000;
        isWriteMp4GenresAsText = false;
        padNumberTotalLength = PadNumberOption.PAD_ONE_ZERO;
        id3v2Version = ID3V2Version.ID3_V24;
        checkIsWritable = false;
        preserveFileIdentity = false;

        //default all lyrics3 fields to save. id3v1 fields are individual
        // settings. id3v2 fields are always looked at to save.
        for (String fieldId : Lyrics3v2Fields.getInstanceOf().getAllKeys()) {
            lyrics3SaveFieldMap.put(fieldId, true);
        }

        addKeyword(FrameBodyCOMM.class, "ultimix");
        addKeyword(FrameBodyCOMM.class, "dance");
        addKeyword(FrameBodyCOMM.class, "mix");
        addKeyword(FrameBodyCOMM.class, "remix");
        addKeyword(FrameBodyCOMM.class, "rmx");
        addKeyword(FrameBodyCOMM.class, "live");
        addKeyword(FrameBodyCOMM.class, "cover");
        addKeyword(FrameBodyCOMM.class, "soundtrack");
        addKeyword(FrameBodyCOMM.class, "version");
        addKeyword(FrameBodyCOMM.class, "acoustic");
        addKeyword(FrameBodyCOMM.class, "original");
        addKeyword(FrameBodyCOMM.class, "cd");
        addKeyword(FrameBodyCOMM.class, "extended");
        addKeyword(FrameBodyCOMM.class, "vocal");
        addKeyword(FrameBodyCOMM.class, "unplugged");
        addKeyword(FrameBodyCOMM.class, "acapella");
        addKeyword(FrameBodyCOMM.class, "edit");
        addKeyword(FrameBodyCOMM.class, "radio");
        addKeyword(FrameBodyCOMM.class, "original");
        addKeyword(FrameBodyCOMM.class, "album");
        addKeyword(FrameBodyCOMM.class, "studio");
        addKeyword(FrameBodyCOMM.class, "instrumental");
        addKeyword(FrameBodyCOMM.class, "unedited");
        addKeyword(FrameBodyCOMM.class, "karoke");
        addKeyword(FrameBodyCOMM.class, "quality");
        addKeyword(FrameBodyCOMM.class, "uncensored");
        addKeyword(FrameBodyCOMM.class, "clean");
        addKeyword(FrameBodyCOMM.class, "dirty");

        addKeyword(FrameBodyTIPL.class, "f.");
        addKeyword(FrameBodyTIPL.class, "feat");
        addKeyword(FrameBodyTIPL.class, "feat.");
        addKeyword(FrameBodyTIPL.class, "featuring");
        addKeyword(FrameBodyTIPL.class, "ftng");
        addKeyword(FrameBodyTIPL.class, "ftng.");
        addKeyword(FrameBodyTIPL.class, "ft.");
        addKeyword(FrameBodyTIPL.class, "ft");

        GenreTypes.getInstanceOf().iterateValues(new GenreTypes.ValuesIterator() {
            @Override
            public boolean begin(int count) {
                return true;
            }

            @Override
            public boolean value(String value) {
                addKeyword(FrameBodyCOMM.class, value);
                return true;
            }

            @Override
            public void end() {
            }
        });

        addReplaceWord("v.", "vs.");
        addReplaceWord("vs.", "vs.");
        addReplaceWord("versus", "vs.");
        addReplaceWord("f.", "feat.");
        addReplaceWord("feat", "feat.");
        addReplaceWord("featuring", "feat.");
        addReplaceWord("ftng.", "feat.");
        addReplaceWord("ftng", "feat.");
        addReplaceWord("ft.", "feat.");
        addReplaceWord("ft", "feat.");

        // TODO: 1/17/17 iterator not used, why was this here? I see no side effects from the call
//        iterator = this.getKeywordListIterator(FrameBodyTIPL.class);


        addParenthesis("(", ")");
        addParenthesis("[", "]");
        addParenthesis("{", "}");
        addParenthesis("<", ">");
    }


    /**
     * @throws IllegalArgumentException if class is not of type AbstractID3v2FrameBody
     */
    private void addKeyword(Class<? extends ID3v24FrameBody> id3v2FrameBodyClass, String keyword) {
        if (!AbstractID3v2FrameBody.class.isAssignableFrom(id3v2FrameBodyClass)) {
            throw new IllegalArgumentException("Invalid class type. Must be AbstractId3v2FrameBody " + id3v2FrameBodyClass);
        }

        if ((keyword != null) && (keyword.length() > 0)) {
            LinkedList<String> keywordList;

            if (!keywordMap.containsKey(id3v2FrameBodyClass)) {
                keywordList = new LinkedList<>();
                keywordMap.put(id3v2FrameBodyClass, keywordList);
            } else {
                keywordList = keywordMap.get(id3v2FrameBodyClass);
            }

            keywordList.add(keyword);
        }
    }

    private void addParenthesis(String open, String close) {
        parenthesisMap.put(open, close);
    }

    private void addReplaceWord(String oldWord, String newWord) {
        replaceWordMap.put(oldWord, newWord);
    }

    /**
     * @return are tags unsynchronized when written if contain bit pattern that could be mistaken for audio marker
     */
    public boolean isUnsyncTags() {
        return unsyncTags;
    }

    /**
     * Unsync tag where necessary, currently only applies to IDv23
     *
     * @param unsyncTags set whether tags are  unsynchronized when written if contain bit pattern that could be mistaken for audio marker
     */
    public void setUnsyncTags(boolean unsyncTags) {
        this.unsyncTags = unsyncTags;
    }

    /**
     * Do we remove unnecessary trailing null characters on write
     *
     * @return true if we remove unnecessary trailing null characters on write
     */
    public boolean isRemoveTrailingTerminatorOnWrite() {
        return removeTrailingTerminatorOnWrite;
    }

    /**
     * Remove unnecessary trailing null characters on write
     */
    public void setRemoveTrailingTerminatorOnWrite(boolean removeTrailingTerminatorOnWrite) {
        this.removeTrailingTerminatorOnWrite = removeTrailingTerminatorOnWrite;
    }

    /**
     * Get the default text encoding to use for new v23 frames, when unicode is required
     * UTF16 will always be used because that is the only valid option for v23/v22
     */
    public byte getId3v23DefaultTextEncoding() {
        return id3v23DefaultTextEncoding;
    }

    /**
     * Set the default text encoding to use for new v23 frames, when unicode is required
     * UTF16 will always be used because that is the only valid option for v23/v22
     */
    public void setId3v23DefaultTextEncoding(byte id3v23DefaultTextEncoding) {
        if ((id3v23DefaultTextEncoding == TextEncoding.ISO_8859_1) || (id3v23DefaultTextEncoding == TextEncoding.UTF_16)) {
            this.id3v23DefaultTextEncoding = id3v23DefaultTextEncoding;
        }
    }

    /**
     * Get the default text encoding to use for new v24 frames, it defaults to simple ISO8859
     * but by changing this value you could always used UTF8 for example whether you needed to or not
     */
    public byte getId3v24DefaultTextEncoding() {
        return id3v24DefaultTextEncoding;
    }

    /**
     * Set the default text encoding to use for new v24 frames, it defaults to simple ISO8859
     * but by changing this value you could always used UTF8 for example whether you needed to or not
     */
    public void setId3v24DefaultTextEncoding(byte id3v24DefaultTextEncoding) {
        if ((id3v24DefaultTextEncoding == TextEncoding.ISO_8859_1) ||
                (id3v24DefaultTextEncoding == TextEncoding.UTF_16) ||
                (id3v24DefaultTextEncoding == TextEncoding.UTF_16BE) ||
                (id3v24DefaultTextEncoding == TextEncoding.UTF_8)) {
            this.id3v24DefaultTextEncoding = id3v24DefaultTextEncoding;
        }

    }

    /**
     * Get the text encoding to use for new v24 frames when unicode is required, it defaults to UTF16 just
     * because this encoding is understand by all ID3 versions
     */
    public byte getId3v24UnicodeTextEncoding() {
        return id3v24UnicodeTextEncoding;
    }

    /**
     * Set the text encoding to use for new v24 frames when unicode is required, it defaults to UTF16 just
     * because this encoding is understand by all ID3 versions
     */
    public void setId3v24UnicodeTextEncoding(byte id3v24UnicodeTextEncoding) {
        if ((id3v24UnicodeTextEncoding == TextEncoding.UTF_16) ||
                (id3v24UnicodeTextEncoding == TextEncoding.UTF_16BE) ||
                (id3v24UnicodeTextEncoding == TextEncoding.UTF_8)) {
            this.id3v24UnicodeTextEncoding = id3v24UnicodeTextEncoding;
        }
    }

    /**
     * When writing frames if this is set to true then the frame will be written
     * using the defaults disregarding the text encoding originally used to create
     * the frame.
     */
    public boolean isResetTextEncodingForExistingFrames() {
        return resetTextEncodingForExistingFrames;
    }

    /**
     * When writing frames if this is set to true then the frame will be written
     * using the defaults disregarding the text encoding originally used to create
     * the frame.
     */
    public void setResetTextEncodingForExistingFrames(boolean resetTextEncodingForExistingFrames) {
        this.resetTextEncodingForExistingFrames = resetTextEncodingForExistingFrames;
    }

    public boolean isTruncateTextWithoutErrors() {
        return truncateTextWithoutErrors;
    }

    public void setTruncateTextWithoutErrors(boolean truncateTextWithoutErrors) {
        this.truncateTextWithoutErrors = truncateTextWithoutErrors;
    }

    public boolean isPadNumbers() {
        return padNumbers;
    }

    public void setPadNumbers(boolean padNumbers) {
        this.padNumbers = padNumbers;
    }

    public boolean isAndroid() {
        return isAndroid;
    }

    public void setAndroid(boolean android) {
        isAndroid = android;
    }

    /**
     * When this is set and using the generic interface ealvatag will make some adjustmensts
     * when saving field sso they work best with the specified Tagger
     */
    public int getPlayerCompatability() {
        return playerCompatability;
    }

    public void setPlayerCompatability(int playerCompatability) {
        this.playerCompatability = playerCompatability;
    }

    /**
     * When you specify a field should be stored as UTF16 in ID3 this means write with BOM indicating whether
     * written as Little Endian or Big Endian, its defaults to little Endian
     */
    public boolean isEncodeUTF16BomAsLittleEndian() {
        return isEncodeUTF16BomAsLittleEndian;
    }

    @SuppressWarnings("SameParameterValue")
    public void setEncodeUTF16BomAsLittleEndian(boolean encodeUTF16BomAsLittleEndian) {
        isEncodeUTF16BomAsLittleEndian = encodeUTF16BomAsLittleEndian;
    }

    /**
     * When we have to create new audio files and shift audio data to fit in more metadata this value
     * set the maximum amount in bytes that can be transferred in one call, this is to protect against
     * various OutOfMemoryExceptions that cna occur, especially on networked filesystems.
     */
    public long getWriteChunkSize() {
        return writeChunkSize;
    }

    public void setWriteChunkSize(long writeChunkSize) {
        this.writeChunkSize = writeChunkSize;
    }

    /**
     * If enabled we always use the ©gen atom rather than the gnre atom when writing genres to mp4s
     * This is known to help some android apps
     */
    public boolean isWriteMp4GenresAsText() {
        return isWriteMp4GenresAsText;
    }

    @SuppressWarnings("SameParameterValue")
    public void setWriteMp4GenresAsText(boolean writeMp4GenresAsText) {
        isWriteMp4GenresAsText = writeMp4GenresAsText;
    }

    /**
     * If enabled we always use the ©gen atom rather than the gnre atom when writing genres to mp4s
     * This is known to help some android apps
     */
    public boolean isWriteMp3GenresAsText() {
        return isWriteMp3GenresAsText;
    }

    public void setWriteMp3GenresAsText(boolean writeMp3GenresAsText) {
        isWriteMp3GenresAsText = writeMp3GenresAsText;
    }

    /**
     * Total length of number, i.e if set to 2 the value 1 would be stored as 01, if set to 3 would bs stored as 001
     */
    public PadNumberOption getPadNumberTotalLength() {
        return padNumberTotalLength;
    }

    public void setPadNumberTotalLength(PadNumberOption padNumberTotalLength) {
        this.padNumberTotalLength = padNumberTotalLength;
    }

    /**
     * Itunes expects APIC description to be encoded as ISO-8859-1 even if text encoding is set to 1 (UTF16)
     */
    public boolean isAPICDescriptionITunesCompatible() {
        return isAPICDescriptionITunesCompatible;
    }

    public void setAPICDescriptionITunesCompatible(boolean APICDescriptionITunesCompatible) {
        isAPICDescriptionITunesCompatible = APICDescriptionITunesCompatible;
    }

    /**
     * Whether Files.isWritable should be used to check if a file can be written. In some
     * cases, isWritable can return false negatives.
     */
    public boolean isCheckIsWritable() {
        return checkIsWritable;
    }

    public void setCheckIsWritable(boolean checkIsWritable) {
        this.checkIsWritable = checkIsWritable;
    }

    /**
     * <p>
     * If set to {@code true}, when writing, make an attempt to overwrite the existing file in-place
     * instead of first moving it out of the way and moving a temp file into its place.
     * </p>
     * <p>
     * Preserving the file identity has the advantage of preserving the creation time
     * as well as the Unix inode or Windows
     * <a href="https://msdn.microsoft.com/en-us/library/aa363788(v=vs.85).aspx">fileIndex</a>.
     * </p>
     *
     * @return {@code true} or {@code false}. Default is {@code false}.
     */
    public boolean isPreserveFileIdentity() {
        return preserveFileIdentity;
    }

    /**
     * If set to {@code true}, when writing, make an attempt to preserve the file identity.
     *
     * @param preserveFileIdentity {@code true} or {@code false}
     * @see #isPreserveFileIdentity()
     */
    @SuppressWarnings("SameParameterValue")
    public void setPreserveFileIdentity(boolean preserveFileIdentity) {
        this.preserveFileIdentity = preserveFileIdentity;
    }

    public boolean isWriteWavForTwonky() {
        return isWriteWavForTwonky;
    }

    @SuppressWarnings("SameParameterValue")
    public void setWriteWavForTwonky(boolean isWriteWavForTwonky) {
        this.isWriteWavForTwonky = isWriteWavForTwonky;
    }
}
