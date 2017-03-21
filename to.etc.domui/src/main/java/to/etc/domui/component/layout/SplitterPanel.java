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

import to.etc.domui.dom.header.*;
import to.etc.domui.dom.html.*;

import javax.annotation.*;
import java.util.*;
import java.util.stream.*;

/**
 * Splitter control is just wrapper DIV around javascript baset splitter implementation based on
 * jQuery.splitter.js - animated splitter plugin, version 1.0 (2010/01/02), author Kristaps Kukurs (contact@krikus.com)
 *
 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
 * Created on Aug 19, 2010
 */
public class SplitterPanel extends Div {
	private Div m_panelA;

	private Div m_panelB;

	private boolean m_vertical;

	private int m_minASize = 0;

	private int m_maxASize = 0;

	private int m_minBSize = 0;

	private int m_maxBSize = 0;

	/** Specifies position of splitter in percentages when user clicks on splitter button. */
	private int m_closableToPerc = 90;

	/** Specifies initial position of splitter in percentages. Default is -1, means no customization, position is automatically set show first panel. */
	private int m_initPosInPerc = -1;

	/**
	 * panelA, panelB and vertical/horozontal layout can not be changed after creation of splitter.
	 * @param panelA left/top panel
	 * @param panelB right/bottom panel
	 * @param vertical T for vertical, F for horizontal layout
	 */
	public SplitterPanel(Div panelA, Div panelB, boolean vertical) {
		m_panelA = panelA;
		m_panelB = panelB;
		m_vertical = vertical;
	}

	public Div getPanelA() {
		return m_panelA;
	}

	public Div getPanelB() {
		return m_panelB;
	}

	public boolean isVertical() {
		return m_vertical;
	}

	public int getMinASize() {
		return m_minASize;
	}

	public void setMinASize(int minASize) {
		m_minASize = minASize;
	}

	public int getMaxASize() {
		return m_maxASize;
	}

	public void setMaxASize(int maxASize) {
		m_maxASize = maxASize;
	}

	public int getMinBSize() {
		return m_minBSize;
	}

	public void setMinBSize(int minBSize) {
		m_minBSize = minBSize;
	}

	public int getMaxBSize() {
		return m_maxBSize;
	}

	public void setMaxBSize(int maxBSize) {
		m_maxBSize = maxBSize;
	}

	public int getClosableToPerc() {
		return m_closableToPerc;
	}

	public void setClosableToPerc(int closableToPerc) {
		m_closableToPerc = closableToPerc;
	}

	public int getInitPosInPerc() {
		return m_initPosInPerc;
	}

	public void setInitPosInPerc(int initPosInPerc) {
		m_initPosInPerc = initPosInPerc;
	}

	@Override
	public void createContent() throws Exception {
		if(m_vertical) {
			addCssClass("ui-splt-vert");
			m_panelA.removeCssClass("ui-splt-top");
			m_panelA.addCssClass("ui-splt-left");
			m_panelB.removeCssClass("ui-splt-bottom");
			m_panelB.addCssClass("ui-splt-right");
		} else {
			addCssClass("ui-splt-horz");
			m_panelA.removeCssClass("ui-splt-left");
			m_panelA.addCssClass("ui-splt-top");
			m_panelB.removeCssClass("ui-splt-right");
			m_panelB.addCssClass("ui-splt-bottom");
		}
		add(m_panelA);
		add(m_panelB);
		getActualID();
		appendCreateJS("$(document).ready(function() {" + getMakeSplitterJavascriptCall() + "});");
	}

	/**
	 * Force the javascript to load when this panel is used.
	 * @see to.etc.domui.dom.html.NodeBase#onAddedToPage(to.etc.domui.dom.html.Page)
	 */
	@Override
	public void onAddedToPage(Page p) {
		getPage().addHeaderContributor(HeaderContributor.loadJavascript("$js/jquery.splitter.js"), 100);
	}

	@Nonnull
	public String getMakeSplitterJavascriptCall() {
		Map<String, String> params = new HashMap<String, String>();

		addParamIfPositive(params, m_minASize, "minAsize");
		addParamIfPositive(params, m_maxASize, "maxAsize");
		addParamIfPositive(params, m_minBSize, "minBsize");
		addParamIfPositive(params, m_maxBSize, "maxBsize");

		if(m_vertical) {
			params.put("splitVertical", "true");
		} else {
			params.put("splitHorizontal", "true");
		}
		params.put("A", "$('#" + m_panelA.getActualID() + "')");
		params.put("B", "$('#" + m_panelB.getActualID() + "')");
		params.put("closeableto", m_closableToPerc + "");

		addParamIfPositive(params, m_initPosInPerc, "initPos");

		String paramStr = params.entrySet().stream().map(a -> "'" + a.getKey() + "':" + a.getValue()).collect(Collectors.joining(",", "{", "}"));

		return "$('#" + getActualID() + "').splitter(" + paramStr + ");";
	}

	@Nonnull
	public String resizeSplitterJavascriptCall() {
		return "$('#" + getActualID() + "').trigger(\"resize\");";
	}

	private void addParamIfPositive(@Nonnull Map<String, String> params, int value, @Nonnull String name) {
		if(value > 0) {
			params.put(name, value + "");
		}
	}
}
