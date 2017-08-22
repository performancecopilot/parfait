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

/**
 * A monitor is notified of any changes to the value of any {@link Monitorable}
 * objects it is attached to. This makes it effectively an 'output sink' for all
 * monitorable changes. When a Monitorable changes value, it will notify all
 * Monitors via {@link #valueChanged(Monitorable)}. Note that Monitors are under
 * no obligation to process this immediately - they may elect to queue the
 * notification for later update, swallow intermediate updates, etc., depending
 * on implementation.
 */
public interface Monitor {

    /**
     * Notifies the Monitor about a change in the underlying value of a
     * {@link Monitorable}. It is not guaranteed that the value obtained by
     * {@link Monitorable#get()} will return the value that triggered the
     * update, as the value may update in the meantime.
     * 
     * @param monitorable
     *            the Monitorable whose value has changed.
     */
    void valueChanged(Monitorable<?> monitorable);

}
