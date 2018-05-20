package to.etc.domui.util.importers;

import org.eclipse.jdt.annotation.Nullable;

import java.util.Date;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 25-4-18.
 */
class EmptyColumn extends AbstractImportColumn implements IImportColumn {
	private final String m_name;

	public EmptyColumn(String name) {
		m_name = name;
	}

	@Nullable @Override public String getStringValue() {
		return null;
	}

	@Nullable
	@Override public Date asDate() {
		return null;
	}

	@Override public String getName() {
		return m_name;
	}
}
