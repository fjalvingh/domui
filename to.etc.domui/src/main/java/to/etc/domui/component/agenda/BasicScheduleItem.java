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
package to.etc.domui.component.agenda;

import java.util.*;

public class BasicScheduleItem implements ScheduleItem {
	private String m_details;

	private String m_imageURL;

	private String m_name;

	private Date m_start;

	private Date m_end;

	private String m_id;

	private String m_type;

	public BasicScheduleItem(String id, Date start, Date end, String name, String details, String type, String imageURL) {
		m_id = id;
		m_start = start;
		m_end = end;
		m_name = name;
		m_details = details;
		m_type = type;
		m_imageURL = imageURL;
	}

	@Override
	public String getDetails() {
		return m_details;
	}

	@Override
	public Date getEnd() {
		return m_end;
	}

	@Override
	public String getID() {
		return m_id;
	}

	@Override
	public String getImageURL() {
		return m_imageURL;
	}

	@Override
	public String getName() {
		return m_name;
	}

	@Override
	public Date getStart() {
		return m_start;
	}

	@Override
	public String getType() {
		return m_type;
	}
}
