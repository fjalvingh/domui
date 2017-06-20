package to.etc.domui.component.wizard2;

import to.etc.domui.component.layout.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.themes.*;
import to.etc.webapp.*;
import to.etc.webapp.nls.*;

import java.util.*;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 20-6-17.
 */
abstract public class WizardDialog extends Dialog {
	static private final BundleRef BUNDLE = BundleRef.create(WizardDialog.class, "messages");

	private List<WizardPage> m_pageList = new ArrayList<>();

	private WizardPage m_currentPage;

	private final Div m_pageDiv = new Div();

	private boolean m_building;

	abstract protected void createPages() throws Exception;

	@Override final public void createContent() throws Exception {
		m_building = true;
		m_pageList.clear();
		createPages();
		m_building = false;

		if(m_pageList.size() == 0)
			throw new ProgrammerErrorException("Please add wizard pages with addPage() inside the createPages() method");

		getButtonBar().addButton(BUNDLE.getString("wizardstep.default.cancelbutton"), Theme.BTN_CANCEL, click -> {});
		getButtonBar().addButton(BUNDLE.getString("wizardstep.default.backbutton"), "THEME/btnBack.png", click -> prevPage());
		getButtonBar().addButton(BUNDLE.getString("wizardstep.default.nextbutton"), "THEME/btnNext.png", click -> nextPage());
		getButtonBar().addButton(BUNDLE.getString("wizardstep.default.endbutton"), Theme.BTN_CONFIRM, click -> {});

		//-- Your data binding for button state was OK, so that goes here somewhere

		WizardNavigatorFragment navigator = new WizardNavigatorFragment(this);
		add(navigator);

		//-- How can we use data binding to make sure that the ^^^ navigator updates its "current page"?

		m_pageDiv.removeAllChildren();
		add(m_pageDiv);
		m_pageDiv.setCssClass("ui-wzdl-page");

		setWizardPage(0);
	}

	private int getCurrentPageIndex() {
		return m_pageList.indexOf(m_currentPage);
	}

	private void nextPage() {
		int index = getCurrentPageIndex();
		if(index + 1 >= m_pageList.size())
			return;

		//-- We want to LEAVE the current page. Can we? What should we cause to be done here? To what?


		//-- Assuming we can-
		setWizardPage(index + 1);
	}

	private void prevPage() {
		int index = getCurrentPageIndex();
		if(index <= 0)
			return;

		//-- We want to go BACK to a previous page page. Can we? What should we cause to be done here? To what?


		//-- Assuming we can-
		setWizardPage(index - 1);
	}

	private void setWizardPage(int index) {
		if(index < 0 || index >= m_pageList.size())
			throw new IllegalStateException("Bad page index");
		WizardPage currentPage = m_currentPage;
		if(null != currentPage) {
			currentPage.remove();
			m_currentPage = null;
		}
		currentPage = m_pageList.get(index);
		m_pageDiv.add(currentPage);
		m_currentPage = currentPage;
	}

	protected void addPage(WizardPage wizardPage) {
		if(! m_building)
			throw new IllegalStateException("You cannot change the wizard's pages after it has been built");
		m_pageList.add(wizardPage);
	}

	public List<WizardPage> getPageList() {
		return m_pageList;
	}

	public WizardPage getCurrentPage() {
		return m_currentPage;
	}
}
