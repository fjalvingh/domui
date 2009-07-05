package to.etc.domui.component.buttons;

import to.etc.domui.dom.html.*;
import to.etc.domui.parts.*;
import to.etc.domui.state.*;
import to.etc.util.*;

/**
 * An HTML button containing a rendered image as the button content.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 21, 2008
 */
public class ImageButton extends Button {
	private final Img m_img;

	private String m_baseSrc;

	private String m_text;

	private String m_color;

	private int m_size = -1;

	private String m_style;

	private String m_font;

	private String m_icon;

	public ImageButton() {
		m_img = new Img();
		add(m_img);
		m_img.setBorder(0);
		setCssClass("ui-ib");
		setThemeImage("bg-button-3.png");
		setStyle("b");
		setColor("black");
	}

	public ImageButton(final String txt) {
		this();
		setLiteralText(txt);
	}

	/**
	 * Uses a resource as the base for the image.
	 * @param resourceBase
	 * @param name
	 */
	public void setImage(final Class< ? > resourceBase, final String name) {
		String rb = resourceBase.getName();
		int pos = rb.lastIndexOf('.');
		if(pos == -1)
			throw new IllegalStateException("??");
		m_baseSrc = "RES/" + rb.substring(0, pos + 1).replace('.', '/') + name;
		genURL();
	}

	/**
	 * Uses a resource as the base for the image.
	 * @param resourceBase
	 * @param name
	 */
	public void setIconImage(final Class< ? > resourceBase, final String name) {
		String rb = resourceBase.getName();
		int pos = rb.lastIndexOf('.');
		if(pos == -1)
			throw new IllegalStateException("??");
		m_icon = "RES/" + rb.substring(0, pos + 1).replace('.', '/') + name;
		genURL();
	}

	public void setIcon(final String name) {
		m_icon = name;
		genURL();
	}

	public void setThemeIcon(final String name) {
		m_icon = PageContext.getRequestContext().getRelativeThemePath(name);
		genURL();
	}

	/**
	 * Uses a resource as the base for the image.
	 * @param resourceBase
	 * @param name
	 */
	public void setImage(final String name) {
		m_baseSrc = name;
		genURL();
	}

	public void setThemeImage(final String src) {
		m_baseSrc = PageContext.getRequestContext().getRelativeThemePath(src);
		genURL();
	}

	private void genURL() {
		StringBuilder sb = new StringBuilder(128);
		sb.append(PropBtnPart.class.getName());
		sb.append(".part?img=");
		sb.append(m_baseSrc);
		if(m_text != null) {
			sb.append("&amp;txt=");
			StringTool.encodeURLEncoded(sb, m_text);
		}
		if(m_color != null) {
			sb.append("&amp;color=");
			StringTool.encodeURLEncoded(sb, m_color);
		}
		if(m_font != null) {
			sb.append("&amp;font=");
			StringTool.encodeURLEncoded(sb, m_font);
		}
		if(m_style != null) {
			sb.append("&amp;style=");
			StringTool.encodeURLEncoded(sb, m_style);
		}
		if(m_size > 0) {
			sb.append("&amp;size=");
			sb.append(m_size);
		}
		if(m_icon != null) {
			sb.append("&amp;icon=");
			StringTool.encodeURLEncoded(sb, m_icon);
		}
		m_img.setSrc(sb.toString());
	}

	public String getText() {
		return m_text;
	}

	@Override
	public void setLiteralText(final String text) {
		m_text = text;
		decodeAccelerator(text);
		genURL();
	}

	//
	//	public String getColor() {
	//		return m_color;
	//	}
	//
	//	public void setColor(String color) {
	//		m_color = color;
	//		genURL();
	//	}
	//
	public int getSize() {
		return m_size;
	}

	public void setSize(final int size) {
		m_size = size;
		genURL();
	}

	public String getStyle() {
		return m_style;
	}

	public void setStyle(final String style) {
		m_style = style;
		genURL();
	}

	public String getFont() {
		return m_font;
	}

	public void setFont(final String font) {
		m_font = font;
		genURL();
	}

	public String getFontColor() {
		return m_color;
	}

	public void setFontColor(final String color) {
		m_color = color;
	}

	private void decodeAccelerator(final String txt) {
		int ix = 0;
		int len = txt.length();
		while(ix < len) {
			int pos = txt.indexOf('!', ix);
			if(pos == -1)
				return;
			if(pos > 0 && txt.charAt(pos - 1) == '\\') {
				//-- Escaped. Try next one.
				ix = pos + 1;
			} else {
				if(pos + 1 >= len)
					return;
				char c = txt.charAt(pos + 1);
				if(Character.isLetter(c)) {
					c = Character.toLowerCase(c);
					setAccessKey(c);
					return;
				}
			}
		}
	}
}
