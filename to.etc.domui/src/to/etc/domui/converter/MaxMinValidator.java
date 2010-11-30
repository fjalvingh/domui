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

import to.etc.domui.trouble.*;
import to.etc.domui.util.*;

public class MaxMinValidator implements IValueValidator<Number> {
	private Number m_max, m_min;

	/**
	 * Create a validator comparing to these INCLUSIVE bounds.
	 * @param max
	 * @param min
	 */
	public MaxMinValidator(Number min, Number max) {
		m_max = max;
		m_min = min;
	}

	/**
	 * Sigh. Of course Number does not implement Comparable, because that would
	 * be useful.
	 * @see to.etc.domui.converter.IValueValidator#validate(java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void validate(Number input) throws Exception {
		Class< ? > ac = input.getClass();
		if(m_max.getClass() == ac && m_min.getClass() == ac && input instanceof Comparable< ? >) {
			int r = ((Comparable<Number>) input).compareTo(m_min);
			if(r < 0) {
				throw new ValidationException(Msgs.V_TOOSMALL, m_min.toString());
			}
			r = ((Comparable<Number>) input).compareTo(m_max);
			if(r > 0) {
				throw new ValidationException(Msgs.V_TOOLARGE, m_max.toString());
			}
		} else {
			if(input.doubleValue() > m_max.doubleValue())
				throw new ValidationException(Msgs.V_TOOLARGE, m_max.toString());
			if(input.doubleValue() < m_min.doubleValue())
				throw new ValidationException(Msgs.V_TOOSMALL, m_min.toString());
		}
	}
}
