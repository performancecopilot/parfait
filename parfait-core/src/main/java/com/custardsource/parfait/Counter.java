package com.custardsource.parfait;

public interface Counter {
    void inc();
    
    void inc(long increment);

    public static final Counter NULL_COUNTER = new Counter(){
        @Override
        public void inc() {
        }

        @Override
        public void inc(long increment) {
        }
    };
}
