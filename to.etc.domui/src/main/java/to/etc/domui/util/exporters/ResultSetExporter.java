package to.etc.domui.util.exporters;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.eclipse.jdt.annotation.NonNullByDefault;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;

/**
 * Basic exporter that dumps result set into workbook, as new sheet with specified name.
 */
@NonNullByDefault
public class ResultSetExporter {

	private final ExcelFormat m_format;

	public ResultSetExporter(ExcelFormat format) {
		m_format = format;
	}

	public void writeExcel(Workbook workbook, ResultSet rs, int rowCount, String sheetName) {
		try {
			ResultSetMetaData rsmd = rs.getMetaData();
			int numColumns = rsmd.getColumnCount();
			int numRows = rowCount + 1;

			Sheet sheet = workbook.createSheet(sheetName);
			if (numRows > ExcelFormat.getRowsLimit(m_format)) {
				sheet.createRow(0).createCell(0).setCellValue("too much rows generated, unable to export: " + numRows);
				return;
			}

			Row headerRow = sheet.createRow(0);
			for (int i = 0; i < numColumns; i++) {
				headerRow.createCell(i).setCellValue(rsmd.getColumnLabel(i + 1));
			}

			int rowNumber = 1;
			while (rs.next()) {
				Row row = sheet.createRow(rowNumber++);
				for (int colIx = 0; colIx < numColumns; colIx++) {
					Cell cell = row.createCell(colIx);
					setCellValue(cell, rsmd, rs, colIx + 1);
					if (isStringType(rsmd, colIx + 1)) {
						cell.getCellStyle().setAlignment(HorizontalAlignment.LEFT);
					} else {
						cell.getCellStyle().setAlignment(HorizontalAlignment.RIGHT);
					}
				}
			}

			sheet.createFreezePane(0, 1, 0, 2);
		} catch (SQLException e) {
			System.out.println("Error while reading result set and writing to excel file!");
			e.printStackTrace();
		}
	}

	private void setCellValue(Cell cell, ResultSetMetaData rsmd, ResultSet rs, int i) throws SQLException {
		switch(rsmd.getColumnType(i)) {
			default:
				cell.setCellValue(rs.getString(i));
				break;

			case Types.BOOLEAN:
			case Types.BIT:
				cell.setCellValue(rs.getBoolean(i));
				break;

			case Types.DATE:
			case Types.TIME:
			case Types.TIMESTAMP:
				cell.setCellValue(rs.getDate(i));
				break;

			case Types.INTEGER:
			case Types.DECIMAL:
			case Types.NUMERIC:
			case Types.DOUBLE:
			case Types.REAL:
			case Types.FLOAT:
				BigDecimal bigDecimal = rs.getBigDecimal(i);
				if(null == bigDecimal) {
					cell.setCellValue((String) null);
				} else {
					cell.setCellValue(bigDecimal.doubleValue());
				}
				break;
		}
	}

	private boolean isStringType(ResultSetMetaData rsmd, int index) throws SQLException {

		switch (rsmd.getColumnType(index)) {
			default:
				return false;
			case Types.VARCHAR:
			case Types.CHAR:
			case Types.NVARCHAR:
				return true;
		}
	}
}
