package to.etc.domui.util.images.converters;

import java.io.*;
import java.util.*;

final public class ImageConverterRegistry {
	static private List<IImageConverter> m_list = new ArrayList<IImageConverter>();

	static private List<IImageIdentifier> m_identList = new ArrayList<IImageIdentifier>();

	private ImageConverterRegistry() {}

	static synchronized public void registerFactory(IImageConverter c) {
		m_list = new ArrayList<IImageConverter>(m_list); // Copy original
		m_list.add(c); // Append new one
	}

	static synchronized List<IImageConverter> getConverterList() {
		return m_list;
	}

	static synchronized public void registerIdentifier(IImageIdentifier c) {
		m_identList = new ArrayList<IImageIdentifier>(m_identList); // Copy original
		m_identList.add(c); // Append new one
	}

	public static synchronized List<IImageIdentifier> getIdentList() {
		return m_identList;
	}

	/**
	 * Select the best converter to use to handle the (first) conversions in the list.
	 * @param mime
	 * @param convs
	 * @return
	 */
	static public IImageConverter findBestConverter(String mime, List<IImageConversionSpecifier> convs) throws Exception {
		IImageConverter best = null;
		int bestscore = -1;
		for(IImageConverter ic : getConverterList()) {
			int score = ic.accepts(mime, convs);
			if(score > bestscore) {
				bestscore = score;
				best = ic;
			}
		}

		if(best == null)
			throw new IllegalStateException("No image converter known to convert a " + mime + " using " + convs.get(0));
		return best;
	}

	static public ImageData identify(String mime, File src) {
		for(IImageIdentifier ii : getIdentList()) {
			ImageData id = ii.identifyImage(src, mime);
			if(id != null)
				return id;
		}
		return null;
	}

	static {
		BitmapConverter bc = new BitmapConverter();
		registerFactory(bc);
		registerIdentifier(bc);
	}
}
