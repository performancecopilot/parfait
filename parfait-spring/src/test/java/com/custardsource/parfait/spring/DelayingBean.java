package com.custardsource.parfait.spring;

import java.util.Random;

@Profiled
public class DelayingBean {
	private final int delay;
	
	public DelayingBean() {
		this.delay = new Random().nextInt(100);
	}
	
	@Profiled
	public void doThing() {
		try {
			Thread.sleep(delay);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
}
