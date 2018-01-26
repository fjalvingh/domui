package to.etc.domui.parts;

import java.awt.*;
import java.awt.image.*;

import javax.annotation.*;
import javax.imageio.*;

import to.etc.domui.parts.GrayscalerPart.Key;
import to.etc.domui.server.*;
import to.etc.domui.server.parts.*;
import to.etc.domui.util.resources.*;
import to.etc.util.*;

/**
 * This part creates a grayscaled version of an image passed. The image can come from a theme. It's main usage is
 * to create gray versions of icons for disabled action entities. The implementation uses per-pixel conversion and
 * thus is slow; the reason is that apparently none of the well-known color-to-gray conversion methods allow for
 * transparency to be retained. This means that this code should not be used for large bitmaps.
 * <p>Since the image created by this call gets buffered using it for icons is no problem.</p>
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 25, 2012
 */
public class GrayscalerPart implements IBufferedPartFactory<Key> {

	static public final class Key {

		@Nonnull
		final private String m_icon;

		final private boolean m_sprite;

		public Key(@Nonnull String icon, boolean isSprite) {
			m_icon = icon;
			m_sprite = isSprite;
		}

		@Nonnull
		public String getIcon() {
			return m_icon;
		}

		public boolean isSprite() {
			return m_sprite;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((m_icon == null) ? 0 : m_icon.hashCode());
			result = prime * result + (m_sprite ? 1231 : 1237);
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if(this == obj)
				return true;
			if(obj == null)
				return false;
			if(getClass() != obj.getClass())
				return false;
			final Key other = (Key) obj;
			if(m_sprite != other.m_sprite)
				return false;
			if(m_icon == null) {
				return other.m_icon == null;
			} else
				return m_icon.equals(other.m_icon);
		}


	}

	@Override
	@Nonnull
	public Key decodeKey(DomApplication application, @Nonnull IExtendedParameterInfo param) throws Exception {
		String icon = param.getParameter("icon");
		if(null == icon)
			throw new IllegalStateException("Missing icon parameter");

		boolean isSprite = "true".equalsIgnoreCase(param.getParameter("sprite"));

		return new Key(icon, isSprite);
	}

	@Override
	public void generate(@Nonnull PartResponse pr, @Nonnull DomApplication da, @Nonnull Key k, @Nonnull IResourceDependencyList rdl) throws Exception {
		BufferedImage bi = PartUtil.loadImage(da, k.getIcon(), rdl);

		if(k.isSprite())
			bi = prepareSpriteImage(bi);
		else
			bi = prepareImage(bi);

		ImageIO.write(bi, "png", pr.getOutputStream());
		pr.setMime("image/png");
		pr.setCacheTime(da.getDefaultExpiryTime());
	}

	@Nonnull
	private BufferedImage prepareImage(@Nonnull BufferedImage image) {
		convertToGrayscale(image);
		return image;
	}

	@Nonnull
	private BufferedImage prepareSpriteImage(BufferedImage image) {
		BufferedImage sprite = new BufferedImage(image.getWidth(), 2 * image.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = sprite.createGraphics();

		g2.drawImage(image, null, 0, 0);
		convertToGrayscale(image);
		g2.drawImage(image, null, 0, image.getHeight());

		g2.dispose();
		return sprite;
	}

	/**
	 * We will brute-force the conversion, since none of the known methods for grayscaling retain transparency 8-(
	 * @param image
	 */
	private void convertToGrayscale(BufferedImage image) {
		for(int y = image.getHeight(); --y >= 0;) {
			for(int x = image.getWidth(); --x >= 0;) {
				int argb = image.getRGB(x, y);

				//-- Calculate pixel luminance retaining transparency.
				int a = argb & 0xff000000;
				int r = (argb >> 16) & 0xff;
				int g = (argb >> 8) & 0xff;
				int b = argb & 0xff;
				int lum = (int) (r * 0.299 + g * 0.587 + b * 0.114);
				argb = a + (lum << 16) + (lum << 8) + (lum);
				image.setRGB(x, y, argb);
			}
		}
	}

	/**
	 * Return the URL for a grayscaled image icon. The icon should be an application-relative path.
	 * @param icon
	 * @return
	 */
	@Nonnull
	public static String getURL(@Nonnull String icon) {
		StringBuilder sb = new StringBuilder();
		sb.append(GrayscalerPart.class.getName()).append(".part?icon=");
		StringTool.encodeURLEncoded(sb, icon);
		return sb.toString();
	}

	/**
	 * Return the URL for a sprite containing original and grayscaled image icon.
	 * The icon should be an application-relative path.
	 * @param icon
	 * @return
	 */
	@Nonnull
	public static String getSpriteURL(@Nonnull String icon) {
		StringBuilder sb = new StringBuilder();
		sb.append(GrayscalerPart.class.getName()).append(".part?sprite=true&icon=");
		StringTool.encodeURLEncoded(sb, icon);
		return sb.toString();
	}
}
