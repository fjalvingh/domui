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

	private final Map<String, CellStyle> m_styles = new HashMap<>();

	public ResultSetExcelExporter(ExcelFormat format) {
		m_format = format;
	}

	private CellStyle errorCs(Workbook workbook) {
		String key = "error";
		CellStyle cs = m_styles.get(key);
		if (null == cs) {
			cs = workbook.createCellStyle();
			cs.setAlignment(HorizontalAlignment.LEFT);
			cs.setIndention((short) 1);
			cs.setFillForegroundColor(IndexedColors.RED.getIndex());
			cs.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			cs.setWrapText(false);
			Font font = cloneFromDefault(workbook);
			font.setItalic(true);
			font.setColor(IndexedColors.DARK_RED.getIndex());
			cs.setFont(font);
			m_styles.put(key, cs);
		}
		return cs;
	}

	@NonNull
	private Font cloneFromDefault(Workbook workbook) {
		Font font = workbook.createFont();
		Font defaultFont = workbook.getFontAt((short) 0);
		font.setFontHeightInPoints(defaultFont.getFontHeightInPoints());
		font.setFontName(defaultFont.getFontName());
		font.setColor(defaultFont.getColor());
		return font;
	}

	public void writeExcel(Workbook workbook, ResultSet rs, String sheetName) {
		try {
			ResultSetMetaData rsmd = rs.getMetaData();
			int numColumns = rsmd.getColumnCount();

			Sheet sheet = workbook.createSheet(sheetName);

			Row headerRow = sheet.createRow(0);
			for (int i = 0; i < numColumns; i++) {
				Cell headerCell = headerRow.createCell(i);
				headerCell.setCellValue(rsmd.getColumnLabel(i + 1));
			}

			int rowNumber = 1;
			while (rs.next()) {
				if (rowNumber >= m_format.getMaxRowsLimit()) {
					Cell cell = sheet.createRow(1).createCell(0);
					cell.setCellValue("too much rows generated, unable to export all, truncated results...!");
					cell.setCellStyle(errorCs(workbook));
					sheet.addMergedRegion(new CellRangeAddress(1,1, 0, 8));
					sheet.createFreezePane(0, 2, 0, 3);
					return;
				}
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
