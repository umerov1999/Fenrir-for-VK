/*
 * Entagged Audio Tag library
 * Copyright (c) 2003-2005 Raphaël Slinckx <raphael@slinckx.net>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package ealvatag.audio;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import ealvatag.audio.exceptions.CannotReadException;
import ealvatag.audio.exceptions.CannotWriteException;
import ealvatag.audio.exceptions.InvalidAudioFrameException;
import ealvatag.audio.mp3.MP3FileReader;
import ealvatag.audio.mp3.MP3FileWriter;
import ealvatag.audio.real.RealFileReader;
import ealvatag.logging.ErrorMessage;
import ealvatag.tag.TagException;

/**
 * The main entry point for the Tag Reading/Writing operations, this class will
 * select the appropriate reader/writer for the given file.
 * <p>
 * <p>
 * It selects the appropriate reader/writer based on the file extension (case
 * ignored).
 *
 * @author Raphael Slinckx
 * @version $Id$
 * @see AudioFileImpl
 * @see ealvatag.tag.Tag
 * @since v0.01
 */
@SuppressWarnings("unused")
public class AudioFileIO {

    private static AudioFileIO defaultInstance;
    private final ModificationHandler modificationHandler;
    private final ImmutableMap<String, AudioFileReaderFactory> readerFactories;
    private final ImmutableMap<String, AudioFileWriterFactory> writerFactories;

    private AudioFileIO() {
        modificationHandler = new ModificationHandler();

        // !! Do not forget to also add new supported extensions to AudioFileFilter
        // !!
        // TODO: 1/19/17 This warning "do not forget" = ensure tests check this

        // Tag Readers
        AudioFileReaderFactory realReaderFactory = new AudioFileReaderFactory() {
            @Override
            public AudioFileReader make() {
                return new RealFileReader();
            }
        };

        readerFactories = ImmutableMap.<String, AudioFileReaderFactory>builder()
                .put(SupportedFileFormat.MP3.getFileSuffix(), new CachingAudioFileReaderFactory() {
                    @Override
                    protected AudioFileReader doMake() {
                        return new MP3FileReader();
                    }
                })
                .put(SupportedFileFormat.RA.getFileSuffix(), realReaderFactory)
                .put(SupportedFileFormat.RM.getFileSuffix(), realReaderFactory)
                .build();

        writerFactories = ImmutableMap.<String, AudioFileWriterFactory>builder()
                .put(SupportedFileFormat.MP3.getFileSuffix(), new AudioFileWriterFactory() {
                    @Override
                    public AudioFileWriter make() {
                        return new MP3FileWriter();
                    }
                })
                .build();
    }

    static AudioFileIO instance() {
        if (defaultInstance == null) {
            synchronized (AudioFileIO.class) {
                if (defaultInstance == null) {
                    defaultInstance = new AudioFileIO();
                }
            }
        }
        return defaultInstance;
    }

    /**
     * Read the tag contained in the given file.
     *
     * @param f The file to read.
     * @return The AudioFile with the file tag and the file encoding info.
     * @throws CannotReadException        If the file could not be read, the extension wasn't recognized, or an IO error occurred during the
     *                                    read.
     * @throws TagException               various tag exceptions (to be refactored)
     * @throws java.io.IOException        if error reading
     * @throws InvalidAudioFrameException if audio frame is corrupted
     */
    public static AudioFile read(File f) throws CannotReadException,
            IOException,
            TagException,
            InvalidAudioFrameException {
        return instance().readFile(f, false);
    }

    /**
     * Read the tag contained in the given file, but ignore any artwork fields. In a memory constrained environment (Android) doing batch
     * reads, this is a very large performance improvement.
     * <p>
     * However, iff the file contained artwork the resulting Tag is read-only to prevent unintended lost artwork. Don't use this method
     * when editing tags as you can't save any editing results.
     * <p>
     * This is not fully supported or tested across all file types.
     *
     * @param f The file to read.
     * @return A read-only AudioFile
     * @throws CannotReadException        If the file could not be read, the extension wasn't recognized, or an IO error occurred during the
     *                                    read.
     * @throws TagException               various tag exceptions (to be refactored)
     * @throws java.io.IOException        if error reading
     * @throws InvalidAudioFrameException if audio frame is corrupted
     */
    public static AudioFile readIgnoreArtwork(File f) throws CannotReadException,
            IOException,
            TagException,
            InvalidAudioFrameException {
        return instance().readFile(f, true);
    }

    /**
     * Read the tag contained in the given file.
     *
     * @param f   The file to read.
     * @param ext The extension to be used.
     * @return The AudioFile with the file tag and the file encoding info.
     * @throws CannotReadException        If the file could not be read, the extension wasn't recognized, or an IO error occurred during the
     *                                    read.
     * @throws TagException               various tag exceptions (to be refactored)
     * @throws java.io.IOException        if error reading
     * @throws InvalidAudioFrameException if audio frame is corrupted
     */
    public static AudioFile readAs(File f, String ext) throws CannotReadException,
            IOException,
            TagException,
            InvalidAudioFrameException {
        return instance().readFileAs(f, ext.toLowerCase());
    }

