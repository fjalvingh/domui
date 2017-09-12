/*
 * DomUI Java User Interface library
 * Copyright (c) 2010 by Frits Jalvingh, Itris B.V.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * See the "sponsors" file for a list of supporters.
 *
 * The latest version of DomUI and related code, support and documentation
 * can be found at http://www.domui.org/
 * The contact for the project is Frits Jalvingh <jal@etc.to>.
 */
package to.etc.domui.dom.css;

import to.etc.domui.util.DomUtil;

/**
 * Base class for all remote DOM nodes containing only CSS properties && change management for those.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Aug 17, 2007
 */
public class CssBase {
	private String m_cachedStyle;

	/*-- CSS Background properties --*/
	private BackgroundAttachment m_backgroundAttachment;

	private String m_backgroundColor;

	private String m_backgroundImage;

	private String m_backgroundPosition;

	private String m_backgroundRepeat;

	/*-- CSS Border properties --*/
	private int m_borderLeftWidth = -1;

	private int m_borderRightWidth = -1;

	private int m_borderTopWidth = -1;

	private int m_borderBottomWidth = -1;

	private String m_borderTopColor;

	private String m_borderBottomColor;

	private String m_borderLeftColor;

	private String m_borderRightColor;

	private String m_borderTopStyle;

	private String m_borderBottomStyle;

	private String m_borderLeftStyle;

	private String m_borderRightStyle;

	/*-- CSS Classification. --*/
	private ClearType m_clear;

//	private String m_cursor;

	private DisplayType m_display;

	private FloatType m_float;

	private PositionType m_position;

	private VisibilityType m_visibility;

	/*-- CSS Dimension properties --*/
	private String m_height;

	private String m_lineHeight;

	private String m_maxHeight;

	private String m_maxWidth;

	private String m_minHeight;

	private String m_minWidth;

	private String m_width;

	/*-- CSS Font properties. --*/
	private String m_fontFamily;

	private String m_fontSize;

	private String m_fontSizeAdjust;

	private FontStyle m_fontStyle;

	private FontVariant m_fontVariant;

	private String m_fontWeight;

	private String m_color;

	/*-- Positioning --*/
	private Overflow m_overflow;

	private int m_zIndex = Integer.MIN_VALUE;

	private String m_top;

	private String m_bottom;

	private String m_left;

	private String m_right;

	/*--- TEXT properties -----*/
	private TextAlign m_textAlign;

	private VerticalAlignType m_verticalAlign;

	/*-- CSS Margin properties --*/
	private String m_marginLeft;

	private String m_marginRight;

	private String m_marginTop;

	private String m_marginBottom;

	/*-- CSS Padding properties --*/
	private String m_paddingLeft;

	private String m_paddingRight;

	private String m_paddingTop;

	private String m_paddingBottom;
	
	private TextTransformType m_transform;

	public String getCachedStyle() {
		return m_cachedStyle;
	}

	public void setCachedStyle(final String cachedStyle) {
		m_cachedStyle = cachedStyle;
	}

	/**
	 * Called as soon as a property of <i>this</i> object changes. This dirties this
	 * object.
	 */
	protected void changed() {
	// FIXME Needs impl.
	}

	public BackgroundAttachment getBackgroundAttachment() {
		return m_backgroundAttachment;
	}

	public void setBackgroundAttachment(final BackgroundAttachment backgroundAttachment) {
		if(!DomUtil.isEqual(backgroundAttachment, m_backgroundAttachment))
			changed();
		m_backgroundAttachment = backgroundAttachment;
	}

	public String getBackgroundColor() {
		return m_backgroundColor;
	}

	public void setBackgroundColor(final String backgroundColor) {
		if(!DomUtil.isEqual(backgroundColor, m_backgroundColor))
			changed();
		m_backgroundColor = backgroundColor;
	}

	public String getBackgroundImage() {
		return m_backgroundImage;
	}

	public void setBackgroundImage(final String backgroundImage) {
		if(!DomUtil.isEqual(backgroundImage, m_backgroundImage))
			changed();
		m_backgroundImage = backgroundImage;
	}

