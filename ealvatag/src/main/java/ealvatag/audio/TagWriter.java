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

import java.io.IOException;
import java.io.RandomAccessFile;

import ealvatag.audio.exceptions.CannotWriteException;
import ealvatag.tag.Tag;

/**
 * Original TagWriter
 * <p>
 * Created by Paul on 15/09/2015.
 */
public interface TagWriter {
    void delete(Tag tag, RandomAccessFile raf, RandomAccessFile tempRaf) throws IOException, CannotWriteException;


    /**
     * Write tag to file
     */
    void write(AudioFile af, Tag tag, RandomAccessFile raf, RandomAccessFile rafTemp) throws CannotWriteException, IOException;
}
