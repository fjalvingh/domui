package to.etc.domui.component.misc;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.dom.html.Div;
import to.etc.domui.server.RequestContextImpl;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 22-7-18.
 */
public class PopinClosePanel extends Div {

	@Nullable
	private Runnable m_onClosed;

	@Override public void createContent() throws Exception {
		appendCreateJS("WebUI.registerPopinClose('#" + getActualID() + "');");
	}

	@Override
	public void componentHandleWebAction(@NonNull RequestContextImpl ctx, @NonNull String action) throws Exception {
		if("POPINCLOSE?".equals(action)) {
			handleClosing();
		} else
			super.componentHandleWebAction(ctx, action);
	}

	final public void closePanel() {
		appendJavascript("WebUI.popinClosed('#" + getActualID() + "');");
		handleClosing();
	}

	private void handleClosing() {
		remove();
		Runnable onClosed = getOnClosed();
		if(null != onClosed) {
			onClosed.run();
		}
	}

	@Nullable
	public Runnable getOnClosed() {
		return m_onClosed;
	}

	public void setOnClosed(@Nullable Runnable onClosed) {
		m_onClosed = onClosed;
	}
}
