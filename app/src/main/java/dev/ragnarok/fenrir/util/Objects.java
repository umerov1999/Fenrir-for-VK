package dev.ragnarok.fenrir.util;

import androidx.annotation.Nullable;


public class Objects {

    private Objects() {

    }

    public static boolean nonNull(@Nullable Object o) {
        return o != null;
    }

    public static boolean isNull(@Nullable Object o) {
        return o == null;
    }

    /**
     * Perform a safe equals between 2 objects.
     * <p>
     * It manages the case where the first object is null and it would have resulted in a
     * {@link NullPointerException} if <code>o1.equals(o2)</code> was used.
     *
     * @param o1 First object to check.
     * @param o2 Second object to check.
     * @return <code>true</code> if both objects are equal. <code>false</code> otherwise
     * @see Object#equals(Object) uals()
     */
    public static boolean safeEquals(Object o1, Object o2) {
        if (o1 == null) {
            return o2 == null;
        } else {
            return o1.equals(o2);
        }
    }
}
