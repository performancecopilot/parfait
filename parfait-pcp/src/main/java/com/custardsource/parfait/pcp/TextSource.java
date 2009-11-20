package com.custardsource.parfait.pcp;

import com.custardsource.parfait.Monitorable;
import com.custardsource.parfait.dxm.MetricName;

public interface TextSource {
    public String getText(Monitorable<?> monitorable, MetricName mappedName);
}
