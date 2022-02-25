package to.etc.domui.component.delayed;

import to.etc.domui.component.layout.ExpandCollapsePanel;
import to.etc.domui.component.layout.MessageLine;
import to.etc.domui.component.misc.MsgBox;
import to.etc.domui.dom.errors.MsgType;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.Pre;
import to.etc.domui.util.Msgs;
import to.etc.parallelrunner.IAsyncRunnable;
import to.etc.util.CancelledException;
import to.etc.util.MessageException;
import to.etc.util.StringTool;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 14-12-21.
 */
abstract public class AsyncDiv<T extends IAsyncRunnable> extends Div {
	final private T m_runnable;

	private final String m_what;

	public AsyncDiv(T runnable, String what) {
		m_runnable = runnable;
		m_what = what;
	}

	public AsyncDiv(T runnable) {
		m_runnable = runnable;
		m_what = null;
	}

	@Override
	final public void createContent() throws Exception {
		String what = m_what;
		if(null != what) {
			add(new HTag(1, what));
		}
		add(new AsyncContainer(m_runnable, (cancelled, errorException) -> {
			//-- If we've got an exception replace the contents with the exception message.
			if(errorException != null && !(errorException instanceof CancelledException)) {
				createError(m_runnable, errorException, false);
			} else if(cancelled) {
				MsgBox.info(this, Msgs.BUNDLE.getString(Msgs.ASYNC_CONTAINER_CANCELLED));
			} else {
				removeAllChildren();
				createContent(m_runnable);
			}
		}));
	}

	protected void createError(T task, Exception errorException, boolean cancelled) throws Exception {
		if(cancelled) {
			add(new MessageLine(MsgType.INFO, Msgs.BUNDLE.getString(Msgs.ASYNC_CONTAINER_CANCELLED)));
		} else if(errorException instanceof MessageException) {
			add(new MessageLine(MsgType.ERROR, errorException.getMessage()));
		} else {
			add(new MessageLine(MsgType.ERROR, Msgs.BUNDLE.getString(Msgs.ASYNC_ERROR)));

			StringBuilder sb = new StringBuilder(8192);
			StringTool.strStacktrace(sb, errorException);
			String s = sb.toString();
			s = s.replace("\n", "<br/>\n");

			ExpandCollapsePanel ecp = new ExpandCollapsePanel("Details");
			add(ecp);

			Pre p = new Pre();
			ecp.setContent(p);
			p.setText(s);
		}
	}

	abstract public void createContent(T task) throws Exception;
}
