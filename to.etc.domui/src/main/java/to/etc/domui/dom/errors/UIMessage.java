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

import to.etc.domui.dom.html.*;
import to.etc.domui.logic.errors.*;
import to.etc.domui.trouble.*;
import to.etc.domui.util.*;
import to.etc.webapp.nls.*;

import javax.annotation.*;
import java.util.*;

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
	@Nonnull
	static private final Object[] NONE = new Object[0];


	/** The message bundle to use, or null if the deprecated "global messages" are used. */
	@Nonnull
	private BundleRef m_bundle;

	/** The error message code for the error that has occured. This exists always and is a lookup into the error NLS messages. */
	@Nonnull
	private String m_code;

	/** The type of the message (error, warning, informational); this mainly defines whether actions continue, and it defines an icon to show. */
	@Nonnull
	final private MsgType m_type;

	/** When set this is used in error messages as an indication of which input field contains the error. It usually contains the value for the "label" of the control. */
	private String m_errorLocation;

	/** For errors that have parameters - these are the parameters. This is null if no parameters are present. */
	@Nonnull
	private Object[] m_parameters;

	/**
	 * The Node that this error pertains to. It is usually a Component-type node.
	 */
	private NodeBase m_errorNode;

	/** The group links messages together, so a bunch of them can be deleted at the same time. */
	private String m_group;

	/** The message key is the bundle class' package name plus the message code, separated by a '.' */
	final private String m_key;

	/**
	 * Create an error message container.
	 * @param errorNode			If not-null this is the node that "owns" the error. This node will show a visual indication of the fact that it contains an error.
	 * @param errorLocation		If not-null this is a user-understandable name of the input item that contains the error. It usually is the "label" associated with the problem.
	 * @param type				The type of message: error, warning or info.
	 * @param br				The bundle containing the message for the code. If this is null (deprecated) the "global bundle set" is used *WHICH IS DEPRECATED*.
	 * @param code				The code for the message.
	 * @param parameters		If needed a set of parameters to render into the message.
	 */
	protected UIMessage(@Nullable NodeBase errorNode, @Nullable String errorLocation, @Nonnull MsgType type, @Nonnull BundleRef br, @Nonnull String code, @Nullable Object[] parameters) {
		this(errorNode, errorLocation, type, br, code, parameters, null);
	}

	protected UIMessage(@Nullable NodeBase errorNode, @Nullable String errorLocation, @Nonnull MsgType type, @Nonnull BundleRef br, @Nonnull String code, @Nullable Object[] parameters,
		@Nullable String group) {
		if(code == null)
			throw new NullPointerException("Message code cannot be null");
		if(type == null)
			throw new NullPointerException("Message type cannot be null");
		m_bundle = br;
		m_errorNode = errorNode;
		m_code = code;
		m_parameters = parameters == null ? NONE : parameters;
		m_type = type;
		m_errorLocation = errorLocation;
		m_group = group;
		m_key = br.getBundleKey() + "#" + code;
	}

	/**
	 * Return a unique key for the message as "bundle name" '#' "code". The bundle name is defined as the bundle's class package + '.' + message file name without extension
	 * @return
	 */
	public String getMessageKey() {
		return m_key;
	}

	@Nonnull
	public UIMessage group(@Nonnull String name) {
		m_group = name;
		return this;
	}

	@Nullable
	public String getGroup() {
		return m_group;
	}

	public String getCode() {
		return m_code;
	}

	/**
	 * Returns the message bundle the code is in, or null if the code is a global message code (deprecated).
	 * @return
	 */
	@Nullable
	public BundleRef getBundle() {
		return m_bundle;
	}

	@Nonnull
	public Object[] getParameters() {
		return m_parameters;
	}

	@Nullable
	public NodeBase getErrorNode() {
		return m_errorNode;
	}

	public void setErrorNode(@Nullable NodeBase errorNode) {
		m_errorNode = errorNode;
	}

	@Nonnull
	public MsgType getType() {
		return m_type;
	}

	/**
	 * When set this is used in error messages as an indication of which input field contains the
	 * error. It usually contains the value for the "label" of the control.
	 * @return
	 */
	@Nullable
	public String getErrorLocation() {
		return m_errorLocation;
	}

	public void setErrorLocation(String errorLocation) {
		m_errorLocation = errorLocation;
	}

	/**
	 * Chaining setter for setErrorLocation.
	 *
	 * @param errorLocation
	 * @return
	 */
	@Nonnull
	public UIMessage location(@Nullable String errorLocation){
		m_errorLocation = errorLocation;
		return this;
	}

	/**
	 * Returns the message part of the error message, properly localized for the request's locale.
	 * @return
	 */
	@Nonnull
	public String getMessage() {
		if(m_bundle != null)
			return m_bundle.formatMessage(m_code, m_parameters);

		return Msgs.BUNDLE.formatMessage(m_code, m_parameters);
	}

	@Nonnull
	public static UIMessage create(@Nullable NodeBase n, @Nonnull ProblemInstance pi) {
		return new UIMessage(n, null, pi.getProblem().getSeverity(), pi.getProblem().getBundle(), pi.getProblem().getCode(), pi.getParameters());
	}

	@Nonnull
	static public UIMessage error(@Nonnull CodeException x) {
		return new UIMessage(null, null, MsgType.ERROR, x.getBundle(), x.getCode(), x.getParameters());
	}

	@Nonnull
	static public UIMessage error(@Nonnull UIException x) {
		return new UIMessage(null, null, MsgType.ERROR, x.getBundle(), x.getCode(), x.getParameters());
	}

	@Nonnull
	static public UIMessage error(NodeBase node, String errorLocation, @Nonnull BundleRef ref, @Nonnull String code, Object... param) {
		return new UIMessage(node, errorLocation, MsgType.ERROR, ref, code, param);
	}

	@Nonnull
	static public UIMessage error(String errorLocation, @Nonnull BundleRef ref, @Nonnull String code, Object... param) {
		return new UIMessage(null, errorLocation, MsgType.ERROR, ref, code, param);
	}

	@Nonnull
	static public UIMessage error(NodeBase node, @Nonnull BundleRef ref, @Nonnull String code, Object... param) {
		return new UIMessage(node, node.getErrorLocation(), MsgType.ERROR, ref, code, param);
	}

	@Nonnull
	static public UIMessage error(@Nonnull BundleRef ref, @Nonnull String code, Object... param) {
		return new UIMessage(null, null, MsgType.ERROR, ref, code, param);
	}

	@Nonnull
	static public UIMessage warning(NodeBase node, String errorLocation, @Nonnull BundleRef ref, @Nonnull String code, Object... param) {
		return new UIMessage(node, errorLocation, MsgType.WARNING, ref, code, param);
	}

	@Nonnull
	static public UIMessage warning(NodeBase node, @Nonnull BundleRef ref, @Nonnull String code, Object... param) {
		return new UIMessage(node, null, MsgType.WARNING, ref, code, param);
	}

	@Nonnull
	static public UIMessage warning(@Nonnull BundleRef ref, @Nonnull String code, Object... param) {
		return new UIMessage(null, null, MsgType.WARNING, ref, code, param);
	}

	@Nonnull
	static public UIMessage info(@Nonnull BundleRef ref, @Nonnull String code, Object... param) {
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
		if(m_group == null) {
			if(other.m_group != null)
				return false;
		} else if(!m_group.equals(other.m_group)) {
			return false;
		}
		if(m_errorLocation == null) {
			return other.m_errorLocation == null;
		} else
			return m_errorLocation.equals(other.m_errorLocation);


	}

	@Nonnull
	@Override
	public String toString() {
		return getMessage();
	}
}
