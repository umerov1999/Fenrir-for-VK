package ealvatag.tag;

/**
 * Thrown if the try and create a field with invalid data
 * <p>
 * <p>For example if try and create an Mp4Field with type Byte using data that cannot be parsed as a number
 * then this exception will be thrown
 */
public class FieldDataInvalidException extends TagException {
    public FieldDataInvalidException(String msg) {
        super(msg);
    }

    public FieldDataInvalidException(Throwable cause) {
        super(cause);
    }
}
