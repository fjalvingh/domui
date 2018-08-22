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

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.dom.html.NodeContainer;
import to.etc.domui.dom.html.TextNode;
import to.etc.domui.server.ExceptionUtil;
import to.etc.util.SecurityUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * A single message reported through the bugs thing.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 18, 2010
 */
@NonNullByDefault
final public class BugItem {
	final private BugSeverity m_severity;

	final private Date m_timestamp = new Date();

	final private String m_threadName;

	@NonNull
	final private String m_message;

	@Nullable
	final private Throwable m_exception;

	final private Exception m_location;

	@Nullable
	private List<NodeBase> m_formattedMsg;

	final private List<Object> m_contextItems = new ArrayList<>();

	private final List<IBugContribution> m_contributions = new ArrayList<>();

	@Nullable
	private String m_hash;

	private int m_number;

	/**
	 * Create a bug item with a simple String message.
	 */
	BugItem(@NonNull String message) {
		this(BugSeverity.BUG, message, null, Collections.emptyList());
	}

	/**
	 * Create a bug item with a set of UI Nodes to show as the message.
	 */
	BugItem(List<NodeBase> msg) {
		this(BugSeverity.BUG, flatten(msg), null, Collections.emptyList());
		m_formattedMsg = msg;
	}

	BugItem(@NonNull String message, @Nullable Throwable exception) {
		this(BugSeverity.BUG, message, exception, Collections.emptyList());
	}

	BugItem(@NonNull String message, @Nullable Throwable exception, List<Object> contextItems) {
		this(BugSeverity.BUG, message, exception, contextItems);
	}

	BugItem(@NonNull BugSeverity severity, @NonNull String message, @Nullable Throwable exception, List<Object> contextItems) {
		m_severity = severity;
		m_message = message;
		m_exception = exception;
		m_threadName = Thread.currentThread().getName();
		m_location = initLocation();
		m_contextItems.addAll(contextItems);
	}

	private static String flatten(List<NodeBase> msg) {
		StringBuilder sb = new StringBuilder();
		flatten(sb, msg);
		return sb.toString();
	}

	/**
	 * Flatten all nodes, and extract a text only message by appending all #text nodes.
	 */
	static private void flatten(StringBuilder sb, List<NodeBase> msg) {
		for(NodeBase b : msg) {
			flatten(sb, b);
		}
	}

	/**
	 * Flatten all nodes, and extract a text only message by appending all #text nodes.
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
	private Exception initLocation() {
		try {
			throw new Exception("duh");
		} catch(Exception x) {
			return x;
		}
	}

	/**
	 * Return the timestamp the bug occured on.
	 */
	@NonNull
	public Date getTimestamp() {
		return m_timestamp;
	}

	/**
	 * Return the error as a string.
	 */
	@NonNull
	public String getMessage() {
		return m_message;
	}

	/**
	 * Return the exception if the bug has one.
	 */
	@Nullable
	public Throwable getException() {
		return m_exception;
	}

	/**
	 * Return an exception which marks the location of the bug.
	 * FIXME This should return StackTraceElements, not an exception.
	 */
	@NonNull
	public Exception getLocation() {
		if(m_location != null)
			return m_location;
		throw new IllegalStateException("?? Location unset??");
	}

	/**
	 * Return the #of the bug in this set.
	 */
	public int getNumber() {
		return m_number;
	}

	public void setNumber(int number) {
		m_number = number;
	}

	/**
	 * If the bug was created with a set of nodes to render this returns those nodes.
	 */
	@Nullable
	public List<NodeBase> getFormattedMsg() {
		return m_formattedMsg;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb
			.append(new SimpleDateFormat("MMdd HH:mm:ss").format(m_timestamp)).append(" ")
			.append(getSeverity())
			.append(": ").append(getMessage());
		return sb.toString();
	}

	public BugSeverity getSeverity() {
		return m_severity;
	}

	public String getThreadName() {
		return m_threadName;
	}

	public synchronized void addContribution(IBugContribution item) {
		m_contributions.add(item);
	}

	public synchronized List<IBugContribution> getContributions() {
		return Collections.unmodifiableList(m_contributions);
	}

	public synchronized List<Object> getContextItems() {
		return Collections.unmodifiableList(m_contextItems);
	}

	@Nullable
	public <T> T findContextItem(Class<T> clazz) {
		for(Object contextItem : getContextItems()) {
			if(clazz.isAssignableFrom(contextItem.getClass())) {
				return (T) contextItem;
			}
		}
		return null;
	}


	/**
	 * Try to create a hash for the issue that should more or less uniquely identify it.
	 */
	public String getHash() {
		String hash = m_hash;
		if(null == hash) {
			Throwable exception = getException();
			if(null == exception) {
				//-- Use the message
				String message = getMessage().toLowerCase().replace(" ", "");
				hash = m_hash = SecurityUtils.getMD5Hash(message, "utf-8");
			} else {
				hash = m_hash = ExceptionUtil.getExceptionHash(exception);
			}
		}
		return hash;
	}
}
