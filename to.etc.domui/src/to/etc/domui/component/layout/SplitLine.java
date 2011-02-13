package to.etc.domui.component.layout;

import to.etc.domui.dom.header.*;
import to.etc.domui.dom.html.*;

/**
 * Experimental, do not use- Splitter which itself is just a DIV and which does not <i>contain</i>
 * the controlled regions but just has a reference to them. The controlled regions are, by definition,
 * the child before and the child after the splitter.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Feb 13, 2011
 */
public class SplitLine extends Div {
	@Override
	public void createContent() throws Exception {
		int myindex = getParent().findChildIndex(this);
		if(myindex <= 0 || myindex + 1 >= getParent().getChildCount())
			return;
		NodeBase a = getParent().getChild(myindex - 1);
		NodeBase b = getParent().getChild(myindex + 1);
		a.addCssClass("ui-xsplt-top");
		b.addCssClass("ui-xsplt-bottom");

		setCssClass("splitbarH");
		Div x = new Div();
		add(x);
		x.setCssClass("splitbuttonH");

		StringBuilder sb = new StringBuilder();
		sb.append("$(document).ready(function() {");

		//-- Splitter ui
		sb.append("$('#").append(getActualID()).append("').splitter({");
		sb.append("A: $('#").append(a.getActualID()).append("'),");
		sb.append("B: $('#").append(b.getActualID()).append("'),");
		sb.append("splitHorizontal:true");
		sb.append("});");

		//-- end ready
		sb.append("});");
		//		appendCreateJS(sb);
	}

	/**
	 * Force the javascript to load when this panel is used.
	 * @see to.etc.domui.dom.html.NodeBase#onAddedToPage(to.etc.domui.dom.html.Page)
	 */
	@Override
	public void onAddedToPage(Page p) {
		getPage().addHeaderContributor(HeaderContributor.loadJavascript("$js/jquery.splitter.js"), 100);
	}

}