    /**
     * Read the tag contained in the given file.
     *
     * @param f   The file to read.
     * @param ext The extension to be used.
     * @return The AudioFile with the file tag and the file encoding info.
     * @throws CannotReadException        If the file could not be read, the extension wasn't recognized, or an IO error occurred during the
     *                                    read.
     * @throws TagException               various tag exceptions (to be refactored)
     * @throws java.io.IOException        if error reading
     * @throws InvalidAudioFrameException if audio frame is corrupted
     */
    private AudioFile readFileAs(File f, String ext) throws CannotReadException,
            IOException,
            TagException,
            InvalidAudioFrameException {
        return readAudioFile(f, ext, true);
    }

    /**
     * Read the tag contained in the given file.
     *
     * @param file The file to read.
     * @return The AudioFile with the file tag and the file encoding info.
     * @throws CannotReadException        If the file could not be read, the extension wasn't recognized, or an IO error occurred during the
     *                                    read.
     * @throws TagException               various tag exceptions (to be refactored)
     * @throws java.io.IOException        if error reading
     * @throws InvalidAudioFrameException if audio frame is corrupted
     */
    public AudioFile readFileMagic(File file) throws CannotReadException,
            IOException,
            TagException,
            InvalidAudioFrameException {
        return readAudioFile(file, Utils.getMagicExtension(file), false);
    }

    private AudioFile readFile(File file, boolean ignoreArtwork) throws CannotReadException,
            IOException,
            TagException,
            InvalidAudioFrameException {
        return readAudioFile(file, Files.getFileExtension(file.getName()), ignoreArtwork);
    }

    /**
     * Delete the tag, if any, contained in the given file.
     *
     * @param f The file where the tag will be deleted
     * @throws ealvatag.audio.exceptions.CannotWriteException If the file could not be written/accessed, the extension wasn't recognized, or
     *                                                        other IO error occurred.
     */
    void deleteTag(AudioFileImpl f) throws CannotWriteException {
        String ext = Files.getFileExtension(f.getFile().getName());

        AudioFileWriter afw = getWriterForExtension(ext);

        afw.delete(f);
    }

    private AudioFileWriter getWriterForExtension(String ext) throws CannotWriteException {
        AudioFileWriterFactory factory = writerFactories.get(ext);
        if (factory == null) {
            throw new CannotWriteException(ErrorMessage.NO_DELETER_FOR_THIS_FORMAT, ext);
        }
        return factory.make().setAudioFileModificationListener(modificationHandler);
    }

    private AudioFile readAudioFile(File f, String ext, boolean ignoreArtwork) throws CannotReadException,
            IOException,
            TagException,
            InvalidAudioFrameException {
        String extension = ext.toLowerCase(Locale.ROOT);
        return getReaderForExtension(extension).read(f, extension, ignoreArtwork);
    }

    private AudioFileReader getReaderForExtension(String ext) throws CannotReadException {
        AudioFileReaderFactory factory = readerFactories.get(ext);
        if (factory == null) {
            throw new CannotReadException(ext, ErrorMessage.NO_READER_FOR_THIS_FORMAT);
        }
        return factory.make();
    }

    /**
     * Write the tag contained in the audioFile in the actual file on the disk.
     *
     * @param audioFile The AudioFile to be written
     * @throws CannotWriteException If the file could not be written/accessed, the extension wasn't recognized, or other IO error occurred.
     */
    void writeFile(AudioFileImpl audioFile) throws CannotWriteException {
        String ext = audioFile.getExt();
        AudioFileWriter afw = getWriterForExtension(ext);
        if (afw == null) {
            throw new CannotWriteException(ErrorMessage.NO_WRITER_FOR_THIS_FORMAT, ext);
        }
        afw.write(audioFile);
    }

    /**
     * Write the tag contained in the audioFile in the actual file on the disk.
     *
     * @param audioFile  The AudioFile to be written
     * @param targetPath The AudioFile path to which to be written without the extension. Cannot be null
     * @throws IllegalArgumentException if targetPath is null or empty
     * @throws CannotWriteException     If the file could not be written/accessed, the extension wasn't recognized, or other IO error
     *                                  occurred.
     */
    void writeFileAs(AudioFileImpl audioFile, String targetPath) throws CannotWriteException {
        try {
            File destination = new File(targetPath + "." + audioFile.getExt());
            Utils.copyThrowsOnException(audioFile.getFile(), destination);
            audioFile.setFile(destination);
            writeFile(audioFile);
        } catch (IOException e) {
            throw new CannotWriteException(e, "Error While Copying");
        }
    }


    // These can be added back in the AudioFile interface save() and saveAs() methods if they're needed. No tests involve the
    // AudioFileModificationListener and I see no evidence they're used in the MP3File, which is the actual writer (except for a little
    // bit of code in the AudioFileWriter specific subclass. Bit of a mess
//    /**
//     * Adds an listener for all file formats.
//     *
//     * @param listener listener
//     */
//    public void addAudioFileModificationListener(AudioFileModificationListener listener) {
//        this.modificationHandler.addAudioFileModificationListener(listener);
//    }
//
//    /**
//     * Removes a listener for all file formats.
//     *
//     * @param listener listener
//     */
//    public void removeAudioFileModificationListener(AudioFileModificationListener listener) {
//        this.modificationHandler.removeAudioFileModificationListener(listener);
//    }

}
