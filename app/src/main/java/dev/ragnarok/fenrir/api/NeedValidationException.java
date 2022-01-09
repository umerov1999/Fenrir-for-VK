package dev.ragnarok.fenrir.api;

public class NeedValidationException extends Exception {

    private final String type;
    private final String validate_url;
    private final String validation_sid;
    private final String phoneMask;

    public NeedValidationException(String type, String validate_url, String validation_sid, String phoneMask) {
        this.type = type;
        this.validate_url = validate_url;
        this.validation_sid = validation_sid;
        this.phoneMask = phoneMask;
    }

    public String getSid() {
        return validation_sid;
    }

    public String getPhoneMask() {
        return phoneMask;
    }

    public String getValidationType() {
        return type;
    }

    public String getValidationURL() {
        return validate_url;
    }
}