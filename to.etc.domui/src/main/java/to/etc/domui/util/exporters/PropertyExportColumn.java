package to.etc.domui.util.exporters;

import to.etc.domui.component.meta.PropertyMetaModel;

import javax.annotation.DefaultNonNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 31-10-17.
 */
@DefaultNonNull
public class PropertyExportColumn<T> implements IExportColumn<T> {
	final private PropertyMetaModel<T> m_pmm;

	@Nullable
	final private String m_label;

	public PropertyExportColumn(PropertyMetaModel<T> pmm, String label) {
		m_pmm = pmm;
		m_label = label;
	}

	public PropertyExportColumn(PropertyMetaModel<T> pmm) {
		m_pmm = pmm;
		m_label = null;
	}

	@Nonnull @Override public Class<?> getActualType() {
		return m_pmm.getActualType();
	}

	@Nullable @Override public String getLabel() {
		return m_label == null ? m_pmm.getDefaultLabel() : m_label;
	}

	@Nullable @Override public T getValue(@Nonnull Object in) throws Exception {
		return m_pmm.getValue(in);
	}
}
