package ealvatag.audio.real;

import ealvatag.audio.GenericTag;
import ealvatag.tag.FieldDataInvalidException;
import ealvatag.tag.FieldKey;
import ealvatag.tag.TagField;
import ealvatag.tag.UnsupportedFieldException;

public class RealTag extends GenericTag {
    public String toString() {
        return "REAL " + super.toString();
    }

    public TagField createCompilationField(boolean value) throws UnsupportedFieldException {
        try {
            return createField(FieldKey.IS_COMPILATION, String.valueOf(value));
        } catch (FieldDataInvalidException e) {
            throw new RuntimeException(e); // should never happen unless library misconfiguration
        }
    }
}
