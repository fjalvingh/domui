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

	private StringBuilder	m_text	= new StringBuilder();

	/**
	 * <pre>
	 * Test the method StringTool.removeLeadingCharIfTooLongForOracle() with only
	 * one 1-byte characters. And the size is exactly StringTool.MAX_SIZE_IN_BYTES_FOR_ORACLE_VARCHAR2.
	 *
	 * @throws Exception
	 * </pre>
	 */
	@Test
	public void testTruncLeadingOracleColumnMaxSize() throws Exception {

		int textLength = StringTool.MAX_SIZE_IN_BYTES_FOR_ORACLE_VARCHAR2;
		String textIn = makeText(textLength, "a");

		Assert.assertEquals("Length of m_text should be " + StringTool.MAX_SIZE_IN_BYTES_FOR_ORACLE_VARCHAR2, StringTool.MAX_SIZE_IN_BYTES_FOR_ORACLE_VARCHAR2, textIn.getBytes("UTF8").length);

		String textOut = StringTool.truncLeadingOracleColumn(textIn, StringTool.MAX_SIZE_IN_BYTES_FOR_ORACLE_VARCHAR2);
		Assert.assertNotNull("This can't be null!", textOut);

		Assert.assertEquals("Length of m_text should be " + StringTool.MAX_SIZE_IN_BYTES_FOR_ORACLE_VARCHAR2, StringTool.MAX_SIZE_IN_BYTES_FOR_ORACLE_VARCHAR2, textOut.getBytes("UTF8").length);
	}

	/**
	 * <pre>
	 * Test the method StringTool.removeLeadingCharIfTooLongForOracle() with one 2-bytes (é)
	 * and one 1-bytes character (a). And the size is too long.
	 *
	 * @throws Exception
	 * </pre>
	 */
	@Test
	public void testTruncLeadingOracleColumnTooLarge() throws Exception {

		int textLength = StringTool.MAX_SIZE_IN_BYTES_FOR_ORACLE_VARCHAR2;
		String textIn = makeText(textLength, "éa");
		Assert.assertEquals("Length of m_text should be 12000", 12000, textIn.getBytes("UTF8").length);

		String textOut = StringTool.truncLeadingOracleColumn(textIn, StringTool.MAX_SIZE_IN_BYTES_FOR_ORACLE_VARCHAR2);
		Assert.assertNotNull("This can't be null!", textOut);
		Assert.assertEquals("Length of m_text should be " + StringTool.MAX_SIZE_IN_BYTES_FOR_ORACLE_VARCHAR2, StringTool.MAX_SIZE_IN_BYTES_FOR_ORACLE_VARCHAR2, textOut.getBytes("UTF8").length);
	}

	/**
	 * <pre>
	 * Test the method StringTool.removeLeadingCharIfTooLongForOracle() with all kind of
	 * characters. And the size is too long.
	 *
	 * @throws Exception
	 * </pre>
	 */
	@Test
	public void testTruncLeadingOracleColumnAllKindOfChars() throws Exception {

		int textLength = 300;
		String textIn = makeText(textLength, "jkslgkj804839h3jotjng08ĺjgŕ´sś skjgs jkglsdśdfśdé");
		Assert.assertEquals("Length of m_text should be 16800", 16800, textIn.getBytes("UTF8").length);

		String textOut = StringTool.truncLeadingOracleColumn(textIn, StringTool.MAX_SIZE_IN_BYTES_FOR_ORACLE_VARCHAR2);
		Assert.assertNotNull("This can't be null!", textOut);
		Assert.assertEquals("Length of m_text should be " + StringTool.MAX_SIZE_IN_BYTES_FOR_ORACLE_VARCHAR2, StringTool.MAX_SIZE_IN_BYTES_FOR_ORACLE_VARCHAR2, textOut.getBytes("UTF8").length);
	}


	/**
	 * <pre>
	 * Test the method StringTool.removeLeadingCharIfTooLongForOracle() with only diacritical
	 * characters. And the size is too long.
	 *
	 * @throws Exception
	 * </pre>
	 */
	@Test
	public void testtruncLeadingOracleColumnOnlyDiacriticalChars() throws Exception {

		int textLength = 3000;
		String textIn = makeText(textLength, "śéü");
		Assert.assertEquals("Length of m_text should be 18000", 18000, textIn.getBytes("UTF8").length);

		String textOut = StringTool.truncLeadingOracleColumn(textIn, StringTool.MAX_SIZE_IN_BYTES_FOR_ORACLE_VARCHAR2);
		Assert.assertNotNull("This can't be null!", textOut);
		Assert.assertEquals("Length of m_text should be " + (StringTool.MAX_SIZE_IN_BYTES_FOR_ORACLE_VARCHAR2 - 2), StringTool.MAX_SIZE_IN_BYTES_FOR_ORACLE_VARCHAR2 - 2,
			textOut.getBytes("UTF8").length);
	}

	/**
	 * <pre>
	 * Test the method StringTool.removeLeadingCharIfTooLongForOracle() with only
	 * one 1-byte characters. And the size is more than the column size (50).
	 *
	 * @throws Exception
	 * </pre>
	 */
	@Test
	public void testTruncLeadingOracleColumnTooMuchChar() throws Exception {

		int textLength = 60;
		String textIn = makeText(textLength, "a");

		Assert.assertEquals("Length of m_text should be 60", 60, textIn.getBytes("UTF8").length);

		// COLUMN SIZE is 50
		String textOut = StringTool.truncLeadingOracleColumn(textIn, 50);
		Assert.assertNotNull("This can't be null!", textOut);
		Assert.assertEquals("Length of m_text should be 50", 50, textOut.getBytes("UTF8").length);
	}

	/**
	 * <pre>
	 * Test the method StringTool.removeLeadingCharIfTooLongForOracle() with too much
	 * bytes. And the size is the same as the column size (3000).
	 *
	 * @throws Exception
	 * </pre>
	 */
	@Test
	public void testTruncLeadingOracleColumnTooMuchBytes() throws Exception {

		int textLength = 3000;
		String textIn = makeText(textLength, "éa");

		Assert.assertEquals("Length of m_text should be 9000", 9000, textIn.getBytes("UTF8").length);

		// COLUMN SIZE is 3000
		String textOut = StringTool.truncLeadingOracleColumn(textIn, 3000);
		Assert.assertNotNull("This can't be null!", textOut);
		Assert.assertEquals("Length of m_text in bytes should be 4000", 4000, textOut.getBytes("UTF8").length);
		// Because there are to much bytes used the max number of characters is 2667.
		Assert.assertEquals("Length of m_text in characters should be 2667", 2667, textOut.length());
	}

	/**
	 * <pre>
	 * Test the method StringTool.strToJavascriptString(final String cs, final boolean dblquote)
	 * if single quotes are escaped as expected.
	 *
	 * </pre>
	 */
	@Test
	public void testStrToJavascriptStringSingleQuotes() {

		String textIn = "'test string'";
		String testOut = StringTool.strToJavascriptString(textIn, false);

		Assert.assertTrue(testOut.contains("\\'test string\\'"));
	}

	/**
	 * <pre>
	 * Test the method StringTool.strToJavascriptString(final String cs, final boolean dblquote)
	 * if double quotes are escaped as expected.
	 *
	 * @throws Exception
	 * </pre>
	 */
	@Test
	public void testStrToJavascriptStringDoubleQuotes() {

		String textIn = "\"test string\"";
		String testOut = StringTool.strToJavascriptString(textIn, true);

		StringBuilder sb = new StringBuilder();
		sb.append("\\\"");
		sb.append("test string");
		sb.append("\\\"");
		String expectedStringSequence = sb.toString();

		Assert.assertTrue(testOut.contains(expectedStringSequence));

	}

	private String makeText(int textLength, String text) {

		m_text.setLength(0);
		for(int i = 0; i < textLength; i++) {
			m_text.append(text);
		}
		return m_text.toString();
	}
}
