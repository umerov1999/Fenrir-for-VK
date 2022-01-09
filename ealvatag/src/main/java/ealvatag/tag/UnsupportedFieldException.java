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

import java.util.Locale;

/**
 * Thrown when a {@link Tag} instance does not support a particular Field
 * <p>
 * Created by Eric A. Snell on 1/14/17.
 */
public class UnsupportedFieldException extends RuntimeException {

    private static final long serialVersionUID = -985995829314388820L;

    /**
     * @param message the name of the field that caused the exception
     */
    public UnsupportedFieldException(String message) {
        super(message);
    }

    public UnsupportedFieldException(String message, Object... formatArgs) {
        super(String.format(Locale.getDefault(), message, formatArgs));
    }


}
