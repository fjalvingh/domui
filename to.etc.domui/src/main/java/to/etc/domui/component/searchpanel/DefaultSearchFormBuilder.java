package to.etc.domui.component.searchpanel;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.component2.form4.FormBuilder;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.IControl;
import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.dom.html.NodeContainer;

import static java.util.Objects.requireNonNull;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 3-2-18.
 */
public class DefaultSearchFormBuilder implements ISearchFormBuilder {
	@Nullable
	private FormBuilder m_builder;

	private Div m_target;

	@Override public void setTarget(NodeContainer target) throws Exception {
		Div root = m_target = new Div("ui-dfsb-panel");
		target.add(root);
		Div d = new Div("ui-dfsb-part");
		root.add(d);
		m_builder = new FormBuilder(d);
	}

	@Override public void append(SearchControlLine<?> it) throws Exception {
		NodeContainer label = it.getLabel();
		IControl<?> control = it.getControl();
		fb().label(label).item((NodeBase) control);
		//fb().control(control);
	}

	public void addBreak() {
		NodeContainer target = requireNonNull(m_target);
		Div d = new Div("ui-dfsb-part");
		target.add(d);
		m_builder = new FormBuilder(d);
	}

	@Override public void finish() throws Exception {
		m_builder = null;
	}

	@NonNull
	public FormBuilder fb() {
		return requireNonNull(m_builder);
	}
}
