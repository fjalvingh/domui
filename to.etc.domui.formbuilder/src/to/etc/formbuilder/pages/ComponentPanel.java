package to.etc.formbuilder.pages;

import java.util.*;

import javax.annotation.*;

import to.etc.domui.dom.html.*;

public class ComponentPanel extends Div {
	final private List<IFbComponent> m_componentList;

	public ComponentPanel(@Nonnull List<IFbComponent> componentList) {
		m_componentList = componentList;

	}

	@Override
	public void createContent() throws Exception {
		setCssClass("fb-cp");
//		HTag ht = new HTag(4);
//		add(ht);
//		ht.add("Components");
//		ht.setCssClass("fb-comp");

		for(IFbComponent comp : m_componentList) {
			PnlComponent pc = new PnlComponent(comp);
			add(pc);
		}
	}
}
