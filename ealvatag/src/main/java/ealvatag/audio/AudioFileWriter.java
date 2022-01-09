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
import static ealvatag.logging.EalvaTagLog.LogLevel.TRACE;
import static ealvatag.logging.EalvaTagLog.LogLevel.WARN;

import com.google.common.io.Files;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

import ealvatag.audio.exceptions.CannotReadException;
import ealvatag.audio.exceptions.CannotWriteException;
import ealvatag.audio.exceptions.ModifyVetoException;
import ealvatag.audio.mp3.MP3File;
import ealvatag.logging.EalvaTagLog;
import ealvatag.logging.EalvaTagLog.JLogger;
import ealvatag.logging.EalvaTagLog.JLoggers;
import ealvatag.logging.ErrorMessage;
import ealvatag.tag.NullTag;
import ealvatag.tag.Tag;
import ealvatag.tag.TagFieldContainer;
import ealvatag.tag.TagOptionSingleton;
import ealvatag.utils.Check;

/**
 * This abstract class is the skeleton for tag writers.
 * <p>
 * <p>
 * It handles the creation/closing of the randomaccessfile objects and then call
 * the subclass method writeTag or deleteTag. These two method have to be
 * implemented in the subclass.
 *
 * @author Raphael Slinckx
 * @version $Id: AudioFileWriter.java,v 1.21 2009/05/05 15:59:14 paultaylor Exp $
 * @since v0.02
 */
public abstract class AudioFileWriter {
    static final int MINIMUM_FILESIZE = 100;
    private static final String TEMP_FILENAME_SUFFIX = ".tmp";
    private static final String WRITE_MODE = "rw";
    // Logger Object
    private static final JLogger LOG = JLoggers.get(AudioFileWriter.class, EalvaTagLog.MARKER);

    //If filename too long try recreating it with length no longer than 50 that should be safe on all operating
    //systems
    private static final String FILE_NAME_TOO_LONG = "File name too long";
    private static final int FILE_NAME_TOO_LONG_SAFE_LIMIT = 50;

    private AudioFileModificationListener modificationListener = NullAudioFileModificationListener.INSTANCE;

