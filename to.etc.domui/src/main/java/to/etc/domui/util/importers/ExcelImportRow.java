package to.etc.domui.util.importers;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import to.etc.webapp.query.QNotFoundException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Date;
import java.util.List;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 31-10-17.
 */
public class ExcelImportRow implements IImportRow {
	final private Row m_row;

	private final List<String> m_headerNames;

	public ExcelImportRow(Row row, List<String> headerNames) {
		m_row = row;
		m_headerNames = headerNames;
	}

	@Nullable
	private String getHeaderName(int index) {
		if(index >= m_headerNames.size())
			return null;
		return m_headerNames.get(index);
	}

	@Override public int getColumnCount() {
		return m_row.getLastCellNum() + 1;
	}

	@Override public IImportColumn get(int index) {
		if(index > m_row.getLastCellNum() || index < 0)
			throw new IllegalStateException("Row index " + index + " is invalid: must be 0 <= index <=" + (m_row.getLastCellNum() + 1));
		if(index < m_row.getFirstCellNum())
			return new EmptyColumn(getHeaderName(index));

		Cell cell = m_row.getCell(index);
		if(null == cell)
			return new EmptyColumn(getHeaderName(index));
		return new ExcelColumn(cell, getHeaderName(index));
	}

	public IImportColumn get(String name) {
		int index = -1;
		for(int i = m_headerNames.size(); --i >= 0;) {
			if(m_headerNames.get(i).equals(name)) {
				if(index == -1)
					index = i;
				else
					throw new IllegalStateException("The field name '" + name + "' occurs more than once in the header");
			}
		}
		if(index == -1)
			throw new QNotFoundException("Column", "The column with header name '" + name + "' could not be found");
		return get(index);
	}

	static private class ExcelColumn extends AbstractImportColumn implements IImportColumn {
		private final Cell m_cell;

		private final String m_name;

		public ExcelColumn(@Nonnull Cell cell, String name) {
			if(null == cell)
				throw new IllegalStateException("Cell cannot be null");
			m_cell = cell;
			m_name = name;
		}

		@Nullable
		public String getName() {
			return m_name;
		}

		@Nullable @Override public String getStringValue() {
			return m_cell.getStringCellValue();
		}

		@Override public Date asDate() {
			return m_cell.getDateCellValue();
		}
	}

	static private class EmptyColumn extends AbstractImportColumn implements IImportColumn {
		private final String m_name;

		public EmptyColumn(String name) {
			m_name = name;
		}

		@Nullable @Override public String getStringValue() {
			return null;
		}

		@Override public Date asDate() {
			return null;
		}
	}
}
