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

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.server.DomApplication;
import to.etc.domui.trouble.ThingyNotFoundException;
import to.etc.domui.util.resources.IResourceDependencyList;
import to.etc.domui.util.resources.IResourceRef;
import to.etc.sjit.ImaTool;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

public class PartUtil {
	private PartUtil() {
	}

	static private boolean isa(String name, String ext) {
		int pos = name.lastIndexOf('.');
		if(pos == -1)
			return false;
		return name.substring(pos + 1).equalsIgnoreCase(ext);
	}

	/**
	 * Load an image, either through a resource (when the part starts with RES) or as a webapp file.
	 */
	static public BufferedImage loadImage(DomApplication da, String image, @NonNull IResourceDependencyList rdl) throws Exception {
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
			else
				throw new IllegalArgumentException("The image '" + image + "' must be .gif, .jpg, .jpeg or .png");

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
			} catch(Exception x) {
				//-- Ignore
			}
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

	private static final Map<String, Color> m_colors = new HashMap<>();

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
