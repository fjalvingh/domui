package to.etc.domui.component.delayed;

import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.dom.html.NodeContainer;
import to.etc.domui.dom.html.Pre;
import to.etc.domui.util.Msgs;
import to.etc.util.StringTool;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

abstract public class AbstractAsyncRunnable implements IAsyncRunnable {
	@Nonnull
	final private NodeContainer m_replacedNode;

	abstract public void onCompleted() throws Exception;

	public AbstractAsyncRunnable(@Nonnull NodeContainer replacedNode) {
		m_replacedNode = replacedNode;
	}

	@Override
	public void onCompleted(boolean cancelled, @Nullable Exception errorException) throws Exception {
		//-- If we've got an exception replace the contents with the exception message.
		if(errorException != null) {
			StringBuilder sb = new StringBuilder(8192);
			StringTool.strStacktrace(sb, errorException);
			addNode(new Pre().add(sb.toString()));
			return;
		}

		//-- If there is no result- either we were cancelled OR there are no results..
		if(cancelled) {
			addNode(new Pre().add(Msgs.BUNDLE.getString(Msgs.ASYNC_CONTAINER_CANCELLED_MSG)));
			return;
		}
		onCompleted();
	}

	protected void addNode(@Nonnull NodeBase b) {
		if(m_replacedNode != m_replacedNode.getPage().getBody())
			m_replacedNode.appendAfterMe(b);
		else
			m_replacedNode.add(new Div(b));
	}
}
