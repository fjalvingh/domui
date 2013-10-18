package to.etc.webapp.json;

import java.io.*;
import java.util.*;

import org.junit.*;

import to.etc.util.*;

public class TestJson {
	@Test
	public void testRoundTrip1() throws Exception {
		JsonData1 d1 = new JsonData1(123, 456, "Hello", null, null);
		JsonData1 d2 = new JsonData1(666, 777, "World", null, d1);
		List<Long> nl = d2.getList2();
		nl.add(Long.valueOf(9871));
		nl.add(Long.valueOf(9872));
		nl.add(Long.valueOf(9873));
		nl.add(Long.valueOf(9875));

		List<JsonData1> cl = d2.getList1();
		cl.add(new JsonData1(12, 81, "Bleh1", null, null));
		cl.add(new JsonData1(13, 82, "Bleh2", null, null));
		cl.add(new JsonData1(14, 83, "Bleh3", null, null));
		cl.add(new JsonData1(15, 84, "Bleh4", null, null));

		JSON json = new JSON();
		StringWriter sw = new StringWriter();
		json.render(new IndentWriter(sw), d2);

		String io = sw.getBuffer().toString();
		System.out.println(io);

		//-- Reverse
		StringReader sr = new StringReader(io);

		JsonData1 data = json.decode(JsonData1.class, sr);

	}

	@Test
	public void testRender2() throws Exception {
		JSON json = new JSON();

		StringReader sr = new StringReader("{number2:777,number1:666,string1:\"World\",next:{number2:456,number1:123,string1:\"Hello\"}}");

		JsonData1 data = json.decode(JsonData1.class, sr);

		StringWriter sw = new StringWriter();
		json.render(new IndentWriter(sw), data);

		System.out.println("recode: " + sw.getBuffer());

	}
}
