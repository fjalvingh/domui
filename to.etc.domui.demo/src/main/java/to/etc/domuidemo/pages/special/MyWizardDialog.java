package to.etc.domuidemo.pages.special;

import to.etc.domui.component.wizard3.*;
import to.etc.domui.dom.html.*;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 20-6-17.
 */
public class MyWizardDialog extends to.etc.domui.component.wizard3.WizardDialog {
	@Override protected void createPages() throws Exception {
		title("My first Wizard");
		setTwoButtonBars();
		addStep(new PageOne());
		addStep(new PageTwo());
	}
}

class PageOne extends WizardStep {
	public PageOne() {
		super("Select object");
	}

	@Override public void createContent() throws Exception {
		add(new HTag(1, "Just something for page 1"));
	}
}

class PageTwo extends WizardStep {
	public PageTwo() {
		super("Insert somewhere ;)");
		setCancelButton();
		setFinishButton();
	}

	@Override public void createContent() throws Exception {
		add(new HTag(1, "Just something for page 2"));
	}
}
