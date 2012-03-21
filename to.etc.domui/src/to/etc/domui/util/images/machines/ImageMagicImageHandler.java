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
package to.etc.domui.util.images.machines;

import java.io.*;
import java.util.*;

import to.etc.domui.util.*;
import to.etc.domui.util.images.converters.*;
import to.etc.util.*;

final public class ImageMagicImageHandler implements ImageHandler {
	static public final String PNG = "image/png";

	static public final String JPEG = "image/jpeg";

	static public final String JPG = "image/jpg";

	static public final String GIF = "image/gif";

	static private final String[] UNIXPATHS = {"/usr/bin", "/usr/local/bin", "/bin", "/opt/imagemagick"};

	static private final String[] WINDOWSPATHS = {"c:\\program files\\ImageMagick", "c:\\Windows"};

	static private final String DENSITY = "400";

	static private boolean m_initialized;

	static private ImageMagicImageHandler m_instance;

	private File m_convert;

	private File m_identify;

	private File m_fileCommand;

	/** Allow max. 2 concurrent ImageMagick tasks to prevent server trouble. */
	private int m_maxTasks = 2;

	/** The current #of running tasks. */
	private int m_numTasks;

	private ImageMagicImageHandler() {}

	/**
	 * This returns the ImageMagic manipulator *if* it is available. If
	 * ImageMagic is not available then this returns null.
	 * @return
	 */
	static public synchronized ImageMagicImageHandler getInstance() {
		if(!m_initialized)
			initialize();
		return m_instance;
	}

	static private boolean onWindows() {
		return File.separatorChar == '\\';
	}

	/**
	 * Initializes and checks to see if ImageMagick is present.
	 * @return
	 */
	static private synchronized void initialize() {
		m_initialized = true;
		String ext = "";

		List<String> pathlist = new ArrayList<String>();
		if(File.separatorChar == '\\') {
			pathlist.addAll(Arrays.asList(WINDOWSPATHS));
			ext = ".exe";
		} else {
			pathlist.addAll(Arrays.asList(UNIXPATHS));
			ext = "";
		}
		String path = System.getenv("PATH");
		if(path != null) {
			String[] list = path.split("\\" + File.pathSeparator);
			if(list != null)
				pathlist.addAll(Arrays.asList(list));
		}
		System.out.println("ImageMagickHandler: paths " + pathlist);

		ImageMagicImageHandler m = new ImageMagicImageHandler();

		//-- Locate the Linux 'file' command, if present,
		for(String s : pathlist) {
			File f = new File(s, "file" + ext);
			if(f.exists()) {
				m.m_fileCommand = f;
				break;
			}
		}

		//-- Locate ImageMagick using predefined paths;
		for(String s : pathlist) {
			File f = new File(s, "convert" + ext);
			if(f.exists()) {
				m.m_convert = f;
				f = new File(s, "identify" + ext);
				if(f.exists()) {
					m.m_identify = f;
					m_instance = m;
					return;
				}
			}
		}
		System.out.println("Warning: ImageMagick not found.");
	}

	/**
	 * Waits for a task slot to become available.
	 */
	private synchronized void start() {
		while(m_numTasks >= m_maxTasks) {
			try {
				wait();
			} catch(InterruptedException ix) {
				throw new RuntimeException(ix);
			}
		}
		m_numTasks++; // Use one
	}

	private synchronized void done() {
		m_numTasks--;
		notify();
	}