	public String getBackgroundPosition() {
		return m_backgroundPosition;
	}

	public void setBackgroundPosition(final String backgroundPosition) {
		if(!DomUtil.isEqual(backgroundPosition, m_backgroundPosition))
			changed();
		m_backgroundPosition = backgroundPosition;
	}

	public String getBackgroundRepeat() {
		return m_backgroundRepeat;
	}

	public void setBackgroundRepeat(final String backgroundRepeat) {
		if(!DomUtil.isEqual(backgroundRepeat, m_backgroundRepeat))
			changed();
		m_backgroundRepeat = backgroundRepeat;
	}

	public int getBorderLeftWidth() {
		return m_borderLeftWidth;
	}

	public void setBorderLeftWidth(final int borderLeftWidth) {
		if(borderLeftWidth != m_borderLeftWidth)
			changed();
		m_borderLeftWidth = borderLeftWidth;
	}

	public int getBorderRightWidth() {
		return m_borderRightWidth;
	}

	public void setBorderRightWidth(final int borderRightWidth) {
		if(m_borderRightWidth != borderRightWidth)
			changed();
		m_borderRightWidth = borderRightWidth;
	}

	public int getBorderTopWidth() {
		return m_borderTopWidth;
	}

	public void setBorderTopWidth(final int borderTopWidth) {
		if(m_borderTopWidth != borderTopWidth)
			changed();
		m_borderTopWidth = borderTopWidth;
	}

	public int getBorderBottomWidth() {
		return m_borderBottomWidth;
	}

	public void setBorderBottomWidth(final int borderBottomWidth) {
		if(m_borderBottomWidth != borderBottomWidth)
			changed();
		m_borderBottomWidth = borderBottomWidth;
	}

	public String getBorderTopColor() {
		return m_borderTopColor;
	}

	public void setBorderTopColor(final String borderTopColor) {
		if(!DomUtil.isEqual(borderTopColor, m_borderTopColor))
			changed();
		m_borderTopColor = borderTopColor;
	}

	public String getBorderBottomColor() {
		return m_borderBottomColor;
	}

	public void setBorderBottomColor(final String borderBottomColor) {
		if(!DomUtil.isEqual(borderBottomColor, m_borderBottomColor))
			changed();
		m_borderBottomColor = borderBottomColor;
	}

	public String getBorderLeftColor() {
		return m_borderLeftColor;
	}

	public void setBorderLeftColor(final String borderLeftColor) {
		if(!DomUtil.isEqual(borderLeftColor, m_borderLeftColor))
			changed();
		m_borderLeftColor = borderLeftColor;
	}

	public String getBorderRightColor() {
		return m_borderRightColor;
	}

	public void setBorderRightColor(final String borderRightColor) {
		if(!DomUtil.isEqual(borderRightColor, m_borderRightColor))
			changed();
		m_borderRightColor = borderRightColor;
	}

	public String getBorderTopStyle() {
		return m_borderTopStyle;
	}

	public void setBorderTopStyle(final String borderTopStyle) {
		if(!DomUtil.isEqual(borderTopStyle, m_borderTopStyle))
			changed();
		m_borderTopStyle = borderTopStyle;
	}

	public String getBorderBottomStyle() {
		return m_borderBottomStyle;
	}

	public void setBorderBottomStyle(final String borderBottomStyle) {
		if(!DomUtil.isEqual(borderBottomStyle, m_borderBottomStyle))
			changed();
		m_borderBottomStyle = borderBottomStyle;
	}

	public String getBorderLeftStyle() {
		return m_borderLeftStyle;
	}

	public void setBorderLeftStyle(final String borderLeftStyle) {
		if(!DomUtil.isEqual(borderLeftStyle, m_borderLeftStyle))
			changed();
		m_borderLeftStyle = borderLeftStyle;
	}

	public String getBorderRightStyle() {
		return m_borderRightStyle;
	}

	public void setBorderRightStyle(final String borderRightStyle) {
		if(!DomUtil.isEqual(borderRightStyle, m_borderRightStyle))
			changed();
		m_borderRightStyle = borderRightStyle;
	}

