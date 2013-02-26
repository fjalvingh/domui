package to.etc.domui.util;

import java.io.*;
import java.util.*;

import to.etc.util.*;

import net.htmlparser.jericho.*;

/**
 * Html utilities.
 *
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Feb 26, 2013
 */
final public class HtmlUtil {
	private HtmlUtil() {}

	private static final Set<String> VALID_ELEMENT_NAMES = new HashSet<String>(Arrays.asList(new String[]{HTMLElementName.BR, HTMLElementName.P, HTMLElementName.B, HTMLElementName.I,
		HTMLElementName.OL, HTMLElementName.UL, HTMLElementName.LI, HTMLElementName.A, HTMLElementName.CODE}));

	private static final Set<String> VALID_ATTRIBUTE_NAMES = new HashSet<String>(Arrays.asList(new String[]{"id", "class", "href", "target", "title"}));

	private static final Object VALID_MARKER = new Object();

	static public String removeUnsafe(String htmlIn) {
		System.out.println("Sanitize: input=" + htmlIn);
		String s = sanitise(htmlIn, false, true);
		StringBuilder sb = new StringBuilder();
		try {
			StringTool.entitiesToUnicode(sb, s, true);
		} catch(IOException x) {
			;
		}
		s = sb.toString();

		System.out.println("Output: " + s);
		return s;
	}

	private static String sanitise(String pseudoHTML, boolean formatWhiteSpace, boolean stripInvalidElements) {
		StringBuilder sb = new StringBuilder(pseudoHTML.length() + 2000);
		try {
			StringTool.entitiesToUnicode(sb, pseudoHTML, true);
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
					continue; // element will be encoded along with surrounding text
				outputDocument.remove(tag);
			}
			reencodeTextSegment(source, outputDocument, pos, tag.getBegin(), formatWhiteSpace);
			pos = tag.getEnd();
		}
		reencodeTextSegment(source, outputDocument, pos, source.getEnd(), formatWhiteSpace);
		return outputDocument.toString();
	}

	private static boolean processTag(Tag tag, OutputDocument outputDocument) {
		String elementName = tag.getName();
		if(!VALID_ELEMENT_NAMES.contains(elementName))
			return false;
		if(tag.getTagType() == StartTagType.NORMAL) {
			Element element = tag.getElement();
			if(HTMLElements.getEndTagRequiredElementNames().contains(elementName)) {
				if(element.getEndTag() == null)
					return false; // reject start tag if its required end tag is missing
			} else if(HTMLElements.getEndTagOptionalElementNames().contains(elementName)) {
				if(elementName == HTMLElementName.LI && !isValidLITag(tag))
					return false; // reject invalid LI tags
				if(element.getEndTag() == null)
					outputDocument.insert(element.getEnd(), getEndTagHTML(elementName)); // insert optional end tag if it is missing
			}
			outputDocument.replace(tag, getStartTagHTML(element.getStartTag()));
		} else if(tag.getTagType() == EndTagType.NORMAL) {
			if(tag.getElement() == null)
				return false; // reject end tags that aren't associated with a start tag
			if(elementName == HTMLElementName.LI && !isValidLITag(tag))
				return false; // reject invalid LI tags
			outputDocument.replace(tag, getEndTagHTML(elementName));
		} else {
			return false; // reject abnormal tags
		}
		return true;
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
		String res = removeUnsafe("One<p>Two</p><p><br /></p>");
		System.out.println("Res=" + res);
		res = removeUnsafe(res);
		System.out.println("Res=" + res);


	}


}
