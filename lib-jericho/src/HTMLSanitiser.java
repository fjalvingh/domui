import net.htmlparser.jericho.*;
import java.util.*;

/**
 * Provides facilities to sanitise HTML containing unwanted or invalid tags into clean HTML.
 * <p>
 * The sanitation process consists of the following steps:
 * <ul>
 *  <li>
 *   Find all potential HTML tags in the input text. For each tag:
 *   <ul>
 *    <li>If it is one of the allowed tags
 *     (<code>&lt;br&gt;</code>, <code>&lt;p&gt;</code>, <code>&lt;b&gt;</code>, <code>&lt;i&gt;</code>,
 *      <code>&lt;ol&gt;</code>, <code>&lt;ul&gt;</code>, <code>&lt;li&gt;</code>, <code>&lt;a&gt;</code>) then:
 *     <ul>
 *      <li>If a matching end tag is required, check that the end tag exists and is correctly nested. If not, reject the tag.
 *      <li>Check that the element is in a valid position (e.g. <code>&lt;li&gt;</code> elements must be inside <code>&lt;ul&gt;</code> or <code>&lt;ol&gt;</code> elements). If not, reject the element.
 *      <li>Keep only the allowed attributes (<code>id</code>, <code>class</code>, <code>href</code>, <code>target</code>, <code>title</code>) and strip any others.
 *      <li>Ensure all attributes are XHTML compliant (all values enclosed in double quotes and fully encoded)
 *      <li>Ensure tags are XHTML compliant (convert to lower case and add closing slash to empty element tag, e.g. <code>&lt;br /&gt;</code>)
 *     </ul>
 *    </li>
 *    <li>If it is not one of the allowed tags or was rejected for any reason:
 *     <ul>
 *      <li>If the method strips invalid markup, completely remove the tag or element from the output,
 *       otherwise encode it so that it renders verbatim.
 *     </ul>
 *    </li>
 *   </ul>
 *  </li>
 *  <li>
 *   If the <code>formatWhiteSpace</code> option is enabled:
 *   <ul>
 *    <li>Line breaks, being Carriage Return (U+000D) or Line Feed (U+000A) characters, and Form Feed characters (U+000C)
 *     are converted to "<code>&lt;br /&gt;</code>".  CR/LF pairs are treated as a single line break.
 *    <li>Multiple consecutive spaces are converted so that every second space is converted to "<code>&amp;nbsp;</code>"
 *     while ensuring the last is always a normal space.
 *    <li>Tab characters (U+0009) are converted as if they were four consecutive spaces.
 *   </ul>
 *  </li>
 *  <li>Ensure all remainding text is fully encoded.
 * </ul>
 */
public class HTMLSanitiser {
	private HTMLSanitiser() {} // not instantiable

	// list of HTML elements that will be retained in the final output:
	private static final Set<String> VALID_ELEMENT_NAMES=new HashSet<String>(Arrays.asList(new String[] {
		HTMLElementName.BR,
		HTMLElementName.P,
		HTMLElementName.B,
		HTMLElementName.I,
		HTMLElementName.OL,
		HTMLElementName.UL,
		HTMLElementName.LI,
		HTMLElementName.A
	}));

	// list of HTML attributes that will be retained in the final output:
	private static final Set<String> VALID_ATTRIBUTE_NAMES=new HashSet<String>(Arrays.asList(new String[] {
		"id","class","href","target","title"
	}));

	private static final Object VALID_MARKER=new Object();

