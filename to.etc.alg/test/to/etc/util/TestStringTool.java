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
	 * Test method for {@link to.etc.util.StringTool#isBlank(java.lang.String)}.
	 */
	@Test
	public void testIsBlank() {
		Assert.assertTrue(StringTool.isBlank(null));
		Assert.assertTrue(StringTool.isBlank(""));
		Assert.assertTrue(StringTool.isBlank(" "));
		Assert.assertFalse(StringTool.isBlank("bob"));
		Assert.assertFalse(StringTool.isBlank("  bob  "));
	}

}
