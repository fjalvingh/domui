package to.etc.domui.util.resources;

import java.io.*;
import java.util.*;

import javax.annotation.*;

import to.etc.domui.component.image.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.server.*;
import to.etc.sjit.*;

/**
 * This caches (theme) resource data like the dimensions of images used in buttons where needed.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 11/26/15.
 */
@DefaultNonNull
final public class ResourceInfoCache {
	final private DomApplication m_application;

	static final private class ImageDimension {
		@Nullable
		Dimension m_dimension;
	}

	private final Map<String, ImageDimension> m_imageDimensionMap = new HashMap<>();

	public ResourceInfoCache(DomApplication application) {
		m_application = application;
	}

	/**
	 * Find the image dimensions for the specified resource specified in Java terms. This means that
	 * THEME/xxxx refers to a looked up icon through the current theme and style of the specified node's page.
	 * @param node
	 * @param themeResource
	 * @return
	 * @throws Exception
	 */
	@Nullable
	public Dimension getImageDimension(NodeBase node, String themeResource) throws Exception {
		String path = node.getThemedResourceRURL(themeResource);
		return getImageDimension(path);
	}

	/**
	 * Find the image dimensions for the specified resource specified as a RURL. This means that for normal
	 * resources the name starts with $THEME/xxxx/yyy/xxxx, referring to the proper theme instance.
	 * <b>Warning</b> do NOT use for resources specified like "THEME/xxxxx", use {@link #getImageDimension(NodeBase, String)} for that.
	 * @param resourceRURL
	 * @return
	 */
	@Nullable
	public Dimension getImageDimension(String resourceRURL) throws Exception {
		ImageDimension id;
		synchronized(m_imageDimensionMap) {                            // Step 1: fast release
			id = m_imageDimensionMap.get(resourceRURL);
			if(null == id) {
				id = new ImageDimension();
				m_imageDimensionMap.put(resourceRURL, id);
			}
		}

		Dimension d;
		synchronized(id) {                                            // Step 2: slow release if calculating size
			d = id.m_dimension;
			if(null == d) {
				d = id.m_dimension = calculateImageDimensions(resourceRURL);
			}
		}
		return d;
	}

	@Nullable
	private Dimension calculateImageDimensions(String resourceRURL) throws Exception {
		System.out.println("Calculating dimensions of " + resourceRURL);
		IResourceRef ref = DomApplication.get().getResource(resourceRURL, ResourceDependencyList.NULL);
		try(InputStream is = ref.getInputStream()) {
			if(null == is)
				return null;
			java.awt.Dimension d = ImaTool.getImageDimension(is);
			if(null == d)
				return null;
			return new Dimension(d);
		}
	}
}