	/**
	 * Returns a sanitised version of the specified HTML, encoding any unwanted tags.
	 * <p>
	 * Calling this method is equivalent to {@link #encodeInvalidMarkup(String,boolean) encodeInvalidMarkup(pseudoHTML,false)}.
	 * <p>
	 * <dl>
	 *  <dt><b>Example:</b></dt>
	 *  <dd>
	 *   <table border="1">
	 *    <tr><td>Method call:</td><td><pre style="margin:0">HTMLSanitiser.encodeInvalidMarkup("&lt;P&gt;&lt;u&gt;Line   1&lt;/u&gt;\n&lt;b&gt;Line   2&lt;/b&gt;\n&lt;script&gt;doBadStuff()&lt;/script&gt;")</pre></td></tr>
	 *    <tr><td>Output:</td><td><pre style="margin:0">&lt;p&gt;&amp;lt;u&amp;gt;Line   1&amp;lt;/u&amp;gt;\n&lt;b&gt;Line   2&lt;/b&gt;\n&amp;lt;script&amp;gt;doBadStuff()&amp;lt;/script&amp;gt;&lt;/p&gt;</pre></td></tr>
	 *    <tr><td>Rendered output:</td><td><p>&lt;u&gt;Line   1&lt;/u&gt; <b>Line   2</b> &lt;script&gt;doBadStuff()&lt;/script&gt;</p></td></tr>
	 *   </table>
	 *   In this example:
	 *   <ul>
	 *    <li>The <code>&lt;P&gt;</code> tag is kept and converted to lower case
	 *    <li>The optional end tag <code>&lt;/p&gt;</code> is added
	 *    <li>The <code>&lt;b&gt;</code> element is kept
	 *    <li>The unwanted <code>&lt;u&gt;</code> and <code>&lt;script&gt;</code> elements are encoded so that they render verbatim
	 *   </ul>
	 *  </dd>
	 * </dl>
	 * 
	 * @param pseudoHTML  The potentially invalid HTML to sanitise.
	 * @return a sanitised version of the specified HTML, encoding any unwanted tags.
	 */
	public static String encodeInvalidMarkup(String pseudoHTML) {
		return encodeInvalidMarkup(pseudoHTML,false);
	}

	/**
	 * Returns a sanitised version of the specified HTML, encoding any unwanted tags.
	 * <p>
	 * Encoding unwanted and invalid tags results in them appearing verbatim in the rendered output,
	 * helping to highlight the problem so that the source HTML can be fixed.
	 * <p>
	 * Specifying a value of <code>true</code> as an argument to the <code>formatWhiteSpace</code> parameter
	 * results in the formatting of white space as described in the sanitisation process in the class description above.
	 * <p>
	 * <dl>
	 *  <dt><b>Example:</b></dt>
	 *  <dd>
	 *   <table border="1">
	 *    <tr><td>Method call:</td><td><pre style="margin:0">HTMLSanitiser.encodeInvalidMarkup("&lt;P&gt;&lt;u&gt;Line   1&lt;/u&gt;\n&lt;b&gt;Line   2&lt;/b&gt;\n&lt;script&gt;doBadStuff()&lt;/script&gt;",true)</pre></td></tr>
	 *    <tr><td>Output:</td><td><pre style="margin:0">&lt;p&gt;&amp;lt;u&amp;gt;Line &amp;nbsp; 1&amp;lt;/u&amp;gt;&lt;br /&gt;&lt;b&gt;Line &amp;nbsp; 2&lt;/b&gt;&lt;br /&gt;&amp;lt;script&amp;gt;doBadStuff()&amp;lt;/script&amp;gt;&lt;/p&gt;</pre></td></tr>
	 *    <tr><td>Rendered output:</td><td><p>&lt;u&gt;Line &nbsp; 1&lt;/u&gt;<br /><b>Line &nbsp; 2</b><br />&lt;script&gt;doBadStuff()&lt;/script&gt;</p></td></tr>
	 *   </table>
	 *   In this example:
	 *   <ul>
	 *    <li>The <code>&lt;P&gt;</code> tag is kept and converted to lower case
	 *    <li>The optional end tag <code>&lt;/p&gt;</code> is added
	 *    <li>The <code>&lt;b&gt;</code> element is kept
	 *    <li>The unwanted <code>&lt;u&gt;</code> and <code>&lt;script&gt;</code> elements are encoded so that they render verbatim
	 *    <li>The line feed characters are converted to <code>&lt;br /&gt;</code> elements
	 *    <li>Non-breaking spaces (<code>&amp;nbsp;</code>) are added to ensure the multiple spaces are rendered as they appear in the input.
	 *   </ul>
	 *  </dd>
	 * </dl>
	 * 
	 * @param pseudoHTML  The potentially invalid HTML to sanitise.
	 * @param formatWhiteSpace  Specifies whether white space should be marked up in the output.
	 * @return a sanitised version of the specified HTML, encoding any unwanted tags.
	 */
	public static String encodeInvalidMarkup(String pseudoHTML, boolean formatWhiteSpace) {
		return sanitise(pseudoHTML,formatWhiteSpace,false);
	}

