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

import com.google.common.base.Optional;

import java.io.File;

import ealvatag.audio.exceptions.CannotWriteException;
import ealvatag.tag.Tag;
import ealvatag.tag.TagOptionSingleton;
import ealvatag.tag.reference.ID3V2Version;

/**
 * Interface to an AudioFile, which is the primary entry point to reading/editing fields in a {@link Tag} and getting audio information
 * from {@link AudioHeader}
 * <p>
 * Created by Eric A. Snell on 1/21/17.
 */
public interface AudioFile {

    /**
     * Indicates if the audio file was opened read only. Opening read only allows for some optimizations. If some tags are ignored during
     * reading, the AudioFile is automatically marked read-only. Just this instance, not any underlying file or storage.
     *
     * @return true if file was opened only to read tags
     */
    boolean readOnly();

    /**
     * Write the tag contained in this AudioFile in the actual file on the disk
     *
     * @throws CannotWriteException If the file could not be written/accessed, the extension wasn't recognized, or other IO error occurred.
     */
    void save() throws CannotWriteException;

    void saveAs(String fullPathWithoutExtension) throws IllegalArgumentException, CannotWriteException;

    /**
     * Delete any {@link Tag} in the underlying file
     *
     * @throws CannotWriteException If the file could not be written/accessed, the extension wasn't recognized, or other IO error occurred.
     */
    void deleteFileTag() throws CannotWriteException;

    /**
     * The {@link File} instance this AudioFile represents
     *
     * @return the underlying {@link File}
     */
    File getFile();

    /**
     * Return audio header information
     *
     * @return the {@link AudioHeader} that describes this AudioFile
     */
    AudioHeader getAudioHeader();

    /**
     * Returns the tag contained in this AudioFile. The {@link Tag} contains any useful metadata, like
     * artist, album, title, etc. If the file does not contain any tag then the return will be absent. Some audio formats do
     * not allow there to be no tag so in this case the reader would return an empty tag whereas for others such
     * as mp3 it is purely optional.
     *
     * @return Returns the optional tag contained in this AudioFile, ie may be absent
     */
    Optional<Tag> getTag();

    /**
     * Discards the current {@link Tag}, makes and sets a new {@link Tag} of the default type, and returns the new {@link Tag}
     *
     * @return the newly created default type {@link Tag}. {@link #getTag()} will now return this same instance
     * @throws UnsupportedFileType if the current file type is unsupported/unrecognized
     */
    Tag setNewDefaultTag() throws UnsupportedFileType;

    /**
     * Get the current tag if it exists. Otherwise, set a new default {@link Tag}
     *
     * @return the existing tag or a new default tag for the given file type
     * @throws UnsupportedFileType if the file type is unrecognized
     * @see #getTag()
     * @see #setNewDefaultTag()
     */
    Tag getTagOrSetNewDefault() throws UnsupportedFileType, CannotWriteException;

    /**
     * If a current {@link Tag} exists, convert to the default type and return it. If none exists, set a new default type {@link Tag}
     * <p>
     * Conversions are currently only necessary/available for formats that support ID3
     *
     * @return the existing tag converted to the default type, or a new default tag for the given file type
     * @throws UnsupportedFileType if the file type is unrecognized
     * @see TagOptionSingleton#setID3V2Version(ID3V2Version)
     * @see #getTag()
     * @see #setNewDefaultTag()
     */
    Tag getConvertedTagOrSetNewDefault() throws CannotWriteException;
}
