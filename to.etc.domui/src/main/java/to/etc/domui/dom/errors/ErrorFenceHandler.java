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

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import to.etc.domui.dom.html.NodeContainer;
import to.etc.domui.server.DomApplication;
import to.etc.domui.util.DomUtil;
import to.etc.webapp.nls.IBundleCode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * When controls or business logic encounters errors that need to be
 * reported back to the user they add an error to either a control (usually
 * for validation/conversion errors) or to the page itself (for errors where
 * there's no clear "location" where the problem has occurred).
 * <p>
 * Making these errors visible is not the responsibility of a component, but
 * is delegated to one or more ErrorMessageListeners. These listeners get
 * called when an error is registered with a component (or when an error
 * is cleared).
 * <p>
 * The error listener is responsible for handling the actual reporting of the error,
 * and it usually does this by altering the output tree, for instance by adding
 * the error message to the page's defined "error box" and making that box visible. Other
 * listeners can change the CSS Class of the error node in question, causing it to be
 * displayed in a different color for instance.
 * <p>
 * If a page has no registered error handlers it "inherits" the default error handlers
 * from the current Application. By overriding that one you can easily alter the way
 * errors are reported in the entire application.
 * <p>
 * Special components that handle error messages also exist, and these components usually
 * register themselves as listeners when they are added to the tree. This is the best method
 * of handling error reporting because the page designer can easily determine where they are
 * shown.
 */
public class ErrorFenceHandler implements IErrorFence {
	private static final Logger LOG = LoggerFactory.getLogger(ErrorFenceHandler.class);

	private NodeContainer m_container;

	/**
	 * The list of thingies that need to know about page errors.
	 */
	private List<IErrorMessageListener> m_errorListeners = Collections.EMPTY_LIST;

	private List<UIMessage> m_messageList = Collections.EMPTY_LIST;

	public ErrorFenceHandler(NodeContainer container) {
		m_container = container;
	}

	public NodeContainer getContainer() {
		return m_container;
	}

	/**
	 * Add a new error message listener to the page.
	 */
	@Override
	public void addErrorListener(@NonNull IErrorMessageListener eml) {
		if(m_errorListeners == Collections.EMPTY_LIST)
			m_errorListeners = new ArrayList<IErrorMessageListener>(4);
		if(!m_errorListeners.contains(eml))
			m_errorListeners.add(eml);
	}

	/**
	 * Discard an error message listener.
	 */
	@Override
	public void removeErrorListener(@NonNull IErrorMessageListener eml) {
		m_errorListeners.remove(eml);
	}

	@Override
	public void addMessage(@NonNull UIMessage uim) {
		if(!m_messageList.contains(uim)) { ////prevent double adding of same uim
			if(m_messageList == Collections.EMPTY_LIST)
				m_messageList = new ArrayList<UIMessage>(15);
			m_messageList.add(uim);
		}

		// ; now call all pending listeners. If this page has NO listeners we use the application default.
		if(m_errorListeners.isEmpty()) {
			//-- No default listeners: this means errors will not be visible. Ask the application to add an error handling component.
			DomApplication.get().addDefaultErrorComponent(getContainer()); // Ask the application to add,

			//-- jal 20171115 If that component set a new fence then delegate to there.
			IErrorFence fence = DomUtil.getMessageFence(getContainer());
			if(fence != this) {
				fence.addMessage(uim);
				return;
			}
		}
		for(IErrorMessageListener eml : m_errorListeners) {
			try {
				eml.errorMessageAdded(uim);
			} catch(Exception x) {
				LOG.error("Exception in error listener: " + x, x);
			}
		}
	}

	@Override
	public void removeMessage(@NonNull UIMessage uim) {
		if(!m_messageList.remove(uim)) // Must be known to the page or something's wrong..
			return;

		//-- Call the listeners.
		List<IErrorMessageListener> list = m_errorListeners;
		for(IErrorMessageListener eml : new ArrayList<IErrorMessageListener>(list)) {
			try {
				eml.errorMessageRemoved(uim);
			} catch(Exception x) {
				LOG.error("Error in removing error fence message: " + x, x);
			}
		}
	}

	@Override
	public void clearGlobalMessagesByGroup(@Nullable String code) {
		List<UIMessage> todo = new ArrayList<>();
		for(UIMessage m : m_messageList) {
			if(code != null && code.equals(m.getGroup()))
				todo.add(m);
		}

		//-- Remove all messages from the list,
		for(UIMessage m : todo)
			removeMessage(m);
	}

	@Override
	public void clearGlobalMessages(@Nullable IBundleCode code) {
		List<UIMessage> todo = new ArrayList<>();
		for(UIMessage m : m_messageList) {
			if(m.getErrorNode() == null && (code == null || code.equals(m.getCode())))
				todo.add(m);
		}

		//-- Remove all messages from the list,
		for(UIMessage m : todo)
			removeMessage(m);
	}

	@Override
	public List<UIMessage> getMessageList() {
		return m_messageList;
	}
}
