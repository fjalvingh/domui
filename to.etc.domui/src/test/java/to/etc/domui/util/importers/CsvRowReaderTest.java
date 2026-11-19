package to.etc.domui.util.importers;

import org.junit.Assert;
import org.junit.Test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
					//String sv = row.get(0).getStringValue();
					//System.out.println(sv);
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


	/**
	 * Every field of every record, as the reader returns them.
	 */
	static private List<List<String>> read(String csv, boolean treatEmptyStringAsNull) throws Exception {
		CsvRowReader reader = new CsvRowReader(new StringReader(csv)).doubleQuotes().escapeChar('\\').multiLine().skipCr(false).treatEmptyStringAsNull(treatEmptyStringAsNull);
		List<List<String>> rows = new ArrayList<>();
		while(reader.readRecord()) {
			List<String> row = new ArrayList<>();
			for(int i = 0; i < reader.getColumnCount(); i++) {
				row.add(reader.getColumnValue(i));
			}
			rows.add(row);
		}
		return rows;
	}

	static private final String NULLS_AND_BLANKS = "1,,\"a\"\r\n2,\"\",\"  \"\r\n3,\"x\",\r\n4,  ,\"x\"";

	@Test
	public void unquotedEmptyIsNullAndQuotedValuesAreKept() throws Exception {
		Assert.assertEquals(List.of(
			Arrays.asList("1", null, "a"),
			Arrays.asList("2", "", "  "),
			Arrays.asList("3", "x", null),
			Arrays.asList("4", "  ", "x")
		), read(NULLS_AND_BLANKS, false));
	}

	@Test
	public void treatEmptyStringAsNullAlsoMakesEmptyAndBlankValuesNull() throws Exception {
		Assert.assertEquals(List.of(
			Arrays.asList("1", null, "a"),
			Arrays.asList("2", null, null),
			Arrays.asList("3", "x", null),
			Arrays.asList("4", null, "x")
		), read(NULLS_AND_BLANKS, true));
	}

	@Test
	public void quotesEscapesAndNewlinesInsideQuotesAreKept() throws Exception {
		Assert.assertEquals(List.of(
			Arrays.asList("q\"q", "b\\s", "two\r\nlines")
		), read("\"q\"\"q\",\"b\\\\s\",\"two\r\nlines\"\r\n", false));
	}

	@Test
	public void theLineNumberCountsLinesIncludingThoseInsideQuotes() throws Exception {
		CsvRowReader reader = new CsvRowReader(new StringReader("1,\"a\"\r\n2,\"two\r\nlines\"\r\n3,\"c\"\r\n")).doubleQuotes().multiLine().skipCr(false);
		Assert.assertEquals(1, reader.getLineNumber());
		Assert.assertTrue(reader.readRecord());
		Assert.assertEquals(2, reader.getLineNumber());
		Assert.assertTrue(reader.readRecord());
		Assert.assertEquals(4, reader.getLineNumber());
		Assert.assertTrue(reader.readRecord());
		Assert.assertEquals(5, reader.getLineNumber());
	}
}
