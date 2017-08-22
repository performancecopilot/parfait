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

public class Counter implements Runnable {
    private MonitoredCounter counter;
    private int time = 1000;

    Counter () {
        counter = new MonitoredCounter("example.counter", "A simple Counter that increments once per second");
    }

    public void run () {
        try {
            while (true) {
                counter.inc();
                System.out.println("Counter set to: " + counter.get());
                Thread.sleep(time);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
