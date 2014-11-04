package to.etc.domui.component.combobox;

import javax.annotation.*;

import to.etc.domui.dom.html.*;

/**
 * This replaces the select 'option' field for the ComboBox. Unlike the select this
 * can contain images and such.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 18, 2014
 */
final class ComboOption<V> extends Div {
	@Nullable
	private final V m_value;

	private boolean m_selected;

	public ComboOption(@Nullable V value) {
		setCssClass("ui-cbb-op");
		m_value = value;
	}

	@Nullable
	public V getValue() {
		return m_value;
	}

	public boolean isSelected() {
		return m_selected;
	}

	public void setSelected(boolean selected) {
		m_selected = selected;
	}
}
