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
package to.etc.domui.util.bugs;

import java.util.*;

import javax.annotation.*;

import to.etc.domui.dom.html.*;

/**
 * A single message reported through the bugs thing.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 18, 2010
 */
final public class BugItem {
	final private Date m_timestamp = new Date();

	@Nonnull
	private String m_message;

	@Nullable
	private Throwable m_exception;

	@Nonnull
	private Exception m_location;

	@Nullable
	private List<NodeBase> m_formattedMsg;

	private int m_number;

	public BugItem(@Nonnull String message) {
		m_message = message;
		initLocation();
	}

	public BugItem(List<NodeBase> msg) {
		m_formattedMsg = msg;
		m_exception = null;
		initLocation();
		StringBuilder sb = new StringBuilder();
		flatten(sb, msg);
		m_message = sb.toString();
	}

	static private void flatten(StringBuilder sb, List<NodeBase> msg) {
		for(NodeBase b : msg) {
			flatten(sb, b);
		}
	}

	private static void flatten(StringBuilder sb, NodeBase b) {
		if(b instanceof TextNode)
			sb.append(((TextNode) b).getText());
		else if(b instanceof NodeContainer) {
			for(NodeBase cb : ((NodeContainer) b))
				flatten(sb, cb);
		}
	}

	public BugItem(@Nonnull String message, @Nullable Throwable exception) {
		m_message = message;
		m_exception = exception;
		initLocation();
	}

	private void initLocation() {
		try {
			throw new Exception("duh");
		} catch(Exception x) {
			m_location = x;
		}
	}

	@Nonnull
	public Date getTimestamp() {
		return m_timestamp;
	}

	@Nonnull
	public String getMessage() {
		return m_message;
	}

	@Nullable
	public Throwable getException() {
		return m_exception;
	}

	@Nonnull
	public Exception getLocation() {
		return m_location;
	}

	public int getNumber() {
		return m_number;
	}

	public void setNumber(int number) {
		m_number = number;
	}

	public List<NodeBase> getFormattedMsg() {
		return m_formattedMsg;
	}
}
