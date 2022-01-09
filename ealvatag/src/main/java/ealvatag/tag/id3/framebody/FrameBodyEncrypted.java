/*
 *  Jthink Copyright (C)2010
 *
 *  This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser
 *  General Public  License as published by the Free Software Foundation; either version 2.1 of the License,
 *  or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 *  the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License along with this library; if not,
 *  you can get a copy from http://www.opensource.org/licenses/lgpl-license.php or write to the Free Software
 *  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 */
package ealvatag.tag.id3.framebody;

import java.nio.ByteBuffer;

import ealvatag.tag.InvalidTagException;
import ealvatag.tag.datatype.ByteArraySizeTerminated;
import ealvatag.tag.datatype.DataTypes;
import okio.Buffer;

/**
 * Encrypted frame.
 * <p>
 * <p>
 * Container for an encrypted frame, we cannot decrypt encrypted frame but it may be possible
 * for the calling application to decrypt the frame if they understand how it has been encrypted,
 * information on this will be held within an ENCR frame
 *
 * @author : Paul Taylor
 */
public class FrameBodyEncrypted extends AbstractID3v2FrameBody implements ID3v24FrameBody, ID3v23FrameBody {
    private String identifier;

    /**
     * Creates a new FrameBodyEncrypted dataType.
     */
    public FrameBodyEncrypted(String identifier) {
        this.identifier = identifier;
    }

    public FrameBodyEncrypted(FrameBodyEncrypted body) {
        super(body);
    }

    /**
     * Read from file
     *
     * @param identifier
     * @param byteBuffer
     * @param frameSize
     * @throws InvalidTagException
     */
    public FrameBodyEncrypted(String identifier, ByteBuffer byteBuffer, int frameSize) throws InvalidTagException {
        super(byteBuffer, frameSize);
        this.identifier = identifier;
    }

    public FrameBodyEncrypted(String identifier, Buffer byteBuffer, int frameSize) throws InvalidTagException {
        super(byteBuffer, frameSize);
        this.identifier = identifier;
    }


    /**
     * The ID3v2 frame identifier
     *
     * @return the ID3v2 frame identifier  for this frame type
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * TODO:proper mapping
     */
    protected void setupObjectList() {
        addDataType(new ByteArraySizeTerminated(DataTypes.OBJ_DATA, this));
    }
}