    /**
     * Delete the tag (if any) present in the given file
     *
     * @param af The file to process
     * @throws CannotWriteException if anything went wrong
     */
    public void delete(AudioFile af) throws CannotWriteException {
        File file = af.getFile();
        if (TagOptionSingleton.getInstance().isCheckIsWritable() && file.canWrite()) {
            LOG.log(ERROR, "Unable to write file %s", file);
            throw new CannotWriteException(ErrorMessage.GENERAL_DELETE_FAILED, file);
        }

        if (af.getFile().length() <= MINIMUM_FILESIZE) {
            throw new CannotWriteException(ErrorMessage.GENERAL_DELETE_FAILED_BECAUSE_FILE_IS_TOO_SMALL, file);
        }

        RandomAccessFile raf = null;
        RandomAccessFile rafTemp = null;
        File tempF = null;

        // Will be set to true on VetoException, causing the finally block to
        // discard the tempfile.
        boolean revert = false;

        try {

            tempF = File.createTempFile(af.getFile().getName().replace('.', '_'),
                    TEMP_FILENAME_SUFFIX,
                    af.getFile().getParentFile());
            rafTemp = new RandomAccessFile(tempF, WRITE_MODE);
            raf = new RandomAccessFile(af.getFile(), WRITE_MODE);
            raf.seek(0);
            rafTemp.seek(0);

            try {
                modificationListener.fileWillBeModified(af, true);
                deleteTag(af.getTag().orNull(), raf, rafTemp);
                modificationListener.fileModified(af, tempF);
            } catch (ModifyVetoException veto) {
                throw new CannotWriteException(veto);
            }

        } catch (Exception e) {
            revert = true;
            throw new CannotWriteException(e, "\"" + af.getFile().getAbsolutePath() + "\" :" + e);
        } finally {
            // will be set to the remaining file.
            File result = af.getFile();
            try {
                if (raf != null) {
                    raf.close();
                }
                if (rafTemp != null) {
                    rafTemp.close();
                }

                if (tempF.length() > 0 && !revert) {
                    boolean deleteResult = af.getFile().delete();
                    if (!deleteResult) {
                        LOG.log(WARN, ErrorMessage.GENERAL_WRITE_FAILED_TO_DELETE_ORIGINAL_FILE, af.getFile(), tempF);
                        throw new CannotWriteException(ErrorMessage.GENERAL_WRITE_FAILED_TO_DELETE_ORIGINAL_FILE,
                                af.getFile(),
                                tempF);
                    }
                    boolean renameResult = tempF.renameTo(af.getFile());
                    if (!renameResult) {
                        LOG.log(WARN, ErrorMessage.GENERAL_WRITE_FAILED_TO_RENAME_TO_ORIGINAL_FILE, af.getFile(), tempF);
                        throw new CannotWriteException(ErrorMessage.GENERAL_WRITE_FAILED_TO_RENAME_TO_ORIGINAL_FILE,
                                af.getFile(),
                                tempF);
                    }
                    result = tempF;

                    // If still exists we can now delete
                    if (tempF.exists()) {
                        if (!tempF.delete()) {
                            // Non critical failed deletion
                            LOG.log(WARN, ErrorMessage.GENERAL_WRITE_FAILED_TO_DELETE_TEMPORARY_FILE, tempF);
                        }
                    }
                } else {
                    // It was created but never used
                    if (!tempF.delete()) {
                        // Non critical failed deletion
                        LOG.log(WARN, ErrorMessage.GENERAL_WRITE_FAILED_TO_DELETE_TEMPORARY_FILE, tempF);
                    }
                }
            } catch (Exception ex) {
                LOG.log(ERROR, ex, "AudioFileWriter exception cleaning up delete %s or %s", af.getFile(), tempF);
            }
            modificationListener.fileOperationFinished(result);
        }
    }

    /**
     * Delete the tag (if any) present in the given randomaccessfile, and do not
     * close it at the end.
     */
    public void delete(Tag tag, RandomAccessFile raf, RandomAccessFile tempRaf)
            throws CannotReadException, CannotWriteException, IOException {
        raf.seek(0);
        tempRaf.seek(0);
        deleteTag(tag, raf, tempRaf);
    }

    /**
     * Same as above, but delete tag in the file.
     *
     * @throws IOException          is thrown when the RandomAccessFile operations throw it (you should never throw them manually)
     * @throws CannotWriteException when an error occured during the deletion of the tag
     */
    protected abstract void deleteTag(Tag tag, RandomAccessFile raf, RandomAccessFile tempRaf)
            throws CannotReadException, CannotWriteException, IOException;

    /**
     * This method sets the {@link AudioFileModificationListener}.<br>
     * There is only one listener allowed, if you want more instances to be
     * supported, use the {@link ModificationHandler} to broadcast those events.<br>
     *
     * @param listener The listener. <code>null</code> allowed to deregister.
     */
    AudioFileWriter setAudioFileModificationListener(AudioFileModificationListener listener) {
        modificationListener = NullAudioFileModificationListener.nullToNullIntance(listener);
        return this;
    }

