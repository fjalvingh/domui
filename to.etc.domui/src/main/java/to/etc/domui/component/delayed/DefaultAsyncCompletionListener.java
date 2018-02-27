package to.etc.domui.component.delayed;

import to.etc.domui.component.misc.MsgBox;
import to.etc.domui.dom.html.NodeContainer;
import to.etc.util.StringTool;

import javax.annotation.Nullable;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 27-2-18.
 */
public class DefaultAsyncCompletionListener implements IAsyncCompletionListener {
	private final NodeContainer m_parent;

	public DefaultAsyncCompletionListener(NodeContainer parent) {
		m_parent = parent;
	}

	@Override public void onCompleted(boolean cancelled, @Nullable Exception errorException) throws Exception {
		//-- If we've got an exception replace the contents with the exception message.
		if(errorException != null) {
			errorException.printStackTrace();
			StringBuilder sb = new StringBuilder(8192);
			StringTool.strStacktrace(sb, errorException);
			String s = sb.toString();
			s = s.replace("\n", "<br/>\n");

			MsgBox.error(m_parent, "Exception while creating result for asynchronous task:<br/>" + s);
		}
	}
}
