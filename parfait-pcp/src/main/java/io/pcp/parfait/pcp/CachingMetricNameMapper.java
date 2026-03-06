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

package io.pcp.parfait.pcp;

import io.pcp.parfait.dxm.MetricName;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Wrapper around a MetricNameMapper that caches the metric name string to MetricName object mapping.
 * This cache is intended to improve performance by removing the need to parse a metric name every time it's
 * mapped to a MetricName object.
 */
public class CachingMetricNameMapper implements MetricNameMapper {

    private final Map<String, MetricName> cache = new ConcurrentHashMap<>();
    private final MetricNameMapper innerMapper;

    public CachingMetricNameMapper(MetricNameMapper mapper) {
        this.innerMapper = mapper;
    }

    @Override
    public MetricName map(String name) {
        return cache.computeIfAbsent(name, innerMapper::map);
    }
}
