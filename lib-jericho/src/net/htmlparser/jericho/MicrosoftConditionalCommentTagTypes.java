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

/**
 * Contains {@linkplain TagType tag types} representing Microsoft&reg; <a target="_blank" href="http://en.wikipedia.org/wiki/Conditional_comment">conditional comments</a>.
 * <p>
 * Officially there are only two types of conditional comment,
 * <a target="_blank" href="http://en.wikipedia.org/wiki/Conditional_comment#Downlevel-hidden_conditional_comment">downlevel-hidden</a> and
 * <a target="_blank" href="http://en.wikipedia.org/wiki/Conditional_comment#Downlevel-revealed_conditional_comment">downlevel-revealed</a>,
 * with each defining a start tag containing an "<code>if</code>" expression and an end tag containing the text "<code>endif</code>".
 * <p>
 * These four official tags are represented by the tag types
 * {@link #DOWNLEVEL_HIDDEN_IF}, {@link #DOWNLEVEL_HIDDEN_ENDIF}, {@link #DOWNLEVEL_REVEALED_IF} and {@link #DOWNLEVEL_REVEALED_ENDIF}.
 * <p>
 * The conditional expression of an instance of any of the "<code>if</code>" tag types can be extracted using the {@link StartTag#getTagContent()} method.
 * For example, if the variable <code>conditionalCommentIfTag</code> represents the tag <code>&lt;![if !IE]&gt;</code>, then the expression
 * <code>conditionalCommentIfTag.getTagContent().toString().trim()</code> yields the string "<code>!IE</code>".
 * <p>
 * Conditional comments are only recognised by Internet Explorer.  All other browsers recognise
 * <a target="_blank" href="http://en.wikipedia.org/wiki/Conditional_comment#Downlevel-hidden_conditional_comment">downlevel-hidden</a> conditional comments as
 * normal {@linkplain StartTagType#COMMENT comments}, and ignore
 * <a target="_blank" href="http://en.wikipedia.org/wiki/Conditional_comment#Downlevel-revealed_conditional_comment">downlevel-revealed</a> conditional comment tags as
 * unrecognised.
 * <p>
 * The use of <a target="_blank" href="http://en.wikipedia.org/wiki/Conditional_comment#Downlevel-revealed_conditional_comment">downlevel-revealed</a> conditional comments
 * is generally avoided because they represent invalid HTML code.  They can however be replaced by functionally equivalent syntactical constructs based on
 * <a target="_blank" href="http://en.wikipedia.org/wiki/Conditional_comment#Downlevel-hidden_conditional_comment">downlevel-hidden</a> conditional comments
 * so that the HTML remains valid.
 * These composite constructs are represented by the tag types
 * {@link #DOWNLEVEL_REVEALED_VALIDATING_IF}, {@link #DOWNLEVEL_REVEALED_VALIDATING_ENDIF} and {@link #DOWNLEVEL_REVEALED_VALIDATING_SIMPLIFIED_IF}.
 * <p>
 * Because none of the conditional comment end tags start with "<code>&lt;/</code>", they are represented in this library as
 * {@linkplain StartTagType start tag types}, and the parser makes no attempt to match if-endif tag pairs to form {@linkplain Element elements}.
 * <p>
 * The tag types defined in this class are not {@linkplain TagType#register() registered} by default.
 */
public final class MicrosoftConditionalCommentTagTypes {

