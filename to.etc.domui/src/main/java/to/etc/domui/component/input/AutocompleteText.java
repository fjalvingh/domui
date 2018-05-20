package to.etc.domui.component.input;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.dom.html.Select;


/**
 * String input that is connected to select control. Connected select control provides source strings for autocomplete functionality.
 *
 *
 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
 * Created on Nov 7, 2012
 */
class AutocompleteText extends TextStr {

	@Nullable
	private Select m_select;

	/**
	 * Initialize client side javascript plugin to support component on client side. Needs to be executed when both input and connected select have assigned actualIds.
	 * @throws Exception
	 */
	void initializeJS() throws Exception {
		appendCreateJS("WebUI.initAutocomplete('" + getActualID() + "','" + getSelect().getActualID() + "')");
	}

	@NonNull
	protected Select getSelect() {
		Select select = m_select;
		if(select == null) {
			throw new IllegalStateException(Select.class.getName() + " not connected to " + AutocompleteText.class.getName());
		}
		return select;
	}

	protected void setSelect(@NonNull Select select) {
		m_select = select;
	}

	protected boolean hasSelect() {
		return m_select != null;
	}
}
