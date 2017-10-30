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

import static tec.uom.se.AbstractUnit.ONE;

import javax.measure.Unit;
import javax.measure.quantity.Time;

import systems.uom.quantity.Information;
import tec.uom.se.unit.MetricPrefix;
import tec.uom.se.unit.Units;

class Specification {
    private final String name;
    private final boolean optional;
    private final String description;
    private final Unit<?> unit;
    private final ValueSemantics semantics;
    private final String mBeanName;
    private final String mBeanAttributeName;
    private final String mBeanCompositeDataItem;

    Specification(String name, boolean optional, String description,
                String semantics, String units, String mBeanName,
                String mBeanAttributeName, String mBeanCompositeDataItem) {
        this.name = name;
        this.optional = optional;
        this.description = description;
        this.mBeanName = mBeanName;
        this.unit = parseUnits(name, units);
        this.semantics = parseSemantics(name, semantics);
        this.mBeanAttributeName = mBeanAttributeName;
        this.mBeanCompositeDataItem = mBeanCompositeDataItem;
    }

    ValueSemantics getSemantics() {
        return semantics;
    }

    String getName() {
        return name;
    }

    boolean getOptional() {
        return optional;
    }

    String getDescription() {
        return description;
    }

    Unit<?> getUnits() {
        return unit;
    }

    String getMBeanName() {
        return mBeanName;
    }

    String getMBeanAttributeName() {
        return mBeanAttributeName;
    }

    String getMBeanCompositeDataItem() {
        return mBeanCompositeDataItem;
    }

    private ValueSemantics parseSemantics(String name, String semantics) {
        if (!semantics.isEmpty()) {
            if (semantics.equalsIgnoreCase("constant") ||
                semantics.equalsIgnoreCase("discrete"))
                return ValueSemantics.CONSTANT;
            else if (semantics.equalsIgnoreCase("count") ||
                     semantics.equalsIgnoreCase("counter"))
                return ValueSemantics.MONOTONICALLY_INCREASING;
            else if (semantics.equalsIgnoreCase("gauge") ||
                     semantics.equalsIgnoreCase("instant") ||
                     semantics.equalsIgnoreCase("instantaneous"))
                return ValueSemantics.FREE_RUNNING;
            throw new SpecificationException(name, "Unexpected semantics [" + semantics + "]");
        }
        return ValueSemantics.FREE_RUNNING;
    }

    private Unit<?> parseUnits(String name, String units) {
        if (units.equalsIgnoreCase("milliseconds")) {
            Unit<Time> MILLISECONDS = MetricPrefix.MILLI(Units.SECOND);
            return MILLISECONDS;
        }
        if (units.equalsIgnoreCase("bytes")) {
            Unit<Information> BYTE = systems.uom.unicode.CLDR.BYTE;
            return BYTE;
        }
        if (!units.isEmpty()) {
            String msg = "Unexpected units [" + units + "]";
            throw new SpecificationException(name, msg);
        }
        return ONE;
    }
}
