package to.etc.util;

import org.junit.*;

/**
 * Test StringTool methods
 *
 * @author <a href="mailto:ben.schoen@itris.nl">Ben Schoen</a>
 * @since Dec 5, 2012
 */
public class TestStringTool {

	/**
	 * Test method for {@link to.etc.util.StringTool#isNotBlank(java.lang.String)}.
	 */
	@Test
	public void testIsNotBlank() {
		Assert.assertTrue(StringTool.isNotBlank(null) == false);
		Assert.assertTrue(StringTool.isNotBlank("") == false);
		Assert.assertTrue(StringTool.isNotBlank(" ") == false);
		Assert.assertTrue(StringTool.isNotBlank("bob") == true);
		Assert.assertTrue(StringTool.isNotBlank("  bob  ") == true);
	}

}