	/**
	 * The tag type given to a <a target="_blank" href="http://en.wikipedia.org/wiki/Conditional_comment#Downlevel-hidden_conditional_comment">downlevel-hidden conditional comment</a> <code>if</code> tag
	 * (<code>&lt;&#33;--[if<var> &#46;&#46;&#46; </var>]&gt;</code>).
	 * <p>
	 * The corresponding end tag is represented by the tag type {@link #DOWNLEVEL_HIDDEN_ENDIF}.
	 * <p>
	 * <dl>
	 *  <dt>Properties:</dt>
	 *   <dd>
	 *    <table class="bordered" style="margin: 15px" cellspacing="0">
	 *     <tr><th>Property<th>Value
	 *     <tr><td>{@link StartTagType#getDescription() Description}<td>Microsoft downlevel-hidden conditional comment IF
	 *     <tr><td>{@link StartTagType#getStartDelimiter() StartDelimiter}<td><code>&lt;!--[if</code>
	 *     <tr><td>{@link StartTagType#getClosingDelimiter() ClosingDelimiter}<td><code>]&gt;</code>
	 *     <tr><td>{@link StartTagType#isServerTag() IsServerTag}<td><code>false</code>
	 *     <tr><td>{@link StartTagType#getNamePrefix() NamePrefix}<td><code>!--[if</code>
	 *     <tr><td>{@link StartTagType#getCorrespondingEndTagType() CorrespondingEndTagType}<td><code>null</code>
	 *     <tr><td>{@link StartTagType#hasAttributes() HasAttributes}<td><code>false</code>
	 *     <tr><td>{@link StartTagType#isNameAfterPrefixRequired() IsNameAfterPrefixRequired}<td><code>false</code>
	 *    </table>
	 *  <dt>Example:</dt>
	 *   <dd><code>&lt;!--[if IE]&gt;</code></dd>
	 * </dl>
	 */
	public static final StartTagType DOWNLEVEL_HIDDEN_IF=StartTagTypeMicrosoftDownlevelHiddenConditionalCommentIf.INSTANCE;

	/**
	 * The tag type given to a <a target="_blank" href="http://en.wikipedia.org/wiki/Conditional_comment#Downlevel-hidden_conditional_comment">downlevel-hidden conditional comment</a> <code>endif</code> tag
	 * (<code>&lt;&#33;[endif]--&gt;</code>).
	 * <p>
	 * The corresponding start tag is represented by the tag type {@link #DOWNLEVEL_HIDDEN_IF}.
	 * <p>
	 * Although this functions as an end tag, it is represented in this library as a {@linkplain StartTagType start tag type} because it does not start with the characters "<code>&lt;/</code>".
	 * <p>
	 * <dl>
	 *  <dt>Properties:</dt>
	 *   <dd>
	 *    <table class="bordered" style="margin: 15px" cellspacing="0">
	 *     <tr><th>Property<th>Value
	 *     <tr><td>{@link StartTagType#getDescription() Description}<td>Microsoft downlevel-hidden conditional comment ENDIF
	 *     <tr><td>{@link StartTagType#getStartDelimiter() StartDelimiter}<td><code>&lt;![endif]--&gt;</code>
	 *     <tr><td>{@link StartTagType#getClosingDelimiter() ClosingDelimiter}<td><i>(empty&nbsp;string)</i>
	 *     <tr><td>{@link StartTagType#isServerTag() IsServerTag}<td><code>false</code>
	 *     <tr><td>{@link StartTagType#getNamePrefix() NamePrefix}<td><code>![endif]--&gt;</code>
	 *     <tr><td>{@link StartTagType#getCorrespondingEndTagType() CorrespondingEndTagType}<td><code>null</code>
	 *     <tr><td>{@link StartTagType#hasAttributes() HasAttributes}<td><code>false</code>
	 *     <tr><td>{@link StartTagType#isNameAfterPrefixRequired() IsNameAfterPrefixRequired}<td><code>false</code>
	 *    </table>
	 *  <dt>Example:</dt>
	 *   <dd><code>&lt;![endif]&gt;</code></dd>
	 * </dl>
	 */
	public static final StartTagType DOWNLEVEL_HIDDEN_ENDIF=StartTagTypeMicrosoftDownlevelHiddenConditionalCommentEndif.INSTANCE;

