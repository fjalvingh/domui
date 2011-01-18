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
package to.etc.domui.parts;

import java.awt.*;
import java.awt.font.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.text.*;
import java.util.*;

import javax.annotation.*;
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
	private DomApplication m_application;

	private Properties m_properties;

	private PropBtnPart.ButtonPartKey m_key;

	private IResourceDependencyList m_dependencies;

	protected BufferedImage m_rootImage;

	private Graphics2D m_targetGraphics;

	protected BufferedImage m_iconImage;

	public void generate(PartResponse pr, DomApplication da, PropBtnPart.ButtonPartKey key, Properties p, @Nonnull IResourceDependencyList rdl) throws Exception {
		m_application = da;
		m_properties = p;
		m_key = key;
		m_dependencies = rdl;

		try {
			initBackground();
			initIcon();
			initTextFont();
			initTextColor();
			decodeAccelerator();

			initAttributedText();

			//-- Everything is known. Calculate how much space icon+text will take.
			FontRenderContext frc = getGraphics().getFontRenderContext();
			TextLayout layout = new TextLayout(m_attributedString.getIterator(), frc);
			Rectangle2D r = layout.getBounds();

			int totalwidth = (int) r.getWidth();
			if(m_iconImage != null) {
				totalwidth += m_iconImage.getWidth();

				int t = getInt("text.icon.xoffset", -999);
				if(t != -999)
					totalwidth += t;

				t = m_iconImage.getWidth() + getInt("icon.xoffset", 10);
				totalwidth += t;
				t = getInt("text.iconoffset", 2);
				totalwidth += t;
			} else {
				totalwidth += 20;
			}
			//			System.out.println("totalwidth=" + totalwidth + ", x=" + r.getX() + ", y=" + r.getY() + ", w=" + r.getWidth() + ", h=" + r.getHeight());

			if(totalwidth > m_rootImage.getWidth()) {
				growRootWider(totalwidth);
			}


			if(m_iconImage != null)
				renderIcon();
			if(getKey().m_text != null) {
				renderAttributedText();
			}
			compress(pr);
		} finally {
			try {
				if(null != m_targetGraphics)
					m_targetGraphics.dispose();
			} catch(Exception x) {}
		}
	}

	/**
	 * Grow the root image wider, to the specified pixel width, by replicating a single
	 * vertical line.
	 * @param totalwidth
	 */
	private void growRootWider(int totalwidth) {
		BufferedImage newbi = new BufferedImage(totalwidth, m_rootImage.getHeight(), m_rootImage.getType());

		int split = getInt("split", m_rootImage.getWidth() / 2); // Get the splice point, default to middle of image width

		BufferedImage leftbi = m_rootImage.getSubimage(0, 0, split, m_rootImage.getHeight()); // Left side of the result
		Graphics2D g2d = (Graphics2D) newbi.getGraphics();
		try {
			g2d.drawImage(leftbi, 0, 0, null);

			//-- Get the splice image;
			BufferedImage	splice = m_rootImage.getSubimage(split, 0, 1, m_rootImage.getHeight());	// Get a 1-pixel wide splice
			int leftsz = m_rootImage.getWidth() - split;
			int gapwidth = totalwidth - m_rootImage.getWidth();
			for(int x = split, i = gapwidth; --i >= 0; x++) {
				g2d.drawImage(splice, x, 0, null); // Replicate splice over the gap
			}

			//-- Finally: append the right size.
			BufferedImage rightbi = m_rootImage.getSubimage(split, 0, m_rootImage.getWidth() - split, m_rootImage.getHeight());
			g2d.drawImage(rightbi, split + gapwidth, 0, null); // Replicate splice over the gap

			m_rootImage = newbi;
			if(null != m_targetGraphics)
				m_targetGraphics.dispose();
			m_targetGraphics = null;
		} finally {
			try {
				g2d.dispose();
			} catch(Exception x) {}
		}
	}

	protected void compress(PartResponse pr) throws Exception {
		ImageIO.write(m_rootImage, "PNG", pr.getOutputStream());
		pr.setMime("image/png");
	}

	protected void initIcon() throws Exception {
		if(getKey().m_icon == null || getKey().m_icon.trim().length() == 0)
			return;

		m_iconImage = loadImage("/" + getKey().m_icon);
	}

	public Graphics2D getGraphics() {
		if(m_targetGraphics == null) {
			m_targetGraphics = (Graphics2D) m_rootImage.getGraphics();
			initAntiAliasing();


		}
		return m_targetGraphics;
	}

	protected void initAntiAliasing() {
		String s = getProperty("text.antialias");
		if(s == null || !s.toLowerCase().startsWith("f")) {
			getGraphics().setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			getGraphics().setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		}
	}

	protected void initBackground() throws Exception {
		String rurl = getKey().m_img == null ? getProperty("bg.image") : "/" + getKey().m_img;
		if(rurl != null) {
			m_rootImage = loadImage(rurl);
			return;
		}
		throw new IllegalStateException("Missing 'bg.image' key in button properties file");
	}

	protected void renderIcon() throws Exception {
		int xoffset = getInt("icon.xoffset", 10);
		int yoffset = getInt("icon.yoffset", 0);
		int x = xoffset;
		int y = 0;

		//-- Determine a position. Height is centered;
		String yalign = getProperty("icon.yalign", "center");
		if("center".equalsIgnoreCase(yalign)) {
			if(m_iconImage.getHeight() < m_rootImage.getHeight()) {
				y = (m_rootImage.getHeight() - m_iconImage.getHeight()) / 2 + yoffset;
			}
		} else if("top".equalsIgnoreCase(yalign)) {
			y = m_rootImage.getHeight() - yoffset;
		} else if("bottom".equalsIgnoreCase(yalign)) {
			y = yoffset;
		} else
			throw new IllegalStateException("icon.yalign must be 'top', 'bottom', 'center'");
		getGraphics().drawImage(m_iconImage, x, y, null);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Text rendering code.								*/
	/*--------------------------------------------------------------*/
	private String m_actualText;

	private int m_acceleratorIndex = -1;

	private Font m_textFont;

	private AttributedString m_attributedString;

	private Color m_textColor;

	//	protected void renderText() throws Exception {
	//		if(getKey().m_text != null) {
	//			initTextFont();
	//			initTextColor();
	//			decodeAccelerator();
	//			initAttributedText();
	//			renderAttributedText();
	//		}
	//	}

	protected void initTextColor() {
		String col = getProperty("text.color", "000000");
		m_textColor = PartUtil.makeColor(col);
	}

	protected Color getColor(String key, Color c) {
		String col = getProperty(key, null);
		if(col == null)
			return c;
		return PartUtil.makeColor(col);
	}

	protected void initTextFont() {
		String family = getProperty("text.font", "sans");
		String style = getProperty("text.style");
		int size = getInt("text.size", 12);
		m_textFont = PartUtil.getFont(family, style, size);
		//		System.out.println("Font is "+m_textFont);
	}

	protected void decodeAccelerator() {
		//-- Create an attributed text thingy to render the accelerator with an underscore.
		String txt = getKey().m_text;
		if(txt == null) {
			m_actualText = "";
			m_acceleratorIndex = -1;
			return;
		}
		StringBuilder sb = new StringBuilder(txt.length());
		int accpos = -1;
		int ix = 0;
		int len = txt.length();
		while(ix < len) {
			int pos = txt.indexOf('!', ix);
			if(pos == -1) {
				sb.append(txt, ix, len);
				break;
			}
			if(pos > 0 && txt.charAt(pos - 1) == '\\') {
				//-- Escaped thingy?
				sb.append(txt, ix, pos - 1); // Copy excluding \\
				sb.append('!');
				ix = pos + 1;
			} else if(pos + 1 < len && accpos == -1) {
				sb.append(txt, ix, pos); // Copy upto !
				accpos = sb.length(); // Accelerator is here,
				ix = pos + 1;
			} else {
				//-- 2nd accelerator..
				sb.append(txt, ix, pos + 1);
				ix = pos + 1;
			}
		}

		m_actualText = sb.toString();
		m_acceleratorIndex = accpos;
	}

	protected void initAttributedText() {
		//-- Create an Attributed string containing the text to render, with the accelerator underscored proper.
		m_attributedString = new AttributedString(m_actualText);
		m_attributedString.addAttribute(TextAttribute.FONT, m_textFont);
		m_attributedString.addAttribute(TextAttribute.FOREGROUND, m_textColor);
		m_attributedString.addAttribute(TextAttribute.SIZE, Float.valueOf(getInt("text.size", 12)));
		if(m_acceleratorIndex != -1) {
			m_attributedString.addAttribute(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_LOW_ONE_PIXEL, m_acceleratorIndex, m_acceleratorIndex + 1);
		}
	}

	/**
	 * Render the actual, attributed text on the image.
	 * @throws Exception
	 */
	protected void renderAttributedText() throws Exception {
		//-- Prepare for rendering.
		FontRenderContext frc = getGraphics().getFontRenderContext();
		TextLayout layout = new TextLayout(m_attributedString.getIterator(), frc);
		Rectangle2D r = layout.getBounds();

		//-- Get rendering info
		String xalign = getProperty("text.xalign", "center");
		String yalign = getProperty("text.yalign", "center");
		int xoffset = getInt("text.xoffset", 0);
		int yoffset = getInt("text.yoffset", 0);

		//-- Calculate an Y position; this is independent of any icon rendered
		int y = 0;
		if("center".equals(yalign)) {
			int cy = (m_rootImage.getHeight() - (int) r.getHeight()) / 2;
			y += cy - (int) (r.getY()) + yoffset;
		} else if("top".equals(yalign)) {
			y = m_rootImage.getHeight() - (int) r.getHeight() + yoffset;
		} else if("bottom".equals(yalign)) {
			y = yoffset;
		} else
			throw new IllegalStateException("text.yalign must be top, bottom or center");

		//-- Calculate an X position; this one depends on the presence of any icon.
		int x = 0;
		if(m_iconImage != null) {
			//-- Calculate a fence x
			String s = getProperty("text.icon.xalign");
			if(s != null)
				xalign = s;
			int t = getInt("text.icon.xoffset", -999);
			if(t != -999)
				xoffset = t;

			int xfence = m_iconImage.getWidth() + getInt("icon.xoffset", 10);
			int xiconoff = getInt("text.iconoffset", 2);
			xfence += xiconoff;

			if("center".equals(xalign)) {
				int cx = (m_rootImage.getWidth() - (int) r.getWidth() - xfence) / 2;
				if(cx < 0)
					cx = 0;
				x = xfence + cx + xoffset;
			} else if("left".equals(xalign)) {
				x = xfence + xoffset;
			} else if("right".equals(xalign)) {
				x = m_rootImage.getWidth() - (int) r.getWidth() - xoffset;
			} else
				throw new IllegalStateException("text.xalign must be center,left,right");
		} else {
			if("center".equals(xalign)) {
				int cx = (m_rootImage.getWidth() - (int) r.getWidth()) / 2;
				if(cx < 0)
					cx = 0;
				x = xoffset + cx;
			} else if("left".equals(xalign)) {
				x = xoffset;
			} else if("right".equals(xalign)) {
				x = m_rootImage.getWidth() - (int) r.getWidth() - xoffset;
			} else
				throw new IllegalStateException("text.xalign must be center,left,right");
		}
		layout.draw(getGraphics(), x, y);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Utility stuff..										*/
	/*--------------------------------------------------------------*/

	protected BufferedImage loadImage(String rurl) throws Exception {
		rurl = rurl.trim();
		String name;
		if(rurl.startsWith("/")) {
			name = rurl.substring(1);
		} else {
			//-- Add the path to the properties file.
			int pos = getKey().m_propfile.lastIndexOf('/');
			if(pos == -1)
				name = rurl;
			else
				name = getKey().m_propfile.substring(0, pos + 1) + rurl;
		}
		return PartUtil.loadImage(getApplication(), name, getDependencies());
	}

	public DomApplication getApplication() {
		return m_application;
	}

	public IResourceDependencyList getDependencies() {
		return m_dependencies;
	}

	public PropBtnPart.ButtonPartKey getKey() {
		return m_key;
	}

	public Properties getProperties() {
		return m_properties;
	}

	public String getProperty(String key) {
		return getProperties().getProperty(key);
	}

	public String getProperty(String key, String dflt) {
		String s = getProperty(key);
		if(s == null || s.trim().length() == 0)
			return dflt;
		return s;
	}

	public int getInt(String key, int dflt) {
		String s = getProperty(key);
		if(s != null) {
			try {
				return Integer.parseInt(s.trim());
			} catch(Exception x) {}
		}
		return dflt;
	}
}
