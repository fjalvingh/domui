package to.etc.server.servlet.parts;

import java.awt.*;
import java.awt.image.*;
import java.io.*;

import to.etc.server.*;
import to.etc.server.cache.*;
import to.etc.server.vfs.*;
import to.etc.sjit.*;
import to.etc.util.*;

public class PartUtil {
	private PartUtil() {
	}

	static private boolean isa(String name, String ext) {
		int pos = name.lastIndexOf('.');
		if(pos == -1)
			return false;
		return name.substring(pos + 1).equalsIgnoreCase(ext);
	}

	static public BufferedImage loadImage(VfsPathResolver r, String image, DependencySet depset) throws Exception {
		VfsKey key = r.resolvePath(new PathSplitter(image));
		VfsSource src = VFS.getInstance().get(key, depset);
		if(src == null)
			throw new ProgrammerException("The image '" + image + "' was not found.");
		InputStream is = null;
		try {
			BufferedImage bi = null;
			is = src.getInputStream();

			if(isa(image, "gif"))
				bi = ImaTool.loadGIF(is);
			else if(isa(image, "jpg") || isa(image, "jpeg"))
				bi = ImaTool.loadJPEG(is);
			else if(isa(image, "png"))
				bi = ImaTool.loadPNG(is);
			else
				throw new ProgrammerException("The image '" + image + "' must be .gif, .jpg or .jpeg");

			//			System.out.println("size of image is "+xy(m_src_bi.getWidth(), m_src_bi.getHeight()));

			//-- Convert the image to a full-color image
			if(bi.getType() == BufferedImage.TYPE_INT_ARGB)
				return bi;
			BufferedImage newbi = new BufferedImage(bi.getWidth(), bi.getHeight(), BufferedImage.TYPE_INT_ARGB);
			Graphics2D g2d = (Graphics2D) newbi.getGraphics();
			g2d.drawImage(bi, 0, 0, null);
			return newbi;
		} finally {
			try {
				if(is != null)
					is.close();
			} catch(Exception x) {}
		}
	}
}
