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
 * MetricNameTranslators can be provided to {@link io.pcp.parfait.dropwizard.MetricAdapterFactoryImpl}
 * to translate the metric names originating from Dropwizard to those that will be published in Parfait.
 */
public interface MetricNameTranslator {

    /**
     * Translate a metric name into the name the metric will be published under in Parfait
     *
     * @param name The metric name provided by Dropwizard
     * @return The name to use to publish the metric in Parfait
     */
    String translate(String name);
}
