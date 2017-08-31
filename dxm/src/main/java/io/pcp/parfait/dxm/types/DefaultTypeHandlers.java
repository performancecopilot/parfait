/*
 * Copyright 2009-2017 Aconex
 *
 * Licensed under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.  See the License for the specific language governing
 * permissions and limitations under the License.
 */

package io.pcp.parfait.dxm.types;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;


/**
 * A set of convenience {@link TypeHandler TypeHandlers} for converting numeric and other basic Java
 * types to standard PCP types.
 * 
 * @author Cowan
 */
public class DefaultTypeHandlers {
    /**
     * Converts a {@link Number} into a PCP signed 32-bit integer. May cause loss of precision if
     * the supplied Number can not be reasonably coerced into an integer.
     */
    public static final TypeHandler<Number> NUMBER_AS_INTEGER = new AbstractTypeHandler<Number>(
            MmvMetricType.I32, 4) {
        public void putBytes(ByteBuffer buffer, Number value) {
            buffer.putInt(value == null ? 0 : value.intValue());
        }
    };

    /**
     * Converts a {@link Number} into a PCP signed 64-bit integer. May cause loss of precision if
     * the supplied Number has a floating-point component.
     */
    public static final TypeHandler<Number> NUMBER_AS_LONG = new AbstractTypeHandler<Number>(
            MmvMetricType.I64, 8) {
        public void putBytes(ByteBuffer buffer, Number value) {
            buffer.putLong(value == null ? 0 : value.longValue());
        }
    };

    /**
     * Converts a {@link Number} into a PCP double. May cause loss of precision if the supplied
     * Number is not a double.
     */
    public static final TypeHandler<Number> NUMBER_AS_DOUBLE = new AbstractTypeHandler<Number>(
            MmvMetricType.DOUBLE, 8) {
        public void putBytes(ByteBuffer buffer, Number value) {
            buffer.putDouble(value == null ? 0 : value.doubleValue());
        }
    };

    /**
     * Converts a {@link Number} into a PCP double. May cause loss of precision if the supplied
     * Number is not a float.
     */
    public static final TypeHandler<Number> NUMBER_AS_FLOAT = new AbstractTypeHandler<Number>(
            MmvMetricType.DOUBLE, 8) {
        public void putBytes(ByteBuffer buffer, Number value) {
            buffer.putFloat(value == null ? 0 : value.floatValue());
        }
    };

    /**
     * Converts a {@link Boolean} into a PCP signed 32-bit integer.
     */
    public static final TypeHandler<Boolean> BOOLEAN_AS_INT = new AbstractTypeHandler<Boolean>(
            MmvMetricType.I32, 4) {
        public void putBytes(ByteBuffer buffer, Boolean value) {
            buffer.putInt(value == null ? 0 : (value ? 1 : 0));
        }
    };

    /**
     * Converts an {@link AtomicBoolean} into a PCP unsigned 32-bit integer.
     */
    public static final TypeHandler<AtomicBoolean> ATOMIC_BOOLEAN_AS_INT = new AbstractTypeHandler<AtomicBoolean>(
            MmvMetricType.I32, 4) {
        public void putBytes(ByteBuffer buffer, AtomicBoolean value) {
            buffer.putInt(value == null ? 0 : (value.get() ? 1 : 0));
        }
    };

    /**
     * @return a standard set of handlers to convert some common Java types (including the
     *         java.util.concurrent Atomic primitives) to the most suitable default TypeHandler,
     *         without a loss of precision.
     */
    public static Map<Class<?>, TypeHandler<?>> getDefaultMappings() {
        Map<Class<?>, TypeHandler<?>> mappings = new HashMap<Class<?>, TypeHandler<?>>();
        mappings.put(Integer.class, NUMBER_AS_INTEGER);
        mappings.put(AtomicInteger.class, NUMBER_AS_INTEGER);
        mappings.put(Long.class, NUMBER_AS_LONG);
        mappings.put(AtomicLong.class, NUMBER_AS_LONG);
        mappings.put(Short.class, NUMBER_AS_INTEGER);
        mappings.put(Byte.class, NUMBER_AS_INTEGER);
        mappings.put(Boolean.class, BOOLEAN_AS_INT);
        mappings.put(AtomicBoolean.class, ATOMIC_BOOLEAN_AS_INT);
        mappings.put(Double.class, NUMBER_AS_DOUBLE);
        mappings.put(Float.class, NUMBER_AS_FLOAT);
        return mappings;
    }
}
