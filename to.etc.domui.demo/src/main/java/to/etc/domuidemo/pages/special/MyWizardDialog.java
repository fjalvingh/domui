package to.etc.domuidemo.pages.special;

import to.etc.domui.component.wizard3.*;
import to.etc.domui.dom.html.*;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 20-6-17.
 */
public class MyWizardDialog extends AbstractWizardDialog {
	@Override protected void createSteps() throws Exception {
		title("My first Wizard");
		addStep(new PageOne());
		addStep(new PageTwo());
	}

	@Override
	protected void addCssClassButtons() throws Exception {
		addCssClassCancelButton("ui-wzdl-align-left");
	}
}

class PageOne extends AbstractWizardStep {
	public PageOne() {
		super("Select object");
	}

	@Override public void createContent() throws Exception {
		add(new HTag(1, "Just something for page 1"));
	}
}

class PageTwo extends AbstractWizardStep {
	public PageTwo() {
		super("Insert somewhere ;)");
		setCancelButton();
		setFinishButton();
	}

	@Override public void createContent() throws Exception {
		add(new HTag(1, "Just something for page 2"));
	}
}
