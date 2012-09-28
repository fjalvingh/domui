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

import java.util.*;

import to.etc.domui.trouble.*;
import to.etc.domui.util.*;
import to.etc.util.*;

/**
 * Converter to convert a computer "size" to a size in bytes, KB, MB, GB etc.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 30, 2009
 */
public class DoubleSizeConverter implements IConverter<Double> {
	final private double m_factor;

	public DoubleSizeConverter() {
		m_factor = 1.0d;
	}

	public DoubleSizeConverter(double factor) {
		m_factor = factor;
	}

	@Override
	public String convertObjectToString(Locale loc, Double in) throws UIException {
		if(null == in)
			return null;
		return StringTool.strSize((long) (in.doubleValue() * m_factor));
	}

	@Override
	public Double convertStringToObject(Locale loc, String val) throws UIException {
		val = val.trim();
		if(val.length() == 0)
			throw new ValidationException(Msgs.V_INVALID);

		int lindex = val.length();
		while(lindex > 0 && !Character.isDigit(val.charAt(lindex - 1)))
			lindex--;
		if(lindex <= 0)
			throw new ValidationException(Msgs.V_INVALID);

		String f = val.substring(lindex).trim().toLowerCase();
		if(f.length() > 1)
			f = f.substring(0, 1);
		val = val.substring(0, lindex).trim();
		double size = Double.parseDouble(val);
		if("k".equals(f))
			size *= 1024;
		else if("m".equals(f))
			size *= 1024 * 1024;
		else if("g".equals(f))
			size *= 1024l * 1024l * 1024l;
		else if("t".equals(f))
			size *= 1024l * 1024l * 1024l * 1024l;
		else if(f.length() != 0)
			throw new ValidationException(Msgs.V_INVALID);

		return Double.valueOf(size / m_factor);
	}
}
