package com.custardsource.parfait.dxm;

public interface IdentifierSourceSet {
    public IdentifierSource instanceDomainSource();
    public IdentifierSource instanceSource(String domain);
    public IdentifierSource metricSource();
    
    public static IdentifierSourceSet DEFAULT_SET = new IdentifierSourceSet() {
        private final IdentifierSource DEFAULT_SOURCE = new HashingIdentifierSource();
        
        @Override
        public IdentifierSource metricSource() {
            return DEFAULT_SOURCE;
        }
        
        @Override
        public IdentifierSource instanceSource(String domain) {
            return DEFAULT_SOURCE;
        }
        
        @Override
        public IdentifierSource instanceDomainSource() {
            return DEFAULT_SOURCE;
        }
    };
    
    /**
     * {@link IdentifierSourceSet} which will fail for indoms and metrics, but allow default hashed
     * values for Instances
     */
    public static IdentifierSourceSet EXPLICIT_SET = new IdentifierSourceSet() {
        private final IdentifierSource DEFAULT_SOURCE = new HashingIdentifierSource();
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
}