	/**
	 * The tag type given to a <a target="_blank" href="http://en.wikipedia.org/wiki/Conditional_comment#Downlevel-revealed_conditional_comment">downlevel-revealed conditional comment</a> <code>if</code> tag
	 * (<code>&lt;&#33;[if<var> &#46;&#46;&#46; </var>]&gt;</code>).
	 * <p>
	 * The corresponding end tag is represented by the tag type {@link #DOWNLEVEL_REVEALED_ENDIF}.
	 * <p>
	 * <dl>
	 *  <dt>Properties:</dt>
	 *   <dd>
	 *    <table class="bordered" style="margin: 15px" cellspacing="0">
	 *     <tr><th>Property<th>Value
	 *     <tr><td>{@link StartTagType#getDescription() Description}<td>Microsoft downlevel-revealed conditional comment IF
	 *     <tr><td>{@link StartTagType#getStartDelimiter() StartDelimiter}<td><code>&lt;![if</code>
	 *     <tr><td>{@link StartTagType#getClosingDelimiter() ClosingDelimiter}<td><code>]&gt;</code>
	 *     <tr><td>{@link StartTagType#isServerTag() IsServerTag}<td><code>false</code>
	 *     <tr><td>{@link StartTagType#getNamePrefix() NamePrefix}<td><code>![if</code>
	 *     <tr><td>{@link StartTagType#getCorrespondingEndTagType() CorrespondingEndTagType}<td><code>null</code>
	 *     <tr><td>{@link StartTagType#hasAttributes() HasAttributes}<td><code>false</code>
	 *     <tr><td>{@link StartTagType#isNameAfterPrefixRequired() IsNameAfterPrefixRequired}<td><code>false</code>
	 *    </table>
	 *  <dt>Example:</dt>
	 *   <dd><code>&lt;![if !IE]&gt;</code></dd>
	 * </dl>
	 */
	public static final StartTagType DOWNLEVEL_REVEALED_IF=StartTagTypeMicrosoftDownlevelRevealedConditionalCommentIf.INSTANCE;

	/**
	 * The tag type given to a <a target="_blank" href="http://en.wikipedia.org/wiki/Conditional_comment#Downlevel-revealed_conditional_comment">downlevel-revealed conditional comment</a> <code>endif</code> tag
	 * (<code>&lt;&#33;[endif]&gt;</code>).
	 * <p>
	 * The corresponding start tag is represented by the tag type {@link #DOWNLEVEL_REVEALED_IF}.
	 * <p>
	 * Although this functions as an end tag, it is represented in this library as a {@linkplain StartTagType start tag type} because it does not start with the characters "<code>&lt;/</code>".
	 * <p>
	 * <dl>
	 *  <dt>Properties:</dt>
	 *   <dd>
	 *    <table class="bordered" style="margin: 15px" cellspacing="0">
	 *     <tr><th>Property<th>Value
	 *     <tr><td>{@link StartTagType#getDescription() Description}<td>Microsoft downlevel-revealed conditional comment ENDIF
	 *     <tr><td>{@link StartTagType#getStartDelimiter() StartDelimiter}<td><code>&lt;![endif]&gt;</code>
	 *     <tr><td>{@link StartTagType#getClosingDelimiter() ClosingDelimiter}<td><i>(empty&nbsp;string)</i>
	 *     <tr><td>{@link StartTagType#isServerTag() IsServerTag}<td><code>false</code>
	 *     <tr><td>{@link StartTagType#getNamePrefix() NamePrefix}<td><code>![endif]&gt;</code>
	 *     <tr><td>{@link StartTagType#getCorrespondingEndTagType() CorrespondingEndTagType}<td><code>null</code>
	 *     <tr><td>{@link StartTagType#hasAttributes() HasAttributes}<td><code>false</code>
	 *     <tr><td>{@link StartTagType#isNameAfterPrefixRequired() IsNameAfterPrefixRequired}<td><code>false</code>
	 *    </table>
	 *  <dt>Example:</dt>
	 *   <dd><code>&lt;![endif]&gt;</code></dd>
	 * </dl>
	 */
	public static final StartTagType DOWNLEVEL_REVEALED_ENDIF=StartTagTypeMicrosoftDownlevelRevealedConditionalCommentEndif.INSTANCE;

