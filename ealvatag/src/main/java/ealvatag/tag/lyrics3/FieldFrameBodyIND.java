/**
 * @author : Paul Taylor
 * @author : Eric Farng
 * <p>
 * Version @version:$Id$
 * <p>
 * MusicTag Copyright (C)2003,2004
 * <p>
 * This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public  License as published by the Free Software Foundation; either version 2.1 of the License,
 * or (at your option) any later version.
 * <p>
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public License along with this library; if not,
 * you can get a copy from http://www.opensource.org/licenses/lgpl-license.php or write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 * <p>
 * Description:
 */
package ealvatag.tag.lyrics3;

import java.nio.ByteBuffer;

import ealvatag.tag.InvalidTagException;
import ealvatag.tag.datatype.BooleanString;


public class FieldFrameBodyIND extends AbstractLyrics3v2FieldFrameBody {
    /**
     * Creates a new FieldBodyIND datatype.
     */
    public FieldFrameBodyIND() {
        //        this.setObject("Lyrics Present", new Boolean(false));
        //        this.setObject("Timestamp Present", new Boolean(false));
    }

    public FieldFrameBodyIND(FieldFrameBodyIND body) {
        super(body);
    }

    /**
     * Creates a new FieldBodyIND datatype.
     *
     * @param lyricsPresent
     * @param timeStampPresent
     */
    public FieldFrameBodyIND(boolean lyricsPresent, boolean timeStampPresent) {
        setObjectValue("Lyrics Present", lyricsPresent);
        setObjectValue("Timestamp Present", timeStampPresent);
    }

    /**
     * Creates a new FieldBodyIND datatype.
     *
     * @param byteBuffer
     * @throws InvalidTagException
     */
    public FieldFrameBodyIND(ByteBuffer byteBuffer) throws InvalidTagException {
        read(byteBuffer);
    }

    /**
     * @return
     */
    public String getAuthor() {
        return (String) getObjectValue("Author");
    }

    /**
     * @param author
     */
    public void setAuthor(String author) {
        setObjectValue("Author", author);
    }

    /**
     * @return
     */
    public String getIdentifier() {
        return "IND";
    }

    /**
     *
     */
    protected void setupObjectList() {
        addDataType(new BooleanString("Lyrics Present", this));
        addDataType(new BooleanString("Timestamp Present", this));
    }
}
