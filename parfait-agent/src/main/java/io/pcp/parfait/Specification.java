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

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;

import systems.uom.quantity.Information;
import tec.uom.se.unit.MetricPrefix;
import tec.uom.se.unit.Units;
import static tec.uom.se.AbstractUnit.ONE;

import javax.measure.Unit;
import javax.measure.quantity.Time;

public class Specification {
    public String name;
    public String description;
    public Unit<?> unit = ONE;
    public ValueSemantics semantics = ValueSemantics.FREE_RUNNING;
    public String mBeanName;
    public String mBeanAttributeName;
    public String mBeanCompositeDataItem;
    
    public Specification() {
    }

    private Specification(String name, String description,
                String semantics, String units, String mBeanName,
                String mBeanAttributeName, String mBeanCompositeDataItem) {
        this.name = name;
        this.description = description;
        this.mBeanName = mBeanName;
        if (!units.isEmpty())
            this.unit = parseUnits(units);
        if (!semantics.isEmpty())
            this.semantics = parseSemantics(semantics);
        if (!mBeanAttributeName.isEmpty())
            this.mBeanAttributeName = mBeanAttributeName;
        if (!mBeanCompositeDataItem.isEmpty())
            this.mBeanCompositeDataItem = mBeanCompositeDataItem;
    }

    public Specification(JsonNode node) {
        this(node.path("name").asText(),
             node.path("description").asText(),
             node.path("semantics").asText(),
             node.path("units").asText(),
             node.path("mBeanName").asText(),
             node.path("mBeanAttributeName").asText(),
             node.path("mBeanCompositeDataItem").asText());
    }

    public ValueSemantics getSemantics() {
        return semantics;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Unit<?> getUnits() {
        return unit;
    }

    public String getMBeanName() {
        return mBeanName;
    }

    public String getMBeanAttributeName() {
        return mBeanAttributeName;
    }

    public String getMBeanCompositeDataItem() {
        return mBeanCompositeDataItem;
    }

    public ValueSemantics parseSemantics(String semantics) {
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
            // TODO: else throw ParserException
        }
        return ValueSemantics.FREE_RUNNING;
    }

    public Unit<?> parseUnits(String units) {
        if (units.equalsIgnoreCase("milliseconds")) {
            Unit<Time> MILLISECONDS = MetricPrefix.MILLI(Units.SECOND);
            return MILLISECONDS;
        }
        if (units.equalsIgnoreCase("bytes")) {
            Unit<Information> BYTE = systems.uom.unicode.CLDR.BYTE;
            return BYTE;
        }
        if (!units.isEmpty()) {
           // TODO: throw ParserException
        }
        return ONE;

// TODO: uom SimpleUnitFormat?  Something else?
//        return [AbstractUnit].parse(units);

    }
}
