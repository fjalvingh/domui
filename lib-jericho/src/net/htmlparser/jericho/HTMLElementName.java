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
 * Contains static fields representing the {@linkplain Element#getName() names} of
 * <a target="_blank" href="http://www.w3.org/TR/html401/index/elements.html">all elements defined in the HTML 4.01 specification</a>
 * and the <a target="_blank" href="http://dev.w3.org/html5/spec/Overview.html#elements-1">draft HTML 5 specification</a>.
 * <p>
 * All of the name strings are in lower case.
 * <p>
 * The {@link HTMLElements} class is closely related to this interface, containing static methods which group these names 
 * by the characteristics of their associated <a href="HTMLElements.html#HTMLElement">elements</a>.
 * <p>
 * This interface does not specify any methods, but can be inherited by other classes, or statically imported (Java 5.0),
 * to provide less verbose access to the contained element name static fields.
 * <p>
 * The field values in this interface can be used as <code>name</code> arguments in <a href="Tag.html#NamedSearch">named tag searches</a>.
 *
 * @see HTMLElements
 * @see Element
 */
public interface HTMLElementName {
	/**
	 * <a target="_blank" href="http://www.w3.org/TR/html401/struct/links.html#edef-A">HTML element A</a> - anchor.
	 * <p>
	 * This is an {@linkplain HTMLElements#getInlineLevelElementNames() inline-level} element.
	 * <p>
	 * The end tag of this element is {@linkplain HTMLElements#getEndTagRequiredElementNames() required}.
	 */
	public static final String A="a";

	/**
	 * <a target="_blank" href="http://www.w3.org/TR/html401/struct/text.html#edef-ABBR">HTML element ABBR</a> - abbreviated form (e.g., WWW, HTTP, etc.).
	 * <p>
	 * This is an {@linkplain HTMLElements#getInlineLevelElementNames() inline-level} element.
	 * <p>
	 * The end tag of this element is {@linkplain HTMLElements#getEndTagRequiredElementNames() required}.
	 */
	public static final String ABBR="abbr";

	/**
	 * <a target="_blank" href="http://www.w3.org/TR/html401/struct/text.html#edef-ACRONYM">HTML element ACRONYM</a> - acronym.
	 * <p>
	 * This is an {@linkplain HTMLElements#getInlineLevelElementNames() inline-level} element.
	 * <p>
	 * The end tag of this element is {@linkplain HTMLElements#getEndTagRequiredElementNames() required}.
	 */
	public static final String ACRONYM="acronym";

	/**
	 * <a target="_blank" href="http://www.w3.org/TR/html401/struct/global.html#edef-ADDRESS">HTML element ADDRESS</a> - information on author.
	 * <p>
	 * This is a {@linkplain HTMLElements#getBlockLevelElementNames() block-level} element.
	 * <p>
	 * The end tag of this element is {@linkplain HTMLElements#getEndTagRequiredElementNames() required}.
	 */
	public static final String ADDRESS="address";

	/**
	 * <a target="_blank" href="http://www.w3.org/TR/html401/struct/objects.html#edef-APPLET">HTML element APPLET</a> - Java applet.
	 * <p>
	 * This is an {@linkplain HTMLElements#getInlineLevelElementNames() inline-level} element.
	 * <p>
	 * The end tag of this element is {@linkplain HTMLElements#getEndTagRequiredElementNames() required}.
	 * <p>
	 * This element is <a target="blank" href="http://www.w3.org/TR/html401/conform.html#deprecated">deprecated</a> in HTML 4.01.
	 * (see {@link HTMLElements#getDeprecatedElementNames()})
	 */
	public static final String APPLET="applet";

	/**
	 * <a target="_blank" href="http://www.w3.org/TR/html401/struct/objects.html#edef-AREA">HTML element AREA</a> - client-side image map area.
	 * <p>
	 * The end tag of this element is {@linkplain HTMLElements#getEndTagForbiddenElementNames() forbidden}.
	 */
	public static final String AREA="area";

	/**
	 * <a target="_blank" href="http://dev.w3.org/html5/spec/Overview.html#the-article-element">HTML element article</a> - self-contained syndicatable or reusable composition.
	 * <p>
	 * This is a {@linkplain HTMLElements#getBlockLevelElementNames() block-level} element.
	 * <p>
	 * The end tag of this element is {@linkplain HTMLElements#getEndTagRequiredElementNames() required}.
	 */
	public static final String ARTICLE="article";

	/**
	 * <a target="_blank" href="http://dev.w3.org/html5/spec/Overview.html#the-aside-element">HTML element aside</a> - sidebar for tangentially related content.
	 * <p>
	 * This is a {@linkplain HTMLElements#getBlockLevelElementNames() block-level} element.
	 * <p>
	 * The end tag of this element is {@linkplain HTMLElements#getEndTagRequiredElementNames() required}.
	 */
	public static final String ASIDE="aside";

	/**
	 * <a target="_blank" href="http://dev.w3.org/html5/spec/Overview.html#the-audio-element">HTML element audio</a> - audio player.
	 * <p>
	 * The end tag of this element is {@linkplain HTMLElements#getEndTagRequiredElementNames() required}.
	 */
	public static final String AUDIO="audio";

	/**
	 * <a target="_blank" href="http://www.w3.org/TR/html401/present/graphics.html#edef-B">HTML element B</a> - bold text style.
	 * <p>
	 * This is an {@linkplain HTMLElements#getInlineLevelElementNames() inline-level} element.
	 * <p>
	 * The end tag of this element is {@linkplain HTMLElements#getEndTagRequiredElementNames() required}.
	 */
	public static final String B="b";

	/**
	 * <a target="_blank" href="http://www.w3.org/TR/html401/struct/links.html#edef-BASE">HTML element BASE</a> - document base URI.
	 * <p>
	 * The end tag of this element is {@linkplain HTMLElements#getEndTagForbiddenElementNames() forbidden}.
	 */
	public static final String BASE="base";

	/**
	 * <a target="_blank" href="http://www.w3.org/TR/html401/present/graphics.html#edef-BASEFONT">HTML element BASEFONT</a> - base font size.
	 * <p>
	 * This is an {@linkplain HTMLElements#getInlineLevelElementNames() inline-level} element.
	 * <p>
	 * The end tag of this element is {@linkplain HTMLElements#getEndTagForbiddenElementNames() forbidden}.
 	 * <p>
	 * This element is <a target="blank" href="http://www.w3.org/TR/html401/conform.html#deprecated">deprecated</a> in HTML 4.01.
	 * (see {@link HTMLElements#getDeprecatedElementNames()})
	 */
	public static final String BASEFONT="basefont";

	/**
	 * <a target="_blank" href="http://dev.w3.org/html5/spec/Overview.html#the-bdi-element">HTML element bdi</a> - text directionality isolation.
	 * <p>
	 * This is an {@linkplain HTMLElements#getInlineLevelElementNames() inline-level} element.
	 * <p>
	 * The end tag of this element is {@linkplain HTMLElements#getEndTagRequiredElementNames() required}.
	 */
	public static final String BDI="bdi";

	/**
	 * <a target="_blank" href="http://www.w3.org/TR/html401/struct/dirlang.html#edef-BDO">HTML element BDO</a> - I18N BiDi over-ride.
	 * <p>
	 * This is an {@linkplain HTMLElements#getInlineLevelElementNames() inline-level} element.
	 * <p>
	 * The end tag of this element is {@linkplain HTMLElements#getEndTagRequiredElementNames() required}.
	 */
	public static final String BDO="bdo";

	/**
	 * <a target="_blank" href="http://www.w3.org/TR/html401/present/graphics.html#edef-BIG">HTML element BIG</a> - large text style.
	 * <p>
	 * This is an {@linkplain HTMLElements#getInlineLevelElementNames() inline-level} element.
	 * <p>
	 * The end tag of this element is {@linkplain HTMLElements#getEndTagRequiredElementNames() required}.
	 */
	public static final String BIG="big";

	/**
	 * <a target="_blank" href="http://www.w3.org/TR/html401/struct/text.html#edef-BLOCKQUOTE">HTML element BLOCKQUOTE</a> - long quotation.
	 * <p>
	 * This is a {@linkplain HTMLElements#getBlockLevelElementNames() block-level} element.
	 * <p>
	 * The end tag of this element is {@linkplain HTMLElements#getEndTagRequiredElementNames() required}.
	 */
	public static final String BLOCKQUOTE="blockquote";

	/**
	 * <a target="_blank" href="http://www.w3.org/TR/html401/struct/global.html#edef-BODY">HTML element BODY</a> - document body.
	 * <p>
	 * The start tag of this element is {@linkplain HTMLElements#getStartTagOptionalElementNames() optional}.
	 * <p>
	 * The end tag of this element is {@linkplain HTMLElements#getEndTagOptionalElementNames() optional}:
	 * <table class="CompactDL" cellspacing="0">
	 *  <tr>
	 *   <td title="Start tags that terminate this element"><a href="HTMLElements.html#getTerminatingStartTagNames(java.lang.String)">Terminating start tags</a>:
	 *   <td>(none)
	 *  <tr>
	 *   <td title="End tags that terminate this element"><a href="HTMLElements.html#getTerminatingEndTagNames(java.lang.String)">Terminating end tags</a>:
	 *   <td>{@link #BODY}, {@link #HTML}
	 *  <tr>
	 *   <td title="Elements that can be nested inside this element without terminating it"><a href="HTMLElements#getNonterminatingElementNames(java.lang.String)">Nonterminating elements</a>:
	 *   <td>{@link #HTML}
	 * </table>
	 * <p>
	 * Note that the {@link #HTML} element is included as a
	 * {@linkplain HTMLElements#getNonterminatingElementNames(String) nonterminating element} in case the source contains
	 * (illegaly) nested HTML elements.
	 */
	public static final String BODY="body";

	/**
	 * <a target="_blank" href="http://www.w3.org/TR/html401/struct/text.html#edef-BR">HTML element BR</a> - forced line break.
	 * <p>
	 * This is an {@linkplain HTMLElements#getInlineLevelElementNames() inline-level} element.
	 * <p>
	 * The end tag of this element is {@linkplain HTMLElements#getEndTagForbiddenElementNames() forbidden}.
	 */
	public static final String BR="br";

	/**
	 * <a target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#edef-BUTTON">HTML element BUTTON</a> - push button.
	 * <p>
	 * This is an {@linkplain HTMLElements#getInlineLevelElementNames() inline-level} element.
	 * <p>
	 * The end tag of this element is {@linkplain HTMLElements#getEndTagRequiredElementNames() required}.
	 */
	public static final String BUTTON="button";

	/**
	 * <a target="_blank" href="http://dev.w3.org/html5/spec/Overview.html#the-article-element">HTML element canvas</a> - scriptable bitmap canvas.
	 * <p>
	 * The end tag of this element is {@linkplain HTMLElements#getEndTagRequiredElementNames() required}.
	 */
	public static final String CANVAS="canvas";

	/**
	 * <a target="_blank" href="http://www.w3.org/TR/html401/struct/tables.html#edef-CAPTION">HTML element CAPTION</a> - table caption.
	 * <p>
	 * The end tag of this element is {@linkplain HTMLElements#getEndTagRequiredElementNames() required}.
	 */
	public static final String CAPTION="caption";

	/**
	 * <a target="_blank" href="http://www.w3.org/TR/html401/present/graphics.html#edef-CENTER">HTML element CENTER</a> - shorthand for DIV align=center.
	 * <p>
	 * This is a {@linkplain HTMLElements#getBlockLevelElementNames() block-level} element.
	 * <p>
	 * The end tag of this element is {@linkplain HTMLElements#getEndTagRequiredElementNames() required}.
 	 * <p>
	 * This element is <a target="blank" href="http://www.w3.org/TR/html401/conform.html#deprecated">deprecated</a> in HTML 4.01.
	 * (see {@link HTMLElements#getDeprecatedElementNames()})
	 */
	public static final String CENTER="center";

	/**
	 * <a target="_blank" href="http://www.w3.org/TR/html401/struct/text.html#edef-CITE">HTML element CITE</a> - citation.
	 * <p>
	 * This is an {@linkplain HTMLElements#getInlineLevelElementNames() inline-level} element.
	 * <p>
	 * The end tag of this element is {@linkplain HTMLElements#getEndTagRequiredElementNames() required}.
	 */
	public static final String CITE="cite";

	/**
	 * <a target="_blank" href="http://www.w3.org/TR/html401/struct/text.html#edef-CODE">HTML element CODE</a> - computer code fragment.
	 * <p>
	 * This is an {@linkplain HTMLElements#getInlineLevelElementNames() inline-level} element.
	 * <p>
	 * The end tag of this element is {@linkplain HTMLElements#getEndTagRequiredElementNames() required}.
	 */
	public static final String CODE="code";

	/**
	 * <a target="_blank" href="http://www.w3.org/TR/html401/struct/tables.html#edef-COL">HTML element COL</a> - table column.
	 * <p>
	 * The end tag of this element is {@linkplain HTMLElements#getEndTagForbiddenElementNames() forbidden}.
	 */
	public static final String COL="col";

	/**
	 * <a target="_blank" href="http://www.w3.org/TR/html401/struct/tables.html#edef-COLGROUP">HTML element COLGROUP</a> - table column group.
	 * <p>
	 * The end tag of this element is {@linkplain HTMLElements#getEndTagOptionalElementNames() optional}:
	 * <table class="CompactDL" cellspacing="0">
	 *  <tr>
	 *   <td title="Start tags that terminate this element"><a href="HTMLElements.html#getTerminatingStartTagNames(java.lang.String)">Terminating start tags</a>:
	 *   <td>{@link #COLGROUP}, {@link #TBODY}, {@link #TFOOT}, {@link #THEAD}, {@link #TR}
	 *  <tr>
	 *   <td title="End tags that terminate this element"><a href="HTMLElements.html#getTerminatingEndTagNames(java.lang.String)">Terminating end tags</a>:
	 *   <td>{@link #COLGROUP}, {@link #TABLE}
	 *  <tr>
	 *   <td title="Elements that can be nested inside this element without terminating it"><a href="HTMLElements#getNonterminatingElementNames(java.lang.String)">Nonterminating elements</a>:
	 *   <td>{@link #TABLE}
	 * </table>
	 */
	public static final String COLGROUP="colgroup";

	/**
	 * <a target="_blank" href="http://dev.w3.org/html5/spec/Overview.html#the-command">HTML element command</a> - menu command.
	 * <p>
	 * The end tag of this element is {@linkplain HTMLElements#getEndTagForbiddenElementNames() forbidden}.
	 */
	public static final String COMMAND="command";

	/**
	 * <a target="_blank" href="http://dev.w3.org/html5/spec/forms.html#the-datalist-element">HTML element datalist</a> - container for options for combo box control.
	 * <p>
	 * The end tag of this element is {@linkplain HTMLElements#getEndTagRequiredElementNames() required}.
	 */
	public static final String DATALIST="datalist";

	/**
	 * <a target="_blank" href="http://www.w3.org/TR/html401/struct/lists.html#edef-DD">HTML element DD</a> - definition description.
	 * <p>
	 * The end tag of this element is {@linkplain HTMLElements#getEndTagOptionalElementNames() optional}:
	 * <table class="CompactDL" cellspacing="0">
	 *  <tr>
	 *   <td title="Start tags that terminate this element"><a href="HTMLElements.html#getTerminatingStartTagNames(java.lang.String)">Terminating start tags</a>:
	 *   <td>{@link #DD}, {@link #DT}
	 *  <tr>
	 *   <td title="End tags that terminate this element"><a href="HTMLElements.html#getTerminatingEndTagNames(java.lang.String)">Terminating end tags</a>:
	 *   <td>{@link #DD}, {@link #DL}
	 *  <tr>
	 *   <td title="Elements that can be nested inside this element without terminating it"><a href="HTMLElements#getNonterminatingElementNames(java.lang.String)">Nonterminating elements</a>:
	 *   <td>{@link #DL}
	 * </table>
	 */
	public static final String DD="dd";

	/**
	 * <a target="_blank" href="http://www.w3.org/TR/html401/struct/text.html#edef-DEL">HTML element DEL</a> - deleted text.
	 * <p>
	 * This is an {@linkplain HTMLElements#getInlineLevelElementNames() inline-level} element.
	 * <p>
	 * The end tag of this element is {@linkplain HTMLElements#getEndTagRequiredElementNames() required}.
	 */
	public static final String DEL="del";

	/**
	 * <a target="_blank" href="http://dev.w3.org/html5/spec/Overview.html#the-details-element">HTML element details</a> - disclosure control for hiding details.
	 * <p>
	 * This is a {@linkplain HTMLElements#getBlockLevelElementNames() block-level} element.
	 * <p>
	 * The end tag of this element is {@linkplain HTMLElements#getEndTagRequiredElementNames() required}.
	 */
	public static final String DETAILS="details";

	/**
	 * <a target="_blank" href="http://www.w3.org/TR/html401/struct/text.html#edef-DFN">HTML element DFN</a> - instance definition.
	 * <p>
	 * This is an {@linkplain HTMLElements#getInlineLevelElementNames() inline-level} element.
	 * <p>
	 * The end tag of this element is {@linkplain HTMLElements#getEndTagRequiredElementNames() required}.
	 */
	public static final String DFN="dfn";

	/**
	 * <a target="_blank" href="http://www.w3.org/TR/html401/struct/lists.html#edef-DIR">HTML element DIR</a> - directory list.
	 * <p>
	 * This is a {@linkplain HTMLElements#getBlockLevelElementNames() block-level} element.
	 * <p>
	 * The end tag of this element is {@linkplain HTMLElements#getEndTagRequiredElementNames() required}.
 	 * <p>
	 * This element is <a target="blank" href="http://www.w3.org/TR/html401/conform.html#deprecated">deprecated</a> in HTML 4.01.
	 * (see {@link HTMLElements#getDeprecatedElementNames()})
	 */
	public static final String DIR="dir";

	/**
	 * <a target="_blank" href="http://www.w3.org/TR/html401/struct/global.html#edef-DIV">HTML element DIV</a> - generic language/style container.
	 * <p>
	 * The end tag of this element is {@linkplain HTMLElements#getEndTagRequiredElementNames() required}.
	 * <p>
	 * This is a {@linkplain HTMLElements#getBlockLevelElementNames() block-level} element.
	 */
	public static final String DIV="div";

	/**
	 * <a target="_blank" href="http://www.w3.org/TR/html401/struct/lists.html#edef-DL">HTML element DL</a> - definition list.
	 * <p>
	 * The end tag of this element is {@linkplain HTMLElements#getEndTagRequiredElementNames() required}.
	 * <p>
	 * This is a {@linkplain HTMLElements#getBlockLevelElementNames() block-level} element.
	 */
	public static final String DL="dl";

	/**
	 * <a target="_blank" href="http://www.w3.org/TR/html401/struct/lists.html#edef-DT">HTML element DT</a> - definition term.
	 * <p>
	 * The end tag of this element is {@linkplain HTMLElements#getEndTagOptionalElementNames() optional}:
	 * <table class="CompactDL" cellspacing="0">
	 *  <tr>
	 *   <td title="Start tags that terminate this element"><a href="HTMLElements.html#getTerminatingStartTagNames(java.lang.String)">Terminating start tags</a>:
	 *   <td>{@link #DD}, {@link #DT}
	 *  <tr>
	 *   <td title="End tags that terminate this element"><a href="HTMLElements.html#getTerminatingEndTagNames(java.lang.String)">Terminating end tags</a>:
	 *   <td>{@link #DL}, {@link #DT}
	 *  <tr>
	 *   <td title="Elements that can be nested inside this element without terminating it"><a href="HTMLElements#getNonterminatingElementNames(java.lang.String)">Nonterminating elements</a>:
	 *   <td>{@link #DL}
	 * </table>
	 */
	public static final String DT="dt";

	/**
	 * <a target="_blank" href="http://www.w3.org/TR/html401/struct/text.html#edef-EM">HTML element EM</a> - emphasis.
	 * <p>
	 * This is an {@linkplain HTMLElements#getInlineLevelElementNames() inline-level} element.
	 * <p>
	 * The end tag of this element is {@linkplain HTMLElements#getEndTagRequiredElementNames() required}.
	 */
	public static final String EM="em";

	/**
	 * <a target="_blank" href="http://dev.w3.org/html5/spec/Overview.html#the-embed-element">HTML element embed</a> - plugin.
	 * <p>
	 * The end tag of this element is {@linkplain HTMLElements#getEndTagForbiddenElementNames() forbidden}.
	 */
	public static final String EMBED="embed";

	/**
	 * <a target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#edef-FIELDSET">HTML element FIELDSET</a> - form control group.
	 * <p>
	 * This is a {@linkplain HTMLElements#getBlockLevelElementNames() block-level} element.
	 * <p>
	 * The end tag of this element is {@linkplain HTMLElements#getEndTagRequiredElementNames() required}.
	 */
	public static final String FIELDSET="fieldset";

	/**
	 * <a target="_blank" href="http://dev.w3.org/html5/spec/Overview.html#the-figcaption-element">HTML element figcaption</a> - caption for figure.
	 * <p>
	 * The end tag of this element is {@linkplain HTMLElements#getEndTagRequiredElementNames() required}.
	 */
	public static final String FIGCAPTION="figcaption";

	/**
	 * <a target="_blank" href="http://dev.w3.org/html5/spec/Overview.html#the-figure-element">HTML element figure</a> - figure with optional caption.
	 * <p>
	 * The end tag of this element is {@linkplain HTMLElements#getEndTagRequiredElementNames() required}.
	 */
	public static final String FIGURE="figure";

	/**
	 * <a target="_blank" href="http://www.w3.org/TR/html401/present/graphics.html#edef-FONT">HTML element FONT</a> - local change to font.
	 * <p>
	 * This is an {@linkplain HTMLElements#getInlineLevelElementNames() inline-level} element.
	 * <p>
	 * The end tag of this element is {@linkplain HTMLElements#getEndTagRequiredElementNames() required}.
 	 * <p>
	 * This element is <a target="blank" href="http://www.w3.org/TR/html401/conform.html#deprecated">deprecated</a> in HTML 4.01.
	 * (see {@link HTMLElements#getDeprecatedElementNames()})
	 */
	public static final String FONT="font";

	/**
	 * <a target="_blank" href="http://dev.w3.org/html5/spec/Overview.html#the-footer-element">HTML element footer</a> - footer for a page or section.
	 * <p>
	 * This is a {@linkplain HTMLElements#getBlockLevelElementNames() block-level} element.
	 * <p>
	 * The end tag of this element is {@linkplain HTMLElements#getEndTagRequiredElementNames() required}.
	 */
	public static final String FOOTER="footer";

	/**
	 * <a target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#edef-FORM">HTML element FORM</a> - interactive form.
	 * <p>
	 * This is a {@linkplain HTMLElements#getBlockLevelElementNames() block-level} element.
	 * <p>
	 * The end tag of this element is {@linkplain HTMLElements#getEndTagRequiredElementNames() required}.
	 */
	public static final String FORM="form";

	/**
	 * <a target="_blank" href="http://www.w3.org/TR/html401/present/frames.html#edef-FRAME">HTML element FRAME</a> - subwindow.
	 * <p>
	 * The end tag of this element is {@linkplain HTMLElements#getEndTagForbiddenElementNames() forbidden}.
	 */
	public static final String FRAME="frame";

	/**
	 * <a target="_blank" href="http://www.w3.org/TR/html401/present/frames.html#edef-FRAMESET">HTML element FRAMESET</a> - window subdivision.
	 * <p>
	 * The end tag of this element is {@linkplain HTMLElements#getEndTagRequiredElementNames() required}.
	 */
	public static final String FRAMESET="frameset";

	/**
	 * <a target="_blank" href="http://www.w3.org/TR/html401/struct/global.html#edef-H1">HTML element H1</a> - heading.
	 * <p>
	 * This is a {@linkplain HTMLElements#getBlockLevelElementNames() block-level} element.
	 * <p>
	 * The end tag of this element is {@linkplain HTMLElements#getEndTagRequiredElementNames() required}.
	 */
	public static final String H1="h1";

	/**
	 * <a target="_blank" href="http://www.w3.org/TR/html401/struct/global.html#edef-H2">HTML element H2</a> - heading.
	 * <p>
	 * This is a {@linkplain HTMLElements#getBlockLevelElementNames() block-level} element.
	 * <p>
	 * The end tag of this element is {@linkplain HTMLElements#getEndTagRequiredElementNames() required}.
	 */
	public static final String H2="h2";

	/**
	 * <a target="_blank" href="http://www.w3.org/TR/html401/struct/global.html#edef-H3">HTML element H3</a> - heading.
	 * <p>
	 * This is a {@linkplain HTMLElements#getBlockLevelElementNames() block-level} element.
	 * <p>
	 * The end tag of this element is {@linkplain HTMLElements#getEndTagRequiredElementNames() required}.
	 */
	public static final String H3="h3";

	/**
	 * <a target="_blank" href="http://www.w3.org/TR/html401/struct/global.html#edef-H4">HTML element H4</a> - heading.
	 * <p>
	 * This is a {@linkplain HTMLElements#getBlockLevelElementNames() block-level} element.
	 * <p>
	 * The end tag of this element is {@linkplain HTMLElements#getEndTagRequiredElementNames() required}.
	 */
	public static final String H4="h4";

	/**
	 * <a target="_blank" href="http://www.w3.org/TR/html401/struct/global.html#edef-H5">HTML element H5</a> - heading.
	 * <p>
	 * This is a {@linkplain HTMLElements#getBlockLevelElementNames() block-level} element.
	 * <p>
	 * The end tag of this element is {@linkplain HTMLElements#getEndTagRequiredElementNames() required}.
	 */
	public static final String H5="h5";

	/**
	 * <a target="_blank" href="http://www.w3.org/TR/html401/struct/global.html#edef-H6">HTML element H6</a> - heading.
	 * <p>
	 * This is a {@linkplain HTMLElements#getBlockLevelElementNames() block-level} element.
	 * <p>
	 * The end tag of this element is {@linkplain HTMLElements#getEndTagRequiredElementNames() required}.
	 */
	public static final String H6="h6";

	/**
	 * <a target="_blank" href="http://www.w3.org/TR/html401/struct/global.html#edef-HEAD">HTML element HEAD</a> - document head.
	 * <p>
	 * The start tag of this element is {@linkplain HTMLElements#getStartTagOptionalElementNames() optional}.
	 * <p>
	 * The end tag of this element is {@linkplain HTMLElements#getEndTagOptionalElementNames() optional}:
	 * <table class="CompactDL" cellspacing="0">
	 *  <tr>
	 *   <td title="Start tags that terminate this element"><a href="HTMLElements.html#getTerminatingStartTagNames(java.lang.String)">Terminating start tags</a>:
	 *   <td>{@link #BODY}, {@link #FRAMESET}
	 *  <tr>
	 *   <td title="End tags that terminate this element"><a href="HTMLElements.html#getTerminatingEndTagNames(java.lang.String)">Terminating end tags</a>:
	 *   <td>{@link #HEAD}, {@link #HTML}
	 *  <tr>
	 *   <td title="Elements that can be nested inside this element without terminating it"><a href="HTMLElements#getNonterminatingElementNames(java.lang.String)">Nonterminating elements</a>:
	 *   <td>(none)
	 * </table>
	 */
	public static final String HEAD="head";

	/**
	 * <a target="_blank" href="http://dev.w3.org/html5/spec/Overview.html#the-header-element">HTML element header</a> - introductory or navigational aids for a page or section.
	 * <p>
	 * This is a {@linkplain HTMLElements#getBlockLevelElementNames() block-level} element.
	 * <p>
	 * The end tag of this element is {@linkplain HTMLElements#getEndTagRequiredElementNames() required}.
	 */
	public static final String HEADER="header";

	/**
	 * <a target="_blank" href="http://dev.w3.org/html5/spec/Overview.html#the-hgroup-element">HTML element hgroup</a> - heading group.
	 * <p>
	 * This is a {@linkplain HTMLElements#getBlockLevelElementNames() block-level} element.
	 * <p>
	 * The end tag of this element is {@linkplain HTMLElements#getEndTagRequiredElementNames() required}.
	 */
	public static final String HGROUP="hgroup";

	/**
	 * <a target="_blank" href="http://www.w3.org/TR/html401/present/graphics.html#edef-HR">HTML element HR</a> - horizontal rule.
	 * <p>
	 * This is a {@linkplain HTMLElements#getBlockLevelElementNames() block-level} element.
	 * <p>
	 * The end tag of this element is {@linkplain HTMLElements#getEndTagForbiddenElementNames() forbidden}.
	 */
	public static final String HR="hr";

	/**
	 * <a target="_blank" href="http://www.w3.org/TR/html401/struct/global.html#edef-HTML">HTML element HTML</a> - document root element.
	 * <p>
	 * The start tag of this element is {@linkplain HTMLElements#getStartTagOptionalElementNames() optional}.
	 * <p>
	 * The end tag of this element is {@linkplain HTMLElements#getEndTagOptionalElementNames() optional}:
	 * <table class="CompactDL" cellspacing="0">
	 *  <tr>
	 *   <td title="Start tags that terminate this element"><a href="HTMLElements.html#getTerminatingStartTagNames(java.lang.String)">Terminating start tags</a>:
	 *   <td>(none)
	 *  <tr>
	 *   <td title="End tags that terminate this element"><a href="HTMLElements.html#getTerminatingEndTagNames(java.lang.String)">Terminating end tags</a>:
	 *   <td>{@link #HTML}
	 *  <tr>
	 *   <td title="Elements that can be nested inside this element without terminating it"><a href="HTMLElements#getNonterminatingElementNames(java.lang.String)">Nonterminating elements</a>:
	 *   <td>{@link #HTML}
	 * </table>
	 * <p>
	 * Note that the {@link #HTML} element is included as a
	 * {@linkplain HTMLElements#getNonterminatingElementNames(String) nonterminating element} in case the source contains
	 * (illegaly) nested HTML elements.
	 */
	public static final String HTML="html";

	/**
	 * <a target="_blank" href="http://www.w3.org/TR/html401/present/graphics.html#edef-I">HTML element I</a> - italic text style.
	 * <p>
	 * This is an {@linkplain HTMLElements#getInlineLevelElementNames() inline-level} element.
	 * <p>
	 * The end tag of this element is {@linkplain HTMLElements#getEndTagRequiredElementNames() required}.
	 */
	public static final String I="i";

	/**
	 * <a target="_blank" href="http://www.w3.org/TR/html401/present/frames.html#edef-IFRAME">HTML element IFRAME</a> - inline subwindow.
	 * <p>
	 * This is an {@linkplain HTMLElements#getInlineLevelElementNames() inline-level} element.
	 * <p>
	 * The end tag of this element is {@linkplain HTMLElements#getEndTagRequiredElementNames() required}.
	 */
	public static final String IFRAME="iframe";

	/**
	 * <a target="_blank" href="http://www.w3.org/TR/html401/struct/objects.html#edef-IMG">HTML element IMG</a> - Embedded image.
	 * <p>
	 * This is an {@linkplain HTMLElements#getInlineLevelElementNames() inline-level} element.
	 * <p>
	 * The end tag of this element is {@linkplain HTMLElements#getEndTagForbiddenElementNames() forbidden}.
	 */
	public static final String IMG="img";

	/**
	 * <a target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#edef-INPUT">HTML element INPUT</a> - form control.
	 * <p>
	 * This is an {@linkplain HTMLElements#getInlineLevelElementNames() inline-level} element.
	 * <p>
	 * The end tag of this element is {@linkplain HTMLElements#getEndTagForbiddenElementNames() forbidden}.
	 */
	public static final String INPUT="input";

	/**
	 * <a target="_blank" href="http://www.w3.org/TR/html401/struct/text.html#edef-INS">HTML element INS</a> - inserted text.
	 * <p>
	 * This is an {@linkplain HTMLElements#getInlineLevelElementNames() inline-level} element.
	 * <p>
	 * The end tag of this element is {@linkplain HTMLElements#getEndTagRequiredElementNames() required}.
	 */
	public static final String INS="ins";

	/**
	 * <a target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#edef-ISINDEX">HTML element ISINDEX</a> - single line prompt.
	 * <p>
	 * This is a {@linkplain HTMLElements#getBlockLevelElementNames() block-level} element.
	 * <p>
	 * The end tag of this element is {@linkplain HTMLElements#getEndTagForbiddenElementNames() forbidden}.
 	 * <p>
	 * This element is <a target="blank" href="http://www.w3.org/TR/html401/conform.html#deprecated">deprecated</a> in HTML 4.01.
	 * (see {@link HTMLElements#getDeprecatedElementNames()})
	 */
	public static final String ISINDEX="isindex";

	/**
	 * <a target="_blank" href="http://www.w3.org/TR/html401/struct/text.html#edef-KBD">HTML element KBD</a> - text to be entered by the user.
	 * <p>
	 * This is an {@linkplain HTMLElements#getInlineLevelElementNames() inline-level} element.
	 * <p>
	 * The end tag of this element is {@linkplain HTMLElements#getEndTagRequiredElementNames() required}.
	 */
	public static final String KBD="kbd";

	/**
	 * <a target="_blank" href="http://dev.w3.org/html5/spec/Overview.html#the-keygen-element">HTML element keygen</a> - cryptographic key-pair generator form control.
	 * <p>
	 * This is an {@linkplain HTMLElements#getInlineLevelElementNames() inline-level} element.
	 * <p>
	 * The end tag of this element is {@linkplain HTMLElements#getEndTagForbiddenElementNames() forbidden}.
	 */
	public static final String KEYGEN="keygen";

	/**
	 * <a target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#edef-LABEL">HTML element LABEL</a> - form field label text.
	 * <p>
	 * This is an {@linkplain HTMLElements#getInlineLevelElementNames() inline-level} element.
	 * <p>
	 * The end tag of this element is {@linkplain HTMLElements#getEndTagRequiredElementNames() required}.
	 */
	public static final String LABEL="label";

	/**
	 * <a target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#edef-LEGEND">HTML element LEGEND</a> - fieldset legend.
	 * <p>
	 * The end tag of this element is {@linkplain HTMLElements#getEndTagRequiredElementNames() required}.
	 */
	public static final String LEGEND="legend";

	/**
	 * <a target="_blank" href="http://www.w3.org/TR/html401/struct/lists.html#edef-LI">HTML element LI</a> - list item.
	 * <p>
	 * The end tag of this element is {@linkplain HTMLElements#getEndTagOptionalElementNames() optional}:
	 * <table class="CompactDL" cellspacing="0">
	 *  <tr>
	 *   <td title="Start tags that terminate this element"><a href="HTMLElements.html#getTerminatingStartTagNames(java.lang.String)">Terminating start tags</a>:
	 *   <td>{@link #LI}
	 *  <tr>
	 *   <td title="End tags that terminate this element"><a href="HTMLElements.html#getTerminatingEndTagNames(java.lang.String)">Terminating end tags</a>:
	 *   <td>{@link #LI}, {@link #OL}, {@link #UL}
	 *  <tr>
	 *   <td title="Elements that can be nested inside this element without terminating it"><a href="HTMLElements#getNonterminatingElementNames(java.lang.String)">Nonterminating elements</a>:
	 *   <td>{@link #OL}, {@link #UL}
	 * </table>
	 */
	public static final String LI="li";

	/**
	 * <a target="_blank" href="http://www.w3.org/TR/html401/struct/links.html#edef-LINK">HTML element LINK</a> - a media-independent link.
	 * <p>
	 * The end tag of this element is {@linkplain HTMLElements#getEndTagForbiddenElementNames() forbidden}.
	 */
	public static final String LINK="link";

	/**
	 * <a target="_blank" href="http://www.w3.org/TR/html401/struct/objects.html#edef-MAP">HTML element MAP</a> - client-side image map.
	 * <p>
	 * This is an {@linkplain HTMLElements#getInlineLevelElementNames() inline-level} element.
	 * <p>
	 * The end tag of this element is {@linkplain HTMLElements#getEndTagRequiredElementNames() required}.
	 */
	public static final String MAP="map";

	/**
	 * <a target="_blank" href="http://dev.w3.org/html5/spec/Overview.html#the-mark-element">HTML element mark</a> - highlight.
	 * <p>
	 * This is an {@linkplain HTMLElements#getInlineLevelElementNames() inline-level} element.
	 * <p>
	 * The end tag of this element is {@linkplain HTMLElements#getEndTagRequiredElementNames() required}.
	 */
	public static final String MARK="mark";

	/**
	 * <a target="_blank" href="http://www.w3.org/TR/html401/struct/lists.html#edef-MENU">HTML element MENU</a> - menu list.
	 * <p>
	 * This is a {@linkplain HTMLElements#getBlockLevelElementNames() block-level} element.
	 * <p>
	 * The end tag of this element is {@linkplain HTMLElements#getEndTagRequiredElementNames() required}.
 	 * <p>
	 * This element is <a target="blank" href="http://www.w3.org/TR/html401/conform.html#deprecated">deprecated</a> in HTML 4.01.
	 * (see {@link HTMLElements#getDeprecatedElementNames()})
	 */
	public static final String MENU="menu";

	/**
	 * <a target="_blank" href="http://www.w3.org/TR/html401/struct/global.html#edef-META">HTML element META</a> - generic metainformation.
	 * <p>
	 * The end tag of this element is {@linkplain HTMLElements#getEndTagForbiddenElementNames() forbidden}.
	 */
	public static final String META="meta";

	/**
	 * <a target="_blank" href="http://dev.w3.org/html5/spec/Overview.html#the-meter-element">HTML element meter</a> - gauge.
	 * <p>
	 * This is an {@linkplain HTMLElements#getInlineLevelElementNames() inline-level} element.
	 * <p>
	 * The end tag of this element is {@linkplain HTMLElements#getEndTagRequiredElementNames() required}.
	 */
	public static final String METER="meter";

	/**
	 * <a target="_blank" href="http://dev.w3.org/html5/spec/Overview.html#the-nav-element">HTML element nav</a> - section with navigational links.
	 * <p>
	 * This is a {@linkplain HTMLElements#getBlockLevelElementNames() block-level} element.
	 * <p>
	 * The end tag of this element is {@linkplain HTMLElements#getEndTagRequiredElementNames() required}.
	 */
	public static final String NAV="nav";

	/**
	 * <a target="_blank" href="http://www.w3.org/TR/html401/present/frames.html#edef-NOFRAMES">HTML element NOFRAMES</a> - alternate content container for non frame-based rendering.
	 * <p>
	 * This is a {@linkplain HTMLElements#getBlockLevelElementNames() block-level} element.
	 * <p>
	 * The end tag of this element is {@linkplain HTMLElements#getEndTagRequiredElementNames() required}.
	 */
	public static final String NOFRAMES="noframes";

	/**
	 * <a target="_blank" href="http://www.w3.org/TR/html401/interact/scripts.html#edef-NOSCRIPT">HTML element NOSCRIPT</a> - alternate content container for non script-based rendering.
	 * <p>
	 * This is a {@linkplain HTMLElements#getBlockLevelElementNames() block-level} element.
	 * <p>
	 * The end tag of this element is {@linkplain HTMLElements#getEndTagRequiredElementNames() required}.
	 */
	public static final String NOSCRIPT="noscript";

	/**
	 * <a target="_blank" href="http://www.w3.org/TR/html401/struct/objects.html#edef-OBJECT">HTML element OBJECT</a> - generic embedded object.
	 * <p>
	 * This is an {@linkplain HTMLElements#getInlineLevelElementNames() inline-level} element.
	 * <p>
	 * The end tag of this element is {@linkplain HTMLElements#getEndTagRequiredElementNames() required}.
	 */
	public static final String OBJECT="object";

	/**
	 * <a target="_blank" href="http://www.w3.org/TR/html401/struct/lists.html#edef-OL">HTML element OL</a> - ordered list.
	 * <p>
	 * This is a {@linkplain HTMLElements#getBlockLevelElementNames() block-level} element.
	 * <p>
	 * The end tag of this element is {@linkplain HTMLElements#getEndTagRequiredElementNames() required}.
	 */
	public static final String OL="ol";

	/**
	 * <a target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#edef-OPTGROUP">HTML element OPTGROUP</a> - option group.
	 * <p>
	 * The end tag of this element is {@linkplain HTMLElements#getEndTagRequiredElementNames() required}.
	 */
	public static final String OPTGROUP="optgroup";

	/**
	 * <a target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#edef-OPTION">HTML element OPTION</a> - selectable choice.
	 * <p>
	 * The end tag of this element is {@linkplain HTMLElements#getEndTagOptionalElementNames() optional}:
	 * <table class="CompactDL" cellspacing="0">
	 *  <tr>
	 *   <td title="Start tags that terminate this element"><a href="HTMLElements.html#getTerminatingStartTagNames(java.lang.String)">Terminating start tags</a>:
	 *   <td>{@link #OPTGROUP}, {@link #OPTION}
	 *  <tr>
	 *   <td title="End tags that terminate this element"><a href="HTMLElements.html#getTerminatingEndTagNames(java.lang.String)">Terminating end tags</a>:
	 *   <td>{@link #OPTION}, {@link #SELECT}
	 *  <tr>
	 *   <td title="Elements that can be nested inside this element without terminating it"><a href="HTMLElements#getNonterminatingElementNames(java.lang.String)">Nonterminating elements</a>:
	 *   <td>(none)
	 * </table>
	 */
	public static final String OPTION="option";

	/**
	 * <a target="_blank" href="http://dev.w3.org/html5/spec/Overview.html#the-output-element">HTML element output</a> - calculated output value.
	 * <p>
	 * This is an {@linkplain HTMLElements#getInlineLevelElementNames() inline-level} element.
	 * <p>
	 * The end tag of this element is {@linkplain HTMLElements#getEndTagRequiredElementNames() required}.
	 */
	public static final String OUTPUT="output";

	/**
	 * <a target="_blank" href="http://www.w3.org/TR/html401/struct/text.html#edef-P">HTML element P</a> - paragraph.
	 * <p>
	 * This is a {@linkplain HTMLElements#getBlockLevelElementNames() block-level} element.
	 * <p>
	 * The end tag of this element is {@linkplain HTMLElements#getEndTagOptionalElementNames() optional}:
	 * <table class="CompactDL" cellspacing="0">
	 *  <tr>
	 *   <td title="Start tags that terminate this element"><a href="HTMLElements.html#getTerminatingStartTagNames(java.lang.String)">Terminating start tags</a>:
	 *   <td>{@linkplain HTMLElements#getBlockLevelElementNames() All block-level element names},<br />
	 *    {@link #DD}, {@link #DT}, {@link #LI}, {@link #TD}, {@link #TH}
	 *  <tr>
	 *   <td title="End tags that terminate this element"><a href="HTMLElements.html#getTerminatingEndTagNames(java.lang.String)">Terminating end tags</a>:
	 *   <td>{@linkplain HTMLElements#getBlockLevelElementNames() All block-level element names},<br />
	 *    {@link #BODY}, {@link #CAPTION}, {@link #DD}, {@link #DT}, {@link #HTML}, {@link #LEGEND}, {@link #TD}, {@link #TH},
	 *    {@link #TBODY}, {@link #TFOOT}, {@link #THEAD}, {@link #TR}
	 *  <tr>
	 *   <td title="Elements that can be nested inside this element without terminating it"><a href="HTMLElements#getNonterminatingElementNames(java.lang.String)">Nonterminating elements</a>:
	 *   <td>(none)
	 * </table>
	 * <p>
	 * The <a target="_blank" href="http://www.w3.org/TR/html401/struct/text.html#edef-P">definition of this element in the HTML 4.01 specification</a>
	 * explicitly states that the <code>P</code> element cannot contain {@linkplain HTMLElements#getBlockLevelElementNames() block-level}
	 * elements.
	 * Despite this, all of the popular browsers (in at least some modes of operation) allow <code>P</code> elements to enclose 
	 * {@link #TABLE} elements, which are also block-level elements.
	 * <p>
	 * It is possible to make this parser compatible with this incorrect behaviour by executing the following code:
	 * <pre>
	 * {@link HTMLElements#getTerminatingStartTagNames(String) HTMLElements.getTerminatingStartTagNames}(HTMLElementName.P).remove(HTMLElementName.TABLE);
	 * {@link HTMLElements#getNonterminatingElementNames(String) HTMLElements.getNonterminatingElementNames}(HTMLElementName.P).add(HTMLElementName.TABLE);</pre>
	 */
	public static final String P="p";

	/**
	 * <a target="_blank" href="http://www.w3.org/TR/html401/struct/objects.html#edef-PARAM">HTML element PARAM</a> - named property value.
	 * <p>
	 * The end tag of this element is {@linkplain HTMLElements#getEndTagForbiddenElementNames() forbidden}.
	 */
	public static final String PARAM="param";

	/**
	 * <a target="_blank" href="http://www.w3.org/TR/html401/struct/text.html#edef-PRE">HTML element PRE</a> - preformatted text.
	 * <p>
	 * This is a {@linkplain HTMLElements#getBlockLevelElementNames() block-level} element.
	 * <p>
	 * The end tag of this element is {@linkplain HTMLElements#getEndTagRequiredElementNames() required}.
	 */
	public static final String PRE="pre";

	/**
	 * <a target="_blank" href="http://dev.w3.org/html5/spec/Overview.html#the-progress-element">HTML element progress</a> - progress bar.
	 * <p>
	 * This is an {@linkplain HTMLElements#getInlineLevelElementNames() inline-level} element.
	 * <p>
	 * The end tag of this element is {@linkplain HTMLElements#getEndTagRequiredElementNames() required}.
	 */
	public static final String PROGRESS="progress";

	/**
	 * <a target="_blank" href="http://www.w3.org/TR/html401/struct/text.html#edef-Q">HTML element Q</a> - short inline quotation.
	 * <p>
	 * This is an {@linkplain HTMLElements#getInlineLevelElementNames() inline-level} element.
	 * <p>
	 * The end tag of this element is {@linkplain HTMLElements#getEndTagRequiredElementNames() required}.
	 */
	public static final String Q="q";

	/**
	 * <a target="_blank" href="http://dev.w3.org/html5/spec/Overview.html#the-rp-element">HTML element rp</a> - parenthesis for ruby annotation text.
	 * <p>
	 * This is an {@linkplain HTMLElements#getInlineLevelElementNames() inline-level} element.
	 * <p>
	 * The end tag of this element is {@linkplain HTMLElements#getEndTagOptionalElementNames() optional}:
	 * <table class="CompactDL" cellspacing="0">
	 *  <tr>
	 *   <td title="Start tags that terminate this element"><a href="HTMLElements.html#getTerminatingStartTagNames(java.lang.String)">Terminating start tags</a>:
	 *   <td>{@link #RP}, {@link #RT}
	 *  <tr>
	 *   <td title="End tags that terminate this element"><a href="HTMLElements.html#getTerminatingEndTagNames(java.lang.String)">Terminating end tags</a>:
	 *   <td>{@link #RUBY}
	 *  <tr>
	 *   <td title="Elements that can be nested inside this element without terminating it"><a href="HTMLElements#getNonterminatingElementNames(java.lang.String)">Nonterminating elements</a>:
	 *   <td>(none)
	 * </table>
	 */
	public static final String RP="rp";

	/**
	 * <a target="_blank" href="http://dev.w3.org/html5/spec/Overview.html#the-rt-element">HTML element rt</a> - ruby annotation text.
	 * <p>
	 * This is an {@linkplain HTMLElements#getInlineLevelElementNames() inline-level} element.
	 * <p>
	 * The end tag of this element is {@linkplain HTMLElements#getEndTagOptionalElementNames() optional}:
	 * <table class="CompactDL" cellspacing="0">
	 *  <tr>
	 *   <td title="Start tags that terminate this element"><a href="HTMLElements.html#getTerminatingStartTagNames(java.lang.String)">Terminating start tags</a>:
	 *   <td>{@link #RP}, {@link #RT}
	 *  <tr>
	 *   <td title="End tags that terminate this element"><a href="HTMLElements.html#getTerminatingEndTagNames(java.lang.String)">Terminating end tags</a>:
	 *   <td>{@link #RUBY}
	 *  <tr>
	 *   <td title="Elements that can be nested inside this element without terminating it"><a href="HTMLElements#getNonterminatingElementNames(java.lang.String)">Nonterminating elements</a>:
	 *   <td>(none)
	 * </table>
	 */
	public static final String RT="rt";

	/**
	 * <a target="_blank" href="http://dev.w3.org/html5/spec/Overview.html#the-ruby-element">HTML element ruby</a> - ruby annotation(s).
	 * <p>
	 * This is an {@linkplain HTMLElements#getInlineLevelElementNames() inline-level} element.
	 * <p>
	 * The end tag of this element is {@linkplain HTMLElements#getEndTagRequiredElementNames() required}.
	 */
	public static final String RUBY="ruby";

	/**
	 * <a target="_blank" href="http://www.w3.org/TR/html401/present/graphics.html#edef-S">HTML element S</a> - strike-through text style.
	 * <p>
	 * This is an {@linkplain HTMLElements#getInlineLevelElementNames() inline-level} element.
	 * <p>
	 * The end tag of this element is {@linkplain HTMLElements#getEndTagRequiredElementNames() required}.
 	 * <p>
	 * This element is <a target="blank" href="http://www.w3.org/TR/html401/conform.html#deprecated">deprecated</a> in HTML 4.01.
	 * (see {@link HTMLElements#getDeprecatedElementNames()})
	 */
	public static final String S="s";

	/**
	 * <a target="_blank" href="http://www.w3.org/TR/html401/struct/text.html#edef-SAMP">HTML element SAMP</a> - sample program output, scripts, etc..
	 * <p>
	 * This is an {@linkplain HTMLElements#getInlineLevelElementNames() inline-level} element.
	 * <p>
	 * The end tag of this element is {@linkplain HTMLElements#getEndTagRequiredElementNames() required}.
	 */
	public static final String SAMP="samp";

	/**
	 * <a target="_blank" href="http://www.w3.org/TR/html401/interact/scripts.html#edef-SCRIPT">HTML element SCRIPT</a> - script statements.
	 * <p>
	 * This is an {@linkplain HTMLElements#getInlineLevelElementNames() inline-level} element.
	 * <p>
	 * The end tag of this element is {@linkplain HTMLElements#getEndTagRequiredElementNames() required}.
	 */
	public static final String SCRIPT="script";

	/**
	 * <a target="_blank" href="http://dev.w3.org/html5/spec/Overview.html#the-section-element">HTML element section</a> - generic document or application section.
	 * <p>
	 * This is a {@linkplain HTMLElements#getBlockLevelElementNames() block-level} element.
	 * <p>
	 * The end tag of this element is {@linkplain HTMLElements#getEndTagRequiredElementNames() required}.
	 */
	public static final String SECTION="section";

	/**
	 * <a target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#edef-SELECT">HTML element SELECT</a> - option selector.
	 * <p>
	 * This is an {@linkplain HTMLElements#getInlineLevelElementNames() inline-level} element.
	 * <p>
	 * The end tag of this element is {@linkplain HTMLElements#getEndTagRequiredElementNames() required}.
	 */
	public static final String SELECT="select";

	/**
	 * <a target="_blank" href="http://www.w3.org/TR/html401/present/graphics.html#edef-SMALL">HTML element SMALL</a> - small text style.
	 * <p>
	 * This is an {@linkplain HTMLElements#getInlineLevelElementNames() inline-level} element.
	 * <p>
	 * The end tag of this element is {@linkplain HTMLElements#getEndTagRequiredElementNames() required}.
	 */
	public static final String SMALL="small";

	/**
	 * <a target="_blank" href="http://www.w3.org/TR/html401/struct/links.html#edef-LINK">HTML element source</a> - media source for video or audio.
	 * <p>
	 * The end tag of this element is {@linkplain HTMLElements#getEndTagForbiddenElementNames() forbidden}.
	 */
	public static final String SOURCE="source";

	/**
	 * <a target="_blank" href="http://www.w3.org/TR/html401/struct/global.html#edef-SPAN">HTML element SPAN</a> - generic language/style container.
	 * <p>
	 * This is an {@linkplain HTMLElements#getInlineLevelElementNames() inline-level} element.
	 */
	public static final String SPAN="span";

	/**
	 * <a target="_blank" href="http://www.w3.org/TR/html401/present/graphics.html#edef-STRIKE">HTML element STRIKE</a> - strike-through text.
	 * <p>
	 * This is an {@linkplain HTMLElements#getInlineLevelElementNames() inline-level} element.
 	 * <p>
	 * This element is <a target="blank" href="http://www.w3.org/TR/html401/conform.html#deprecated">deprecated</a> in HTML 4.01.
	 * (see {@link HTMLElements#getDeprecatedElementNames()})
	 */
	public static final String STRIKE="strike";

	/**
	 * <a target="_blank" href="http://www.w3.org/TR/html401/struct/text.html#edef-STRONG">HTML element STRONG</a> - strong emphasis.
	 * <p>
	 * This is an {@linkplain HTMLElements#getInlineLevelElementNames() inline-level} element.
	 * <p>
	 * The end tag of this element is {@linkplain HTMLElements#getEndTagRequiredElementNames() required}.
	 */
	public static final String STRONG="strong";

	/**
	 * <a target="_blank" href="http://www.w3.org/TR/html401/present/styles.html#edef-STYLE">HTML element STYLE</a> - style info.
	 * <p>
	 * The end tag of this element is {@linkplain HTMLElements#getEndTagRequiredElementNames() required}.
	 */
	public static final String STYLE="style";

	/**
	 * <a target="_blank" href="http://www.w3.org/TR/html401/struct/text.html#edef-SUB">HTML element SUB</a> - subscript.
	 * <p>
	 * This is an {@linkplain HTMLElements#getInlineLevelElementNames() inline-level} element.
	 * <p>
	 * The end tag of this element is {@linkplain HTMLElements#getEndTagRequiredElementNames() required}.
	 */
	public static final String SUB="sub";

	/**
	 * <a target="_blank" href="http://dev.w3.org/html5/spec/Overview.html#the-summary-element">HTML element summary</a> - caption for details.
	 * <p>
	 * The end tag of this element is {@linkplain HTMLElements#getEndTagRequiredElementNames() required}.
	 */
	public static final String SUMMARY="summary";

	/**
	 * <a target="_blank" href="http://www.w3.org/TR/html401/struct/text.html#edef-SUP">HTML element SUP</a> - superscript.
	 * <p>
	 * This is an {@linkplain HTMLElements#getInlineLevelElementNames() inline-level} element.
	 * <p>
	 * The end tag of this element is {@linkplain HTMLElements#getEndTagRequiredElementNames() required}.
	 */
	public static final String SUP="sup";

	/**
	 * <a target="_blank" href="http://www.w3.org/TR/html401/struct/tables.html#edef-TABLE">HTML element TABLE</a> - table.
	 * <p>
	 * This is a {@linkplain HTMLElements#getBlockLevelElementNames() block-level} element.
	 * <p>
	 * The end tag of this element is {@linkplain HTMLElements#getEndTagRequiredElementNames() required}.
	 */
	public static final String TABLE="table";

	/**
	 * <a target="_blank" href="http://www.w3.org/TR/html401/struct/tables.html#edef-TBODY">HTML element TBODY</a> - table body.
	 * <p>
	 * The start tag of this element is {@linkplain HTMLElements#getStartTagOptionalElementNames() optional}.
	 * <p>
	 * The end tag of this element is {@linkplain HTMLElements#getEndTagOptionalElementNames() optional}:
	 * <table class="CompactDL" cellspacing="0">
	 *  <tr>
	 *   <td title="Start tags that terminate this element"><a href="HTMLElements.html#getTerminatingStartTagNames(java.lang.String)">Terminating start tags</a>:
	 *   <td>{@link #TBODY}, {@link #TFOOT}, {@link #THEAD}
	 *  <tr>
	 *   <td title="End tags that terminate this element"><a href="HTMLElements.html#getTerminatingEndTagNames(java.lang.String)">Terminating end tags</a>:
	 *   <td>{@link #TABLE}, {@link #TBODY}
	 *  <tr>
	 *   <td title="Elements that can be nested inside this element without terminating it"><a href="HTMLElements#getNonterminatingElementNames(java.lang.String)">Nonterminating elements</a>:
	 *   <td>{@link #TABLE}
	 * </table>
	 * <p>
	 * Note that the {@link #TFOOT} and {@link #THEAD} elements are included as
	 * {@linkplain HTMLElements#getTerminatingStartTagNames(String) terminating start tags}, even though the 
	 * <a target="_blank" href="http://www.w3.org/TR/html401/struct/tables.html#h-11.2.3">HTML 4.01 specification section 11.2.3</a>
	 * states that they must precede the {@link #TBODY} element inside a {@link #TABLE}.
	 * Most browsers tolerate an incorrect ordering of the {@link #THEAD}, {@link #TFOOT} and {@link #TBODY} elements,
	 * so this parser also recognises the elements in any order.
	 */
	public static final String TBODY="tbody";

	/**
	 * <a target="_blank" href="http://www.w3.org/TR/html401/struct/tables.html#edef-TD">HTML element TD</a> - table data cell.
	 * <p>
	 * The end tag of this element is {@linkplain HTMLElements#getEndTagOptionalElementNames() optional}:
	 * <table class="CompactDL" cellspacing="0">
	 *  <tr>
	 *   <td title="Start tags that terminate this element"><a href="HTMLElements.html#getTerminatingStartTagNames(java.lang.String)">Terminating start tags</a>:
	 *   <td>{@link #TBODY}, {@link #TD}, {@link #TFOOT}, {@link #TH}, {@link #THEAD}, {@link #TR}
	 *  <tr>
	 *   <td title="End tags that terminate this element"><a href="HTMLElements.html#getTerminatingEndTagNames(java.lang.String)">Terminating end tags</a>:
	 *   <td>{@link #TABLE}, {@link #TBODY}, {@link #TD}, {@link #TFOOT}, {@link #THEAD}, {@link #TR}
	 *  <tr>
	 *   <td title="Elements that can be nested inside this element without terminating it"><a href="HTMLElements#getNonterminatingElementNames(java.lang.String)">Nonterminating elements</a>:
	 *   <td>{@link #TABLE}
	 * </table>
	 */
	public static final String TD="td";

	/**
	 * <a target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#edef-TEXTAREA">HTML element TEXTAREA</a> - multi-line text field.
	 * <p>
	 * The end tag of this element is {@linkplain HTMLElements#getEndTagRequiredElementNames() required}.
	 * <p>
	 * This is an {@linkplain HTMLElements#getInlineLevelElementNames() inline-level} element.
	 */
	public static final String TEXTAREA="textarea";

	/**
	 * <a target="_blank" href="http://www.w3.org/TR/html401/struct/tables.html#edef-TFOOT">HTML element TFOOT</a> - table footer.
	 * <p>
	 * The end tag of this element is {@linkplain HTMLElements#getEndTagOptionalElementNames() optional}:
	 * <table class="CompactDL" cellspacing="0">
	 *  <tr>
	 *   <td title="Start tags that terminate this element"><a href="HTMLElements.html#getTerminatingStartTagNames(java.lang.String)">Terminating start tags</a>:
	 *   <td>{@link #TBODY}, {@link #TFOOT}, {@link #THEAD}
	 *  <tr>
	 *   <td title="End tags that terminate this element"><a href="HTMLElements.html#getTerminatingEndTagNames(java.lang.String)">Terminating end tags</a>:
	 *   <td>{@link #TABLE}, {@link #TFOOT}
	 *  <tr>
	 *   <td title="Elements that can be nested inside this element without terminating it"><a href="HTMLElements#getNonterminatingElementNames(java.lang.String)">Nonterminating elements</a>:
	 *   <td>{@link #TABLE}
	 * </table>
	 */
	public static final String TFOOT="tfoot";

	/**
	 * <a target="_blank" href="http://www.w3.org/TR/html401/struct/tables.html#edef-TH">HTML element TH</a> - table header cell.
	 * <p>
	 * The end tag of this element is {@linkplain HTMLElements#getEndTagOptionalElementNames() optional}:
	 * <table class="CompactDL" cellspacing="0">
	 *  <tr>
	 *   <td title="Start tags that terminate this element"><a href="HTMLElements.html#getTerminatingStartTagNames(java.lang.String)">Terminating start tags</a>:
	 *   <td>{@link #TBODY}, {@link #TD}, {@link #TFOOT}, {@link #TH}, {@link #THEAD}, {@link #TR}
	 *  <tr>
	 *   <td title="End tags that terminate this element"><a href="HTMLElements.html#getTerminatingEndTagNames(java.lang.String)">Terminating end tags</a>:
	 *   <td>{@link #TABLE}, {@link #TBODY}, {@link #TFOOT}, {@link #TH}, {@link #THEAD}, {@link #TR}
	 *  <tr>
	 *   <td title="Elements that can be nested inside this element without terminating it"><a href="HTMLElements#getNonterminatingElementNames(java.lang.String)">Nonterminating elements</a>:
	 *   <td>{@link #TABLE}
	 * </table>
	 */
	public static final String TH="th";

	/**
	 * <a target="_blank" href="http://www.w3.org/TR/html401/struct/tables.html#edef-THEAD">HTML element THEAD</a> - table header.
	 * <p>
	 * The end tag of this element is {@linkplain HTMLElements#getEndTagOptionalElementNames() optional}:
	 * <table class="CompactDL" cellspacing="0">
	 *  <tr>
	 *   <td title="Start tags that terminate this element"><a href="HTMLElements.html#getTerminatingStartTagNames(java.lang.String)">Terminating start tags</a>:
	 *   <td>{@link #TBODY}, {@link #TFOOT}, {@link #THEAD}
	 *  <tr>
	 *   <td title="End tags that terminate this element"><a href="HTMLElements.html#getTerminatingEndTagNames(java.lang.String)">Terminating end tags</a>:
	 *   <td>{@link #TABLE}, {@link #THEAD}
	 *  <tr>
	 *   <td title="Elements that can be nested inside this element without terminating it"><a href="HTMLElements#getNonterminatingElementNames(java.lang.String)">Nonterminating elements</a>:
	 *   <td>{@link #TABLE}
	 * </table>
	 */
	public static final String THEAD="thead";

	/**
	 * <a target="_blank" href="http://dev.w3.org/html5/spec/Overview.html#the-time-element">HTML element time</a> - date and/or time.
	 * <p>
	 * This is an {@linkplain HTMLElements#getInlineLevelElementNames() inline-level} element.
	 * <p>
	 * The end tag of this element is {@linkplain HTMLElements#getEndTagRequiredElementNames() required}.
	 */
	public static final String TIME="time";

	/**
	 * <a target="_blank" href="http://www.w3.org/TR/html401/struct/global.html#edef-TITLE">HTML element TITLE</a> - document title.
	 * <p>
	 * The end tag of this element is {@linkplain HTMLElements#getEndTagRequiredElementNames() required}.
	 */
	public static final String TITLE="title";

	/**
	 * <a target="_blank" href="http://www.w3.org/TR/html401/struct/tables.html#edef-TR">HTML element TR</a> - table row.
	 * <p>
	 * The end tag of this element is {@linkplain HTMLElements#getEndTagOptionalElementNames() optional}:
	 * <table class="CompactDL" cellspacing="0">
	 *  <tr>
	 *   <td title="Start tags that terminate this element"><a href="HTMLElements.html#getTerminatingStartTagNames(java.lang.String)">Terminating start tags</a>:
	 *   <td>{@link #TBODY}, {@link #TFOOT}, {@link #THEAD}, {@link #TR}
	 *  <tr>
	 *   <td title="End tags that terminate this element"><a href="HTMLElements.html#getTerminatingEndTagNames(java.lang.String)">Terminating end tags</a>:
	 *   <td>{@link #TABLE}, {@link #TBODY}, {@link #TFOOT}, {@link #THEAD}, {@link #TR}
	 *  <tr>
	 *   <td title="Elements that can be nested inside this element without terminating it"><a href="HTMLElements#getNonterminatingElementNames(java.lang.String)">Nonterminating elements</a>:
	 *   <td>{@link #TABLE}
	 * </table>
	 */
	public static final String TR="tr";

	/**
	 * <a target="_blank" href="http://www.w3.org/TR/html401/present/graphics.html#edef-TT">HTML element TT</a> - teletype or monospaced text style.
	 * <p>
	 * The end tag of this element is {@linkplain HTMLElements#getEndTagRequiredElementNames() required}.
	 * <p>
	 * This is an {@linkplain HTMLElements#getInlineLevelElementNames() inline-level} element.
	 */
	public static final String TT="tt";

	/**
	 * <a target="_blank" href="http://www.w3.org/TR/html401/present/graphics.html#edef-U">HTML element U</a> - underlined text style.
	 * <p>
	 * This is an {@linkplain HTMLElements#getInlineLevelElementNames() inline-level} element.
	 * <p>
	 * The end tag of this element is {@linkplain HTMLElements#getEndTagRequiredElementNames() required}.
 	 * <p>
	 * This element is <a target="blank" href="http://www.w3.org/TR/html401/conform.html#deprecated">deprecated</a> in HTML 4.01.
	 * (see {@link HTMLElements#getDeprecatedElementNames()})
	 */
	public static final String U="u";

	/**
	 * <a target="_blank" href="http://www.w3.org/TR/html401/struct/lists.html#edef-UL">HTML element UL</a> - unordered list.
	 * <p>
	 * This is a {@linkplain HTMLElements#getBlockLevelElementNames() block-level} element.
	 * <p>
	 * The end tag of this element is {@linkplain HTMLElements#getEndTagRequiredElementNames() required}.
	 */
	public static final String UL="ul";

	/**
	 * <a target="_blank" href="http://www.w3.org/TR/html401/struct/text.html#edef-VAR">HTML element VAR</a> - instance of a variable or program argument.
	 * <p>
	 * This is an {@linkplain HTMLElements#getInlineLevelElementNames() inline-level} element.
	 * <p>
	 * The end tag of this element is {@linkplain HTMLElements#getEndTagRequiredElementNames() required}.
	 */
	public static final String VAR="var";

	/**
	 * <a target="_blank" href="http://dev.w3.org/html5/spec/Overview.html#the-video-element">HTML element video</a> - video player.
	 * <p>
	 * The end tag of this element is {@linkplain HTMLElements#getEndTagRequiredElementNames() required}.
	 */
	public static final String VIDEO="video";

	/**
	 * <a target="_blank" href="http://dev.w3.org/html5/spec/Overview.html#the-wbr-element">HTML element wbr</a> - line breaking opportunity.
	 * <p>
	 * This is an {@linkplain HTMLElements#getInlineLevelElementNames() inline-level} element.
	 * <p>
	 * The end tag of this element is {@linkplain HTMLElements#getEndTagForbiddenElementNames() forbidden}.
	 */
	public static final String WBR="wbr";
}
