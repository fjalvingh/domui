package to.etc.domui.parts;

import java.io.*;
import java.util.*;

import to.etc.domui.caches.images.*;
import to.etc.domui.server.*;
import to.etc.domui.server.parts.*;
import to.etc.domui.trouble.*;
import to.etc.domui.util.images.*;
import to.etc.domui.util.images.converters.*;

abstract public class ImagePartBase implements IUnbufferedPartFactory {
	static public class ImageKeys {
		private DomApplication m_application;

		private IRequestContext m_requestContext;

		private String m_rurl;

		private IImageRetriever m_retriever;

		private Object m_key;

		private List<IImageConversionSpecifier> m_conversions = new ArrayList<IImageConversionSpecifier>(10);

		public ImageKeys(DomApplication application, IRequestContext requestContext, String rurl) {
			m_application = application;
			m_requestContext = requestContext;
			m_rurl = rurl;
		}

		public IImageRetriever getRetriever() {
			return m_retriever;
		}

		public void setRetriever(IImageRetriever retriever) {
			m_retriever = retriever;
		}

		public Object getKey() {
			return m_key;
		}

		public void setKey(Object key) {
			m_key = key;
		}

		public List<IImageConversionSpecifier> getConversions() {
			return m_conversions;
		}

		public void setConversions(List<IImageConversionSpecifier> conversions) {
			m_conversions = conversions;
		}

		public DomApplication getApplication() {
			return m_application;
		}

		public IRequestContext getRequestContext() {
			return m_requestContext;
		}

		public String getRURL() {
			return m_rurl;
		}
	}

	abstract protected void decodeSource(ImageKeys ik) throws Exception;

	public void generate(DomApplication app, String rurl, RequestContextImpl param) throws Exception {
		ImageKeys ik = new ImageKeys(app, param, rurl);
		decodeSource(ik);
		decodeMutations(ik);
		CachedImageData ii = getImage(ik);
		if(ii == null)
			throw new ThingyNotFoundException("Image not found.");
		generateImage(param, ii);
	}

	protected CachedImageData getImage(ImageKeys ik) throws Exception {
		if(ik.getRetriever() == null)
			throw new IllegalStateException("No image source (retriever) known");
		if(ik.getKey() == null)
			throw new IllegalStateException("No image source (key) known");
		CachedImageData ii = ImageCache.getInstance().getImage(ik.getRetriever(), ik.getKey(), ik.getConversions());
		return ii;
	}

	protected void generateImage(RequestContextImpl ri, CachedImageData ii) throws Exception {
		ri.getResponse().setContentType(ii.getImageInfo().getMime());
		ri.getResponse().setContentLength(ii.getSize());
		OutputStream os = ri.getResponse().getOutputStream();
		try {
			for(int i = 0; i < ii.getBuffers().length; i++) {
				os.write(ii.getBuffers()[i]);
			}
		} finally {
			try {
				os.close();
			} catch(Exception x) {}
		}
	}

	protected void decodeMutations(ImageKeys ik) throws Exception {
		decodePage(ik);
		decodeResize(ik);
		decodeFormat(ik);
	}

	protected void decodeResize(ImageKeys ik) throws Exception {
		String v = ik.getRequestContext().getParameter("resize");
		if(v == null)
			return;
		int p = v.indexOf('x');
		if(p != -1) {
			String ws = v.substring(0, p).trim();
			String hs = v.substring(p + 1).trim();
			try {
				int w = Integer.parseInt(ws);
				int h = Integer.parseInt(hs);
				ik.getConversions().add(new ImageResize(w, h));
				return;
			} catch(Exception x) {}
		}
		throw new IllegalStateException("The value of 'resize' must be a size spec in the format 'wxh', like '300x200', not '" + v + "'");
	}

	/**
	 * If we have a parameter "page=xxx" add a page selector
	 * @param ik
	 * @throws Exception
	 */
	protected void decodePage(ImageKeys ik) throws Exception {
		String v = ik.getRequestContext().getParameter("page");
		if(v == null)
			return;
		int pnr = Integer.parseInt(v);
		ik.getConversions().add(new ImagePageSelect(pnr));
	}

	protected void decodeFormat(ImageKeys ik) throws Exception {
		String v = ik.getRequestContext().getParameter("format");
		if(v == null)
			return;
		ik.getConversions().add(new ImageConvert(v));
	}
}
