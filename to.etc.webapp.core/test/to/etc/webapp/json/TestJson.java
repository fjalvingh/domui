package to.etc.webapp.json;

import java.io.*;

import org.junit.*;

public class TestJson {
	@Test
	public void testRender1() throws Exception {
		JsonData1 d1 = new JsonData1(123, 456, "Hello", null, null);
		JsonData1 d2 = new JsonData1(666, 777, "World", null, d1);

		JSON json = new JSON();
		StringWriter sw = new StringWriter();
		json.render(sw, d2);

		System.out.println(sw.getBuffer());
	}

	@Test
	public void testRender2() throws Exception {
		JSON json = new JSON();

		StringReader sr = new StringReader("{number2:777,number1:666,string1:\"World\",next:{number2:456,number1:123,string1:\"Hello\"}}");

		JsonData1 data = json.decode(JsonData1.class, sr);

		StringWriter sw = new StringWriter();
		json.render(sw, data);

		System.out.println("recode: " + sw.getBuffer());

	}
}
