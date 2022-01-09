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

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

import ealvatag.tag.TagFieldContainer;

/**
 * Abstract class for creating the raw content that represents the tag so it can be written
 * to file.
 */
public abstract class AbstractTagCreator {
    /**
     * Convert tagdata to rawdata ready for writing to file with no additional padding
     *
     * @param tag
     * @return
     * @throws UnsupportedEncodingException
     */
    public ByteBuffer convert(TagFieldContainer tag) throws UnsupportedEncodingException {
        return convert(tag, 0);
    }

    /**
     * Convert tagdata to rawdata ready for writing to file
     *
     * @param tag
     * @param padding TODO is this padding or additional padding
     * @return
     * @throws UnsupportedEncodingException
     */
    public abstract ByteBuffer convert(TagFieldContainer tag, int padding) throws UnsupportedEncodingException;
}
