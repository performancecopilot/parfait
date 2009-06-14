package com.custardsource.parfait.pcp;

import java.io.IOException;

import com.custardsource.parfait.pcp.types.TypeHandler;

public interface PcpWriter {
    /**
     * Adds a new metric to the writer, with an initial default value. Uses the default
     * {@link TypeHandler} based on the runtime type of the initialValue parameter.
     * 
     * @param name
     *            the name of the metric to export. Must not exceed any byte-length limits specified
     *            by the implementation
     * @param initialValue
     *            the 'default' value to write into the file at initialisation time
     * @throws IllegalArgumentException
     *             if the name is too long, the metric name has already been added, or this is no
     *             type handler registered for the runtime class of the initial value
     * @throws IllegalStateException
     *             if this writer has already been started, finalising the file layout
     */
	public abstract void addMetric(String name, Object initialValue);

	/**
	 * Adds a new metric to the writer, with an initial default value. Uses the default
	 * {@link TypeHandler} based on the runtime type of the initialValue parameter.
	 * 
	 * @param name
     *            the name of the metric to export. Must not exceed any byte-length limits specified
     *            by the implementation
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
	public abstract <T> void addMetric(String name, T initialValue,
			TypeHandler<T> pcpType);

	/**
	 * Updates the metric value of the given metric, once the writer has been started
	 * 
	 * @param name
	 *            the metric to update
	 * @param value
	 *            the new value (must be convertible by the {@link TypeHandler} used when adding the
	 *            metric)
	 */
	public abstract void updateMetric(String name, Object value);

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

}