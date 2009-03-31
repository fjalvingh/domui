package to.etc.domui.dom.css;

import to.etc.domui.util.DomUtil;

/**
 * Base class for all remote DOM nodes containing only CSS properties && change management for those.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Aug 17, 2007
 */
public class CssBase {
	/*-- CSS Background properties --*/
	private BackgroundAttachment	m_backgroundAttachment;
	private String			m_backgroundColor;
	private String			m_backgroundImage;
	private String			m_backgroundPosition;
	private String			m_backgroundRepeat;

	/*-- CSS Border properties --*/
	private int				m_borderLeftWidth = -1;
	private int				m_borderRightWidth = -1;
	private int				m_borderTopWidth = -1;
	private int				m_borderBottomWidth = -1;

	private String			m_borderTopColor;
	private String			m_borderBottomColor;
	private String			m_borderLeftColor;
	private String			m_borderRightColor;

	private String			m_borderTopStyle;
	private String			m_borderBottomStyle;
	private String			m_borderLeftStyle;
	private String			m_borderRightStyle;

	/*-- CSS Classification. --*/
	private ClearType		m_clear;
	private String			m_cursor;
	private DisplayType		m_display;
	private FloatType		m_float;
	
	private PositionType	m_position;

	private VisibilityType	m_visibility;

	/*-- CSS Dimension properties --*/
	private String			m_height;
	private String			m_lineHeight;
	private String			m_maxHeight;
	private String			m_maxWidth;
	private String			m_minHeight;
	private String			m_minWidth;
	private String			m_width;

	/*-- CSS Font properties. --*/
	private String			m_fontFamily;
	private String			m_fontSize;
	private String			m_fontSizeAdjust;
	private FontStyle		m_fontStyle;
	private FontVariant		m_fontVariant;
	private String			m_fontWeight;

	private String			m_color;

	/*-- Positioning --*/
	private Overflow		m_overflow;
	private int				m_zIndex = Integer.MIN_VALUE;
	private int				m_top = Integer.MIN_VALUE;
	private int				m_bottom = Integer.MIN_VALUE;
	private int				m_left	= Integer.MIN_VALUE;
	private int				m_right = Integer.MIN_VALUE;


	/**
	 * Called as soon as a property of <i>this</i> object changes. This dirties this
	 * object.
	 */
	protected void	changed() {
		// FIXME Needs impl.
	}

