package to.etc.domui.util.importers;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.webapp.query.QNotFoundException;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 31-10-17.
 */
@NonNullByDefault
public class ExcelImportRow implements IImportRow {
	final private ExcelRowReader m_rr;

	final private Row m_row;

	private final List<String> m_headerNames;

	public ExcelImportRow(ExcelRowReader rr, Row row, List<String> headerNames) {
		m_rr = rr;
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
		short cell = m_row.getLastCellNum();
		return cell < 0 ? 0 : cell;
		//return cell /* + 1 jal 20190204 removed, last cell number seems 1 based */ ;
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
		return new ExcelColumn(this, cell, headerName);
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

		private final ExcelImportRow m_row;

		public ExcelColumn(ExcelImportRow row, @NonNull Cell cell, String name) {
			m_row = row;
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
						return m_row.m_rr.convertDouble(m_cell.getNumericCellValue());
						//return Double.toString(m_cell.getNumericCellValue());
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

		@Nullable @Override public BigDecimal getDecimal() {
			try {
				switch(m_cell.getCellTypeEnum()) {
					default:
						throw new IllegalStateException("Unknown cell type: " + m_cell.getCellTypeEnum());

					case _NONE:
					case BLANK:
						return null;

					case BOOLEAN:
						return BigDecimal.ONE;

					case NUMERIC:
						return new BigDecimal(m_cell.getNumericCellValue());
					case STRING:
						String value = m_cell.getStringCellValue();
						if(value == null)
							return null;
						value = value.trim().replace(',', '.');
						if(value.length() == 0)
							return null;
						return new BigDecimal(value);
				}
			} catch(Exception x) {
				x.printStackTrace();
				String base;
				try {
					base = m_cell.getStringCellValue();
				} catch(Exception xx) {
					base = null;
				}

				throw new ImportValueException(x, "@[" + m_cell.getSheet().getSheetName() + ":" + m_cell.getAddress()+ "], value '" + base + "': " + x.toString());
				//throw new ImportValueException(x, "@[" + m_cell.getSheet().getSheetName() + ":" + m_cell.getAddress().getRow()+ ", " + m_cell.getAddress().getColumn() + "] " + x.toString());
			}
		}
	}

}