    /**
     * Prechecks before normal write
     * <p>
     * <ul>
     * <li>If the tag is actually empty, remove the tag</li>
     * <li>if the file is not writable, throw exception
     * <li>
     * <li>If the file is too small to be a valid file, throw exception
     * <li>
     * </ul>
     */
    private void precheckWrite(AudioFile af) throws CannotWriteException {
        Tag tag = af.getTag().or(NullTag.INSTANCE);
        if (tag == NullTag.INSTANCE) {
            throw new CannotWriteException("Null tag");
        }

        if (tag.isEmpty()) {
            delete(af);
            return;
        }

        File file = af.getFile();
        if (TagOptionSingleton.getInstance().isCheckIsWritable() && file.canWrite()) {
            LOG.log(ERROR, ErrorMessage.GENERAL_WRITE_FAILED, af.getFile());
            throw new CannotWriteException(ErrorMessage.GENERAL_WRITE_FAILED_TO_OPEN_FILE_FOR_EDITING, file);
        }

        if (file.length() <= MINIMUM_FILESIZE) {
            LOG.log(ERROR, ErrorMessage.GENERAL_WRITE_FAILED_BECAUSE_FILE_IS_TOO_SMALL, file);
            throw new CannotWriteException(ErrorMessage.GENERAL_WRITE_FAILED_BECAUSE_FILE_IS_TOO_SMALL, file);
        }
    }

