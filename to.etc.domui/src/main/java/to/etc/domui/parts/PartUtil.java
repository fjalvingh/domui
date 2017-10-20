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

import org.apache.batik.transcoder.SVGAbstractTranscoder;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;
import to.etc.domui.server.DomApplication;
import to.etc.domui.server.IParameterInfo;
import to.etc.domui.trouble.ThingyNotFoundException;
import to.etc.domui.util.resources.IResourceDependencyList;
import to.etc.domui.util.resources.IResourceRef;
import to.etc.sjit.ImaTool;
import to.etc.util.StringInputStream;

import javax.annotation.Nonnull;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

public class PartUtil {
	private PartUtil() {}

	static public int getInt(IParameterInfo param, String name, int def) {
		String v = param.getParameter(name);
		if(v == null)
			return def;
		v = v.trim();
		if(v.length() == 0)
			return def;
		try {
			return Integer.parseInt(v);
		} catch(Exception x) {
			return def;
		}
	}


	static private boolean isa(String name, String ext) {
		int pos = name.lastIndexOf('.');
		if(pos == -1)
			return false;
		return name.substring(pos + 1).equalsIgnoreCase(ext);
	}

	/**
	 * Loads a properties file from a resource string.
	 * @param da
	 * @param src
	 * @return
	 * @throws Exception
	 */
	@Nonnull
	static public Properties loadProperties(DomApplication da, String src, IResourceDependencyList rdl) throws Exception {
		String svg = da.internalGetThemeManager().getThemeReplacedString(rdl, src);

		InputStream is = new StringInputStream(svg, "utf-8");
		try {
			Properties p = new Properties();
			p.load(is);
			return p;
		} finally {
			try {
				is.close();
			} catch(Exception x) {}
		}
	}

	static public String getURI(String in) {
		if(in == null)
			return null;
		int pos = in.indexOf('?');
		if(pos == -1)
			return in;
		return in.substring(0, pos);
	}

	static public IParameterInfo getParameters(String in) {
		if(in == null)
			return null;
		int pos = in.indexOf('?');
		if(pos == -1)
			return null;
		return new ParameterInfoImpl(in.substring(pos + 1));
	}

	/**
	 * Load an image, either through a resource (when the part starts with RES) or as a webapp file.
	 */
	static public BufferedImage loadImage(DomApplication da, String in, @Nonnull IResourceDependencyList rdl) throws Exception {
		//-- Split input in URL and parameters (QD for generic retrieval of resources)
		String image = getURI(in);
		IParameterInfo param = getParameters(in);

		IResourceRef ref = da.getResource(image, rdl);
//		if(ref == null)
//			throw new ThingyNotFoundException("The image '" + image + "' was not found.");
		InputStream is = ref.getInputStream();
		if(is == null)
			throw new ThingyNotFoundException("The image '" + image + "' was not found.");
		try {
			BufferedImage bi = null;

			if(isa(image, "gif"))
				bi = ImaTool.loadGIF(is);
			else if(isa(image, "jpg") || isa(image, "jpeg"))
				bi = ImaTool.loadJPEG(is);
			else if(isa(image, "png"))
				bi = ImaTool.loadPNG(is);
			else if(image.endsWith("svg")) {
				bi = loadSvg(da, rdl, image, param);
			} else
				throw new IllegalArgumentException("The image '" + image + "' must be .gif, .jpg or .jpeg");

			//			System.out.println("size of image is "+xy(m_src_bi.getWidth(), m_src_bi.getHeight()));

			//-- Convert the image to a full-color image
			if(bi.getType() == BufferedImage.TYPE_INT_ARGB)
				return bi;
			BufferedImage newbi = new BufferedImage(bi.getWidth(), bi.getHeight(), BufferedImage.TYPE_INT_ARGB);
			Graphics2D g2d = (Graphics2D) newbi.getGraphics();
			g2d.drawImage(bi, 0, 0, null);
			return newbi;
		} finally {
			try {
				is.close();
			} catch(Exception x) {}
		}
	}

