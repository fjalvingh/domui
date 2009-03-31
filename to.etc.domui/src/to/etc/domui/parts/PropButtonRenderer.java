package to.etc.domui.parts;

import java.awt.*;
import java.awt.font.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.text.*;
import java.util.*;

import javax.imageio.*;

import to.etc.domui.server.*;
import to.etc.domui.server.parts.*;
import to.etc.domui.util.resources.*;

/**
 * The actual renderer class for a property-file based renderer.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 21, 2008
 */
public class PropButtonRenderer {
	private DomApplication			m_application;
	private Properties				m_properties;
	private PropBtnPart.ButtonPartKey		m_key;
	private ResourceDependencyList	m_dependencies;
	protected BufferedImage			m_rootImage;
	private Graphics2D				m_graphics;
	protected BufferedImage			m_iconImage;

	public void		generate(PartResponse pr, DomApplication da, PropBtnPart.ButtonPartKey key, Properties p, ResourceDependencyList rdl) throws Exception {
		m_application	= da;
		m_properties	= p;
		m_key			= key;
		m_dependencies	= rdl;

		initialize();
		if(m_iconImage != null)
			renderIcon();
		renderText();
		
//
//		if(k.m_icon != null) {
//			BufferedImage iconbi = PartUtil.loadImage(da, k.m_icon);
//			renderIcon(bi, g, k, iconbi);
//		}
//		renderText(bi, g, k);
		compress(pr);
	}

	protected void	compress(PartResponse pr) throws Exception {
		ImageIO.write(m_rootImage, "PNG", pr.getOutputStream());
		pr.setMime("image/png");
	}

	protected void	initialize() throws Exception {
		initBackground();
		initGraphics();
		initAntiAliasing();
		initIcon();
	}

	protected void	initIcon() throws Exception {
		if(getKey().m_icon == null || getKey().m_icon.trim().length() == 0)
			return;

		m_iconImage	= loadImage("/"+getKey().m_icon);
	}

	protected void	initGraphics() {
		m_graphics = (Graphics2D)m_rootImage.getGraphics();
	}
	protected void	initAntiAliasing() {
		String s = getProperty("text.antialias");
		if(s == null || ! s.toLowerCase().startsWith("f")) {
			getGraphics().setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			getGraphics().setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		}
	}
	protected void	initBackground() throws Exception {
		String	rurl	= getKey().m_img == null ? getProperty("bg.image") : "/"+getKey().m_img;
		if(rurl != null) {
			m_rootImage 	= loadImage(rurl);
			String v = getKey().m_color == null ? getProperty("recolor.color") : getKey().m_color;
			if(v!=null && v.length() > 0)
				m_rootImage = recolor(m_rootImage, null);
			return;
		}
		throw new IllegalStateException("Missing 'bg.image' key in button properties file");
	}

	/**
	 * Experimental: try to recolor a b/w input into a colored one.
	 * @param src
	 * @param clr
	 * @return
	 */
	private BufferedImage	recolor(BufferedImage src, Color clr) {
		Color	target = getKey().m_color == null ? getColor("recolor.color", Color.orange) : PartUtil.makeColor(getKey().m_color);
		byte[]	ra	= new byte[256];
		byte[]	ga	= new byte[256];
		byte[]	ba	= new byte[256];
		byte[]	aa	= new byte[256];
		
		int	tr	= target.getRed();
		int	tg	= target.getGreen();
		int	tb	= target.getBlue();
		int	cr = 0, cg = 0, cb = 0;
		
		int		sci = getKey().m_start == -1 ? getInt("recolor.start", 0) : getKey().m_start;
		int		eci	= getKey().m_end == -1 ? getInt("recolor.end", 256) : getKey().m_end;

		int	i	= 0;
		while(i < sci) {
			aa[i] = (byte)i;
			ra[i] = ga[i] = ba[i] = (byte) 0;
			i++;
		}

		int	dt	= eci-sci;
		int	hdt	= dt / 2;
		int	o	= 0;
		while(i < eci) {
			aa[i] = (byte)i;

			cr = (tr*o+hdt) / dt;
			cg = (tg*o+hdt) / dt;
			cb = (tb*o+hdt) / dt;
			ra[i] = (byte)cr;
			ga[i] = (byte)cg;
			ba[i] = (byte)cb;
			i++;
			o++;
//			System.out.println("i="+i+"   r="+cr+", g="+cg+", b="+cb);
		}

		while(i < 256) {
			aa[i] = (byte)i;
			ra[i] = (byte)tr;
			ga[i] = (byte)tg;
			ba[i] = (byte)tb;
			i++;
		}
//		System.out.println("rt="+tr+", gt="+tg+", bt="+tb+", type="+src.getType());
		
		//-- order: green, red, alpha, blue
		LookupTable	lt	= new ByteLookupTable(0, new byte[][] {ga, ra, aa, ba});
//		LookupTable	lt	= new ByteLookupTable(0, ra);
		LookupOp	lop	= new LookupOp(lt, null);
//		BufferedImage	dest = lop.createCompatibleDestImage(src, null);
		return lop.filter(src, null);
//		return dest;
	}