    /**
     * Write the tag (if not empty) present in the AudioFile in the associated
     * File
     *
     * @param audioFile The file we want to process
     * @throws CannotWriteException if anything went wrong
     */
    // TODO Creates temp file in same folder as the original file, this is safe
    // but would impose a performance overhead if the original file is on a networked drive
    public void write(AudioFileImpl audioFile) throws CannotWriteException {
        Check.checkArgNotNull(audioFile, Check.CANNOT_BE_NULL, "audioFile");
        LOG.log(TRACE, "Started writing tag data for file %s", audioFile);

        // Prechecks

        precheckWrite(audioFile);

        //mp3's use a different mechanism to the other formats
        if (audioFile instanceof MP3File) {
            audioFile.save();
            return;
        }

        RandomAccessFile raf = null;
        RandomAccessFile rafTemp = null;
        File newFile;
        File result;

        // Create temporary File
        try {
            newFile = File.createTempFile(audioFile.getFile().getName().replace('.', '_'),
                    TEMP_FILENAME_SUFFIX,
                    audioFile.getFile().getParentFile());
        }
        // Unable to create temporary file, can happen in Vista if have Create
        // Files/Write Data set to Deny
        catch (IOException ioe) {
            if (ioe.getMessage().equals(FILE_NAME_TOO_LONG) &&
                    (audioFile.getFile().getName().length() > FILE_NAME_TOO_LONG_SAFE_LIMIT)) {
                try {

                    newFile = File.createTempFile(audioFile.getFile()
                                    .getName()
                                    .substring(0, FILE_NAME_TOO_LONG_SAFE_LIMIT)
                                    .replace('.', '_'),
                            TEMP_FILENAME_SUFFIX,
                            audioFile.getFile().getParentFile());

                } catch (IOException ioe2) {
                    LOG.log(ERROR, ioe2,
                            ErrorMessage.GENERAL_WRITE_FAILED_TO_CREATE_TEMPORARY_FILE_IN_FOLDER,
                            audioFile.getFile());
                    throw new CannotWriteException(ErrorMessage.GENERAL_WRITE_FAILED_TO_CREATE_TEMPORARY_FILE_IN_FOLDER,
                            audioFile.getFile());
                }
            } else {
                LOG.log(ERROR, ioe, ErrorMessage.GENERAL_WRITE_FAILED_TO_CREATE_TEMPORARY_FILE_IN_FOLDER,
                        audioFile.getFile());
                throw new CannotWriteException(ErrorMessage.GENERAL_WRITE_FAILED_TO_CREATE_TEMPORARY_FILE_IN_FOLDER,
                        audioFile.getFile());
            }
        }

        // Open temporary file and actual file for editing
        try {
            rafTemp = new RandomAccessFile(newFile, WRITE_MODE);
            raf = new RandomAccessFile(audioFile.getFile(), WRITE_MODE);

        }
        // Unable to write to writable file, can happen in Vista if have Create
        // Folders/Append Data set to Deny
        catch (IOException ioe) {
            LOG.log(ERROR, ioe, ErrorMessage.GENERAL_WRITE_FAILED_TO_OPEN_FILE_FOR_EDITING, audioFile.getFile());

            // If we managed to open either file, delete it.
            try {
                if (raf != null) {
                    raf.close();
                }
                if (rafTemp != null) {
                    rafTemp.close();
                }
            } catch (IOException ioe2) {
                // Warn but assume has worked okay
                LOG.log(WARN, ioe2, ErrorMessage.GENERAL_WRITE_PROBLEM_CLOSING_FILE_HANDLE, audioFile.getFile());
            }

            // Delete the temp file ( we cannot delete until closed corresponding
            // rafTemp)
            if (!newFile.delete()) {
                // Non critical failed deletion
                LOG.log(WARN, ErrorMessage.GENERAL_WRITE_FAILED_TO_DELETE_TEMPORARY_FILE, newFile);
            }

            throw new CannotWriteException(ErrorMessage.GENERAL_WRITE_FAILED_TO_OPEN_FILE_FOR_EDITING, audioFile.getFile());
        }

        // Write data to File
        try {

            raf.seek(0);
            rafTemp.seek(0);
            try {
                modificationListener.fileWillBeModified(audioFile, false);
                writeTag(audioFile, audioFile.getTagFieldContainer(), raf, rafTemp);
                modificationListener.fileModified(audioFile, newFile);
            } catch (ModifyVetoException veto) {
                throw new CannotWriteException(veto);
            }
        } catch (Exception e) {
            LOG.log(ERROR, e, ErrorMessage.GENERAL_WRITE_FAILED_BECAUSE, audioFile.getFile());

            try {
                if (raf != null) {
                    raf.close();
                }
                if (rafTemp != null) {
                    rafTemp.close();
                }
            } catch (IOException ioe) {
                // Warn but assume has worked okay
                LOG.log(WARN, ioe, ErrorMessage.GENERAL_WRITE_PROBLEM_CLOSING_FILE_HANDLE, audioFile.getFile());
            }

            // Delete the temporary file because either it was never used so
            // lets just tidy up or we did start writing to it but
            // the write failed and we havent renamed it back to the original
            // file so we can just delete it.
            if (!newFile.delete()) {
                // Non critical failed deletion
                LOG.log(WARN, ErrorMessage.GENERAL_WRITE_FAILED_TO_DELETE_TEMPORARY_FILE, newFile);
            }
            throw new CannotWriteException(ErrorMessage.GENERAL_WRITE_FAILED_BECAUSE, audioFile.getFile(), e);
        } finally {
            try {
                if (raf != null) {
                    raf.close();
                }
                if (rafTemp != null) {
                    rafTemp.close();
                }
            } catch (IOException ioe) {
                // Warn but assume has worked okay
                LOG.log(WARN, ioe, ErrorMessage.GENERAL_WRITE_PROBLEM_CLOSING_FILE_HANDLE, audioFile.getFile());
            }
        }

        // Result held in this file
        result = audioFile.getFile();

        // If the temporary file was used
        if (newFile.length() > 0) {
            transferNewFileToOriginalFile(newFile,
                    audioFile.getFile(),
                    TagOptionSingleton.getInstance().isPreserveFileIdentity());
        } else {
            // Delete the temporary file that wasn't ever used
            if (!newFile.delete()) {
                // Non critical failed deletion
                LOG.log(WARN, ErrorMessage.GENERAL_WRITE_FAILED_TO_DELETE_TEMPORARY_FILE, newFile);
            }
        }

        modificationListener.fileOperationFinished(result);
    }

