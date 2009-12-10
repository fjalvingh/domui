package to.etc.domui.parts;

import java.io.*;
import java.util.*;

import to.etc.domui.caches.images.*;
import to.etc.domui.server.*;
import to.etc.domui.server.parts.*;
import to.etc.domui.state.*;
import to.etc.domui.trouble.*;
import to.etc.domui.util.images.*;
import to.etc.domui.util.images.converters.*;
import to.etc.util.*;

public class CachedImagePart implements IUnbufferedPartFactory {
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

	public void generate(DomApplication app, String rurl, RequestContextImpl param) throws Exception {
		//-- Split the url into retriever key and instance key.
		String[] ar = rurl.split("/");
		if(ar == null || ar.length != 2)
			throw new ThingyNotFoundException("Image '"+rurl+"'not found (bad rurl).");

		ImageKey ikey = ImageCache.getInstance().createImageKey(ar[0], ar[1]);
		if(ikey == null)
			throw new ThingyNotFoundException("The image source '" + ar[0] + "' is not registered/known");

		//-- Decode all permutations requested,
		List<IImageConversionSpecifier> conversions = new ArrayList<IImageConversionSpecifier>(10);
		decodeMutations(param, conversions);

		//-- Get full image from the cache now;
		FullImage fima = ImageCache.getInstance().getFullImage(ikey, conversions);
		if(fima == null)
			throw new ThingyNotFoundException("The image '" + rurl + "' is not known in it's factory");
		generateImage(param, fima);
	}

	protected void generateImage(RequestContextImpl ri, FullImage fima) throws Exception {
		ri.getResponse().setContentType(fima.getInfo().getMime());
		ri.getResponse().setContentLength(fima.getSource().getSize());
		OutputStream os = ri.getResponse().getOutputStream();
		InputStream is = null;
		try {
			if(fima.getSource() instanceof IImageMemorySource) {
				for(byte[] buf : ((IImageMemorySource) fima.getSource()).getImageBuffers()) {
					os.write(buf);
				}
			} else {
				is = fima.getSource().getImageStream();
				FileTool.copyFile(os, is);
			}
			os.close();
			os = null;
		} finally {
			FileTool.closeAll(is, os);
		}
	}

	protected void decodeMutations(IParameterInfo pin, List<IImageConversionSpecifier> list) throws Exception {
		decodePage(pin, list);
		decodeResize(pin, list);
		decodeFormat(pin, list);
	}

	protected void decodeResize(IParameterInfo pin, List<IImageConversionSpecifier> ik) throws Exception {
		String v = pin.getParameter("resize");
		if(v == null)
			return;
		int p = v.indexOf('x');
		if(p != -1) {
			String ws = v.substring(0, p).trim();
			String hs = v.substring(p + 1).trim();
			try {
				int w = Integer.parseInt(ws);
				int h = Integer.parseInt(hs);
				ik.add(new ImageResize(w, h));
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
	protected void decodePage(IParameterInfo pin, List<IImageConversionSpecifier> ik) throws Exception {
		String v = pin.getParameter("page");
		if(v == null)
			return;
		int pnr = Integer.parseInt(v);
		ik.add(new ImagePageSelect(pnr));
	}

	protected void decodeFormat(IParameterInfo pin, List<IImageConversionSpecifier> ik) throws Exception {
		String v = pin.getParameter("format");
		if(v == null)
			return;
		ik.add(new ImageConvert(v));
	}

	static public String getURL(String providerkey, String instancekey, String... convs) {
		StringBuilder	sb	= new StringBuilder();
		sb.append(CachedImagePart.class.getName());
		sb.append(".part");
		sb.append('/');
		sb.append(providerkey);
		sb.append('/');
		sb.append(instancekey);
		int ix = 0;
		for(String cv : convs) {
			sb.append(ix == 0 ? "?" : "&");
			sb.append(cv);
		}
		return PageContext.getRequestContext().getRelativePath(sb.toString());
	}
}
