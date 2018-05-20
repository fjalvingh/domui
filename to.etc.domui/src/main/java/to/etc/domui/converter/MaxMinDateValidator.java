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
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.dom.errors.UIMessage;
import to.etc.domui.trouble.ValidationException;
import to.etc.domui.util.Msgs;
import to.etc.util.DateUtil;
import to.etc.webapp.nls.NlsContext;

import java.util.Date;

/**
 * Create a validator comparing minimum and maximum dates to this INCLUSIVE bound.
 *
 * @author <a href="mailto:rob.gersteling@itris.nl">Rob Gersteling</a>
 * @since Oct 11, 2016
 */
@NonNullByDefault
public final class MaxMinDateValidator implements IValueValidator<Date> {
	@Nullable
	private final Date m_minDate, m_maxDate;

	@Nullable
	private final UIMessage m_minDateMsg, m_maxDateMsg;

	private MaxMinDateValidator(Builder b) {
		m_minDate = b.m_min;
		m_maxDate = b.m_max;
		m_minDateMsg = b.m_minMsg;
		m_maxDateMsg = b.m_maxMsg;
	}

	/**
	 * Validates min / max dates against given date INCLUSIVE bound
	 *
	 * @see to.etc.domui.converter.IValueValidator#validate(java.lang.Object)
	 */
	@Override
	public void validate(@Nullable Date input) throws Exception {
		if(input == null)
			return;
		final Date min = m_minDate;
		if(null != min && input.before(min)) {
			throwError(Msgs.V_TOOSMALL, min, m_minDateMsg);
		}
		final Date max = m_maxDate;
		if(null != max && input.after(max)) {
			throwError(Msgs.V_TOOLARGE, max, m_maxDateMsg);
		}
	}

	private void throwError(@NonNull String code, @NonNull Date val, @Nullable UIMessage msg) {
		if(msg != null) {
			throw new ValidationException(msg.getBundle(), msg.getCode(), msg.getParameters());
		} else {
			DateConverter dc = new DateConverter();
			throw new ValidationException(code, dc.convertObjectToString(NlsContext.getLocale(), val));
		}
	}

	/**
	 * Builder class for assigning minimum / maximum dates and there's error messages
	 * The min or max date is mandatory, messages are optional.
	 * If no message is set a default messsage will be returned.
	 *
	 * @author <a href="mailto:rob.gersteling@itris.nl">Rob Gersteling</a>
	 * @since Oct 12, 2016
	 */
	public static class Builder {
		@Nullable
		private Date m_min, m_max;

		@Nullable
		private UIMessage m_minMsg, m_maxMsg;

		public Builder minimumDate(@NonNull Date minDate) {
			m_min = DateUtil.truncateDate(minDate);
			return this;
		}

		public Builder maximumDate(@NonNull Date maxDate) {
			m_max = DateUtil.truncateDate(maxDate);
			return this;
		}

		public Builder minimumMessage(@NonNull UIMessage minMsg) {
			m_minMsg = minMsg;
			return this;
		}

		public Builder maximumMessage(@NonNull UIMessage maxMsg) {
			m_maxMsg = maxMsg;
			return this;
		}

		/**
		 * If all's correct the Datevalidator is returned
		 *
		 * @return
		 * @throws Exception
		 */
		@NonNull
		public MaxMinDateValidator build() throws Exception {
			if(null == m_max && null == m_min) {
				throw new IllegalArgumentException("MaxDate or MinDate is mandatory");
			}
			if(null != m_max && null != m_min && m_min.after(m_max)) {
				throw new IllegalArgumentException("MaxDate cannot be smaller than MinDate");
			}

			return new MaxMinDateValidator(this);
		}
	}
}
