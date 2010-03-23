package to.etc.domui.converter;

import java.math.*;
import java.text.*;

import javax.annotation.*;

import to.etc.domui.component.meta.*;
import to.etc.domui.trouble.*;
import to.etc.domui.util.*;
import to.etc.webapp.nls.*;

public class NumericUtil {
	private NumericUtil() {}

	/*--------------------------------------------------------------*/
	/*	CODING:	Parsing.											*/
	/*--------------------------------------------------------------*/
	/**
	 * Parse an integer using all allowed embellishments.
	 * @param input
	 * @return
	 */
	static public int parseInt(String input) {
		MiniScanner ms = MiniScanner.getInstance();
		if(!ms.scanLaxEuro(input))
			return 0;
		try {
			return Integer.parseInt(ms.getStringResult());
		} catch(Exception x) {
			throw new ValidationException(Msgs.V_INVALID);
		}
	}

	@Nullable
	static public Integer parseIntWrapper(String input) {
		MiniScanner ms = MiniScanner.getInstance();
		if(!ms.scanLaxEuro(input))
			return null;
		try {
			return Integer.valueOf(ms.getStringResult());
		} catch(Exception x) {
			throw new ValidationException(Msgs.V_INVALID);
		}
	}

	static public long parseLong(String input) {
		MiniScanner ms = MiniScanner.getInstance();
		if(!ms.scanLaxEuro(input))
			return 0;
		return Long.parseLong(ms.getStringResult());
	}

	@Nullable
	static public Long parseLongWrapper(String input) {
		MiniScanner ms = MiniScanner.getInstance();
		if(!ms.scanLaxEuro(input))
			return null;
		try {
			return Long.valueOf(ms.getStringResult());
		} catch(Exception x) {
			throw new ValidationException(Msgs.V_INVALID);
		}
	}

	static public double parseDouble(String input) {
		MiniScanner ms = MiniScanner.getInstance();
		if(!ms.scanLaxEuro(input))
			return 0;
		try {
			return Double.parseDouble(ms.getStringResult());
		} catch(Exception x) {
			throw new ValidationException(Msgs.V_INVALID);
		}
	}

	@Nullable
	static public Double parseDoubleWrapper(String input) {
		MiniScanner ms = MiniScanner.getInstance();
		if(!ms.scanLaxEuro(input))
			return null;
		return Double.valueOf(ms.getStringResult());
	}

	@Nullable
	static public BigDecimal parseBigDecimal(String input) {
		MiniScanner ms = MiniScanner.getInstance();
		if(!ms.scanLaxEuro(input))
			return null;
		try {
			return new BigDecimal(ms.getStringResult());
		} catch(Exception x) {
			throw new ValidationException(Msgs.V_INVALID);
		}
	}

	/**
	 * Parse any supported numeric wrapper type.
	 * @param <T>
	 * @param type
	 * @param input
	 * @return
	 */
	@Nullable
	static public <T> T parseNumber(Class<T> type, String input) {
		if(Integer.class == type)
			return (T) parseIntWrapper(input);
		else if(Long.class == type)
			return (T) parseLongWrapper(input);
		else if(Double.class == type)
			return (T) parseDoubleWrapper(input);
		else if(BigDecimal.class == type)
			return (T) parseBigDecimal(input);
		else
			throw new IllegalArgumentException("Unsupported numeric type in conversion=" + type);
	}

	static private final String[] FULLBYSCALE = { //
	"###,###,###,###,###,###,###,###,###,###,###,###,##0" //
		, "###,###,###,###,###,###,###,###,###,###,###,###,##0,0" //
		, "###,###,###,###,###,###,###,###,###,###,###,###,##0,00" //
		, "###,###,###,###,###,###,###,###,###,###,###,###,##0,000" //
		, "###,###,###,###,###,###,###,###,###,###,###,###,##0,0000" //
		, "###,###,###,###,###,###,###,###,###,###,###,###,##0,00000" //
		, "###,###,###,###,###,###,###,###,###,###,###,###,##0,000000" //
	};

	static private final String[]	NUMBERBYSCALE = { //
	"#" //
		, "#.0" //
		, "#.00" //
		, "#.000" //
		, "#.0000" //
		, "#.00000" //
		, "#.000000" //
	};

	@Nonnull
	static public String renderNumber(Number v, NumericPresentation np, int scale) {
		if(v == null)
			return "";
		Class< ? > type = v.getClass();
		DecimalFormatSymbols dfs = new DecimalFormatSymbols(NlsContext.getLocale()); // Get numeric format symbols for the locale
		if(DomUtil.isIntegerType(type)) {
			switch(np){
				default:
					throw new IllegalArgumentException("Unsupported numeric presentation for numeric type " + v.getClass() + ": " + np);

				case NUMBER:
					return v.toString();
				case NUMBER_FULL:
					return new DecimalFormat("###,###,###,###,###,###,###,###,###,###,###,###,##0", dfs).format(v);
				case NUMBER_SCIENTIFIC:
					if(type != BigDecimal.class)
						v = new BigDecimal(v.longValue());
					return new DecimalFormat("#.#E#", dfs).format(v);
			}
		}

		if(scale > 6)
			scale = 6;
		else if(scale < 0)
			scale = 0;

		switch(np){
			default:
				throw new IllegalArgumentException("Unsupported numeric presentation for numeric type " + v.getClass() + ": " + np);

			case NUMBER:
				return new DecimalFormat(NUMBERBYSCALE[scale], dfs).format(v);

			case NUMBER_FULL:
				return new DecimalFormat(FULLBYSCALE[scale], dfs).format(v);
			case NUMBER_SCIENTIFIC:
				return new DecimalFormat("#.#E#", dfs).format(v);
		}
	}

	public static <T> IConverter<T> createNumberConverter(Class<T> type, NumericPresentation np, int scale) {
		return new NumberConverter(type, np, scale);
	}
}
