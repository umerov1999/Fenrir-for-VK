package ealvatag.audio.mp3;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import ealvatag.audio.AudioFile;
import ealvatag.audio.AudioFileReader;
import ealvatag.audio.GenericAudioHeader;
import ealvatag.audio.exceptions.CannotReadException;
import ealvatag.audio.exceptions.InvalidAudioFrameException;
import ealvatag.tag.TagException;
import ealvatag.tag.TagFieldContainer;

/**
 * Read Mp3 Info (retrofitted to entagged ,done differently to entagged which is why some methods throw RuntimeException)
 * because done elsewhere
 */
public class MP3FileReader extends AudioFileReader {
    protected GenericAudioHeader getEncodingInfo(RandomAccessFile raf) throws CannotReadException, IOException {
        throw new RuntimeException("MP3FileReader.getEncodingInfo should be called");
    }

    protected TagFieldContainer getTag(RandomAccessFile raf, boolean ignoreArtwork) throws CannotReadException, IOException {
        throw new RuntimeException("MP3FileReader.getEncodingInfo should be called");
    }

    public AudioFile read(File f, String extension, boolean ignoreArtwork) throws IOException,
            TagException,
            CannotReadException,
            InvalidAudioFrameException {
        return new MP3File(f, extension, MP3File.LOAD_IDV1TAG | MP3File.LOAD_IDV2TAG, ignoreArtwork);
    }

}
