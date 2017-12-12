package to.etc.domui.util.importers;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 12-12-17.
 */
public class CsvImportRow implements IImportRow {
	private final List<String> m_columns;

	private final List<IImportColumn> m_colWrappers = new ArrayList<>();

	public CsvImportRow(List<String> columns) {
		m_columns = new ArrayList<>(columns);
	}

	@Override public int getColumnCount() {
		return m_columns.size();
	}

	@Override public IImportColumn get(int index) {
		if(index < 0 || index >= m_columns.size())
			throw new IllegalStateException("Column index invalid: must be between 0 and " + m_columns.size());
		while(index >= m_colWrappers.size()) {
			m_colWrappers.add(new Col(m_colWrappers.size()));
		}
		return m_colWrappers.get(index);
	}

	private class Col extends AbstractImportColumn implements IImportColumn {
		private final int m_index;

		public Col(int index) {
			m_index = index;
		}

		@Nullable @Override public String getStringValue() {
			return m_columns.get(m_index);
		}

		@Nullable @Override public Date asDate() {
			throw new IllegalStateException("Not implemented yet");
		}
	}
}
