package to.etc.domui.component.layout;

import java.util.*;

import to.etc.domui.dom.css.*;
import to.etc.domui.dom.errors.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.util.*;

/**
 * This is the default in-component error handling panel, for components that
 * handle their own errors.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Dec 9, 2009
 */
public class ErrorMessageDiv extends Div implements IErrorMessageListener {
	private List<UIMessage> m_msgList = new ArrayList<UIMessage>();

	public ErrorMessageDiv(NodeContainer parent) {
		parent.setErrorFence();
		DomUtil.getMessageFence(parent).addErrorListener(this);
		setCssClass("ui-emd");
		setVisibility(VisibilityType.HIDDEN);
	}

	public ErrorMessageDiv() {
		setCssClass("ui-emd");
		setVisibility(VisibilityType.HIDDEN);
	}

	public void setAsErrorFence(NodeContainer parent) {
		parent.setErrorFence();
		DomUtil.getMessageFence(parent).addErrorListener(this);
	}

	public void errorMessageAdded(Page pg, UIMessage m) {
		if(m_msgList.contains(m))
			return;
		m_msgList.add(m);
		createErrorUI(m);
	}

	private void createErrorUI(UIMessage m) {
		if(getVisibility() != VisibilityType.VISIBLE) {
			setVisibility(VisibilityType.VISIBLE);
		}
		Div d = new Div();
		add(d);
		d.setCssClass("ui-emd-msg ui-emd-" + m.getType().name().toLowerCase());
		d.setUserObject(m);
		DomUtil.renderErrorMessage(d, m);
		if(m.getErrorNode() != null) {
			m.getErrorNode().addCssClass("ui-input-err");
		}
	}

	public void errorMessageRemoved(Page pg, UIMessage m) {
		if(!m_msgList.remove(m))
			return;

		for(NodeBase b : this) {
			if(b.getUserObject() == m) {
				//-- Remove this object!
				b.remove();
				if(m.getErrorNode() != null)
					m.getErrorNode().removeCssClass("ui-input-err");
				break;
			}
		}

		if(getChildCount() == 0) {
			setVisibility(VisibilityType.HIDDEN);
		}
	}
}
