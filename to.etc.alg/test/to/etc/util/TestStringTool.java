package to.etc.util;

import javax.annotation.*;

import org.junit.*;

/**
 * Test StringTool methods
 *
 * @author <a href="mailto:ben.schoen@itris.nl">Ben Schoen</a>
 * @since Dec 5, 2012
 */
public class TestStringTool {

	private final static String	PREFIX	= "PREFIX_";

	@Test(expected = IllegalArgumentException.class)
	public void testRandomStringWithPrefixLongerThanLength() {
		StringTool.getRandomStringWithPrefix(2, PREFIX);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testRandomStringWithPrefixSameThanLength() {
		StringTool.getRandomStringWithPrefix(7, PREFIX);
	}

	@Test
	public void testRandomStrings() {

		for(int i = 8; i < 1000; i++) {
			String result = StringTool.getRandomStringWithPrefix(i, PREFIX);
			checkRandomString(i, result);
		}
	}

	private void checkRandomString(int length, @Nonnull String result) {
		Assert.assertEquals("The string must be smaller than " + length + " : " + result, length, result.length());
		Assert.assertTrue("THe string must start with tne prefix 'PREFIX_' : " + result, result.startsWith(PREFIX));
	}


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
