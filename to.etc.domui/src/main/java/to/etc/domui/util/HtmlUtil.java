package to.etc.domui.util;

import net.htmlparser.jericho.Attribute;
import net.htmlparser.jericho.CharacterReference;
import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.EndTagType;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.HTMLElements;
import net.htmlparser.jericho.OutputDocument;
import net.htmlparser.jericho.Segment;
import net.htmlparser.jericho.Source;
import net.htmlparser.jericho.StartTag;
import net.htmlparser.jericho.StartTagType;
import net.htmlparser.jericho.Tag;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import to.etc.util.StringTool;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Html utilities.
 *
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Feb 26, 2013
 */
final public class HtmlUtil {
	private static final Logger LOG = LoggerFactory.getLogger(HtmlUtil.class);

	private HtmlUtil() {}

	private static final Set<String> VALID_ELEMENT_NAMES = new HashSet<String>(Arrays.asList(HTMLElementName.BR, HTMLElementName.P, HTMLElementName.B, HTMLElementName.I,
		HTMLElementName.OL, HTMLElementName.UL, HTMLElementName.LI, HTMLElementName.A, HTMLElementName.CODE, HTMLElementName.DIV
		//, HTMLElementName.H1, HTMLElementName.H2, HTMLElementName.H3, HTMLElementName.H4, HTMLElementName.H5, HTMLElementName.H6, HTMLElementName.EM
		//, HTMLElementName.DD, HTMLElementName.DL, HTMLElementName.DT, HTMLElementName.FONT, HTMLElementName.PRE
	));

	private static final Set<String> VALID_ATTRIBUTE_NAMES = new HashSet<String>(Arrays.asList("id", "class", "href", "target", "title", "color", "face", "size"));

	private static final Object VALID_MARKER = new Object();

	@Nullable
	static public String removeUnsafe(@Nullable String htmlIn) {
		if(null == htmlIn)
			return null;
//		System.out.println("Sanitize: input=" + htmlIn);
		String s = sanitize(htmlIn, false, true);
		s = compact(s);
		return s;
	}

	private static String sanitize(String pseudoHTML, boolean formatWhiteSpace, boolean stripInvalidElements) {
		StringBuilder sb = new StringBuilder(pseudoHTML.length() + 2000);
		try {
			StringTool.entitiesToUnicode(sb, pseudoHTML, true);	// jal 20131221 Do NOT convert quoted < and >!!
			pseudoHTML = sb.toString();
		} catch(IOException x) {
			//-- Sigh.
		}

		Source source = new Source(pseudoHTML);
		source.fullSequentialParse();
		OutputDocument outputDocument = new OutputDocument(source);
		List<Tag> tags = source.getAllTags();
		int pos = 0;
		for(Tag tag : tags) {
			if(processTag(tag, outputDocument)) {
				tag.setUserData(VALID_MARKER);
			} else {
				if(!stripInvalidElements)
					continue;									// element will be encoded along with surrounding text
				outputDocument.remove(tag);
			}
			reencodeTextSegment(source, outputDocument, pos, tag.getBegin(), formatWhiteSpace);
			pos = tag.getEnd();
		}
		reencodeTextSegment(source, outputDocument, pos, source.getEnd(), formatWhiteSpace);

		//-- Remove any entities from the result
		sb.setLength(0);
		try {
			StringTool.entitiesToUnicode(sb, outputDocument.toString(), true);	// jal 20131221 Do NOT convert quoted < and >!!
			pseudoHTML = sb.toString();
		} catch(IOException x) {
			//-- Sigh.
		}
		return sb.toString();
	}

	static private final String WS1 = "<p><br/></p>";
	static private final String WS2 = "<p><br /></p>";


	private static String compact(String input) {
		try {
			//-- Remove all instances of <p><br/><p> at the start and end of the input
			while(input.startsWith(WS1))
				input = input.substring(WS1.length());
			while(input.startsWith(WS2))
				input = input.substring(WS2.length());
			while(input.endsWith(WS1))
				input = input.substring(0, input.length() - WS1.length());
			while(input.endsWith(WS2))
				input = input.substring(0, input.length() - WS2.length());
			return input;
		} catch(Exception x) {
			LOG.error("HtmlUtil.compact error: " + x, x);
			return input;
		}
	}

