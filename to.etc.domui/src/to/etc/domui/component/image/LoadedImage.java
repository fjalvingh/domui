package to.etc.domui.component.image;

import to.etc.domui.trouble.*;
import to.etc.domui.util.images.converters.*;
import to.etc.domui.util.images.machines.*;
import to.etc.sjit.*;
import to.etc.webapp.nls.*;

import javax.annotation.*;
import java.io.*;
import java.util.*;

/**
 * An image that was updated/loaded by the user and has not yet been stored in wherever.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Nov 5, 2014
 */
final public class LoadedImage implements IUIImage {
	static private final BundleRef BUNDLE = BundleRef.create(ImageSelectControl.class, "messages");

	@Nonnull
	private final File m_source;

	@Nonnull
	private final Dimension m_dimension;

	@Nonnull
	private final String m_mime;

	@Nonnull
	private final Map<Dimension, LoadedImageInstance> m_sizeMap = new HashMap<>();

	@Nullable
	private final List<Object> m_resourceList;

	@Nonnull
	static public LoadedImage	create(@Nonnull File original, @Nullable Dimension maxSize, @Nullable List<Object> resourceList) throws Exception {
		ImageInfo identify = ImageMagicImageHandler.getInstance().identify(original);
		if(identify.getPageCount() != 1)
			throw new UIException(BUNDLE, "image.invalid");

		OriginalImagePage page = identify.getPageList().get(0);
		Dimension dimension = new Dimension(page.getWidth(), page.getHeight());		// Original's size.
		if(maxSize != null && !maxSize.contains(dimension)) {						// Original too large -> resize it
			//-- Resize
			ImageSpec spec = new ImageSpec(original, identify);
			ImageConverterHelper h = new ImageConverterHelper();
			ImageSpec resized = ImageMagicImageHandler.getInstance().thumbnail(h, spec, 0, maxSize.getWidth(), maxSize.getHeight(), "image/png");
			original = resized.getSource();											// Original is now the initial resized one.
			if(null != resourceList)
				resourceList.add(original);

			LoadedImageInstance oli = new LoadedImageInstance(original, identify.getMime(), dimension);
			return new LoadedImage(original, resized.getMime(), new Dimension(resized.getInfo().getPage(0).getWidth(), resized.getInfo().getPage(0).getHeight()), resourceList);
		}

		return new LoadedImage(original, page.getMimeType(), new Dimension(page.getWidth(), page.getHeight()), resourceList);
	}

	private LoadedImage(@Nonnull File source, @Nonnull String mime, @Nonnull Dimension size, @Nullable List<Object> resourceList) {
		m_source = source;
		m_mime = mime;
		m_dimension = size;
		m_resourceList = resourceList;
	}

	@Nonnull @Override public IUIImageInstance getImage(@Nullable Dimension size, boolean thumbNail) throws Exception {
		//-- Get size after resize,
		if(size == null) {
			size = thumbNail ? Dimension.ICON : m_dimension;
		}

		java.awt.Dimension od = ImaTool.resizeWithAspect(size.getWidth(), size.getHeight(), m_dimension.getWidth(), m_dimension.getHeight());
		size = new Dimension(od);
		LoadedImageInstance ii = m_sizeMap.get(size);
		if(null == ii) {
			if(m_dimension.equals(size) || size.contains(m_dimension)) {
				//-- Source is smaller or same size as requested -> return source
				ii = new LoadedImageInstance(m_source, m_mime, m_dimension);
			} else {
				//-- Resize.
				ImageSpec spec = new ImageSpec(m_source, m_mime, m_dimension.getWidth(), m_dimension.getHeight());
				ImageConverterHelper h = new ImageConverterHelper();
				ImageSpec resized;
				if(thumbNail) {
					resized = ImageMagicImageHandler.getInstance().thumbnail(h, spec, 0, size.getWidth(), size.getHeight(), "image/png");
				} else {
					resized = ImageMagicImageHandler.getInstance().scale(h, spec, 0, size.getWidth(), size.getHeight(), "image/png");
				}
				List<Object> resourceList = m_resourceList;
				if(null != resourceList)
					resourceList.add(resized.getSource());
				ii = new LoadedImageInstance(resized.getSource(), resized.getMime(), new Dimension(resized.getInfo().getPage(0).getWidth(), resized.getInfo().getPage(0).getHeight()));
			}
			m_sizeMap.put(size, ii);
		}
		return ii;
	}

	@Nonnull
	public File getSource() {
		return m_source;
	}

	@Nonnull public String getMime() {
		return m_mime;
	}

	@Nonnull public Dimension getDimension() {
		return m_dimension;
	}
}
