package to.etc.domui.component.lookupform2;

import to.etc.domui.component2.form4.FormBuilder;
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

	@Override public void setTarget(NodeContainer target) throws Exception {
		m_builder = new FormBuilder(target);
	}

	@Override public void append(LookupLine<?> it) throws Exception {
		NodeContainer label = it.getLabel();
		if(null != label)
			fb().label(label);
		IControl<?> control = it.getControl();
		fb().control(control);
	}

	@Override public void finish() throws Exception {
		m_builder = null;
	}

	@Nonnull
	public FormBuilder fb() {
		return requireNonNull(m_builder);
	}
}
