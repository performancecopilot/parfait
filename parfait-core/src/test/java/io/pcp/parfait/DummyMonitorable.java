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

import static tec.uom.se.AbstractUnit.ONE;

import javax.measure.Unit;

public class DummyMonitorable implements Monitorable<String> {
    private final String name;

    public DummyMonitorable(String name) {
        this.name = name;
    }

    @Override
    public String get() {
        return "DummyValue";
    }

    @Override
    public String getDescription() {
        return "Blah";
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public ValueSemantics getSemantics() {
        return ValueSemantics.CONSTANT;
    }

    @Override
    public Class<String> getType() {
        return String.class;
    }

    @Override
    public Unit<?> getUnit() {
        return ONE;
    }

    @Override
    public void attachMonitor(Monitor m) {
    }

    @Override
    public void removeMonitor(Monitor m) {
    }

}
