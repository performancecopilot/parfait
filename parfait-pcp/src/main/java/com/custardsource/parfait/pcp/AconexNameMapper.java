package com.custardsource.parfait.pcp;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.ImmutableList;

/**
 * Does Aconex-specific replacements of names. Replacement patterns may contain
 * 
 * @author pcowan
 */
public class AconexNameMapper extends RegexSequenceNameMapper {
    private static final Pattern CONTROLLER_PATTERN = Pattern
            .compile("aconex.controllers.([^\\.]+)\\.(.*)");
    private static final String CONTROLLER_REPLACEMENT = "aconex.controllers[%P$1].$2";

    private static final Pattern EMAIL_SENT_PATTERN = Pattern
            .compile("aconex.email.sent.([^\\.]+)\\.(.*)");
    private static final String EMAIL_SENT_REPLACEMENT = "aconex.email.sent[%P$1].$2";

    private static final Pattern CACHE_PATTERN = Pattern.compile("aconex.cache.([^\\.]+)\\.(.*)");
    private static final String CACHE_REPLACEMENT = "aconex.cache[%P$1].$2";

    private static final Pattern SESSION_PATTERN = Pattern
            .compile("aconex.sessions.([^\\.]+)\\.(.*)");
    private static final String SESSION_REPLACEMENT = "aconex.sessions.bin[%P$1].$2";

    private static final Pattern SPACEUSED_PATTERN = Pattern.compile("aconex.spaceused.([^\\.]+)\\.(.*)");
    private static final String SPACEUSED_REPLACEMENT = "aconex.spaceused[%P$1].$2";

    private static final Pattern KEYPOOL_PATTERN = Pattern.compile("aconex\\.keypool\\.(.*)");
    private static final String KEYPOOL_REPLACEMENT = "aconex.keypool[%P$1]";

    private static final Pattern GENERIC_PATTERN = Pattern.compile("aconex.([^\\[]+)");
    private static final String GENERIC_REPLACEMENT = "aconex[%I].$1";

    public AconexNameMapper(String instance) {
        super(getReplacements(instance));
    }

    private static List<Replacement> getReplacements(String instance) {
        Replacement controllerReplacement = new Replacement(CONTROLLER_PATTERN,
                replacementPatternFor(CONTROLLER_REPLACEMENT, instance));
        Replacement emailReplacement = new Replacement(EMAIL_SENT_PATTERN, replacementPatternFor(
                EMAIL_SENT_REPLACEMENT, instance));
        Replacement cacheReplacement = new Replacement(CACHE_PATTERN, replacementPatternFor(
                CACHE_REPLACEMENT, instance));
        Replacement sessionReplacement = new Replacement(SPACEUSED_PATTERN, replacementPatternFor(
                SPACEUSED_REPLACEMENT, instance));
        Replacement spaceUsedReplacement = new Replacement(SESSION_PATTERN, replacementPatternFor(
                SESSION_REPLACEMENT, instance));
        Replacement keypoolReplacement = new Replacement(KEYPOOL_PATTERN, replacementPatternFor(
                KEYPOOL_REPLACEMENT, instance));
        Replacement genericReplacement = new Replacement(GENERIC_PATTERN, replacementPatternFor(
                GENERIC_REPLACEMENT, instance));

        return ImmutableList.of(controllerReplacement, emailReplacement, cacheReplacement,
                spaceUsedReplacement, sessionReplacement, keypoolReplacement, genericReplacement);
    }

    private static String replacementPatternFor(String rawReplacement, String instance) {
        String prefix = instance + "/";
        String result = rawReplacement.replaceAll(Pattern.quote("%P"), Matcher
                .quoteReplacement(prefix));
        result = result.replaceAll(Pattern.quote("%I"), Matcher.quoteReplacement(instance));
        return result;
    }
}
