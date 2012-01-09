package to.etc.binaries.cache;

import java.awt.*;

import to.etc.binaries.images.*;
import to.etc.sjit.*;

public class BinaryImageResizeConverter implements TwoStepBinaryConverter {
	static private String	PNG		= "image/png".intern();

	static private String[]	MIMES	= {"image/jpeg".intern(), "image/jpg".intern(), PNG};

	static private boolean isValidMime(String mime) {
		for(String s : MIMES) {
			if(s == mime)
				return true;
		}
		return false;
	}

	/**
	 * Returns T if this converter can convert.
	 *
	 * @see nl.itris.vp.util.binaries.BinaryConverter#accepts(nl.itris.vp.util.binaries.BinaryInfo, java.lang.String, java.lang.String, int, int)
	 */
	public boolean accepts(BinaryInfo corebi, String type, String mime, int width, int height) {
		//        if(! "raster".equalsIgnoreCase(type))
		//            return false;

		//-- Is the input type acceptable?
		mime = corebi.getMime();
		if(!isValidMime(mime)) // Dunno howto handle?
			return false;
		if(width <= 0 || height <= 0)
			return false;
		return true;
	}

	/**
	 * Calculate the metrics of the resulting image.
	 *
	 * @see nl.itris.vp.util.binaries.TwoStepBinaryConverter#calculate(nl.itris.vp.util.binaries.BinaryRef, java.lang.String, java.lang.String, int, int)
	 */
	public ConverterResult calculate(BinaryInfo corebi, String type, String mime, int w, int h) throws Exception {
		//-- Calculate a new size then return the thing that will be created.
		Dimension d = ImaTool.resizeWithAspect(w, h, corebi.getWidth(), corebi.getHeight());
		return new ConverterResult(type, PNG, d.width, d.height);
	}

	/**
	 * Generate the resized image. If the image is small this uses basic JAI, else it uses the
	 * queued image encoder to prevent OOMs.
	 *
	 * @see nl.itris.vp.util.binaries.TwoStepBinaryConverter#generate(nl.itris.vp.util.binaries.BinaryRef, nl.itris.vp.util.binaries.BinaryInfo)
	 */
	public ImageDataSource generate(BinaryRef source, String type, String mime, int w, int h) throws Exception {
		return ImageManipulator.scale(source.getFile(), 0, w, h, mime);
	}
}