	/**
	 * Returns a sanitised version of the specified HTML, stripping any unwanted tags.
	 * <p>
	 * Calling this method is equivalent to {@link #stripInvalidMarkup(String,boolean) stripInvalidMarkup(pseudoHTML,false)}.
	 * <p>
	 * <dl>
	 *  <dt><b>Example:</b></dt>
	 *  <dd>
	 *   <table border="1">
	 *    <tr><td>Method call:</td><td><pre style="margin:0">HTMLSanitiser.stripInvalidMarkup("&lt;P&gt;&lt;u&gt;Line   1&lt;/u&gt;\n&lt;b&gt;Line   2&lt;/b&gt;\n&lt;script&gt;doBadStuff()&lt;/script&gt;")</pre></td></tr>
	 *    <tr><td>Output:</td><td><pre style="margin:0">&lt;p&gt;Line   1\n&lt;b&gt;Line   2&lt;/b&gt;\n&lt;/p&gt;</pre></td></tr>
	 *    <tr><td>Rendered output:</td><td><p>Line   1 <b>Line   2</b> </p></td></tr>
	 *   </table>
	 *   In this example:
	 *   <ul>
	 *    <li>The <code>&lt;P&gt;</code> tag is kept and converted to lower case
	 *    <li>The optional end tag <code>&lt;/p&gt;</code> is added
	 *    <li>The <code>&lt;b&gt;</code> element is kept
	 *    <li>The unwanted <code>&lt;u&gt;</code> and <code>&lt;script&gt;</code> elements are stripped from the output
	 *   </ul>
	 *  </dd>
	 * </dl>
	 * 
	 * @param pseudoHTML  The potentially invalid HTML to sanitise.
	 * @return a sanitised version of the specified HTML, stripping any unwanted tags.
	 */
	public static String stripInvalidMarkup(String pseudoHTML) {
		return stripInvalidMarkup(pseudoHTML,false);
	}

	/**
	 * Returns a sanitised version of the specified HTML, stripping any unwanted tags.
	 * <p>
	 * Stripping unwanted and invalid tags is the preferred option if the output is for public consumption.
	 * <p>
	 * Specifying a value of <code>true</code> as an argument to the <code>formatWhiteSpace</code> parameter
	 * results in the formatting of white space as described in the sanitisation process in the class description above.
	 * <p>
	 * <dl>
	 *  <dt><b>Example:</b></dt>
	 *  <dd>
	 *   <table border="1">
	 *    <tr><td>Method call:</td><td><pre style="margin:0">HTMLSanitiser.stripInvalidMarkup("&lt;P&gt;&lt;u&gt;Line   1&lt;/u&gt;\n&lt;b&gt;Line   2&lt;/b&gt;\n&lt;script&gt;doBadStuff()&lt;/script&gt;",true)</pre></td></tr>
	 *    <tr><td>Output:</td><td><pre style="margin:0">&lt;p&gt;Line &amp;nbsp; 1&lt;br /&gt;&lt;b&gt;Line &amp;nbsp; 2&lt;/b&gt;&lt;br /&gt;&lt;/p&gt;</pre></td></tr>
	 *    <tr><td>Rendered output:</td><td><p>Line &nbsp; 1<br /><b>Line &nbsp; 2</b><br /></p></td></tr>
	 *   </table>
	 *   In this example:
	 *   <ul>
	 *    <li>The <code>&lt;P&gt;</code> tag is kept and converted to lower case
	 *    <li>The optional end tag <code>&lt;/p&gt;</code> is added
	 *    <li>The <code>&lt;b&gt;</code> element is kept
	 *    <li>The unwanted <code>&lt;u&gt;</code> and <code>&lt;script&gt;</code> elements are stripped from the output
	 *    <li>The line feed characters are converted to <code>&lt;br /&gt;</code> elements
	 *    <li>Non-breaking spaces (<code>&amp;nbsp;</code>) are added to ensure the multiple spaces are rendered as they appear in the input.
	 *   </ul>
	 *  </dd>
	 * </dl>
	 * 
	 * @param pseudoHTML  The potentially invalid HTML to sanitise.
	 * @param formatWhiteSpace  Specifies whether white space should be marked up in the output.
	 * @return a sanitised version of the specified HTML, stripping any unwanted tags.
	 */
	public static String stripInvalidMarkup(String pseudoHTML, boolean formatWhiteSpace) {
		return sanitise(pseudoHTML,formatWhiteSpace,true);
	}

