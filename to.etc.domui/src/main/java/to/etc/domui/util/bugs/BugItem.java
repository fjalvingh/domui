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
	final private Throwable m_exception;

	@Nullable
	private Exception m_location;

	@Nullable
	private List<NodeBase> m_formattedMsg;

	private int m_number;

	/**
	 * Create a bug item with a simple String message.
	 * @param message
	 */
	public BugItem(@Nonnull String message) {
		m_message = message;
		m_exception = null;
		initLocation();
	}

	/**
	 * Create a bug item with a set of UI Nodes to show as the message.
	 * @param msg
	 */
	public BugItem(List<NodeBase> msg) {
		m_formattedMsg = msg;
		m_exception = null;
		initLocation();
		StringBuilder sb = new StringBuilder();
		flatten(sb, msg);
		m_message = sb.toString();
	}

	/**
	 * Create a bug item from text and exception.
	 * @param message
	 * @param exception
	 */
	public BugItem(@Nonnull String message, @Nullable Throwable exception) {
		m_message = message;
		m_exception = exception;
		initLocation();
	}

	/**
	 * Flatten all nodes, and extract a text only message by appending all #text nodes.
	 *
	 * @param sb
	 * @param msg
	 */
	static private void flatten(StringBuilder sb, List<NodeBase> msg) {
		for(NodeBase b : msg) {
			flatten(sb, b);
		}
	}

	/**
	 * Flatten all nodes, and extract a text only message by appending all #text nodes.
	 *
	 * @param sb
	 * @param b
	 */
	private static void flatten(StringBuilder sb, NodeBase b) {
		if(b instanceof TextNode)
			sb.append(((TextNode) b).getText());
		else if(b instanceof NodeContainer) {
			for(NodeBase cb : ((NodeContainer) b))
				flatten(sb, cb);
		}
	}

	/**
	 * Mark the bug's location by creating a stacktrace to it.
	 */
	private void initLocation() {
		try {
			throw new Exception("duh");
		} catch(Exception x) {
			m_location = x;
		}
	}

	/**
	 * Return the timestamp the bug occured on.
	 * @return
	 */
	@Nonnull
	public Date getTimestamp() {
		return m_timestamp;
	}

	/**
	 * Return the error as a string.
	 * @return
	 */
	@Nonnull
	public String getMessage() {
		return m_message;
	}

	/**
	 * Return the exception if the bug has one.
	 * @return
	 */
	@Nullable
	public Throwable getException() {
		return m_exception;
	}

	/**
	 * Return an exception which marks the location of the bug.
	 * FIXME This should return StackTraceElements, not an exception.
	 * @return
	 */
	@Nonnull
	public Exception getLocation() {
		if(m_location != null)
			return m_location;
		throw new IllegalStateException("?? Location unset??");
	}

	/**
	 * Return the #of the bug in this set.
	 * @return
	 */
	public int getNumber() {
		return m_number;
	}

	public void setNumber(int number) {
		m_number = number;
	}

	/**
	 * If the bug was created with a set of nodes to render this returns those nodes.
	 * @return
	 */
	@Nullable
	public List<NodeBase> getFormattedMsg() {
		return m_formattedMsg;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		sb.append("message=").append(getMessage());
		sb.append("]");
		return sb.toString();
	}
}
