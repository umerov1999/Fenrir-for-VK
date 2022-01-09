package ealvatag.logging;

/**
 * Defines Error Messages
 */
public final class ErrorMessage {
    public static final String OGG_VORBIS_NO_FRAMING_BIT = "The OGG Stream is not valid, Vorbis tag valid framing bit is wrong %s ";
    public static final String GENERAL_WRITE_FAILED = "Cannot make changes to file %s";
    public static final String GENERAL_WRITE_FAILED_FILE_LOCKED =
            "Cannot make changes to file %s because it is being used by another application";
    public static final String GENERAL_WRITE_FAILED_BECAUSE_FILE_IS_TOO_SMALL =
            "Cannot make changes to file %s because too small to be an audio file";
    public static final String GENERAL_WRITE_FAILED_TO_DELETE_ORIGINAL_FILE =
            "Cannot make changes to file %s because unable to delete the original file ready for updating from temporary file %s";
    public static final String GENERAL_WRITE_FAILED_TO_RENAME_TO_ORIGINAL_FILE =
            "Cannot make changes to file %s because unable to rename from temporary file %s";
    public static final String GENERAL_WRITE_FAILED_TO_RENAME_ORIGINAL_FILE_TO_BACKUP =
            "Cannot make changes to file %s because unable to rename the original file to %s";
    public static final String GENERAL_WRITE_FAILED_TO_RENAME_ORIGINAL_BACKUP_TO_ORIGINAL = "Unable to rename backup %s back to file %s";
    public static final String GENERAL_WRITE_FAILED_NEW_FILE_DOESNT_EXIST = "New file %s does not exist";
    public static final String GENERAL_WRITE_FAILED_BECAUSE = "Cannot make changes to file %s";
    public static final String GENERAL_WRITE_FAILED_BECAUSE_FILE_NOT_FOUND =
            "Cannot make changes to file %s because the file cannot be found";
    public static final String GENERAL_WRITE_WARNING_UNABLE_TO_DELETE_BACKUP_FILE = "Unable to delete the backup file %s";
    public static final String GENERAL_WRITE_PROBLEM_CLOSING_FILE_HANDLE = "Problem closing file handles for file %s";
    public static final String GENERAL_DELETE_FAILED = "Cannot delete file %s because not writable";
    public static final String GENERAL_DELETE_FAILED_BECAUSE_FILE_IS_TOO_SMALL =
            "Cannot write to file %s because too small to be an audio file";
    public static final String GENERAL_WRITE_FAILED_TO_DELETE_TEMPORARY_FILE = "Unable to delete the temporary file %s";
    public static final String GENERAL_WRITE_FAILED_TO_CREATE_TEMPORARY_FILE_IN_FOLDER =
            "Cannot modify %s because do not have permissions to create files in the folder";
    public static final String GENERAL_WRITE_FAILED_TO_MODIFY_TEMPORARY_FILE_IN_FOLDER =
            "Cannot modify %s because do not have permissions to modify files in the folder %s";
    public static final String GENERAL_WRITE_FAILED_TO_OPEN_FILE_FOR_EDITING =
            "Cannot modify %s because do not have permissions to modify file";
    public static final String NULL_PADDING_FOUND_AT_END_OF_MP4 = "Null Padding found at end of file starting at offset %s";
    public static final String OGG_HEADER_CANNOT_BE_FOUND = "OggS Header could not be found, not an ogg stream %s";
    public static final String ASF_FILE_HEADER_SIZE_DOES_NOT_MATCH_FILE_SIZE =
            "For file %s the File header size is %d but different to actual file size of %d";
    public static final String ASF_FILE_HEADER_MISSING = "For file %s the File Header missing. Invalid ASF/WMA file.";
    public static final String ASF_HEADER_MISSING = "For file %s the Asf Header missing. Invalid ASF/WMA file.";
    public static final String WMA_INVALID_LANGUAGE_USE = "The use of language %s ist not allowed for %s (only %s allowed)";
    public static final String WMA_INVALID_STREAM_REFERNCE = "The stream number %s is invalid. Only %s allowed for %s.";
    public static final String WMA_INVALID_GUID_USE = "The use of GUID ist not allowed for %s";
    public static final String WMA_LENGTH_OF_DATA_IS_TOO_LARGE =
            "Trying to create field with %s bytes of data but the maximum data allowed in WMA files is %s for %s.";
    public static final String WMA_LENGTH_OF_LANGUAGE_IS_TOO_LARGE =
            "Trying to create language entry, but UTF-16LE representation is %s and exceeds maximum allowed of 255.";
    public static final String WMA_LENGTH_OF_STRING_IS_TOO_LARGE =
            "Trying to create field but UTF-16LE representation is %s and exceeds maximum allowed of 65535.";
    public static final String VORBIS_COMMENT_LENGTH_TOO_LARGE = "Comment field length is very large %s , assuming comment is corrupt";
    public static final String VORBIS_COMMENT_LENGTH_LARGE_THAN_HEADER = "Comment field length %s is larger than total comment header %s ";
    public static final String NO_PERMISSIONS_TO_WRITE_TO_FILE = "Unable to write to %s";
    public static final String DO_NOT_KNOW_HOW_TO_CREATE_THIS_ATOM_TYPE = "DO not know how to create this atom type %s";
    public static final String OGG_CONTAINS_ID3TAG = "Ogg File contains invalid ID3 Tag, skipping ID3 Tag of length:%s";
    public static final String FLAC_CONTAINS_ID3TAG = "%s Flac File contains invalid ID3 Tag, skipping ID3 Tag of length:%d";
    public static final String ADDITIONAL_MOOV_ATOM_AT_END_OF_MP4 = "Additional moov atom found at end of file starting at offset %s";
    public static final String ATOM_LENGTH_LARGER_THAN_DATA =
            "The atom %s states its data length to be %s but there are only %s bytes remaining in the file";
    public static final String NO_AUDIO_HEADER_FOUND = "No audio header found within %s";
    public static final String FLAC_NO_BLOCKTYPE = "Flac file has invalid block type %s";
    public static final String GENERAL_READ = "File %s being read";
    public static final String INVALID_DATATYPE = "Problem reading %s in %s. %s";
    public static final String MP4_FILE_NOT_CONTAINER = "This file does not appear to be an Mp4 file";
    public static final String MP4_FILE_NOT_AUDIO = "This file does not appear to be an Mp4 Audio file, could be corrupted or video ";
    public static final String MP4_FILE_IS_VIDEO = "This file appears to be an Mp4 Video file, video files are not supported ";
    public static final String MP4_CHANGES_TO_FILE_FAILED = "Unable to make changes to Mp4 file";
    public static final String MP4_CHANGES_TO_FILE_FAILED_NO_DATA = "Unable to make changes to Mp4 file, no data was written";
    public static final String MP4_CHANGES_TO_FILE_FAILED_DATA_CORRUPT =
            "Unable to make changes to Mp4 file, invalid data length has been written";
    public static final String MP4_CHANGES_TO_FILE_FAILED_NO_TAG_DATA = "Unable to make changes to Mp4 file, no tag data has been written";
    public static final String MP4_CHANGES_TO_FILE_FAILED_CANNOT_FIND_AUDIO =
            "Unable to make changes to Mp4 file, unable to determine start of audio";
    public static final String FLAC_NO_FLAC_HEADER_FOUND = "Flac Header not found, not a flac file";
    public static final String OGG_VORBIS_NO_VORBIS_HEADER_FOUND = "Cannot find vorbis setup parentHeader";
    public static final String OGG_VORBIS_NO_SETUP_BLOCK = "Could not find the Ogg Setup block";
    public static final String GENERAL_UNIDENITIFED_IMAGE_FORMAT =
            "Cannot safetly identify the format of this image setting to default type of Png";
    public static final String MP4_FILE_META_ATOM_CHILD_DATA_NOT_NULL = "Expect data in meta box to be null";
    public static final String WMA_ONLY_STRING_IN_CD = "Only Strings are allowed in content description objects";
    public static final String MP4_CANNOT_FIND_AUDIO = "Unable to determine start of audio in file";
    public static final String ARTWORK_CANNOT_BE_CREATED_WITH_THIS_METHOD = "Cover Art cannot be created using this method";
    public static final String NOT_STANDARD_MP$_GENRE = "This is not a standard genre value, use custom genre field instead";
    public static final String MP4_CHANGES_TO_FILE_FAILED_INCORRECT_OFFSETS =
            "Unable to make changes to Mp4 file, incorrect offsets written difference was %s";
    public static final String MP4_CHANGES_TO_FILE_FAILED_INCORRECT_NUMBER_OF_TRACKS =
            "Unable to make changes to Mp4 file, incorrect number of tracks: %s vs %s";
    public static final String MP4_REVERSE_DNS_FIELD_HAS_NO_DATA = "Reverse dns field:%s has no data";
    public static final String MP4_UNABLE_READ_REVERSE_DNS_FIELD =
            "Unable to create reverse dns field because of exception, adding as binary data instead";
    public static final String MP3_ID3TAG_LENGTH_INCORRECT =
            " %s:Checking further because the ID3 Tag ends at %s but the mp3 audio doesnt start until %s";
    public static final String MP3_RECALCULATED_POSSIBLE_START_OF_MP3_AUDIO = "%s: Recalculated possible start of the audio to be at %s";
    public static final String MP3_RECALCULATED_START_OF_MP3_AUDIO = "%s: Recalculated the start of the audio to be at %s";
    public static final String MP3_START_OF_AUDIO_CONFIRMED =
            "%s: Confirmed audio starts at %s whether searching from start or from end of ID3 tag";
    public static final String MP3_URL_SAVED_ENCODED = "Url:%s saved in encoded form as %s";
    public static final String MP3_UNABLE_TO_ENCODE_URL =
            "Unable to save url:%s because cannot encode all characters setting to blank instead";
    public static final String MP4_NO_GENREID_FOR_GENRE = "No genre id could be found for this genre atom with data length %s";
    public static final String MP4_GENRE_OUT_OF_RANGE = "Genre Id %s does not map to a valid genre";
    public static final String MP3_PICTURE_TYPE_INVALID = "Picture Type is set to invalid value:%s";
    public static final String MP3_REFERENCE_KEY_INVALID = "%s:No key could be found with the value of:%s";
    public static final String MP3_UNABLE_TO_ADJUST_PADDING =
            "Problem adjusting padding in large file, expecting to write:%s only wrote:%s";
    public static final String MP4_IMAGE_FORMAT_IS_NOT_TO_EXPECTED_TYPE =
            "ImageFormat for cover art atom is not set to a known image format, instead set to %s";
    public static final String MP3_FRAME_IS_COMPRESSED = "Filename %s:%s is compressed";
    public static final String MP3_FRAME_IS_ENCRYPTED = "Filename %s:%s is encrypted";
    public static final String MP3_FRAME_IS_GROUPED = "Filename %s:%s is grouped";
    public static final String MP3_FRAME_IS_UNSYNCHRONISED = "Filename %s:%s is unsynchronised";
    public static final String MP3_FRAME_IS_DATA_LENGTH_INDICATOR = "Filename %s:%s has a data length indicator";
    public static final String ID3_EXTENDED_HEADER_SIZE_INVALID =
            "%s Invalid Extended Header Size of %s assuming no extended header after all";
    public static final String ID3_EXTENDED_HEADER_SIZE_TOO_SMALL = "%s Invalid Extended Header Size of %s is too smal to be valid";
    public static final String ID3_INVALID_OR_UNKNOWN_FLAG_SET = "%s Invalid or unknown bit flag 0x%s set in ID3 tag header";
    public static final String ID3_TAG_UNSYNCHRONIZED = "%s the ID3 Tag is unsynchronized";
    public static final String ID3_TAG_EXPERIMENTAL = "%s the ID3 Tag is experimental";
    public static final String ID3_TAG_FOOTER = "%s the ID3 Tag is has a footer";
    public static final String ID3_TAG_EXTENDED = "%s the ID3 Tag is extended";
    public static final String ID3_TAG_CRC = "%s the ID3 Tag has crc check";
    public static final String ID3_TAG_COMPRESSED = "%s the ID3 Tag is compressed";
    public static final String ID3_TAG_CRC_SIZE = "%s According to Extended Header the ID3 Tag has crc32 of %s";
    public static final String ID3_TAG_PADDING_SIZE = "%s According to Extended Header the ID3 Tag has padding size of %s";
    public static final String ID_TAG_SIZE = "%s Tag size is %s according to header (does not include header size, add 10)";
    public static final String ID3_TAG_CRC_FLAG_SET_INCORRECTLY = "%s CRC Data flag not set correctly.";
    public static final String ID3_UNABLE_TO_DECOMPRESS_FRAME = "Unable to decompress frame %s in file %s";
    public static final String NO_WRITER_FOR_THIS_FORMAT = "No Writer associated with this extension:%s";
    public static final String NO_READER_FOR_THIS_FORMAT = "No Reader associated with this extension:%s";
    public static final String NO_DELETER_FOR_THIS_FORMAT = "No Deleter associated with this extension:%s";

    private ErrorMessage() {
    }

}
