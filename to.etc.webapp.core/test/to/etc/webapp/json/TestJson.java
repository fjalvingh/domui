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


}
