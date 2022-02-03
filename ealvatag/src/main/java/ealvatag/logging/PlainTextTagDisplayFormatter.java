/*
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
 */
package ealvatag.logging;


import androidx.annotation.NonNull;

/*
 * For Formatting metadata contents of a file as simple text
 */
public class PlainTextTagDisplayFormatter extends AbstractTagDisplayFormatter {
    private static volatile PlainTextTagDisplayFormatter instance;

    private final StringBuilder sb = new StringBuilder();
    private final StringBuilder indent = new StringBuilder();

    public PlainTextTagDisplayFormatter() {

    }

    public static AbstractTagDisplayFormatter getInstanceOf() {
        if (instance == null) {
            synchronized (PlainTextTagDisplayFormatter.class) {
                if (instance == null) {
                    instance = new PlainTextTagDisplayFormatter();
                }
            }
        }
        return instance;
    }

    public void openHeadingElement(String type, String value) {
        addElement(type, value);
        increaseLevel();
    }

    public void openHeadingElement(String type, boolean value) {
        openHeadingElement(type, String.valueOf(value));
    }

    public void openHeadingElement(String type, int value) {
        openHeadingElement(type, String.valueOf(value));
    }

    public void closeHeadingElement(String type) {
        decreaseLevel();
    }

    @SuppressWarnings("WeakerAccess")
    public void increaseLevel() {
        level++;
        indent.append("  ");
    }

    @SuppressWarnings("WeakerAccess")
    public void decreaseLevel() {
        level--;
        indent.setLength(Math.max(indent.length() - 2, 0));
    }

    public void addElement(String type, String value) {
        sb.append(indent).append(type).append(":").append(value).append('\n');
    }

    public void addElement(String type, int value) {
        addElement(type, String.valueOf(value));
    }

    public void addElement(String type, boolean value) {
        addElement(type, String.valueOf(value));
    }

    @NonNull
    public String toString() {
        return sb.toString();
    }
}
