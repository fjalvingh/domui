package to.etc.domui.parts;

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;

import to.etc.domui.server.*;
import to.etc.domui.trouble.*;
import to.etc.domui.util.*;
import to.etc.domui.util.resources.*;
import to.etc.sjit.*;

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
	static public Properties loadProperties(DomApplication da, String src, ResourceDependencyList rdl) throws Exception {
		IResourceRef ref = da.getApplicationResourceByName(src);
		if(ref == null)
			return null;
		if(rdl != null)
			rdl.add(ref);
		InputStream is = ref.getInputStream();
		if(is == null)
			return null;
		try {
			Properties p = new Properties();
			p.load(is);
			if(ref instanceof WebappResourceRef)
				p.put("webui.webapp", "true");
			return p;
		} finally {
			try {
				is.close();
			} catch(Exception x) {}
		}
	}

	/**
	 * Load an image, either thru a resource (when the part starts with RES) or as a webapp file.
	 * @param r
	 * @param image
	 * @param depset
	 * @return
	 * @throws Exception
	 */
	static public BufferedImage loadImage(DomApplication da, String image, ResourceDependencyList rdl) throws Exception {
		IResourceRef ref = da.getApplicationResourceByName(image);
		if(ref == null)
			throw new ThingyNotFoundException("The image '" + image + "' was not found.");
		InputStream is = ref.getInputStream();
		if(is == null)
			throw new ThingyNotFoundException("The image '" + image + "' was not found.");
		if(rdl != null)
			rdl.add(ref);
		try {
			BufferedImage bi = null;

			if(isa(image, "gif"))
				bi = ImaTool.loadGIF(is);
			else if(isa(image, "jpg") || isa(image, "jpeg"))
				bi = ImaTool.loadJPEG(is);
			else if(isa(image, "png"))
				bi = ImaTool.loadPNG(is);
			else
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
				if(is != null)
					is.close();
			} catch(Exception x) {}
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
