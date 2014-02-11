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
package to.etc.domui.component.delayed;

import to.etc.domui.dom.html.*;
import to.etc.domui.state.*;

/**
 * This is a div whose content can be refreshed automatically because the client polls
 * for changes every few seconds. The content for the div must be updated for every
 * call to checkForChanges().
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 15, 2009
 */
public class PollingDiv extends Div implements IPolledForUpdate {
	/**
	 * This is the method which gets called every few seconds. The default implementation
	 * just forces a rebuild on this component causing it to be rebuilt anew fully. Better
	 * implementations only change what's really needed to prevent excessive screen updates
	 * and flickering browser horror.
	 * @throws Exception
	 */
	@Override
	public void checkForChanges() throws Exception {
		forceRebuild();
		build();
	}

	/**
	 * Register as a poll thing when added to page.
	 * @see to.etc.domui.dom.html.NodeBase#onAddedToPage(to.etc.domui.dom.html.Page)
	 */
	@Override
	public void onAddedToPage(Page p) {
		getPage().getConversation().registerPoller(this);
	}

	/**
	 * Unregister as poller when removed.
	 * @see to.etc.domui.dom.html.NodeBase#onRemoveFromPage(to.etc.domui.dom.html.Page)
	 */
	@Override
	public void onRemoveFromPage(Page p) {
		ConversationContext cc = getPage().internalGetConversation();
		if(null != cc)
			cc.unregisterPoller(this);
	}
}
