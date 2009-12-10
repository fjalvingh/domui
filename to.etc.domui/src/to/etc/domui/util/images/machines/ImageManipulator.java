package to.etc.domui.util.images.machines;

import java.io.*;

public class ImageManipulator {
	static private ImageHandler m_instance;

	synchronized public static ImageHandler getImageHandler() {
		if(m_instance == null) {
			m_instance = ImageMagicImageHandler.getInstance();
			if(m_instance == null)
				throw new RuntimeException("No ImageHandler current, and none of the defaults work.");
		}
		return m_instance;
	}

	static public ImageInfo identify(File input) throws Exception {
		return getImageHandler().identify(input);
	}
}
