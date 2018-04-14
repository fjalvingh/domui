package to.etc.domui.util.exporters;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.component.meta.PropertyMetaModel;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 31-10-17.
 */
@NonNullByDefault
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

	@NonNull @Override public Class<?> getActualType() {
		return m_pmm.getActualType();
	}

	@Nullable @Override public String getLabel() {
		return m_label == null ? m_pmm.getDefaultLabel() : m_label;
	}

	@Nullable @Override public T getValue(@NonNull Object in) throws Exception {
		return m_pmm.getValue(in);
	}
}
