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

import static tech.units.indriya.AbstractUnit.ONE;

import io.pcp.parfait.ValueSemantics;

import javax.measure.quantity.Dimensionless;
import javax.measure.Unit;

public class DefaultMetricDescriptorLookup implements MetricDescriptorLookup {

    @Override
    public MetricDescriptor getDescriptorFor(final String metricName) {
        return new MetricDescriptor() {
            @Override
            public Unit<Dimensionless> getUnit() {
                return ONE;
            }

            @Override
            public String getDescription() {
                return metricName;
            }

            @Override
            public ValueSemantics getSemantics() {
                return ValueSemantics.FREE_RUNNING;
            }
        };
    }
}
