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

package io.pcp.parfait;

/**
 * Possible semantics of the individual values of a metric. This is used by some
 * output mechanisms to associate metadata with a particular value, do rate
 * conversion, etc etc.
 */
public enum ValueSemantics {
    /**
     * A value which never (or very seldom) changes over the lifetime of a
     * virtual machine. Examples might be maximum heap size, JVM version, etc.
     */
    CONSTANT,
    /**
     * Values which freely and arbitrarily change over time, either upwards or
     * downwards. Examples might include Java heap memory in use, number of
     * currently-logged-on users, etc.
     */
    FREE_RUNNING,
    /**
     * Values which only ever increase over time during the life of a metric.
     * Examples may include total number of Java Garbage Collections executed,
     * or the total number of users to have logged on to the application since
     * startup.
     */
    MONOTONICALLY_INCREASING;
}
