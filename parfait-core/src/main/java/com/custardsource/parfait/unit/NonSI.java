/*
 *  Unit-API - Units of Measurement API for Java
 *  Copyright (c) 2005-2016, Jean-Marie Dautelle, Werner Keil, V2COM.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of JSR-363 nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED
 * AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.custardsource.parfait.unit;

import static tec.units.ri.unit.Units.SECOND;
import static tec.units.ri.AbstractUnit.ONE;

import javax.measure.Quantity;
import javax.measure.Unit;
import javax.measure.quantity.Frequency;
import javax.measure.quantity.Time;

import com.custardsource.parfait.quantity.Information;
import com.custardsource.parfait.quantity.InformationRate;

import tec.units.ri.AbstractSystemOfUnits;
import tec.units.ri.AbstractUnit;
import tec.units.ri.function.LogConverter;
import tec.units.ri.function.RationalConverter;
import tec.units.ri.unit.AlternateUnit;
import tec.units.ri.unit.ProductUnit;

/**
 * <p>
 * This class contains units that are not part of the International System of
 * Units, that is, they are outside the SI, but are important and widely used
 * in the field of computer system performance analysis.
 * </p>
 * 
 * <p>
 * This is a collection of performance-related NonSI units (bytes, bits, bps),
 * as well as some other closely related units for convenience.
 * </p>
 */
public class NonSI extends AbstractSystemOfUnits implements Nameable {

	public static final String SYSTEM_NAME = "ParfaitUnitsNonSI";

	// compatibility, since
	// Class.getSimpleName()
	// isn't available.

	protected NonSI() {
	}

	private static final NonSI INSTANCE = new NonSI();

	// ///////////////
	// Information //
	// ///////////////

	/**
	 * The unit for binary information (standard name <code>bit</code>).
	 */
	public static final Unit<Information> BIT = addUnit(
		new AlternateUnit<Information>(ONE, "bit"), Information.class);

	/**
	 * A unit of data amount equal to <code>8 {@link SI#BIT}</code> (BinarY
	 * TErm, standard name <code>byte</code>).
	 */
	public static final Unit<Information> BYTE = addUnit(BIT.multiply(8));

	/**
	 * The unit for binary information rate (standard name <code>bit/s</code>).
	 */
	public static final ProductUnit<InformationRate> BITS_PER_SECOND
		= addUnit(new ProductUnit<InformationRate>(BIT.divide(SECOND)),
				InformationRate.class);

	/**
	 * Equivalent {@link #BYTE}
	 */
	public static final Unit<Information> OCTET = BYTE;

	///////////////
	// Frequency //
	///////////////

	/**
	 * A unit used to measure the frequency (rate) at which an imaging device
	 * produces unique consecutive images (standard name <code>fps</code>).
	 */
	public static final Unit<Frequency> FRAMES_PER_SECOND = addUnit(
			ONE.divide(SECOND)).asType(Frequency.class);

        /////////////////////
        // Collection View //
        /////////////////////

	@Override
	public String getName() {
		return SYSTEM_NAME;
	}

	/**
	 * Returns the unique instance of this class.
	 * 
	 * @return the NonSI instance.
	 */
	public static NonSI getInstance() {
		return INSTANCE;
	}

	/**
	 * Adds a new unit not mapped to any specified quantity type.
	 *
	 * @param unit
	 *          the unit being added.
	 * @return <code>unit</code>.
	 */
	private static <U extends Unit<?>> U addUnit(U unit) {
		INSTANCE.units.add(unit);
		return unit;
	}

	/**
	 * Adds a new unit and maps it to the specified quantity type.
	 *
	 * @param unit
	 *          the unit being added.
	 * @param type
	 *          the quantity type.
	 * @return <code>unit</code>.
	 */
	private static <U extends AbstractUnit<?>> U addUnit(U unit, Class<? extends Quantity<?>> type) {
		INSTANCE.units.add(unit);
		INSTANCE.quantityToUnit.put(type, unit);
		return unit;
	}
}
