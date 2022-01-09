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
import ealvatag.tag.NullTag;
import ealvatag.tag.Tag;

/**
 * No-op implementation of the {@link AudioFile} interface.
 * <p>
 * Created by Eric A. Snell on 2/2/17.
 */
public final class NullAudioFile implements AudioFile {
    public static final AudioFile INSTANCE = new NullAudioFile();

    private static final File DUMMY_FILE = new File("/NO/SUCH/FILE/NullAudioFile");

    private NullAudioFile() {
    }

    @Override
    public boolean readOnly() {
        return true;
    }

    @Override
    public void save() throws CannotWriteException {
    }

    @Override
    public void saveAs(String fullPathWithoutExtension) throws IllegalArgumentException, CannotWriteException {
    }

    @Override
    public void deleteFileTag() throws CannotWriteException {
    }

    @Override
    public File getFile() {
        return DUMMY_FILE;
    }

    @Override
    public AudioHeader getAudioHeader() {
        return NullAudioHeader.INSTANCE;
    }

    @Override
    public Optional<Tag> getTag() {
        return Optional.absent();
    }

    @Override
    public Tag setNewDefaultTag() throws UnsupportedFileType {
        throw new UnsupportedFileType(DUMMY_FILE.getPath());
    }

    @Override
    public Tag getTagOrSetNewDefault() throws UnsupportedFileType {
        throw new UnsupportedFileType(DUMMY_FILE.getPath());
    }

    @Override
    public Tag getConvertedTagOrSetNewDefault() {
        return NullTag.INSTANCE;
    }
}
