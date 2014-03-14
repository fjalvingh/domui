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

class StartTagTypeMicrosoftDownlevelRevealedValidatingConditionalCommentIf extends StartTagTypeGenericImplementation {
	static final StartTagTypeMicrosoftDownlevelRevealedValidatingConditionalCommentIf INSTANCE=new StartTagTypeMicrosoftDownlevelRevealedValidatingConditionalCommentIf();

	private StartTagTypeMicrosoftDownlevelRevealedValidatingConditionalCommentIf() {
		this("Microsoft downlevel-revealed validating conditional comment IF","<!--[if","]><!-->");
	}

	protected StartTagTypeMicrosoftDownlevelRevealedValidatingConditionalCommentIf(final String description, final String startDelimiter, final String closingDelimiter) {
		super(description,startDelimiter,closingDelimiter,null,false);
	}

	protected int getEnd(final Source source, final int pos) {
		// This method needs to be overridden because this tag type shares the same start delimiter as the downlevel hidden conditional comment.
		// The closing delimiter of the other tag type must not appear inside this tag.
		// Take the following example:
		// <!--[if IE]> ... <![endif]--> ... <!--[if !(IE 5)]><!--> ... <!--<![endif]-->
		// If the default implementation were used, then the parser would recognise the first tag as:
		// <!--[if IE]> ... <![endif]--> ... <!--[if !(IE 5)]><!-->
		final int delimiterBegin=source.getParseText().indexOf(MicrosoftConditionalCommentTagTypes.DOWNLEVEL_HIDDEN_IF.getClosingDelimiter(),pos);
		if (delimiterBegin==-1) return -1;
		if (source.getParseText().containsAt(getClosingDelimiter(),delimiterBegin)) return delimiterBegin+getClosingDelimiter().length();
		// this is a downlevel hidden conditional comment, so fail this tag type silently without displaying a log message
		return -2;
	}
}
