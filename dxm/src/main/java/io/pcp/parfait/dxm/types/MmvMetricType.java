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


/**
 * Metric types as represented in mmv_stats.h (which in turn parallel pmapi.h)
 * 
 * @author Cowan
 */
public enum MmvMetricType {
    NOT_SUPPORTED(-1, "unsupported"),
    I32(0, "int"),
    U32(1, "uint"),
    I64(2, "long"),
    U64(3, "ulong"),
    FLOAT(4, "float"),
    DOUBLE(5, "double"),
    STRING(6, "string");

    private final int identifier;
	private final String description;

    private MmvMetricType(int identifier, String description) {
        this.identifier = identifier;
        this.description = description;
    }

    public int getIdentifier() {
        return this.identifier;
    }

	public String getDescription() {
		return description;
	}
}