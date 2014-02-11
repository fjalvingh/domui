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

final class RendererCSS {
	private static enum Side {
		top, right, bottom, left
	}

	private static Map<String,Float> UNIT_FACTOR=new HashMap<String,Float>();
	static {
		UNIT_FACTOR.put("em",1.0F);
		UNIT_FACTOR.put("ex",1.0F);
		UNIT_FACTOR.put("px",0.125F);
		UNIT_FACTOR.put("in",8F);
		UNIT_FACTOR.put("cm",3F);
		UNIT_FACTOR.put("mm",0.3F);
		UNIT_FACTOR.put("pt",0.1F);
		UNIT_FACTOR.put("pc",1.2F);
	}

	public static int getTopMargin(final Element element, int defaultValue) {
		return getMargin(get(element),Side.top,defaultValue);
	}

	public static int getBottomMargin(final Element element, int defaultValue) {
		return getMargin(get(element),Side.bottom,defaultValue);
	}

	public static int getLeftMargin(final Element element, int defaultValue) {
		return getMargin(get(element),Side.left,defaultValue);
	}

	public static int getRightMargin(final Element element, int defaultValue) {
		return getMargin(get(element),Side.right,defaultValue);
	}

	private static String get(final Element element) {
		return element.getAttributeValue("style");
	}

	private static int getMargin(final String css, final Side side, final int defaultValue) {
		if (css==null) return defaultValue;
		String[] styles=css.split(";");
		for (int i=0; i<styles.length; i++) styles[i]=styles[i].toLowerCase().trim();
		int margin=getStyleValue(styles,side,"margin");
		int padding=getStyleValue(styles,side,"padding");
		if (margin==-1) return padding!=-1 ? padding : defaultValue;
		return padding!=-1 ? margin+padding : margin;
	}

	private static int getStyleValue(final String[] styles, final Side side, final String styleName) {
		int combinedStyleValue=-1;
		for (int i=0; i<styles.length; i++) {
			final String style=styles[i];
			if (style.length()<=styleName.length()+1 || !style.startsWith(styleName)) continue;
			int colonPos=style.indexOf(':');
			if (colonPos==-1) continue;
			String styleValue=style.substring(colonPos+1).trim();
			if (styleValue.length()==0) continue;
			boolean explicitSide=false;
			int styleNameEnd=styleName.length();
			if (style.charAt(styleName.length())=='-' && style.startsWith(side.name(),styleName.length()+1)) {
				// eg margin-top: 1em
				explicitSide=true;
				styleNameEnd=styleName.length()+1+side.name().length();
				if (style.length()<=styleNameEnd+1) continue;
			}
			if (styleNameEnd!=colonPos && !Segment.isWhiteSpace(style.charAt(styleNameEnd))) continue;
			if (!explicitSide) {
				// eg margin: 1em 0 2px 0
				final String[] styleValueItems=styleValue.split("\\s+");
				int itemIndex=side.ordinal();
				final int itemCount=styleValueItems.length;
				if (itemCount==0) continue;
				if (itemCount==1) {
					// top/right/bottom/left all in one item
					itemIndex=0;
				} else if (itemCount==2) {
					// top/bottom, left/right
					itemIndex=side.ordinal()%2;
				} else if (itemCount==3) {
					// top, left/right, bottom
					if (side==Side.left) itemIndex=1;
				}
				styleValue=styleValueItems[itemIndex].trim();
			}
			int value=0;
			if (styleValue.length()==0) continue;
			if (styleValue.charAt(styleValue.length()-1)=='%') continue;
			if (styleValue.equals("auto") || styleValue.equals("inherit")) continue;
			if (styleValue.length()<3) {
				if (!styleValue.equals("0")) continue;
			} else {
				Float unitFactor=UNIT_FACTOR.get(styleValue.substring(styleValue.length()-2));
				if (unitFactor==null) continue;
				float rawValue;
				try {
					rawValue=Float.parseFloat(styleValue.substring(0,styleValue.length()-2));
				} catch (NumberFormatException ex) {
					continue;
				}
				value=Math.round(rawValue*unitFactor);
			}
			if (explicitSide) return value;
			combinedStyleValue=value;
		}
		return combinedStyleValue;
	}
}