	/*-- Border shortcut calls. --*/

	public void setBorderWidth(final int w) {
		setBorderLeftWidth(w);
		setBorderRightWidth(w);
		setBorderTopWidth(w);
		setBorderBottomWidth(w);
	}

	public void setBorderStyle(final String bs) {
		setBorderLeftStyle(bs);
		setBorderRightStyle(bs);
		setBorderTopStyle(bs);
		setBorderBottomStyle(bs);
	}

	public void setBorderColor(final String bs) {
		setBorderLeftColor(bs);
		setBorderRightColor(bs);
		setBorderTopColor(bs);
		setBorderBottomColor(bs);
	}

	public void setBorder(final int w) {
		setBorderWidth(w);
	}

	public void setBorder(final int w, final String color, final String style) {
		setBorderWidth(w);
		setBorderColor(color);
		setBorderStyle(style);
	}

	public ClearType getClear() {
		return m_clear;
	}

	public void setClear(final ClearType clear) {
		if(!DomUtil.isEqual(clear, m_clear))
			changed();
		m_clear = clear;
	}

//	public String getCursor() {
//		return m_cursor;
//	}
//
//	public void setCursor(final String cursor) {
//		if(!DomUtil.isEqual(cursor, m_cursor))
//			changed();
//		m_cursor = cursor;
//	}

	public DisplayType getDisplay() {
		return m_display;
	}

	public void setDisplay(final DisplayType display) {
		if(!DomUtil.isEqual(display, m_display))
			changed();
		m_display = display;
	}

	/**
	 * Used to switch the display attribute when it is switched by an effect.
	 * @param dt
	 * @return
	 */
	public boolean internalSetDisplay(final DisplayType dt) {
		if(m_display == dt)
			return false;
		m_display = dt;
		setCachedStyle(null);
		return true;
	}

	public Overflow getOverflow() {
		return m_overflow;
	}

	public void setOverflow(final Overflow overflow) {
		if(m_overflow != overflow)
			changed();
		m_overflow = overflow;
	}

	public FloatType getFloat() {
		return m_float;
	}

	public void setFloat(final FloatType f) {
		if(m_float == f)
			return;
		changed();
		m_float = f;
	}

	public PositionType getPosition() {
		return m_position;
	}

	public void setPosition(final PositionType position) {
		if(m_position == position)
			return;
		changed();
		m_position = position;
	}

	public VisibilityType getVisibility() {
		return m_visibility;
	}

	public void setVisibility(final VisibilityType visibility) {
		if(m_visibility == visibility)
			return;
		changed();
		m_visibility = visibility;
	}

	public String getHeight() {
		return m_height;
	}

	public void setHeight(final String height) {
		if(DomUtil.isEqual(height, m_height))
			return;
		changed();
		m_height = height;
	}

	public String getLineHeight() {
		return m_lineHeight;
	}

	public void setLineHeight(final String lineHeight) {
		if(DomUtil.isEqual(m_lineHeight, lineHeight))
			return;
		changed();
		m_lineHeight = lineHeight;
	}

	public String getMaxHeight() {
		return m_maxHeight;
	}

	public void setMaxHeight(final String maxHeight) {
		if(DomUtil.isEqual(m_maxHeight, maxHeight))
			return;
		changed();
		m_maxHeight = maxHeight;
	}

	public String getMaxWidth() {
		return m_maxWidth;
	}

	public void setMaxWidth(final String maxWidth) {
		if(DomUtil.isEqual(m_maxWidth, maxWidth))
			return;
		changed();
		m_maxWidth = maxWidth;
	}

	public String getMinHeight() {
		return m_minHeight;
	}

	public void setMinHeight(final String minHeight) {
		if(DomUtil.isEqual(m_minHeight, minHeight))
			return;
		changed();
		m_minHeight = minHeight;
	}

	public String getMinWidth() {
		return m_minWidth;
	}

	public void setMinWidth(final String minWidth) {
		if(DomUtil.isEqual(m_minWidth, minWidth))
			return;
		changed();
		m_minWidth = minWidth;
	}

