package to.etc.domui.util.images.machines;


import java.io.*;
import java.util.*;

import to.etc.domui.util.images.converters.*;

public interface ImageHandler {
	public List<ImagePage> identify(File input) throws Exception;

	public ImageSpec scale(ImageConverterHelper h, ImageSpec source, int page, int width, int height, String targetMime) throws Exception;

	public ImageSpec thumbnail(ImageConverterHelper h, ImageSpec source, int page, int width, int height, String targetMime) throws Exception;
}
