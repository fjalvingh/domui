package to.etc.domui.component.tbl;

import org.eclipse.jdt.annotation.NonNull;
import to.etc.domui.component.binding.StyleBinder;
import to.etc.domui.component.meta.MetaManager;
import to.etc.domui.component.meta.PropertyMetaModel;
import to.etc.domui.util.IValueAccessor;
import to.etc.webapp.query.QField;

/**
 * Binding builder for binding styles to a table column.
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 08-11-21.
 */
final public class ColumnStyleBindingBuilder<I, T> {
	private final ColumnDef<I, T> m_column;

	private final StyleBinder m_binder;

	private IValueAccessor<?> m_property;

	public ColumnStyleBindingBuilder(ColumnDef<I, T> column, StyleBinder binder) {
		m_column = column;
		m_binder = binder;
	}

	@NonNull
	public <S> ColumnDef<I, T> to(@NonNull IValueAccessor<S> property) throws Exception {
		m_property = property;
		m_column.styleBindingComplete(this);
		return m_column;
	}

	@NonNull
	public ColumnDef<I, T> to(@NonNull String property) throws Exception {
		PropertyMetaModel<?> pmm = MetaManager.getPropertyMeta(m_column.getColumnList().getActualClass(), property);
		return to(pmm);
	}
	@NonNull
	public ColumnDef<I, T> to(@NonNull QField<I, ?> property) throws Exception {
		return to(property.getName());
	}

	public StyleBinder getBinder() {
		return m_binder;
	}

	public IValueAccessor<?> getProperty() {
		return m_property;
	}
}