	public BackgroundAttachment getBackgroundAttachment() {
		return m_backgroundAttachment;
	}
	public void setBackgroundAttachment(BackgroundAttachment backgroundAttachment) {
		if(! DomUtil.isEqual(backgroundAttachment, m_backgroundAttachment))
			changed();
		m_backgroundAttachment = backgroundAttachment;
	}
	public String getBackgroundColor() {
		return m_backgroundColor;
	}
	public void setBackgroundColor(String backgroundColor) {
		if(! DomUtil.isEqual(backgroundColor, m_backgroundColor))
			changed();
		m_backgroundColor = backgroundColor;
	}
	public String getBackgroundImage() {
		return m_backgroundImage;
	}
	public void setBackgroundImage(String backgroundImage) {
		if(! DomUtil.isEqual(backgroundImage, m_backgroundImage))
			changed();
		m_backgroundImage = backgroundImage;
	}
	public String getBackgroundPosition() {
		return m_backgroundPosition;
	}
	public void setBackgroundPosition(String backgroundPosition) {
		if(! DomUtil.isEqual(backgroundPosition, m_backgroundPosition))
			changed();
		m_backgroundPosition = backgroundPosition;
	}
	public String getBackgroundRepeat() {
		return m_backgroundRepeat;
	}
	public void setBackgroundRepeat(String backgroundRepeat) {
		if(! DomUtil.isEqual(backgroundRepeat, m_backgroundRepeat))
			changed();
		m_backgroundRepeat = backgroundRepeat;
	}
	public int getBorderLeftWidth() {
		return m_borderLeftWidth;
	}
	public void setBorderLeftWidth(int borderLeftWidth) {
		if(borderLeftWidth!=m_borderLeftWidth)
			changed();
		m_borderLeftWidth = borderLeftWidth;
	}
	public int getBorderRightWidth() {
		return m_borderRightWidth;
	}
	public void setBorderRightWidth(int borderRightWidth) {
		if(m_borderRightWidth != borderRightWidth)
			changed();
		m_borderRightWidth = borderRightWidth;
	}
	public int getBorderTopWidth() {
		return m_borderTopWidth;
	}
	public void setBorderTopWidth(int borderTopWidth) {
		if(m_borderTopWidth != borderTopWidth)
			changed();
		m_borderTopWidth = borderTopWidth;
	}
	public int getBorderBottomWidth() {
		return m_borderBottomWidth;
	}
	public void setBorderBottomWidth(int borderBottomWidth) {
		if(m_borderBottomWidth != borderBottomWidth)
			changed();
		m_borderBottomWidth = borderBottomWidth;
	}
	public String getBorderTopColor() {
		return m_borderTopColor;
	}
	public void setBorderTopColor(String borderTopColor) {
		if(! DomUtil.isEqual(borderTopColor, m_borderTopColor))
			changed();
		m_borderTopColor = borderTopColor;
	}
	public String getBorderBottomColor() {
		return m_borderBottomColor;
	}
	public void setBorderBottomColor(String borderBottomColor) {
		if(! DomUtil.isEqual(borderBottomColor, m_borderBottomColor))
			changed();
		m_borderBottomColor = borderBottomColor;
	}
	public String getBorderLeftColor() {
		return m_borderLeftColor;
	}
	public void setBorderLeftColor(String borderLeftColor) {
		if(! DomUtil.isEqual(borderLeftColor, m_borderLeftColor))
			changed();
		m_borderLeftColor = borderLeftColor;
	}
	public String getBorderRightColor() {
		return m_borderRightColor;
	}
	public void setBorderRightColor(String borderRightColor) {
		if(! DomUtil.isEqual(borderRightColor, m_borderRightColor))
			changed();
		m_borderRightColor = borderRightColor;
	}
	public String getBorderTopStyle() {
		return m_borderTopStyle;
	}
	public void setBorderTopStyle(String borderTopStyle) {
		if(! DomUtil.isEqual(borderTopStyle, m_borderTopStyle))
			changed();
		m_borderTopStyle = borderTopStyle;
	}
	public String getBorderBottomStyle() {
		return m_borderBottomStyle;
	}
	public void setBorderBottomStyle(String borderBottomStyle) {
		if(! DomUtil.isEqual(borderBottomStyle, m_borderBottomStyle))
			changed();
		m_borderBottomStyle = borderBottomStyle;
	}
	public String getBorderLeftStyle() {
		return m_borderLeftStyle;
	}
	public void setBorderLeftStyle(String borderLeftStyle) {
		if(! DomUtil.isEqual(borderLeftStyle, m_borderLeftStyle))
			changed();
		m_borderLeftStyle = borderLeftStyle;
	}
	public String getBorderRightStyle() {
		return m_borderRightStyle;
	}
	public void setBorderRightStyle(String borderRightStyle) {
		if(! DomUtil.isEqual(borderRightStyle, m_borderRightStyle))
			changed();
		m_borderRightStyle = borderRightStyle;
	}
	
	/*-- Border shortcut calls. --*/
	
	public void	setBorderWidth(int w) {
		setBorderLeftWidth(w);
		setBorderRightWidth(w);
		setBorderTopWidth(w);
		setBorderBottomWidth(w);
	}
	public void	setBorderStyle(String bs) {
		setBorderLeftStyle(bs);
		setBorderRightStyle(bs);
		setBorderTopStyle(bs);
		setBorderBottomStyle(bs);
	}
	public void	setBorderColor(String bs) {
		setBorderLeftColor(bs);
		setBorderRightColor(bs);
		setBorderTopColor(bs);
		setBorderBottomColor(bs);
	}
	public void	setBorder(int w) {
		setBorderWidth(w);
	}
	public void	setBorder(int w, String color, String style) {
		setBorderWidth(w);
		setBorderColor(color);
		setBorderStyle(style);
	}
	
