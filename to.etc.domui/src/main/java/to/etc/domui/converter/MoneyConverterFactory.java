/*
 * DomUI Java User Interface library
 * Copyright (c) 2010 by Frits Jalvingh, Itris B.V.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * See the "sponsors" file for a list of supporters.
 *
 * The latest version of DomUI and related code, support and documentation
 * can be found at http://www.domui.org/
 * The contact for the project is Frits Jalvingh <jal@etc.to>.
 */
package to.etc.domui.converter;

import java.math.*;

import to.etc.domui.component.meta.*;
import to.etc.util.*;

public class MoneyConverterFactory implements IConverterFactory {
	@Override
	public int accept(Class< ? > clz, PropertyMetaModel< ? > pmm) {
		if(pmm == null)
			return -1;

		//-- We only accept double and BigDecimal as base types,
		if(clz != Double.class && clz != double.class && clz != BigDecimal.class)
			return -1;

		switch(pmm.getNumericPresentation()){
			default:
				return -1;
			case MONEY:
			case MONEY_FULL:
			case MONEY_FULL_TRUNC:
			case MONEY_NO_SYMBOL:
			case MONEY_NUMERIC:
				return 10; // Must have higher priority than Double factory.
		}
	}

	/**
	 * Returns one of the converter classes for converting a double into one of the numericPresentations.
	 * @param pmm
	 * @return
	 */
	static public IConverter<Double> createDoubleMoneyConverters(NumericPresentation np) {
		switch(np){
			default:
				throw new IllegalStateException("? Dont know converter: " + np);
			case MONEY:
			case MONEY_FULL:
				return ConverterRegistry.getConverterInstance(MoneyDoubleFullConverter.class);
			case MONEY_FULL_TRUNC:
				return ConverterRegistry.getConverterInstance(MoneyDoubleTruncatedWithSign.class);
			case MONEY_NO_SYMBOL:
				return ConverterRegistry.getConverterInstance(MoneyDoubleNoSign.class);
			case MONEY_NUMERIC:
				return ConverterRegistry.getConverterInstance(MoneyDoubleNumeric.class);
		}
	}

	/**
	 * Returns one of the converter classes for converting a double into one of the numericPresentations.
	 * @param pmm
	 * @return
	 */
	static public IConverter<BigDecimal> createBigDecimalMoneyConverters(NumericPresentation np) {
		switch(np){
			default:
				throw new IllegalStateException("? Dont know converter: " + np);
			case MONEY:
			case MONEY_FULL:
				return ConverterRegistry.getConverterInstance(MoneyBigDecimalFullConverter.class);
			case MONEY_FULL_TRUNC:
				return ConverterRegistry.getConverterInstance(MoneyBigDecimalTruncatedWithSign.class);
			case MONEY_NO_SYMBOL:
				return ConverterRegistry.getConverterInstance(MoneyBigDecimalNoSign.class);
			case MONEY_NUMERIC:
				return ConverterRegistry.getConverterInstance(MoneyBigDecimalNumeric.class);
		}
	}

	/**
	 * Create the appropriate converter.
	 *
	 * @see to.etc.domui.converter.IConverterFactory#createConverter(java.lang.Class, to.etc.domui.component.meta.PropertyMetaModel)
	 */
	@Override
	public <X, T extends IConverter<X>> T createConverter(Class<X> clz, PropertyMetaModel<X> pmm) {
		try {
			if(clz == Double.class || clz == double.class) {
				return (T) createDoubleMoneyConverters(pmm.getNumericPresentation());
			}
			if(clz == BigDecimal.class) {
				return (T) createBigDecimalMoneyConverters(pmm.getNumericPresentation());
			}
		} catch(Exception x) { // James Gosling is an idiot.
			throw WrappedException.wrap(x);
		}
		throw new IllegalStateException("? Dont know converter?");
	}
}
