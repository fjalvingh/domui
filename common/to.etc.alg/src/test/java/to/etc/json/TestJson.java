package to.etc.json;

import java.io.*;
import java.util.*;

import org.junit.*;

import to.etc.util.*;

public class TestJson {
	@Test
	public void testRoundTrip1() throws Exception {
		// 1. Create structure.
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

		//-- 2. Render it initially as a string.
		StringWriter sw = new StringWriter();
		JSON.render(new IndentWriter(sw), d2);
		String io = sw.getBuffer().toString();
		System.out.println(io);

		//-- 3. Parse that string, now.
		StringReader sr = new StringReader(io);
		JsonData1 data = JSON.decode(JsonData1.class, sr);

		//-- 4. Then re-render.
		sw.getBuffer().setLength(0);
		JSON.render(new IndentWriter(sw), d2);
		String io2 = sw.getBuffer().toString();
		Assert.assertEquals(io, io2);
	}

	@Test
	public void testRender2() throws Exception {
		String in = "{list1:[],list2:[],next:{list1:[],list2:[],number1:123,number2:456,onoff:false,string1:'Hello'},number1:666,number2:777,onoff:false,string1:'World'}";
		StringReader sr = new StringReader(in);
		JsonData1 data = JSON.decode(JsonData1.class, sr);

		StringWriter sw = new StringWriter();
		JSON.render(sw, data);
		String out = sw.getBuffer().toString();
		System.out.println("recode: " + out);
		Assert.assertEquals(in, out);


	}
}
