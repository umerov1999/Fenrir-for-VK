/*
 * @author : Paul Taylor
 * <p>
 * Version @version:$Id$
 * <p>
 * MusicTag Copyright (C)2003,2004
 * <p>
 * This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public  License as published by the Free Software Foundation; either version 2.1 of the License,
 * or (at your option) any later version.
 * <p>
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public License along with this library; if not,
 * you can get a copy from http://www.opensource.org/licenses/lgpl-license.php or write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 */
package ealvatag.logging;

import androidx.annotation.NonNull;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;

/*
 * For Formatting the metadata contents of a file in an XML format
 *
 * This could provide the basis of a representation of a files metadata, which can then be manipulated to
 * to create technical reports.
 */
public class XMLTagDisplayFormatter extends AbstractTagDisplayFormatter {
//  private static XMLTagDisplayFormatter formatter;

    private static final String xmlOpenStart = "<";
    private static final String xmlOpenEnd = ">";
    private static final String xmlCloseStart = "</";
    private static final String xmlCloseEnd = ">";
    private static final String xmlSingleTagClose = " />";
    private static final String xmlCDataTagOpen = "<![CDATA[";
    private static final String xmlCDataTagClose = "]]>";


    private final StringBuilder sb = new StringBuilder();

    public XMLTagDisplayFormatter() {

    }

    /**
     * Return xml open tag round a string e.g <tag>
     */
    @SuppressWarnings("WeakerAccess")
    public static String xmlOpen(String xmlName) {
        return xmlOpenStart + xmlName + xmlOpenEnd;
    }

    @SuppressWarnings("WeakerAccess")
    public static String xmlOpenHeading(String name, String data) {
        return (xmlOpen(name + " id=\"" + data + "\""));
    }


    /**
     * Return CDATA tag around xml data e.g <![CDATA[xmlData]]>
     * We also need to deal with special chars
     */
    @SuppressWarnings("WeakerAccess")
    public static String xmlCData(String xmlData) {
        char tempChar;
        StringBuilder replacedString = new StringBuilder();
        for (int i = 0; i < xmlData.length(); i++) {
            tempChar = xmlData.charAt(i);
            if ((Character.isLetterOrDigit(tempChar)) || (Character.isSpaceChar(tempChar))) {
                replacedString.append(tempChar);
            } else {
                replacedString.append("&#x").append(Integer.toString(Character.codePointAt(xmlData, i), 16));
            }
        }
        return xmlCDataTagOpen + replacedString + xmlCDataTagClose;
    }

    /**
     * Return xml close tag around a string e.g </tag>
     */
    @SuppressWarnings("WeakerAccess")
    public static String xmlClose(String xmlName) {
        return xmlCloseStart + xmlName + xmlCloseEnd;
    }

    @SuppressWarnings("unused")
    public static String xmlSingleTag(String data) {
        return xmlOpenStart + data + xmlSingleTagClose;
    }

    @SuppressWarnings("WeakerAccess")
    public static String xmlFullTag(String xmlName, String data) {
        return xmlOpen(xmlName) + xmlCData(data) + xmlClose(xmlName);
    }

    /**
     * Replace any special xml characters with the appropiate escape sequences
     * required to be done for the actual element names
     */
    @SuppressWarnings("WeakerAccess")
    public static String replaceXMLCharacters(String xmlData) {
        StringBuilder sb = new StringBuilder();
        StringCharacterIterator sCI = new StringCharacterIterator(xmlData);
        for (char c = sCI.first(); c != CharacterIterator.DONE; c = sCI.next()) {
            switch (c) {
                case '&':
                    sb.append("&amp;");
                    break;
                case '<':
                    sb.append("&lt;");
                    break;
                case '>':
                    sb.append("&gt;");
                    break;
                case '"':
                    sb.append("&quot;");
                    break;
                case '\'':
                    sb.append("&apos;");
                    break;


                default:
                    sb.append(c);
            }
        }
        return sb.toString();
    }

    public void openHeadingElement(String type, String value) {
        if (value.length() == 0) {
            sb.append(xmlOpen(type));
        } else {
            sb.append(xmlOpenHeading(type, replaceXMLCharacters(value)));
        }
    }

    public void openHeadingElement(String type, boolean value) {
        openHeadingElement(type, String.valueOf(value));
    }

    public void openHeadingElement(String type, int value) {
        openHeadingElement(type, String.valueOf(value));
    }

    public void closeHeadingElement(String type) {
        sb.append(xmlClose(type));
    }

    public void addElement(String type, String value) {
        sb.append(xmlFullTag(type, replaceXMLCharacters(value)));
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