	private static String sanitise(String pseudoHTML, boolean formatWhiteSpace, boolean stripInvalidElements) {
		Source source=new Source(pseudoHTML);
		source.fullSequentialParse();
		OutputDocument outputDocument=new OutputDocument(source);
		List<Tag> tags=source.getAllTags();
		int pos=0;
	  for (Tag tag : tags) {
			if (processTag(tag,outputDocument)) {
			  tag.setUserData(VALID_MARKER);
			} else {
				if (!stripInvalidElements) continue; // element will be encoded along with surrounding text
				outputDocument.remove(tag);
			}
			reencodeTextSegment(source,outputDocument,pos,tag.getBegin(),formatWhiteSpace);
			pos=tag.getEnd();
		}
	  reencodeTextSegment(source,outputDocument,pos,source.getEnd(),formatWhiteSpace);
		return outputDocument.toString();
	}

	private static boolean processTag(Tag tag, OutputDocument outputDocument) {
		String elementName=tag.getName();
		if (!VALID_ELEMENT_NAMES.contains(elementName)) return false;
		if (tag.getTagType()==StartTagType.NORMAL) {
			Element element=tag.getElement();
			if (HTMLElements.getEndTagRequiredElementNames().contains(elementName)) {
				if (element.getEndTag()==null) return false; // reject start tag if its required end tag is missing
			} else if (HTMLElements.getEndTagOptionalElementNames().contains(elementName)) {
				if (elementName==HTMLElementName.LI && !isValidLITag(tag)) return false; // reject invalid LI tags
				if (element.getEndTag()==null) outputDocument.insert(element.getEnd(),getEndTagHTML(elementName)); // insert optional end tag if it is missing
			}
			outputDocument.replace(tag,getStartTagHTML(element.getStartTag()));
		} else if (tag.getTagType()==EndTagType.NORMAL) {
			if (tag.getElement()==null) return false; // reject end tags that aren't associated with a start tag
			if (elementName==HTMLElementName.LI && !isValidLITag(tag)) return false; // reject invalid LI tags
			outputDocument.replace(tag,getEndTagHTML(elementName));
		} else {
			return false; // reject abnormal tags
		}
		return true;
	}

	private static boolean isValidLITag(Tag tag) {
		Element parentElement=tag.getElement().getParentElement();
		if (parentElement==null) return false; // ignore LI elements without a parent
		if (parentElement.getStartTag().getUserData()!=VALID_MARKER) return false; // ignore LI elements who's parent is not valid
		return parentElement.getName()==HTMLElementName.UL || parentElement.getName()==HTMLElementName.OL; // only accept LI tags who's immediate parent is UL or OL.
	}

	private static void reencodeTextSegment(Source source, OutputDocument outputDocument, int begin, int end, boolean formatWhiteSpace) {
	  if (begin>=end) return;
	  Segment textSegment=new Segment(source,begin,end);
		String decodedText=CharacterReference.decode(textSegment);
		String encodedText=formatWhiteSpace ? CharacterReference.encodeWithWhiteSpaceFormatting(decodedText) : CharacterReference.encode(decodedText);
    outputDocument.replace(textSegment,encodedText);
	}

