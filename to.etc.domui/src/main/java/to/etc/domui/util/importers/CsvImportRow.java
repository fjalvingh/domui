package to.etc.domui.util.importers;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 12-12-17.
 */
@NonNullByDefault
public class CsvImportRow implements IImportRow {
	private final List<String> m_columns;

	private final List<IImportColumn> m_colWrappers = new ArrayList<>();

	private final CsvRowReader m_reader;

	public CsvImportRow(CsvRowReader reader, List<String> columns) {
		m_columns = new ArrayList<>(columns);
		m_reader = reader;
	}

	@Override
	public int getColumnCount() {
		return m_columns.size();
	}

	@Override
	public IImportColumn get(int index) throws IOException {
		//if(index < 0 || index >= m_columns.size())
		//	throw new IllegalStateException("Column index invalid: must be between 0 and " + m_columns.size());
		while(index >= m_colWrappers.size()) {
			m_colWrappers.add(new Col(m_colWrappers.size(), m_reader.getColumnName(index)));
		}
		return m_colWrappers.get(index);
	}

	@NonNull
	@Override
	public IImportColumn get(String name) throws IOException {
		int index = m_reader.getColumnIndex(name);
		if(index == -1)
			throw new IOException("The column with the name '" + name + "' does not exist");
		return get(index);
	}

	@Override
	public boolean isEmpty() throws IOException {
		for(int i = 0; i < getColumnCount(); i++) {
			if(get(i).getStringValue() != null)
				return false;
		}
		return true;
	}

	private class Col extends AbstractImportColumn implements IImportColumn {
		private final int m_index;

		@Nullable
		private final String m_name;

		public Col(int index, @Nullable String name) {
			m_index = index;
			m_name = name;
		}

		@Nullable
		@Override
		public String getStringValue() {
			if(m_columns.size() <= m_index)
				return null;
			String txt = m_columns.get(m_index);
			if(m_reader.isDontSkipWs())
				return txt;
			return trimAllWS(txt);
		}

		@Nullable
		@Override
		public Date asDate() {
			DateFormat df = m_reader.getDateFormat();
			if(null == df)
				throw new IllegalStateException("Date format for CSV file is not set. Either set it on the reader, or use asDate(String) with a format.");
			String stringValue = getStringValue();
			if(null == stringValue)
				return null;
			stringValue = stringValue.trim();
			if(stringValue.isEmpty())
				return null;
			try {
				return df.parse(stringValue);
			} catch(Exception x) {
				throw new ImportValueException("Invalid date: " + stringValue);
			}
		}

		@Nullable
		@Override
		public Date asDate(String dateFormat) {
			String stringValue = getStringValue();
			if(null == stringValue)
				return null;
			stringValue = stringValue.trim();
			if(stringValue.isEmpty())
				return null;

			DateFormat sdf = m_reader.getDateFormat(dateFormat);
			try {
				return sdf.parse(stringValue);
			} catch(Exception x) {
				throw new ImportValueException("Invalid date: " + stringValue);
			}
		}

		@Nullable
		@Override
		public String getName() {
			return m_name;
		}
	}
}
