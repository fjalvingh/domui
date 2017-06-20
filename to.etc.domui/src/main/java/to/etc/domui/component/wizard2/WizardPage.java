package to.etc.domui.component.wizard2;

import to.etc.domui.dom.html.*;

import javax.annotation.*;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 20-6-17.
 */
@DefaultNonNull
abstract public class WizardPage extends Div {
	private final String m_pageLabel;

	public WizardPage(String pageLabel) {
		m_pageLabel = pageLabel;
	}

	public String getPageLabel() {
		return m_pageLabel;
	}

	@Override abstract public void createContent() throws Exception;

}
