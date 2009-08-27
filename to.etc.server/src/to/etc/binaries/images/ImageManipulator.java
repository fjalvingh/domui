package to.etc.binaries.images;

import java.io.*;
import java.util.*;

public class ImageManipulator {
	static private ImageHandler	m_instance;

	synchronized public static ImageHandler getImageHandler() {
		if(m_instance == null) {
			m_instance = ImageMagicImageHandler.getInstance();
			if(m_instance == null)
				throw new RuntimeException("No ImageHandler current, and none of the defaults work.");
		}
		return m_instance;
	}

	public List<ImagePage> identify(File input) throws Exception {
		return getImageHandler().identify(input);
	}

	static public ImageDataSource thumbnail(File inf, int page, int w, int h, String mime) throws Exception {
		return getImageHandler().thumbnail(inf, page, w, h, mime);
	}

	static public ImageDataSource scale(File inf, int page, int w, int h, String mime) throws Exception {
		return getImageHandler().scale(inf, page, w, h, mime);
	}

	static public ImageDataSource scale(File inf, int page, int sw, int sh, int w, int h, String mime) throws Exception {
		//        int size = (int) ((long)sw * (long)sh * 32 / (1024*1024));
		return getImageHandler().scale(inf, page, w, h, mime);
	}
}