	private static CharSequence getStartTagHTML(StartTag startTag) {
		// tidies and filters out non-approved attributes
		StringBuilder sb=new StringBuilder();
		sb.append('<').append(startTag.getName());
	  for (Attribute attribute : startTag.getAttributes()) {
	    if (VALID_ATTRIBUTE_NAMES.contains(attribute.getKey())) {
				sb.append(' ').append(attribute.getName());
				if (attribute.getValue()!=null) {
					sb.append("=\"");
				  sb.append(CharacterReference.encode(attribute.getValue()));
					sb.append('"');
				}
			}
	  }
	  if (startTag.getElement().getEndTag()==null && !HTMLElements.getEndTagOptionalElementNames().contains(startTag.getName())) sb.append(" /");
		sb.append('>');
		return sb;
	}

	private static String getEndTagHTML(String tagName) {
		return "</"+tagName+'>';
	}


	//////////////////////////////////////////////////////////////////////////////////////
	// THE METHODS BELOW ARE USED ONLY FOR DEMONSTRATING THE FUNCTIONALITY OF THE CLASS //
	//////////////////////////////////////////////////////////////////////////////////////
	// See test/src/samples/HTMLSanitiserTest.java for a comprehensive test suite.

	public static void main(String[] args) throws Exception {
		System.out.println("Examples of HTMLSanitiser.encodeInvalidMarkup:");
		System.out.println("----------------------------------------------\n");
		
		displayEncodeInvalidMarkup("ab & c","encode text");
		displayEncodeInvalidMarkup("abc <u>def</u> geh","<U> element not allowed");
		displayEncodeInvalidMarkup("<p>abc","add optional end tag");
		displayEncodeInvalidMarkup("<script>abc</script>","remove potentially dangerous script");
		displayEncodeInvalidMarkup("<p class=\"xyz\" onmouseover=\"nastyscript\">abc</p>","keep approved attributes but strip non-approved attributes");
		displayEncodeInvalidMarkup("<p id=abc class='xyz'>abc</p>","tidy up attributes to make them XHTML compliant");
		displayEncodeInvalidMarkup("List:<ul><li>A</li><li>B<li>C</ul>","inserts optional end tags");

		System.out.println("Examples of HTMLSanitiser.stripInvalidMarkup:");
		System.out.println("---------------------------------------------\n");

		displayStripInvalidMarkup("ab & c","encode text");
		displayStripInvalidMarkup("abc <u>def</u> geh","<U> element not allowed");
		displayStripInvalidMarkup("<p>abc","add optional end tag");
		displayStripInvalidMarkup("<script>abc</script>","remove potentially dangerous script");
		displayStripInvalidMarkup("<p class=\"xyz\" onmouseover=\"nastyscript\">abc</p>","keep approved attributes but strip non-approved attributes");
		displayStripInvalidMarkup("<p id=abc class='xyz'>abc</p>","tidy up attributes to make them XHTML compliant");
		displayStripInvalidMarkup("List:<ul><li>A</li><li>B<li>C</ul>","inserts optional end tags");
		displayStripInvalidMarkup("List:<li>A</li><li>B<li>C","missing required <UL> or <OL> element");
		displayStripInvalidMarkup("List:<ul><li>A</li><b><li>B</b><li>C</ul>","<LI> is invalid as it is not directly under <UL> or <OL>");

		System.out.println("Examples of HTMLSanitiser.stripInvalidMarkup with formatWhiteSpace=true:");
		System.out.println("------------------------------------------------------------------------\n");

		displayStripInvalidMarkup("abc\ndef",true,"convert LF to <BR>");
		displayStripInvalidMarkup("    abc",true,"ensure consecutive spaces are rendered");
		displayStripInvalidMarkup("\tabc",true,"convert TAB to equivalent of four spaces");
	}

	private static void displayEncodeInvalidMarkup(String input, String explanation) {
		display(input,explanation,HTMLSanitiser.encodeInvalidMarkup(input));
	}

	private static void displayStripInvalidMarkup(String input, String explanation) {
		display(input,explanation,HTMLSanitiser.stripInvalidMarkup(input));
	}

	private static void displayStripInvalidMarkup(String input, boolean formatWhiteSpace, String explanation) {
		display(input,explanation,HTMLSanitiser.stripInvalidMarkup(input,formatWhiteSpace));
	}

	private static void display(String input, String explanation, String output) {
		System.out.println(explanation+":\ninput : "+input+"\noutput: "+output+"\n");
	}
}
