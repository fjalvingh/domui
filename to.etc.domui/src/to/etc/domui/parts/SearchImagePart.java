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
import java.awt.image.*;
import java.io.*;

import javax.annotation.*;
import javax.imageio.*;

import to.etc.domui.component.input.*;
import to.etc.domui.server.*;
import to.etc.domui.server.parts.*;
import to.etc.domui.state.*;
import to.etc.domui.util.*;
import to.etc.domui.util.resources.*;
import to.etc.sjit.*;
import to.etc.util.*;

/**
 * Generates background image for specified search field caption. 
 * Usualy used by {@link Text#setSearchMarker(String)}
 * 
 *
 * @author <a href="mailto:btadic@execom.eu">Bojan Tadic</a>
 * Created on Nov 1, 2011
 */
public class SearchImagePart implements IBufferedPartFactory {
	static private final String PREFIX = "$searchMarker$";

	@Override
	public Object decodeKey(String rurl, IExtendedParameterInfo param) throws Exception {
		return PREFIX + rurl;
	}

	/**
	 * Generate image if is not in cache.
	 * @see to.etc.domui.server.parts.IBufferedPartFactory#generate(to.etc.domui.server.parts.PartResponse, to.etc.domui.server.DomApplication, java.lang.Object, to.etc.domui.util.resources.IResourceDependencyList)
	 */
	@Override
	public void generate(PartResponse pr, DomApplication da, Object key, IResourceDependencyList rdl) throws Exception {
		String ext = ((String) key).substring(PREFIX.length()).replace("_", " ");

		InputStream is = null;
		try {

			is = getInputStream(drawImage(ext));

			if(is == null)
				throw new IllegalStateException("Image is generated incorrectly");
			FileTool.copyFile(pr.getOutputStream(), is);
			pr.setMime("image/png");
			pr.setCacheTime(da.getDefaultExpiryTime());
		} finally {
			try {
				if(is != null)
					is.close();
			} catch(Exception x) {}
		}

	}

	private InputStream getInputStream(BufferedImage bi) throws IOException {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		ImageIO.write(bi, "png", os);
		InputStream stream = new ByteArrayInputStream(os.toByteArray());
		return stream;
	}

	private static String getURL(String ext) {
		String formated = ext.replace(" ", "_");
		return SearchImagePart.class.getName() + ".part/" + formated;
	}

	/**
	 * Dinamicly add backgroudn image for search marker.
	 * Background image have small magnifier icon and difined text (labelText)
	 *  
	 * @param labelText text printed on the image
	 * @return
	 */
	public static String getBackgroundImage(String labelText) {
		if(DomUtil.isBlank(labelText)) {
			labelText = "";
		}
		String url = UIContext.getRequestContext().getRelativePath(SearchImagePart.getURL(labelText));
		return url;
	}

	/**
	 * Draw small magnifier icon and difined text
	 * @param labelText
	 * @return
	 */
	private BufferedImage drawImage(@Nonnull String labelText) {
		BufferedImage bufferedImage = new BufferedImage(200, 20, BufferedImage.TRANSLUCENT);

		Graphics2D g = bufferedImage.createGraphics();
		g.setComposite(makeComposite(0.3F));

		InputStream inputStream = SearchImagePart.class.getResourceAsStream("icon-search.png");
		BufferedImage searchIcon = null;
		try {
			searchIcon = ImaTool.loadPNG(inputStream);
		} catch(IOException e) {
			e.printStackTrace();
		}
		g.drawImage(searchIcon, null, 0, 0);

		if(!DomUtil.isBlank(labelText.trim())) {
			Font font = new Font("VERDANA", Font.BOLD, 10);
			drawText(g, font, labelText, 21, 1, new Color(Integer.parseInt("E7E7E7", 16)));
			drawText(g, font, labelText, 20, 0, new Color(Integer.parseInt("5C5C5C", 16)));
		}

		return bufferedImage;
	}

	/**
	 * Add opacity to image
	 * @param alpha
	 * @return
	 */
	private AlphaComposite makeComposite(float alpha) {
		int type = AlphaComposite.SRC_OVER;
		return (AlphaComposite.getInstance(type, alpha));
	}

	/**
	 * Draw String on convas.
	 *
	 * @param g
	 * @param textValue
	 * @param x  X coordinate.
	 * @param y  Y coordinate. Top of text
	 * @param stringColor
	 */
	private void drawText(Graphics2D g, Font font, String textValue, int x, int y, Color stringColor) {
		Font oldFont = g.getFont();
		Color old = g.getColor();
		g.setFont(font);
		g.setColor(stringColor);
		FontMetrics fm = g.getFontMetrics();
		int startX = x;
		int startY = y + fm.getHeight() - 1;
		g.drawString(textValue, startX, startY);
		g.setColor(old);
		g.setFont(oldFont);
	}


}
