// Jericho HTML Parser - Java based library for analysing and manipulating HTML
// Version 3.3
// Copyright (C) 2004-2009 Martin Jericho
// http://jericho.htmlparser.net/
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of either one of the following licences:
//
// 1. The Eclipse Public License (EPL) version 1.0,
// included in this distribution in the file licence-epl-1.0.html
// or available at http://www.eclipse.org/legal/epl-v10.html
//
// 2. The GNU Lesser General Public License (LGPL) version 2.1 or later,
// included in this distribution in the file licence-lgpl-2.1.txt
// or available at http://www.gnu.org/licenses/lgpl.txt
//
// This library is distributed on an "AS IS" basis,
// WITHOUT WARRANTY OF ANY KIND, either express or implied.
// See the individual licence texts for more details.

package net.htmlparser.jericho;

import java.util.*;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Performs a simple rendering of HTML markup into text.
 * <p>
 * This provides a human readable version of the segment content that is modelled on the way
 * <a target="_blank" href="http://www.mozilla.com/thunderbird/">Mozilla Thunderbird</a> and other email clients provide an automatic conversion of
 * HTML content to text in their <a target="_blank" href="http://tools.ietf.org/html/rfc2046#section-5.1.4">alternative MIME encoding</a> of emails.
 * <p>
 * The output using default settings complies with the "text/plain; format=flowed" (DelSp=No) protocol described in
 * <a target="_blank" href="http://tools.ietf.org/html/rfc3676">RFC3676</a>.
 * <p>
 * Many properties are available to customise the output, possibly the most significant of which being {@link #setMaxLineLength(int) MaxLineLength}.
 * See the individual property descriptions for details.
 * <p>
 * Use one of the following methods to obtain the output:
 * <ul>
 *  <li>{@link #writeTo(Writer)}</li>
 *  <li>{@link #appendTo(Appendable)}</li>
 *  <li>{@link #toString()}</li>
 *  <li>{@link CharStreamSourceUtil#getReader(CharStreamSource) CharStreamSourceUtil.getReader(this)}</li>
 * </ul>
 * <p>
 * The rendering of some constructs, especially tables, is very rudimentary.
 * No attempt is made to render nested tables properly, except to ensure that all of the text content is included in the output.
 * <p>
 * Rendering an entire {@link Source} object performs a {@linkplain Source#fullSequentialParse() full sequential parse} automatically.
 * <p>
 * Any aspect of the algorithm not specifically mentioned here is subject to change without notice in future versions.
 * <p>
 * To extract pure text without any rendering of the markup, use the {@link TextExtractor} class instead.
 */
public class Renderer implements CharStreamSource {
	private final Segment rootSegment;
	private int maxLineLength=DEFAULT_LINE_LENGTH;
	private int hrLineLength=DEFAULT_LINE_LENGTH-4;
	private String newLine="\r\n";
	private boolean includeHyperlinkURLs=true;
	private boolean includeAlternateText=true;
	private boolean decorateFontStyles=false;
	private boolean convertNonBreakingSpaces=Config.ConvertNonBreakingSpaces;
	private int blockIndentSize=4;
	private int listIndentSize=6;
	private char[] listBullets=new char[] {'*','o','+','#'};
	private boolean includeFirstElementTopMargin=false;
	private String tableCellSeparator=" \t";

	private static final int DEFAULT_LINE_LENGTH=76;
	private static final int UNORDERED_LIST=-1;

	private static Map<String,ElementHandler> ELEMENT_HANDLERS=new HashMap<String,ElementHandler>();
	static {
		ELEMENT_HANDLERS.put(HTMLElementName.A,A_ElementHandler.INSTANCE);
		ELEMENT_HANDLERS.put(HTMLElementName.ADDRESS,StandardBlockElementHandler.INSTANCE_0_0);
		ELEMENT_HANDLERS.put(HTMLElementName.APPLET,AlternateTextElementHandler.INSTANCE);
		ELEMENT_HANDLERS.put(HTMLElementName.B,FontStyleElementHandler.INSTANCE_B);
		ELEMENT_HANDLERS.put(HTMLElementName.BLOCKQUOTE,StandardBlockElementHandler.INSTANCE_1_1_INDENT);
		ELEMENT_HANDLERS.put(HTMLElementName.BR,BR_ElementHandler.INSTANCE);
		ELEMENT_HANDLERS.put(HTMLElementName.BUTTON,RemoveElementHandler.INSTANCE);
		ELEMENT_HANDLERS.put(HTMLElementName.CAPTION,StandardBlockElementHandler.INSTANCE_0_0);
		ELEMENT_HANDLERS.put(HTMLElementName.CENTER,StandardBlockElementHandler.INSTANCE_1_1);
		ELEMENT_HANDLERS.put(HTMLElementName.CODE,FontStyleElementHandler.INSTANCE_CODE);
		ELEMENT_HANDLERS.put(HTMLElementName.DD,StandardBlockElementHandler.INSTANCE_0_0_INDENT);
		ELEMENT_HANDLERS.put(HTMLElementName.DIR,ListElementHandler.INSTANCE_UL);
		ELEMENT_HANDLERS.put(HTMLElementName.DIV,StandardBlockElementHandler.INSTANCE_0_0);
		ELEMENT_HANDLERS.put(HTMLElementName.DT,StandardBlockElementHandler.INSTANCE_0_0);
		ELEMENT_HANDLERS.put(HTMLElementName.EM,FontStyleElementHandler.INSTANCE_I);
		ELEMENT_HANDLERS.put(HTMLElementName.FIELDSET,StandardBlockElementHandler.INSTANCE_1_1);
		ELEMENT_HANDLERS.put(HTMLElementName.FORM,StandardBlockElementHandler.INSTANCE_1_1);
		ELEMENT_HANDLERS.put(HTMLElementName.H1,StandardBlockElementHandler.INSTANCE_2_1);
		ELEMENT_HANDLERS.put(HTMLElementName.H2,StandardBlockElementHandler.INSTANCE_2_1);
		ELEMENT_HANDLERS.put(HTMLElementName.H3,StandardBlockElementHandler.INSTANCE_2_1);
		ELEMENT_HANDLERS.put(HTMLElementName.H4,StandardBlockElementHandler.INSTANCE_2_1);
		ELEMENT_HANDLERS.put(HTMLElementName.H5,StandardBlockElementHandler.INSTANCE_2_1);
		ELEMENT_HANDLERS.put(HTMLElementName.H6,StandardBlockElementHandler.INSTANCE_2_1);
		ELEMENT_HANDLERS.put(HTMLElementName.HEAD,RemoveElementHandler.INSTANCE);
		ELEMENT_HANDLERS.put(HTMLElementName.HR,HR_ElementHandler.INSTANCE);
		ELEMENT_HANDLERS.put(HTMLElementName.I,FontStyleElementHandler.INSTANCE_I);
		ELEMENT_HANDLERS.put(HTMLElementName.IMG,AlternateTextElementHandler.INSTANCE);
		ELEMENT_HANDLERS.put(HTMLElementName.INPUT,AlternateTextElementHandler.INSTANCE);
		ELEMENT_HANDLERS.put(HTMLElementName.LEGEND,StandardBlockElementHandler.INSTANCE_0_0);
		ELEMENT_HANDLERS.put(HTMLElementName.LI,LI_ElementHandler.INSTANCE);
		ELEMENT_HANDLERS.put(HTMLElementName.MENU,ListElementHandler.INSTANCE_UL);
		ELEMENT_HANDLERS.put(HTMLElementName.MAP,RemoveElementHandler.INSTANCE);
		ELEMENT_HANDLERS.put(HTMLElementName.NOFRAMES,RemoveElementHandler.INSTANCE);
		ELEMENT_HANDLERS.put(HTMLElementName.NOSCRIPT,RemoveElementHandler.INSTANCE);
		ELEMENT_HANDLERS.put(HTMLElementName.OL,ListElementHandler.INSTANCE_OL);
		ELEMENT_HANDLERS.put(HTMLElementName.P,StandardBlockElementHandler.INSTANCE_1_1);
		ELEMENT_HANDLERS.put(HTMLElementName.PRE,PRE_ElementHandler.INSTANCE);
		ELEMENT_HANDLERS.put(HTMLElementName.SCRIPT,RemoveElementHandler.INSTANCE);
		ELEMENT_HANDLERS.put(HTMLElementName.SELECT,RemoveElementHandler.INSTANCE);
		ELEMENT_HANDLERS.put(HTMLElementName.STRONG,FontStyleElementHandler.INSTANCE_B);
		ELEMENT_HANDLERS.put(HTMLElementName.STYLE,RemoveElementHandler.INSTANCE);
		ELEMENT_HANDLERS.put(HTMLElementName.TEXTAREA,RemoveElementHandler.INSTANCE);
		ELEMENT_HANDLERS.put(HTMLElementName.TD,TD_ElementHandler.INSTANCE);
		ELEMENT_HANDLERS.put(HTMLElementName.TH,TD_ElementHandler.INSTANCE);
		ELEMENT_HANDLERS.put(HTMLElementName.TR,StandardBlockElementHandler.INSTANCE_0_0);
		ELEMENT_HANDLERS.put(HTMLElementName.U,FontStyleElementHandler.INSTANCE_U);
		ELEMENT_HANDLERS.put(HTMLElementName.UL,ListElementHandler.INSTANCE_UL);
	}	

	/**
	 * Constructs a new <code>Renderer</code> based on the specified {@link Segment}.
	 * @param segment  the segment containing the HTML to be rendered.
	 * @see Segment#getRenderer()
	 */
	public Renderer(final Segment segment) {
		rootSegment=segment;
	}

	// Documentation inherited from CharStreamSource
	public void writeTo(final Writer writer) throws IOException {
		appendTo(writer);
		writer.flush();
	}

	// Documentation inherited from CharStreamSource
	public void appendTo(final Appendable appendable) throws IOException {
		new Processor(this,rootSegment,getMaxLineLength(),getHRLineLength(),getNewLine(),getIncludeHyperlinkURLs(),getIncludeAlternateText(),getDecorateFontStyles(),getConvertNonBreakingSpaces(),getBlockIndentSize(),getListIndentSize(),getListBullets(),getTableCellSeparator()).appendTo(appendable);
	}
	
	// Documentation inherited from CharStreamSource
	public long getEstimatedMaximumOutputLength() {
		return rootSegment.length();
	}

	// Documentation inherited from CharStreamSource
	public String toString() {
		return CharStreamSourceUtil.toString(this);
	}

	/**
	 * Sets the column at which lines are to be wrapped.
	 * <p>
	 * Lines that would otherwise exceed this length are wrapped onto a new line at a word boundary.
	 * <p>
	 * Setting this property automatically sets the {@link #setHRLineLength(int) HRLineLength} property to <code>MaxLineLength - 4</code>.
	 * <p>
	 * Setting this property to zero disables line wrapping completely, and leaves the value of {@link #setHRLineLength(int) HRLineLength} unchanged.
	 * <p>
	 * A Line may still exceed this length if it consists of a single word, where the length of the word plus the line indent exceeds the maximum length.
	 * In this case the line is wrapped immediately after the end of the word.
	 * <p>
	 * The default value is <code>76</code>, which reflects the maximum line length for sending
	 * email data specified in <a target="_blank" href="http://rfc.net/rfc2049.html#s3.">RFC2049 section 3.5</a>.
	 * 
	 * @param maxLineLength  the column at which lines are to be wrapped.
	 * @return this <code>Renderer</code> instance, allowing multiple property setting methods to be chained in a single statement. 
	 * @see #getMaxLineLength()
	 */
	public Renderer setMaxLineLength(final int maxLineLength) {
		this.maxLineLength=maxLineLength;
		if (maxLineLength>0) hrLineLength=Math.max(2,maxLineLength-4);
		return this;
	}

	/**
	 * Returns the column at which lines are to be wrapped.
	 * <p>
	 * See the {@link #setMaxLineLength(int)} method for a full description of this property.
	 *
	 * @return the column at which lines are to be wrapped, or zero if line wrapping is disabled.
	 */	
	public int getMaxLineLength() {
		return maxLineLength;
	}

	/**
	 * Sets the length of a horizontal line.
	 * <p>
	 * The length determines the number of hyphen characters used to render {@link HTMLElementName#HR HR} elements.
	 * <p>
	 * This property is set automatically to <code>MaxLineLength - 4</code> when the {@link #setMaxLineLength(int) MaxLineLength} property is set.
	 * The default value is <code>72</code>.
	 *
	 * @param hrLineLength  the length of a horizontal line.
	 * @return this <code>Renderer</code> instance, allowing multiple property setting methods to be chained in a single statement. 
	 * @see #getHRLineLength()
	 */
	public Renderer setHRLineLength(final int hrLineLength) {
		this.hrLineLength=hrLineLength;
		return this;
	}

	/**
	 * Returns the length of a horizontal line.
	 * <p>
	 * See the {@link #setHRLineLength(int)} method for a full description of this property.
	 *
	 * @return the length of a horizontal line.
	 */	
	public int getHRLineLength() {
		return hrLineLength;
	}

	/**
	 * Sets the string to be used to represent a <a target="_blank" href="http://en.wikipedia.org/wiki/Newline">newline</a> in the output.
	 * <p>
	 * The default value is <code>"\r\n"</code> <span title="carriage return + line feed">(CR+LF)</span> regardless of the platform on which the library is running.
	 * This is so that the default configuration produces valid 
	 * <a target="_blank" href="http://tools.ietf.org/html/rfc1521#section-7.1.2">MIME plain/text</a> output, which mandates the use of CR+LF for line breaks.
	 * <p>
	 * Specifying a <code>null</code> argument causes the output to use same new line string as is used in the source document, which is
	 * determined via the {@link Source#getNewLine()} method.
	 * If the source document does not contain any new lines, a "best guess" is made by either taking the new line string of a previously parsed document,
	 * or using the value from the static {@link Config#NewLine} property.
	 * 
	 * @param newLine  the string to be used to represent a <a target="_blank" href="http://en.wikipedia.org/wiki/Newline">newline</a> in the output, may be <code>null</code>.
	 * @return this <code>Renderer</code> instance, allowing multiple property setting methods to be chained in a single statement. 
	 * @see #getNewLine()
	 */
	public Renderer setNewLine(final String newLine) {
		this.newLine=newLine;
		return this;
	}

	/**
	 * Returns the string to be used to represent a <a target="_blank" href="http://en.wikipedia.org/wiki/Newline">newline</a> in the output.
	 * <p>
	 * See the {@link #setNewLine(String)} method for a full description of this property.
	 *
	 * @return the string to be used to represent a <a target="_blank" href="http://en.wikipedia.org/wiki/Newline">newline</a> in the output.
	 */
	public String getNewLine() {
		if (newLine==null) newLine=rootSegment.source.getBestGuessNewLine();
		return newLine;
	}

	/**
	 * Sets whether hyperlink URLs are included in the output.
	 * <p>
	 * The default value is <code>true</code>.
	 * <p>
	 * When this property is <code>true</code>, the URL of each hyperlink is included in the output as determined by the implementation of the
	 * {@link #renderHyperlinkURL(StartTag)} method.
	 * <p>
	 * <dl>
	 *  <dt>Example:</dt>
	 *  <dd>
	 *   <p>
	 *   Assuming the default implementation of {@link #renderHyperlinkURL(StartTag)}, when this property is <code>true</code>, the following HTML:
	 *   <blockquote class="code">
	 *    <code>&lt;a href="http://jericho.htmlparser.net/"&gt;Jericho HTML Parser&lt;/a&gt;</code>
	 *   </blockquote>
	 *   produces the following output:
	 *   <blockquote class="code">
	 *    <code>Jericho HTML Parser &lt;http://jericho.htmlparser.net/&gt;</code>
	 *   </blockquote>
	 *  </dd>
	 * </dl>
	 *
	 * @param includeHyperlinkURLs  specifies whether hyperlink URLs are included in the output.
	 * @return this <code>Renderer</code> instance, allowing multiple property setting methods to be chained in a single statement. 
	 * @see #getIncludeHyperlinkURLs()
	 */
	public Renderer setIncludeHyperlinkURLs(final boolean includeHyperlinkURLs) {
		this.includeHyperlinkURLs=includeHyperlinkURLs;
		return this;
	}

	/**
	 * Indicates whether hyperlink URLs are included in the output.
	 * <p>
	 * See the {@link #setIncludeHyperlinkURLs(boolean)} method for a full description of this property.
	 *
	 * @return <code>true</code> if hyperlink URLs are included in the output, otherwise <code>false</code>.
	 */
	public boolean getIncludeHyperlinkURLs() {
		return includeHyperlinkURLs;
	}

	/**
	 * Renders the hyperlink URL from the specified {@link StartTag}.
	 * <p>
	 * A return value of <code>null</code> indicates that the hyperlink URL should not be rendered at all.
	 * <p>
	 * The default implementation of this method returns <code>null</code> if the <code>href</code> attribute of the specified start tag
	 * starts with "<code>javascript:</code>", is a relative or invalid URI, or is missing completely.
	 * In all other cases it returns the value of the <code>href</code> attribute enclosed in angle brackets.
	 * <p>
	 * See the documentation of the {@link #setIncludeHyperlinkURLs(boolean)} method for an example of how a hyperlink is rendered by the default implementation.
	 * <p>
	 * This method can be overridden in a subclass to customise the rendering of hyperlink URLs.
	 * <p>
	 * Rendering of hyperlink URLs can be disabled completely without overriding this method by setting the
	 * {@link #setIncludeHyperlinkURLs(boolean) IncludeHyperlinkURLs} property to <code>false</code>.
	 * <p>
	 * <dl>
	 *  <dt>Example:</dt>
	 *  <dd>
	 *   To render hyperlink URLs without the enclosing angle brackets:<br /><br />
	 *   <code>
	 *    Renderer renderer=new Renderer(segment) {<br />
	 *    &nbsp; &nbsp; public String renderHyperlinkURL(StartTag startTag) {<br />
	 *    &nbsp; &nbsp; &nbsp; &nbsp; String href=startTag.getAttributeValue("href");<br />
	 *    &nbsp; &nbsp; &nbsp; &nbsp; if (href==null || href.startsWith("javascript:")) return null;<br />
	 *    &nbsp; &nbsp; &nbsp; &nbsp; try {<br />
	 *    &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; URI uri=new URI(href);<br />
	 *    &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; if (!uri.isAbsolute()) return null;<br />
	 *    &nbsp; &nbsp; &nbsp; &nbsp; } catch (URISyntaxException ex) {<br />
	 *    &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; return null;<br />
	 *    &nbsp; &nbsp; &nbsp; &nbsp; }<br />
	 *    &nbsp; &nbsp; &nbsp; &nbsp; return href;<br />
	 *    &nbsp; &nbsp; }<br />
	 *    };<br />
	 *    String renderedSegment=renderer.toString();
	 *   </code>
	 *  </dd>
	 * </dl>
	 * @param startTag  the start tag of the hyperlink element, must not be <code>null</code>.
	 * @return The rendered hyperlink URL from the specified {@link StartTag}, or <code>null</code> if the hyperlink URL should not be rendered.
	 */
	public String renderHyperlinkURL(final StartTag startTag) {
		final String href=startTag.getAttributeValue("href");
		if (href==null || href.startsWith("javascript:")) return null;
		try {
			URI uri=new URI(href);
			if (!uri.isAbsolute()) return null;
		} catch (URISyntaxException ex) {
			return null;
		}
		return '<'+href+'>';
	}

	/**
	 * Sets whether the alternate text of a tag that has an <code>alt</code> attribute is included in the output.
	 * <p>
	 * The default value is <code>true</code>.
	 * Note that this is not conistent with common email clients such as Mozilla Thunderbird which do not render alternate text at all,
	 * even when a tag specifies alternate text.
	 * <p>
	 * When this property is <code>true</code>, the alternate text is included in the output as determined by the implementation of the
	 * {@link #renderAlternateText(StartTag)} method.
	 * <p>
	 * <dl>
	 *  <dt>Example:</dt>
	 *  <dd>
	 *   <p>
	 *   Assuming the default implementation of {@link #renderAlternateText(StartTag)}, when this property is <code>true</code>, the following HTML:
	 *   <blockquote class="code">
	 *    <code>&lt;img src="smiley.png" alt="smiley face" /&gt;</code>
	 *   </blockquote>
	 *   produces the following output:
	 *   <blockquote class="code">
	 *    <code>[smiley face]</code>
	 *   </blockquote>
	 *  </dd>
	 * </dl>
	 *
	 * @param includeAlternateText  specifies whether the alternate text of a tag that has an <code>alt</code> attribute is included in the output.
	 * @return this <code>Renderer</code> instance, allowing multiple property setting methods to be chained in a single statement. 
	 * @see #getIncludeAlternateText()
	 */
	public Renderer setIncludeAlternateText(final boolean includeAlternateText) {
		this.includeAlternateText=includeAlternateText;
		return this;
	}

	/**
	 * Indicates whether the alternate text of a tag that has an <code>alt</code> attribute is included in the output.
	 * <p>
	 * See the {@link #setIncludeAlternateText(boolean)} method for a full description of this property.
	 *
	 * @return <code>true</code> if the alternate text of a tag that has an <code>alt</code> attribute is included in the output, otherwise <code>false</code>.
	 */
	public boolean getIncludeAlternateText() {
		return includeAlternateText;
	}

	/**
	 * Renders the alternate text of the specified start tag.
	 * <p>
	 * A return value of <code>null</code> indicates that the alternate text is not to be rendered at all.
	 * <p>
	 * The default implementation of this method returns <code>null</code> if the <code>alt</code> attribute of the specified start tag is missing or empty, or if the
	 * specified start tag is from an {@link HTMLElementName#AREA AREA} element.
	 * In all other cases it returns the value of the <code>alt</code> attribute enclosed in square brackets <code>[&hellip;]</code>.
	 * <p>
	 * See the documentation of the {@link #setIncludeAlternateText(boolean)} method for an example of how alternate text is rendered by the default implementation.
	 * <p>
	 * This method can be overridden in a subclass to customise the rendering of alternate text.
	 * <p>
	 * Rendering of alternate text can be disabled completely without overriding this method by setting the
	 * {@link #setIncludeAlternateText(boolean) IncludeAlternateText} property to <code>false</code>.
	 * <p>
	 * <dl>
	 *  <dt>Example:</dt>
	 *  <dd>
	 *   To render alternate text with double angle quotation marks instead of square brackets:<br /><br />
	 *   <code>
	 *    Renderer renderer=new Renderer(segment) {<br />
	 *    &nbsp; &nbsp; public String renderAlternateText(StartTag startTag) {<br />
	 *    &nbsp; &nbsp; &nbsp; &nbsp; if (startTag.getName()==HTMLElementName.AREA) return null;
	 *    &nbsp; &nbsp; &nbsp; &nbsp; String alt=startTag.getAttributeValue("alt");<br />
	 *    &nbsp; &nbsp; &nbsp; &nbsp; if (alt==null || alt.length()==0) return null;<br />
	 *    &nbsp; &nbsp; &nbsp; &nbsp; return '«'+alt+'»';<br />
	 *    &nbsp; &nbsp; }<br />
	 *    };<br />
	 *    String renderedSegment=renderer.toString();
	 *   </code>
	 *  </dd>
	 * </dl>
	 * @param startTag  the start tag containing an <code>alt</code> attribute, must not be <code>null</code>.
	 * @return The rendered alternate text, or <code>null</code> if the alternate text should not be rendered.
	 */
	public String renderAlternateText(final StartTag startTag) {
		if (startTag.getName()==HTMLElementName.AREA) return null;
		final String alt=startTag.getAttributeValue("alt");
		if (alt==null || alt.length()==0) return null;
		return '['+alt+']';
	}

	/**
	 * Sets whether decoration characters are to be included around the content of some
	 * <a target="_blank" href="http://www.w3.org/TR/html401/present/graphics.html#h-15.2.1">font style elements</a> and
	 * <a target="_blank" href="http://www.w3.org/TR/html401/struct/text.html#h-9.2.1">phrase elements</a>.
	 * <p>
	 * The default value is <code>false</code>.
	 * <p>
	 * Below is a table summarising the decorated elements.
	 * <p>
	 * <style type="text/css">
	 *  table#FontStyleElementSummary td, table#FontStyleElementSummary th {text-align: center; padding-bottom: 2px}
	 * </style>
	 * <table id="FontStyleElementSummary" class="bordered" cellspacing="0">
	 *  <tr><th title="HTML elements decorated">Elements</th><th title="The character placed around the element content">Character</th><th>Example Output</th></tr>
	 *  <tr><td>{@link HTMLElementName#B B} and {@link HTMLElementName#STRONG STRONG}</td><td><code>*</code></td><td><code>*bold text*</code></td></tr>
	 *  <tr><td>{@link HTMLElementName#I I} and {@link HTMLElementName#EM EM}</td><td><code>/</code></td><td><code>/italic text/</code></td></tr>
	 *  <tr><td>{@link HTMLElementName#U U}</td><td><code>_</code></td><td><code>_underlined text_</code></td></tr>
	 *  <tr><td>{@link HTMLElementName#CODE CODE}</td><td><code>|</code></td><td><code>|code|</code></td></tr>
	 * </table>
	 *
	 * @param decorateFontStyles  specifies whether decoration characters are to be included around the content of some font style elements.
	 * @return this <code>Renderer</code> instance, allowing multiple property setting methods to be chained in a single statement. 
	 * @see #getDecorateFontStyles()
	 */
	public Renderer setDecorateFontStyles(final boolean decorateFontStyles) {
		this.decorateFontStyles=decorateFontStyles;
		return this;
	}

	/**
	 * Indicates whether decoration characters are to be included around the content of some
	 * <a target="_blank" href="http://www.w3.org/TR/html401/present/graphics.html#h-15.2.1">font style elements</a> and
	 * <a target="_blank" href="http://www.w3.org/TR/html401/struct/text.html#h-9.2.1">phrase elements</a>.
	 * <p>
	 * See the {@link #setDecorateFontStyles(boolean)} method for a full description of this property.
	 *
	 * @return <code>true</code> if decoration characters are to be included around the content of some font style elements, otherwise <code>false</code>.
	 */
	public boolean getDecorateFontStyles() {
		return decorateFontStyles;
	}

	/**
	 * Sets whether non-breaking space ({@link CharacterEntityReference#_nbsp &amp;nbsp;}) character entity references are converted to spaces.
	 * <p>
	 * The default value is that of the static {@link Config#ConvertNonBreakingSpaces} property at the time the <code>Renderer</code> is instantiated.
	 *
	 * @param convertNonBreakingSpaces  specifies whether non-breaking space ({@link CharacterEntityReference#_nbsp &amp;nbsp;}) character entity references are converted to spaces.
	 * @return this <code>Renderer</code> instance, allowing multiple property setting methods to be chained in a single statement. 
	 * @see #getConvertNonBreakingSpaces()
	 */
	public Renderer setConvertNonBreakingSpaces(boolean convertNonBreakingSpaces) {
		this.convertNonBreakingSpaces=convertNonBreakingSpaces;
		return this;
	}

	/**
	 * Indicates whether non-breaking space ({@link CharacterEntityReference#_nbsp &amp;nbsp;}) character entity references are converted to spaces.
	 * <p>
	 * See the {@link #setConvertNonBreakingSpaces(boolean)} method for a full description of this property.
	 * 
	 * @return <code>true</code> if non-breaking space ({@link CharacterEntityReference#_nbsp &amp;nbsp;}) character entity references are converted to spaces, otherwise <code>false</code>.
	 */
	public boolean getConvertNonBreakingSpaces() {
		return convertNonBreakingSpaces;
	}

	/**
	 * Sets the size of the indent to be used for anything other than {@link HTMLElementName#LI LI} elements.
	 * <p>
	 * At present this applies to {@link HTMLElementName#BLOCKQUOTE BLOCKQUOTE} and {@link HTMLElementName#DD DD} elements.
	 * <p>
	 * The default value is <code>4</code>.
	 * 
	 * @param blockIndentSize  the size of the indent.
	 * @return this <code>Renderer</code> instance, allowing multiple property setting methods to be chained in a single statement. 
	 * @see #getBlockIndentSize()
	 */
	public Renderer setBlockIndentSize(final int blockIndentSize) {
		this.blockIndentSize=blockIndentSize;
		return this;
	}

	/**
	 * Returns the size of the indent to be used for anything other than {@link HTMLElementName#LI LI} elements.
	 * <p>
	 * See the {@link #setBlockIndentSize(int)} method for a full description of this property.
	 *
	 * @return the size of the indent to be used for anything other than {@link HTMLElementName#LI LI} elements.
	 */
	public int getBlockIndentSize() {
		return blockIndentSize;
	}

	/**
	 * Sets the size of the indent to be used for {@link HTMLElementName#LI LI} elements.
	 * <p>
	 * The default value is <code>6</code>.
	 * <p>
	 * This applies to {@link HTMLElementName#LI LI} elements inside both {@link HTMLElementName#UL UL} and {@link HTMLElementName#OL OL} elements.
	 * <p>
	 * The bullet or number of the list item is included as part of the indent.
	 * 
	 * @param listIndentSize  the size of the indent.
	 * @return this <code>Renderer</code> instance, allowing multiple property setting methods to be chained in a single statement. 
	 * @see #getListIndentSize()
	 */
	public Renderer setListIndentSize(final int listIndentSize) {
		this.listIndentSize=listIndentSize;
		return this;
	}

	/**
	 * Returns the size of the indent to be used for {@link HTMLElementName#LI LI} elements.
	 * <p>
	 * See the {@link #setListIndentSize(int)} method for a full description of this property.
	 *
	 * @return the size of the indent to be used for {@link HTMLElementName#LI LI} elements.
	 */
	public int getListIndentSize() {
		return listIndentSize;
	}

	/**
	 * Sets the bullet characters to use for list items inside {@link HTMLElementName#UL UL} elements.
	 * <p>
	 * The values in the default array are <code>*</code>, <code>o</code>, <code>+</code> and <code>#</code>.
	 * <p>
	 * If the nesting of rendered lists goes deeper than the length of this array, the bullet characters start repeating from the first in the array.
	 * <p>
	 * WARNING: If any of the characters in the default array are modified, this will affect all other instances of this class using the default array.
	 * 
	 * @param listBullets  an array of characters to be used as bullets, must have at least one entry.
	 * @return this <code>Renderer</code> instance, allowing multiple property setting methods to be chained in a single statement. 
	 * @see #getListBullets()
	 */
	public Renderer setListBullets(final char[] listBullets) {
		if (listBullets==null || listBullets.length==0) throw new IllegalArgumentException("listBullets argument must be an array of at least one character");
		this.listBullets=listBullets;
		return this;
	}

	/**
	 * Returns the bullet characters to use for list items inside {@link HTMLElementName#UL UL} elements.
	 * <p>
	 * See the {@link #setListBullets(char[])} method for a full description of this property.
	 *
	 * @return the bullet characters to use for list items inside {@link HTMLElementName#UL UL} elements.
	 */
	public char[] getListBullets() {
		return listBullets;
	}

	/**
	 * Sets whether the top margin of the first element is rendered.
	 * <p>
	 * The default value is <code>false</code>.
	 * <p>
	 * If this property is set to <code>true</code>, then the source "<code>&lt;h1&gt;Heading&lt;/h1&gt;</code>" would be rendered as "<code>\r\n\r\nHeading</code>",
	 * assuming all other default settings.
	 * If this property is <code>false</code>, then the same source would be rendered as "<code>Heading</code>".
	 * <p>
	 * Note that the bottom margin of the last element is never rendered.
	 *
	 * @param includeFirstElementTopMargin  specifies whether the top margin of the first element is rendered.
	 * @return this <code>Renderer</code> instance, allowing multiple property setting methods to be chained in a single statement. 
	 * @see #getIncludeFirstElementTopMargin()
	 */
	public Renderer setIncludeFirstElementTopMargin(final boolean includeFirstElementTopMargin) {
		this.includeFirstElementTopMargin=includeFirstElementTopMargin;
		return this;
	}

	/**
	 * Indicates whether the top margin of the first element is rendered.
	 * <p>
	 * See the {@link #setIncludeFirstElementTopMargin(boolean)} method for a full description of this property.
	 * 
	 * @return <code>true</code> if the top margin of the first element is rendered, otherwise <code>false</code>.
	 */
	public boolean getIncludeFirstElementTopMargin() {
		return includeFirstElementTopMargin;
	}

	/**
	 * Sets the string that is to separate table cells.
	 * <p>
	 * The default value is <code>" \t"</code> (a space followed by a tab).
	 * 
	 * @param tableCellSeparator  the string that is to separate table cells.
	 * @return this <code>Renderer</code> instance, allowing multiple property setting methods to be chained in a single statement. 
	 * @see #getTableCellSeparator()
	 */
	public Renderer setTableCellSeparator(final String tableCellSeparator) {
		this.tableCellSeparator=tableCellSeparator;
		return this;
	}

	/**
	 * Returns the string that is to separate table cells.
	 * <p>
	 * See the {@link #setTableCellSeparator(String)} method for a full description of this property.
	 *
	 * @return the string that is to separate table cells.
	 */
	public String getTableCellSeparator() {
		return tableCellSeparator;
	}

	/**
	 * Sets the default top margin of an HTML block element with the specified name.
	 * <p>
	 * The top margin is the number of blank lines that are to be inserted above the rendered block.
	 * <p>
	 * As this is a static method, the setting affects all instances of the <code>Renderer</code> class.
	 * <p>
	 * The <code>htmlElementName</code> argument must be one of the following:<br />
	 * {@link HTMLElementName#ADDRESS ADDRESS},
	 * {@link HTMLElementName#BLOCKQUOTE BLOCKQUOTE},
	 * {@link HTMLElementName#CAPTION CAPTION},
	 * {@link HTMLElementName#CENTER CENTER},
	 * {@link HTMLElementName#DD DD},
	 * {@link HTMLElementName#DIR DIR},
	 * {@link HTMLElementName#DIV DIV},
	 * {@link HTMLElementName#DT DT},
	 * {@link HTMLElementName#FIELDSET FIELDSET},
	 * {@link HTMLElementName#FORM FORM},
	 * {@link HTMLElementName#H1 H1},
	 * {@link HTMLElementName#H2 H2},
	 * {@link HTMLElementName#H3 H3},
	 * {@link HTMLElementName#H4 H4},
	 * {@link HTMLElementName#H5 H5},
	 * {@link HTMLElementName#H6 H6},
	 * {@link HTMLElementName#HR HR},
	 * {@link HTMLElementName#LEGEND LEGEND},
	 * {@link HTMLElementName#LI LI},
	 * {@link HTMLElementName#MENU MENU},
	 * {@link HTMLElementName#OL OL},
	 * {@link HTMLElementName#P P},
	 * {@link HTMLElementName#PRE PRE},
	 * {@link HTMLElementName#TR TR},
	 * {@link HTMLElementName#UL UL}
	 *
	 * @param htmlElementName  (required) the case insensitive name of a supported HTML block element.
	 * @param topMargin  the new top margin of the specified element.
	 * @throws UnsupportedOperationException if an unsupported element name is specified.
	 */
	public static void setDefaultTopMargin(String htmlElementName, final int topMargin) {
		htmlElementName=HTMLElements.getConstantElementName(htmlElementName.toLowerCase());
		ELEMENT_HANDLERS.put(htmlElementName,getAbstractBlockElementHandler(htmlElementName).newTopMargin(topMargin));
	}

	/**
	 * Returns the default top margin of an HTML block element with the specified name.
	 * <p>
	 * See the {@link #setDefaultTopMargin(String htmlElementName, int topMargin)} method for a full description of this property.
	 * 
	 * @param htmlElementName  (required) the case insensitive name of a supported HTML block element.
	 * @return the default top margin of an HTML block element with the specified name.
	 * @throws UnsupportedOperationException if an unsupported element name is specified.
	 */
	public static int getDefaultTopMargin(final String htmlElementName) {
		return getAbstractBlockElementHandler(htmlElementName.toLowerCase()).getTopMargin();
	}

	/**
	 * Sets the default bottom margin of an HTML block element with the specified name.
	 * <p>
	 * The bottom margin is the number of blank lines that are to be inserted below the rendered block.
	 * <p>
	 * As this is a static method, the setting affects all instances of the <code>Renderer</code> class.
	 * <p>
	 * The <code>htmlElementName</code> argument must be one of the following:<br />
	 * {@link HTMLElementName#ADDRESS ADDRESS},
	 * {@link HTMLElementName#BLOCKQUOTE BLOCKQUOTE},
	 * {@link HTMLElementName#CAPTION CAPTION},
	 * {@link HTMLElementName#CENTER CENTER},
	 * {@link HTMLElementName#DD DD},
	 * {@link HTMLElementName#DIR DIR},
	 * {@link HTMLElementName#DIV DIV},
	 * {@link HTMLElementName#DT DT},
	 * {@link HTMLElementName#FIELDSET FIELDSET},
	 * {@link HTMLElementName#FORM FORM},
	 * {@link HTMLElementName#H1 H1},
	 * {@link HTMLElementName#H2 H2},
	 * {@link HTMLElementName#H3 H3},
	 * {@link HTMLElementName#H4 H4},
	 * {@link HTMLElementName#H5 H5},
	 * {@link HTMLElementName#H6 H6},
	 * {@link HTMLElementName#HR HR},
	 * {@link HTMLElementName#LEGEND LEGEND},
	 * {@link HTMLElementName#LI LI},
	 * {@link HTMLElementName#MENU MENU},
	 * {@link HTMLElementName#OL OL},
	 * {@link HTMLElementName#P P},
	 * {@link HTMLElementName#PRE PRE},
	 * {@link HTMLElementName#TR TR},
	 * {@link HTMLElementName#UL UL}
	 *
	 * @param htmlElementName  (required) the case insensitive name of a supported HTML block element.
	 * @param bottomMargin  the new bottom margin of the specified element.
	 * @throws UnsupportedOperationException if an unsupported element name is specified.
	 */
	public static void setDefaultBottomMargin(String htmlElementName, final int bottomMargin) {
		htmlElementName=HTMLElements.getConstantElementName(htmlElementName.toLowerCase());
		ELEMENT_HANDLERS.put(htmlElementName,getAbstractBlockElementHandler(htmlElementName).newBottomMargin(bottomMargin));
	}

	/**
	 * Returns the default bottom margin of an HTML block element with the specified name.
	 * <p>
	 * See the {@link #setDefaultBottomMargin(String htmlElementName, int bottomMargin)} method for a full description of this property.
	 * 
	 * @param htmlElementName  (required) the case insensitive name of a supported HTML block element.
	 * @return the default bottom margin of an HTML block element with the specified name.
	 * @throws UnsupportedOperationException if an unsupported element name is specified.
	 */
	public static int getDefaultBottomMargin(final String htmlElementName) {
		return getAbstractBlockElementHandler(htmlElementName.toLowerCase()).getBottomMargin();
	}

	/**
	 * Sets the default value of whether an HTML block element of the specified name is indented.
	 * <p>
	 * As this is a static method, the setting affects all instances of the <code>Renderer</code> class.
	 * <p>
	 * The <code>htmlElementName</code> argument must be one of the following:<br />
	 * {@link HTMLElementName#ADDRESS ADDRESS},
	 * {@link HTMLElementName#BLOCKQUOTE BLOCKQUOTE},
	 * {@link HTMLElementName#CAPTION CAPTION},
	 * {@link HTMLElementName#CENTER CENTER},
	 * {@link HTMLElementName#DD DD},
	 * {@link HTMLElementName#DIR DIR},
	 * {@link HTMLElementName#DIV DIV},
	 * {@link HTMLElementName#DT DT},
	 * {@link HTMLElementName#FIELDSET FIELDSET},
	 * {@link HTMLElementName#FORM FORM},
	 * {@link HTMLElementName#H1 H1},
	 * {@link HTMLElementName#H2 H2},
	 * {@link HTMLElementName#H3 H3},
	 * {@link HTMLElementName#H4 H4},
	 * {@link HTMLElementName#H5 H5},
	 * {@link HTMLElementName#H6 H6},
	 * {@link HTMLElementName#HR HR},
	 * {@link HTMLElementName#LEGEND LEGEND},
	 * {@link HTMLElementName#MENU MENU},
	 * {@link HTMLElementName#OL OL},
	 * {@link HTMLElementName#P P},
	 * {@link HTMLElementName#PRE PRE},
	 * {@link HTMLElementName#TR TR},
	 * {@link HTMLElementName#UL UL}
	 *
	 * @param htmlElementName  (required) the case insensitive name of a supported HTML block element.
	 * @param indent  whether the the specified element is indented.
	 * @throws UnsupportedOperationException if an unsupported element name is specified.
	 */
	public static void setDefaultIndent(String htmlElementName, final boolean indent) {
		htmlElementName=HTMLElements.getConstantElementName(htmlElementName.toLowerCase());
		if (htmlElementName==HTMLElementName.LI) throw new UnsupportedOperationException();
		ELEMENT_HANDLERS.put(htmlElementName,getAbstractBlockElementHandler(htmlElementName).newIndent(indent));
	}

	/**
	 * Returns the default value of whether an HTML block element of the specified name is indented.
	 * <p>
	 * See the {@link #setDefaultIndent(String htmlElementName, boolean indent)} method for a full description of this property.
	 * 
	 * @param htmlElementName  (required) the case insensitive name of a supported HTML block element.
	 * @return the default value of whether an HTML block element of the specified name is indented.
	 * @throws UnsupportedOperationException if an unsupported element name is specified.
	 */
	public static boolean isDefaultIndent(String htmlElementName) {
		htmlElementName=HTMLElements.getConstantElementName(htmlElementName.toLowerCase());
		if (htmlElementName==HTMLElementName.LI) throw new UnsupportedOperationException();
		return getAbstractBlockElementHandler(htmlElementName.toLowerCase()).isIndent();
	}

	private static AbstractBlockElementHandler getAbstractBlockElementHandler(String htmlElementName) {
		ElementHandler elementHandler=ELEMENT_HANDLERS.get(htmlElementName);
		if (elementHandler==null || !(elementHandler instanceof AbstractBlockElementHandler)) throw new UnsupportedOperationException("Cannot set block properties on element "+htmlElementName);
		return (AbstractBlockElementHandler)elementHandler;
	}
	
	/** This class does the actual work, but is first passed final copies of all the parameters for efficiency. */
	private static final class Processor {
		private final Renderer renderer;
		private final Segment rootSegment;
		private final Source source;
		private final int maxLineLength;
		private final int hrLineLength;
		private final String newLine;
		private final boolean includeHyperlinkURLs;
		private final boolean includeAlternateText;
		private final boolean decorateFontStyles;
		private final boolean convertNonBreakingSpaces;
		private final int blockIndentSize;
		private final int listIndentSize;
		private final char[] listBullets;
		private final String tableCellSeparator;
	
		private Appendable appendable;
		private int renderedIndex; // keeps track of where rendering is up to in case of overlapping elements
		private boolean atStartOfLine;
		private boolean skipInitialNewLines;
		private int col;
		private int listIndentLevel;
		private int indentSize;
		private int blockVerticalMargin; // minimum number of blank lines to output at the current block boundary, or NO_MARGIN (-1) if we are not currently at a block boundary.
		private boolean preformatted;
		private boolean lastCharWhiteSpace;
		private final boolean ignoreInitialWhiteSpace=false; // can remove this at some stage once we're sure it won't be used.
		private boolean bullet;
		private int listBulletNumber;
	
		private static final int NO_MARGIN=-1;
	
		public Processor(final Renderer renderer, final Segment rootSegment, final int maxLineLength, final int hrLineLength, final String newLine, final boolean includeHyperlinkURLs, final boolean includeAlternateText, final boolean decorateFontStyles, final boolean convertNonBreakingSpaces, final int blockIndentSize, final int listIndentSize, final char[] listBullets, final String tableCellSeparator) {
			this.renderer=renderer;
			this.rootSegment=rootSegment;
			source=rootSegment.source;
			this.maxLineLength=maxLineLength;
			this.hrLineLength=hrLineLength;
			this.newLine=newLine;
			this.includeHyperlinkURLs=includeHyperlinkURLs;
			this.includeAlternateText=includeAlternateText;
			this.decorateFontStyles=decorateFontStyles;
			this.convertNonBreakingSpaces=convertNonBreakingSpaces;
			this.blockIndentSize=blockIndentSize;
			this.listIndentSize=listIndentSize;
			this.listBullets=listBullets;
			this.tableCellSeparator=tableCellSeparator;
		}
	
		public void appendTo(final Appendable appendable) throws IOException {
			reset();
			this.appendable=appendable;
			List<Element> elements=rootSegment instanceof Element ? Collections.singletonList((Element)rootSegment) : rootSegment.getChildElements();
			appendSegmentProcessingChildElements(rootSegment.begin,rootSegment.end,elements);
		}
	
		private void reset() {
			renderedIndex=0;
			atStartOfLine=true;
			skipInitialNewLines=!renderer.includeFirstElementTopMargin;
			col=0;
			listIndentLevel=0;
			indentSize=0;
			blockVerticalMargin=NO_MARGIN;
			preformatted=false;
			lastCharWhiteSpace=false;
			//ignoreInitialWhiteSpace=false;
			bullet=false;
		}
	
		private void appendElementContent(final Element element) throws IOException {
			final int contentEnd=element.getContentEnd();
			if (element.isEmpty() || renderedIndex>=contentEnd) return;
			final int contentBegin=element.getStartTag().end;
			appendSegmentProcessingChildElements(Math.max(renderedIndex,contentBegin),contentEnd,element.getChildElements());
		}
	
		private void appendSegmentProcessingChildElements(final int begin, final int end, final List<Element> childElements) throws IOException {
			int index=begin;
			for (Element childElement : childElements) {
				if (index>=childElement.end) continue;
				if (index<childElement.begin) appendSegmentRemovingTags(index,childElement.begin);
				getElementHandler(childElement).process(this,childElement);
				index=Math.max(renderedIndex,childElement.end);
			}
			if (index<end) appendSegmentRemovingTags(index,end);
		}
	
		private static ElementHandler getElementHandler(final Element element) {
			if (element.getStartTag().getStartTagType().isServerTag()) return RemoveElementHandler.INSTANCE; // hard-coded configuration does not include server tags in child element hierarchy, so this is normally not executed.
			ElementHandler elementHandler=ELEMENT_HANDLERS.get(element.getName());
			return (elementHandler!=null) ? elementHandler : StandardInlineElementHandler.INSTANCE;
		}
	
		private void appendSegmentRemovingTags(final int begin, final int end) throws IOException {
			int index=begin;
			while (true) {
				Tag tag=source.getNextTag(index);
				if (tag==null || tag.begin>=end) break;
				appendSegment(index,tag.begin);
				index=tag.end;
			}
			appendSegment(index,end);
		}

		private void appendSegment(int begin, final int end) throws IOException {
 			assert begin<=end;
			if (begin<renderedIndex) begin=renderedIndex;
			if (begin>=end) return;
			try {
				if (preformatted)
					appendPreformattedSegment(begin,end);
				else
					appendNonPreformattedSegment(begin,end);
			} finally {
				if (renderedIndex<end) renderedIndex=end;
			}
		}
	
		private void appendPreformattedSegment(final int begin, final int end) throws IOException {
			assert begin<end;
			assert begin>=renderedIndex;
			if (isBlockBoundary()) appendBlockVerticalMargin();
			final String text=CharacterReference.decode(source.subSequence(begin,end),false,convertNonBreakingSpaces);
			for (int i=0; i<text.length(); i++) {
				final char ch=text.charAt(i);
				if (ch=='\n') {
					newLine();
				} else if (ch=='\r') {
					newLine();
					final int nextI=i+1;
					if (nextI==text.length()) break;
					if (text.charAt(nextI)=='\n') i++;
				} else {
					append(ch);
				}
			}
		}

		private void appendNonPreformattedSegment(final int begin, final int end) throws IOException {
			assert begin<end;
			assert begin>=renderedIndex;
			final String text=CharacterReference.decodeCollapseWhiteSpace(source.subSequence(begin,end),convertNonBreakingSpaces);
			if (text.length()==0) {
				// collapsed text is zero length but original segment wasn't, meaning it consists purely of white space.
				if (!ignoreInitialWhiteSpace) lastCharWhiteSpace=true;
				return;
			}
			appendNonPreformattedText(text,Segment.isWhiteSpace(source.charAt(begin)),Segment.isWhiteSpace(source.charAt(end-1)));
		}

		private void appendText(final String text) throws IOException {
			assert text.length()>0;
			appendNonPreformattedText(text,Segment.isWhiteSpace(text.charAt(0)),Segment.isWhiteSpace(text.charAt(text.length()-1)));
		}
			
		private void appendNonPreformattedText(final String text, final boolean isWhiteSpaceAtStart, final boolean isWhiteSpaceAtEnd) throws IOException {
			if (isBlockBoundary()) {
				appendBlockVerticalMargin();
			} else if (lastCharWhiteSpace || (isWhiteSpaceAtStart && !ignoreInitialWhiteSpace)) {
				// output white space only if not on a block boundary
				append(' ');
			}
			int textIndex=0;
			int i=0;
			lastCharWhiteSpace=false;
			//ignoreInitialWhiteSpace=false;
			while (true) {
				for (; i<text.length(); i++) {
					if (text.charAt(i)!=' ') continue; // search for end of word
					// At end of word. To comply with RFC264 Format=Flowed protocol, need to make sure we don't wrap immediately before ">" or "From ".
					if (i+1<text.length() && text.charAt(i+1)=='>') continue;
					if (i+6<text.length() && text.startsWith("From ",i+1)) continue;
					break; // OK to wrap here if necessary
				}
				if (maxLineLength>0 && col+i-textIndex+1>=maxLineLength) {
					if (lastCharWhiteSpace && (listIndentLevel|indentSize)==0) append(' ');
					startNewLine(0);
				} else if (lastCharWhiteSpace) {
					append(' ');
				}
				append(text,textIndex,i);
				if (i==text.length()) break;
				lastCharWhiteSpace=true;
				textIndex=++i;
			}
			lastCharWhiteSpace=isWhiteSpaceAtEnd;
		}

		private boolean isBlockBoundary() {
			return blockVerticalMargin!=NO_MARGIN;
		}

		private void appendBlockVerticalMargin() throws IOException {
			assert blockVerticalMargin!=NO_MARGIN;
			if (skipInitialNewLines) {
				// at first text after <li> element or start of document
				skipInitialNewLines=false;
				final int indentCol=indentSize+listIndentLevel*listIndentSize;
				if (col==indentCol) {
					atStartOfLine=false; // no need to call appendIndent() from appendTextInit().
				} else {
					// there was an indenting block since the <li> or start of document
					if (bullet || col>indentCol) {
						// just start new line as normal if the last indenting block is another <li>, or if the current column is already past the required indent
						startNewLine(0);
					} else {
						// just append spaces to get the column up to the required indent
						while (indentCol>col) {
							appendable.append(' ');
							col++;
						}
						atStartOfLine=false; // make sure appendIndent() isn't called again from appendTextInit()
					}
				}
			} else {
				startNewLine(blockVerticalMargin);
			}
			blockVerticalMargin=NO_MARGIN;
		}

		private void blockBoundary(final int verticalMargin) throws IOException {
			// Set a block boundary with the given vertical margin.  The vertical margin is the minimum number of blank lines to output between the blocks.
			// This method can be called multiple times at a block boundary, and the next textual output will output the number of blank lines determined by the
			// maximum vertical margin of all the method calls.
			if (blockVerticalMargin<verticalMargin) blockVerticalMargin=verticalMargin;
		}
	
		private void startNewLine(int verticalMargin) throws IOException {
			// ensures we end up at the start of a line with the specified vertical margin between the previous textual output and the next textual output.
			final int requiredNewLines=verticalMargin+(atStartOfLine?0:1);
			for (int i=0; i<requiredNewLines; i++) appendable.append(newLine);
			atStartOfLine=true;
			col=0;
		}

		private void newLine() throws IOException {
			appendable.append(newLine);
			atStartOfLine=true;
			col=0;
		}

		private void appendTextInit() throws IOException {
			skipInitialNewLines=false;
			if (atStartOfLine) appendIndent();
		}
	
		private void appendIndent() throws IOException {
			for (int i=indentSize; i>0; i--) appendable.append(' ');
			if (bullet) {
				for (int i=(listIndentLevel-1)*listIndentSize; i>0; i--) appendable.append(' ');
				if (listBulletNumber==UNORDERED_LIST) {
					for (int i=listIndentSize-2; i>0; i--) appendable.append(' ');
					appendable.append(listBullets[(listIndentLevel-1)%listBullets.length]).append(' ');
				} else {
					String bulletNumberString=Integer.toString(listBulletNumber);
					for (int i=listIndentSize-bulletNumberString.length()-2; i>0; i--) appendable.append(' ');
					appendable.append(bulletNumberString).append(". ");
				}
				bullet=false;
			} else {
				for (int i=listIndentLevel*listIndentSize; i>0; i--) appendable.append(' ');
			}
			col=indentSize+listIndentLevel*listIndentSize;
			atStartOfLine=false;
		}
	
		private Processor append(final char ch) throws IOException {
			appendTextInit();
			appendable.append(ch);
			col++;
			return this;
		}
		
		private Processor append(final String text) throws IOException {
			appendTextInit();
			appendable.append(text);
			col+=text.length();
			return this;
		}
	
		private void append(final CharSequence text, final int begin, final int end) throws IOException {
			appendTextInit();
			for (int i=begin; i<end; i++) appendable.append(text.charAt(i));
			col+=end-begin;
		}
	}
	
	private interface ElementHandler {
		void process(Processor x, Element element) throws IOException;
	}
	
	private static final class RemoveElementHandler implements ElementHandler {
		public static final ElementHandler INSTANCE=new RemoveElementHandler();
		public void process(Processor x, Element element) {}
	}
	
	private static final class StandardInlineElementHandler implements ElementHandler {
		public static final ElementHandler INSTANCE=new StandardInlineElementHandler();
		public void process(Processor x, Element element) throws IOException {
			x.appendElementContent(element);
		}
	}

	private static final class FontStyleElementHandler implements ElementHandler {
		public static final ElementHandler INSTANCE_B=new FontStyleElementHandler('*');
		public static final ElementHandler INSTANCE_I=new FontStyleElementHandler('/');
		public static final ElementHandler INSTANCE_U=new FontStyleElementHandler('_');
		public static final ElementHandler INSTANCE_CODE=new FontStyleElementHandler('|');
		private final char decorationChar;
		public FontStyleElementHandler(char decorationChar) {
			this.decorationChar=decorationChar;
		}
		public void process(Processor x, Element element) throws IOException {
			if (x.decorateFontStyles) {
				if (x.isBlockBoundary()) {
					x.appendBlockVerticalMargin();
				} else if (x.lastCharWhiteSpace) {
					// output white space only if not on a block boundary
					x.append(' ');
					x.lastCharWhiteSpace=false;
				}
				x.append(decorationChar);
				x.appendElementContent(element);
				if (x.decorateFontStyles) x.append(decorationChar);
			} else {
				x.appendElementContent(element);
			}
		}
	}

	abstract private static class AbstractBlockElementHandler implements ElementHandler {
		private final int topMargin;
		private final int bottomMargin;
		private final boolean indent;
		protected AbstractBlockElementHandler(int topMargin, int bottomMargin, boolean indent) {
			this.topMargin=topMargin;
			this.bottomMargin=bottomMargin;
			this.indent=indent;
		}
		public void process(Processor x, Element element) throws IOException {
			x.blockBoundary(RendererCSS.getTopMargin(element,topMargin));
			int leftMargin=RendererCSS.getLeftMargin(element,indent ? x.blockIndentSize : 0);
			x.indentSize+=leftMargin;
			processBlockContent(x,element);
			x.indentSize-=leftMargin;
			x.blockBoundary(RendererCSS.getBottomMargin(element,bottomMargin));
		}
		public AbstractBlockElementHandler newTopMargin(int topMargin) {
			return newInstance(topMargin,this.bottomMargin,this.indent);
		}
		public int getTopMargin() {
			return topMargin;
		}
		public AbstractBlockElementHandler newBottomMargin(int bottomMargin) {
			return newInstance(this.topMargin,bottomMargin,this.indent);
		}
		public int getBottomMargin() {
			return bottomMargin;
		}
		public AbstractBlockElementHandler newIndent(boolean indent) {
			return newInstance(this.topMargin,this.bottomMargin,indent);
		}
		public boolean isIndent() {
			return indent;
		}
		abstract protected void processBlockContent(Processor x, Element element) throws IOException;
		abstract protected AbstractBlockElementHandler newInstance(int topMargin, int bottomMargin, boolean indent);
	}

	private static final class StandardBlockElementHandler extends AbstractBlockElementHandler {
		public static final ElementHandler INSTANCE_0_0=new StandardBlockElementHandler(0,0,false);
		public static final ElementHandler INSTANCE_1_1=new StandardBlockElementHandler(1,1,false);
		public static final ElementHandler INSTANCE_2_1=new StandardBlockElementHandler(2,1,false);
		public static final ElementHandler INSTANCE_0_0_INDENT=new StandardBlockElementHandler(0,0,true);
		public static final ElementHandler INSTANCE_1_1_INDENT=new StandardBlockElementHandler(1,1,true);
		private StandardBlockElementHandler(int topMargin, int bottomMargin, boolean indent) {
			super(topMargin,bottomMargin,indent);
		}
		protected void processBlockContent(Processor x, Element element) throws IOException {
			x.appendElementContent(element);
		}
		protected AbstractBlockElementHandler newInstance(int topMargin, int bottomMargin, boolean indent) {
			return new StandardBlockElementHandler(topMargin,bottomMargin,indent);
		}
	}

	private static final class A_ElementHandler implements ElementHandler {
		public static final ElementHandler INSTANCE=new A_ElementHandler();
		public void process(Processor x, Element element) throws IOException {
			if (!x.includeHyperlinkURLs) {
				x.appendElementContent(element);
				return;
			}
			String renderedHyperlinkURL=x.renderer.renderHyperlinkURL(element.getStartTag());
			if (renderedHyperlinkURL==null) {
				x.appendElementContent(element);
				return;
			}
			String href=element.getAttributeValue("href");
			final boolean displayContent=href==null || !getInformalURL(href).equals(getInformalURL(element.getContent().toString())); // only display the content if it is not the same as the URL
			int linkLength=renderedHyperlinkURL.length();
			if (displayContent) {
				x.appendElementContent(element);
				linkLength++; // allow for space after content
			}
			if (x.maxLineLength>0 && x.col+linkLength>=x.maxLineLength) {
				x.startNewLine(0);
			} else if (displayContent) {
				x.append(' ');
			}
			x.append(renderedHyperlinkURL);
			x.lastCharWhiteSpace=true;
		}
	}

	private static final String getInformalURL(String url) {
		if (url.startsWith("http://")) url=url.substring(7);
		if (url.endsWith("/")) url=url.substring(0,url.length()-1);
		return url;
	}
	
	private static final class BR_ElementHandler implements ElementHandler {
		public static final ElementHandler INSTANCE=new BR_ElementHandler();
		public void process(Processor x, Element element) throws IOException {
			if (x.isBlockBoundary() && !x.atStartOfLine && !x.skipInitialNewLines) x.newLine(); // add an extra new line if we're at a block boundary and aren't already at the start of the next line and it's not the first element after <li>
			x.newLine();
			x.blockBoundary(0);
		}
	}

	private static final class HR_ElementHandler extends AbstractBlockElementHandler {
		public static final ElementHandler INSTANCE=new HR_ElementHandler();
		private HR_ElementHandler() {
			this(0,0,false);
		}
		private HR_ElementHandler(int topMargin, int bottomMargin, boolean indent) {
			super(topMargin,bottomMargin,indent);
		}
		protected void processBlockContent(Processor x, Element element) throws IOException {
			x.appendBlockVerticalMargin();
			x.append('-');
			for (int i=x.col; i<x.hrLineLength; i++) x.appendable.append('-');
			x.col=x.hrLineLength;
		}
		protected AbstractBlockElementHandler newInstance(int topMargin, int bottomMargin, boolean indent) {
			return new HR_ElementHandler(topMargin,bottomMargin,indent);
		}
	}

	private static final class AlternateTextElementHandler implements ElementHandler {
		public static final ElementHandler INSTANCE=new AlternateTextElementHandler();
		public void process(Processor x, Element element) throws IOException {
			if (!x.includeAlternateText) return;
			String text=x.renderer.renderAlternateText(element.getStartTag());
			if (text==null) return;
			x.appendText(text);
		}
	}

	private static final class ListElementHandler extends AbstractBlockElementHandler {
		public static final ElementHandler INSTANCE_OL=new ListElementHandler(0);
		public static final ElementHandler INSTANCE_UL=new ListElementHandler(UNORDERED_LIST);
		private final int initialListBulletNumber;
		private ListElementHandler(int initialListBulletNumber) {
			this(initialListBulletNumber,0,0,false);
		}
		private ListElementHandler(int initialListBulletNumber, int topMargin, int bottomMargin, boolean indent) {
			super(topMargin,bottomMargin,indent);
			this.initialListBulletNumber=initialListBulletNumber;
		}
		protected void processBlockContent(Processor x, Element element) throws IOException {
			int oldListBulletNumber=x.listBulletNumber;
			x.listBulletNumber=initialListBulletNumber;
			x.listIndentLevel++;
			x.appendElementContent(element);
			x.listIndentLevel--;
			x.listBulletNumber=oldListBulletNumber;
		}
		protected AbstractBlockElementHandler newInstance(int topMargin, int bottomMargin, boolean indent) {
			return new ListElementHandler(initialListBulletNumber,topMargin,bottomMargin,indent);
		}
	}

	private static final class LI_ElementHandler extends AbstractBlockElementHandler {
		public static final ElementHandler INSTANCE=new LI_ElementHandler();
		private LI_ElementHandler() {
			this(0,0,false);
		}
		private LI_ElementHandler(int topMargin, int bottomMargin, boolean indent) {
			super(topMargin,bottomMargin,indent);
		}
		protected void processBlockContent(Processor x, Element element) throws IOException {
			if (x.listBulletNumber!=UNORDERED_LIST) x.listBulletNumber++;
			x.bullet=true;
			x.appendBlockVerticalMargin();
			x.appendIndent();
			x.skipInitialNewLines=true;
			x.blockBoundary(0); // this shouldn't result in the output of any new lines but ensures surrounding white space is ignored
			x.appendElementContent(element);
			x.bullet=false;
		}
		protected AbstractBlockElementHandler newInstance(int topMargin, int bottomMargin, boolean indent) {
			return new LI_ElementHandler(topMargin,bottomMargin,indent);
		}
	}

	private static final class PRE_ElementHandler extends AbstractBlockElementHandler {
		public static final ElementHandler INSTANCE=new PRE_ElementHandler();
		private PRE_ElementHandler() {
			this(1,1,false);
		}
		private PRE_ElementHandler(int topMargin, int bottomMargin, boolean indent) {
			super(topMargin,bottomMargin,indent);
		}
		protected void processBlockContent(Processor x, Element element) throws IOException {
			boolean oldPreformatted=x.preformatted; // should always be false
			x.preformatted=true;
			x.appendElementContent(element);
			x.preformatted=oldPreformatted;
		}
		protected AbstractBlockElementHandler newInstance(int topMargin, int bottomMargin, boolean indent) {
			return new PRE_ElementHandler(topMargin,bottomMargin,indent);
		}
	}

	private static final class TD_ElementHandler implements ElementHandler {
		public static final ElementHandler INSTANCE=new TD_ElementHandler();
		public void process(Processor x, Element element) throws IOException {
			if (!x.isBlockBoundary()) x.append(x.tableCellSeparator);
			x.lastCharWhiteSpace=false;
			x.appendElementContent(element);
		}
	}

}