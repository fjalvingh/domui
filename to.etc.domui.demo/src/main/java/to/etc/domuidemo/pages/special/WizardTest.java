package to.etc.domuidemo.pages.special;

import to.etc.domui.component.buttons.*;
import to.etc.domui.component.wizard2.*;
import to.etc.domui.dom.html.*;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 20-6-17.
 */
public class WizardTest extends UrlPage {
	@Override public void createContent() throws Exception {
		DefaultButton button = new DefaultButton("Click to start", click -> open());
		add(button);
	}

	private void open() {
		WizardDialog dialog = new MyWizardDialog();
		add(dialog);
	}
}
