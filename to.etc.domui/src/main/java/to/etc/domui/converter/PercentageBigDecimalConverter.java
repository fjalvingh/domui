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

import org.eclipse.jdt.annotation.NonNull;
import to.etc.domui.trouble.UIException;
import to.etc.domui.trouble.ValidationException;
import to.etc.domui.util.Msgs;
import to.etc.util.StringTool;
import to.etc.webapp.nls.NlsContext;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * This converts a BigDecimal percentage amount to a full representation including percentage sign.
 */
public class PercentageBigDecimalConverter implements IConverter<BigDecimal> {

	private final int m_scale;

	public PercentageBigDecimalConverter(int scale) {
		m_scale = scale;
	}

	@NonNull
	public static String getFormatedPercentageString(BigDecimal in, int scale) {
		DecimalFormatSymbols dfs = new DecimalFormatSymbols(NlsContext.getLocale()); // Get numeric format symbols for the locale
		String pattern = "##0." + StringTool.fill(scale, '#');
		DecimalFormat df = new DecimalFormat(pattern, dfs);
		return df.format(in.multiply(BigDecimal.valueOf(100))) + "%";
	}

	@Override
	public String convertObjectToString(Locale loc, BigDecimal in) throws UIException {
		if(in == null)
			return null;
		return getFormatedPercentageString(in, m_scale);
	}

	/**
	 * Does a conversion of an percentage amount to a BigDecimal. The input can contain percentage value with or without trailing percentage sign.
	 * Input value must be between 0 and 100 including. Input value is rounded to decimal digits defined by scale.
	 *
	 * @see IConverter#convertStringToObject(Locale, String)
	 */
	@Override
	public BigDecimal convertStringToObject(Locale loc, String in) throws UIException {
		if(in == null)
			return null;

		in = in.trim();
		if(in.endsWith("%"))
			in = in.substring(0, in.length() - 1).trim();

		in = in.replace(',', '.'); // If percentage is entered with comma replace with dot

		try {
			return new BigDecimal(in).divide(BigDecimal.valueOf(100)).setScale(m_scale + 2, RoundingMode.HALF_EVEN);
		} catch(NumberFormatException ex) {
			throw new ValidationException(Msgs.vBadPercentage, in);
		}
	}

	public int getScale() {
		return m_scale;
	}
}
