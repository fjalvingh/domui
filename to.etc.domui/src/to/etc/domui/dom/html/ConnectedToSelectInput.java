package to.etc.domui.dom.html;

import to.etc.domui.component.input.*;
import to.etc.domui.dom.header.*;

public class ConnectedToSelectInput extends TextStr {

	public static interface IConnectableToInput {
		Select getSelectControl() throws Exception;
	}

	private IConnectableToInput m_connectedControl;

	/**
	 * If T, drops down connected select when down arrow is pressed. T by default.
	 */
	private boolean m_doesDropDown = true;

	/**
	 * Force the javascript to load when this panel is used.
	 * @see to.etc.domui.dom.html.NodeBase#onAddedToPage(to.etc.domui.dom.html.Page)
	 */
	@Override
	public void onAddedToPage(Page p) {
		getPage().addHeaderContributor(HeaderContributor.loadJavascript("$js/jquery.connectedToSelectInput.js"), 100);
	}

	/**
	 * Initialize client side jquery plugin to support component on client side. Needs to be executed when both input and connected select have assigned actualIds.
	 * @throws Exception
	 */
	public void initialize() throws Exception {
		if(m_connectedControl == null || m_connectedControl.getSelectControl() == null) {
			throw new IllegalStateException(IConnectableToInput.class.getName() + " not connected to " + ConnectedToSelectInput.class.getName());
		}
		if(m_connectedControl.getSelectControl().internalGetID() == null) {
			throw new IllegalStateException("Connected select is not built at " + this);
		}
		appendCreateJS("ConnectedToSelectInput.initialize('" + getActualID() + "','" + m_connectedControl.getSelectControl().getActualID() + "', " + m_doesDropDown + ")");
	}

	public IConnectableToInput getConnectedControl() {
		return m_connectedControl;
	}

	public void setConnectedControl(IConnectableToInput connectedControl) {
		m_connectedControl = connectedControl;
	}

	/**
	 * Returns if drop down of connected select list is enabled using down arrow key inside input.
	 * @return
	 */
	public boolean isDoesDropDown() {
		return m_doesDropDown;
	}

	/**
	 * Set to enable/disable showing and drop down of connected select via down key. 
	 * @param doesDropDown
	 */
	public void setDoesDropDown(boolean doesDropDown) {
		m_doesDropDown = doesDropDown;
	}
}
