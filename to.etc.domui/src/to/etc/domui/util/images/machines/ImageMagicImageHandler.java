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

	static private boolean m_initialized;

	static private ImageMagicImageHandler m_instance;

	private File m_convert;

	private File m_identify;

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

	/**
	 * Initializes and checks to see if ImageMagick is present.
	 * @return
	 */
	static private synchronized void initialize() {
		m_initialized = true;
		String ext = "";
		String[] paths;
		if(File.separatorChar == '\\') {
			paths = WINDOWSPATHS;
			ext = ".exe";
		} else {
			paths = UNIXPATHS;
			ext = "";
		}
		ImageMagicImageHandler m = new ImageMagicImageHandler();
		for(String s : paths) {
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
	public ImageInfo identify(File input) throws Exception {
		//		start();
		try {
			//-- Start 'identify' and capture the resulting data
			ProcessBuilder pb = new ProcessBuilder(m_identify.toString(), "-ping", input.toString());
			StringBuilder sb = new StringBuilder(8192);
			int xc = ProcessTools.runProcess(pb, sb);
			if(xc != 0)
				throw new Exception("External command exception: " + m_identify + " returned error code " + xc + "\n" + sb.toString());

			System.out.println("identify: result=" + sb.toString());
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
			ImageInfo oid = new ImageInfo(mime, list);
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
	public ImageSpec thumbnail(ImageConverterHelper h, ImageSpec source, int page, int width, int height, String targetMime) throws Exception {
		//-- Create a thumb.
		start();
		try {
			//-- Create an extension for the target mime type,
			String ext = findExt(targetMime);
			if(ext == null)
				throw new IllegalArgumentException("The mime type '" + targetMime + "' is not supported");
			File tof = h.createWorkFile(ext);
			ProcessBuilder pb = new ProcessBuilder(m_convert.toString(), source.getSource().toString() + "[" + page + "]", "-thumbnail", width + "x" + height, tof.toString());
			//			System.out.println("Command: " + pb.toString());
			StringBuilder sb = new StringBuilder(8192);
			int xc = ProcessTools.runProcess(pb, sb);
			System.out.println("convert: " + sb.toString());
			if(xc != 0)
				throw new Exception("External command exception: " + m_convert + " returned error code " + xc + "\n" + sb.toString());
			return new ImageSpec(tof, targetMime, width, height);
		} finally {
			done();
		}
	}

	public ImageSpec scale(ImageConverterHelper h, ImageSpec source, int page, int width, int height, String targetMime) throws Exception {
		//-- Create a scaled image
		start();
		try {
			String ext = findExt(targetMime);
			if(ext == null)
				throw new IllegalArgumentException("The mime type '" + targetMime + "' is not supported");
			File tof = h.createWorkFile(ext);

			//-- Start 'identify' and capture the resulting data.
			ProcessBuilder pb = new ProcessBuilder(m_convert.toString(), "-resize", width + "x" + height, source.getSource().toString() + "[" + page + "]", "-strip", tof.toString());
			//			System.out.println("Command: " + pb.toString());
			StringBuilder sb = new StringBuilder(8192);
			int xc = ProcessTools.runProcess(pb, sb);
			System.out.println("convert: " + sb.toString());
			if(xc != 0)
				throw new Exception("External command exception: " + m_convert + " returned error code " + xc + "\n" + sb.toString());
			return new ImageSpec(tof, targetMime, width, height);
		} finally {
			done();
		}
	}
}
