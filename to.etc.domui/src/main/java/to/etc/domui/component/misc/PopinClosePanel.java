package to.etc.domui.component.misc;

import org.eclipse.jdt.annotation.NonNull;
import to.etc.domui.dom.html.Div;
import to.etc.domui.server.RequestContextImpl;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 22-7-18.
 */
public class PopinClosePanel extends Div {
	@Override public void createContent() throws Exception {
		appendCreateJS("WebUI.registerPopinClose('#" + getActualID() + "');");
	}

	@Override
	public void componentHandleWebAction(@NonNull RequestContextImpl ctx, @NonNull String action) throws Exception {
		if("POPINCLOSE?".equals(action)) {
			closePanel();
		} else
			super.componentHandleWebAction(ctx, action);
	}

	protected void closePanel() {
		remove();
	}
}
