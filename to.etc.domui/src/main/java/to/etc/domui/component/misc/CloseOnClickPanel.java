package to.etc.domui.component.misc;

import javax.annotation.*;

import to.etc.domui.dom.html.*;
import to.etc.domui.server.*;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 11/30/15.
 */
public class CloseOnClickPanel extends Div {
	@OverridingMethodsMustInvokeSuper
	@Override
	public void createContent() throws Exception {
		cleanUpPanels();
		appendCreateJS("new WebUI.closeOnClick('" + getActualID() + "');");
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

	public void webActionCLOSEMENU(@Nonnull RequestContextImpl context) throws Exception {
		remove();
	}
}
