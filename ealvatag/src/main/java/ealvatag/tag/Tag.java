/*
 * Entagged Audio Tag library
 * Copyright (c) 2003-2010 Raphaël Slinckx <raphael@slinckx.net>
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
package ealvatag.tag;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.List;

import ealvatag.tag.images.Artwork;

/**
 * This interface represents the basic data structure for the default
 * audio library functionality.
 * <p>
 * Some audio file tagging systems allow to specify multiple values for one type
 * of information. The artist for example. Some songs may be a cooperation of
 * two or more artists. Sometimes a tagging user wants to specify them in the
 * tag without making one long text string.
 * <p>
 * The addField() method can be used for this but it is possible the underlying implementation
 * does not support that kind of storing multiple values and will just overwrite the existing value<br>
 * <br>
 * <b>Code Examples:</b><br>
 * <p>
 * <pre>
 * <code>
 * AudioFile file = AudioFileIO.read(new File(&quot;C:\\test.mp3&quot;));
 *
 * Tag tag = file.getTag();
 * </code>
 * </pre>
 *
 * @author Raphael Slinckx
 * @author Paul Taylor
 */
public interface Tag {

    /**
     * A tag instance may be marked read-only during an initial read to prevent changes or as an optimization of some sort. For example,
     * if a read excludes artwork as an optimization, the tag is marked read only to prevent it being accidentally written without the
     * artwork.
     *
     * @return true if this instance cannot be modified
     */
    boolean isReadOnly();

    /**
     * Get all the {@link FieldKey}s this tag supports
     *
     * @return set of supported keys. Guaranteed non-null
     */
    ImmutableSet<FieldKey> getSupportedFields();

    /**
     * Determines whether the tag has no fields specified.<br>
     *
     * @return <code>true</code> if tag contains no field.
     */
    boolean isEmpty();

    /**
     * Determines whether the tag has at least one field with the specified field key.
     *
     * @param genericKey the key to search for
     * @return true if this tag instance contains the {@link FieldKey}
     * @throws IllegalArgumentException  if {@code fieldKey} is null
     * @throws UnsupportedFieldException if this tag doesn't support the {@link FieldKey}
     */
    boolean hasField(FieldKey genericKey) throws IllegalArgumentException, UnsupportedFieldException;

    /**
     * Determines whether the tag has at least one field with the specified
     * &quot;id&quot;.
     *
     * @param id The field id to look for.
     * @return <code>true</code> if tag contains a {@link TagField} with the given {@linkplain TagField#getId() id}.
     */
    boolean hasField(String id);

    int getFieldCount(Key genericKey) throws IllegalArgumentException, UnsupportedFieldException;

    /**
     * Return the number of fields
     * <p>
     * <p>Fields with the same identifiers are counted separately
     * <p>
     * i.e two TITLE fields in a Vorbis Comment file would count as two
     *
     * @return total number of fields
     */
    int getFieldCount();

    /**
     * Create the field based on the generic key and set it in the tag
     *
     * @param genericKey the field to set (Never {@link FieldKey#COVER_ART}
     * @param values     value(s) to set into the field
     * @return self
     * @throws IllegalArgumentException  if the {@code genericKey} is null or no value is passed
     * @throws UnsupportedFieldException if this Tag does not support the {@link FieldKey}
     * @throws FieldDataInvalidException if the data is invalid for the given field
     */
    Tag setField(FieldKey genericKey, String... values) throws IllegalArgumentException,
            UnsupportedFieldException,
            FieldDataInvalidException;

    /**
     * Create the field based on the generic key and add it to the tag
     * <p>
     * This is handled differently by different formats
     *
     * @param genericKey the field to set
     * @param values     value(2) to set into the field
     * @return self
     * @throws IllegalArgumentException  if the {@code genericKey} is null or no value is passed
     * @throws UnsupportedFieldException if this Tag does not support the {@link FieldKey}
     * @throws FieldDataInvalidException if the data is invalid for the given field
     */
    Tag addField(FieldKey genericKey, String... values) throws IllegalArgumentException,
            UnsupportedFieldException,
            FieldDataInvalidException;

    /**
     * Retrieve String value of the first field that exists for this generic key
     *
     * @param genericKey field to get
     * @return value of the field or empty string if the field does not exist
     * @throws IllegalArgumentException  if {@code genericKey} is null
     * @throws UnsupportedFieldException if the Tag instance doesn't support the {@link FieldKey}
     */
    String getFirst(FieldKey genericKey) throws IllegalArgumentException, UnsupportedFieldException;

