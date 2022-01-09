/*
 * Entagged Audio Tag library
 * Copyright (c) 2003-2005 Raphaël Slinckx <raphael@slinckx.net>
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
package ealvatag.audio.exceptions;

import java.util.Locale;

/**
 * This exception is thrown if the writing process of an audio file failed.
 *
 * @author Rapha�l Slinckx
 */
public class CannotWriteException extends Exception {
    public CannotWriteException() {
    }

    public CannotWriteException(String message) {
        super(message);
    }

    public CannotWriteException(String message, Object... formatArgs) {
        super(String.format(Locale.getDefault(), message, formatArgs));
    }

    public CannotWriteException(Throwable cause, String message) {
        super(message, cause);
    }

    public CannotWriteException(Throwable cause, String message, Object... formatArgs) {
        super(String.format(Locale.getDefault(), message, formatArgs), cause);
    }

    public CannotWriteException(Throwable cause) {
        super(cause);

    }

}
