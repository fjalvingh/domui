package to.etc.formbuilder.pages;

import to.etc.domui.dom.html.*;

public class ComponentPanel extends Div {
	public ComponentPanel() {}

	@Override
	public void createContent() throws Exception {
		setCssClass("fb-cp");
		HTag ht = new HTag(4);
		add(ht);
		ht.add("Components");
		ht.setCssClass("fb-comp");
	}

}
