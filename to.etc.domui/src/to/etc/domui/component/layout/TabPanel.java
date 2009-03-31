package to.etc.domui.component.layout;

import java.util.*;

import to.etc.domui.dom.css.*;
import to.etc.domui.dom.html.*;

/**
 * A panel containing multiple tabs. Each tab consists of two components: the
 * tab label component and the tab page body.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 1, 2008
 */
public class TabPanel extends Div {
	private static class TabInstance {
		private NodeBase	m_label;
		private NodeBase	m_content;
		private Img			m_img;
		private Li			m_tab;

		public TabInstance(NodeBase label, NodeBase content, Img img) {
			m_label = label;
			m_content = content;
			m_img = img;
		}

		public NodeBase getContent() {
			return m_content;
		}

		public NodeBase getLabel() {
			return m_label;
		}
		public Li getTab() {
			return m_tab;
		}
		public void setTab(Li tab) {
			m_tab = tab;
		}
		public Img getImg() {
			return m_img;
		}
	}

	private List<TabInstance> 	m_tablist = new ArrayList<TabInstance>();

	/** The index for the currently visible tab. */
	private int					m_currentTab;

	private Ul					m_tabul;

	public TabPanel() {}

	/**
	 * Simple form for adding a tab which contains a text tabel.
	 *
	 * @param content
	 * @param label
	 */
	public void add(NodeBase content, String label) {
		TextNode tn = new TextNode(label);
		add(content, tn);
	}
	public void add(NodeBase content, String label, String icon) {
		TextNode tn = new TextNode(label);
		add(content, tn, icon);
	}

	/**
	 * Add a tab page with a complex label part.
	 * @param content
	 * @param tablabel
	 */
	public void		add(NodeBase content, NodeBase tablabel) {
		m_tablist.add(new TabInstance(tablabel, content, null));
		if(! isBuilt())
			return;

		//-- Render the new thingies.
	}

	public void		add(NodeBase content, NodeBase tablabel, String icon) {
		Img i = new Img();
		i.setSrc(icon);
		i.setCssClass("ui-tab-icon");
		i.setBorder(0);
		m_tablist.add(new TabInstance(tablabel, content, i));
		if(! isBuilt())
			return;

		//-- Render the new thingies.
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
		if(getCurrentTab() >= m_tablist.size())
			m_currentTab = 0;

		//-- Create the TAB structure.. 
		Div	hdr	= new Div();
		add(hdr);								// The div containing the tab buttons
		hdr.setCssClass("ui-tab-hdr");
		Ul	u = new Ul();
		m_tabul = u;
		hdr.add(u);
		int	index = 0;
		for(TabInstance ti : m_tablist) {
			renderLabel(index, ti);

			//-- Add the body to the tab's main div.
			add(ti.getContent());
			ti.getContent().setClear(ClearType.BOTH);
			ti.getContent().setDisplay(m_currentTab == index ? DisplayType.BLOCK : DisplayType.NONE);
			ti.getContent().addCssClass("ui-tab-pg");
			index++;
		}
	}

	private void	renderLabel(int index, TabInstance ti) {
		Li li = new Li();
		m_tabul.add(index, li);
		ti.setTab(li);								// Save for later use,
		li.setCssClass(index == m_currentTab ? "ui-tab-lbl ui-tab-sel" : "ui-tab-lbl");
		ATag a = new ATag();
		li.add(a);
		if(ti.getImg() != null)
			a.add(ti.getImg());
		a.add(ti.getLabel());						// Append the label.
		final int index_ = index;
		a.setClicked(new IClicked<ATag>() {
			public void clicked(ATag b) throws Exception {
				setCurrentTab(index_);
			}
		});
	}

	public int getCurrentTab() {
		return m_currentTab;
	}

	public void setCurrentTab(int index) {
		System.out.println("Switching to tab "+index);
		if(index == getCurrentTab() || index < 0 || index >= m_tablist.size())	// Silly index
			return;
		if(isBuilt()) {
			//-- We must switch the styles on the current "active" panel and the current "old" panel
			TabInstance	oldti = m_tablist.get(getCurrentTab());		// Get the currently active instance,
			TabInstance	newti = m_tablist.get(index);
			oldti.getContent().setDisplay(DisplayType.NONE);		// Switch displays on content
			newti.getContent().setDisplay(DisplayType.BLOCK);
			oldti.getTab().removeCssClass("ui-tab-sel");			// Remove selected indicator
			newti.getTab().addCssClass("ui-tab-sel");
		}
		m_currentTab = index;			// ORDERED!!! Must be below the above!!!
	}
}
