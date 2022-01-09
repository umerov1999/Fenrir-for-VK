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

package ealvatag.tag.id3.framebody;

import ealvatag.tag.InvalidFrameException;
import ealvatag.tag.InvalidTagException;
import okio.Buffer;

/**
 * Interface for creating {@link AbstractID3v2FrameBody} instances.
 * <p>
 * Created by Eric A. Snell on 1/25/17.
 */
public interface Id3FrameBodyFactory {

    /**
     * Make a Id3v3 frame based on the given {@code frameId}
     *
     * @param frameId   the frame id parsed from the tag
     * @param buffer    source of frame contents
     * @param frameSize the size of the frame
     * @return an Id3v2 frame instance of the type specified by {@code frameId}
     * @throws FrameIdentifierException if the frameId cannot be found
     * @throws InvalidFrameException    if the frame data could not be parsed
     */
    AbstractID3v2FrameBody make(String frameId, Buffer buffer, int frameSize) throws FrameIdentifierException, InvalidTagException;

}
