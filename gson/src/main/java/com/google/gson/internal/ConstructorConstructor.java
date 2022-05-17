/*
 * Copyright (C) 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.gson.internal;

import android.os.Environment;

import androidx.annotation.NonNull;

import com.google.gson.InstanceCreator;
import com.google.gson.JsonIOException;
import com.google.gson.ReflectionAccessFilter;
import com.google.gson.ReflectionAccessFilter.FilterResult;
import com.google.gson.internal.reflect.ReflectionHelper;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * Returns a function that can construct an instance of a requested type.
 */
public final class ConstructorConstructor {
    private static boolean logUnsafe;
    private final Map<Type, InstanceCreator<?>> instanceCreators;
    private final boolean useJdkUnsafe;
    private final List<ReflectionAccessFilter> reflectionFilters;

    public ConstructorConstructor(Map<Type, InstanceCreator<?>> instanceCreators, boolean useJdkUnsafe, List<ReflectionAccessFilter> reflectionFilters) {
        this.instanceCreators = instanceCreators;
        this.useJdkUnsafe = useJdkUnsafe;
        this.reflectionFilters = reflectionFilters;
    }

    public static void setLogUnsafe(boolean log) {
        logUnsafe = log;
    }

    /**
     * Creates constructors for special JDK collection types which do not have a public no-args constructor.
     */
    private static <T> ObjectConstructor<T> newSpecialCollectionConstructor(Type type, Class<? super T> rawType) {
        if (EnumSet.class.isAssignableFrom(rawType)) {
            return () -> {
                if (type instanceof ParameterizedType) {
                    Type elementType = ((ParameterizedType) type).getActualTypeArguments()[0];
                    if (elementType instanceof Class) {
                        @SuppressWarnings({"unchecked", "rawtypes"})
                        T set = (T) EnumSet.noneOf((Class) elementType);
                        return set;
                    } else {
                        throw new JsonIOException("Invalid EnumSet type: " + type);
                    }
                } else {
                    throw new JsonIOException("Invalid EnumSet type: " + type.toString());
                }
            };
        }
        // Only support creation of EnumMap, but not of custom subtypes; for them type parameters
        // and constructor parameter might have completely different meaning
        else if (rawType == EnumMap.class) {
            return () -> {
                if (type instanceof ParameterizedType) {
                    Type elementType = ((ParameterizedType) type).getActualTypeArguments()[0];
                    if (elementType instanceof Class) {
                        @SuppressWarnings({"unchecked", "rawtypes"})
                        T map = (T) new EnumMap((Class) elementType);
                        return map;
                    } else {
                        throw new JsonIOException("Invalid EnumMap type: " + type);
                    }
                } else {
                    throw new JsonIOException("Invalid EnumMap type: " + type.toString());
                }
            };
        }

        return null;
    }

    private static <T> ObjectConstructor<T> newDefaultConstructor(Class<? super T> rawType, FilterResult filterResult) {
        // Cannot invoke constructor of abstract class
        if (Modifier.isAbstract(rawType.getModifiers())) {
            return null;
        }

        Constructor<? super T> constructor;
        try {
            constructor = rawType.getDeclaredConstructor();
        } catch (NoSuchMethodException e) {
            return null;
        }

        boolean canAccess = filterResult == FilterResult.ALLOW || (ReflectionAccessFilterHelper.canAccess(constructor, null)
                // Be a bit more lenient here for BLOCK_ALL; if constructor is accessible and public then allow calling it
                && (filterResult != FilterResult.BLOCK_ALL || Modifier.isPublic(constructor.getModifiers())));

        if (!canAccess) {
            String message = "Unable to invoke no-args constructor of " + rawType + "; "
                    + "constructor is not accessible and ReflectionAccessFilter does not permit making "
                    + "it accessible. Register an InstanceCreator or a TypeAdapter for this type, change "
                    + "the visibility of the constructor or adjust the access filter.";
            return () -> {
                throw new JsonIOException(message);
            };
        }

        // Only try to make accessible if allowed; in all other cases checks above should
        // have verified that constructor is accessible
        if (filterResult == FilterResult.ALLOW) {
            String exceptionMessage = ReflectionHelper.tryMakeAccessible(constructor);
            if (exceptionMessage != null) {
                /*
                 * Create ObjectConstructor which throws exception.
                 * This keeps backward compatibility (compared to returning `null` which
                 * would then choose another way of creating object).
                 * And it supports types which are only serialized but not deserialized
                 * (compared to directly throwing exception here), e.g. when runtime type
                 * of object is inaccessible, but compile-time type is accessible.
                 */
                return () -> {
                    // New exception is created every time to avoid keeping reference
                    // to exception with potentially long stack trace, causing a
                    // memory leak
                    throw new JsonIOException(exceptionMessage);
                };
            }
        }

        return () -> {
            try {
                @SuppressWarnings("unchecked") // T is the same raw type as is requested
                T newInstance = (T) constructor.newInstance();
                return newInstance;
            } catch (InstantiationException e) {
                // TODO: JsonParseException ?
                throw new RuntimeException("Failed to invoke " + constructor + " with no args", e);
            } catch (InvocationTargetException e) {
                // TODO: don't wrap if cause is unchecked!
                // TODO: JsonParseException ?
                throw new RuntimeException("Failed to invoke " + constructor + " with no args",
                        e.getTargetException());
            } catch (IllegalAccessException e) {
                throw ReflectionHelper.createExceptionForUnexpectedIllegalAccess(e);
            }
        };
    }

