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
		return internalParseInt(input);
	}

	@Nullable
	static public Integer parseIntWrapper(String input) {
		return Integer.valueOf(internalParseInt(input));
	}

	static private final BigDecimal MAXINT = BigDecimal.valueOf(Integer.MAX_VALUE);

	static private final BigDecimal MININT = BigDecimal.valueOf(Integer.MIN_VALUE);

	/**
	 * Parses an integer as a BigDecimal, then converts it. It also
	 * does a range check on that BigDecimal. FIXME Complex, do later.
	 * @param input
	 * @return
	 */
	static private int internalParseInt(String input) {
		BigDecimal bd = parseBigDecimal(input);
		if(bd.compareTo(MAXINT) > 0)
			throw new ValidationException(Msgs.V_TOOLARGE, MAXINT);
		if(bd.compareTo(MININT) < 0)
			throw new ValidationException(Msgs.V_TOOSMALL, MININT);
		try {
			return bd.intValueExact();
		} catch(Exception x) {
			throw new ValidationException(Msgs.V_INVALID, input); // Happens when a fraction is present.
		}
	}

	static public long parseLong(String input) {
		MiniScanner ms = MiniScanner.getInstance();
		if(!ms.scanLaxNumber(input))
			return 0;
		try {
			return Long.parseLong(ms.getStringResult());
		} catch(Exception x) {
			throw new ValidationException(Msgs.V_INVALID, input);
		}
	}

	@Nullable
	static public Long parseLongWrapper(String input) {
		MiniScanner ms = MiniScanner.getInstance();
		if(!ms.scanLaxNumber(input))
			return null;
		try {
			return Long.valueOf(ms.getStringResult());
		} catch(Exception x) {
			throw new ValidationException(Msgs.V_INVALID, input);
		}
	}

	static public double parseDouble(String input) {
		MiniScanner ms = MiniScanner.getInstance();
		if(!ms.scanLaxNumber(input))
			return 0;
		try {
			return Double.parseDouble(ms.getStringResult());
		} catch(Exception x) {
			throw new ValidationException(Msgs.V_INVALID, input);
		}
	}

	@Nullable
	static public Double parseDoubleWrapper(String input) {
		MiniScanner ms = MiniScanner.getInstance();
		if(!ms.scanLaxNumber(input))
			return null;
		try {
			return Double.valueOf(ms.getStringResult());
		} catch(Exception x) {
			throw new ValidationException(Msgs.V_INVALID, input);
		}
	}

	@Nullable
	static public BigDecimal parseBigDecimal(String input) {
		MiniScanner ms = MiniScanner.getInstance();
		if(!ms.scanLaxNumber(input))
			return null;
		try {
			return new BigDecimal(ms.getStringResult());
		} catch(Exception x) {
			throw new ValidationException(Msgs.V_INVALID, input);
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
		if(Integer.class == type || int.class == type)
			return (T) parseIntWrapper(input);
		else if(Long.class == type || long.class == type)
			return (T) parseLongWrapper(input);
		else if(Double.class == type || double.class == type)
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

	public static <T extends Number> IConverter<T> createNumberConverter(Class<T> type, NumericPresentation np, int scale) {
		return new NumberConverter<T>(type, np, scale);
	}
}
