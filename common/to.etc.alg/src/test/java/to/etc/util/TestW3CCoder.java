package to.etc.util;

import java.io.*;
import java.text.*;
import java.util.*;

import org.junit.*;

import to.etc.xml.*;

public class TestW3CCoder {
	@Test
	public void testDateEncoding() throws Exception {
		DateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		sf.setLenient(true);

		String s = FileTool.readResourceAsString(getClass(), "codertest1.txt", "utf-8");
		LineNumberReader lnr = new LineNumberReader(new StringReader(s));
		String line;
		while(null != (line = lnr.readLine())) {
			String[] pair = line.split(";");
			if(pair.length != 2)
				throw new IllegalStateException("Bad pair");
			Date dt = sf.parse(pair[0]);

			Date dt2 = new Date(dt.getTime());


			String od = W3CSchemaCoder.encodeDate(dt, null);
			Assert.assertEquals(pair[1], od);
		}


	}

	@Test
	public void testDateEncoding2() throws Exception {
		W3CSchemaCoder.decodeDateTime("2015-09-28T00:00:00");
	}

}
