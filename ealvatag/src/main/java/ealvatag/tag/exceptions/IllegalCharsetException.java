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

package ealvatag.tag.exceptions;

/**
 * Thrown when attempting ot parse and the specified charset cannot be found, doesn't match the spec, or is not allowed for some reason.
 * <p>
 * Created by Eric A. Snell on 1/26/17.
 */
public class IllegalCharsetException extends RuntimeException {
    private static final long serialVersionUID = 3588767037304196412L;

    public IllegalCharsetException(String message, Throwable cause) {
        super(message, cause);
    }
}
