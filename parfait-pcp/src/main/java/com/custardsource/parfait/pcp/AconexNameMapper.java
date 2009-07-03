package com.custardsource.parfait.pcp;

import java.util.List;
import java.util.regex.Pattern;

import com.google.common.collect.ImmutableList;

public class AconexNameMapper extends RegexSequenceNameMapper {
	private static final Pattern CONTROLLER_PATTERN = Pattern
			.compile("aconex.controllers.([^\\.]+)\\.(.*)");
	private static final String CONTROLLER_REPLACEMENT = "aconex.controllers[$1].$2";

	public AconexNameMapper(List<Replacement> replacements) {
		super(ImmutableList.of(new Replacement(CONTROLLER_PATTERN,
				CONTROLLER_REPLACEMENT)));
	}

}
