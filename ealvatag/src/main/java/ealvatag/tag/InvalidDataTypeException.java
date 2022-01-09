/*
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
package ealvatag.tag;

import java.util.Locale;

/**
 * Indicates there was a problem parsing this datatype due to a problem with the data
 * such as the array being empty when trying to read from a file.
 */
public class InvalidDataTypeException extends InvalidTagException {
    private static final long serialVersionUID = -7361525721486799573L;

    public InvalidDataTypeException(Throwable cause, String message) {
        super(message, cause);
    }

    public InvalidDataTypeException(Throwable ex) {
        super(ex);
    }

    public InvalidDataTypeException(String msg) {
        super(msg);
    }

    public InvalidDataTypeException(String msg, Object... formatArgs) {
        super(String.format(Locale.getDefault(), msg, formatArgs));
    }

}
