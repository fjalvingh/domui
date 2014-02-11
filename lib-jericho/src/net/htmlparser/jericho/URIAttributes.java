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

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

final class URIAttributes {
	private static final String[] uriAttributeNames=new String[] {"action","archive","background","cite","href","longdesc","src","usemap"};
	private static final String[] objectURIAttributeNames=new String[] {"classid","codebase","data"};
	
	public static List<Attribute> getList(final Segment segment) {
		if (segment==null || segment.getFirstStartTag()==null) return Collections.emptyList();
		List<Attribute> attributes=new ArrayList<Attribute>();
		for (String uriAttributeName : uriAttributeNames) {
			for (StartTag startTag : segment.getAllStartTags(uriAttributeName,null)) {
				Attribute attribute=startTag.getAttributes().get(uriAttributeName);
				attribute.startTag=startTag;
				attributes.add(attribute);
			}
		}
		for (StartTag startTag : segment.getAllStartTags(HTMLElementName.OBJECT)) {
			for (String uriAttributeName : objectURIAttributeNames) {
				Attribute attribute=startTag.getAttributes().get(uriAttributeName);
				if (attribute==null) continue;
				attribute.startTag=startTag;
				attributes.add(attribute);
			}
		}
		Collections.sort(attributes);
		return attributes;
	}
	
	public static List<Segment> getStyleURISegments(final Segment segment) {
		if (segment==null || segment.length()==0) return Collections.emptyList();
		if (segment.getFirstStartTag()==null) {
			// no start tags in this segment, assume the segment is a style attribute value
			int urlDelimiterStartPos=segment.getSource().getParseText().indexOf("url(",segment.getBegin(),segment.getEnd());
			if (urlDelimiterStartPos==-1) return Collections.emptyList();
			return addURLSegmentsFromCSS(new ArrayList<Segment>(),new Segment(segment.getSource(),urlDelimiterStartPos,segment.getEnd()));
		}
		List<Segment> uriSegments=new ArrayList<Segment>();
		for (StartTag startTag : segment.getAllStartTags("style",null)) {
			addURLSegmentsFromCSS(uriSegments,startTag.getAttributes().get("style").getValueSegment());
		}
		for (Element element : segment.getAllElements(HTMLElementName.STYLE)) {
			addURLSegmentsFromCSS(uriSegments,element.getContent());
		}
		Collections.sort(uriSegments);
		return uriSegments;
	}

	private static List<Segment> addURLSegmentsFromCSS(final List<Segment> uriSegments, final Segment cssSegment) {
		final Source source=cssSegment.getSource();
		final ParseText parseText=source.getParseText();
		final int breakAtIndex=cssSegment.getEnd();
		for (int pos=cssSegment.getBegin(); (pos=parseText.indexOf("url(",pos,breakAtIndex))!=-1;) {
			pos+=4;
			while (pos<breakAtIndex && Segment.isWhiteSpace(parseText.charAt(pos))) pos++;
			if (pos>=breakAtIndex) break;
			if (isQuote(parseText.charAt(pos))) {
				pos++;
				if (pos>=breakAtIndex) break;
			}
			final int uriBegin=pos;
			final int closingBracketPos=parseText.indexOf(')',uriBegin,breakAtIndex);
			if (closingBracketPos==-1) break;
			pos=closingBracketPos;
			while (Segment.isWhiteSpace(parseText.charAt(pos-1))) pos--;
			if (isQuote(parseText.charAt(pos-1))) pos--;
			final int uriEnd=pos;
			if (uriEnd<=uriBegin) break;
			uriSegments.add(new Segment(source,uriBegin,uriEnd));
			pos=closingBracketPos;
		}
		return uriSegments;
	}

	private static boolean isQuote(final char ch) {
		return ch=='"' || ch=='\'';
	}
}