	/**
	 * The tag type given to a validating <a target="_blank" href="http://en.wikipedia.org/wiki/Conditional_comment#Downlevel-revealed_conditional_comment">downlevel-revealed conditional comment</a> <code>if</code> tag
	 * (<code>&lt;&#33;--[if<var> &#46;&#46;&#46; </var>]&gt;&lt;&#33;--&gt;</code>).
	 * <p>
	 * The corresponding end tag is represented by the tag type {@link #DOWNLEVEL_REVEALED_VALIDATING_ENDIF}.
	 * <p>
	 * This tag is actually a composite syntactical construct containing a
	 * <a target="_blank" href="http://en.wikipedia.org/wiki/Conditional_comment#Downlevel-hidden_conditional_comment">downlevel-hidden conditional comment</a> <code>if</code> tag.
	 * <p>
	 * <dl>
	 *  <dt>Properties:</dt>
	 *   <dd>
	 *    <table class="bordered" style="margin: 15px" cellspacing="0">
	 *     <tr><th>Property<th>Value
	 *     <tr><td>{@link StartTagType#getDescription() Description}<td>Microsoft downlevel-revealed validating conditional comment IF
	 *     <tr><td>{@link StartTagType#getStartDelimiter() StartDelimiter}<td><code>&lt;!--[if</code>
	 *     <tr><td>{@link StartTagType#getClosingDelimiter() ClosingDelimiter}<td><code>]&gt;&lt;!--&gt;</code>
	 *     <tr><td>{@link StartTagType#isServerTag() IsServerTag}<td><code>false</code>
	 *     <tr><td>{@link StartTagType#getNamePrefix() NamePrefix}<td><code>!--[if</code>
	 *     <tr><td>{@link StartTagType#getCorrespondingEndTagType() CorrespondingEndTagType}<td><code>null</code>
	 *     <tr><td>{@link StartTagType#hasAttributes() HasAttributes}<td><code>false</code>
	 *     <tr><td>{@link StartTagType#isNameAfterPrefixRequired() IsNameAfterPrefixRequired}<td><code>false</code>
	 *    </table>
	 *  <dt>Example:</dt>
	 *   <dd><code>&lt;!--[if !(IE 5)]&gt;&lt;!--&gt;</code></dd>
	 * </dl>
	 */
	public static final StartTagType DOWNLEVEL_REVEALED_VALIDATING_IF=StartTagTypeMicrosoftDownlevelRevealedValidatingConditionalCommentIf.INSTANCE;

	/**
	 * The tag type given to a validating <a target="_blank" href="http://en.wikipedia.org/wiki/Conditional_comment#Downlevel-revealed_conditional_comment">downlevel-revealed conditional comment</a> <code>endif</code> tag
	 * (<code>&lt;&#33;--&lt;&#33;[endif]--&gt;</code>).
	 * <p>
	 * The corresponding start tag is represented by the tag type {@link #DOWNLEVEL_REVEALED_VALIDATING_IF}.
	 * <p>
	 * This tag is actually a composite syntactical construct containing a
	 * <a target="_blank" href="http://en.wikipedia.org/wiki/Conditional_comment#Downlevel-hidden_conditional_comment">downlevel-hidden conditional comment</a> <code>endif</code> tag.
	 * <p>
	 * Although this functions as an end tag, it is represented in this library as a {@linkplain StartTagType start tag type} because it does not start with the characters "<code>&lt;/</code>".
	 * <p>
	 * <dl>
	 *  <dt>Properties:</dt>
	 *   <dd>
	 *    <table class="bordered" style="margin: 15px" cellspacing="0">
	 *     <tr><th>Property<th>Value
	 *     <tr><td>{@link StartTagType#getDescription() Description}<td>Microsoft downlevel-revealed validating conditional comment ENDIF
	 *     <tr><td>{@link StartTagType#getStartDelimiter() StartDelimiter}<td><code>&lt;!--&lt;![endif]--&gt;</code>
	 *     <tr><td>{@link StartTagType#getClosingDelimiter() ClosingDelimiter}<td><i>(empty&nbsp;string)</i>
	 *     <tr><td>{@link StartTagType#isServerTag() IsServerTag}<td><code>false</code>
	 *     <tr><td>{@link StartTagType#getNamePrefix() NamePrefix}<td><code>!--&lt;![endif]--&gt;</code>
	 *     <tr><td>{@link StartTagType#getCorrespondingEndTagType() CorrespondingEndTagType}<td><code>null</code>
	 *     <tr><td>{@link StartTagType#hasAttributes() HasAttributes}<td><code>false</code>
	 *     <tr><td>{@link StartTagType#isNameAfterPrefixRequired() IsNameAfterPrefixRequired}<td><code>false</code>
	 *    </table>
	 *  <dt>Example:</dt>
	 *   <dd><code>&lt;!--&lt;![endif]--&gt;</code></dd>
	 * </dl>
	 */
	public static final StartTagType DOWNLEVEL_REVEALED_VALIDATING_ENDIF=StartTagTypeMicrosoftDownlevelRevealedValidatingConditionalCommentEndif.INSTANCE;