    /**
     * Retrieve String value of the first field that exists for this format specific key
     * <p>
     * Can be used to retrieve fields with any identifier, useful if the identifier is not within {@link FieldKey}
     *
     * @param id field to get
     * @return value of the field or empty string if the field does not exist
     * @throws IllegalArgumentException  if {@code id} is null or empty
     * @throws UnsupportedFieldException if the Tag instance doesn't support the field specified by {@code id}
     * @deprecated use {@link #getValue(FieldKey)}. eg. {@code getValue(ALBUM).or("")}
     */
    String getFirst(String id) throws IllegalArgumentException, UnsupportedFieldException;

    /**
     * Get the value of the first field for the key. Convenience method for {@link #getValue(FieldKey, int)} with 0 index.
     *
     * @param genericKey the specific field to get
     * @return the String value if the field exists in this tag, else {@link Optional#absent()}
     * @throws IllegalArgumentException if {@code key} is null
     */
    Optional<String> getValue(FieldKey genericKey) throws IllegalArgumentException;

    /**
     * Get the value of the field for the key at the given index
     *
     * @param genericKey the specific field to get
     * @param index      the index into the list of values for the given key
     * @return the String value if the field exists in this tag at the given index, else {@link Optional#absent()}
     * @throws IllegalArgumentException if {@code key} is null
     * @see #getFieldCount(Key)
     */
    Optional<String> getValue(FieldKey genericKey, int index) throws IllegalArgumentException;

    /**
     * Retrieve String value of the nth tag field that exists for this generic key
     *
     * @param genericKey field to query
     * @param index      index to query
     * @return the value of the {@link FieldKey} at the given {@code index}. Empty string if
     * @throws IllegalArgumentException  if {@code genericKey} is null
     * @throws UnsupportedFieldException if the Tag instance doesn't support the {@link FieldKey}
     * @deprecated use {@link #getValue(FieldKey, int)} eg. {@code getValue(PERFORMER, 1).or("")}
     */
    String getFieldAt(FieldKey genericKey, int index) throws IllegalArgumentException, UnsupportedFieldException;

    /**
     * Retrieve all String values that exist for this generic key
     *
     * @param genericKey The field genericKey.
     * @return A list of values with the given &quot;genericKey&quot;.
     * @throws IllegalArgumentException  if {@code genericKey} is null
     * @throws UnsupportedFieldException if the Tag instance doesn't support the {@link FieldKey}
     */
    List<String> getAll(FieldKey genericKey) throws IllegalArgumentException, UnsupportedFieldException;

    /**
     * Delete any fields with this key
     *
     * @param genericKey key of field to delete
     * @return self
     * @throws IllegalArgumentException  if {@code genericKey} is null
     * @throws UnsupportedFieldException if the Tag instance doesn't support the {@link FieldKey}
     */
    Tag deleteField(FieldKey genericKey) throws IllegalArgumentException, UnsupportedFieldException;

    /**
     * Delete any fields with this id
     *
     * @param id field id
     * @throws IllegalArgumentException  if {@code id} is null or empty
     * @throws UnsupportedFieldException some tag implementations supported a limited number of fields and may throw this exception
     */
    Tag deleteField(String id) throws IllegalArgumentException, UnsupportedFieldException;

    /**
     * Create artwork field based on the data in artwork and then set it in the tag
     *
     * @param artwork the artwork to set
     * @return self
     * @throws IllegalArgumentException  if the {@code artwork} is null
     * @throws UnsupportedFieldException if this Tag does not support artwork
     * @throws FieldDataInvalidException if the data is invalid. Cannot be encoded, ...
     */
    Tag setArtwork(Artwork artwork) throws IllegalArgumentException, UnsupportedFieldException, FieldDataInvalidException;

    /**
     * Create artwork field based on the data in artwork and then add it to the tag
     *
     * @param artwork the artwork to added
     * @return self
     * @throws IllegalArgumentException  if the {@code artwork} is null
     * @throws UnsupportedFieldException if this Tag does not support artwork
     * @throws FieldDataInvalidException if the data is invalid. Cannot be encoded, ...
     */
    Tag addArtwork(Artwork artwork) throws IllegalArgumentException, UnsupportedFieldException, FieldDataInvalidException;

    /**
     * @return first artwork
     * @throws UnsupportedFieldException if the tag does not support artwork
     */
    Optional<Artwork> getFirstArtwork() throws UnsupportedFieldException;

