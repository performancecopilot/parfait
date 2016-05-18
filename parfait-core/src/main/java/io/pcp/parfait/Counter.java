package io.pcp.parfait;

public interface Counter {
    public static final Counter NULL_COUNTER = new Counter(){
        @Override
        public void inc() {
        }

        @Override
        public void inc(long increment) {
        }
    };
    
    void inc();
    
    void inc(long increment);
}