	/**
	 * Runs the "identify" call and returns per-page info.
	 *
	 * @param input
	 * @return
	 */
	@Override
	public ImageInfo identify(File input) throws Exception {
		//		start();
		try {
			//-- Start with issuing a 'file' command, if available
			StringBuilder sb = new StringBuilder(8192);
			String typeDescription = null;
			if(m_fileCommand != null) {
				ProcessBuilder pb = new ProcessBuilder(m_fileCommand.getAbsolutePath(), "-b", input.getAbsolutePath());
				int xc = ProcessTools.runProcess(pb, sb);
				if(xc == 0) {
					String txt = sb.toString().trim();
					int len = txt.length();
					int ix = 0;
					while(ix < len) {
						char c = txt.charAt(ix);
						if(c != '\r' && c != '\n' && !Character.isWhitespace(c))
							break;
						ix++;
					}
					if(ix < len) {
						int epos = txt.indexOf('\n', ix);
						if(epos != -1)
							txt = txt.substring(ix, epos).trim();
						else
							txt = txt.substring(ix).trim();
					}
					typeDescription = txt;
				}
			}

			//-- Start 'identify' and capture the resulting data
			ProcessBuilder pb = new ProcessBuilder(m_identify.toString(), "-ping", input.toString());
			sb.setLength(0);
			int xc = ProcessTools.runProcess(pb, sb);
			if(xc != 0) {
				//-- Identify has failed... Assume the format is incorrect - should we fix this later?
				return new ImageInfo(null, typeDescription, false, Collections.EMPTY_LIST);
				//				throw new Exception("External command exception: " + m_identify + " returned error code " + xc + "\n" + sb.toString());
			}

			//			System.out.println("identify: result=" + sb.toString());
			//-- Walk the resulting thingy
			List<OriginalImagePage> list = new ArrayList<OriginalImagePage>();
			LineNumberReader lr = new LineNumberReader(new StringReader(sb.toString()));
			String mime = null;
			String line;
			while(null != (line = lr.readLine())) {
				StringTokenizer st = new StringTokenizer(line, " \t");
				if(st.hasMoreTokens()) {
					String file = st.nextToken();
					if(st.hasMoreTokens()) {
						String type = st.nextToken();
						if(st.hasMoreTokens()) {
							String size = st.nextToken();
							OriginalImagePage dap = decodePage(file, type, size);
							if(dap != null) {
								list.add(dap);
								if(mime == null)
									mime = dap.getMimeType();
							}
						}
					}
				}
			}
			ImageInfo oid = new ImageInfo(mime, typeDescription, true, list);
			return oid;
		} finally {
			//			done();
		}
	}

	static private OriginalImagePage decodePage(String file, String type, String size) {
		int page = 0;
		int pos = file.indexOf('[');
		if(pos != -1) {
			//-- Embedded page #
			int epos = file.indexOf(']', pos + 1);
			if(epos != -1) {
				page = StringTool.strToInt(file.substring(pos + 1, epos), 0);
			}
		}

		//-- 2. Decode size,
		pos = size.indexOf('x');
		if(pos == -1)
			return null;
		int width = StringTool.strToInt(size.substring(0, pos), 0);
		int height = StringTool.strToInt(size.substring(pos + 1), 0);
		if(width == 0 || height == 0)
			return null;
		String s = type.toLowerCase();
		String mime = ServerTools.getExtMimeType(s);
		OriginalImagePage dap = new OriginalImagePage(page, width, height, mime, type, false);
		return dap;
	}

	static private String findExt(String mime) {
		if(mime.equalsIgnoreCase(GIF))
			return "gif";
		else if(mime.equalsIgnoreCase(JPEG) || mime.equalsIgnoreCase(JPG))
			return "jpg";
		else if(mime.equalsIgnoreCase(PNG))
			return "png";
		return null;
	}

