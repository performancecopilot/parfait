package io.pcp.parfait.pcp;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.pcp.parfait.dxm.MetricName;

public class RegexSequenceNameMapper implements MetricNameMapper {
	private final List<Replacement> replacements; 
	
	public RegexSequenceNameMapper(List<Replacement> replacements) {
		this.replacements = new ArrayList<Replacement>(replacements);
	}
	
	@Override
	public MetricName map(String name) {
		for (Replacement replacement : replacements) {
			Matcher m = replacement.pattern.matcher(name);
			if (m.matches()) {
				name = m.replaceAll(replacement.replacement);
			}
			
		}
		return MetricName.parse(name);
	}
	
	public static class Replacement {
		private final Pattern pattern;
		private final String replacement;
		
		public Replacement(Pattern pattern, String replacement) {
			this.pattern = pattern;
			this.replacement = replacement;
		}
	}
}
