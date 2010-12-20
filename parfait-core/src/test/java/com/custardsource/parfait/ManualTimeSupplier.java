package com.custardsource.parfait;

import com.google.common.base.Supplier;

final class ManualTimeSupplier implements Supplier<Long> {
	private long time;
	
	public void setTime(long time) {
		this.time = time;			
	}

	public void tick(long increment) {
		this.time += increment;	
	}
	
	@Override
	public Long get() {
		return time;
	}
}