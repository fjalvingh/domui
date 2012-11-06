package to.etc.domui.dom.html;

import to.etc.domui.component.input.*;
import to.etc.domui.dom.header.*;

public class ConnectedToSelectInput extends TextStr {

	public static interface IConnectableToInput {
		Select getSelectControl() throws Exception;
	}

	private IConnectableToInput m_connectedControl;

	/**
	 * Force the javascript to load when this panel is used.
	 * @see to.etc.domui.dom.html.NodeBase#onAddedToPage(to.etc.domui.dom.html.Page)
	 */
	@Override
	public void onAddedToPage(Page p) {
		getPage().addHeaderContributor(HeaderContributor.loadJavascript("$js/jquery.connectedToSelectInput.js"), 100);
	}

	public void initialize() throws Exception {
		if(m_connectedControl == null || m_connectedControl.getSelectControl() == null) {
			throw new IllegalStateException(IConnectableToInput.class.getName() + " not connected to " + ConnectedToSelectInput.class.getName());
		}
		if(m_connectedControl.getSelectControl().internalGetID() == null) {
			throw new IllegalStateException("Connected select is not built at " + this);
		}
		appendCreateJS("ConnectedToSelectInput.initialize('" + getActualID() + "','" + m_connectedControl.getSelectControl().getActualID() + "')");
	}

	public IConnectableToInput getConnectedControl() {
		return m_connectedControl;
	}

	public void setConnectedControl(IConnectableToInput connectedControl) {
		m_connectedControl = connectedControl;
	}
}