	private static boolean processTag(Tag tag, OutputDocument outputDocument) {
		String elementName = tag.getName();

		if(!VALID_ELEMENT_NAMES.contains(elementName))
			return false;
		if(tag.getTagType() == StartTagType.NORMAL) {
			Element element = tag.getElement();
			if(isParentInCodeTag(tag)) {
				//-- Content inside <code> tag - replace all divs and br's with crlf
				if(elementName == HTMLElementName.DIV) {
					return false;
				} else if(elementName == HTMLElementName.BR) {
					outputDocument.replace(tag, "\n");
					return true;
				}
			} else if(HTMLElementName.CODE == elementName) {
				//-- If this is </code><code> (we're at the start element) remove both.
				Tag prev = tag.getPreviousTag();
				if(prev != null && prev.getTagType() == EndTagType.NORMAL && prev.getName() == HTMLElementName.CODE) {
					outputDocument.remove(prev);
//					outputDocument.remove(tag);
					outputDocument.replace(tag, "\n");
				}
				return true;
			} else if(HTMLElements.getEndTagRequiredElementNames().contains(elementName)) {
				if(element.getEndTag() == null)
					return false; 										// reject start tag if its required end tag is missing
			} else if(HTMLElements.getEndTagOptionalElementNames().contains(elementName)) {
				if(elementName == HTMLElementName.LI && !isValidLITag(tag))
					return false; 										// reject invalid LI tags
				if(element.getEndTag() == null)
					outputDocument.insert(element.getEnd(), getEndTagHTML(elementName)); // insert optional end tag if it is missing
			}
			CharSequence text = getStartTagHTML(element.getStartTag());
			String tagtext = tag.toString();
			if(!tagtext.equals(text.toString()))
				outputDocument.replace(tag, text);
		} else if(tag.getTagType() == EndTagType.NORMAL) {
			if(tag.getElement() == null)
				return false;											// reject end tags that aren't associated with a start tag
			if(elementName == HTMLElementName.LI && !isValidLITag(tag))
				return false;											// reject invalid LI tags
			if(isParentInCodeTag(tag)) {
				if(elementName == HTMLElementName.DIV) {
					outputDocument.replace(tag, "\n");
					return false;
				}
			}
			CharSequence text = getEndTagHTML(elementName);
			String tagtext = tag.toString();
			if(!tagtext.equals(text.toString()))
				outputDocument.replace(tag, text);
		} else {
			return false; 												// reject abnormal tags
		}
		return true;
	}

	private static boolean isParentInCodeTag(Tag tag) {
		Element dad = tag.getElement();
		for(;;) {
			dad = dad.getParentElement();
			if(dad == null)
				return false;
			if(dad.getName() == HTMLElementName.CODE) {
				return true;
			}
		}
	}


	private static boolean isValidLITag(Tag tag) {
		Element parentElement = tag.getElement().getParentElement();
		if(parentElement == null)
			return false; // ignore LI elements without a parent
		if(parentElement.getStartTag().getUserData() != VALID_MARKER)
			return false; // ignore LI elements who's parent is not valid
		return parentElement.getName() == HTMLElementName.UL || parentElement.getName() == HTMLElementName.OL; // only accept LI tags who's immediate parent is UL or OL.
	}

	private static void reencodeTextSegment(Source source, OutputDocument outputDocument, int begin, int end, boolean formatWhiteSpace) {
		if(begin >= end)
			return;
		Segment textSegment = new Segment(source, begin, end);
		String decodedText = CharacterReference.decode(textSegment);
		String encodedText = formatWhiteSpace ? CharacterReference.encodeWithWhiteSpaceFormatting(decodedText) : CharacterReference.encode(decodedText);
		if(!decodedText.equals(encodedText))
			outputDocument.replace(textSegment, encodedText);
	}

	private static CharSequence getStartTagHTML(StartTag startTag) {
		// tidies and filters out non-approved attributes
		StringBuilder sb = new StringBuilder();
		sb.append('<').append(startTag.getName());
		for(Attribute attribute : startTag.getAttributes()) {
			if(VALID_ATTRIBUTE_NAMES.contains(attribute.getKey())) {
				sb.append(' ').append(attribute.getName());
				if(attribute.getValue() != null) {
					sb.append("=\"");
					sb.append(CharacterReference.encode(attribute.getValue()));
					sb.append('"');
				}
			}
		}
		if(startTag.getElement().getEndTag() == null && !HTMLElements.getEndTagOptionalElementNames().contains(startTag.getName()))
			sb.append(" /");
		sb.append('>');
		return sb;
	}

	private static String getEndTagHTML(String tagName) {
		return "</" + tagName + '>';
	}

	public static void main(String[] args) {
		String res = removeUnsafe("Dit<p><code>i1</code><code>i2</code></p><p>een</p><p>twee</p>");
		System.out.println("Res=" + res);

	}


}
