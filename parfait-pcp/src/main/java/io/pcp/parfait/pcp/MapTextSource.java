package io.pcp.parfait.pcp;

import java.util.Map;

import io.pcp.parfait.Monitorable;
import io.pcp.parfait.dxm.MetricName;

public class MapTextSource implements TextSource {
    private final TextSource fallback;
    private final Map<String, String> descriptionsByMetricName;

    public MapTextSource(TextSource fallback, Map<String, String> descriptionsByMetricName) {
        this.fallback = fallback;
        this.descriptionsByMetricName = descriptionsByMetricName;
    }

    @Override
    public String getText(Monitorable<?> monitorable, MetricName mappedName) {
        String override = descriptionsByMetricName.get(mappedName.getMetric());
        return override == null ? fallback.getText(monitorable, mappedName) : override;
    }

}
