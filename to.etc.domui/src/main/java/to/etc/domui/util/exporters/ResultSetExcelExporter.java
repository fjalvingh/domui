package to.etc.domui.util.exporters;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

/**
 * Basic exporter that dumps result set into workbook, as new sheet with specified name.
 */
@NonNullByDefault
public class ResultSetExcelExporter {

	private final ExcelFormat m_format;

	private final ExcelWriterUtil m_excelWriterUtil;

	public ResultSetExcelExporter(ExcelFormat format, Workbook workbook) {
		m_format = format;
		m_excelWriterUtil = new ExcelWriterUtil(format, workbook);
	}

	public void writeExcel(Workbook workbook, ResultSet rs, String sheetName) {
		try {
			ResultSetMetaData rsmd = rs.getMetaData();
			int numColumns = rsmd.getColumnCount();

			Sheet sheet = workbook.createSheet(sheetName);

			Row headerRow = sheet.createRow(1);
			for (int i = 0; i < numColumns; i++) {
				Cell headerCell = headerRow.createCell(i);
				headerCell.setCellValue(rsmd.getColumnLabel(i + 1));
			}

			int rowNumber = 1;
			while (rs.next()) {
				if (rowNumber >= m_format.getMaxRowsLimit() - 1) {
					Cell cell = sheet.createRow(0).createCell(0);
					cell.setCellValue("too much rows generated, unable to export all, truncated results...!");
					cell.setCellStyle(m_excelWriterUtil.errorCs());
					sheet.addMergedRegion(new CellRangeAddress(0,0, 0, numColumns -1));
					sheet.createFreezePane(0, 2, 0, 2);
					return;
				}
				Row row = sheet.createRow(++rowNumber);
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
			Cell cell = sheet.createRow(0).createCell(0);
			cell.setCellValue("Total exported rows: " + (rowNumber - 1));
			sheet.addMergedRegion(new CellRangeAddress(0,0, 0, numColumns -1));
			sheet.createFreezePane(0, 2, 0, 2);

			if (m_excelWriterUtil.isAutoSizeCols()) {
				for (int i = 0; i < numColumns; i++) {
					sheet.autoSizeColumn(i);
				}
			}
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
