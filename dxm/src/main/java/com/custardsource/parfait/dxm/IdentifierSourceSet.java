package com.custardsource.parfait.dxm;

public interface IdentifierSourceSet {
    public static IdentifierSourceSet DEFAULT_SET = new IdentifierSourceSet() {
        private final IdentifierSource instanceDomainSource = new HashingIdentifierSource(1 << 22);
        private final IdentifierSource instanceSource = new HashingIdentifierSource(Integer.MAX_VALUE);
        private final IdentifierSource metricSource = new HashingIdentifierSource(1 << 10);
        
        @Override
        public IdentifierSource metricSource() {
            return metricSource;
        }
        
        @Override
        public IdentifierSource instanceSource(String domain) {
            return instanceSource;
        }
        
        @Override
        public IdentifierSource instanceDomainSource() {
            return instanceDomainSource;
        }
    };
    
    /**
     * {@link IdentifierSourceSet} which will fail for indoms and metrics, but allow default hashed
     * values for Instances
     */
    public static IdentifierSourceSet EXPLICIT_SET = new IdentifierSourceSet() {
        private final IdentifierSource DEFAULT_SOURCE = new HashingIdentifierSource(Integer.MAX_VALUE);
        private final IdentifierSource ERROR_SOURCE = new ErrorThrowingIdentifierSource();
        
        @Override
        public IdentifierSource metricSource() {
            return ERROR_SOURCE;
        }
        
        @Override
        public IdentifierSource instanceSource(String domain) {
            return DEFAULT_SOURCE;
        }
        
        @Override
        public IdentifierSource instanceDomainSource() {
            return ERROR_SOURCE;
        }
    };
    
    /**
     * An IdentifierSourceSet for use in contexts where identifiers truly do not matter and are not limited in range
     * -- e.g. test cases or writers which do not need to limit identifiers
     */
    public static IdentifierSourceSet LIMITLESS_SET = new IdentifierSourceSet() {
        private final IdentifierSource defaultSource = new HashingIdentifierSource(Integer.MAX_VALUE);

        @Override
        public IdentifierSource metricSource() {
            return defaultSource;
        }

        @Override
        public IdentifierSource instanceSource(String domain) {
            return defaultSource;
        }

        @Override
        public IdentifierSource instanceDomainSource() {
            return defaultSource;
        }
    };
    
    public IdentifierSource instanceDomainSource();
    public IdentifierSource instanceSource(String domain);
    public IdentifierSource metricSource();
    
}