	public String getWidth() {
		return m_width;
	}

	public void setWidth(final String width) {
		if(DomUtil.isEqual(m_width, width))
			return;
		changed();
		m_width = width;
	}

	public String getFontFamily() {
		return m_fontFamily;
	}

	public void setFontFamily(final String fontFamily) {
		if(DomUtil.isEqual(m_fontFamily, fontFamily))
			return;
		changed();
		m_fontFamily = fontFamily;
	}

	public String getFontSize() {
		return m_fontSize;
	}

	public void setFontSize(final String fontSize) {
		if(DomUtil.isEqual(m_fontSize, fontSize))
			return;
		changed();
		m_fontSize = fontSize;
	}

	public String getFontSizeAdjust() {
		return m_fontSizeAdjust;
	}

	public void setFontSizeAdjust(final String fontSizeAdjust) {
		if(DomUtil.isEqual(m_fontSizeAdjust, fontSizeAdjust))
			return;
		changed();
		m_fontSizeAdjust = fontSizeAdjust;
	}

	public FontStyle getFontStyle() {
		return m_fontStyle;
	}

	public void setFontStyle(final FontStyle fontStyle) {
		if(DomUtil.isEqual(m_fontStyle, fontStyle))
			return;
		changed();
		m_fontStyle = fontStyle;
	}

	public FontVariant getFontVariant() {
		return m_fontVariant;
	}

	public void setFontVariant(final FontVariant fontVariant) {
		if(DomUtil.isEqual(m_fontVariant, fontVariant))
			return;
		changed();
		m_fontVariant = fontVariant;
	}

	public String getFontWeight() {
		return m_fontWeight;
	}

	public void setFontWeight(final String fontWeight) {
		if(DomUtil.isEqual(m_fontWeight, fontWeight))
			return;
		changed();
		m_fontWeight = fontWeight;
	}

	public int getZIndex() {
		return m_zIndex;
	}

	public void setZIndex(final int index) {
		if(m_zIndex != index)
			changed();
		m_zIndex = index;
	}

	public String getTop() {
		return m_top;
	}

	public void setTop(final String top) {
		if(DomUtil.isEqual(top, m_top))
			return;
		changed();
		m_top = top;
	}

	public void setTop(int px) {
		String s = Integer.toString(px) + "px";
		setTop(s);
	}

	public String getBottom() {
		return m_bottom;
	}

	public void setBottom(final String bottom) {
		if(DomUtil.isEqual(bottom, m_bottom))
			return;
		changed();
		m_bottom = bottom;
	}

	public void setBottom(int px) {
		String s = Integer.toString(px) + "px";
		setBottom(s);
	}

	public String getLeft() {
		return m_left;
	}

	public void setLeft(final String left) {
		if(DomUtil.isEqual(left, m_left))
			return;
		changed();
		m_left = left;
	}

	public void setLeft(final int px) {
		String s = Integer.toString(px) + "px";
		setLeft(s);
	}

	public String getRight() {
		return m_right;
	}

	public void setRight(final String right) {
		if(DomUtil.isEqual(right, m_right))
			return;
		changed();
		m_right = right;
	}

	public void setRight(final int px) {
		String s = Integer.toString(px) + "px";
		setRight(s);
	}

	public String getColor() {
		return m_color;
	}

	public void setColor(final String color) {
		if(DomUtil.isEqual(color, m_color))
			return;
		changed();
		m_color = color;
	}

	public TextAlign getTextAlign() {
		return m_textAlign;
	}

	public void setTextAlign(final TextAlign textAlign) {
		if(m_textAlign == textAlign)
			return;
		changed();
		m_textAlign = textAlign;
	}

	public VerticalAlignType getVerticalAlign() {
		return m_verticalAlign;
	}

	public void setVerticalAlign(final VerticalAlignType verticalAlign) {
		if(m_verticalAlign == verticalAlign)
			return;
		changed();
		m_verticalAlign = verticalAlign;
	}

	public String getMarginLeft() {
		return m_marginLeft;
	}

