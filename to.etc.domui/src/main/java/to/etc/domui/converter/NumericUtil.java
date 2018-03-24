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

import to.etc.domui.component.meta.NumericPresentation;
import to.etc.domui.component.meta.PropertyMetaModel;
import to.etc.domui.trouble.ValidationException;
import to.etc.domui.util.DomUtil;
import to.etc.domui.util.Msgs;
import to.etc.webapp.nls.NlsContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;

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
	 * When scale is undefined on decima numbers conversions, use this global defined value.
	 */
	static public final int DEFAULT_FRACTION_DIGITS = 2;

	/**
	 * Parses an integer as a BigDecimal, then converts it. It also
	 * does a range check on that BigDecimal. FIXME Complex, do later.
	 * @param input
	 * @return
	 */
	static private int internalParseInt(String input) {
		BigDecimal bd = parseBigDecimal(input, 0, NumericPresentation.NUMBER);
		if(bd == null) {
			throw new ValidationException(Msgs.V_INVALID, input); // No input is invalid input.
		}
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
		if(!ms.scanLaxNumber(input, 0, true))
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
		if(!ms.scanLaxNumber(input, 0, true))
			return null;
		try {
			return Long.valueOf(ms.getStringResult());
		} catch(Exception x) {
			throw new ValidationException(Msgs.V_INVALID, input);
		}
	}

	@Nullable
	static public Double parseDoubleWrapper(String input, int scale, NumericPresentation np) {
		MiniScanner ms = MiniScanner.getInstance();
		boolean useStrictScale = np == NumericPresentation.NUMBER_FULL;
		if(!ms.scanLaxNumber(input, scale, useStrictScale))
			return null;
		try {
			return Double.valueOf(ms.getStringResult());
		} catch(Exception x) {
			throw new ValidationException(Msgs.V_INVALID, input);
		}
	}

	@Nullable
	static public BigDecimal parseBigDecimal(String input, int scale, NumericPresentation np) {
		MiniScanner ms = MiniScanner.getInstance();
		boolean useStrictScale = np == NumericPresentation.NUMBER_FULL;
		if(!ms.scanLaxNumber(input, scale, useStrictScale))
			return null;
		try {
			return new BigDecimal(ms.getStringResult());
		} catch(Exception x) {
			throw new ValidationException(Msgs.V_INVALID, input);
		}
	}


	/**
	 * DEPRECATED: This method wrongly assumes the scale of the number parsed - use {@link #parseNumber(Class, String, int)}.
	 *
	 * Parse any supported numeric wrapper type. In case that any specific scale must be used, use other method {@link NumericUtil#parseNumber(Class, String, int, NumericPresentation)}
	 * @param <T>
	 * @param type	In case of decimal types, it uses scale defined by DEFAULT_FRACTION_DIGITS.
	 * @param input
	 * @return
	 */
	@Deprecated
	@Nullable
	static public <T> T parseNumber(Class<T> type, String input) {
		if(Integer.class == type || int.class == type || Long.class == type || long.class == type) {
			return parseNumber(type, input, 0, NumericPresentation.NUMBER);
		} else {
			return parseNumber(type, input, DEFAULT_FRACTION_DIGITS, NumericPresentation.NUMBER);
		}
	}

	/**
	 * Parse any supported numeric wrapper type. In case that any specific scale must be used, use other method {@link NumericUtil#parseNumber(Class, String, int, NumericPresentation)}
	 * @param <T>
	 * @param type	In case of decimal types, it uses scale defined by DEFAULT_FRACTION_DIGITS.
	 * @param input
	 * @return
	 */
	@Nullable
	static public <T> T parseNumber(Class<T> type, String input, int scale) {
		if(Integer.class == type || int.class == type || Long.class == type || long.class == type) {
			return parseNumber(type, input, 0, NumericPresentation.NUMBER);
		} else {
			return parseNumber(type, input, scale, NumericPresentation.NUMBER);
		}
	}


	/**
	 * Parse any supported numeric wrapper type.
	 *
	 * @param <T>
	 * @param type
	 * @param input
	 * @param scale Integer based types can be used only with scale 0 -> no decimal places allowed here.
	 * @param np
	 * @return
	 */
	@Nullable
	static public <T> T parseNumber(Class<T> type, String input, int scale, NumericPresentation np) {
		if(scale != 0 && (Integer.class == type || int.class == type || Long.class == type || long.class == type)) {
			throw new IllegalArgumentException("Unsupported scale (" + scale + " - it must be 0) for type in conversion=" + type);
		}
		if(Integer.class == type || int.class == type) {
			return (T) parseIntWrapper(input);
		} else if(Long.class == type || long.class == type) {
			return (T) parseLongWrapper(input);
		} else if(Double.class == type || double.class == type)
			return (T) parseDoubleWrapper(input, scale, np);
		else if(BigDecimal.class == type)
			return (T) parseBigDecimal(input, scale, np);
		else
			throw new IllegalArgumentException("Unsupported numeric type in conversion=" + type);
	}

	static private final String[] FULL_BY_SCALE = { //
	"###,###,###,###,###,###,###,###,###,###,###,###,##0" //
		, "###,###,###,###,###,###,###,###,###,###,###,###,##0.0" //
		, "###,###,###,###,###,###,###,###,###,###,###,###,##0.00" //
		, "###,###,###,###,###,###,###,###,###,###,###,###,##0.000" //
		, "###,###,###,###,###,###,###,###,###,###,###,###,##0.0000" //
		, "###,###,###,###,###,###,###,###,###,###,###,###,##0.00000" //
		, "###,###,###,###,###,###,###,###,###,###,###,###,##0.000000" //
	};

	static private final String[]	NUMBER_BY_SCALE = { //
	"#0" //
		, "#0.0" //
		, "#0.00" //
		, "#0.000" //
		, "#0.0000" //
		, "#0.00000" //
		, "#0.000000" //
	};

	static private final String[] NUMBER_BY_SCALE_TRUNC_ZEROS = { //
	"#0" //
		, "#0.#" //
		, "#0.##" //
		, "#0.###" //
		, "#0.####" //
		, "#0.#####" //
		, "#0.######" //
	};

	@Nonnull
	static public String renderNumber(Number v, NumericPresentation np, int inScale) {
		if(v == null)
			return "";
		Class< ? > type = v.getClass();
		DecimalFormatSymbols dfs = new DecimalFormatSymbols(NlsContext.getLocale()); // Get numeric format symbols for the locale
		if(DomUtil.isIntegerType(type)) {
			switch(np){
				default:
					throw new IllegalArgumentException("Unsupported numeric presentation for numeric type " + v.getClass() + ": " + np);

				case NUMBER_SCALED:
					return v.toString();
				case UNKNOWN:
				case NUMBER:
				case NUMBER_FULL:
					return new DecimalFormat("###,###,###,###,###,###,###,###,###,###,###,###,##0", dfs).format(v);
				case NUMBER_SCIENTIFIC:
					if(type != BigDecimal.class)
						v = new BigDecimal(v.longValue());
					return new DecimalFormat("#.#E#", dfs).format(v);
			}
		}

		int scale = inScale;
		if(scale > 6)
			scale = 6;
		else if(scale < 0)
			scale = 0;

		switch(np){
			default:
				throw new IllegalArgumentException("Unsupported numeric presentation for numeric type " + v.getClass() + ": " + np);

			case UNKNOWN:
			case NUMBER:
				//-- If scale is unknown and the number is scaled, just print everything.
				if(inScale == -1 && DomUtil.isScaledType(v.getClass())) {
					NumberFormat nf = NumberFormat.getInstance(NlsContext.getLocale());
					nf.setGroupingUsed(true);
					return nf.format(v);
				}

				@Nonnull
				String res = new DecimalFormat(NUMBER_BY_SCALE_TRUNC_ZEROS[scale], dfs).format(v);
				if(res != null && (res.endsWith(".") || res.endsWith(","))) {
					//If we have 1000. then we need to cut of last decimal separator
					res = res.substring(0, res.length() - 2);
				}
				return res;

			case NUMBER_SCALED:
				return new DecimalFormat(NUMBER_BY_SCALE[scale], dfs).format(v);
			case NUMBER_FULL:
				return new DecimalFormat(FULL_BY_SCALE[scale], dfs).format(v);
			case NUMBER_SCIENTIFIC:
				return new DecimalFormat("#.#E#", dfs).format(v);
		}
	}

	public static <T extends Number> IConverter<T> createNumberConverter(Class<T> type, NumericPresentation np, int scale) {
		return new NumberConverter<T>(type, np, scale);
	}

	@SuppressWarnings({"unchecked"})
	public static <T extends Number> void assignNumericConverter(final PropertyMetaModel<T> pmm, boolean editable, final IConvertable<T> node, Class<T> type) {
		if(pmm.getConverter() != null)
			node.setConverter(pmm.getConverter());
		else {
			NumericPresentation np = null;
			//			if(!editable)
			np = pmm.getNumericPresentation();
			int scale = pmm.getScale();
			if(DomUtil.isIntegerType(type) && scale != 0) {
				//FIXME: vmijic 20110718 - Since this combination in pmm can break existing code, for now we just log this places.
				//SCHEDULED FOR DELETE - if it is proven that this actually does not happen, (if no such items in logs are found) this check shold be removed.
				if(scale > 0)
					System.out.println(pmm + ": WRONG SCALE on int types! Detected (scale :" + scale + ") is changed to 0!");
				scale = 0;
			}
			IConverter<T> c = createNumberConverter(type, np, scale);
			node.setConverter(c);
		}
	}

}
