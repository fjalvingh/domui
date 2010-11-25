/*
 * DomUI Java User Interface - shared code
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
package to.etc.dbpool.info;

import javax.annotation.*;

/**
 * This encapsulates a single stored performance metric of a given type and key.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Nov 8, 2010
 */
public class PerfItem {
	/** The key to use to prevent duplicates in the list, like screen name or sql statement. */
	@Nonnull
	final private String m_key;

	/** If applicable, an ident for the request that caused this metric. This is usually the complete URL for web requests. */
	@Nullable
	final private String m_request;

	/** Something which holds whatever data required to display this-item's metric data */
	@Nullable
	final private Object m_data;

	/** The actual value used to sort this in the top-xxx list. */
	final private long m_metric;

	public PerfItem(@Nonnull String key, long metric, @Nullable String request, @Nullable Object data) {
		m_key = key;
		m_metric = metric;
		m_data = data;
		m_request = request;
	}

	@Nonnull
	public String getKey() {
		return m_key;
	}

	@Nullable
	public Object getData() {
		return m_data;
	}

	public long getMetric() {
		return m_metric;
	}

	@Nullable
	public String getRequest() {
		return m_request;
	}
}
