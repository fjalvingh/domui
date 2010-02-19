package to.etc.domui.component.fasthtml;

import to.etc.domui.dom.html.*;

public class HtmlMiniEditor extends TextArea {
	@Override
	public void createContent() throws Exception {
		appendCreateJS("$(\"#" + getActualID() + "\").wysiwyg({css:'/ui/$themes/blue/style.theme.css'});");
	}


}
