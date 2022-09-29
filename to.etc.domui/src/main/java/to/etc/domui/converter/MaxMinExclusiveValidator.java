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
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.dom.errors.UIMessage;
import to.etc.domui.trouble.ValidationException;
import to.etc.domui.util.Msgs;
import to.etc.webapp.nls.IBundleCode;

/**
 * This validator checks to see if a Number is between two <b>exclusive</b> bounds. Exclusive means:
 * if the number == max or the number == min it is <b>not allowed</b>, i.e. the range is &lt;min..max&gt;.
 */
public class MaxMinExclusiveValidator implements IValueValidator<Number> {
	@NonNull
	private Number m_max, m_min;

	@Nullable
	private final UIMessage m_msg;

	/**
	 * Create a validator comparing to these INCLUSIVE bounds.
	 *
	 * @param max
	 * @param min
	 * @param msg If specified this error message will be shown, otherwise default error message is shown.
	 */
	public MaxMinExclusiveValidator(@NonNull Number min, @NonNull Number max, @Nullable UIMessage msg) {
		m_max = max;
		m_min = min;
		m_msg = msg;
	}

	/**
	 * Create a validator comparing to these INCLUSIVE bounds.
	 *
	 * @param max
	 * @param min
	 */
	public MaxMinExclusiveValidator(@NonNull Number min, @NonNull Number max) {
		this(min, max, null);
	}

	/**
	 * Sigh. Of course Number does not implement Comparable, because that would
	 * be useful.
	 *
	 * @see IValueValidator#validate(Object)
	 */
	@Override
	public void validate(Number input) throws Exception {
		Class<?> ac = input.getClass();
		if(m_max.getClass() == ac && m_min.getClass() == ac && input instanceof Comparable<?>) {
			int r = ((Comparable<Number>) input).compareTo(m_min);
			if(r <= 0) {
				throwError(Msgs.vTooSmall, m_min);
			}
			r = ((Comparable<Number>) input).compareTo(m_max);
			if(r >= 0) {
				throwError(Msgs.vTooLarge, m_max);
			}
		} else {
			if(input.doubleValue() >= m_max.doubleValue())
				throwError(Msgs.vTooLarge, m_max);
			if(input.doubleValue() <= m_min.doubleValue())
				throwError(Msgs.vTooSmall, m_min);
		}
	}

	private void throwError(@NonNull IBundleCode code, @NonNull Number val) {
		UIMessage msg = m_msg;
		if(msg != null) {
			throw new ValidationException(msg);
		} else {
			throw new ValidationException(code, val.toString());
		}
	}
}
