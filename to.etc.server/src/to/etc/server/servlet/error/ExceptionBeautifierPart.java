package to.etc.server.servlet.error;

import java.io.*;

import javax.servlet.*;

import to.etc.server.*;
import to.etc.server.servlet.*;
import to.etc.server.servlet.parts.*;
import to.etc.server.vfs.*;
import to.etc.util.*;

public class ExceptionBeautifierPart implements UnbufferedPartFactory {
	//	private RequestContext			m_ctx;

	public void generate(RequestContext rctx, String input, VfsPathResolver r) throws Exception {
		//-- The 1st part is the name of the source used; the rest is relative to that source
		int pos = input.indexOf('/');
		if(pos == -1)
			throw new ServletException("Missing / in url.");
		String source = input.substring(0, pos);
		String rurl = input.substring(pos + 1);
		ExceptionBeautifier.Source src = ExceptionBeautifier.findSourceByName(source, rurl);
		OutputStream os = null;
		try {
			if(src == null) {
				System.out.println("No ExceptionBeautifier stream for " + input);
				rctx.getResponse().sendError(404, "No ExceptionBeautifier stream for " + input);
				return;
			}

			//-- Find a mime type by extension.
			String ext = FileTool.getFileExtension(rurl);
			String mime = ServerTools.getExtMimeType(ext);
			if(mime == null)
				mime = "application/octet-stream";
			else if(mime.startsWith("text/"))
				mime = mime + "; encoding=" + src.getEncoding();
			rctx.getResponse().setContentType(mime);
			os = rctx.getResponse().getOutputStream();
			FileTool.copyFile(os, src.getInput());
		} finally {
			try {
				if(os != null)
					os.close();
			} catch(Exception x) {}
			try {
				if(src != null)
					src.getInput().close();
			} catch(Exception x) {}
		}
	}
}