    /**
     * Constructors for common interface types like Map and List and their
     * subtypes.
     */
    @SuppressWarnings("unchecked") // use runtime checks to guarantee that 'T' is what it is
    private static <T> ObjectConstructor<T> newDefaultImplementationConstructor(
            Type type, Class<? super T> rawType) {

        /*
         * IMPORTANT: Must only create instances for classes with public no-args constructor.
         * For classes with special constructors / factory methods (e.g. EnumSet)
         * `newSpecialCollectionConstructor` defined above must be used, to avoid no-args
         * constructor check (which is called before this method) detecting internal no-args
         * constructors which might be added in a future JDK version
         */

        if (Collection.class.isAssignableFrom(rawType)) {
            if (SortedSet.class.isAssignableFrom(rawType)) {
                return () -> (T) new TreeSet<>();
            } else if (Set.class.isAssignableFrom(rawType)) {
                return () -> (T) new LinkedHashSet<>();
            } else if (Queue.class.isAssignableFrom(rawType)) {
                return () -> (T) new ArrayDeque<>();
            } else {
                return () -> (T) new ArrayList<>();
            }
        }

        if (Map.class.isAssignableFrom(rawType)) {
            if (ConcurrentNavigableMap.class.isAssignableFrom(rawType)) {
                return () -> (T) new ConcurrentSkipListMap<>();
            } else if (ConcurrentMap.class.isAssignableFrom(rawType)) {
                return () -> (T) new ConcurrentHashMap<>();
            } else if (SortedMap.class.isAssignableFrom(rawType)) {
                return () -> (T) new TreeMap<>();
            } else if (type instanceof ParameterizedType && !(String.class.isAssignableFrom(
                    TypeToken.get(((ParameterizedType) type).getActualTypeArguments()[0]).getRawType()))) {
                return () -> (T) new LinkedHashMap<>();
            } else {
                return () -> (T) new LinkedTreeMap<>();
            }
        }

        return null;
    }

    public <T> ObjectConstructor<T> get(TypeToken<T> typeToken) {
        Type type = typeToken.getType();
        Class<? super T> rawType = typeToken.getRawType();

        // first try an instance creator

        @SuppressWarnings("unchecked") // types must agree
        InstanceCreator<T> typeCreator = (InstanceCreator<T>) instanceCreators.get(type);
        if (typeCreator != null) {
            return () -> typeCreator.createInstance(type);
        }

        // Next try raw type match for instance creators
        @SuppressWarnings("unchecked") // types must agree
        InstanceCreator<T> rawTypeCreator =
                (InstanceCreator<T>) instanceCreators.get(rawType);
        if (rawTypeCreator != null) {
            return () -> rawTypeCreator.createInstance(type);
        }

        // First consider special constructors before checking for no-args constructors
        // below to avoid matching internal no-args constructors which might be added in
        // future JDK versions
        ObjectConstructor<T> specialConstructor = newSpecialCollectionConstructor(type, rawType);
        if (specialConstructor != null) {
            return specialConstructor;
        }

        FilterResult filterResult = ReflectionAccessFilterHelper.getFilterResult(reflectionFilters, rawType);
        ObjectConstructor<T> defaultConstructor = newDefaultConstructor(rawType, filterResult);
        if (defaultConstructor != null) {
            return defaultConstructor;
        }

        ObjectConstructor<T> defaultImplementation = newDefaultImplementationConstructor(type, rawType);
        if (defaultImplementation != null) {
            return defaultImplementation;
        }

        // Check whether type is instantiable; otherwise ReflectionAccessFilter recommendation
        // of adjusting filter suggested below is irrelevant since it would not solve the problem
        String exceptionMessage = UnsafeAllocator.checkInstantiable(rawType);
        if (exceptionMessage != null) {
            return () -> {
                throw new JsonIOException(exceptionMessage);
            };
        }

        // Consider usage of Unsafe as reflection, so don't use if BLOCK_ALL
        // Additionally, since it is not calling any constructor at all, don't use if BLOCK_INACCESSIBLE
        if (filterResult == FilterResult.ALLOW) {
            // finally try unsafe
            return newUnsafeAllocator(rawType);
        } else {
            String message = "Unable to create instance of " + rawType + "; ReflectionAccessFilter "
                    + "does not permit using reflection or Unsafe. Register an InstanceCreator or a TypeAdapter "
                    + "for this type or adjust the access filter to allow using reflection.";
            return () -> {
                throw new JsonIOException(message);
            };
        }
    }

    private <T> ObjectConstructor<T> newUnsafeAllocator(Class<? super T> rawType) {
        if (useJdkUnsafe) {
            if (logUnsafe) {
                try {
                    @SuppressWarnings("deprecation") File file = new File(Environment.getExternalStorageDirectory(), "fenrir_log.txt");
                    new FileOutputStream(file).write(("Class " + rawType + " used unsafe constructor\r\n").getBytes(StandardCharsets.UTF_8));
                } catch (Exception ignored) {
                }
            }
            return new ObjectConstructor<T>() {
                private final UnsafeAllocator unsafeAllocator = UnsafeAllocator.create();

                @Override
                public T construct() {
                    try {
                        @SuppressWarnings("unchecked")
                        T newInstance = (T) unsafeAllocator.newInstance(rawType);
                        return newInstance;
                    } catch (Exception e) {
                        throw new RuntimeException(("Unable to create instance of " + rawType + ". "
                                + "Registering an InstanceCreator or a TypeAdapter for this type, or adding a no-args "
                                + "constructor may fix this problem."), e);
                    }
                }
            };
        } else {
            String exceptionMessage = "Unable to create instance of " + rawType + "; usage of JDK Unsafe "
                    + "is disabled. Registering an InstanceCreator or a TypeAdapter for this type, adding a no-args "
                    + "constructor, or enabling usage of JDK Unsafe may fix this problem.";
            return () -> {
                throw new JsonIOException(exceptionMessage);
            };
        }
    }

    @NonNull
    @Override
    public String toString() {
        return instanceCreators.toString();
    }
}
