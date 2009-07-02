package com.custardsource.parfait.pcp;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.custardsource.parfait.dxm.MetricName;

public class AconexNameMapper implements MetricNameMapper {
	private static final Pattern CONTROLLER_PATTERN = Pattern.compile("aconex.controllers.([^\\.]+)\\.(.*)");
	
	@Override
	public MetricName map(String name) {
		Matcher m = CONTROLLER_PATTERN.matcher(name);
		if (m.matches()) {
			name = m.replaceAll("aconex.controllers[$1].$2");
		}
		return MetricName.parse(name);
	}
}
