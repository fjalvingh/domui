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
 * @author <a href="mailto:jal@mumble.to">Frits Jalvingh</a>
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

	public final void setSizeMax(int sizeMax) {
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

	public static final HttpServletRequest wrapIfNeeded(HttpServletRequest req) {
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
	public List<UploadItem> parseRequest(HttpServletRequest ctx, String hdrencoding) throws FileUploadException, IOException {
		//-- Get all data from the request and pass to the main parser.
		String ct = ctx.getContentType();
		int requestSize = ctx.getContentLength();

		return parseRequest(ctx.getInputStream(), hdrencoding, ct, requestSize);
	}

	static private String getStringHeader(Map<String, Object> hdr, String name) {
		Object o = hdr.get(name.toLowerCase());
		if(o == null || o instanceof List)
			return null;
		return (String) o;
	}

	static private void discardItems(List<UploadItem> l) {
		for(int i = l.size(); --i >= 0;) {
			UploadItem ui = l.get(i);
			try {
				ui.discard();
			}
			catch(Exception x) {
				x.printStackTrace();
			}
		}
	}

	public List<UploadItem> parseRequest(InputStream is, String hdrencoding, String contentType, int requestSize) throws FileUploadException, IOException {
		//-- Check parameters
		if((contentType == null) || (!contentType.toLowerCase().startsWith(MULTIPART)))
			throw new FileUploadException("Content type is not an accepted multipart type but " + contentType);
		if(requestSize == -1)
			throw new FileUploadException("The content length is missing or invalid");
		if(requestSize > m_sizeMax)
			throw new FileUploadSizeExceededException("The uploaded data exceeds the max size that can be uploaded (the max size is "
					+ StringTool.strSize(m_sizeMax) + ", the upload is " + StringTool.strSize(requestSize) + ")");
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
							if(filename != null)
								readItem(p, l, headermap, multi, fieldname, filename);
							else
								multi.discardBodyData(); // Not a file: ignore.
							nextSubPart = multi.readBoundary();
						}
						multi.setBoundary(boundary); // Reset boundary for originating.
					}
					else {
						//-- Not a multithingy: get single field or file
						String filename = decodeHeaderItem(headermap, CONTENT_DISPOSITION, "filename", FORM_DATA, ATTACHMENT, p);
						readItem(p, l, headermap, multi, fieldname, filename);
					}
				}
				if(!multi.readBoundary()) // Is there more?
				{
					ok = true;
					return l;
				}
			}
		}
		finally {
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
	private byte[] decodeBoundary(MiniParser p, String contentType) {
		p.init(contentType);
		while(p.parseNext()) {
			if("boundary".equalsIgnoreCase(p.getProperty())) {
				String val = p.getValue();
				if(val == null || val.length() == 0)
					return null;

				//-- Convert boundary to bytes
				try {
					return val.getBytes("ISO-8859-1");
				}
				catch(Exception x) {
					return val.getBytes();
				}
			}
		}

		//-- No boundary parameter found -> exit.
		return null;
	}

	private String decodeHeaderItem(Map<String, Object> headers, String headername, String keyname, String h1, String h2, MiniParser mp) {
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
	private void readItem(MiniParser p, List<UploadItem> l, Map<String, Object> headermap, MultipartStream multi, String fieldname, String fn) throws IOException {
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

		/*
		 * 
		 * If "filename" is null it was not passed and this is a form item.
		 * If it's not null but the empty string then the upload file box
		 * was left empty.
		 */
		boolean isfile = fn != null; // Is a file item?
		if(isfile && fn.trim().length() == 0)
			fn = null; // Empty string means no file entered

		ImplUploadItem ui = new ImplUploadItem(fieldname, contenttype, charset, fn, isfile);

		//-- Copy data to an output buffer or output file, depending on the size.
		OutputStream			os	= null;
		ByteArrayOutputStream	bos	= null;
		File					resf= null;

		//-- If this is a file write it to a tempfile, else write it to a byte array && convert to a string value
		boolean ok = false;
		try {
			if(fn != null) {
				String ext = FileTool.getFileExtension(fn);
				if(ext.length() == 0)
					ext = "tmp";
				resf	= File.createTempFile("upld", "."+ext);
				os	= new FileOutputStream(resf);
			} else {
				bos	= new ByteArrayOutputStream(8192);
				os = bos;
			}
			multi.readBodyData(os);
			os.close();
			os	= null;
			ok = true;
		} finally {
			try { if(os != null) os.close(); } catch(Exception x) {}
			if(! ok) {
				try { if(resf != null) resf.delete(); } catch(Exception x) {}
			}
		}

		//-- Decode worked, and data flushed either to bytearray or file...
		if(bos != null) {
			byte[]	data = bos.toByteArray();
			String val = new String(data, charset);
			ui.setValue(val);
		} else {
			ui.setValue(resf);
		}
		l.add(ui);
	}
}
