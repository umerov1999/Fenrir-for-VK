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

import java.nio.ByteBuffer;

import ealvatag.tag.InvalidTagException;
import ealvatag.tag.datatype.DataTypes;
import ealvatag.utils.StandardCharsets;
import okio.Buffer;

/**
 * Base class for PIC and APIC frame body.
 * <p>
 * Created by Eric A. Snell on 2/3/17.
 */
public abstract class AbstractArtworkFrameBody extends AbstractID3v2FrameBody {
    AbstractArtworkFrameBody() {
    }

    AbstractArtworkFrameBody(ByteBuffer byteBuffer, int frameSize) throws InvalidTagException {
        super(byteBuffer,
                frameSize);
    }

    AbstractArtworkFrameBody(Buffer buffer, int frameSize) throws InvalidTagException {
        super(buffer, frameSize);
    }

    AbstractArtworkFrameBody(AbstractID3v2FrameBody copyObject) {
        super(copyObject);
    }

    public abstract byte[] getImageData();

    public int getPictureType() {
        return ((Long) getObjectValue(DataTypes.OBJ_PICTURE_TYPE)).intValue();
    }

    public abstract boolean isImageUrl();

    public abstract String getMimeType();

    /**
     * @return the image url if there is otherwise return an empty String
     */
    public String getImageUrl() {
        if (isImageUrl()) {
            return new String(((byte[]) getObjectValue(DataTypes.OBJ_PICTURE_DATA)),
                    0,
                    ((byte[]) getObjectValue(DataTypes.OBJ_PICTURE_DATA)).length,
                    StandardCharsets.ISO_8859_1);
        } else {
            return "";
        }
    }
}
