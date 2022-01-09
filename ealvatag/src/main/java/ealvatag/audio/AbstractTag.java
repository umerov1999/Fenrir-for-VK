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

/*
 * jaudiotagger library
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package ealvatag.audio;

import static ealvatag.utils.Check.CANNOT_BE_NULL;
import static ealvatag.utils.Check.CANNOT_BE_NULL_OR_EMPTY;
import static ealvatag.utils.Check.checkArgNotNull;
import static ealvatag.utils.Check.checkArgNotNullOrEmpty;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ealvatag.tag.FieldDataInvalidException;
import ealvatag.tag.FieldKey;
import ealvatag.tag.Key;
import ealvatag.tag.Tag;
import ealvatag.tag.TagField;
import ealvatag.tag.TagFieldContainer;
import ealvatag.tag.TagTextField;
import ealvatag.tag.UnsupportedFieldException;
import ealvatag.tag.images.Artwork;

/**
 * This class is the default implementation for
 * {@link ealvatag.tag.Tag} and introduces some more useful
 * functionality to be implemented.<br>
 *
 * @author Raphaël Slinckx
 */
public abstract class AbstractTag implements TagFieldContainer {

    private static final List<TagField> EMPTY_TAG_FIELD_LIST = ImmutableList.of();
    /**
     * This map stores the {@linkplain TagField#getId() ids} of the stored
     * fields to the {@linkplain TagField fields} themselves. Because a linked hashMap is used the order
     * that they are added in is preserved, the only exception to this rule is when two fields of the same id
     * exist, both will be returned according to when the first item was added to the file. <br>
     */
    private final Map<String, List<TagField>> fields = new LinkedHashMap<>();
    private boolean readOnly;
    /**
     * Stores the amount of {@link TagField} with {@link TagField#isCommon()}
     * <code>true</code>.
     */
    private int commonNumber;

    protected AbstractTag(boolean readOnly) {
        this.readOnly = readOnly;
    }

    public List<String> getAll(String id) {
        List<String> fields = new ArrayList<>();
        List<TagField> tagFields = getFieldList(id);
        for (int i = 0, size = tagFields.size(); i < size; i++) {
            fields.add(tagFields.get(i).toString());
        }
        return fields;
    }

    protected Optional<String> getValue(String id, int index) {
        List<TagField> tagFieldList = getFieldList(id);
        if (tagFieldList.size() > index) {
            return Optional.of(tagFieldList.get(index).toString());
        }
        return Optional.absent();
    }

    protected String getItem(String id, int index) {
        List<TagField> tagFieldList = getFieldList(id);
        if (tagFieldList.size() > index) {
            return tagFieldList.get(index).toString();
        }
        return "";
    }

    @Override
    public boolean isReadOnly() {
        return readOnly;
    }

    protected void setReadOnly() {
        readOnly = true;
    }

    /**
     * Is this tag empty
     *
     * @see ealvatag.tag.Tag#isEmpty()
     */
    @Override
    public boolean isEmpty() {
        return fields.size() == 0;
    }

    protected int getFieldsMapSize() {
        return fields.size();
    }

    /**
     * Returns the list of {@link TagField}s for the given key
     *
     * @param id the tag field key
     * @return associated list. Empty if no such field exists
     */
    protected List<TagField> getFieldList(String id) {
        List<TagField> list = fields.get(id);
        return list == null ? EMPTY_TAG_FIELD_LIST : list;
    }

    @Override
    public boolean hasField(FieldKey genericKey) {
        return hasField(genericKey.name());
    }

    /**
     * Does this tag contain a field with the specified id
     *
     * @see ealvatag.tag.Tag#hasField(java.lang.String)
     */
    @Override
    public boolean hasField(String id) {
        return getFieldList(id).size() != 0;
    }

    @Override
    public int getFieldCount(Key genericKey) throws IllegalArgumentException, UnsupportedFieldException {
        return getFieldList(genericKey.name()).size();
    }

    /**
     * Return field count
     * <p>
     * TODO:There must be a more efficient way to do this.
     *
     * @return field count
     */
    @Override
    public int getFieldCount() {
        getAll().size();
        Iterator it = getFields();
        int count = 0;
        while (it.hasNext()) {
            count++;
            it.next();
        }
        return count;
    }

    public List<TagField> getAll() {
        List<TagField> fieldList = new ArrayList<>();
        for (List<TagField> listOfFields : fields.values()) {
            for (TagField next : listOfFields) {
                fieldList.add(next);
            }
        }
        return fieldList;
    }

    @Override
    public Tag setField(FieldKey genericKey, String... values) throws IllegalArgumentException,
            UnsupportedFieldException,
            FieldDataInvalidException {
        TagField tagfield = createField(genericKey, values);
        setField(tagfield);
        return this;
    }

    @Override
    public Tag addField(FieldKey genericKey, String... values) throws IllegalArgumentException,
            UnsupportedFieldException,
            FieldDataInvalidException {
        TagField tagfield = createField(genericKey, values);
        addField(tagfield);
        return this;
    }

    @Override
    public String getFirst(FieldKey genericKey) throws IllegalArgumentException, UnsupportedFieldException {
        return getValue(genericKey, 0).or("");
    }

    public Optional<String> getValue(FieldKey genericKey) throws IllegalArgumentException {
        return getValue(genericKey, 0);
    }