    /**
     * <p>
     * Transfers the content from {@code newFile} to a file named {@code originalFile}.
     * With regards to file identity
     * (inode/<a href="https://msdn.microsoft.com/en-us/library/aa363788(v=vs.85).aspx">fileIndex</a>),
     * after execution, {@code originalFile} may be a completely new file or the same file as before execution,
     * depending
     * on {@code reuseExistingOriginalFile}.
     * </p>
     * <p>
     * Reusing the existing file may be slower, if both the temp file and the original file are located
     * in the same filesystem, because an actual copy is created instead of just a file rename.
     * If both files are on different filesystems, a copy is always needed — regardless of which method is used.
     * </p>
     *
     * @param newFile                   new file
     * @param originalFile              original file
     * @param reuseExistingOriginalFile {@code true} or {@code false}
     * @throws CannotWriteException If the file cannot be written
     */
    private void transferNewFileToOriginalFile(File newFile,
                                               File originalFile,
                                               boolean reuseExistingOriginalFile) throws CannotWriteException {
        if (reuseExistingOriginalFile) {
            transferNewFileContentToOriginalFile(newFile, originalFile);
        } else {
            transferNewFileToNewOriginalFile(newFile, originalFile);
        }
    }

    /**
     * <p>
     * Writes the contents of the given {@code newFile} to the given {@code originalFile},
     * overwriting the already existing content in {@code originalFile}.
     * This ensures that the file denoted by the abstract pathname {@code originalFile}
     * keeps the same Unix inode or Windows
     * <a href="https://msdn.microsoft.com/en-us/library/aa363788(v=vs.85).aspx">fileIndex</a>.
     * </p>
     * <p>
     * If no errors occur, the method follows this approach:
     * </p>
     * <ol>
     * <li>Rename <code>originalFile</code> to <code>originalFile.old</code></li>
     * <li>Rename <code>newFile</code> to <code>originalFile</code> (this implies a file identity change for
     * <code>originalFile</code>)</li>
     * <li>Delete <code>originalFile.old</code></li>
     * <li>Delete <code>newFile</code></li>
     * </ol>
     *
     * @param newFile      File containing the data we want in the {@code originalFile}
     * @param originalFile Before execution this denotes the original, unmodified file. After execution it denotes the name of the file with
     *                     the modified content and new inode/fileIndex.
     * @throws CannotWriteException if the file cannot be written
     */
    private void transferNewFileContentToOriginalFile(File newFile, File originalFile)
            throws CannotWriteException {
        // try to obtain exclusive lock on the file
        try (RandomAccessFile raf = new RandomAccessFile(originalFile, "rw")) {
            FileChannel outChannel = raf.getChannel();
            try (FileLock lock = outChannel.tryLock()) {
                if (lock != null) {
                    transferNewFileContentToOriginalFile(newFile, originalFile, raf, outChannel);
                } else {
                    // we didn't get a lock
                    LOG.log(WARN, ErrorMessage.GENERAL_WRITE_FAILED_FILE_LOCKED, originalFile);
                    throw new CannotWriteException(ErrorMessage.GENERAL_WRITE_FAILED_FILE_LOCKED, originalFile);
                }
            } catch (IOException e) {
                LOG.log(WARN, ErrorMessage.GENERAL_WRITE_FAILED_FILE_LOCKED, originalFile);
                // we didn't get a lock, this may be, because locking is not supported by the OS/JRE
                // this can happen on OS X with network shares (samba, afp)
                // for details see https://stackoverflow.com/questions/33220148/samba-share-gradle-java-io-exception
                // coarse check that works on OS X:
                if ("Operation not supported".equals(e.getMessage())) {
                    // transfer without lock
                    transferNewFileContentToOriginalFile(newFile, originalFile, raf, outChannel);
                } else {
                    throw new CannotWriteException(e, ErrorMessage.GENERAL_WRITE_FAILED_FILE_LOCKED, originalFile);
                }
            } catch (Exception e) {
                // tryLock failed for some reason other than an IOException — we're definitely doomed
                LOG.log(WARN, ErrorMessage.GENERAL_WRITE_FAILED_FILE_LOCKED, originalFile);
                throw new CannotWriteException(e, ErrorMessage.GENERAL_WRITE_FAILED_FILE_LOCKED, originalFile);
            }
        } catch (FileNotFoundException e) {
            LOG.log(WARN, ErrorMessage.GENERAL_WRITE_FAILED_BECAUSE_FILE_NOT_FOUND, originalFile);
            throw new CannotWriteException(e, ErrorMessage.GENERAL_WRITE_FAILED_BECAUSE_FILE_NOT_FOUND, originalFile);
        } catch (Exception e) {
            LOG.log(WARN, ErrorMessage.GENERAL_WRITE_FAILED, originalFile);
            throw new CannotWriteException(e, ErrorMessage.GENERAL_WRITE_FAILED, originalFile);
        }
    }

