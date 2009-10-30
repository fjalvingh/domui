package to.etc.domui.test.upload;

import java.io.*;
import java.util.*;

import org.junit.*;

import to.etc.domui.util.upload.*;

public class TestUploadParser {
	@Test
	public void testGood() throws Exception {
		InputStream is = getClass().getResourceAsStream("good.bin");
		UploadParser up = new UploadParser();
		List<UploadItem> res = up.parseRequest(is, "utf-8", "multipart/form-data; boundary=---------------------------761455922829130801673802772", 999);
		for(UploadItem it : res) {
			System.out.println("item: " + it.getName());
		}
		Assert.assertEquals(2, res.size());
	}

	@Test
	public void testBad() throws Exception {
		InputStream is = getClass().getResourceAsStream("bad.bin");
		UploadParser up = new UploadParser();
		up.parseRequest(is, "utf-8", "multipart/form-data; boundary=--boun-da-ry-0xababaeaGfHdNarcolethe-mumble-to-content-eNCoDer-gxixmar-rennes-le-chateau124a098aa8eetc", 999);
	}


}
