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

/**
 * The strategy used to retrieve the metric descriptor for metrics being adapted
 */
public interface MetricDescriptorLookup {

    /**
     * Get the metric descriptor for the specified metric
     *
     * @param metricName The name the metric is publish under in Dropwizard
     * @return The MetricDescriptor for that metric
     */
    MetricDescriptor getDescriptorFor(String metricName);
}
