package to.etc.domui.parts;

import java.awt.image.*;

import javax.annotation.*;
import javax.imageio.*;

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
public class GrayscalerPart implements IBufferedPartFactory {
	static public class Key {
		@Nonnull
		final private String m_icon;

		public Key(@Nonnull String icon) {
			m_icon = icon;
		}

		@Nonnull
		public String getIcon() {
			return m_icon;
		}
	};

	@Override
	@Nonnull
	public Object decodeKey(@Nonnull String rurl, @Nonnull IExtendedParameterInfo param) throws Exception {
		String icon = param.getParameter("icon");
		if(null == icon)
			throw new IllegalStateException("Missing icon parameter");
		return new Key(icon);
	}

	@Override
	public void generate(@Nonnull PartResponse pr, @Nonnull DomApplication da, @Nonnull Object key, @Nonnull IResourceDependencyList rdl) throws Exception {
		Key k = (Key) key;
		BufferedImage bi = PartUtil.loadImage(da, da.getThemedResourceRURL(k.getIcon()), rdl);

		//-- We will brute-force the conversion, since none of the known methods for grayscaling retain transparency 8-(
		for(int y = bi.getHeight(); --y >= 0;) {
			for(int x = bi.getWidth(); --x >= 0;) {
				int argb = bi.getRGB(x, y);

				//-- Calculate pixel luminance retaining transparency.
				int a = argb & 0xff000000;
				int r = (argb >> 16) & 0xff;
				int g = (argb >> 8) & 0xff;
				int b = argb & 0xff;
				int lum = (int) (r * 0.299 + g * 0.587 + b * 0.114);
				argb = a + (lum << 16) + (lum << 8) + (lum);
				bi.setRGB(x, y, argb);
			}
		}

		//		GrayFilter filter = new GrayFilter(true, 20);
		//		ImageProducer prod = new FilteredImageSource(bi.getSource(), filter);
		//		Image grayImage = Toolkit.getDefaultToolkit().createImage(prod);
		//		BufferedImage dimg = ImaTool.makeBuffered(grayImage);
		ImageIO.write(bi, "png", pr.getOutputStream());
		pr.setMime("image/png");
		pr.setCacheTime(da.getDefaultExpiryTime());
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
}
