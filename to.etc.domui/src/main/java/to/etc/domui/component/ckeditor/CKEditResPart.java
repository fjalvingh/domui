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
package to.etc.domui.component.ckeditor;

import java.io.*;
import java.text.*;
import java.util.*;

import javax.annotation.*;

import to.etc.domui.component.htmleditor.*;
import to.etc.domui.dom.*;
import to.etc.domui.parts.*;
import to.etc.domui.server.*;
import to.etc.domui.server.parts.*;
import to.etc.domui.state.*;
import to.etc.domui.trouble.*;

/**
 * This part handles requests from the HtmlEditor component (using the
 * CKeditor) for images and other files. When called it always gets
 * passed the conversation and the ID of the editor in question. It
 * uses that to retrieve the IEditorFileSystem implementation added
 * on it. That filesystem is then used to generate the required
 * responses.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 1, 2008
 */
public class CKEditResPart implements IUnbufferedPartFactory {
	static private final ThreadLocal<DateFormat> m_format = new ThreadLocal<DateFormat>();

	static private DateFormat getFormatter() {
		DateFormat df = m_format.get();
		if(df == null) {
			df = new SimpleDateFormat("yyyyMMddHHmmss");
			m_format.set(df);
		}
		return df;
	}

	@Override
	public void generate(@Nonnull DomApplication app, @Nonnull String rurl, @Nonnull RequestContextImpl param) throws Exception {
		System.out.println("QS=" + param.getRequestResponse().getQueryString());
		System.out.println("RURL=" + rurl);

		ComponentPartRenderer cpr = new ComponentPartRenderer();
		cpr.initialize(app, param, rurl); // Decode input to get to the component in question.
		if(cpr.getArgs().length != 4)
			throw new IllegalStateException("Invalid input URL '" + rurl + "': must be in format cid/pageclass/componentID/resourceType.");
		String resty = cpr.getArgs()[3];

		if(!(cpr.getComponent() instanceof CKEditor))
			throw new ThingyNotFoundException("The component " + cpr.getComponent().getActualID() + " on page " + cpr.getPage().getBody() + " is not an HtmlEditor instance");
		CKEditor e = (CKEditor) cpr.getComponent();
		IEditorFileSystem ifs = e.getFileSystem();
		if(ifs == null)
			throw new ThingyNotFoundException("The HtmlEditor component " + cpr.getComponent().getActualID() + " on page " + cpr.getPage().getBody() + " has no file system attached to it");

		//-- Create a base URL refering to this part handler && component
		StringBuilder sb = new StringBuilder(128);
		sb.append(UIContext.getRequestContext().getRelativePath(CKEditResPart.class.getName()));
		sb.append("/");
		sb.append(rurl);
		sb.append(".part");

		//-- Finally: handle the command.
		String cmd = param.getParameter("Command");
		if("init".equalsIgnoreCase(cmd))
			sendInit(app, ifs, param);
		else if("getfoldersandfiles".equalsIgnoreCase(cmd))
			sendFolderAndFiles(app, ifs, param, resty, sb);
		else if("File".equalsIgnoreCase(cmd))
			sendFile(app, ifs, param, resty);
		else
			throw new IllegalStateException("Unimplemented command: " + cmd);
	}

	private IBrowserOutput defaultHeader(RequestContextImpl ctx, String cmd, String rtype, String path) throws Exception {
		Writer outputWriter = ctx.getOutputWriter("text/xml; charset=UTF-8", "utf-8");
		IBrowserOutput w = new PrettyXmlOutputWriter(outputWriter);
		w.tag("Connector");
		w.attr("command", cmd);
		w.attr("resourceType", rtype);
		w.endtag();

		//-- CurrentFolder kludge
		w.tag("CurrentFolder");
		w.attr("path", path);
		w.attr("url", "whatteh");
		w.endAndCloseXmltag();

		return w;
	}

	private String getPath(RequestContextImpl ctx, String name) throws Exception {
		String rpath = ctx.getParameter(name);
		if(rpath == null)
			rpath = "";
		while(rpath.startsWith("/"))
			rpath = rpath.substring(1);
		if(rpath.contains("..") || rpath.contains(":") || rpath.contains("\\")) // May not go UP nor can it contain a drive letter or backslash
			throw new IllegalStateException("Invalid input path");
		return rpath;
	}

