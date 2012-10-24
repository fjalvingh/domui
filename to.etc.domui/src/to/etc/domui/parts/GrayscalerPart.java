package to.etc.domui.parts;

import java.awt.*;
import java.awt.color.*;
import java.awt.image.*;

import javax.annotation.*;
import javax.imageio.*;

import to.etc.domui.server.*;
import to.etc.domui.server.parts.*;
import to.etc.domui.util.resources.*;
import to.etc.sjit.*;

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

		ColorSpace gsColorSpace = ColorSpace.getInstance(ColorSpace.CS_GRAY);

		ComponentColorModel ccm = new ComponentColorModel(gsColorSpace, true, false, Transparency.TRANSLUCENT, DataBuffer.TYPE_BYTE);

		WritableRaster raster = ccm.createCompatibleWritableRaster(bi.getWidth(), bi.getHeight());

		Image grayImage = new BufferedImage(ccm, raster, ccm.isAlphaPremultiplied(), null);

		//		GrayFilter filter = new GrayFilter(true, 20);
		//		ImageProducer prod = new FilteredImageSource(bi.getSource(), filter);
		//		Image grayImage = Toolkit.getDefaultToolkit().createImage(prod);

		BufferedImage dimg = ImaTool.makeBuffered(grayImage);
		ImageIO.write(dimg, "png", pr.getOutputStream());
		pr.setMime("image/png");
		pr.setCacheTime(da.getDefaultExpiryTime());
	}
}
