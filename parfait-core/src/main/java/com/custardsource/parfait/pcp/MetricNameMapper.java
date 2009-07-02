package com.custardsource.parfait.pcp;

import com.custardsource.parfait.dxm.MetricName;

/**
 * Maps a String to a PCP {@link MetricName}.
 */
public interface MetricNameMapper {
	MetricName map(String name);
	
	static final MetricNameMapper PASSTHROUGH_MAPPER = new MetricNameMapper() {
		@Override
		public MetricName map(String name) {
			return MetricName.parse(name);
		}
	};
}
