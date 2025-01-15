package to.etc.domui.util.exporters;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.function.FunctionEx;

public class ExportColumn<T> implements IExportColumn<T> {
	private final String m_label;

	private final Class<T> m_type;

	private final FunctionEx<Object, T> m_getter;

	private IExportCellRenderer<?, ?, ?> m_renderer;

	public ExportColumn(String label, Class<T> type, FunctionEx<Object, T> getter) {
		m_label = label;
		m_type = type;
		m_getter = getter;
	}

	@Nullable
	@Override
	public String getLabel() {
		return m_label;
	}

	@NonNull
	@Override
	public Class<?> getActualType() {
		return m_type;
	}

	@Nullable
	@Override
	public IExportCellRenderer<?, ?, ?> getRenderer() {
		return null;
	}

	public ExportColumn<T> renderer(IExportCellRenderer<?, ?, ?> renderer) {
		m_renderer = renderer;
		return this;
	}

	@Nullable
	@Override
	public Object convertValue(@Nullable Object value) throws Exception {
		return value;
	}

	@Nullable
	@Override
	public T getValue(@NonNull Object in) throws Exception {
		return m_getter.apply(in);
	}
}
