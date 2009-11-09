package com.custardsource.parfait.dxm;

public interface IdentifierSourceSet {
    public IdentifierSource instanceDomainSource();
    public IdentifierSource instanceSource(InstanceDomain domain);
    public IdentifierSource metricSource();
    
    public static IdentifierSourceSet DEFAULT_SET = new IdentifierSourceSet() {
        @Override
        public IdentifierSource metricSource() {
            return new HashingIdentifierSource();
        }
        
        @Override
        public IdentifierSource instanceSource(InstanceDomain domain) {
            return new HashingIdentifierSource();
        }
        
        @Override
        public IdentifierSource instanceDomainSource() {
            return new HashingIdentifierSource();
        }
    };
}
