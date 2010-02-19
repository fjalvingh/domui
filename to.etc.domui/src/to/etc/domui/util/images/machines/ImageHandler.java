package to.etc.domui.util.images.machines;


import java.io.*;

import to.etc.domui.util.images.converters.*;

public interface ImageHandler {
	public ImageInfo identify(File input) throws Exception;

	public ImageSpec scale(ImageConverterHelper h, ImageSpec source, int page, int width, int height, String targetMime) throws Exception;

	public ImageSpec thumbnail(ImageConverterHelper h, ImageSpec source, int page, int width, int height, String targetMime) throws Exception;
}
