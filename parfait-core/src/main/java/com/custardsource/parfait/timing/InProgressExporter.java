package com.custardsource.parfait.timing;



public class InProgressExporter {
    private final EventTimer timer;
    private final ThreadContext context;
    
    public InProgressExporter(EventTimer timer, ThreadContext context) {
        this.timer = timer;
        this.context = context;
    }
    
    public InProgressSnapshot getSnapshot() {
        return InProgressSnapshot.capture(timer, context);
    }
}
