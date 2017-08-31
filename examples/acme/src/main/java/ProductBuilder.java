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

import io.pcp.parfait.MonitoredCounter;
import java.util.concurrent.ThreadLocalRandom;

import static tec.uom.se.unit.MetricPrefix.MILLI;
import static tec.uom.se.unit.Units.SECOND;

public class ProductBuilder extends Thread {
    private MonitoredCounter completed;
    private MonitoredCounter failures;
    private MonitoredCounter totalTime;
    private Integer bound;
    private String name;

    ProductBuilder(String name) {
        this.name = name;
        this.bound = 500;
        this.completed = new MonitoredCounter("products[" + name + "].count",
                "Acme factory product throughput");
        this.failures = new MonitoredCounter("products[" + name + "].fails",
                "Count of failures to complete an Acme product build.");
        this.totalTime = new MonitoredCounter("products[" + name + "].time",
                "Cumulative machine time spent producing Acme products.",
                MILLI(SECOND));
    }

    public void difficulty(Integer bound) {
        this.bound = bound;
    }

    private void build() {
        // emulate the work of building this product, by sleeping
        Integer elapsed = ThreadLocalRandom.current().nextInt(0, this.bound);
        try {
            sleep(elapsed);
            // increase counters for this product, another completed!
            totalTime.inc(elapsed);
            completed.inc();
        } catch (InterruptedException e) {
            failures.inc();
        }
    }

    @Override
    public void run() {
        while (true) {
            build();
            System.out.format("Built %d %s\n", completed.get(), name);
        }
    }
}
