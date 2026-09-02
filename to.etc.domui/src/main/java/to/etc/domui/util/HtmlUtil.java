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

	private static final Set<String> VALID_ELEMENT_NAMES = new HashSet<>(Arrays.asList(HTMLElementName.BR, HTMLElementName.P, HTMLElementName.B, HTMLElementName.I, HTMLElementName.U,
		HTMLElementName.OL, HTMLElementName.UL, HTMLElementName.LI, HTMLElementName.A, HTMLElementName.CODE, HTMLElementName.DIV, HTMLElementName.STRIKE, HTMLElementName.STRONG,
		HTMLElementName.BLOCKQUOTE, HTMLElementName.SUP, HTMLElementName.SUB, HTMLElementName.HR
		//, HTMLElementName.H1, HTMLElementName.H2, HTMLElementName.H3, HTMLElementName.H4, HTMLElementName.H5, HTMLElementName.H6, HTMLElementName.EM
		//, HTMLElementName.DD, HTMLElementName.DL, HTMLElementName.DT, HTMLElementName.FONT, HTMLElementName.PRE
	));

	private static final Set<String> VALID_ATTRIBUTE_NAMES = new HashSet<>(Arrays.asList("id", "class", "href", "target", "title", "color", "face", "size", "style"));

	/**
	 * Attributes whose value is a url. Their value is checked against {@link #SAFE_URL_SCHEMES},
	 * because an allowed attribute with a "javascript:" value is exactly as dangerous as a script
	 * element would have been.
	 */
	private static final Set<String> URL_ATTRIBUTE_NAMES = new HashSet<>(Arrays.asList("href", "src", "action", "background", "cite", "formaction", "longdesc", "poster", "xlink:href"));

	/** The url schemes an attribute value may use; everything else (javascript:, data:, vbscript:) is dropped. */
	private static final Set<String> SAFE_URL_SCHEMES = new HashSet<>(Arrays.asList("http", "https", "mailto", "ftp", "ftps", "tel"));

	/**
	 * Elements that are removed <i>with their content</i>. For all other rejected elements only the
	 * tags are removed and the text between them is kept - which is right for a rejected table cell
	 * but very wrong for a script, whose "text" is code.
	 */
	private static final Set<String> KILL_CONTENT_ELEMENT_NAMES = new HashSet<>(Arrays.asList(HTMLElementName.SCRIPT, HTMLElementName.STYLE, HTMLElementName.IFRAME,
		HTMLElementName.OBJECT, HTMLElementName.EMBED, HTMLElementName.APPLET, HTMLElementName.NOSCRIPT, HTMLElementName.TITLE, HTMLElementName.HEAD,
		HTMLElementName.FRAME, HTMLElementName.FRAMESET, HTMLElementName.BASE, HTMLElementName.LINK, HTMLElementName.META,
		"template", "svg", "math"));

	/**
	 * The killed elements that have no end tag of their own. For the others - script, style and
	 * friends - a missing end tag means everything after them is their content, in a browser too.
	 */
	private static final Set<String> KILL_CONTENT_VOID_ELEMENT_NAMES = new HashSet<>(Arrays.asList(HTMLElementName.BASE, HTMLElementName.LINK, HTMLElementName.META, HTMLElementName.FRAME));

	/** Things inside a style attribute that can load or run something; ordinary formatting has none of them. */
	private static final String[] UNSAFE_STYLE_FRAGMENTS = {"url(", "expression", "javascript", "vbscript", "behavior", "binding", "@import", "\\"};

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
			if(tag.getBegin() < pos)							// Inside an element that was removed with its content
				continue;
			if(stripInvalidElements && isContentKilled(tag)) {
				//-- Remove the element *and everything inside it*: its content is not text to show.
				reencodeTextSegment(source, outputDocument, pos, tag.getBegin(), formatWhiteSpace);
				int end = killedElementEnd(source, tag);
				outputDocument.remove(new Segment(source, tag.getBegin(), end));
				pos = end;
				continue;
			}
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
		if(StartTagType.NORMAL.equals(tag.getTagType())) {
			Element element = tag.getElement();
			if(isParentInCodeTag(tag)) {
				//-- Content inside <code> tag - replace all divs and br's with crlf
				if(HTMLElementName.DIV.equals(elementName)) {
					return false;
				} else if(HTMLElementName.BR.equals(elementName)) {
					outputDocument.replace(tag, "\n");
					return true;
				}
			} else if(HTMLElementName.CODE.equals(elementName)) {
				//-- If this is </code><code> (we're at the start element) remove both.
				Tag prev = tag.getPreviousTag();
				if(prev != null && EndTagType.NORMAL.equals(prev.getTagType()) && HTMLElementName.CODE.equals(prev.getName())) {
					outputDocument.remove(prev);
					outputDocument.replace(tag, "\n");
				}
				return true;
			} else if(HTMLElements.getEndTagRequiredElementNames().contains(elementName)) {
				if(element.getEndTag() == null)
					return false; 										// reject start tag if its required end tag is missing
			} else if(HTMLElements.getEndTagOptionalElementNames().contains(elementName)) {
				if(HTMLElementName.LI.equals(elementName) && !isValidLITag(tag))
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
			if(HTMLElementName.LI.equals(elementName) && !isValidLITag(tag))
				return false;											// reject invalid LI tags
			if(isParentInCodeTag(tag) && HTMLElementName.DIV.equals(elementName)) {
				outputDocument.replace(tag, "\n");
				return false;
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
			if(HTMLElementName.CODE.equals(dad.getName())) {
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
		return HTMLElementName.UL.equals(parentElement.getName()) || HTMLElementName.OL.equals(parentElement.getName()); // only accept LI tags who's immediate parent is UL or OL.
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

	/**
	 * T when this is the start tag of an element that must disappear together with its content.
	 */
	private static boolean isContentKilled(Tag tag) {
		return StartTagType.NORMAL.equals(tag.getTagType()) && KILL_CONTENT_ELEMENT_NAMES.contains(tag.getName());
	}

	/**
	 * Where the killed element ends. With an end tag: after it. Without one: after the start tag for
	 * the elements that never have an end tag, and <b>at the end of the input</b> for the rest -
	 * an unclosed script element makes everything after it script content, in a browser too.
	 */
	private static int killedElementEnd(Source source, Tag tag) {
		Element element = tag.getElement();
		if(null != element && null != element.getEndTag())
			return element.getEnd();
		if(KILL_CONTENT_VOID_ELEMENT_NAMES.contains(tag.getName()))
			return tag.getEnd();
		return source.getEnd();
	}

	/**
	 * Check the value of a url attribute. Anything without a scheme is relative and therefore safe;
	 * a scheme must be one of {@link #SAFE_URL_SCHEMES}. Characters a browser ignores while working
	 * out the scheme (spaces, tabs, newlines, control characters) are removed before looking, so
	 * "java\tscript:..." does not slip through.
	 */
	static boolean isSafeUrl(@Nullable String value) {
		if(null == value)
			return true;									// An attribute without a value carries no scheme
		StringBuilder sb = new StringBuilder(value.length());
		for(int i = 0; i < value.length(); i++) {
			char c = value.charAt(i);
			if(c > ' ' && c != 0x7f)
				sb.append(c);
		}
		String url = sb.toString();
		int colon = url.indexOf(':');
		if(colon < 0)
			return true;									// No scheme at all: a relative url
		//-- A colon after the start of the path, the query or the fragment is not a scheme separator
		int slash = url.indexOf('/');
		int question = url.indexOf('?');
		int hash = url.indexOf('#');
		if((slash >= 0 && slash < colon) || (question >= 0 && question < colon) || (hash >= 0 && hash < colon))
			return true;
		return SAFE_URL_SCHEMES.contains(url.substring(0, colon).toLowerCase());
	}

	/**
	 * Check the value of a style attribute: it may format, but it may not load or run anything.
	 * Css comments and whitespace are removed first, because both can be used to break a keyword up.
	 */
	static boolean isSafeStyle(@Nullable String value) {
		if(null == value)
			return true;
		String style = value.toLowerCase()
			.replaceAll("/\\*.*?\\*/", "")					// css comments
			.replaceAll("\\s+", "");
		for(String bad : UNSAFE_STYLE_FRAGMENTS) {
			if(style.contains(bad))
				return false;
		}
		return true;
	}

	/**
	 * T when this attribute may be rendered: its name must be allowed, and for the attributes whose
	 * value can do something - a url, a style - the value must be allowed too.
	 */
	private static boolean isAcceptedAttribute(Attribute attribute) {
		String name = attribute.getKey();
		if(!VALID_ATTRIBUTE_NAMES.contains(name))
			return false;
		if(URL_ATTRIBUTE_NAMES.contains(name))
			return isSafeUrl(attribute.getValue());
		if("style".equals(name))
			return isSafeStyle(attribute.getValue());
		if("id".equals(name)) {
			String value = attribute.getValue();
			return null == value || !value.startsWith("_");	// DomUI's own node IDs start with _
		}
		return true;
	}

	private static CharSequence getStartTagHTML(StartTag startTag) {
		// tidies and filters out non-approved attributes and non-approved attribute values
		StringBuilder sb = new StringBuilder();
		sb.append('<').append(startTag.getName());
		boolean hasTarget = false;
		for(Attribute attribute : startTag.getAttributes()) {
			if(isAcceptedAttribute(attribute)) {
				if("target".equals(attribute.getKey()))
					hasTarget = true;
				sb.append(' ').append(attribute.getName());
				if(attribute.getValue() != null) {
					sb.append("=\"");
					sb.append(CharacterReference.encode(attribute.getValue()));
					sb.append('"');
				}
			}
		}
		if(hasTarget)
			sb.append(" rel=\"noopener noreferrer\"");		// A target'ed link may not get at the opening window
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
