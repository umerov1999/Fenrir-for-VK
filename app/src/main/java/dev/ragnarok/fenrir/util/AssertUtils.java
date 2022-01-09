package dev.ragnarok.fenrir.util;

public class AssertUtils {

    /**
     * Returns {@code o} if non-null, or throws {@code NullPointerException}.
     */
    public static <T> T requireNonNull(T o) {
        if (o == null) {
            throw new NullPointerException();
        }
        return o;
    }

    /**
     * Returns {@code o} if non-null, or throws {@code NullPointerException}
     * with the given detail message.
     */
    public static <T> T requireNonNull(T o, String message) {
        if (o == null) {
            throw new NullPointerException(message);
        }
        return o;
    }

    public static void assertPositive(int value) {
        if (value <= 0) {
            throw new IllegalStateException();
        }
    }

    public static void assertTrue(boolean value) {
        if (!value) {
            throw new IllegalStateException();
        }
    }
}