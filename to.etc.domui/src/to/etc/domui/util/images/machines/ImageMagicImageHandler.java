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

import javax.annotation.*;

import to.etc.domui.util.images.converters.*;
import to.etc.util.*;
import to.etc.webapp.core.*;

final public class ImageMagicImageHandler implements ImageHandler {
	static public final String PNG = "image/png";

	static public final String JPEG = "image/jpeg";

	static public final String JPG = "image/jpg";

	static public final String GIF = "image/gif";

	/** Locations for ImageMagick, from "unlikely" to "likely". DO NOT CHANGE THIS ORDER - it is important to be able to have a "customized" ImageMagick somewhere - so /usr/bin must be last, not first. */
	static private final String[] UNIXPATHS = {"/opt/imagemagick", "/usr/local/bin", "/usr/bin", "/bin"};

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

	private ImageMagicImageHandler(@Nonnull File ident, @Nonnull File convert, File filecommand) {
		m_convert = convert;
		m_identify = ident;
		m_fileCommand = filecommand;
	}

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

	@Nonnull
	static private String getExt() {
		return onWindows() ? ".exe" : "";
	}

	/**
	 * Initializes and checks to see if ImageMagick is present.
	 * @return
	 */
	static private synchronized void initialize() {
		m_initialized = true;

		//-- 1. First create a path list, so that we can find the "file" command and perhaps later ImageMagick on it.
		//-- Create a path list. Start with hardcoded.
		List<String> pathlist = new ArrayList<String>();
		if(File.separatorChar == '\\') {
			pathlist.addAll(Arrays.asList(WINDOWSPATHS));
		} else {
			pathlist.addAll(Arrays.asList(UNIXPATHS));
		}

		//-- Then append the system path
		String s = System.getenv("PATH");
		if(s != null) {
			String[] list = s.split("\\" + File.pathSeparator);
			if(list != null)
				pathlist.addAll(Arrays.asList(list));
		}

		//-- Locate the Linux 'file' command, if present, on the path
		File filecommand = null;
		for(String path : pathlist) {
			File f = new File(path, "file" + getExt());
			if(f.exists()) {
				filecommand = f;
				break;
			}
		}

		//-- Now try to locate imagemagick.
		s = System.getenv("VP_MAGICK");                     // VP_MAGICK environment variable
		if(!StringTool.isBlank(s)) {
			File f = new File(s);
			if(initMagick(f, filecommand))
				return;
		}

		s = System.getProperty("vp.magick");
		if(!StringTool.isBlank(s)) {
			File f = new File(s);
			if(initMagick(f, filecommand))
				return;
		}

		//-- Locate ImageMagick using paths
		for(String path : pathlist) {
			File base = new File(path);
			if(initMagick(base, filecommand))
				return;
		}
		System.out.println("Error: ImageMagick not found in paths " + pathlist);
	}

	static private synchronized boolean initMagick(@Nonnull File base, @Nullable File filecommand) {
		File convert = new File(base, "convert" + getExt());
		if(convert.exists()) {
			File ident = new File(base, "identify" + getExt());
			if(ident.exists()) {
				ImageMagicImageHandler h = new ImageMagicImageHandler(ident, convert, filecommand);
				m_instance = h;
				System.out.println("ImageMagick: using base=" + base);
				return true;
			}
		}
		return false;
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
				handleIdentifyError(input, xc, sb);
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
			if(list.isEmpty()) {
				handleIdentifyError(input, xc, sb);
			}
			ImageInfo oid = new ImageInfo(mime, typeDescription, true, list);
			return oid;
		} finally {
			//			done();
		}
	}

	private void handleIdentifyError(File imageFile, int xc, StringBuilder output) {
		final StringBuilder errorMessage = new StringBuilder();
		errorMessage.append("Identify failed for file: ");
		errorMessage.append(imageFile.toString());
		errorMessage.append('\n');
		errorMessage.append("Identify exited with code ");
		errorMessage.append(xc);
		errorMessage.append('\n');
		errorMessage.append(output.length() == 0 ? "Identify returned 0 lines." : output.toString());
		throw new IllegalStateException(errorMessage.toString());
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
			//-- rg 20120608 quality 95 is best for for png files
			if(width != 0 && height != 0) {
				String rsz = width + "x" + height;
				pb = new ProcessBuilder(m_convert.toString(), "-density", DENSITY, "-size", rsz, source.getSource().toString() + "[" + page + "]", "-thumbnail", rsz, "-coalesce", "-quality", "95", tof.toString());
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
