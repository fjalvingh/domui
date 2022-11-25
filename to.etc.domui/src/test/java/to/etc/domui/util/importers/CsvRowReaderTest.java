package to.etc.domui.util.importers;

import org.junit.Assert;
import org.junit.Test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 12-01-22.
 */
public class CsvRowReaderTest {
	@Test
	public void testRowReader() throws Exception {
		try(InputStream is = getClass().getResourceAsStream("/importers/1.csv")) {
			try(InputStreamReader r = new InputStreamReader(is, StandardCharsets.UTF_8)) {
				CsvRowReader rr = new CsvRowReader(r);
				int rowCount = 0;
				int colCount = 0;
				for(IImportRow row : rr) {
					rowCount++;
					for(int i = 0; i < row.getColumnCount(); i++) {
						row.get(i).getStringValue();
						colCount++;
					}
				}
				Assert.assertEquals("Row count must match", 88, rowCount);
				Assert.assertEquals("Col count must match", 264, colCount);
			}
		}

	}

	@Test
	public void testRowReaderLastLineWithoutLF() throws Exception {
		try(InputStream is = getClass().getResourceAsStream("/importers/2.csv")) {
			try(InputStreamReader r = new InputStreamReader(is, StandardCharsets.UTF_8)) {
				CsvRowReader rr = new CsvRowReader(r);
				int rowCount = 0;
				int colCount = 0;
				for(IImportRow row : rr) {
					rowCount++;
					for(int i = 0; i < row.getColumnCount(); i++) {
						row.get(i).getStringValue();
						colCount++;
					}
				}
				Assert.assertEquals("Row count must match", 88, rowCount);
				Assert.assertEquals("Col count must match", 264, colCount);

			}
		}

		//-- As an extra check (because IntelliJ will add a lf if the csv is edited) make sure the file did not end in 0xa
		try(InputStream is = getClass().getResourceAsStream("/importers/2.csv")) {
			int c, pc = 0;
			while(-1 != (c = is.read())) {
				pc = c;
			}
			Assert.assertTrue("File does not end in newline", pc != 0x0a && pc != 0x0d);
		}
	}

}
