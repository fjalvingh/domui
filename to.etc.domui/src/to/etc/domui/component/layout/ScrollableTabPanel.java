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

import to.etc.domui.dom.css.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.util.*;

/**
 * Customization of {@link TabPanel} that render tabs in single line, provide scroller buttons if needed.
 *
 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
 * Created on Sep 2, 2010
 */
public class ScrollableTabPanel extends TabPanelBase {
	private Ul m_tabul;

	/** Used to store scrollable header container div. */
	private Div m_scrollNavig;

	public ScrollableTabPanel() {
		super(false);
	}

	public ScrollableTabPanel(final boolean markErrorTabs) {
		super(markErrorTabs);
	}

	@Override
	public void createContent() throws Exception {
		setCssClass("ui-stab-c");

		//-- Adjust selected tab index
		if(getCurrentTab() >= getTabCount() || getCurrentTab() < 0)
			internalSetCurrentTab(0);

		//Make scroll container div around tab headers and scroll buttons.
		m_scrollNavig = new Div();
		m_scrollNavig.setCssClass("ui-stab-scrl");
		add(m_scrollNavig);

		Div leftArrow = new Div();
		m_scrollNavig.add(leftArrow);
		leftArrow.setCssClass("ui-stab-scrl-left");
		leftArrow.setOnClickJS("WebUI.scrollLeft(this);");
		leftArrow.setFloat(FloatType.LEFT);

		Div rightArrow = new Div();
		m_scrollNavig.add(rightArrow);
		rightArrow.setCssClass("ui-stab-scrl-right");
		rightArrow.setOnClickJS("WebUI.scrollRight(this);");
		rightArrow.setFloat(FloatType.RIGHT);

		appendCreateJS("WebUI.recalculateScrollers('" + m_scrollNavig.getActualID() + "');$(window).resize(function(){WebUI.recalculateScrollers('" + m_scrollNavig.getActualID() + "');});");

		//-- Create the TAB structure..
		Div hdr = new Div();
		m_scrollNavig.add(hdr); // The div containing the tab buttons
		hdr.setCssClass("ui-stab-hdr");
		Ul u = new Ul();
		m_tabul = u;

		//We have to ensure that tabs captions can be rendered in single line.
		hdr.setOverflow(Overflow.HIDDEN);
		hdr.setFloat(FloatType.NONE);
		m_tabul.setWidth("3000px");
		m_tabul.setMarginLeft("0px");
		m_tabul.setPadding("0px");
		hdr.add(u);
		Div bgFill = new Div();
		bgFill.setCssClass("ui-tab-ibte");
		hdr.add(bgFill);
		renderTabPanels(m_tabul, this);
		appendCreateJS(JavascriptUtil.disableSelection(m_tabul));
	}

	@Override
	protected void onUnshelve() throws Exception {
		if(m_scrollNavig != null) {
			//We have to handle tab scrollers after page is reloaded due to unshelve.
			appendJavascript("WebUI.recalculateScrollers('" + m_scrollNavig.getActualID() + "');");
		}
	}
}
