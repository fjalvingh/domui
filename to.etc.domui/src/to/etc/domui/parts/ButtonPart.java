package to.etc.domui.parts;

import to.etc.domui.server.*;
import to.etc.domui.server.parts.*;
import to.etc.domui.util.resources.*;

/**
 * This creates a button part from an input URL. The input url has
 * as parameter the image name, the button text and the like.
 *
 * <p>Created on January 23, 2006
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
@Deprecated
public class ButtonPart implements BufferedPartFactory {
	static class ButtonPartKey {
		String		m_image;

		String		m_icon;

		String		m_text;

		String		m_color;

		String		m_size;

		String		m_style;

		String		m_font;

		boolean		m_antialias;

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + (m_antialias ? 1231 : 1237);
			result = prime * result + ((m_color == null) ? 0 : m_color.hashCode());
			result = prime * result + ((m_font == null) ? 0 : m_font.hashCode());
			result = prime * result + ((m_image == null) ? 0 : m_image.hashCode());
			result = prime * result + ((m_size == null) ? 0 : m_size.hashCode());
			result = prime * result + ((m_style == null) ? 0 : m_style.hashCode());
			result = prime * result + ((m_text == null) ? 0 : m_text.hashCode());
			result = prime * result + ((m_icon == null) ? 0 : m_icon.hashCode());
			return result;
		}

		@Override
		public boolean equals(final Object obj) {
			if(this == obj)
				return true;
			if(obj == null)
				return false;
			if(getClass() != obj.getClass())
				return false;
			final ButtonPartKey other = (ButtonPartKey) obj;
			if(m_antialias != other.m_antialias)
				return false;
			if(m_color == null) {
				if(other.m_color != null)
					return false;
			}
			else if(!m_color.equals(other.m_color))
				return false;
			if(m_font == null) {
				if(other.m_font != null)
					return false;
			}
			else if(!m_font.equals(other.m_font))
				return false;
			if(m_image == null) {
				if(other.m_image != null)
					return false;
			}
			else if(!m_image.equals(other.m_image))
				return false;
			if(m_size == null) {
				if(other.m_size != null)
					return false;
			}
			else if(!m_size.equals(other.m_size))
				return false;
			if(m_style == null) {
				if(other.m_style != null)
					return false;
			}
			else if(!m_style.equals(other.m_style))
				return false;
			if(m_text == null) {
				if(other.m_text != null)
					return false;
			}
			else if(!m_text.equals(other.m_text))
				return false;

			if(m_icon == null) {
				if(other.m_icon != null)
					return false;
			}
			else if(!m_icon.equals(other.m_icon))
				return false;
			return true;
		}
	}

	public Object decodeKey(final String rurl, final IParameterInfo info) throws Exception {
		ButtonPartKey	k = new ButtonPartKey();
		k.m_image	= info.getParameter("img");
		k.m_text	= info.getParameter("txt");
		k.m_color	= info.getParameter("color");
		k.m_size	= info.getParameter("size");
		k.m_style	= info.getParameter("style");
		k.m_font	= info.getParameter("font");
		k.m_icon	= info.getParameter("icon");
		String s = info.getParameter("antialias");
		k.m_antialias	= ! "false".equals(s);
		if(k.m_image == null)
			throw new IllegalStateException("Missing img attribute");
		return k;
	}

	public void generate(final PartResponse pr, final DomApplication da, final Object key, final ResourceDependencyList rdl) throws Exception {
		if(true)
			throw new IllegalStateException("DO NOT USE ANYMORE - REPLACED WITH PropBtnPart.part");
//		ButtonPartKey k = (ButtonPartKey) key;
//		System.out.println("Recreating image "+k.m_image);
//		BufferedImage bi = PartUtil.loadImage(da, k.m_image, rdl);
//		Graphics2D	g	= (Graphics2D) bi.getGraphics();
//		if(k.m_antialias) {
//			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//			g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
//		}
//
//		if(k.m_icon != null) {
//			BufferedImage iconbi = PartUtil.loadImage(da, k.m_icon, rdl);
//			renderIcon(bi, g, k, iconbi);
//		}
//		renderText(bi, g, k);
//		ImageIO.write(bi, "PNG", pr.getOutputStream());
//		pr.setMime("image/png");
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Image generator.									*/
	/*--------------------------------------------------------------*/
