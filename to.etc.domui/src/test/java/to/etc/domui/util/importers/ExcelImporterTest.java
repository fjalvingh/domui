package to.etc.domui.util.importers;

import org.junit.Assert;
import org.junit.Test;
import to.etc.domui.util.exporters.ExcelFormat;

import java.io.InputStream;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 10-01-22.
 */
public class ExcelImporterTest {

	/**
	 * Test the normal non-streaming XLSX importer.
	 */
	@Test
	public void testExcelXslxImport1() throws Exception {
		try(InputStream is = getClass().getResourceAsStream("/importers/1.xlsx")) {
			if(null == is)
				throw new IllegalStateException("Resource no found");
			try(ExcelRowReader rr = new ExcelRowReader(is, ExcelFormat.XLSX)) {
				int rowCount = 0;
				int columnCount = 0;
				for(IImportRow row : rr) {
					rowCount++;
					for(int i = 0; i < row.getColumnCount(); i++) {
						row.get(i).getStringValue();
						columnCount++;
					}
				}
				Assert.assertEquals("Row count must be correct", 275, rowCount);
				Assert.assertEquals("Column count must be correct", 550, columnCount);
			}
		}
	}

	/**
	 * Test the streaming XLSX importer.
	 */
	@Test
	public void testStreamingXslxImport1() throws Exception {
		try(InputStream is = getClass().getResourceAsStream("/importers/1.xlsx")) {
			if(null == is)
				throw new IllegalStateException("Resource no found");
			try(ExcelStreamingRowReader rr = new ExcelStreamingRowReader(is, ExcelFormat.XLSX)) {
				int rowCount = 0;
				int columnCount = 0;
				for(IImportRow row : rr) {
					rowCount++;
					for(int i = 0; i < row.getColumnCount(); i++) {
						row.get(i).getStringValue();
						columnCount++;
					}
				}
				Assert.assertEquals("Row count must be correct", 275, rowCount);
				Assert.assertEquals("Column count must be correct", 550, columnCount);
			}
		}
	}


}
