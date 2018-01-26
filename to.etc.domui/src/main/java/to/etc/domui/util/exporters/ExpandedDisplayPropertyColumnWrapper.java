package to.etc.domui.util.exporters;

import to.etc.domui.component.meta.impl.ExpandedDisplayProperty;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This converts a {@link ExpandedDisplayProperty} to a {@link IExportColumn} for exporting data.
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 26-10-17.
 */
public class ExpandedDisplayPropertyColumnWrapper<T> implements IExportColumn<T> {
	private final ExpandedDisplayProperty<T> m_xp;

	public ExpandedDisplayPropertyColumnWrapper(ExpandedDisplayProperty<T> xp) {
		m_xp = xp;
	}

	@Nullable @Override public String getLabel() {
		return m_xp.getDefaultLabel();
	}

	@Nonnull @Override public Class<?> getActualType() {
		return m_xp.getActualType();
	}

	@Nullable @Override public T getValue(@Nonnull Object in) throws Exception {
		return m_xp.getValue(in);
	}
}
