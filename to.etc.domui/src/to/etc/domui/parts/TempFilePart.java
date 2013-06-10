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
import java.sql.*;

import javax.annotation.*;

import to.etc.domui.server.*;
import to.etc.domui.server.parts.*;
import to.etc.domui.trouble.*;
import to.etc.util.*;

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

	static public String registerTempFile(@Nonnull IRequestContext ctx, @Nonnull File target, @Nonnull String mime, @Nullable String type, @Nullable String name) {
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
	 * Saves blob into temporary file, register temporary file in provided context, and returns generated download link.
	 * @param ctx
	 * @param blob
	 * @param mime
	 * @param type
	 * @param filename
	 * @return
	 * @throws Exception
	 */
	public static String getDownloadLink(@Nonnull IRequestContext ctx, @Nonnull Blob blob, String mime, String type, String filename) throws Exception {
		File temp = File.createTempFile("tmp", ".tmp");
		FileTool.saveBlob(temp, blob);
		return TempFilePart.registerTempFile(ctx, temp, mime, type, filename);
	}

	@Override
	public void generate(DomApplication app, String rurl, RequestContextImpl param) throws Exception {
		String fkey = param.getParameter("key");
		String fpw = param.getParameter("passkey");
		if(fkey == null || fpw == null)
			throw new ThingyNotFoundException("Invalid arguments.");

		//-- Get info block from session.
		FileInfo fi = (FileInfo) param.getSession().getAttribute("tempf-" + fkey);
		if(fi == null || !fi.getPw().equals(fpw) || fi.getSource() == null || !fi.getSource().exists())
			throw new ThingyNotFoundException("The content with content-id " + fkey + "/" + fpw + " cannot be found");

		//-- Present: render to output.
		param.getResponse().setContentType(fi.getMime());
		if(fi.getDisposition() != null)
			param.getResponse().addHeader("Content-Disposition", fi.getDisposition());
		param.getResponse().setContentLength((int) fi.getSource().length());
		OutputStream os = param.getResponse().getOutputStream();
		InputStream	is	= new FileInputStream(fi.getSource());
		try {
			FileTool.copyFile(os, is);
		} finally {
			try {
				is.close();
			} catch(Exception x) {}
		}
	}
}
