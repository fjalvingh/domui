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
package to.etc.domui.component.layout;

import to.etc.domui.dom.html.*;
import to.etc.domui.util.*;

/**
 * A panel containing multiple tabs. Each tab consists of two components: the
 * tab label component and the tab page body.
 * Render tabs in multiple lines if component width is not enough to show all tabs.
 * To have tabs rendered into single line with available scrollers use {@link ScrollableTabPanel}.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 1, 2008
 */
public class TabPanel extends TabPanelBase {
	private Ul m_tabul;

	public TabPanel() {
		super(false);
	}

	public TabPanel(final boolean markErrorTabs) {
		super(markErrorTabs);
	}

	/**
	 * Build the tab. We generate a "sliding window" variant where the tabs are part of an
	 * "ul"; each content pane is a div.
	 * The complete generated structure looks like:
	 * <pre><![CDATA[
	 * 	<div class="ui-tab-c">
	 *
	 *
	 *  </div>
	 * ]]></pre>
	 *
	 * @see to.etc.domui.dom.html.NodeBase#createContent()
	 */
	@Override
	public void createContent() throws Exception {
		addCssClass("ui-tab-c");

		//-- Adjust selected tab index
		if(getCurrentTab() >= getTabCount() || getCurrentTab() < 0)
			internalSetCurrentTab(0);

		//-- Create the TAB structure..
		Div hdr = new Div("ui-tab-hdr");
		add(hdr); 									// The div containing the tab buttons
		Ul u = m_tabul = new Ul();
		hdr.add(u);

		Div cont = new Div("ui-tab-content");	// The content area showing the tab contents.
		add(cont);
		renderTabPanels(m_tabul, cont);
		appendCreateJS(JavascriptUtil.disableSelection(m_tabul));
	}
}
