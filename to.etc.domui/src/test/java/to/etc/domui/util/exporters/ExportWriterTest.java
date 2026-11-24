package to.etc.domui.util.exporters;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

public class ExportWriterTest {
	private static final Timestamp TS = Timestamp.valueOf(LocalDateTime.of(2026, 11, 24, 10, 15, 30));

	private static final List<IExportColumn<?>> COLUMNS = List.of(
		new ExportColumn<>("name", String.class, row -> (String) ((Object[]) row)[0]),
		new ExportColumn<>("when", Timestamp.class, row -> (Timestamp) ((Object[]) row)[1])
	);

	@Test
	public void excelMimeTypesAreTheFormatOnes() {
		File f = new File("unused");
		Assert.assertEquals(ExcelFormat.XLSX.getMimeType(), new ExcelExportWriter<>(ExcelFormat.XLSX, f).getMimeType());
		Assert.assertEquals(ExcelFormat.XLS.getMimeType(), new ExcelExportWriter<>(ExcelFormat.XLS, f).getMimeType());
	}

	@Test
	public void csvKeepsTheTimeOfATimestampAndIsUtf8() throws Exception {
		File file = File.createTempFile("export-test-", ".csv");
		try {
			try(CsvExportWriter<Object[]> w = new CsvExportWriter<>(file)) {
				w.startExport(COLUMNS);
				w.exportRow(new Object[] {"Één", TS});
			}
			String csv = Files.readString(file.toPath(), StandardCharsets.UTF_8);
			Assert.assertTrue(csv, csv.contains("\"Één\",\"2026-11-24 10:15:30\""));
		} finally {
			Files.deleteIfExists(file.toPath());
		}
	}

	@Test
	public void excelKeepsTheTimeOfATimestamp() throws Exception {
		File file = File.createTempFile("export-test-", ".xlsx");
		try {
			try(ExcelExportWriter<Object[]> w = new ExcelExportWriter<>(ExcelFormat.XLSX, file)) {
				w.startExport(COLUMNS);
				w.exportRow(new Object[] {"x", TS});
			}
			try(InputStream is = Files.newInputStream(file.toPath()); Workbook wb = WorkbookFactory.create(is)) {
				Cell cell = wb.getSheetAt(0).getRow(1).getCell(1);
				Assert.assertEquals(CellType.NUMERIC, cell.getCellType());
				Assert.assertEquals(TS.toLocalDateTime(), cell.getLocalDateTimeCellValue());
				Assert.assertTrue(cell.getCellStyle().getDataFormatString(), cell.getCellStyle().getDataFormatString().contains("hh:mm"));
			}
		} finally {
			Files.deleteIfExists(file.toPath());
		}
	}
}
