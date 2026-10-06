package to.etc.domui.component.misc;

import org.eclipse.jdt.annotation.NonNull;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.Page;
import to.etc.domui.dom.html.UrlPage;
import to.etc.domui.server.RequestContextImpl;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 11/30/15.
 */
public class CloseOnClickPanel extends Div {
	//@OverridingMethodsMustInvokeSuper
	@Override
	public void createContent() throws Exception {
		appendCreateJS("new WebUI.closeOnClick('" + getActualID() + "');");
	}

	/**
	 * Close the other panels as soon as this one is added to the page. This must not wait until
	 * this panel is built: building happens while the page walks its children, and the panel
	 * that goes away is often a sibling of this one (the same button pressed twice), so removing
	 * it then changes the child list being walked.
	 */
	@Override
	public void onAddedToPage(Page p) {
		super.onAddedToPage(p);
		cleanUpPanels();
	}

	/**
	 * Close all opened panels except the one that is clicked on
	 */
	private void cleanUpPanels() {
		UrlPage page = getPage().getBody();
		page.getDeepChildren(CloseOnClickPanel.class).stream().forEach(panel -> {
			if(!panel.equals(this))
				panel.remove();
		});
	}

	/**
	 * Return T if this thing is closed (not visible)
	 * @return
	 */
	public boolean isClosed() {
		return !isAttached();
	}

	/**
	 * Close this thing (make it invisible).
	 */
	public void close() {
		remove();
		appendJavascript("WebUI.closeOnClick.markClosed('" + getActualID() + "');");
	}

	public void webActionCLOSEMENU(@NonNull RequestContextImpl context) throws Exception {
		remove();
	}
}
