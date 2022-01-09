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

package ealvatag.tag;

/**
 * A general interface for extending {@link Tag} as a container of {@link TagField}
 * <p>
 * Created to start getting "used internally" methods out of the public interface
 * <p>
 * Created by Eric A. Snell on 1/20/17.
 */
public interface TagFieldContainer extends Tag {

    /**
     * Sets a field in the structure, used internally by the library<br>
     *
     * @param field The field to add.
     * @throws FieldDataInvalidException
     */
    void setField(TagField field) throws FieldDataInvalidException;

    /**
     * Adds a field to the structure, used internally by the library<br>
     *
     * @param field The field to add.
     */
    void addField(TagField field) throws FieldDataInvalidException;

}
