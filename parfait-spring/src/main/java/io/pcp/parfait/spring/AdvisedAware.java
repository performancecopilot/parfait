package io.pcp.parfait.spring;

public interface AdvisedAware {
	public void addAdvised(Object advised, String name);

	public String getName();
}