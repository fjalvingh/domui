package to.etc.domui.util.exporters;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.component.meta.ClassMetaModel;
import to.etc.domui.component.meta.MetaManager;
import to.etc.domui.component.meta.PropertyMetaModel;
import to.etc.webapp.nls.NlsContext;
import to.etc.webapp.query.QField;

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

	public <R> PropertyExportColumn(QField<R, T> qField, String label) {
		this(MetaManager.findClassMeta(qField.getRootClass()).getProperty(qField), label);
	}

	public <R> PropertyExportColumn(QField<R, T> qField) {
		this(MetaManager.findClassMeta(qField.getRootClass()).getProperty(qField));
	}

	public PropertyExportColumn(PropertyMetaModel<T> pmm) {
		m_pmm = pmm;
		m_label = null;
	}

	@NonNull
	@Override
	public Class<?> getActualType() {
		return m_pmm.getActualType();
	}

	@Nullable
	@Override
	public String getLabel() {
		return m_label == null ? m_pmm.getDefaultLabel() : m_label;
	}

	@Nullable
	@Override
	public T getValue(@NonNull Object in) throws Exception {
		return m_pmm.getValue(in);
	}

	@Nullable
	@Override
	public Object convertValue(@Nullable Object value) throws Exception {
		if(value instanceof Enum) {
			ClassMetaModel ecmm = MetaManager.findClassMeta(value.getClass());
			String v = ecmm.getDomainLabel(NlsContext.getLocale(), value);
			return v == null ? ((T) value).toString() : v;
		}
		return value;
	}
}
