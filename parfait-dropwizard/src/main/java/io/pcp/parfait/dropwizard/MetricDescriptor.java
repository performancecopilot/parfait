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

package io.pcp.parfait.dropwizard;

import javax.measure.Unit;

import io.pcp.parfait.ValueSemantics;

/**
 * The metadata published with a metric in Parfait
 */
public interface MetricDescriptor {

    /**
     * The unit of the metric
     *
     * @return The JSR-363 unit
     */
    Unit getUnit();

    /**
     * The human-readable description of the metric
     *
     * @return The description
     */
    String getDescription();

    /**
     * The ValueSemantics of the metric
     *
     * @return the ValueSemantics
     * @see io.pcp.parfait.ValueSemantics
     */
    ValueSemantics getSemantics();
}
