package to.etc.domui.component.searchpanel;

import to.etc.domui.component2.form4.FormBuilder;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.IControl;
import to.etc.domui.dom.html.NodeContainer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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
		if(null != label)
			fb().label(label);
		IControl<?> control = it.getControl();
		fb().control(control);
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

	@Nonnull
	public FormBuilder fb() {
		return requireNonNull(m_builder);
	}
}
