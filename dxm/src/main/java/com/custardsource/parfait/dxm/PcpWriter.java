package com.custardsource.parfait.dxm;

import com.custardsource.parfait.dxm.semantics.Semantics;
import com.custardsource.parfait.dxm.types.TypeHandler;

import javax.measure.unit.Unit;
import java.io.IOException;
import java.nio.ByteBuffer;

public interface PcpWriter {
    /**
     * Adds a new metric to the writer, with an initial default value. Uses the default
     * {@link TypeHandler} based on the runtime type of the initialValue parameter.
     * 
     * @param name
     *            the name of the metric to export. Must not exceed any byte-length limits specified
     *            by the implementation
     * @param semantics
     *            the PCP semantics of the metric
     * @param unit
     *            the unit used to measure the metric.
     * @param initialValue
     *            the 'default' value to write into the file at initialisation time
     * @throws IllegalArgumentException
     *             if the name is too long, the metric name has already been added, or this is no
     *             type handler registered for the runtime class of the initial value
     * @throws IllegalStateException
     *             if this writer has already been started, finalising the file layout
     */
    public abstract void addMetric(MetricName name, Semantics semantics, Unit<?> unit,
            Object initialValue);

	/**
	 * Adds a new metric to the writer, with an initial default value. Uses the default
	 * {@link TypeHandler} based on the runtime type of the initialValue parameter.
	 * 
	 * @param name
     *            the name of the metric to export. Must not exceed any byte-length limits specified
     *            by the implementation
     * @param semantics
     *            the PCP semantics of the metric
     * @param unit
     *            the unit used to measure the metric.
	 * @param initialValue
	 *            the 'default' value to write into the file at initialisation time
	 * @param pcpType
	 *            the type converter to use to render the initial value (and all subsequent values)
	 *            to the PCP stream
	 * @throws IllegalArgumentException
	 *             if the name is too long or the metric name has already been added
	 * @throws IllegalStateException
	 *             if this writer has already been started, finalising the file layout
	 */
    public abstract <T> void addMetric(MetricName name, Semantics semantics, Unit<?> unit,
            T initialValue, TypeHandler<T> pcpType);

	/**
	 * Updates the metric value of the given metric, once the writer has been started
	 * 
	 * @param name
	 *            the metric to update
	 * @param value
	 *            the new value (must be convertible by the {@link TypeHandler} used when adding the
	 *            metric)
	 */
	public abstract void updateMetric(MetricName name, Object value);

	/**
	 * Registers a new {@link TypeHandler} to be used to convert all subsequent values of type
	 * runtimeClass
	 * 
	 * @param runtimeClass
	 *            the class to be converted by the new handler
	 * @param handler
	 *            the handler to use
	 */
	public abstract <T> void registerType(Class<T> runtimeClass,
			TypeHandler<T> handler);

	/**
	 * Starts the Writer, freezing the file format and writing out the metadata and initial values.
	 * 
	 * @throws IOException
	 *             if the file cannot be created or written.
	 */
	public abstract void start() throws IOException;
	
    /**
     * Sets the help text associated with an instance domain.
     * 
     * @param instanceDomain
     *            Java pseudo-instance domain identifier (i.e. metric prefix; for
     *            animals.dog[xxx].size this is the 'animals.dog' part)
     * @param shortHelpText
     *            the short help text; must not exceed any length limits specified by the
     *            implementation
     * @param longHelpText
     *            the long explanatory text; must not exceed any length limits specified by the
     *            implementation
     */
	public void setInstanceDomainHelpText(String instanceDomain, String shortHelpText, String longHelpText); 

    /**
     * Sets the help text associated with a particular metric
     * 
     * @param metricName
     *            String version of the metric name, ignoring any possible instance domains. (e.g. for
     *            animals.dog[xxx].size this is 'animals.dog.size')
     * @param shortHelpText
     *            the short help text; must not exceed any length limits specified by the
     *            implementation
     * @param longHelpText
     *            the long explanatory text; must not exceed any length limits specified by the
     *            implementation
     */
    public void setMetricHelpText(String metricName, String shortHelpText, String longHelpText);

    /**
     * Prepares this object such that it can be restarted by invoking the {@link #start()} method
     * again.
     */
    public void reset();


}