package to.etc.domui.component.layout;

import to.etc.domui.dom.css.DisplayType;
import to.etc.domui.dom.errors.*;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.dom.html.Page;
import to.etc.domui.util.*;
import to.etc.webapp.nls.*;

/**
 * This is a CaptionedPanel which captures and displays errors for the tree 
 * it is in. This is the default component used by the framework when no
 * other components captures a generated error; in that case the toplevel
 * code catches the error, creates an ErrorPanel component and inserts that
 * as the first node in the page's DOM.
 * This component only changes the look of the CaptionedPanel and adds listeners
 * for the errors.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 17, 2008
 */
public class ErrorPanel extends CaptionedPanel implements IErrorMessageListener {
	private IErrorFence		m_errorFence;

	/**
	 * Create a default error panel.
	 */
	public ErrorPanel() {
		super(NlsContext.getGlobalMessage(Msgs.UI_ERROR_HEADER), new Div());
		setDisplay(DisplayType.NONE);
		getTitleContainer().setCssClass("ui-err-caption");
		setCssClass("ui-err-outer");
	}

	/**
	 * When I'm added to a page register myself as an error listener for that page.
	 * @see to.etc.domui.dom.html.NodeBase#onAddedToPage(to.etc.domui.dom.html.Page)
	 */
	@Override
	public void onAddedToPage(Page p) {
		super.onAddedToPage(p);
		m_errorFence = DomUtil.getMessageFence(this);
		m_errorFence.addErrorListener(this);
	}

	/**
	 * When I'm removed from a page I may no longer handle it's errors, so remove
	 * myself from the error listener chain.
	 *
	 * @see to.etc.domui.dom.html.NodeBase#onRemoveFromPage(to.etc.domui.dom.html.Page)
	 */
	@Override
	public void onRemoveFromPage(Page p) {
		super.onRemoveFromPage(p);
		m_errorFence.removeErrorListener(this);
	}

	/**
	 * Adds the new error message to this panel, making it visible.
	 * @see to.etc.domui.dom.errors.IErrorMessageListener#errorMessageAdded(to.etc.domui.dom.html.Page, to.etc.domui.dom.errors.UIMessage)
	 */
	public void errorMessageAdded(Page pg, UIMessage m) {
		Div d = new Div();
		d.setUserObject(m);
		d.setButtonText(m.getMessage());
		getContent().add(d);
		if(getContent().getChildCount() == 1)
			setDisplay(DisplayType.BLOCK);
		if(m.getErrorNode() != null) {
			m.getErrorNode().addCssClass("ui-input-err");
		}
	}

	/**
	 * Removes the error message from this panel, rendering it invisible.
	 * @see to.etc.domui.dom.errors.IErrorMessageListener#errorMessageRemoved(to.etc.domui.dom.html.Page, to.etc.domui.dom.errors.UIMessage)
	 */
	public void errorMessageRemoved(Page pg, UIMessage m) {
		for(NodeBase b : getContent()) {
			if(b.getUserObject() == m) {
				//-- Remove this object!
				b.remove();
				if(getContent().getChildCount() == 0)
					setDisplay(DisplayType.NONE);
				if(m.getErrorNode() != null) {
					m.getErrorNode().removeCssClass("ui-input-err");
				}
				return;
			}
		}
	}
}
