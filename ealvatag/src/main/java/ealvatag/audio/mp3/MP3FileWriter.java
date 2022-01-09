package ealvatag.audio.mp3;

import java.io.IOException;
import java.io.RandomAccessFile;

import ealvatag.audio.AudioFile;
import ealvatag.audio.AudioFileWriter;
import ealvatag.audio.exceptions.CannotReadException;
import ealvatag.audio.exceptions.CannotWriteException;
import ealvatag.tag.Tag;
import ealvatag.tag.TagFieldContainer;

/**
 * Write Mp3 Info (retrofitted to entagged ,done differently to entagged which is why some methods throw RuntimeException)
 * because done elsewhere
 */
public class MP3FileWriter extends AudioFileWriter {
    public void deleteTag(AudioFile f) throws CannotWriteException, CannotReadException {
        //Because audio file is an instanceof MP3File this directs it to save
        //taking into account if the tag has been sent to null in which case it will be deleted
        f.save();
    }

    public void writeFile(AudioFile f) throws CannotWriteException, CannotReadException {
        //Because audio file is an instanceof MP3File this directs it to save
        f.save();
    }

    /**
     * Delete the Id3v1 and ID3v2 tags from file
     *
     * @param af
     * @throws CannotWriteException
     */
    @Override
    public synchronized void delete(AudioFile af) throws CannotWriteException {
        ((MP3File) af).setID3v1Tag(null);
        ((MP3File) af).setID3v2Tag(null);
        af.save();
    }

    protected void writeTag(AudioFile audioFile, TagFieldContainer tag, RandomAccessFile raf, RandomAccessFile rafTemp) throws CannotWriteException, IOException {
        throw new RuntimeException("MP3FileReaderwriteTag should not be called");
    }

    protected void deleteTag(Tag tag, RandomAccessFile raf, RandomAccessFile tempRaf) throws CannotWriteException, IOException {
        throw new RuntimeException("MP3FileReader.getEncodingInfo should be called");
    }
}


