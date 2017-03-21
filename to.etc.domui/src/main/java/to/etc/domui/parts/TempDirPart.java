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

import javax.annotation.*;
import javax.servlet.http.*;

import to.etc.domui.server.*;
import to.etc.domui.server.parts.*;
import to.etc.domui.trouble.*;
import to.etc.util.*;
import to.etc.webapp.core.*;

/**
 * Safe reference to a server-side tempfile.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 20, 2010
 */
public class TempDirPart implements IUnbufferedPartFactory {
	static private class TmpDir implements HttpSessionBindingListener {
		@Nonnull
		final private File m_dir;

		public TmpDir(@Nonnull File dir) {
			m_dir = dir;
		}

		@Nonnull
		public File getDir() {
			return m_dir;
		}

		@Override
		public void valueBound(HttpSessionBindingEvent arg0) {}

		@Override
		public void valueUnbound(HttpSessionBindingEvent arg0) {
			try {
				FileTool.deleteDir(m_dir);
			} catch(Exception x) {}
		}
	}

	static public String registerTempDir(@Nonnull IRequestContext ctx, @Nonnull File target) {
		String key = StringTool.generateGUID();

		ctx.getSession().setAttribute("tmpdir-" + key, new TmpDir(target));				// Store in session context

		StringBuilder sb = new StringBuilder();
		sb.append(TempDirPart.class.getName());
		sb.append(".part/").append(key).append("/");
		return sb.toString();
	}

	/**
	 * Generate the appropriate resource inside the tmpdir passed.
	 * @see to.etc.domui.server.parts.IUnbufferedPartFactory#generate(to.etc.domui.server.DomApplication, java.lang.String, to.etc.domui.server.RequestContextImpl)
	 */
	@Override
	public void generate(@Nonnull DomApplication app, @Nonnull String rurl, @Nonnull RequestContextImpl param) throws Exception {
		//-- 1. Get the key from the 1st part of the url,
		int pos = rurl.indexOf('/');
		if(pos == -1)
			throw new ThingyNotFoundException("No / in path " + rurl);
		String key = rurl.substring(0, pos);					// Get the GUID key from the path
		TmpDir dir = (TmpDir) param.getSession().getAttribute("tmpdir-" + key);	// Can we find this stored?
		if(null == dir) {
			throw new ThingyNotFoundException("Tempdir "+key+" not found in session");
		}

		//-- Construct the file inside the dir
		File inside = new File(dir.getDir(), rurl.substring(pos + 1));	// File inside tmpdir
		if(!inside.exists() || !inside.isFile())
			throw new ThingyNotFoundException(rurl + ": no such temp resource");

		//-- Present: render to output.
		String mime = ServerTools.getMimeType(inside.getName());
		if(null == mime)
			mime = "application/octet-stream";
		OutputStream os = param.getRequestResponse().getOutputStream(mime, null, (int) inside.length());
		InputStream is = new FileInputStream(inside);
		try {
			FileTool.copyFile(os, is);
		} finally {
			FileTool.closeAll(is);
		}
	}
}
