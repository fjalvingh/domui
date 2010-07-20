package to.etc.domui.parts;

import java.io.*;

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
		private String m_key;

		private String m_pw;

		private File m_source;

		private String m_mime;

		public FileInfo(String key, String pw, File source, String mime) {
			m_key = key;
			m_pw = pw;
			m_source = source;
			m_mime = mime;
		}

		public String getKey() {
			return m_key;
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

	static public String registerTempFile(IRequestContext ctx, File target, String mime) {
		String key = StringTool.generateGUID();
		String pw = StringTool.generateGUID();
		FileInfo fi = new FileInfo(key, pw, target, mime);
		ctx.getSession().setAttribute("tempf-" + key, fi); // Store in session context

		StringBuilder sb = new StringBuilder();
		sb.append(TempFilePart.class.getName());
		sb.append(".ui?key=").append(key).append("&passkey=").append(pw);
		return sb.toString();
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
