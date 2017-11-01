/*
 * Copyright 2009-2017 Red Hat Inc.
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
 * General Specification parsing error exceptions.
 */
@SuppressWarnings("serial")
public class SpecificationException extends RuntimeException {
    /**
     * Constructor for SpecificationException.
     * @param name the metric being parsed
     * @param details the detailed message
     */
    public SpecificationException(String name, String details) {
        super("Metric " + name + ": " + details);
    }

    /**
     * Constructor for SpecificationException.
     * @param msg the detail message
     * @param cause the root cause (raw Java exception)
     */
    public SpecificationException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
