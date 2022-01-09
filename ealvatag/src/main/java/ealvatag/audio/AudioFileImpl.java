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

import static ealvatag.utils.Check.checkArgNotNull;
import static ealvatag.utils.Check.checkArgNotNullOrEmpty;

import com.google.common.base.Optional;
import com.google.common.base.Supplier;
import com.google.common.io.Files;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

import ealvatag.audio.exceptions.CannotWriteException;
import ealvatag.tag.Tag;
import ealvatag.tag.TagFieldContainer;
import ealvatag.tag.TagOptionSingleton;
import ealvatag.tag.id3.AbstractID3v2Tag;
import ealvatag.tag.id3.ID3v22Tag;
import ealvatag.tag.id3.ID3v23Tag;
import ealvatag.tag.id3.ID3v24Tag;
import ealvatag.tag.reference.ID3V2Version;
import ealvatag.utils.Check;

/**
 * <p>This is the main object manipulated by the user representing an audiofile, its properties and its tag.
 * <p>The preferred way to obtain an <code>AudioFile</code> is to use the <code>AudioFileIO.read(File)</code> method.
 * <p>The <code>AudioHeader</code> contains every properties associated with the file itself (no meta-data), like the
 * bitrate, the sampling rate, the encoding audioHeaders, etc.
 * <p>To get the meta-data contained in this file you have to get the <code>Tag</code> of this <code>AudioFile</code>
 *
 * @author Raphael Slinckx
 * @version $Id$
 * @see AudioFileIO
 * @see Tag
 * @since v0.01
 */
public class AudioFileImpl implements AudioFile {
    protected File file;
    protected AudioHeader audioHeader;
    protected TagFieldContainer tag;
    protected String extension;         // we parsed it once to find the reader, so let's store it and not keep parsing

    /**
     * These constructors are used by the different readers, users should not use them.
     * <p>
     * Create the AudioFile representing file f, the encoding audio headers and containing the tag
     *
     * @param file        The file of the audio file
     * @param extension   the file extension (was used to selected the Reader, so we have already parsed it once)
     * @param audioHeader the encoding audioHeaders over this file
     * @param tag         the tag contained in this file or null if no tag exists
     */
    public AudioFileImpl(File file, String extension, AudioHeader audioHeader, TagFieldContainer tag) {
        checkArgNotNull(file);
        checkArgNotNullOrEmpty(extension);
        checkArgNotNull(audioHeader);
        this.file = file;
        this.extension = extension;
        this.audioHeader = audioHeader;
        this.tag = tag;
    }

    protected AudioFileImpl(File file, String extension) throws FileNotFoundException {
        checkArgNotNull(file);
        checkArgNotNullOrEmpty(extension);
        this.file = file;
        this.extension = extension;
    }

    @Override
    public boolean readOnly() {
        return tag.isReadOnly();
    }

    @Override
    public void save() throws CannotWriteException {
        checkReadOnly();
        AudioFileIO.instance().writeFile(this);
    }

    private void checkReadOnly() throws CannotWriteException {
        if (tag != null && tag.isReadOnly()) {
            throw new CannotWriteException("Opened read only");
        }
    }

    @Override
    public void saveAs(String fullPathWithoutExtension) throws IllegalArgumentException, CannotWriteException {
        checkReadOnly();
        checkArgNotNullOrEmpty(fullPathWithoutExtension, Check.CANNOT_BE_NULL_OR_EMPTY, "fullPathWithoutExtension");
        AudioFileIO.instance().writeFileAs(this, fullPathWithoutExtension);
    }

    @Override
    public void deleteFileTag() throws CannotWriteException {
        checkReadOnly();
        AudioFileIO.instance().deleteTag(this);
    }

    @Override
    public File getFile() {
        return file;
    }

    void setFile(File file) {
        this.file = file;
    }

    String getExt() {
        return extension;
    }

    @Override
    public AudioHeader getAudioHeader() {
        return audioHeader;
    }

    @Override
    public Optional<Tag> getTag() {
        return Optional.fromNullable((Tag) tag);
    }

    @Override
    public Tag setNewDefaultTag() throws UnsupportedFileType {
        return setTag(makeDefaultTag());
    }

    protected Tag setTag(Tag tag) {
        this.tag = (TagFieldContainer) tag;
        return this.tag;
    }

    protected Tag makeDefaultTag() throws UnsupportedFileType {
        return SupportedFileFormat.fromExtension(Files.getFileExtension(file.getName())).makeDefaultTag();
    }

    @Override
    public Tag getTagOrSetNewDefault() throws UnsupportedFileType, CannotWriteException {
        return getTag().or(makeTagSupplier());
    }

    private Supplier<Tag> makeTagSupplier() throws CannotWriteException {
        return new Supplier<Tag>() {
            @Override
            public Tag get() {
                return setTag(makeDefaultTag());
            }
        };
    }

    @Override
    public Tag getConvertedTagOrSetNewDefault() throws CannotWriteException {
        /* TODO Currently only works for Dsf We need additional check here for Wav and Aif because they wrap the ID3
        tag so never return
         * null for getTag() and the wrapper stores the location of the existing tag, would that be broken if tag set
          * to something else
         * // TODO: 1/7/17 this comment may be outdated
         */
        Tag tag = getTagOrSetNewDefault();

        if (tag instanceof AbstractID3v2Tag) {
            return setTag(convertID3Tag((AbstractID3v2Tag) tag, TagOptionSingleton.getInstance().getID3V2Version()));
        } else {
            return setTag(tag);
        }
    }

    /**
     * If using ID3 format convert tag from current version to another as specified by id3V2Version,
     *
     * @return the converted tag or the original if no conversion necessary
     */
    protected AbstractID3v2Tag convertID3Tag(AbstractID3v2Tag tag, ID3V2Version id3V2Version) {
        if (tag instanceof ID3v24Tag) {
            switch (id3V2Version) {
                case ID3_V22:
                    return new ID3v22Tag(tag);
                case ID3_V23:
                    return new ID3v23Tag(tag);
                case ID3_V24:
                    return tag;
            }
        } else if (tag instanceof ID3v23Tag) {
            switch (id3V2Version) {
                case ID3_V22:
                    return new ID3v22Tag(tag);
                case ID3_V23:
                    return tag;
                case ID3_V24:
                    return new ID3v24Tag(tag);
            }
        } else if (tag instanceof ID3v22Tag) {
            switch (id3V2Version) {
                case ID3_V22:
                    return tag;
                case ID3_V23:
                    return new ID3v23Tag(tag);
                case ID3_V24:
                    return new ID3v24Tag(tag);
            }
        }
        return null;
    }

    TagFieldContainer getTagFieldContainer() {
        return tag;
    }


    protected FileChannel getReadFileChannel(File file) throws FileNotFoundException {
        FileChannel channel = new RandomAccessFile(file, "r").getChannel();
        try {
            if (channel.size() == 0) {
                throw new FileNotFoundException("Not found or 0 size " + file.getPath());
            }
            return channel;
        } catch (IOException e) {
            throw new FileNotFoundException(file.getPath() + " " + e.getMessage());
        }
    }

    /**
     * Optional debugging method. Must override to do anything interesting.
     *
     * @return Empty string.
     */
    public String displayStructureAsXML() {
        return "";
    }

    /**
     * Optional debugging method. Must override to do anything interesting.
     */
    @SuppressWarnings("unused")
    public String displayStructureAsPlainText() {
        return "";
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("AudioFileImpl{");
        sb.append("file=").append(file);
        sb.append(", audioHeader=").append(audioHeader);
        sb.append(", tag=").append(tag);
        sb.append(", extension='").append(extension).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