    /**
     * @return a list of all artwork in this file using the format independent Artwork class
     * @throws UnsupportedFieldException if the tag does not support artwork
     */
    List<Artwork> getArtworkList() throws UnsupportedFieldException;

    /**
     * Delete any instance of tag fields used to store artwork
     *
     * @return self
     * @throws UnsupportedFieldException if this tag doesn't support Artwork
     */
    Tag deleteArtwork() throws UnsupportedFieldException;

    /**
     * Returns <code>true</code>, if at least one of the contained
     * fields is a "common" field.
     *
     * @return <code>true</code> if a {@linkplain TagField#isCommon() common} field is present.
     */
    boolean hasCommonFields();

    /**
     * Return the number of fields taking multiple value fields into consideration
     * <p>
     * Fields that actually contain multiple values are counted seperately
     * <p>
     * i.e. a TCON frame in ID3v24 frame containing multiple genres would add to count for each genre.
     *
     * @return total number of fields taking multiple value fields into consideration
     */
    int getFieldCountIncludingSubValues();

    boolean setEncoding(Charset enc) throws FieldDataInvalidException;

    /**
     * Create a new field
     *
     * @param genericKey create field for this key
     * @param value      the value(s) for the created {@link TagField}. Only {@link FieldKey#PERFORMER} supports more than 1 value.
     * @return {@link TagField} for {@code genericKey}
     * @throws IllegalArgumentException  if {@code genericKey} is null or at least 1 values is not passed
     * @throws UnsupportedFieldException if the generic key us unsupported by this tag or is {@link FieldKey#COVER_ART}
     * @throws FieldDataInvalidException data could is not valid for this field type
     */
    TagField createField(FieldKey genericKey, String... value) throws IllegalArgumentException,
            UnsupportedFieldException,
            FieldDataInvalidException;

    TagField createArtwork(Artwork artwork) throws UnsupportedFieldException, FieldDataInvalidException;

    // TODO: 1/23/17 Are the getFields() methods necessary anymore or only for testing??

    /**
     * Returns a {@link List} of {@link TagField} objects whose &quot;{@linkplain TagField#getId() genericKey}&quot;
     * is the specified one.<br>
     *
     * @param genericKey The field genericKey.
     * @return A list of {@link TagField} objects with the given &quot;genericKey&quot;.
     * @throws IllegalArgumentException  if {@code genericKey} is null
     * @throws UnsupportedFieldException if the Tag instance doesn't support the {@link FieldKey}
     */
    ImmutableList<TagField> getFields(FieldKey genericKey) throws IllegalArgumentException, UnsupportedFieldException;

    /**
     * Iterator over all the fields within the tag, handle multiple fields with the same id
     *
     * @return iterator over whole list
     */
    Iterator<TagField> getFields();

    /**
     * Returns a {@link ImmutableList} of {@link TagField} objects whose &quot;{@linkplain TagField#getId() id}&quot;
     * is the specified one.<br>
     * <p>
     * <p>Can be used to retrieve fields with any identifier, useful if the identifier is not within {@link FieldKey}
     *
     * @param id The field id.
     * @return A list of {@link TagField} objects with the given &quot;id&quot;. List is empty if none found
     */
    ImmutableList<TagField> getFields(String id);

    /**
     * Retrieve the first field that exists for this format specific key
     * <p>
     * Can be used to retrieve fields with any identifier, useful if the identifier is not within {@link FieldKey}
     *
     * @param id audio specific key
     * @return tag field
     * @throws IllegalArgumentException  if {@code id} is null or empty
     * @throws UnsupportedFieldException if the Tag instance does not support the field given by {@code id} parameter
     */
    Optional<TagField> getFirstField(String id) throws IllegalArgumentException, UnsupportedFieldException;

    /**
     * @param genericKey field to search for
     * @return the first field that matches this generic key
     * @throws IllegalArgumentException  if {@code fieldKey} is null
     * @throws UnsupportedFieldException if this tag doesn't support the {@link FieldKey}
     */
    Optional<TagField> getFirstField(FieldKey genericKey) throws IllegalArgumentException, UnsupportedFieldException;

    /**
     * Creates isCompilation field
     * <p>
     * It is useful to have this method because it handles ensuring that the correct value to represent a boolean
     * is stored in the underlying field format.
     *
     * @param value the boolean to be converted to the underlying tag representation
     * @return the {@link FieldKey#IS_COMPILATION}
     * @throws UnsupportedFieldException if the Tag doesn't support the {@link FieldKey#IS_COMPILATION} field
     */
    TagField createCompilationField(boolean value) throws UnsupportedFieldException;
}
