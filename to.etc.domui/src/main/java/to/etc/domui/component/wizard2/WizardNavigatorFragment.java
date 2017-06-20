package to.etc.domui.component.wizard2;

import to.etc.domui.component.buttons.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.themes.*;

import java.util.*;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 20-6-17.
 */
public class WizardNavigatorFragment extends Div {
	private final WizardDialog m_wizard;

	/** The page currently shown as "current" */
	private WizardPage m_currentPage;

	public WizardNavigatorFragment(WizardDialog wizard) {
		m_wizard = wizard;
	}

	@Override public void createContent() throws Exception {
		List<WizardPage> pageList = m_wizard.getPageList();
		WizardPage current = m_currentPage = m_wizard.getCurrentPage();
		int currentPageIndex = pageList.indexOf(current);

		for(int pageNumber = 0; pageNumber < pageList.size(); pageNumber++) {
			if(pageNumber != 0)
				add(new Div("ui-wznf-sep"));
			WizardPage page = pageList.get(pageNumber);
			if(pageNumber < currentPageIndex)
				renderAsLink(page);
			else if(pageNumber == currentPageIndex)
				renderAsCurrent(page);
			else
				renderAsFuture(page);
		}
	}

	private void renderAsFuture(WizardPage page) {
		Div cont = new Div("ui-wznf-next");
		add(cont);
		cont.add(new Span(page.getPageLabel()));
	}

	private void renderAsCurrent(WizardPage page) {
		Div cont = new Div("ui-wznf-curr");
		add(cont);
		cont.add(new Span(page.getPageLabel()));
	}

	private void renderAsLink(WizardPage page) {
		Div cont = new Div("ui-wznf-prev");
		add(cont);
		LinkButton lb = new LinkButton(page.getPageLabel(), Theme.BTN_CHECKMARK, click -> {});
		cont.add(lb);
	}
}
