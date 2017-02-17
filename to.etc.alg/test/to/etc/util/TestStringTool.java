package to.etc.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.*;

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

	@Test
	/**
	 * Test the validation of e-mailaddresses. user@localhost is a special one. It should be accepted too.
	 */
	public void testValidateEmail() {

		List<String> validEmailAdresses = Arrays.asList("a.b@c.d", "a.b.c@d.e", "user@localhost");
		for(String emailaddress : validEmailAdresses) {
			checkValidEmailAddress(emailaddress);
		}

		List<String> invalidEmailAdresses = Arrays.asList("a.b@c", "a@b..c", "a.b@.c", "a.b@..c", "@a.b", "a.@b.c", ".a.b@c.d", "a b@b.c", "a @b.c", " a@b.c", "");
		for(String emailaddress : invalidEmailAdresses) {
			checkInValidEmailAddress(emailaddress);
		}
	}

	@Test
	public void isValidEmail_checkEmailLength(){
		String localpart = StringTool.getRandomStringWithPrefix(30, "");
		final String domainName = "domain.com";

		assertTrue(StringTool.isValidEmail(localpart + "@" + domainName));

		localpart = StringTool.getRandomStringWithPrefix(StringTool.MAX_EMAIL_LENGTH, "");
		assertFalse(StringTool.isValidEmail(localpart + "@" + domainName));

		localpart = StringTool.getRandomStringWithPrefix(StringTool.MAX_EMAIL_LENGTH - 1 /*@*/ - domainName.length(), "");
		assertTrue(StringTool.isValidEmail(localpart + "@" + domainName));
	}

	private void checkValidEmailAddress(@Nullable String emailAddress) {
		Assert.assertTrue("E-mailaddress " + emailAddress + " is a valid e-mailaddress.", StringTool.isValidEmail(emailAddress));
	}

	private void checkInValidEmailAddress(@Nullable String emailAddress) {
		Assert.assertFalse("E-mailaddress " + emailAddress + " is an invalid e-mailaddress.", StringTool.isValidEmail(emailAddress));
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

	private StringBuilder m_text = new StringBuilder();

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
	 * Test that small strings are truncated correcly.
	 */
	@Test
	public void testByteTruncate2() {
		String text = "01234567890123456789";
		String res = StringTool.strOracleTruncate(text, 10);
		Assert.assertEquals("Text length must be 10 chars", 10, res.length());
	}

	@Test
	public void testByteTruncate3() {
		String text = makeText(2000, "a");
		String res = StringTool.strOracleTruncate(text, 1900);
		Assert.assertEquals("Text length must be 1900 chars", 1900, res.length());
	}

	@Test
	public void testByteTruncate4() {
		String text = makeText(1333, "\u20ac");					// 1333 Euro signs
		String res = StringTool.strOracleTruncate(text, 1000);
		Assert.assertEquals("Text length must be 1000 chars", 1000, res.length());
	}

	/**
	 * Test the exact cutoff point, just before
	 */
	@Test
	public void testByteTruncate5() {
		String text = makeText(1333, "\u20ac");					// 1333 Euro signs
		String res = StringTool.strOracleTruncate(text, 2000);
		Assert.assertEquals("Text length must be 1333 chars", 1333, res.length());
	}

	/**
	 * Test the exact cutoff point, just before
	 */
	@Test
	public void testByteTruncate6() {
		String text = makeText(1334, "\u20ac");					// 1334 Euro signs = 4002 bytes
		String res = StringTool.strOracleTruncate(text, 2000);
		Assert.assertEquals("Text length must be 1333 chars", 1333, res.length());
	}

	@Test
	public void testByteTruncateSuffix1() {
		String text = "01234567890123456789";
		String res = StringTool.strOracleTruncate(text, 10, "...");
		Assert.assertEquals("0123456...", res);
	}

	/**
	 * Test exact cutoff point: string has max possible size.
	 */
	@Test
	public void testByteTruncateSuffix2() {
		String text = makeText(1333, "\u20ac");
		String res = StringTool.strOracleTruncate(text, 2000, "...");
		Assert.assertEquals("Truncated size must be 1333", 1333, res.length());

		Assert.assertEquals(text, res);
	}

	/**
	 * Test exact cutoff point: string has one euro character too much.
	 */
	@Test
	public void testByteTruncateSuffix3() {
		String text = makeText(1334, "\u20ac");
		String res = StringTool.strOracleTruncate(text, 2000, "...");
		Assert.assertEquals("Truncated size must be 1333", 1333, res.length());

		String match = makeText(1330, "\u20ac") + "...";
		Assert.assertEquals(match, res);
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

	@Test
	public void replaceNewLineChars() {
		Assert.assertEquals("ab", StringTool.replaceNewLineChars("ab\r\n\r\n", ""));
		Assert.assertEquals("ab", StringTool.replaceNewLineChars("ab\n\n", ""));
		Assert.assertEquals("ab", StringTool.replaceNewLineChars("ab", ""));
		Assert.assertEquals("ab bebeb  ", StringTool.replaceNewLineChars("ab\r\nbebeb\n\n", " "));
		Assert.assertEquals("", StringTool.replaceNewLineChars("", ""));
	}

	private String makeText(int textLength, String text) {

		m_text.setLength(0);
		for(int i = 0; i < textLength; i++) {
			m_text.append(text);
		}
		return m_text.toString();
	}

	@Test
	public void validateUrl_valid() {
		assertTrue(StringTool.validateUrl("www.google.com"));
		assertTrue(StringTool.validateUrl("74.125.206.105"));
		assertTrue(StringTool.validateUrl("http://74.125.206.105"));
		assertTrue(StringTool.validateUrl("http://www.google.com"));
		assertTrue(StringTool.validateUrl("http://www.nu.nl/"));
		assertTrue(StringTool.validateUrl("http://www.itris.nl/"));
		assertTrue(StringTool.validateUrl("https://docs.google.com/a/itris.eu/spreadsheets/d/1GxCUI6FheVXAqI44o_zIh_CU7W-odiuJTY/edit#gid=1008896417"));
		assertTrue(StringTool.validateUrl("http://localhost:8080/Itris_VO02/iRed/Portal/Menu/ViewPointMenuMain.jsp"));
		assertTrue(StringTool.validateUrl("http://vfdsf:8080/fdsf/fdsf/nl/webintelligence.pdf"));
		assertTrue(StringTool.validateUrl("http://www.opmaat.nl"));
		assertTrue(StringTool.validateUrl("http://20-web-vp-01/viewpoint"));
		assertTrue(StringTool.validateUrl("localhost:8080/Itris_VO02"));
	}

	@Test
	public void validateUrl_notvalid() {
		assertFalse(StringTool.validateUrl(null));
		assertFalse(StringTool.validateUrl(""));
		assertFalse(StringTool.validateUrl(StringTool.getRandomStringWithPrefix(10, "UT_")));
		assertFalse(StringTool.validateUrl("www.goog le.com"));
		assertFalse(StringTool.validateUrl("www.goog\\le.com"));
		assertFalse(StringTool.validateUrl("www.goog=le.com"));
		assertFalse(StringTool.validateUrl("http://www test test/"));
	}

	@Test
	public void testStrOracleTruncate() throws Exception {
		String s = FileTool.readResourceAsString(getClass(), "longMsg.txt", "utf-8");
		String lessStr = StringTool.strOracleTruncate(s, 3997);
		Assert.assertEquals("we get cut to proper bytes limit", 3990, lessStr.length());
		byte[] data = lessStr.getBytes("UTF-8");
		Assert.assertEquals("we get proper byte size", 4000, data.length);
	}

	@Test
	public void testStrOracleTruncateWithSuffix_withCutoff() throws Exception {
		String s = FileTool.readResourceAsString(getClass(), "longMsg.txt", "utf-8");
		String lessStr = StringTool.strOracleTruncate(s, 4000, "...");
		Assert.assertEquals("we get cut to proper bytes limit", 3990, lessStr.length());
		Assert.assertEquals("we have proper suffix", "...", lessStr.substring(lessStr.length() - 3));
		byte[] data = lessStr.getBytes("UTF-8");
		Assert.assertEquals("we get proper byte size", 4000, data.length);
	}

	@Test
	public void testStrOracleTruncateWithSuffix_noCutOff() throws Exception {
		String oracleStr = StringTool.strOracleTruncate("short one", 4000, "...");
		Assert.assertEquals("we get cut to proper bytes limit", 9, oracleStr.length());
		Assert.assertNotEquals("we do not have suffix", "...", oracleStr.substring(oracleStr.length() - 3));
	}

	@Test
	public void testRenderAsRawHtml_html_cutoff() throws Exception {
		String input = "<p style=\"color:red;\">Make an appointment in the agenda!</p><br>\n"
			+ "<p style=\"color:green;\">Maak een afspraak in de agenda!</p><br>";
		String expected = "<p style=\"color:red;\">Make an appointment in the agenda!</p><br/>"
			+ "<p style=\"color:green;\">Maak een afspraak in de agenda!</p>";
		Assert.assertEquals("we get expected html out", expected, StringTool.renderAsRawHtml(input, true));
	}

	@Test
	public void testRenderAsRawHtml_html_no_cutoff() throws Exception {
		String input = "<p style=\"color:red;\">Make an appointment in the agenda!</p><br>\n"
			+ "<p style=\"color:green;\">Maak een afspraak in de agenda!</p><br><br/>\n<br/>";
		String expected = "<p style=\"color:red;\">Make an appointment in the agenda!</p><br/>"
			+ "<p style=\"color:green;\">Maak een afspraak in de agenda!</p><br/><br/><br/>";
		Assert.assertEquals("we get expected html out", expected, StringTool.renderAsRawHtml(input, false));
	}

	@Test
	public void testRenderAsRawHtml_no_html_cutoff() throws Exception {
		String input = "bla bla bla\nother bla\n\n";
		String expected = "bla bla bla<br/>other bla";
		Assert.assertEquals("we get expected html out", expected, StringTool.renderAsRawHtml(input, true));
	}

	@Test
	public void testRenderAsRawHtml_no_html_no_cutoff() throws Exception {
		String input = "bla bla bla\nother bla\n\n";
		String expected = "bla bla bla<br/>other bla<br/><br/>";
		Assert.assertEquals("we get expected html out", expected, StringTool.renderAsRawHtml(input, false));
	}

	@Test
	public void testRemoveRepeatingCharacters_whenNoRepeatingChars_returnSameString() {
		String s = "abc123 abc 123 11 aa 123456789123456789";
		String res = StringTool.removeRepeatingCharacters(s);
		Assert.assertEquals(s, res);
	}

	@Test
	public void testRemoveRepeatingCharacters_whenHasRepeatingNonDigits_shortenString() {
		String s = "abcXXXabc 123456789123456789";
		String res = StringTool.removeRepeatingCharacters(s);
		Assert.assertEquals("abcXabc 123456789123456789", res);

		s = "abcXXXXXXabc 123456789123456789";
		res = StringTool.removeRepeatingCharacters(s);
		Assert.assertEquals("abcXabc 123456789123456789", res);

		s = "abc123XXXXXXabc 123456789123456789";
		res = StringTool.removeRepeatingCharacters(s);
		Assert.assertEquals("abc123Xabc 123456789123456789", res);

		s = "abcXXXXXX123abc 123456789123456789";
		res = StringTool.removeRepeatingCharacters(s);
		Assert.assertEquals("abcX123abc 123456789123456789", res);
	}

	@Test
	public void testRemoveRepeatingCharacters_whenHasRepeatingDigits_dontShortenString() {
		String s = "abc111abc 123456789123456789";
		String res = StringTool.removeRepeatingCharacters(s);
		Assert.assertEquals("abc111abc 123456789123456789", res);

		s = "abc1111111abc 123456789123456789";
		res = StringTool.removeRepeatingCharacters(s);
		Assert.assertEquals("abc1111111abc 123456789123456789", res);
	}
}