	protected void	renderIcon() throws Exception {
		int		xoffset	= getInt("icon.xoffset", 10);
		int		yoffset	= getInt("icon.yoffset", 0);
		int		x	= xoffset;
		int		y	= 0;

		//-- Determine a position. Height is centered;
		String yalign = getProperty("icon.yalign", "center");
		if("center".equalsIgnoreCase(yalign)) {
			if(m_iconImage.getHeight() < m_rootImage.getHeight()) {
				y = (m_rootImage.getHeight() - m_iconImage.getHeight()) / 2+yoffset;
			}
		} else if("top".equalsIgnoreCase(yalign)) {
			y	= m_rootImage.getHeight() - yoffset;
		} else if("bottom".equalsIgnoreCase(yalign)) {
			y	= yoffset;
		} else
			throw new IllegalStateException("icon.yalign must be 'top', 'bottom', 'center'");
		getGraphics().drawImage(m_iconImage, x, y, null);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Text rendering code.								*/
	/*--------------------------------------------------------------*/
	private String			m_actualText;
	private int				m_acceleratorIndex = -1;
	private Font			m_textFont;
	private AttributedString	m_attributedString;
	private Color			m_textColor;

	protected void	renderText() throws Exception {
		if(getKey().m_text != null) {
			initTextFont();
			initTextColor();
			decodeAccelerator();
			initAttributedText();
			renderAttributedText();
		}
	}

	protected void	initTextColor() {
		String col	= getProperty("text.color", "000000");
		m_textColor = PartUtil.makeColor(col);
	}

	protected Color	getColor(String key, Color c) {
		String col	= getProperty(key, null);
		if(col == null)
			return c;
		return PartUtil.makeColor(col);
	}
	protected void	initTextFont() {
		String	family	= getProperty("text.font", "sans");
		String	style	= getProperty("text.style");
		int		size	= getInt("text.size", 12);
		m_textFont		= PartUtil.getFont(family, style, size);
//		System.out.println("Font is "+m_textFont);
	}

	protected void	decodeAccelerator() {
		//-- Create an attributed text thingy to render the accelerator with an underscore.
		String	txt	= getKey().m_text;
		if(txt == null) {
			m_actualText = null;
			m_acceleratorIndex = -1;
			return;
		}
		StringBuilder	sb = new StringBuilder(txt.length());
		int	accpos	= -1;
		int	ix	= 0;
		int	len	= txt.length();
		while(ix < len) {
			int	pos	= txt.indexOf('!', ix);
			if(pos == -1) {
				sb.append(txt, ix, len);
				break;
			}
			if(pos > 0 && txt.charAt(pos-1) == '\\') {
				//-- Escaped thingy?
				sb.append(txt, ix, pos-1);		// Copy excluding \\
				sb.append('!');
				ix	= pos+1;
			} else if(pos+1 < len && accpos == -1) {
				sb.append(txt, ix, pos);		// Copy upto !
				accpos	= sb.length();			// Accelerator is here,
				ix	= pos+1;
			} else {
				//-- 2nd accelerator..
				sb.append(txt, ix, pos+1);
				ix	= pos+1;
			}
		}

		m_actualText = sb.toString();
		m_acceleratorIndex = accpos;
	}

	protected void	initAttributedText() {
		//-- Create an Attributed string containing the text to render, with the accelerator underscored proper.
		m_attributedString = new AttributedString(m_actualText);
		m_attributedString.addAttribute(TextAttribute.FONT, m_textFont);
		m_attributedString.addAttribute(TextAttribute.FOREGROUND, m_textColor);
		m_attributedString.addAttribute(TextAttribute.SIZE, Float.valueOf(getInt("text.size", 12)));
		if(m_acceleratorIndex != -1) {
			m_attributedString.addAttribute(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_LOW_ONE_PIXEL, m_acceleratorIndex, m_acceleratorIndex+1);
		}
	}

	/**
	 * Render the actual, attributed text on the image. 
	 * @throws Exception
	 */
	protected void	renderAttributedText() throws Exception {
		//-- Prepare for rendering.
		FontRenderContext	frc = getGraphics().getFontRenderContext();
		TextLayout	layout = new TextLayout(m_attributedString.getIterator(), frc);
		Rectangle2D	r	= layout.getBounds();

		//-- Get rendering info
		String	xalign	= getProperty("text.xalign", "center");
		String	yalign	= getProperty("text.yalign", "center");
		int		xoffset	= getInt("text.xoffset", 0);
		int		yoffset	= getInt("text.yoffset", 0);

		//-- Calculate an Y position; this is independent of any icon rendered
		int		y	= 0;
		if("center".equals(yalign)) {
			int		cy	= (m_rootImage.getHeight() - (int)r.getHeight()) / 2;
			y	+= cy - (int)(r.getY()) + yoffset;
		} else if("top".equals(yalign)) {
			y	= m_rootImage.getHeight() - (int)r.getHeight() + yoffset;
		} else if("bottom".equals(yalign)) {
			y	= yoffset;
		} else
			throw new IllegalStateException("text.yalign must be top, bottom or center");

		//-- Calculate an X position; this one depends on the presence of any icon.
		int	x = 0;
		if(m_iconImage != null) {
			//-- Calculate a fence x
			String s = getProperty("text.icon.xalign");
			if(s != null)
				xalign = s;
			int t = getInt("text.icon.xoffset", -999);
			if(t != -999)
				xoffset = t;
			
			int	xfence = m_iconImage.getWidth() + getInt("icon.xoffset", 10);
			int	xiconoff	= getInt("text.iconoffset", 2);
			xfence	+= xiconoff;

			if("center".equals(xalign)) {
				int		cx	= (m_rootImage.getWidth() - (int)r.getWidth() - xfence) / 2;
				if(cx < 0)
					cx = 0;
				x	= xfence+cx+xoffset;
			} else if("left".equals(xalign)) {
				x	= xfence+xoffset;
			} else if("right".equals(xalign)) {
				x	= m_rootImage.getWidth() - (int)r.getWidth() - xoffset;
			} else
				throw new IllegalStateException("text.xalign must be center,left,right");
		} else {
			if("center".equals(xalign)) {
				int		cx	= (m_rootImage.getWidth() - (int)r.getWidth()) / 2;
				if(cx < 0)
					cx = 0;
				x	= xoffset+cx;
			} else if("left".equals(xalign)) {
				x	= xoffset;
			} else if("right".equals(xalign)) {
				x	= m_rootImage.getWidth() - (int)r.getWidth() - xoffset;
			} else
				throw new IllegalStateException("text.xalign must be center,left,right");
		}
		layout.draw(getGraphics(), x, y);
	}
	
	/*--------------------------------------------------------------*/
	/*	CODING:	Utility stuff..										*/
	/*--------------------------------------------------------------*/
	
	protected BufferedImage		loadImage(String rurl) throws Exception {
		rurl = rurl.trim();
		String	name;
		if(rurl.startsWith("/")) {
			name = rurl.substring(1);
		} else {
			//-- Add the path to the properties file.
			int pos = getKey().m_propfile.lastIndexOf('/');
			if(pos == -1)
				name = rurl;
			else
				name = getKey().m_propfile.substring(0, pos+1)+rurl;
		}
		return PartUtil.loadImage(getApplication(), name, getDependencies());
	}

	public DomApplication getApplication() {
		return m_application;
	}
	public ResourceDependencyList getDependencies() {
		return m_dependencies;
	}
	public PropBtnPart.ButtonPartKey getKey() {
		return m_key;
	}
	public Properties getProperties() {
		return m_properties;
	}
	public String	getProperty(String key) {
		return getProperties().getProperty(key);
	}
	public String	getProperty(String key, String dflt) {
		String s = getProperty(key);
		if(s == null || s.trim().length() == 0)
			return dflt;
		return s;
	}
	public int		getInt(String key, int dflt) {
		String s = getProperty(key);
		if(s != null) {
			try {
				return Integer.parseInt(s.trim());
			} catch(Exception x) {
			}
		}
		return dflt;
	}
	public Graphics2D getGraphics() {
		return m_graphics;
	}
}
