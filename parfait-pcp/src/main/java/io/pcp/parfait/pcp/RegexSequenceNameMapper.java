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
