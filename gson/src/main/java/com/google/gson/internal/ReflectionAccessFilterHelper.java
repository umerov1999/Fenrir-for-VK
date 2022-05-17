package com.google.gson.internal;

import com.google.gson.ReflectionAccessFilter;
import com.google.gson.ReflectionAccessFilter.FilterResult;

import java.lang.reflect.AccessibleObject;
import java.util.List;

/**
 * Internal helper class for {@link ReflectionAccessFilter}.
 */
public class ReflectionAccessFilterHelper {
    private ReflectionAccessFilterHelper() {
    }

    // Platform type detection is based on Moshi's Util.isPlatformType(Class)
    // See https://github.com/square/moshi/blob/3c108919ee1cce88a433ffda04eeeddc0341eae7/moshi/src/main/java/com/squareup/moshi/internal/Util.java#L141

    public static boolean isJavaType(Class<?> c) {
        return isJavaType(c.getName());
    }

    private static boolean isJavaType(String className) {
        return className.startsWith("java.") || className.startsWith("javax.");
    }

    public static boolean isAndroidType(Class<?> c) {
        return isAndroidType(c.getName());
    }

    private static boolean isAndroidType(String className) {
        return className.startsWith("android.")
                || className.startsWith("androidx.")
                || isJavaType(className);
    }

    public static boolean isAnyPlatformType(Class<?> c) {
        String className = c.getName();
        return isAndroidType(className) // Covers Android and Java
                || className.startsWith("kotlin.")
                || className.startsWith("kotlinx.")
                || className.startsWith("scala.");
    }

    /**
     * Gets the result of applying all filters until the first one returns a result
     * other than {@link FilterResult#INDECISIVE}, or {@link FilterResult#ALLOW} if
     * the list of filters is empty or all returned {@code INDECISIVE}.
     */
    public static FilterResult getFilterResult(List<ReflectionAccessFilter> reflectionFilters, Class<?> c) {
        for (ReflectionAccessFilter filter : reflectionFilters) {
            FilterResult result = filter.check(c);
            if (result != FilterResult.INDECISIVE) {
                return result;
            }
        }
        return FilterResult.ALLOW;
    }

    public static boolean canAccess(AccessibleObject accessibleObject, Object object) {
        return AccessChecker.INSTANCE.canAccess(accessibleObject, object);
    }

    private static abstract class AccessChecker {
        public static final AccessChecker INSTANCE;

        static {
            INSTANCE = new AccessChecker() {
                @Override
                public boolean canAccess(AccessibleObject accessibleObject, Object object) {
                    // Cannot determine whether object can be accessed, so assume it can be accessed
                    return true;
                }
            };
        }

        public abstract boolean canAccess(AccessibleObject accessibleObject, Object object);
    }
}
