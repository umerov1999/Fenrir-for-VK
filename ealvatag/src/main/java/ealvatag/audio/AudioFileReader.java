/*
 * Copyright (c) 2017 Eric A. Snell
 *
 * This file is part of eAlvaTag.
 *
 * eAlvaTag is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * eAlvaTag is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with eAlvaTag.  If not,
 * see <http://www.gnu.org/licenses/>.
 */
package ealvatag.audio;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import ealvatag.audio.exceptions.CannotReadException;
import ealvatag.audio.exceptions.InvalidAudioFrameException;
import ealvatag.logging.EalvaTagLog;
import ealvatag.logging.EalvaTagLog.JLogger;
import ealvatag.logging.EalvaTagLog.JLoggers;
import ealvatag.logging.EalvaTagLog.LogLevel;
import ealvatag.logging.ErrorMessage;
import ealvatag.tag.TagException;
import ealvatag.tag.TagFieldContainer;

/*
 * This abstract class is the skeleton for tag readers. It handles the creation/closing of
 * the randomaccessfile objects and then call the subclass method getEncodingInfo and getTag.
 * These two method have to be implemented in the subclass.
 *
 *@author	Raphael Slinckx
 *@version	$Id$
 *@since	v0.02
 */

public abstract class AudioFileReader {

    protected static final int MINIMUM_SIZE_FOR_VALID_AUDIO_FILE = 100;
    // Logger Object
    private static final JLogger LOG = JLoggers.get(AudioFileReader.class, EalvaTagLog.MARKER);

    /*
     * Returns the encoding info object associated wih the current File.
     * The subclass can assume the RAF pointer is at the first byte of the file.
     * The RandomAccessFile must be kept open after this function, but can point
     * at any offset in the file.
     *
     * @param raf The RandomAccessFile associtaed with the current file
     * @exception IOException is thrown when the RandomAccessFile operations throw it (you should never throw them
     * manually)
     * @exception CannotReadException when an error occured during the parsing of the encoding infos
     */
    protected abstract GenericAudioHeader getEncodingInfo(RandomAccessFile raf) throws CannotReadException, IOException;


    /*
     * Same as above but returns the Tag contained in the file, or a new one.
     *
     * @param raf The RandomAccessFile associted with the current file
     * @exception IOException is thrown when the RandomAccessFile operations throw it (you should never throw them
     * manually)
     * @exception CannotReadException when an error occured during the parsing of the tag
     */
    protected abstract TagFieldContainer getTag(RandomAccessFile raf, boolean ignoreArtwork) throws CannotReadException, IOException;

    public AudioFile read(File file,
                          String extension,
                          boolean ignoreArtwork) throws CannotReadException,
            IOException,
            TagException,
            InvalidAudioFrameException {
        LOG.log(LogLevel.TRACE, ErrorMessage.GENERAL_READ, file);
        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            raf.seek(0);
            return makeAudioFile(raf, file, extension, ignoreArtwork);
        }
//        catch (Exception e) {
//            LOG.log(LogLevel.ERROR, ErrorMessage.GENERAL_READ.getMsg(file.getAbsolutePath()), e);
//            throw new CannotReadException(file.getAbsolutePath() + ":" + e.getMessage(), e);
//        }
    }

    /**
     * Put read header and read tag in one method so subclasses aren't forced into the 2 step process, but can optimize how the
     * particular format is read.
     *
     * @param raf           the {@link RandomAccessFile} containing the data
     * @param file          file information
     * @param extension     the file extension that was used to identify the file type
     * @param ignoreArtwork
     * @return an {@link AudioFile} containing the parsed header and tag
     * @throws CannotReadException if there is some parsing error
     * @throws IOException         if there is an error reading from the file
     */
    private AudioFile makeAudioFile(RandomAccessFile raf,
                                    File file,
                                    String extension,
                                    boolean ignoreArtwork) throws CannotReadException, IOException {
        GenericAudioHeader info = getEncodingInfo(raf);
        raf.seek(0);
        return new AudioFileImpl(file, extension, info, getTag(raf, ignoreArtwork));
    }
}