//	private void	renderIcon(final BufferedImage bi, final Graphics2D g, final ButtonPartKey k, final BufferedImage iconbi) throws Exception {
//		//-- Determine a position. Height is centered;
//		int h = 0;
//		if(iconbi.getHeight() < bi.getHeight()) {
//			h = (bi.getHeight() - iconbi.getHeight()) / 2;
//		}
//		int w = 12;
//		g.drawImage(iconbi, w, h, null);
//	}
//
//	/**
//	 * Render a text.. Options are:
//	 * font:	the name of the font to use,
//	 * size:	the size in points of the font
//	 * style:	italic, bold
//	 * fg:		the fgcolor of the font, in #xxxxxx format,
//	 * ax, ay:	absolute pos within the image
//	 * cx, cy:	offset from the center of the image
//	 * @param g
//	 * @param i
//	 */
//	private void	renderText(final BufferedImage bi, final Graphics2D g, final ButtonPartKey k) throws Exception {
//		String	font	= k.m_font == null ? "sans" : k.m_font;
//		int		size	= StringTool.strToInt(k.m_size, 12);
//		Color	fg		= PartUtil.makeColor(k.m_color == null ? "#ffffff" : k.m_color );
//		int		style	= 0;
//		if(k.m_style != null) {
//			if(k.m_style.indexOf("i") != -1) style |= Font.ITALIC;
//			if(k.m_style.indexOf("b") != -1) style |= Font.BOLD;
//		}
//
////		System.out.println("Color is "+m_color+": "+fg);
//
//		//-- Create the appropriate font,
//		Font	f	= new Font(font, style, size);
//		String	txt	= k.m_text == null || k.m_text.length() == 0 ? "undef" : k.m_text;
//
//		//-- Create an attributed text thingy to render the accelerator with an underscore.
//		StringBuilder	sb = new StringBuilder(txt.length());
//		int	accpos	= -1;
//		int	ix	= 0;
//		int	len	= txt.length();
//		while(ix < len) {
//			int	pos	= txt.indexOf('!', ix);
//			if(pos == -1) {
//				sb.append(txt, ix, len);
//				break;
//			}
//			if(pos > 0 && txt.charAt(pos-1) == '\\') {
//				//-- Escaped thingy?
//				sb.append(txt, ix, pos-1);		// Copy excluding \\
//				sb.append('!');
//				ix	= pos+1;
//			} else if(pos+1 < len && accpos == -1) {
//				sb.append(txt, ix, pos);		// Copy upto !
//				accpos	= sb.length();			// Accelerator is here,
//				ix	= pos+1;
//			} else {
//				//-- 2nd accelerator..
//				sb.append(txt, ix, pos+1);
//				ix	= pos+1;
//			}
//		}
//
//		String	actual = sb.toString();
//
//		//-- Create an Attributed string containing the text to render, with the accelerator underscored proper.
//		AttributedString	as	= new AttributedString(actual);
//		as.addAttribute(TextAttribute.FONT, f);
//		as.addAttribute(TextAttribute.FOREGROUND, fg);
//		as.addAttribute(TextAttribute.SIZE, Float.valueOf(size));
//		if(accpos != -1) {
//			as.addAttribute(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_LOW_ONE_PIXEL, accpos, accpos+1);
//		}
//
//		//-- Prepare for rendering.
//		FontRenderContext	frc = g.getFontRenderContext();
//		TextLayout	layout = new TextLayout(as.getIterator(), frc);
//		Rectangle2D	r	= layout.getBounds();
//
//		int	x = 0, y = 1;
//
//		//-- Calculate the center of the image;
//		int		cx	= (bi.getWidth() - (int)r.getWidth()) / 2;
//		int		cy	= (bi.getHeight() - (int)r.getHeight()) / 2;
//
//		//-- Adjust these with the calculated values;
//		x	+= cx;
//		y	+= cy - (int)(r.getY());
//
//		layout.draw(g, x, y);
////
////		//-- Now the center to align across is in (x, y). Use the size to actually place the string..
//////		System.out.println("R: "+xy(r.getX(), r.getY())+" - "+xy(r.getWidth(), r.getHeight()));
//////		System.out.println("at "+xy((double)x, (double)y)+": "+txt);
////
////		//-- Render the text;
//		System.out.println("Font is "+f.getFontName());
//	}

	/**
	 * Render a text.. Options are:
	 * font:	the name of the font to use,
	 * size:	the size in points of the font
	 * style:	italic, bold
	 * fg:		the fgcolor of the font, in #xxxxxx format,
	 * ax, ay:	absolute pos within the image
	 * cx, cy:	offset from the center of the image
	 * @param g
	 * @param i
	 */
//	private void	renderText_old(BufferedImage bi, Graphics2D g, ButtonPartKey k) throws Exception
//	{
//		String	font	= k.m_font == null ? "sans" : k.m_font;
//		int		size	= StringTool.strToInt(k.m_size, 12);
//		Color	fg		= PartUtil.makeColor(k.m_color == null ? "#ffffff" : k.m_color );
//		int		style	= 0;
//		if(k.m_style != null)
//		{
//			if(k.m_style.indexOf("i") != -1) style |= Font.ITALIC;
//			if(k.m_style.indexOf("b") != -1) style |= Font.BOLD;
//		}
////		System.out.println("Color is "+m_color+": "+fg);
//
//		//-- Create the appropriate font,
//		Font	f	= new Font(font, style, size);
//		String	txt	= k.m_text == null || k.m_text.length() == 0 ? "undef" : k.m_text;
//
//		int	x, y;
//		x	= -1;
//		y	= -1;
//		if(x == -1 || y == -1)
//		{
//			//-- Relative?
//			x	= 0;
//			y	= 0;
//
//			FontRenderContext	frc = g.getFontRenderContext();
//			Rectangle2D	r = f.getStringBounds(txt, frc);
//
//			//-- Calculate the center of the image;
//			int		cx	= (bi.getWidth() - (int)r.getWidth()) / 2;
//			int		cy	= (bi.getHeight() - (int)r.getHeight()) / 2;
//
//			//-- Adjust these with the calculated values;
//			x	+= cx;
//			y	+= cy - (int)(r.getY());
//
//			//-- Now the center to align across is in (x, y). Use the size to actually place the string..
////			System.out.println("R: "+xy(r.getX(), r.getY())+" - "+xy(r.getWidth(), r.getHeight()));
//		}
////		System.out.println("at "+xy((double)x, (double)y)+": "+txt);
//
//		//-- Render the text;
//		System.out.println("Font is "+f.getFontName());
//		g.setFont(f);
//		g.setColor(fg);
//		g.drawString(txt, x, y);
//	}
}
