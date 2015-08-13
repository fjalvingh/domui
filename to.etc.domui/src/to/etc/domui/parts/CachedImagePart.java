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

import java.io.*;
import java.util.*;

import javax.annotation.*;

import to.etc.domui.caches.images.*;
import to.etc.domui.server.*;
import to.etc.domui.server.parts.*;
import to.etc.domui.state.*;
import to.etc.domui.trouble.*;
import to.etc.domui.util.*;
import to.etc.domui.util.images.*;
import to.etc.domui.util.images.converters.*;
import to.etc.util.*;
import to.etc.webapp.core.*;

public class CachedImagePart implements IUnbufferedPartFactory {

	private static final String IDENTIFICATION_FAILED_FOR_FILE = "Identify failed for file:";

	public static String PARAM_PAGE = "page";

	public static String PARAM_THUMBNAIL = "thumbnail";

	public static String PARAM_FORMAT = "format";

	public static String PARAM_RESIZE = "resize";

	public static String PARAM_FILENAME = "filename";

	public static String PARAM_DISPOSITION = "disposition";

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

	@Override
	public void generate(@Nonnull DomApplication app, @Nonnull String rurl, @Nonnull RequestContextImpl param) throws Exception {
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
		FullImage fima = null;
		 try {
		 	  fima = ImageCache.getInstance().getFullImage(ikey, conversions);
		} catch(IllegalStateException se) {
			String exceptionMessage = se.getMessage();
			if(!StringTool.isBlank(exceptionMessage) && exceptionMessage.contains(IDENTIFICATION_FAILED_FOR_FILE)) {
				String message = "Helaas is het niet mogelijk dit bestand te verwerken. Controleer het bestand.<br/>Technische details: " + exceptionMessage;
				PageParameters pp = new PageParameters(ExpiredDataPage.PARAM_ERRMSG, message);
				ApplicationRequestHandler.generateHttpRedirect(param, DomUtil.createPageURL(ExpiredDataPage.class, pp), "missing resource");
				return;
			} else {
				throw se;
			}
		}

		if(fima == null)
			throw new ThingyNotFoundException("The image '" + rurl + "' is not known in it's factory");
		generateImage(param, fima);
	}

	protected void generateImage(RequestContextImpl ri, FullImage fima) throws Exception {
		//-- Do we need a content-disposition header to force a filename/download?
		String filename = ri.getParameter(PARAM_FILENAME);
		String dis = ri.getParameter(PARAM_DISPOSITION);
		if(dis != null || filename != null) {
			StringBuilder sb = new StringBuilder();
			if(dis == null)
				dis = "inline";
			sb.append(dis);
			if(filename != null) {
				sb.append(";");
				sb.append("filename=");
				sb.append(filename);
			}
			ri.getRequestResponse().addHeader("Content-Disposition", sb.toString());
		}

		OutputStream os = ri.getRequestResponse().getOutputStream(fima.getInfo().getMime(), null, fima.getSource().getSize());
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
		boolean thumb = false;
		String v = pin.getParameter(PARAM_RESIZE);
		if(v == null) {
			v = pin.getParameter(PARAM_THUMBNAIL);
			if(v == null)
				return;
			thumb = true;
		}
		int p = v.indexOf('x');
		if(p != -1) {
			String ws = v.substring(0, p).trim();
			String hs = v.substring(p + 1).trim();
			try {
				int w = Integer.parseInt(ws);
				int h = Integer.parseInt(hs);
				ik.add(thumb ? new ImageThumbnail(w, h) : new ImageResize(w, h));
				return;
			} catch(Exception x) {}
		}
		throw new IllegalStateException("The value of 'resize' or 'thumbnail' must be a size spec in the format 'wxh', like '300x200', not '" + v + "'");
	}

	/**
	 * If we have a parameter "page=xxx" add a page selector
	 * @param ik
	 * @throws Exception
	 */
	protected void decodePage(IParameterInfo pin, List<IImageConversionSpecifier> ik) throws Exception {
		String v = pin.getParameter(PARAM_PAGE);
		if(v == null)
			return;
		int pnr = Integer.parseInt(v);
		ik.add(new ImagePageSelect(pnr));
	}

	protected void decodeFormat(IParameterInfo pin, List<IImageConversionSpecifier> ik) throws Exception {
		String v = pin.getParameter(PARAM_FORMAT);
		if(v == null)
			return;
		String mime = v;

		//-- If format is non-mime try to xlate to mime
		if(mime.indexOf('/') == -1) {
			mime = ServerTools.getExtMimeType(v.toLowerCase());
			if(mime == null)
				throw new IllegalStateException("Cannot find a mime type for format=" + v);
		}

		ik.add(new ImageConvert(mime));
	}

	static public String getURL(String providerkey, String instancekey, String... convs) {
		StringBuilder sb = new StringBuilder();
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
			ix++;
		}
		return UIContext.getRequestContext().getRelativePath(sb.toString());
	}

	static public String getRelativeURL(String providerkey, String instancekey, IPageParameters pp) {
		StringBuilder	sb	= new StringBuilder();
		sb.append(CachedImagePart.class.getName());
		sb.append(".part");
		sb.append('/');
		sb.append(providerkey);
		sb.append('/');
		sb.append(instancekey);
		DomUtil.addUrlParameters(sb, pp, true);
		return DomUtil.getRelativeApplicationResourceURL(sb.toString());
	}

}
