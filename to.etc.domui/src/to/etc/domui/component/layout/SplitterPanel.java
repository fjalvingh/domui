package to.etc.domui.component.layout;

import to.etc.domui.dom.header.*;
import to.etc.domui.dom.html.*;

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

	/**
	 * panelA, panelB and vertical/horozontal layout can not be changed after creation of splitter.
	 * @param pannelA left/top panel
	 * @param pannelB right/bottom panel
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
		appendJavascript("$(document).ready(function() {" + getMakeSplitterJavascriptCall() + "});");
	}

	/**
	 * Force the javascript to load when this panel is used.
	 * @see to.etc.domui.dom.html.NodeBase#onAddedToPage(to.etc.domui.dom.html.Page)
	 */
	@Override
	public void onAddedToPage(Page p) {
		getPage().addHeaderContributor(HeaderContributor.loadJavascript("$js/jquery.splitter.js"), 100);
	}

	public String getMakeSplitterJavascriptCall() {
		StringBuilder params = new StringBuilder();
		if(m_minASize > 0) {
			params.append("minAsize:" + m_minASize + ",");
		}
		if(m_maxASize > 0) {
			params.append("maxAsize:" + m_maxASize + ",");
		}
		if(m_minBSize > 0) {
			params.append("minBsize:" + m_minBSize + ",");
		}
		if(m_maxBSize > 0) {
			params.append("maxBsize:" + m_maxBSize + ",");
		}
		if(m_vertical) {
			params.append("splitVertical:true,");
		} else {
			params.append("splitHorizontal:true,");
		}
		return "$('#" + getActualID() + "').splitter({" + params.toString() + "A:$('#" + m_panelA.getActualID() + "'),B:$('#" + m_panelB.getActualID() + "'),closeableto:0});";
	}

	@Override
	protected void onUnshelve() throws Exception {
		//Since real splitter UI is actually created on browser side, we have to recreate it when page is unshelved.
		appendJavascript(getMakeSplitterJavascriptCall());
	}
}
