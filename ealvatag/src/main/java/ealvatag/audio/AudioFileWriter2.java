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

import static ealvatag.logging.EalvaTagLog.LogLevel.ERROR;
import static ealvatag.logging.EalvaTagLog.LogLevel.WARN;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

import ealvatag.audio.exceptions.CannotReadException;
import ealvatag.audio.exceptions.CannotWriteException;
import ealvatag.audio.exceptions.NoWritePermissionsException;
import ealvatag.logging.EalvaTagLog;
import ealvatag.logging.EalvaTagLog.JLogger;
import ealvatag.logging.EalvaTagLog.JLoggers;
import ealvatag.logging.ErrorMessage;
import ealvatag.tag.Tag;
import ealvatag.tag.TagFieldContainer;
import ealvatag.tag.TagOptionSingleton;

/**
 * Different write because ...??
 * <p>
 * Created by Paul on 28/01/2016.
 */
public abstract class AudioFileWriter2 extends AudioFileWriter {
    private static final JLogger LOG = JLoggers.get(AudioFileWriter2.class, EalvaTagLog.MARKER);

    /**
     * Delete the tag (if any) present in the given file
     *
     * @param af The file to process
     * @throws CannotWriteException if anything went wrong
     */
    @Override
    public void delete(AudioFile af) throws CannotWriteException {
        File file = af.getFile();
        checkCanWriteAndSize(af, file);
        try (FileChannel channel = new RandomAccessFile(file, "rw").getChannel()) {
            deleteTag(af.getTag().orNull(), channel, file.getAbsolutePath());
        } catch (IOException e) {
            LOG.log(WARN, e, ErrorMessage.GENERAL_DELETE_FAILED, file);
            throw new CannotWriteException(e, ErrorMessage.GENERAL_DELETE_FAILED, file);
        }
    }

    private void checkCanWriteAndSize(AudioFile af, File file) throws CannotWriteException {
        if (TagOptionSingleton.getInstance().isCheckIsWritable() && !file.canWrite()) {
            LOG.log(ERROR, ErrorMessage.NO_PERMISSIONS_TO_WRITE_TO_FILE, file);
            throw new CannotWriteException(ErrorMessage.GENERAL_DELETE_FAILED, file);
        }

        if (af.getFile().length() <= MINIMUM_FILESIZE) {
            throw new CannotWriteException(ErrorMessage.GENERAL_DELETE_FAILED_BECAUSE_FILE_IS_TOO_SMALL, file);
        }
    }

    /**
     * Replace with new tag
     *
     * @param audioFile The file we want to process
     */
    @Override
    public void write(AudioFileImpl audioFile) throws CannotWriteException {
        File file = audioFile.getFile();
        checkCanWriteAndSize(audioFile, file);
        try (FileChannel channel = new RandomAccessFile(file, "rw").getChannel()) {
            writeTag(audioFile.getTagFieldContainer(), channel, file.getAbsolutePath());
        } catch (FileNotFoundException e) {
            if (file.exists()) {
                // file exists, permission error
                LOG.log(WARN, ErrorMessage.GENERAL_WRITE_FAILED_TO_OPEN_FILE_FOR_EDITING, file);
                throw new NoWritePermissionsException(e, ErrorMessage.GENERAL_WRITE_FAILED_TO_OPEN_FILE_FOR_EDITING, file);
            } else {
                LOG.log(WARN, ErrorMessage.GENERAL_WRITE_FAILED_BECAUSE_FILE_NOT_FOUND, file);
                throw new CannotWriteException(e, ErrorMessage.GENERAL_WRITE_FAILED_BECAUSE_FILE_NOT_FOUND, file);
            }
        } catch (IOException e) {
            LOG.log(WARN, e, ErrorMessage.GENERAL_WRITE_FAILED_BECAUSE, file);
            throw new CannotWriteException(e);
        }
    }

    protected abstract void deleteTag(Tag tag, FileChannel channel, String fileName) throws CannotWriteException;


    public void deleteTag(Tag tag, RandomAccessFile raf, RandomAccessFile tempRaf)
            throws CannotReadException, CannotWriteException, IOException {
        throw new UnsupportedOperationException("Old method not used in version 2");
    }

    protected abstract void writeTag(TagFieldContainer tag, FileChannel channel, String fileName) throws CannotWriteException;

    protected void writeTag(AudioFile audioFile, TagFieldContainer tag, RandomAccessFile raf, RandomAccessFile rafTemp)
            throws CannotReadException, CannotWriteException, IOException {
        throw new UnsupportedOperationException("Old method not used in version 2");
    }
}
