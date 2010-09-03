package to.etc.domui.component.layout;

import to.etc.domui.dom.html.*;

/**
 * A panel containing multiple tabs. Each tab consists of two components: the
 * tab label component and the tab page body.
 * Render tabs in multiple lines if component width is not enough to show all tabs.
 * To have tabs rendered into single line with available scrollers use {@link ScollableTabPanel}.
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
		setCssClass("ui-tab-c");

		//-- Adjust selected tab index
		if(getCurrentTab() >= getTabCount() || getCurrentTab() < 0)
			internalSetCurrentTab(0);

		//-- Create the TAB structure..
		Div hdr = new Div();
		add(hdr); // The div containing the tab buttons
		hdr.setCssClass("ui-tab-hdr");
		Ul u = new Ul();
		m_tabul = u;
		hdr.add(u);
		renderTabPanels(m_tabul, this);
	}
}
