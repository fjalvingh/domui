package to.etc.domui.util.importers;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.webapp.query.QNotFoundException;

import java.util.Date;
import java.util.List;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 31-10-17.
 */
@NonNullByDefault
public class ExcelImportRow implements IImportRow {
	final private Row m_row;

	private final List<String> m_headerNames;

	public ExcelImportRow(Row row, List<String> headerNames) {
		if(null == row)
			throw new IllegalStateException();
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
		String headerName = getHeaderName(index);
		if(null == headerName)
			headerName = "COL" + index;
		if(index > m_row.getLastCellNum() || index < 0)
			return new EmptyColumn(headerName);
		if(index < m_row.getFirstCellNum())
			return new EmptyColumn(headerName);

		Cell cell = m_row.getCell(index);
		if(null == cell)
			return new EmptyColumn(headerName);
		return new ExcelColumn(cell, headerName);
	}

	@Override
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

		public ExcelColumn(@NonNull Cell cell, String name) {
			if(null == cell)
				throw new IllegalStateException("Cell cannot be null");
			m_cell = cell;
			m_name = name;
		}

		@Override
		@Nullable
		public String getName() {
			return m_name;
		}

		@Nullable @Override public String getStringValue() {
			try {
				switch(m_cell.getCellTypeEnum()) {
					default:
						return m_cell.toString();

					case BLANK:
						return "";

					case BOOLEAN:
						return String.valueOf(m_cell.getBooleanCellValue());

					case NUMERIC:
						return Double.toString(m_cell.getNumericCellValue());
					case STRING:
						return m_cell.getStringCellValue();

					case _NONE:
						return null;
				}

			} catch(Exception x) {
				throw new ImportValueException(x, "@[" + m_cell.getSheet().getSheetName() + ":" + m_cell.getAddress()+ "] " + x.toString());
				//throw new ImportValueException(x, "@[" + m_cell.getSheet().getSheetName() + ":" + m_cell.getAddress().getRow()+ ", " + m_cell.getAddress().getColumn() + "] " + x.toString());
			}
		}

		@Nullable
		@Override public Date asDate() {
			return m_cell.getDateCellValue();
		}
	}

}


