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
}