	public ClearType getClear() {
		return m_clear;
	}
	public void setClear(ClearType clear) {
		if(! DomUtil.isEqual(clear, m_clear))
			changed();
		m_clear = clear;
	}
	public String getCursor() {
		return m_cursor;
	}
	public void setCursor(String cursor) {
		if(! DomUtil.isEqual(cursor, m_cursor))
			changed();
		m_cursor = cursor;
	}
	public DisplayType getDisplay() {
		return m_display;
	}
	public void setDisplay(DisplayType display) {
		if(! DomUtil.isEqual(display, m_display))
			changed();
		m_display = display;
	}
	public Overflow getOverflow() {
		return m_overflow;
	}
	public void setOverflow(Overflow overflow) {
		if(m_overflow != overflow)
			changed();
		m_overflow = overflow;
	}

	public FloatType getFloat() {
		return m_float;
	}
	public void setFloat(FloatType f) {
		m_float = f;
	}
	public PositionType getPosition() {
		return m_position;
	}
	public void setPosition(PositionType position) {
		m_position = position;
	}
	public VisibilityType getVisibility() {
		return m_visibility;
	}
	public void setVisibility(VisibilityType visibility) {
		m_visibility = visibility;
	}
	public String getHeight() {
		return m_height;
	}
	public void setHeight(String height) {
		m_height = height;
	}
	public String getLineHeight() {
		return m_lineHeight;
	}
	public void setLineHeight(String lineHeight) {
		m_lineHeight = lineHeight;
	}
	public String getMaxHeight() {
		return m_maxHeight;
	}
	public void setMaxHeight(String maxHeight) {
		m_maxHeight = maxHeight;
	}
	public String getMaxWidth() {
		return m_maxWidth;
	}
	public void setMaxWidth(String maxWidth) {
		m_maxWidth = maxWidth;
	}
	public String getMinHeight() {
		return m_minHeight;
	}
	public void setMinHeight(String minHeight) {
		m_minHeight = minHeight;
	}
	public String getMinWidth() {
		return m_minWidth;
	}
	public void setMinWidth(String minWidth) {
		m_minWidth = minWidth;
	}
	public String getWidth() {
		return m_width;
	}
	public void setWidth(String width) {
		m_width = width;
	}
	public String getFontFamily() {
		return m_fontFamily;
	}
	public void setFontFamily(String fontFamily) {
		m_fontFamily = fontFamily;
	}
	public String getFontSize() {
		return m_fontSize;
	}
	public void setFontSize(String fontSize) {
		m_fontSize = fontSize;
	}
	public String getFontSizeAdjust() {
		return m_fontSizeAdjust;
	}
	public void setFontSizeAdjust(String fontSizeAdjust) {
		m_fontSizeAdjust = fontSizeAdjust;
	}
	public FontStyle getFontStyle() {
		return m_fontStyle;
	}
	public void setFontStyle(FontStyle fontStyle) {
		m_fontStyle = fontStyle;
	}
	public FontVariant getFontVariant() {
		return m_fontVariant;
	}
	public void setFontVariant(FontVariant fontVariant) {
		m_fontVariant = fontVariant;
	}
	public String getFontWeight() {
		return m_fontWeight;
	}
	public void setFontWeight(String fontWeight) {
		m_fontWeight = fontWeight;
	}
	public int getZIndex() {
		return m_zIndex;
	}
	public void setZIndex(int index) {
		if(m_zIndex != index)
			changed();
		m_zIndex = index;
	}
	public int getTop() {
		return m_top;
	}
	public void setTop(int top) {
		if(m_top != top)
			changed();
		m_top = top;
	}
	public int getBottom() {
		return m_bottom;
	}
	public void setBottom(int bottom) {
		if(m_bottom != bottom)
			changed();
		m_bottom = bottom;
	}
	public int getLeft() {
		return m_left;
	}
	public void setLeft(int left) {
		if(m_left != left)
			changed();
		m_left = left;
	}
	public int getRight() {
		return m_right;
	}
	public void setRight(int right) {
		if(m_right != right)
			changed();
		m_right = right;
	}

	public String getColor() {
		return m_color;
	}

	public void setColor(String color) {
		if(DomUtil.isEqual(color, m_color))
			return;
		changed();
		m_color = color;
	}

	
}
