package ealvatag.utils;

import com.google.common.base.Strings;

/**
 * Along the line of Guava's Preconditions
 * <p>
 * Created by eric on 1/13/17.
 */
public class Check {

    public static final String CANNOT_BE_NULL = "%s cannot be null";
    public static final String CANNOT_BE_NULL_OR_EMPTY = "%s cannot be null or the empty string";
    public static final String AT_LEAST_ONE_REQUIRED = "At least one %s required";

    public static <T> T checkArgNotNull(T reference) {
        if (reference == null) {
            throw new IllegalArgumentException();
        } else {
            return reference;
        }
    }

    public static <T> T checkArgNotNull(T reference, Object errorMessage) {
        if (reference == null) {
            throw new IllegalArgumentException(String.valueOf(errorMessage));
        } else {
            return reference;
        }
    }

    public static <T> T checkArgNotNull(T reference, String errorMessageTemplate, Object... errorMessageArgs) {
        if (reference == null) {
            throw new IllegalArgumentException(String.format(errorMessageTemplate, errorMessageArgs));
        } else {
            return reference;
        }
    }


    public static <T> T checkVarArg0NotNull(T[] reference) {
        if (reference == null || reference[0] == null) {
            throw new IllegalArgumentException();
        } else {
            return reference[0];
        }
    }

    public static <T> T checkVarArg0NotNull(T[] reference, Object errorMessage) {
        if (reference == null || reference[0] == null) {
            throw new IllegalArgumentException(String.valueOf(errorMessage));
        } else {
            return reference[0];
        }
    }

    public static <T> T checkVarArg0NotNull(T[] reference, String errorMessageTemplate, Object... errorMessageArgs) {
        if (reference == null || reference[0] == null) {
            throw new IllegalArgumentException(String.format(errorMessageTemplate, errorMessageArgs));
        } else {
            return reference[0];
        }
    }

    public static String checkArgNotNullOrEmpty(String reference) {
        if (reference == null || Strings.isNullOrEmpty(reference)) {
            throw new IllegalArgumentException();
        }
        return reference;
    }

    public static String checkArgNotNullOrEmpty(String reference,
                                                String errorMessageTemplate) {
        if (reference == null || Strings.isNullOrEmpty(reference)) {
            throw new IllegalArgumentException(errorMessageTemplate);
        }
        return reference;
    }

    public static String checkArgNotNullOrEmpty(String reference,
                                                String errorMessageTemplate,
                                                Object... errorMessageArgs) {
        if (reference == null || Strings.isNullOrEmpty(reference)) {
            throw new IllegalArgumentException(String.format(errorMessageTemplate, errorMessageArgs));
        }
        return reference;
    }
}
