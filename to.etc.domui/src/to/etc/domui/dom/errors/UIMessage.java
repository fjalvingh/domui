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
package to.etc.domui.dom.errors;

import java.util.*;

import to.etc.domui.dom.html.*;
import to.etc.domui.trouble.*;
import to.etc.domui.util.*;
import to.etc.webapp.nls.*;

/**
 * A single error message for a component. The message consists of a message code and optional
 * parameters, and usually hard-refers to the component where the error occured. Global errors
 * exist also; these have a null component and must be shown in whatever error container
 * exists.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 11, 2008
 */
public class UIMessage {
	/** The message bundle to use, or null if the deprecated "global messages" are used. */
	private BundleRef m_bundle;

	/** The error message code for the error that has occured. This exists always and is a lookup into the error NLS messages. */
	private String m_code;

	/** The type of the message (error, warning, informational); this mainly defines whether actions continue, and it defines an icon to show. */
	private MsgType m_type;

	/** When set this is used in error messages as an indication of which input field contains the error. It usually contains the value for the "label" of the control. */
	private String m_errorLocation;

	/** For errors that have parameters - these are the parameters. This is null if no parameters are present. */
	private Object[] m_parameters;

	/**
	 * The Node that this error pertains to. It is usually a Component-type node.
	 */
	private NodeBase m_errorNode;

	/**
	 * This deprecated constructor uses a code from a global message bundle (which is deprecated).
	 *
	 * @param errorNode
	 * @param type
	 * @param code
	 * @param parameters
	 */
	@Deprecated
	private UIMessage(NodeBase errorNode, String errorLocation, MsgType type, String code, Object[] parameters) {
		if(code == null)
			throw new NullPointerException("Message code cannot be null");
		if(type == null)
			throw new NullPointerException("Message type cannot be null");
		m_errorNode = errorNode;
		m_code = code;
		m_parameters = parameters;
		m_type = type;
		m_errorLocation = errorLocation;
	}

	/**
	 * Create an error message container.
	 * @param errorNode			If not-null this is the node that "owns" the error. This node will show a visual indication of the fact that it contains an error.
	 * @param errorLocation		If not-null this is a user-understandable name of the input item that contains the error. It usually is the "label" associated with the problem.
	 * @param type				The type of message: error, warning or info.
	 * @param br				The bundle containing the message for the code. If this is null (deprecated) the "global bundle set" is used *WHICH IS DEPRECATED*.
	 * @param code				The code for the message.
	 * @param parameters		If needed a set of parameters to render into the message.
	 */
	protected UIMessage(NodeBase errorNode, String errorLocation, MsgType type, BundleRef br, String code, Object[] parameters) {
		if(code == null)
			throw new NullPointerException("Message code cannot be null");
		if(type == null)
			throw new NullPointerException("Message type cannot be null");
		m_bundle = br;
		m_errorNode = errorNode;
		m_code = code;
		m_parameters = parameters;
		m_type = type;
		m_errorLocation = errorLocation;
	}

	public String getCode() {
		return m_code;
	}

	/**
	 * Returns the message bundle the code is in, or null if the code is a global message code (deprecated).
	 * @return
	 */
	public BundleRef getBundle() {
		return m_bundle;
	}

	public Object[] getParameters() {
		return m_parameters;
	}

	public NodeBase getErrorNode() {
		return m_errorNode;
	}

	public void setErrorNode(NodeBase errorNode) {
		m_errorNode = errorNode;
	}

	public MsgType getType() {
		return m_type;
	}

	/**
	 * When set this is used in error messages as an indication of which input field contains the
	 * error. It usually contains the value for the "label" of the control.
	 * @return
	 */
	public String getErrorLocation() {
		return m_errorLocation;
	}

	public void setErrorLocation(String errorLocation) {
		m_errorLocation = errorLocation;
	}

	/**
	 * Returns the message part of the error message, properly localized for the request's locale.
	 * @return
	 */
	public String getMessage() {
		if(m_bundle != null)
			return m_bundle.formatMessage(m_code, m_parameters);

		return Msgs.BUNDLE.formatMessage(m_code, m_parameters);
	}

	static public UIMessage error(UIException x) {
		return new UIMessage(null, null, MsgType.ERROR, x.getBundle(), x.getCode(), x.getParameters());
	}

	static public UIMessage error(NodeBase node, String errorLocation, BundleRef ref, String code, Object... param) {
		return new UIMessage(node, errorLocation, MsgType.ERROR, ref, code, param);
	}

	static public UIMessage error(String errorLocation, BundleRef ref, String code, Object... param) {
		return new UIMessage(null, errorLocation, MsgType.ERROR, ref, code, param);
	}

	static public UIMessage error(NodeBase node, BundleRef ref, String code, Object... param) {
		return new UIMessage(node, node.getErrorLocation(), MsgType.ERROR, ref, code, param);
	}

	static public UIMessage error(BundleRef ref, String code, Object... param) {
		return new UIMessage(null, null, MsgType.ERROR, ref, code, param);
	}

	static public UIMessage warning(NodeBase node, String errorLocation, BundleRef ref, String code, Object... param) {
		return new UIMessage(node, errorLocation, MsgType.WARNING, ref, code, param);
	}

	static public UIMessage warning(NodeBase node, BundleRef ref, String code, Object... param) {
		return new UIMessage(node, null, MsgType.WARNING, ref, code, param);
	}

	static public UIMessage warning(BundleRef ref, String code, Object... param) {
		return new UIMessage(null, null, MsgType.WARNING, ref, code, param);
	}

	static public UIMessage info(BundleRef ref, String code, Object... param) {
		return new UIMessage(null, null, MsgType.INFO, ref, code, param);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((m_bundle == null) ? 0 : m_bundle.hashCode());
		result = prime * result + ((m_code == null) ? 0 : m_code.hashCode());
		result = prime * result + ((m_errorNode == null) ? 0 : m_errorNode.hashCode());
		result = prime * result + Arrays.hashCode(m_parameters);
		result = prime * result + ((m_type == null) ? 0 : m_type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj)
			return true;
		if(obj == null)
			return false;
		if(getClass() != obj.getClass())
			return false;
		UIMessage other = (UIMessage) obj;
		if(m_bundle == null) {
			if(other.m_bundle != null)
				return false;
		} else if(!m_bundle.equals(other.m_bundle))
			return false;
		if(m_code == null) {
			if(other.m_code != null)
				return false;
		} else if(!m_code.equals(other.m_code))
			return false;
		if(m_errorNode == null) {
			if(other.m_errorNode != null)
				return false;
		} else if(!m_errorNode.equals(other.m_errorNode))
			return false;
		if(!Arrays.equals(m_parameters, other.m_parameters))
			return false;
		if(m_type == null) {
			if(other.m_type != null)
				return false;
		} else if(!m_type.equals(other.m_type))
			return false;
		return true;
	}

	@Nonnull
	@Override
	public String toString() {
		return getMessage();
	}
}
