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

import to.etc.domui.component.input.Text;
import to.etc.domui.server.DomApplication;
import to.etc.domui.server.IExtendedParameterInfo;
import to.etc.domui.server.parts.IBufferedPartFactory;
import to.etc.domui.server.parts.PartResponse;
import to.etc.domui.state.UIContext;
import to.etc.domui.util.DomUtil;
import to.etc.domui.util.resources.IResourceDependencyList;
import to.etc.util.FileTool;

import javax.annotation.Nonnull;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Generates background image for specified input field caption.
 * Usually used by {@link Text#setMarkerImage(String)}
 *
 *
 * @author <a href="mailto:btadic@execom.eu">Bojan Tadic</a>
 * Created on Nov 1, 2011
 */
public class MarkerImagePart implements IBufferedPartFactory<MarkerImagePartKey> {
	private static final Color DEFAULT_COLOR = Color.GRAY;

	@Override
	public @Nonnull MarkerImagePartKey decodeKey(DomApplication application, @Nonnull IExtendedParameterInfo param) throws Exception {
		MarkerImagePartKey key = MarkerImagePartKey.decode(application, param);
		return key;
	}

	/**
	 * Generate image if is not in cache.
	 * @see to.etc.domui.server.parts.IBufferedPartFactory#generate(to.etc.domui.server.parts.PartResponse, to.etc.domui.server.DomApplication, java.lang.Object, to.etc.domui.util.resources.IResourceDependencyList)
	 */
	@Override
	public void generate(@Nonnull PartResponse pr, @Nonnull DomApplication da, @Nonnull MarkerImagePartKey sipKey, @Nonnull IResourceDependencyList rdl) throws Exception {
		InputStream is = null;

		try {
			BufferedImage bi = PartUtil.loadImage(da, sipKey.getIcon(), rdl);
			is = getInputStream(drawImage(bi, sipKey));

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

	private static String getURL(String icon, String caption, String color) {
		if(null != icon && icon.startsWith("THEME/")) {
			System.err.println("BAD ICON SPEC: " + icon);
			throw new IllegalStateException("BAD ICON SPEC: " + icon);
		}

		StringBuilder sb = new StringBuilder();
		sb.append(MarkerImagePart.class.getName()).append(".part");
		boolean paramExists = false;
		paramExists = MarkerImagePartKey.appendParam(sb, paramExists, MarkerImagePartKey.PARAM_ICON, icon);
		paramExists = MarkerImagePartKey.appendParam(sb, paramExists, MarkerImagePartKey.PARAM_CAPTION, caption);
		paramExists = MarkerImagePartKey.appendParam(sb, paramExists, MarkerImagePartKey.PARAM_COLOR, color);
		return sb.toString();
	}

	private static String getURL(String icon, String caption, String color, String font, int size, String spec) {
		if(null != icon && icon.startsWith("THEME/")) {
			System.err.println("BAD ICON SPEC: " + icon);
			throw new IllegalStateException("BAD ICON SPEC: " + icon);
		}
		StringBuilder sb = new StringBuilder();
		sb.append(MarkerImagePart.class.getName()).append(".part");
		boolean paramExists = false;
		paramExists = MarkerImagePartKey.appendParam(sb, paramExists, MarkerImagePartKey.PARAM_ICON, icon);
		paramExists = MarkerImagePartKey.appendParam(sb, paramExists, MarkerImagePartKey.PARAM_CAPTION, caption);
		paramExists = MarkerImagePartKey.appendParam(sb, paramExists, MarkerImagePartKey.PARAM_COLOR, color);

		paramExists = MarkerImagePartKey.appendParam(sb, paramExists, MarkerImagePartKey.PARAM_FONT, font);
		paramExists = MarkerImagePartKey.appendParam(sb, paramExists, MarkerImagePartKey.PARAM_FONTSIZE, Integer.toString(size));
		paramExists = MarkerImagePartKey.appendParam(sb, paramExists, MarkerImagePartKey.PARAM_SPEC, spec);
		return sb.toString();
	}

	/**
	 * Dynamically add background image for emptyMarker.
	 * Background image have small magnifier icon (THEME/icon-search.png)
	 * @return
	 */
	public static String getBackgroundIconOnly() {
		return getBackgroundImage(null, null, null);
	}

	/**
	 * Dynamically add background image for emptyMarker.
	 * Background image will have only defined icon
	 *
	 * @param icon
	 * @return
	 */
	public static String getBackgroundIconOnly(String icon) {
		return getBackgroundImage(icon, null, null);
	}

	/**
	 * Dynamically add background image for emptyMarker.
	 * Background image have small magnifier icon and and defined text (caption)
	 *
	 * @param caption
	 * @return
	 */
	public static String getBackgroundImage(String caption) {
		return getBackgroundImage(null, caption, null);
	}

	/**
	 * Dynamically add background image for emptyMarker.
	 * Background image have small defined icon and and defined text (caption)
	 *
	 * @param icon
	 * @param caption
	 * @return
	 */
	public static String getBackgroundImage(String icon, String caption) {
		return getBackgroundImage(icon, caption, null);
	}

	/**
	 * Dynamically add background image for emptyMarker.
	 * Background image have small defined icon and and defined text (caption) in defined color
	 * @param icon
	 * @param caption
	 * @param color
	 * @return
	 */
	public static String getBackgroundImage(String icon, String caption, String color) {
		String url = UIContext.getRequestContext().getRelativePath(getURL(icon, caption, color));
		return url;
	}

	public static String getBackgroundImage(String icon, String caption, String color, String font, int size, String spec) {
		String url = UIContext.getRequestContext().getRelativePath(getURL(icon, caption, color, font, size, spec));
		return url;
	}

	/**
	 * Draw background image with icon and caption
	 * @param icon
	 * @return
	 */
	private BufferedImage drawImage(@Nonnull BufferedImage icon, MarkerImagePartKey key) {
		BufferedImage bufferedImage = new BufferedImage(200, 20, Transparency.TRANSLUCENT);

		Graphics2D g = bufferedImage.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g.setComposite(makeComposite(0.3F));

		g.drawImage(icon, null, 0, 0);

		if(!DomUtil.isBlank(key.getCaption())) {
			String caption = key.getCaption();

			//-- Select a font if needed
			String fontname = key.getFont();
			if(DomUtil.isBlank(fontname))
				fontname = "Helvetica";
			int size = key.getFontSize();
			if(size <= 0)
				size = 10;
			int style = 0;
			switch(key.getFontSpec()){
				default:
					throw new IllegalStateException(key.getFontSpec() + ": unsupported??");
				case BOLD:
					style = Font.BOLD;
					break;
				case ITALICS:
					style = Font.ITALIC;
					break;
				case BOLD_ITALICS:
					style = Font.ITALIC | Font.BOLD;
					break;
				case NORM:
					style = 0;
					break;
			}

			Font font = new Font(fontname, style, size);
			Color capColor = null;
			if(!DomUtil.isBlank(key.getColor())) {
				try {
					String captionColor = key.getColor();
					if(captionColor.startsWith("#")) {
						captionColor = captionColor.substring(1);
					}
					capColor = new Color(Integer.parseInt(captionColor, 16));
				} catch(Exception ex) {
					//just ignore
				}
			}
			if(capColor == null) {
				capColor = DEFAULT_COLOR;
			}
			drawText(g, font, caption, 21, 1, Color.WHITE);
			drawText(g, font, caption, 20, 0, capColor);
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
	 * Draw String on canvas.
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