	private static BufferedImage loadSvg(DomApplication da, IResourceDependencyList rdl, String image, IParameterInfo param) throws Exception {
		//-- 1. Get the input as a theme-replaced resource
		String svg = da.internalGetThemeManager().getThemeReplacedString(rdl, image);

		//-- 2. Now generate the thingy using the Batik transcoder:
		BufferedImageTranscoder bit = new BufferedImageTranscoder();
		TranscoderInput in = new TranscoderInput(new StringReader(svg));

		int w = PartUtil.getInt(param, "w", -1);
		int h = PartUtil.getInt(param, "h", -1);

		if(w != -1 && h != -1) {
			bit.addTranscodingHint(SVGAbstractTranscoder.KEY_WIDTH, Float.valueOf(w));
			bit.addTranscodingHint(SVGAbstractTranscoder.KEY_HEIGHT, Float.valueOf(h));
		}
		bit.transcode(in, new TranscoderOutput());
		return bit.getRendered();
	}

	static private class BufferedImageTranscoder extends ImageTranscoder {
		private BufferedImage m_bi;

		public BufferedImageTranscoder() {
		//			hints.put(ImageTranscoder.KEY_BACKGROUND_COLOR, Color.white);
		}

		/**
		 * Creates a new ARGB image with the specified dimension.
		 * @param w the image width in pixels
		 * @param h the image height in pixels
		 */
		@Override
		public BufferedImage createImage(int w, int h) {
			return new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		}

		// Note this method does not need the Transcoder Output. It allows you to loosly assume that TranscoderOutput is of type BufferedImageTranscoderOutput, purely because it doesn't really care.

		/**
		 * Writes the specified image to the specified output.
		 * @param img the image to write
		 * @param output the output where to store the image
		 */
		@Override
		public void writeImage(BufferedImage img, TranscoderOutput output) {
			m_bi = img;
		}

		public BufferedImage getRendered() {
			return m_bi;
		}
	}


	static public Color makeColor(String col) {
		if(col == null)
			return Color.WHITE;
		//		System.out.println("Using color="+col);
		if(col.startsWith("#"))
			col = col.substring(1);
		Color c = m_colors.get(col.toLowerCase());
		if(c != null)
			return c;
		try {
			int v = Integer.parseInt(col, 16);
			return new Color(v);
		} catch(Exception x) {
			return Color.WHITE;
		}
	}

	/**
	 * Locates the optimal font in a font string.
	 * @param family
	 * @param styles
	 * @return
	 */
	static public Font getFont(String family, String styles, int size) {
		int style = 0;
		if(styles != null) {
			styles = styles.toLowerCase();
			if(styles.indexOf("i") != -1)
				style |= Font.ITALIC;
			if(styles.indexOf("b") != -1)
				style |= Font.BOLD;
		}
		StringTokenizer st = new StringTokenizer(family, ";");
		Font f = null;
		while(st.hasMoreTokens()) {
			String txt = st.nextToken().trim();
			f = Font.decode(txt);
			if(!f.getFamily().equals("Dialog"))
				break;
		}

		if(f == null)
			return new Font("Dialog", style, size);

		return f.deriveFont(style, size);
	}

	private static final Map<String, Color> m_colors = new HashMap<String, Color>();

	static {
		m_colors.put("white", Color.WHITE);
		m_colors.put("black", Color.BLACK);
		m_colors.put("blue", Color.BLUE);
		m_colors.put("cyan", Color.CYAN);
		m_colors.put("darkgray", Color.DARK_GRAY);
		m_colors.put("gray", Color.GRAY);
		m_colors.put("green", Color.GREEN);
		m_colors.put("lightgray", Color.LIGHT_GRAY);
		m_colors.put("magenta", Color.MAGENTA);
		m_colors.put("orange", Color.ORANGE);
		m_colors.put("pink", Color.PINK);
		m_colors.put("red", Color.RED);
		m_colors.put("yellow", Color.YELLOW);
	}

}