    private void transferNewFileContentToOriginalFile(File newFile,
                                                      File originalFile,
                                                      RandomAccessFile raf,
                                                      FileChannel outChannel) throws CannotWriteException {
        try (FileChannel inChannel = new FileInputStream(newFile).getChannel()) {
            // copy contents of newFile to originalFile,
            // overwriting the old content in that file
            long size = inChannel.size();
            long position = 0;
            while (position < size) {
                position += inChannel.transferTo(position, 1024L * 1024L, outChannel);
            }
            // truncate raf, in case it used to be longer
            raf.setLength(size);
        } catch (FileNotFoundException e) {
            LOG.log(WARN, ErrorMessage.GENERAL_WRITE_FAILED_NEW_FILE_DOESNT_EXIST, newFile);
            throw new CannotWriteException(e, ErrorMessage.GENERAL_WRITE_FAILED_NEW_FILE_DOESNT_EXIST, newFile);
        } catch (IOException e) {
            LOG.log(WARN, ErrorMessage.GENERAL_WRITE_FAILED_TO_RENAME_TO_ORIGINAL_FILE, originalFile, newFile);
            throw new CannotWriteException(e, ErrorMessage.GENERAL_WRITE_FAILED_TO_RENAME_TO_ORIGINAL_FILE, originalFile, newFile);
        }
        // file is written, all is good, let's delete newFile, as it's not needed anymore
        if (newFile.exists() && !newFile.delete()) {
            // non-critical failed deletion
            LOG.log(WARN, ErrorMessage.GENERAL_WRITE_FAILED_TO_DELETE_TEMPORARY_FILE, newFile);
        }
    }