    @Override
    public String getFirst(String id) throws IllegalArgumentException, UnsupportedFieldException {
        List<TagField> l = getFieldList(id);
        return (l.size() != 0) ? l.get(0).toString() : "";
    }

    public Tag deleteField(String id) throws IllegalArgumentException, UnsupportedFieldException {
        checkArgNotNullOrEmpty(id, CANNOT_BE_NULL_OR_EMPTY, "id");
        fields.remove(id);
        return this;
    }

    public Tag setArtwork(Artwork artwork) throws IllegalArgumentException, UnsupportedFieldException, FieldDataInvalidException {
        setField(createArtwork(checkArgNotNull(artwork, CANNOT_BE_NULL, "artwork")));
        return this;
    }

    public Tag addArtwork(Artwork artwork) throws IllegalArgumentException, UnsupportedFieldException, FieldDataInvalidException {
        addField(createArtwork(checkArgNotNull(artwork, CANNOT_BE_NULL, "artwork")));
        return this;
    }

    public Optional<Artwork> getFirstArtwork() throws UnsupportedFieldException {
        List<Artwork> artwork = getArtworkList();
        if (artwork.size() > 0) {
            return Optional.of(artwork.get(0));
        }
        return Optional.absent();
    }

    public Tag deleteArtwork() throws UnsupportedFieldException {
        return deleteField(FieldKey.COVER_ART);
    }

    /**
     * Does this tag contain any comon fields
     *
     * @see ealvatag.tag.Tag#hasCommonFields()
     */
    @Override
    public boolean hasCommonFields() {
        return commonNumber != 0;
    }

    @Override
    public int getFieldCountIncludingSubValues() {
        return getFieldCount();
    }

    /**
     * Set or add encoding
     */
    public boolean setEncoding(Charset enc) {
        if (!isAllowedEncoding(enc)) {
            return false;
        }

        Iterator it = getFields();
        while (it.hasNext()) {
            TagField field = (TagField) it.next();
            if (field instanceof TagTextField) {
                ((TagTextField) field).setEncoding(enc);
            }
        }

        return true;
    }

    /**
     * Determines whether the given charset encoding may be used for the
     * represented tagging system.
     *
     * @param enc charset encoding.
     * @return <code>true</code> if the given encoding can be used.
     */
    protected abstract boolean isAllowedEncoding(Charset enc);

    @Override
    public Iterator<TagField> getFields() {
        final Iterator<Map.Entry<String, List<TagField>>> it = fields.entrySet().iterator();
        return new Iterator<TagField>() {
            private Iterator<TagField> fieldsIt;

            @Override
            public boolean hasNext() {
                if (fieldsIt == null) {
                    changeIt();
                }
                return it.hasNext() || (fieldsIt != null && fieldsIt.hasNext());
            }

            private void changeIt() {
                if (!it.hasNext()) {
                    return;
                }

                Map.Entry<String, List<TagField>> e = it.next();
                List<TagField> l = e.getValue();
                fieldsIt = l.iterator();
            }

            @Override
            public TagField next() {
                if (!fieldsIt.hasNext()) {
                    changeIt();
                }

                return fieldsIt.next();
            }

            @Override
            public void remove() {
                fieldsIt.remove();
            }
        };
    }

    @Override
    public ImmutableList<TagField> getFields(String id) {
        List<TagField> tagFields = fields.get(id);
        if (tagFields == null) {
            return ImmutableList.of();
        }
        return ImmutableList.copyOf(tagFields);
    }


    @Override
    public Optional<TagField> getFirstField(String id) throws IllegalArgumentException, UnsupportedFieldException {
        List<TagField> l = getFieldList(id);
        return l.size() != 0 ? Optional.fromNullable(l.get(0)) : Optional.<TagField>absent();
    }

    @Override
    public void setField(TagField field) {
        if (field == null) {
            return;
        }

        // If there is already an existing field with same id
        // and both are TextFields, we replace the first element
        List<TagField> list = fields.get(field.getId());
        if (list != null) {
            list.set(0, field);
            return;
        }

        // Else we put the new field in the fields.
        list = new ArrayList<>();
        list.add(field);
        fields.put(field.getId(), list);
        if (field.isCommon()) {
            commonNumber++;
        }
    }

    public void addField(TagField field) {
        if (field == null) {
            return;
        }
        List<TagField> list = fields.get(field.getId());

        // There was no previous item
        if (list == null) {
            list = new ArrayList<>();
            list.add(field);
            fields.put(field.getId(), list);
            if (field.isCommon()) {
                commonNumber++;
            }
        } else {
            // We append to existing list
            list.add(field);
        }
    }

    public String toString() {
        StringBuilder out = new StringBuilder();
        out.append("Tag content:\n");
        Iterator it = getFields();
        while (it.hasNext()) {
            TagField field = (TagField) it.next();
            out.append("\t");
            out.append(field.getId());
            out.append(":");
            out.append(field);
            out.append("\n");
        }
        return out.substring(0, out.length() - 1);
    }


    @Override
    public ImmutableList<TagField> getFields(FieldKey genericKey) throws IllegalArgumentException, UnsupportedFieldException {
        checkArgNotNull(genericKey, CANNOT_BE_NULL, "genericKey");
        return ImmutableList.copyOf(getFieldList(genericKey.name()));
    }
}
