package io.pcp.parfait.pcp;

import io.pcp.parfait.dxm.MetricName;

/**
 * Maps a String to a PCP {@link MetricName}.
 */
public interface MetricNameMapper {
	static final MetricNameMapper PASSTHROUGH_MAPPER = new MetricNameMapper() {
		@Override
		public MetricName map(String name) {
			return MetricName.parse(name);
		}
	};
	
	MetricName map(String name);
}