    /**
     * <p>
     * Replaces the original file with the new file in a way that changes the file identity.
     * In other words, the Unix inode or the Windows
     * <a href="https://msdn.microsoft.com/en-us/library/aa363788(v=vs.85).aspx">fileIndex</a>
     * of the resulting file with the name {@code originalFile} is not identical to the inode/fileIndex
     * of the file named {@code originalFile} before this method was called.
     * </p>
     * <p>
     * If no errors occur, the method follows this approach:
     * </p>
     * <ol>
     * <li>Rename <code>originalFile</code> to <code>originalFile.old</code></li>
     * <li>Rename <code>newFile</code> to <code>originalFile</code> (this implies a file identity change for
     * <code>originalFile</code>)</li>
     * <li>Delete <code>originalFile.old</code></li>
     * <li>Delete <code>newFile</code></li>
     * </ol>
     *
     * @param newFile      File containing the data we want in the {@code originalFile}
     * @param originalFile Before execution this denotes the original, unmodified file. After execution it denotes the name of the file with
     *                     the modified content and new inode/fileIndex.
     * @throws CannotWriteException if the file cannot be written
     */
    private void transferNewFileToNewOriginalFile(File newFile, File originalFile)
            throws CannotWriteException {
        // ==Android==
        // get original creation date
//        final FileTime creationTime = getCreationTime(originalFile);

        // Rename Original File
        // Can fail on Vista if have Special Permission 'Delete' set Deny
        File originalFileBackup = new File(originalFile.getAbsoluteFile().getParentFile().getPath(),
                Files.getNameWithoutExtension(originalFile.getPath()) + ".old");

        //If already exists modify the suffix
        int count = 1;
        while (originalFileBackup.exists()) {
            originalFileBackup = new File(originalFile.getAbsoluteFile().getParentFile().getPath(),
                    Files.getNameWithoutExtension(originalFile.getPath()) + ".old" + count);
            count++;
        }

        boolean renameResult = Utils.rename(originalFile, originalFileBackup);
        if (!renameResult) {
            LOG.log(ERROR, ErrorMessage.GENERAL_WRITE_FAILED_TO_RENAME_ORIGINAL_FILE_TO_BACKUP, originalFile, originalFileBackup);
            //Delete the temp file because write has failed
            // TODO: Simplify: newFile is always != null, otherwise we would not have entered this block (-> if
            // (newFile.length() > 0) %s)
            if (newFile != null) {
                newFile.delete();
            }
            throw new CannotWriteException(ErrorMessage.GENERAL_WRITE_FAILED_TO_RENAME_ORIGINAL_FILE_TO_BACKUP, originalFile, originalFileBackup);
        }

        // Rename Temp File to Original File
        renameResult = Utils.rename(newFile, originalFile);
        if (!renameResult) {
            // Renamed failed so lets do some checks rename the backup back to the original file
            // New File doesnt exist
            if (!newFile.exists()) {
                LOG.log(WARN, ErrorMessage.GENERAL_WRITE_FAILED_NEW_FILE_DOESNT_EXIST, newFile);
            }

            // Rename the backup back to the original
            if (!originalFileBackup.renameTo(originalFile)) {
                // TODO now if this happens we are left with testfile.old
                // instead of testfile.mp4
                LOG.log(WARN, ErrorMessage.GENERAL_WRITE_FAILED_TO_RENAME_ORIGINAL_BACKUP_TO_ORIGINAL, originalFileBackup, originalFile);
            }

            LOG.log(WARN, ErrorMessage.GENERAL_WRITE_FAILED_TO_RENAME_TO_ORIGINAL_FILE, originalFile, newFile);
            throw new CannotWriteException(ErrorMessage.GENERAL_WRITE_FAILED_TO_RENAME_TO_ORIGINAL_FILE, originalFile, newFile);
        } else {
            // Rename was okay so we can now delete the backup of the
            // original
            boolean deleteResult = originalFileBackup.delete();
            if (!deleteResult) {
                // Not a disaster but can't delete the backup so make a
                // warning
                LOG.log(WARN, ErrorMessage.GENERAL_WRITE_WARNING_UNABLE_TO_DELETE_BACKUP_FILE, originalFileBackup);
            }
        }

        // Delete the temporary file if still exists
        if (newFile.exists()) {
            if (!newFile.delete()) {
                // Non critical failed deletion
                LOG.log(WARN, ErrorMessage.GENERAL_WRITE_FAILED_TO_DELETE_TEMPORARY_FILE, newFile);
            }
        }
    }


    /**
     * This is called when a tag has to be written in a file. Three parameters
     * are provided, the tag to write (not empty) Two randomaccessfiles, the
     * first points to the file where we want to write the given tag, and the
     * second is an empty temporary file that can be used if e.g. the file has
     * to be bigger than the original.
     * <p>
     * If something has been written in the temporary file, when this method
     * returns, the original file is deleted, and the temporary file is renamed
     * the the original name
     * <p>
     * If nothing has been written to it, it is simply deleted.
     * <p>
     * This method can assume the raf, rafTemp are pointing to the first byte of
     * the file. The subclass must not close these two files when the method
     * returns.
     *
     * @throws IOException          is thrown when the RandomAccessFile operations throw it (you should never throw them manually)
     * @throws CannotWriteException when an error occured during the generation of the tag
     */
    protected abstract void writeTag(AudioFile audioFile, TagFieldContainer tag, RandomAccessFile raf, RandomAccessFile rafTemp)
            throws CannotReadException, CannotWriteException, IOException;
}
