package to.etc.domuidemo.pages.special;

import to.etc.domui.component.wizard2.*;
import to.etc.domui.dom.html.*;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 20-6-17.
 */
public class MyWizardDialog extends WizardDialog {
	@Override protected void createPages() throws Exception {
		title("My first Wizard");
		addPage(new PageOne());
		addPage(new PageTwo());
	}
}

class PageOne extends WizardPage {
	public PageOne() {
		super("Select object");
	}

	@Override public void createContent() throws Exception {
		add(new HTag(1, "Just something for page 1"));
	}
}

class PageTwo extends WizardPage {
	public PageTwo() {
		super("Insert somewhere ;)");
	}

	@Override public void createContent() throws Exception {
		add(new HTag(1, "Just something for page 2"));
	}
}
