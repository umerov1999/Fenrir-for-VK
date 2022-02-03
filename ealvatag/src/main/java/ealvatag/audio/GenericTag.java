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

import static ealvatag.utils.Check.CANNOT_BE_NULL;
import static ealvatag.utils.Check.checkArgNotNull;
import static ealvatag.utils.Check.checkVarArg0NotNull;

import androidx.annotation.NonNull;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;

import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;

import ealvatag.tag.FieldDataInvalidException;
import ealvatag.tag.FieldKey;
import ealvatag.tag.Tag;
import ealvatag.tag.TagField;
import ealvatag.tag.TagTextField;
import ealvatag.tag.UnsupportedFieldException;
import ealvatag.tag.images.Artwork;
import ealvatag.utils.StandardCharsets;

/**
 * This is a complete example implementation of  {@link AbstractTag}
 *
 * @author Raphaël Slinckx
 */
public abstract class GenericTag extends AbstractTag {
    private static final byte[] EMPTY_BYTE_ARRAY = {};
    final private static ImmutableSet<FieldKey> supportedKeys = ImmutableSet.of(FieldKey.ALBUM,
            FieldKey.ARTIST,
            FieldKey.TITLE,
            FieldKey.TRACK,
            FieldKey.GENRE,
            FieldKey.COMMENT,
            FieldKey.YEAR);


    protected GenericTag() {
        super(false);
    }

    public static ImmutableSet<FieldKey> getSupportedTagKeys() {
        return supportedKeys;
    }

    @Override
    protected boolean isAllowedEncoding(Charset enc) {
        return true;
    }

    @Override
    public TagField createField(FieldKey genericKey, String... values) throws IllegalArgumentException,
            UnsupportedFieldException,
            FieldDataInvalidException {
        checkArgNotNull(genericKey, CANNOT_BE_NULL, "genericKey");
        if (getSupportedFields().contains(genericKey)) {
            return new GenericTagTextField(genericKey.name(), checkVarArg0NotNull(values));
        } else {
            throw new UnsupportedFieldException(genericKey.name());
        }
    }

    @Override
    public String getFirst(FieldKey genericKey) throws IllegalArgumentException, UnsupportedFieldException {
        return getValue(genericKey, 0).or("");
    }

    @Override
    public Optional<String> getValue(FieldKey genericKey, int index) throws IllegalArgumentException {
        checkArgNotNull(genericKey, CANNOT_BE_NULL, "genericKey");
        return getValue(genericKey.name(), index);
    }

    @Override
    public String getFieldAt(FieldKey genericKey, int index) throws IllegalArgumentException, UnsupportedFieldException {
        return getValue(genericKey, index).or("");
    }

    @Override
    public List<String> getAll(FieldKey genericKey) throws IllegalArgumentException, UnsupportedFieldException {
        checkArgNotNull(genericKey, CANNOT_BE_NULL, "genericKey");
        if (!getSupportedFields().contains(genericKey)) {
            throw new UnsupportedFieldException(genericKey.name());
        }
        return getAll(genericKey.name());
    }

    @Override
    public Tag deleteField(FieldKey genericKey) throws IllegalArgumentException, UnsupportedFieldException {
        checkArgNotNull(genericKey, CANNOT_BE_NULL, "genericKey");
        if (getSupportedFields().contains(genericKey)) {
            deleteField(genericKey.name());
        } else {
            throw new UnsupportedFieldException(genericKey.name());
        }
        return this;
    }

    @Override
    public Optional<TagField> getFirstField(FieldKey genericKey)
            throws IllegalArgumentException, UnsupportedFieldException {
        checkArgNotNull(genericKey, CANNOT_BE_NULL, genericKey);
        if (getSupportedFields().contains(genericKey)) {
            return getFirstField(genericKey.name());
        } else {
            throw new UnsupportedFieldException(genericKey.name());
        }
    }

    @Override
    public List<Artwork> getArtworkList() throws UnsupportedFieldException {
        return Collections.emptyList();
    }

    @Override
    public TagField createArtwork(Artwork artwork) throws UnsupportedFieldException, FieldDataInvalidException {
        throw new UnsupportedFieldException(FieldKey.COVER_ART.name());
    }

    @Override
    public ImmutableSet<FieldKey> getSupportedFields() {
        return supportedKeys;
    }

    /**
     * Implementations of {@link TagTextField} for use with
     * &quot;ISO-8859-1&quot; strings.
     *
     * @author Raphaël Slinckx
     */
    protected static class GenericTagTextField implements TagTextField {

        /**
         * Stores the identifier.
         */
        private final String id;
        /**
         * Stores the string.
         */
        private String content;

        /**
         * Creates an instance.
         *
         * @param fieldId        The identifier.
         * @param initialContent The string.
         */
        public GenericTagTextField(String fieldId, String initialContent) {
            id = fieldId;
            content = initialContent;
        }

        @Override
        public void copyContent(TagField field) {
            if (field instanceof TagTextField) {
                content = ((TagTextField) field).getContent();
            }
        }

        @Override
        public String getContent() {
            return content;
        }

        @Override
        public void setContent(String s) {
            content = s;
        }

        @Override
        public Charset getEncoding() {
            return StandardCharsets.ISO_8859_1;
        }

        @Override
        public void setEncoding(Charset s) {
            /* Not allowed */
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public byte[] getRawContent() {
            return content == null ? EMPTY_BYTE_ARRAY : content.getBytes(getEncoding());
        }

        @Override
        public boolean isBinary() {
            return false;
        }

        @Override
        public void isBinary(boolean b) {
            /* not supported */
        }

        @Override
        public boolean isCommon() {
            return true;
        }

        @Override
        public boolean isEmpty() {
            return "".equals(content);
        }

        @NonNull
        @Override
        public String toString() {
            return getContent();
        }
    }
}