	private void sendFolderAndFiles(DomApplication app, IEditorFileSystem ifs, RequestContextImpl ctx, String type, CharSequence baseURL) throws Exception {
		String rpath = getPath(ctx, "CurrentFolder");
		IBrowserOutput w = defaultHeader(ctx, "GetFolderAndFiles", type, rpath);

		w.tag("Error");
		w.attr("number", 0);
		w.endAndCloseXmltag();

		//-- Folders.
		w.tag("Folders");
		w.endtag();
		List< ? > resl = ifs.getFilesAndFolders(type, rpath);

		for(Object o : resl) {
			if(o instanceof EditorFolder) {
				EditorFolder ef = (EditorFolder) o;
				w.tag("Folder");
				w.attr("name", ef.getName());
				w.attr("hasChildren", ef.isHasChildren());
				w.attr("acl", ef.getAcl());
				w.endAndCloseXmltag();
			}
		}
		w.closetag("Folders");

		w.tag("Files");
		w.endtag();
		StringBuilder sb = new StringBuilder(128);
		for(Object o : resl) {
			if(o instanceof EditorFile) {
				EditorFile ef = (EditorFile) o;
				w.tag("File");
				w.attr("name", ef.getName());
				w.attr("date", getFormatter().format(ef.getDate()));
				int sz = ef.getSize() / 1024;
				w.attr("size", sz == 0 ? 1 : sz);

				//-- Pass on an explicit URL for this, because file access needs to go thru this thingerydoo
				sb.setLength(0);
				sb.append(baseURL);
				sb.append("?Command=File&path=");
				sb.append(rpath);
				sb.append('/');
				sb.append(ef.getName());
				w.attr("url", sb.toString());
				w.endAndCloseXmltag();
			}
		}
		w.closetag("Files");

		w.closetag("Connector");
	}

	private void sendInit(DomApplication app, IEditorFileSystem ifs, RequestContextImpl ctx) throws Exception {
		IBrowserOutput w = new PrettyXmlOutputWriter(ctx.getOutputWriter("text/xml; charset=UTF-8", "utf-8"));
		w.tag("Connector");
		w.endtag();
		w.tag("Error");
		w.attr("number", 0);
		w.endAndCloseXmltag();

		w.tag("ConnectorInfo");
		w.attr("enabled", "true");
		w.attr("s", "");
		w.attr("c", "");
		w.attr("thumbsEnabled", "true");
		w.endAndCloseXmltag();

		w.tag("ResourceTypes");
		w.endtag();
		for(EditorResourceType t : ifs.getResourceTypes()) {
			w.tag("ResourceType");
			w.attr("name", t.getName());
			w.attr("url", t.getRootURL());
			w.attr("allowedExtensions", t.getAllowedExtensions().toString().replace("[", "").replace("]", ""));
			w.attr("deniedExtensions", t.getDeniedExtensions().toString().replace("[", "").replace("]", ""));
			w.attr("defaultView", "Thumbnails");
			w.attr("acl", 255);
			w.endAndCloseXmltag();
		}

		w.closetag("ResourceTypes");

		w.closetag("Connector");
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	File content sender.								*/
	/*--------------------------------------------------------------*/
	/**
	 *
	 * @param app
	 * @param ifs
	 * @param ctx
	 * @param type
	 * @throws Exception
	 */
	private void sendFile(DomApplication app, IEditorFileSystem ifs, RequestContextImpl ctx, String type) throws Exception {
		String rpath = getPath(ctx, "path");
		if(rpath.length() == 0)
			throw new ThingyNotFoundException("IEditorFileSystem file with path=" + rpath);
		IEditorFileRef efr = ifs.getStreamRef(type, rpath);
		if(efr == null)
			throw new ThingyNotFoundException("IEditorFileSystem file with path=" + rpath + " does not return a reference");

		//-- Stream the thingy.
		OutputStream os = null;
		try {
			int len = efr.getSize();
			os = ctx.getRequestResponse().getOutputStream(efr.getMimeType(), null, len);
			efr.copyTo(os);
			os.close();
			os = null;
		} finally {
			try {
				efr.close();
			} catch(Exception x) {}
			try {
				if(os != null)
					os.close();
			} catch(Exception x) {}
		}
	}
}