	/**
	 * The tag type given to a validating simplified <a target="_blank" href="http://en.wikipedia.org/wiki/Conditional_comment#Downlevel-revealed_conditional_comment">downlevel-revealed conditional comment</a> <code>if</code> tag
	 * (<code>&lt;&#33;--[if<var> &#46;&#46;&#46; </var>]&gt;--&gt;</code>).
	 * <p>
	 * This form of validating downlevel-revealed conditional comment must only be used when the condition always evaluated to false in Internet Explorer,
	 * which means the condition should be either "<code>false</code>" or "<code>!IE</code>".
	 * <p>
	 * The corresponding end tag is represented by the tag type {@link #DOWNLEVEL_REVEALED_VALIDATING_ENDIF}, which is identical to the corresponding end tag of {@link #DOWNLEVEL_REVEALED_VALIDATING_IF}.
	 * <p>
	 * This tag is actually a composite syntactical construct containing a
	 * <a target="_blank" href="http://en.wikipedia.org/wiki/Conditional_comment#Downlevel-hidden_conditional_comment">downlevel-hidden conditional comment</a> <code>if</code> tag.
	 * <p>
	 * <dl>
	 *  <dt>Properties:</dt>
	 *   <dd>
	 *    <table class="bordered" style="margin: 15px" cellspacing="0">
	 *     <tr><th>Property<th>Value
	 *     <tr><td>{@link StartTagType#getDescription() Description}<td>Microsoft downlevel-revealed validating simplified conditional comment IF
	 *     <tr><td>{@link StartTagType#getStartDelimiter() StartDelimiter}<td><code>&lt;!--[if</code>
	 *     <tr><td>{@link StartTagType#getClosingDelimiter() ClosingDelimiter}<td><code>]&gt;--&gt;</code>
	 *     <tr><td>{@link StartTagType#isServerTag() IsServerTag}<td><code>false</code>
	 *     <tr><td>{@link StartTagType#getNamePrefix() NamePrefix}<td><code>!--[if</code>
	 *     <tr><td>{@link StartTagType#getCorrespondingEndTagType() CorrespondingEndTagType}<td><code>null</code>
	 *     <tr><td>{@link StartTagType#hasAttributes() HasAttributes}<td><code>false</code>
	 *     <tr><td>{@link StartTagType#isNameAfterPrefixRequired() IsNameAfterPrefixRequired}<td><code>false</code>
	 *    </table>
	 *  <dt>Example:</dt>
	 *   <dd><code>&lt;!--[if !IE]&gt;--&gt;</code></dd>
	 * </dl>
	 */
	public static final StartTagType DOWNLEVEL_REVEALED_VALIDATING_SIMPLIFIED_IF=StartTagTypeMicrosoftDownlevelRevealedValidatingSimplifiedConditionalCommentIf.INSTANCE;

	private static final TagType[] TAG_TYPES={
		DOWNLEVEL_HIDDEN_IF,
		DOWNLEVEL_HIDDEN_ENDIF,
		DOWNLEVEL_REVEALED_IF,
		DOWNLEVEL_REVEALED_ENDIF,
		DOWNLEVEL_REVEALED_VALIDATING_IF, // this must come after DOWNLEVEL_HIDDEN_IF so it has higher precedence
		DOWNLEVEL_REVEALED_VALIDATING_SIMPLIFIED_IF, // this must come after DOWNLEVEL_HIDDEN_IF so it has higher precedence
		DOWNLEVEL_REVEALED_VALIDATING_ENDIF
	};

	private MicrosoftConditionalCommentTagTypes() {}

	/** 
	 * {@linkplain TagType#register() Registers} all of the tag types defined in this class at once.
	 * <p>
	 * The tag types must be registered before the parser will recognise them.
	 */
	public static void register() {
		for (TagType tagType : TAG_TYPES) tagType.register();
	}

	/** 
	 * {@linkplain TagType#deregister() Deregisters} all of the tag types defined in this class at once.
	 */
	public static void deregister() {
		for (TagType tagType : TAG_TYPES) tagType.deregister();
	}
	
	/**
	 * Indicates whether the specified tag type is defined in this class.
	 *
	 * @param tagType  the {@link TagType} to test.
	 * @return <code>true</code> if the specified tag type is defined in this class, otherwise <code>false</code>.
	 */
	public static boolean defines(final TagType tagType) {
		for (TagType definedTagType : TAG_TYPES) if (tagType==definedTagType) return true;
		return false;
	}
	
}

