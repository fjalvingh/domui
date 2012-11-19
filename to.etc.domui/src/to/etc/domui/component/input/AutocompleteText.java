package to.etc.domui.component.input;

import javax.annotation.*;


/**
 * String input that is connected to select control. Connected select control provides source strings for autocomplete functionality.
 * 
 *
 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
 * Created on Nov 7, 2012
 */
class AutocompleteText extends TextStr {

	@Nullable
	private ISelectProvider m_selectProvider;

	/**
	 * Initialize client side javascript plugin to support component on client side. Needs to be executed when both input and connected select have assigned actualIds.
	 * @throws Exception
	 */
	void initializeJS() throws Exception {
		if(m_selectProvider == null || m_selectProvider.getSelectControl() == null) {
			throw new IllegalStateException(ISelectProvider.class.getName() + " not connected to " + AutocompleteText.class.getName());
		}
		appendCreateJS("WebUI.initAutocomplete('" + getActualID() + "','" + m_selectProvider.getSelectControl().getActualID() + "')");
	}

	@Nonnull
	ISelectProvider getSelectProvider() {
		return m_selectProvider;
	}

	public void setSelectProvider(@Nonnull ISelectProvider selectProvider) {
		m_selectProvider = selectProvider;
	}

	boolean hasSelectProvider() {
		return m_selectProvider != null;
	}
}
