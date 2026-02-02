package to.etc.util;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 02-02-23.
 */
public class FileToolTest {

	@Test
	public void testSansExtension1() throws Exception {
		String val = FileTool.fileNameSansExtension("test.docx");
		Assert.assertEquals("test", val);
	}

	@Test
	public void testSansExtension2() throws Exception {
		String val = FileTool.fileNameSansExtension("test");
		Assert.assertEquals("test", val);
	}

	@Test
	public void testSansExtension3() throws Exception {
		String val = FileTool.fileNameSansExtension("dir/test.docx");
		Assert.assertEquals("dir/test", val);
	}

	@Test
	public void testSansExtension4() throws Exception {
		String val = FileTool.fileNameSansExtension("dir.ext/test");
		Assert.assertEquals("dir.ext/test", val);
	}

	@Test
	public void testSansExtension5() throws Exception {
		String val = FileTool.fileNameSansExtension("dir.ext/test.docx");
		Assert.assertEquals("dir.ext/test", val);
	}


}
