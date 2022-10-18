package to.etc.domui.component.buttons;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.dom.css.DisplayType;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.dom.html.NodeContainer;
import to.etc.domui.dom.html.TextNode;

import java.util.ArrayList;
import java.util.List;

/**
 * This component can be used to make any content visible/invisible
 * depending on some bound parameter. You can for instance add a button
 * to this that will then be either rendered or not depending on whether
 * this component's "displayed" value is true or false (or its inverse,
 * the hidden parameter being false or true).
 * As we can bind to those parameters updating the visibility of the contained
 * is automatically managed.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 15-11-21.
 */
final public class ActionContainer extends Div {
	private boolean m_displayed;

	private List<NodeBase> m_childList = new ArrayList<>();

	@Override
	public void createContent() throws Exception {
		setDisplay(DisplayType.INLINE_BLOCK);
		if(m_displayed) {
			for(int i = 0; i < m_childList.size(); i++) {
				NodeBase nb = m_childList.get(i);
				super.add(i, nb);
			}
		}
	}

	@NonNull
	@Override
	public <T extends NodeBase> T add(int index, @NonNull T nd) {
		if(index >= m_childList.size())
			m_childList.add(nd);
		else
			m_childList.set(index, nd);
		if(m_displayed) {
			super.add(index, nd);
		}
		return nd;
	}

	@NonNull
	@Override
	public NodeContainer add(@Nullable String txt) {
		if(null == txt || txt.isEmpty())
			return this;
		TextNode tn = new TextNode(txt);
		add(tn);
		return this;
	}

	public boolean isDisplayed() {
		return m_displayed;
	}

	public void setDisplayed(boolean displayed) {
		if(m_displayed == displayed)
			return;
		m_displayed = displayed;
		forceRebuild();
	}

	public boolean isHidden() {
		return ! m_displayed;
	}

	public void setHidden(boolean hidden) {
		setDisplayed(! hidden);
	}
}
