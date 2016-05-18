/**
 * 
 */
package io.pcp.parfait.pcp;

import io.pcp.parfait.Monitorable;
import io.pcp.parfait.dxm.MetricName;

public final class EmptyTextSource implements TextSource {
    @Override
    public String getText(Monitorable<?> monitorable, MetricName mappedName) {
        return "";
    }
}