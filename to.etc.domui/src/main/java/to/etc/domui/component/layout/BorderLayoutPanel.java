package to.etc.domui.component.layout;

import java.util.*;

import javax.annotation.*;

import to.etc.domui.dom.header.*;
import to.etc.domui.dom.html.*;

public class BorderLayoutPanel extends Div {
	public enum Panel {
		NORTH, SOUTH, WEST, EAST, CENTER
	}

	final private Map<Panel, Div> m_panelMap = new HashMap<Panel, Div>();

	public BorderLayoutPanel() {
		select(Panel.CENTER);
		setCssClass("ui-blap");
	}

	public void select(@Nonnull Panel p) {
		if(null == p)
			throw new IllegalArgumentException("null not allowed");
		delegateTo(null);
		Div m = m_panelMap.get(p);
		if(null == m) {
			m = new Div();
			m.setCssClass("ui-layout-" + p.name().toLowerCase());
			m_panelMap.put(p, m);
			add(m);
		}
		delegateTo(m);
	}

	@Override
	protected void onForceRebuild() {
		select(Panel.CENTER);
	}

	@Override
	public void createContent() throws Exception {
	}

	@Override
	protected void afterCreateContent() throws Exception {
		super.afterCreateContent();
		if(m_panelMap.size() != 1)
			appendCreateJS("$('#" + getActualID() + "').layout();");
	}

	@Override
	public void onAddedToPage(Page p) {
		getPage().addHeaderContributor(HeaderContributor.loadJavascript("$js/jquery.layout.js"), 101);
	}
}