	public void setMarginLeft(String marginLeft) {
		if(DomUtil.isEqual(m_marginLeft, marginLeft))
			return;
		changed();
		m_marginLeft = marginLeft;
	}

	public String getMarginRight() {
		return m_marginRight;
	}

	public void setMarginRight(String marginRight) {
		if(DomUtil.isEqual(m_marginRight, marginRight))
			return;
		changed();
		m_marginRight = marginRight;
	}

	public String getMarginTop() {
		return m_marginTop;
	}

	public void setMarginTop(String marginTop) {
		if(DomUtil.isEqual(m_marginTop, marginTop))
			return;
		changed();
		m_marginTop = marginTop;
	}

	public String getMarginBottom() {
		return m_marginBottom;
	}

	public void setMarginBottom(String marginBottom) {
		if(DomUtil.isEqual(m_marginBottom, marginBottom))
			return;
		changed();
		m_marginBottom = marginBottom;
	}

	public void setMargin(String... margin) {
		switch(margin.length){
			default:
				throw new IllegalStateException("Margin must have 1..4 string parameters.");
			case 1:
				setMarginTop(margin[0]);
				setMarginBottom(margin[0]);
				setMarginLeft(margin[0]);
				setMarginRight(margin[0]);
				break;
			case 2:
				setMarginTop(margin[0]);
				setMarginBottom(margin[0]);
				setMarginLeft(margin[1]);
				setMarginRight(margin[1]);
				break;
			case 3:
				setMarginTop(margin[0]);
				setMarginBottom(margin[2]);
				setMarginLeft(margin[1]);
				setMarginRight(margin[1]);
				break;
			case 4:
				setMarginTop(margin[0]);
				setMarginRight(margin[1]);
				setMarginBottom(margin[2]);
				setMarginLeft(margin[3]);
				break;
		}
	}

	public String getPaddingLeft() {
		return m_paddingLeft;
	}

	public void setPaddingLeft(String paddingLeft) {
		if(DomUtil.isEqual(m_paddingLeft, paddingLeft))
			return;
		changed();
		m_paddingLeft = paddingLeft;
	}

	public String getPaddingRight() {
		return m_paddingRight;
	}

	public void setPaddingRight(String paddingRight) {
		if(DomUtil.isEqual(m_paddingRight, paddingRight))
			return;
		changed();
		m_paddingRight = paddingRight;
	}

	public String getPaddingTop() {
		return m_paddingTop;
	}

	public void setPaddingTop(String paddingTop) {
		if(DomUtil.isEqual(m_paddingTop, paddingTop))
			return;
		changed();
		m_paddingTop = paddingTop;
	}

	public String getPaddingBottom() {
		return m_paddingBottom;
	}

	public void setPaddingBottom(String paddingBottom) {
		if(DomUtil.isEqual(m_paddingBottom, paddingBottom))
			return;
		changed();
		m_paddingBottom = paddingBottom;
	}

	public void setPadding(String... padding) {
		switch(padding.length){
			default:
				throw new IllegalStateException("Padding must have 1..4 string parameters.");
			case 1:
				setPaddingTop(padding[0]);
				setPaddingBottom(padding[0]);
				setPaddingLeft(padding[0]);
				setPaddingRight(padding[0]);
				break;
			case 2:
				setPaddingTop(padding[0]);
				setPaddingBottom(padding[0]);
				setPaddingLeft(padding[1]);
				setPaddingRight(padding[1]);
				break;
			case 3:
				setPaddingTop(padding[0]);
				setPaddingBottom(padding[2]);
				setPaddingLeft(padding[1]);
				setPaddingRight(padding[1]);
				break;
			case 4:
				setPaddingTop(padding[0]);
				setPaddingRight(padding[1]);
				setPaddingBottom(padding[2]);
				setPaddingLeft(padding[3]);
				break;
		}
	}
	
	public TextTransformType getTransform() {
		return m_transform;
	}

	public void setTransform(TextTransformType transform) {
		if(!DomUtil.isEqual(m_transform, transform))
			changed();
		m_transform = transform;
	}

}
