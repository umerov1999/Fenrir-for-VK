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

import com.google.gson.InstanceCreator;
import com.google.gson.JsonIOException;
import com.google.gson.internal.reflect.ReflectionHelper;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
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

    public ConstructorConstructor(Map<Type, InstanceCreator<?>> instanceCreators, boolean useJdkUnsafe) {
        this.instanceCreators = instanceCreators;
        this.useJdkUnsafe = useJdkUnsafe;
    }

    public static void setLogUnsafe(boolean log) {
        logUnsafe = log;
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

        ObjectConstructor<T> defaultConstructor = newDefaultConstructor(rawType);
        if (defaultConstructor != null) {
            return defaultConstructor;
        }

        ObjectConstructor<T> defaultImplementation = newDefaultImplementationConstructor(type, rawType);
        if (defaultImplementation != null) {
            return defaultImplementation;
        }
        if (logUnsafe) {
            try {
                @SuppressWarnings("deprecation") File file = new File(Environment.getExternalStorageDirectory(), "fenrir_log.txt");
                new FileOutputStream(file).write(("Class " + rawType + " used unsafe constructor\r\n").getBytes(StandardCharsets.UTF_8));
            } catch (Exception ignored) {
            }
        }
        // finally try unsafe
        return newUnsafeAllocator(rawType);
    }

    private <T> ObjectConstructor<T> newDefaultConstructor(Class<? super T> rawType) {
        Constructor<? super T> constructor;
        try {
            constructor = rawType.getDeclaredConstructor();
        } catch (NoSuchMethodException e) {
            return null;
        }

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
                throw new AssertionError(e);
            }
        };
    }

    /**
     * Constructors for common interface types like Map and List and their
     * subtypes.
     */
    @SuppressWarnings("unchecked") // use runtime checks to guarantee that 'T' is what it is
    private <T> ObjectConstructor<T> newDefaultImplementationConstructor(
            Type type, Class<? super T> rawType) {
        if (Collection.class.isAssignableFrom(rawType)) {
            if (SortedSet.class.isAssignableFrom(rawType)) {
                return () -> (T) new TreeSet<>();
            } else if (EnumSet.class.isAssignableFrom(rawType)) {
                return () -> {
                    if (type instanceof ParameterizedType) {
                        Type elementType = ((ParameterizedType) type).getActualTypeArguments()[0];
                        if (elementType instanceof Class) {
                            return (T) EnumSet.noneOf((Class) elementType);
                        } else {
                            throw new JsonIOException("Invalid EnumSet type: " + type);
                        }
                    } else {
                        throw new JsonIOException("Invalid EnumSet type: " + type);
                    }
                };
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
                return () -> (T) new LinkedTreeMap<String, Object>();
            }
        }

        return null;
    }

    private <T> ObjectConstructor<T> newUnsafeAllocator(Class<? super T> rawType) {
        if (useJdkUnsafe) {
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

    @Override
    public String toString() {
        return instanceCreators.toString();
    }
}
