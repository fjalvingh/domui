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

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.server.DomApplication;
import to.etc.domui.server.IRequestContext;
import to.etc.domui.server.RequestContextImpl;
import to.etc.domui.server.parts.IUnbufferedPartFactory;
import to.etc.domui.state.UIContext;
import to.etc.domui.trouble.ThingyNotFoundException;
import to.etc.util.FileTool;
import to.etc.util.StringTool;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Blob;

/**
 * Safe reference to a server-side tempfile.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 20, 2010
 */
public class TempFilePart implements IUnbufferedPartFactory {
	static private class FileInfo {
		private String m_pw;

		private File m_source;

		private String m_mime;

		private String m_disposition;

		public FileInfo(String pw, File source, String mime, String disp) {
			m_pw = pw;
			m_source = source;
			m_mime = mime;
			m_disposition = disp;
		}

		public String getDisposition() {
			return m_disposition;
		}

		public String getPw() {
			return m_pw;
		}

		public File getSource() {
			return m_source;
		}

		public String getMime() {
			return m_mime;
		}
	}

	public enum Disposition {
		/**
		 * Download the attachment, do not show it.
		 */
		Attachment,

		/**
		 * Show the file by trying to view it in the browser
		 */
		Inline,

		/**
		 * Do content-disposition: let the browser decide what to do with the thing
		 */
		None
	}

	/**
	 * Use the version {@link #registerTempFile(java.io.File, String, to.etc.domui.parts.TempFilePart.Disposition, String)}
	 */
	@Deprecated
	static public String registerTempFile(@NonNull IRequestContext ctx, @NonNull File target, @NonNull String mime, @Nullable String type, @Nullable String name) {
		String key = StringTool.generateGUID();
		String pw = StringTool.generateGUID();
		String disp = null;
		if(type != null)
			disp = type + "; filename=" + name;
		FileInfo fi = new FileInfo(pw, target, mime, disp);
		ctx.getSession().setAttribute("tempf-" + key, fi); // Store in session context

		StringBuilder sb = new StringBuilder();
		sb.append(TempFilePart.class.getName());
		sb.append(".part?key=").append(key).append("&passkey=").append(pw);
		return sb.toString();
	}

	/**
	 * Register a temp file that can be accessed by the browser. The file can reside anywhere but should usually be put
	 * in temp files. Only people knowing the full name for the file, including a set of random-generated "passwords", can
	 * access it. This returns the absolute URL to the file, including host name etc.
	 *
	 * @param disp The disposition: attachment or inline (download or view)
	 * @return The absolute full URL to the file.
	 */
	static public String registerTempFile(@NonNull File target, @NonNull String mime, @NonNull Disposition disp, @Nullable String name) {
		String key = StringTool.generateGUID();
		String pw = StringTool.generateGUID();
		String s = null;
		switch(disp) {
			default:
				break;

			case Attachment:
				s = "attachment";
				break;

			case Inline:
				s = "inline";
				break;
		}
		if(s != null && name != null) {
			name = name.replace(" ", "_"); // spaces are not allowed in names for filename!
			s += "; filename=" + name;
		}
		FileInfo fi = new FileInfo(pw, target, mime, s);
		IRequestContext rc = UIContext.getRequestContext();
		rc.getSession().setAttribute("tempf-" + key, fi); // Store in session context

		StringBuilder sb = new StringBuilder();
		sb.append(TempFilePart.class.getName());
		sb.append(".part?key=").append(key).append("&passkey=").append(pw);
		return rc.getRelativePath(sb.toString());
	}

	/**
	 * Saves blob into temporary file, register temporary file in provided context, and returns generated download link.
	 */
	public static String getDownloadLink(@NonNull IRequestContext ctx, @NonNull Blob blob, String mime, String type, String filename) throws Exception {
		File temp = File.createTempFile("tmp", ".tmp");
		FileTool.saveBlob(temp, blob);
		return TempFilePart.registerTempFile(ctx, temp, mime, type, filename);
	}

	/**
	 * Force the browser to download the specified file, by sending "location.href = (url-to-file)" to the browser.
	 */
	public static void createDownloadAction(@NonNull NodeBase sourcePage, @NonNull File target, @NonNull String mime, @NonNull Disposition disposition, @Nullable String name) {
		String url = registerTempFile(target, mime, disposition, name);
		sourcePage.appendJavascript("WebUI.setSkipLeavePageCheck(true);");
		sourcePage.appendJavascript("location.href=" + StringTool.strToJavascriptString(url, true) + ";");
		sourcePage.appendJavascript("WebUI.setSkipLeavePageCheck(false);");
	}

	@Override
	public void generate(@NonNull DomApplication app, @NonNull String rurl, @NonNull RequestContextImpl param) throws Exception {
		String fkey = param.getPageParameters().getString("key");
		String fpw = param.getPageParameters().getString("passkey");

		//-- Get info block from session.
		FileInfo fi = (FileInfo) param.getSession().getAttribute("tempf-" + fkey);
		if(fi == null || !fi.getPw().equals(fpw) || fi.getSource() == null || !fi.getSource().exists())
			throw new ThingyNotFoundException("The content with content-id " + fkey + "/" + fpw + " cannot be found");

		//-- Present: render to output.
		if(fi.getDisposition() != null)
			param.getRequestResponse().addHeader("Content-Disposition", fi.getDisposition());
		DomApplication.get().getDefaultHTTPHeaderMap().forEach((header, value) -> param.getRequestResponse().addHeader(header, value));
		OutputStream os = param.getRequestResponse().getOutputStream(fi.getMime(), null, (int) fi.getSource().length());
		InputStream is = new FileInputStream(fi.getSource());
		try {
			FileTool.copyFile(os, is);
		} finally {
			try {
				is.close();
			} catch(Exception x) {
			}
		}
	}
}
