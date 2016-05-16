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
package to.etc.domui.util.upload;

import java.io.*;
import java.util.*;

import javax.servlet.http.*;

import to.etc.util.*;

/**
 * This is a file upload parser which parses input type=file
 * requests as posted from forms. It expects an input stream
 * encoded as described in <a href="http://www.ietf.org/rfc/rfc1867.txt">RFC 1867</a>.
 * The input stream is split into it's constituent parts and returned
 * as a parameter map.
 * <p>This code replaces the apache fileupload code because that code does
 * not properly return the fact that *no* file was uploaded (i.e. the input
 * was empty), and because that implementation does not return an actual File
 * without casting.
 * <p>To use this, first ask if a HttpRequest is a valid upload request using the
 * "isMultipartContent()" call. Then ask an instance of this class to parse
 * the request. This returns a Map of items present in the request.
 *
 * <p>Created on Nov 21, 2005
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class UploadParser {
	static private final String MULTIPART = "multipart/";

	//    static private final String MULTIPART_FORM_DATA = "multipart/form-data";
	static private final String MULTIPART_MIXED = "multipart/mixed";

	static private final String CONTENT_DISPOSITION = "Content-disposition";

	static private final String FORM_DATA = "form-data";

	static private final String CONTENT_TYPE = "Content-type";

	static private final String ATTACHMENT = "attachment";

	/** The max size of a file uploaded thru this mechanism. Defaults to 100MB. */
	private int m_sizeMax = 100 * 1024 * 1024;

	public UploadParser() {}

	public final int getSizeMax() {
		return m_sizeMax;
	}

	public final void setSizeMax(final int sizeMax) {
		m_sizeMax = sizeMax;
	}

	//	public final void setWorkDir(File workDir) {
	//		m_workDir = workDir;
	//	}

	/**
	 * Returns T if the request is encoded as multipart (i.e. file upload).
	 * @param req
	 * @return
	 */
	public static final boolean isMultipartContent(HttpServletRequest req) {
		if(!"POST".equalsIgnoreCase(req.getMethod())) // Must be post method
			return false;
		String contentType = req.getContentType(); // Must be multipart encoding.
		if(contentType == null)
			return false;
		return contentType.toLowerCase().startsWith(MULTIPART);
	}

	public static final HttpServletRequest wrapIfNeeded(final HttpServletRequest req) {
		if(!isMultipartContent(req))
			return req;
		if(UploadHttpRequestWrapper.findWrapper(req) != null)
			return req;
		return new UploadHttpRequestWrapper(req);
	}

	//	private synchronized File getWorkDir() {
	//		if(m_workDir == null) {
	//			String iodir = System.getProperty("java.io.tmpdir");
	//			if(iodir == null)
	//				iodir = "/tmp";
	//			File f = new File(iodir, "uploads");
	//			f.mkdirs();
	//			m_workDir = f;
	//		}
	//		return m_workDir;
	//	}
	//

	/*--------------------------------------------------------------*/
	/*	CODING:	Main parser entrypoint.								*/
	/*--------------------------------------------------------------*/
	public List<UploadItem> parseRequest(final HttpServletRequest ctx, final String hdrencoding) throws FileUploadException, IOException {
		try {
			//-- Get all data from the request and pass to the main parser.
			String ct = ctx.getContentType();
			int requestSize = ctx.getContentLength();

			//		ByteArrayOutputStream bos = new ByteArrayOutputStream(1 * 1024 * 1024);
			//		InputStream tis = ctx.getInputStream();
			//		FileTool.copyFile(bos, tis);
			//		tis.close();
			//		bos.close();
			//
			//		//-- Write data to tmpfile
			//		byte[] data = bos.toByteArray();
			//		FileTool.save(new File("/tmp/in.bin"), new byte[][]{data});
			//
			//		final InputStream sis = new ByteArrayInputStream(data);
			final InputStream is = ctx.getInputStream();

			return parseRequest(is, hdrencoding, ct, requestSize);
		} catch(EOFException ex) {
			throw new FileUploadInterruptedException(ex);
		}
	}

	static private String getStringHeader(final Map<String, Object> hdr, final String name) {
		Object o = hdr.get(name.toLowerCase());
		if(o == null || o instanceof List< ? >)
			return null;
		return (String) o;
	}

	static private void discardItems(final List<UploadItem> l) {
		for(int i = l.size(); --i >= 0;) {
			UploadItem ui = l.get(i);
			try {
				ui.discard();
			} catch(Exception x) {
				x.printStackTrace();
			}
		}
	}

	public List<UploadItem> parseRequest(final InputStream is, final String hdrencoding, final String contentType, final int requestSize) throws FileUploadException, IOException {
		//-- Check parameters
		if((contentType == null) || (!contentType.toLowerCase().startsWith(MULTIPART)))
			throw new FileUploadException("Content type is not an accepted multipart type but " + contentType);
		if(requestSize == -1)
			throw new FileUploadException("The content length is missing or invalid");

		/*
		 * jal 20160225 The code below was disabled because it does not work with Tomcat. When the request
		 * is not fully loaded (due to the exception being thrown) Tomcat will send the request AGAIN a
		 * few times.
		 *
		 * The current solution to handle max file size is this:
		 * 1. The Javascript tries to check the file size using the files[0] member of the input type file.
		 * 2. If that fails we have no other choice than to read all input. While doing so the max size
		 * for each fragment is checked. If any fragment exceeds it the exception is thrown AFTER
		 * reading all input. This ensures that server-side handling is correct if javascript fails.
		 */
		//if(requestSize > m_sizeMax)
		//	throw new FileUploadSizeExceededException(m_sizeMax, "The uploaded data exceeds the max size that can be uploaded (the max size is " + StringTool.strSize(m_sizeMax) + ", the upload is "
		//		+ StringTool.strSize(requestSize) + ")");
		MiniParser p = new MiniParser();
		byte[] boundary = decodeBoundary(p, contentType);
		if(boundary == null)
			throw new FileUploadException("The request did not specify a 'boundary'");

		//-- We're done: start scanning the stream.
		HeaderParser hp = new HeaderParser();
		Map<String, Object> headermap = new HashMap<String, Object>();
		MultipartStream multi = new MultipartStream(is, boundary);
		multi.setHeaderEncoding(hdrencoding);
		List<UploadItem> l = new ArrayList<UploadItem>();
		if(!multi.skipPreamble()) // Something there?
			return l;
		boolean ok = false;
		boolean toolarge = false;
		try {
			for(;;) {
				//-- Get the headers into the scratch map
				hp.parse(headermap, multi.readHeaders(), true);
				String fieldname = decodeHeaderItem(headermap, CONTENT_DISPOSITION, "name", FORM_DATA, null, p);

				if(fieldname == null)
					multi.discardBodyData(); // No name: skip this fragment
				else {
					String subContentType = getStringHeader(headermap, CONTENT_TYPE);
					if(subContentType != null && subContentType.toLowerCase().startsWith(MULTIPART_MIXED)) {
						byte[] subBoundary = decodeBoundary(p, subContentType);
						if(subBoundary == null)
							throw new FileUploadException("The sub-content type did not specify a 'boundary'");

						multi.setBoundary(subBoundary);
						boolean nextSubPart = multi.skipPreamble();
						while(nextSubPart) {
							hp.parse(headermap, multi.readHeaders(), true); // Decode this-fragment's headers
							String filename = decodeHeaderItem(headermap, CONTENT_DISPOSITION, "filename", FORM_DATA, ATTACHMENT, p);
							if(filename != null) {
								if(! readItem(p, l, headermap, multi, fieldname, filename))
									toolarge = true;
							} else
								multi.discardBodyData(); // Not a file: ignore.
							nextSubPart = multi.readBoundary();
						}
						multi.setBoundary(boundary); // Reset boundary for originating.
					} else {
						//-- Not a multithingy: get single field or file
						String filename = decodeHeaderItem(headermap, CONTENT_DISPOSITION, "filename", FORM_DATA, ATTACHMENT, p);
						if(! readItem(p, l, headermap, multi, fieldname, filename))
							toolarge = true;
					}
				}
				if(!multi.readBoundary()) {
					if(toolarge)
						throw new FileUploadSizeExceededException(m_sizeMax);
					ok = true;
					return l;
				}
			}
		} finally {
			if(!ok)
				discardItems(l);
		}
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Utility functions.									*/
	/*--------------------------------------------------------------*/
	/**
	 * Decodes the chunk boundary from the content type header.
	 */
	private byte[] decodeBoundary(final MiniParser p, final String contentType) {
		p.init(contentType);
		while(p.parseNext()) {
			if("boundary".equalsIgnoreCase(p.getProperty())) {
				String val = p.getValue();
				if(val == null || val.length() == 0)
					return null;

				//-- Convert boundary to bytes
				try {
					return val.getBytes("ISO-8859-1");
				} catch(Exception x) {
					return val.getBytes();
				}
			}
		}

		//-- No boundary parameter found -> exit.
		return null;
	}

	private String decodeHeaderItem(final Map<String, Object> headers, final String headername, final String keyname, final String h1, final String h2, final MiniParser mp) {
		Object o = headers.get(headername.toLowerCase());
		if(o == null || !(o instanceof String))
			return null;
		String hdr = (String) o;
		if(!hdr.startsWith(h1)) {
			if(h2 == null || !hdr.startsWith(h2))
				return null;
		}

		//-- Header found.. Now locate the thingy we need.
		mp.init(hdr);
		while(mp.parseNext()) {
			if(keyname.equalsIgnoreCase(mp.getProperty())) {
				//-- Found the thingy...
				if(mp.getValue() == null)
					return null;
				return mp.getValue().trim();
			}
		}
		return null;
	}

	/**
	 * Handle reading a single item. If the 'filename' field is not null then this is an actual file, else
	 * it is a form field.
	 *
	 * @param l
	 * @param headermap
	 * @param multi
	 * @param fn
	 * @throws IOException
	 */
	//	@SuppressWarnings("null")
	private boolean readItem(final MiniParser p, final List<UploadItem> l, final Map<String, Object> headermap, final MultipartStream multi, final String fieldname, String fn) throws IOException {
		String contenttype = getStringHeader(headermap, CONTENT_TYPE);
		String charset = null;
		if(contenttype != null) {
			p.init(contenttype);
			while(p.parseNext()) {
				if("charset".equalsIgnoreCase(p.getProperty()) && p.getValue() != null) {
					charset = p.getValue();
					break;
				}
			}
		}
		if(charset == null)
			charset = "utf-8"; // URGENT FIXME Where is the encoding hidden if not here??

		/*
		 *
		 * If "filename" is null it was not passed and this is a form item.
		 * If it's not null but the empty string then the upload file box
		 * was left empty.
		 */
		boolean isfile = fn != null; // Is a file item?
		if(fn != null && fn.trim().length() == 0)
			fn = null; // Empty string means no file entered

		UploadItem ui = new UploadItem(fieldname, contenttype, charset, fn, isfile);

		//-- Copy data to an output buffer or output file, depending on the size.
		OutputStream os = null;
		ByteArrayOutputStream bos = null;
		File resf = null;

		//-- If this is a file write it to a tempfile, else write it to a byte array && convert to a string value
		int bytesRead = -1;
		boolean ok = false;
		try {
			if(fn != null) {
				String ext = FileTool.getFileExtension(fn);
				if(ext.length() == 0)
					ext = "tmp";
				resf = File.createTempFile("upld", "." + ext);
				os = new FileOutputStream(resf);
			} else {
				bos = new ByteArrayOutputStream(8192);
				os = bos;
			}
			bytesRead = multi.readBodyData(os, m_sizeMax);
			os.close();
			os = null;
			ok = true;
		} finally {
			try {
				if(os != null)
					os.close();
			} catch(Exception x) {}
			if(!ok) {
				try {
					if(resf != null)
						resf.delete();
				} catch(Exception x) {}
			}
		}

		if(bytesRead >= m_sizeMax)
			return false;

		//-- Decode worked, and data flushed either to bytearray or file...
		if(bos != null) {
			byte[] data = bos.toByteArray();
			String val = new String(data, charset);
			ui.setValue(val);
		} else {
			ui.setValue(resf);
		}
		l.add(ui);
		return true;
	}
}
