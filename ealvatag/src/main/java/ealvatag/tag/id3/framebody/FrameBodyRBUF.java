/**
 * @author : Paul Taylor
 * @author : Eric Farng
 * <p>
 * Version @version:$Id$
 * <p>
 * MusicTag Copyright (C)2003,2004
 * <p>
 * This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public  License as
 * published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version.
 * <p>
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public License along with this library; if not, you can get a copy from
 * http://www.opensource.org/licenses/lgpl-license.php or write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
 * Boston, MA 02110-1301 USA
 * <p>
 * Description:
 */
package ealvatag.tag.id3.framebody;

import java.nio.ByteBuffer;

import ealvatag.tag.InvalidTagException;
import ealvatag.tag.datatype.BooleanByte;
import ealvatag.tag.datatype.DataTypes;
import ealvatag.tag.datatype.NumberFixedLength;
import ealvatag.tag.id3.ID3v24Frames;
import okio.Buffer;


/**
 * Body of Recommended buffer size frame, generally used for streaming audio
 */
public class FrameBodyRBUF extends AbstractID3v2FrameBody implements ID3v24FrameBody, ID3v23FrameBody {
    private static final int BUFFER_FIELD_SIZE = 3;
    private static final int EMBED_FLAG_BIT_POSITION = 1;
    private static final int OFFSET_FIELD_SIZE = 4;

    /**
     * Creates a new FrameBodyRBUF datatype.
     */
    public FrameBodyRBUF() {
        setObjectValue(DataTypes.OBJ_BUFFER_SIZE, (byte) 0);
        setObjectValue(DataTypes.OBJ_EMBED_FLAG, Boolean.FALSE);
        setObjectValue(DataTypes.OBJ_OFFSET, (byte) 0);
    }

    public FrameBodyRBUF(FrameBodyRBUF body) {
        super(body);
    }

    /**
     * Creates a new FrameBodyRBUF datatype.
     *
     * @param bufferSize
     * @param embeddedInfoFlag
     * @param offsetToNextTag
     */
    public FrameBodyRBUF(byte bufferSize, boolean embeddedInfoFlag, byte offsetToNextTag) {
        setObjectValue(DataTypes.OBJ_BUFFER_SIZE, bufferSize);
        setObjectValue(DataTypes.OBJ_EMBED_FLAG, embeddedInfoFlag);
        setObjectValue(DataTypes.OBJ_OFFSET, offsetToNextTag);
    }

    /**
     * Creates a new FrameBodyRBUF datatype.
     *
     * @param byteBuffer
     * @param frameSize
     * @throws InvalidTagException if unable to create framebody from buffer
     */
    public FrameBodyRBUF(ByteBuffer byteBuffer, int frameSize) throws InvalidTagException {
        super(byteBuffer, frameSize);
    }

    public FrameBodyRBUF(Buffer byteBuffer, int frameSize) throws InvalidTagException {
        super(byteBuffer, frameSize);
    }

    /**
     * The ID3v2 frame identifier
     *
     * @return the ID3v2 frame identifier  for this frame type
     */
    public String getIdentifier() {
        return ID3v24Frames.FRAME_ID_RECOMMENDED_BUFFER_SIZE;
    }

    /**
     *
     */
    protected void setupObjectList() {
        addDataType(new NumberFixedLength(DataTypes.OBJ_BUFFER_SIZE, this, BUFFER_FIELD_SIZE));
        addDataType(new BooleanByte(DataTypes.OBJ_EMBED_FLAG, this, (byte) EMBED_FLAG_BIT_POSITION));
        addDataType(new NumberFixedLength(DataTypes.OBJ_OFFSET, this, OFFSET_FIELD_SIZE));
    }
}
