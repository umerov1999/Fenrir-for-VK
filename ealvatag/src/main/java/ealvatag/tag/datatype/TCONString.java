package ealvatag.tag.datatype;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import ealvatag.tag.id3.AbstractTagFrameBody;

/**
 * Overrides in order to properly support the ID3v23 implemenation of TCON
 */
public class TCONString extends TextEncodedStringSizeTerminated {
    private boolean isNullSeparateMultipleValues = true;


    /**
     * Creates a new empty TextEncodedStringSizeTerminated datatype.
     *
     * @param identifier identifies the frame type
     */
    public TCONString(String identifier, AbstractTagFrameBody frameBody) {
        super(identifier, frameBody);
    }

    /**
     * Copy constructor
     */
    @SuppressWarnings("unused")
    public TCONString(TCONString object) {
        super(object);
    }

    private static List<String> splitV23(String value) {
        String[] valuesarray = value.replaceAll("(\\(\\d+\\)|\\(RX\\)|\\(CR\\)\\w*)", "$1\u0000").split("\u0000");
        List<String> values = Arrays.asList(valuesarray);
        //Read only list so if empty have to create new list
        if (values.size() == 0) {
            values = new ArrayList<>(1);
            values.add("");
        }
        return values;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        return obj instanceof TCONString && super.equals(obj);
    }

    /**
     * If this field is used with ID3v24 then it is usual to null separate values. Within ID3v23 not many
     * frames officially support multiple values, so in absence of better solution we use the v24 method, however
     * some frames such as TCON have there own method and should not null separate values. This can be controlled
     * by this field.
     */
    @SuppressWarnings("WeakerAccess")
    public boolean isNullSeparateMultipleValues() {
        return isNullSeparateMultipleValues;
    }

    public void setNullSeparateMultipleValues(boolean nullSeparateMultipleValues) {
        isNullSeparateMultipleValues = nullSeparateMultipleValues;
    }

    /**
     * Add an additional String to the current String value
     */
    @Override
    public void addValue(String value) {
        //For ID3v24 we separate each value by a null
        if (isNullSeparateMultipleValues()) {
            setValue(this.value + "\u0000" + value);
        } else {
            //For ID3v23 if they pass a numeric value in brackets this indicates a mapping to an ID3v2 genre and
            //can be seen as a refinement and therefore do not need the non-standard (for ID3v23) null seperator
            if (value.startsWith("(")) {
                setValue(this.value + value);
            }
            //but if just text we need to separate some way so we do using null separator
            else {
                setValue(this.value + "\u0000" + value);
            }
        }
    }

    /**
     * How many values are held, each value is separated by a null terminator
     *
     * @return number of values held, usually this will be one.
     */
    public int getNumberOfValues() {
        return getValues().size();
    }

    /**
     * Get the nth value
     *
     * @return the nth value
     * @throws IndexOutOfBoundsException if value does not exist
     */
    public String getValueAtIndex(int index) {
        //Split String into separate components
        List values = getValues();
        return (String) values.get(index);
    }

    /**
     * @return list of all values
     */
    public List<String> getValues() {
        if (value != null) {
            if (isNullSeparateMultipleValues()) {
                return splitByNullSeperator((String) value);
            } else {
                return splitV23((String) value);
            }
        }
        return Collections.emptyList();
    }

    /**
     * Get value(s) whilst removing any trailing nulls
     */
    public String getValueWithoutTrailingNull() {
        List<String> values = getValues();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values.size(); i++) {
            if (i != 0) {
                sb.append("\u0000");
            }
            sb.append(values.get(i));
        }
        return sb.toString();
    }
}
