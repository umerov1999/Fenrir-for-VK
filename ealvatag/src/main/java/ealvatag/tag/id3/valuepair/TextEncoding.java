/**
 * @author : Paul Taylor
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
 * Description: Valid Text Encodings
 */
package ealvatag.tag.id3.valuepair;

import java.nio.charset.Charset;
import java.util.NoSuchElementException;

import ealvatag.utils.StandardCharsets;


/**
 * Text Encoding supported by ID3v24, the id is recognised by ID3
 * whereas the value maps to a java java.nio.charset.Charset, all the
 * charsets defined below are guaranteed on every Java platform.
 * <p>
 * Note in ID3 UTF_16 can be implemented as either UTF16BE or UTF16LE with byte ordering
 * marks, in JAudioTagger we always implement it as UTF16LE because only this order
 * is understood in Windows, OSX seem to understand both.
 */
public class TextEncoding implements SimpleIntStringMap {
    public static final byte MAX_TEXT_ENCODING_ID = 3;
    //Supported ID3 charset ids
    public static final byte ISO_8859_1 = 0;
    public static final byte UTF_16 = 1;               //We use UTF-16 with LE byte-ordering and byte order mark by default
    //but can also use BOM with BE byte ordering
    public static final byte UTF_16BE = 2;
    public static final byte UTF_8 = 3;

    /**
     * The number of bytes used to hold the text encoding field size.
     */
    public static final int TEXT_ENCODING_FIELD_SIZE = 1;

    private static volatile TextEncoding instance;
    private final Charset[] values;

    private TextEncoding() {
        // small number, we'll do scan of the array as opposed to map lookup and overhead.
        values = new Charset[MAX_TEXT_ENCODING_ID + 1];
        values[ISO_8859_1] = StandardCharsets.ISO_8859_1;
        values[UTF_16] = StandardCharsets.UTF_16;
        values[UTF_16BE] = StandardCharsets.UTF_16BE;
        values[UTF_8] = StandardCharsets.UTF_8;

    }

    /**
     * Get singleton for this class.
     *
     * @return singleton
     */
    public static TextEncoding getInstanceOf() {
        if (instance == null) {
            synchronized (TextEncoding.class) {
                if (instance == null) {
                    instance = new TextEncoding();
                }
            }
        }
        return instance;
    }

    /**
     * Allows to lookup id directly via the {@link Charset} instance.
     *
     * @param charset charset
     * @return id, e.g. {@link #ISO_8859_1}
     * @throws NoSuchElementException if {@code charset} is null or not found
     */
    public byte getIdForCharset(Charset charset) throws NoSuchElementException {
        for (byte i = 0; i <= MAX_TEXT_ENCODING_ID; i++) {
            if (values[i].equals(charset)) {
                return i;
            }
        }
        throw new NoSuchElementException(charset == null ? "null" : charset.name());
    }

    /**
     * Allows direct lookup of the {@link Charset} instance via an id.
     * <p>
     * Note: this method previously returned null if the id was not found. Not one place in the code checked for a null return. So, I'll
     * consider this truly exceptional and throw if not a valid id.
     *
     * @param id id, e.g. {@link #ISO_8859_1}
     * @return charset for id
     * @throws NoSuchElementException no charset is found for the given id
     */
    public Charset getCharsetForId(int id) throws NoSuchElementException {
        if (!containsKey(id)) {
            throw new NoSuchElementException("id=" + id);
        }
        return values[id];
    }

    @Override
    public boolean containsKey(int key) {
        return key >= 0 && key <= MAX_TEXT_ENCODING_ID;
    }

    @Override
    public String getValue(int key) {
        if (!containsKey(key)) {
            return "";
        }
        return values[key].name();
    }
}
