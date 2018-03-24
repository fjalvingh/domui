package to.etc.domui.component.delayed;

import to.etc.domui.component.misc.MsgBox;
import to.etc.domui.dom.html.NodeContainer;
import to.etc.domui.util.Msgs;
import to.etc.util.CancelledException;
import to.etc.util.StringTool;

import javax.annotation.Nullable;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 27-2-18.
 */
public class DefaultAsyncCompletionListener implements IAsyncCompletionListener {
	private final NodeContainer m_parent;

	@Nullable
	final private IAsyncCompletionListener m_chained;

	public DefaultAsyncCompletionListener(NodeContainer parent) {
		this(parent, null);
	}
	public DefaultAsyncCompletionListener(NodeContainer parent, IAsyncCompletionListener chained) {
		m_parent = parent;
		m_chained = chained;
	}

	@Override public void onCompleted(boolean cancelled, @Nullable Exception errorException) throws Exception {
		//-- If we've got an exception replace the contents with the exception message.
		if(errorException != null && ! (errorException instanceof CancelledException)) {
			errorException.printStackTrace();
			StringBuilder sb = new StringBuilder(8192);
			StringTool.strStacktrace(sb, errorException);
			String s = sb.toString();
			s = s.replace("\n", "<br/>\n");

			MsgBox.error(m_parent, Msgs.BUNDLE.getString(Msgs.ASYNC_ERROR) + "<br/>" + s);
		} else if(cancelled) {
			MsgBox.info(m_parent, Msgs.BUNDLE.getString(Msgs.ASYNC_CONTAINER_CANCELLED));
		}
		IAsyncCompletionListener chained = m_chained;
		if(null != chained)
			chained.onCompleted(cancelled, errorException);
	}
}