	/**
	 * Create a thumbnail from a source image spec.
	 * @param source
	 * @return
	 * @throws Exception
	 */
	@Override
	public ImageSpec thumbnail(ImageConverterHelper h, ImageSpec source, int page, int width, int height, String targetMime) throws Exception {
		//-- Create a thumb.
		start();
		try {
			//-- Create an extension for the target mime type,
			String ext = findExt(targetMime);
			if(ext == null)
				throw new IllegalArgumentException("The mime type '" + targetMime + "' is not supported");
			File tof = h.createWorkFile(ext);

			ProcessBuilder pb = null;
			if(width != 0 && height != 0) {
				pb = new ProcessBuilder(m_convert.toString(), source.getSource().toString() + "[" + page + "]", "-density", DENSITY, "-thumbnail", width + "x" + height, tof.toString());
			} else {
				//in case that 0 size is passed, use original width / height
				pb = new ProcessBuilder(m_convert.toString(), source.getSource().toString() + "[" + page + "]", "-density", DENSITY, tof.toString());
			}
			//System.out.println("Command: " + pb.command().toString());

			StringBuilder sb = new StringBuilder(8192);

			int xc = ProcessTools.runProcess(pb, sb);
			//			System.out.println("convert: " + sb.toString());
			if(xc != 0)
				throw new Exception("External command exception: " + m_convert + " returned error code " + xc + "\n" + sb.toString());
			return new ImageSpec(tof, targetMime, width, height);
		} finally {
			done();
		}
	}

	@Override
	public ImageSpec scale(ImageConverterHelper h, ImageSpec source, int page, int width, int height, String targetMime) throws Exception {
		if(onWindows()) {
			return thumbnail(h, source, page, width, height, targetMime);
		}
		//-- Create a scaled image
		start();
		try {
			String ext = findExt(targetMime);
			if(ext == null)
				throw new IllegalArgumentException("The mime type '" + targetMime + "' is not supported");
			File tof = h.createWorkFile(ext);

			//-- Start 'identify' and capture the resulting data.
			ProcessBuilder pb = null;
			//-- jal 20100906 Use thumbnail, not resize: resize does not properly filter causing an white image because all black pixels are sized out.
			if(width != 0 && height != 0) {
				String rsz = width + "x" + height;
				pb = new ProcessBuilder(m_convert.toString(), "-density", DENSITY, "-size", rsz, source.getSource().toString() + "[" + page + "]", "-thumbnail", rsz, "-coalesce", "-quality", "100", tof.toString());
			} else {
				//in case that 0 size is passed, use original width / height
				pb = new ProcessBuilder(m_convert.toString(), source.getSource().toString() + "[" + page + "]", "-density", DENSITY, tof.toString());
			}

			//System.out.println("Command: " + pb.command().toString());
			StringBuilder sb = new StringBuilder(8192);
			int xc = ProcessTools.runProcess(pb, sb);
			//			System.out.println("convert: " + sb.toString());
			if(xc != 0)
				throw new Exception("External command exception: " + m_convert + " returned error code " + xc + "\n" + sb.toString());
			return new ImageSpec(tof, targetMime, width, height);
		} finally {
			done();
		}
	}

	public ImageSpec convert(ImageConverterHelper h, ImageSpec source, int page, String targetMime) throws Exception {
		//-- Create a scaled image
		start();
		try {
			String ext = findExt(targetMime);
			if(ext == null)
				throw new IllegalArgumentException("The mime type '" + targetMime + "' is not supported");
			File tof = h.createWorkFile(ext);
			OriginalImagePage pi = source.getInfo().getPage(page);

			//-- jal 20100906 Use thumbnail, not resize: resize does not properly filter causing an white image because all black pixels are sized out.
			ProcessBuilder pb = new ProcessBuilder(m_convert.toString(), source.getSource().toString() + "[" + page + "]", "-coalesce", "-quality", "100", tof.toString());
			System.out.println("Command: " + pb.command().toString());
			StringBuilder sb = new StringBuilder(8192);
			int xc = ProcessTools.runProcess(pb, sb);
			System.out.println("convert: " + sb.toString());
			if(xc != 0)
				throw new Exception("External command exception: " + m_convert + " returned error code " + xc + "\n" + sb.toString());
			return new ImageSpec(tof, targetMime, pi.getWidth(), pi.getHeight());
		} finally {
			done();
		}
	}

}
