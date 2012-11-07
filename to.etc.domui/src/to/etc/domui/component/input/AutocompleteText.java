package to.etc.domui.component.input;

import to.etc.domui.dom.header.*;
import to.etc.domui.dom.html.*;

/**
 * String input that is connected to select control. Connected select control provides source strings for autocomplete functionality.
 * 
 *
 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
 * Created on Nov 7, 2012
 */
class AutocompleteText extends TextStr {

	private ISelectProvider m_selectProvider;

	/**
	 * Force the javascript to load when this component is used.
	 * @see to.etc.domui.dom.html.NodeBase#onAddedToPage(to.etc.domui.dom.html.Page)
	 */
	@Override
	public void onAddedToPage(Page p) {
		getPage().addHeaderContributor(HeaderContributor.loadJavascript("$js/jquery.autocompleteText.js"), 100);
	}

	/**
	 * Initialize client side javascript plugin to support component on client side. Needs to be executed when both input and connected select have assigned actualIds.
	 * @throws Exception
	 */
	void initializeJS() throws Exception {
		if(m_selectProvider == null || m_selectProvider.getSelectControl() == null) {
			throw new IllegalStateException(ISelectProvider.class.getName() + " not connected to " + AutocompleteText.class.getName());
		}
		appendCreateJS("AutocompleteText.initialize('" + getActualID() + "','" + m_selectProvider.getSelectControl().getActualID() + "')");
	}

	ISelectProvider getSelectProvider() {
		return m_selectProvider;
	}

	public void setSelectProvider(ISelectProvider selectProvider) {
		m_selectProvider = selectProvider;
	}
}
