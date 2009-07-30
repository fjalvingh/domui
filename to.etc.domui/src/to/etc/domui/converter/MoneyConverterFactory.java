package to.etc.domui.converter;

import java.math.*;

import to.etc.domui.component.meta.*;
import to.etc.util.*;

public class MoneyConverterFactory implements IConverterFactory {
	public int accept(Class<?> clz, PropertyMetaModel pmm) {
		if(pmm == null)
			return -1;

		//-- We only accept double and BigDecimal as base types,
		if(clz != Double.class && clz != BigDecimal.class)
			return -1;

		switch(pmm.getNumericPresentation()){
			default:
				return -1;
			case MONEY:
			case MONEY_FULL:
			case MONEY_FULL_TRUNC:
			case MONEY_NO_SYMBOL:
			case MONEY_NUMERIC:
				return 5;
		}
	}

	/**
	 * Returns one of the converter classes for converting a double into one of the numericPresentations.
	 * @param pmm
	 * @return
	 */
	static public Class<? extends IConverter<Double>> createDoubleMoneyConverters(NumericPresentation np) {
		switch(np){
			default:
				throw new IllegalStateException("? Dont know converter: " + np);
			case MONEY:
			case MONEY_FULL:
				return MoneyDoubleFullConverter.class;
			case MONEY_FULL_TRUNC:
				return MoneyDoubleTruncatedWithSign.class;
			case MONEY_NO_SYMBOL:
				return MoneyDoubleNoSign.class;
			case MONEY_NUMERIC:
				return MoneyDoubleNumeric.class;
		}
	}

	/**
	 * Returns one of the converter classes for converting a double into one of the numericPresentations.
	 * @param pmm
	 * @return
	 */
	static public Class<? extends IConverter<BigDecimal>> createBigDecimalMoneyConverters(NumericPresentation np) {
		switch(np){
			default:
				throw new IllegalStateException("? Dont know converter: " + np);
			case MONEY:
			case MONEY_FULL:
				return MoneyBigDecimalFullConverter.class;
			case MONEY_FULL_TRUNC:
				return MoneyBigDecimalTruncatedWithSign.class;
			case MONEY_NO_SYMBOL:
				return MoneyBigDecimalNoSign.class;
			case MONEY_NUMERIC:
				return MoneyBigDecimalNumeric.class;
		}
	}

	/**
	 * Create the appropriate converter.
	 *
	 * @see to.etc.domui.converter.IConverterFactory#createConverter(java.lang.Class, to.etc.domui.component.meta.PropertyMetaModel)
	 */
	public <X, T extends IConverter<X>> T createConverter(Class<X> clz, PropertyMetaModel pmm) {
		try {
			if(clz == Double.class) {
				Class<? extends IConverter<Double>> cc = createDoubleMoneyConverters(pmm.getNumericPresentation());
				return (T) cc.newInstance();
			}
			if(clz == BigDecimal.class) {
				Class<? extends IConverter<BigDecimal>> cc = createBigDecimalMoneyConverters(pmm.getNumericPresentation());
				return (T) cc.newInstance();
			}
		} catch(Exception x) { // James Gosling is an idiot.
			throw WrappedException.wrap(x);
		}
		throw new IllegalStateException("? Dont know converter?");
	}
}
