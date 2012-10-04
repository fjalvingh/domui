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
package to.etc.domui.util.images.converters;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;

import to.etc.domui.util.images.machines.*;
import to.etc.sjit.*;

public class BitmapConverter implements IImageConverter, IImageIdentifier {
	static private final Set<String> MIMES = new HashSet<String>();

	static {
		// commented for now (K+v)
		//		MIMES.add("text/html"); // html => html2ps is needed
		MIMES.add("image/html");
		MIMES.add("image/plain");
		MIMES.add("image/gif");
		MIMES.add("image/png");
		MIMES.add("image/tiff");
		MIMES.add("image/jpg");
		MIMES.add("image/jpeg");
		MIMES.add("image/bmp");
		MIMES.add("application/pdf");
		//		MIMES.add("image/");
		//		MIMES.add("image/");

	}

	@Override
	public int accepts(String inputmime, List<IImageConversionSpecifier> conversions) throws Exception {
		if(!MIMES.contains(inputmime))
			return -1;

		//-- Check for supported converters.
		for(IImageConversionSpecifier ics : conversions) {
			if(!(ics instanceof ImagePageSelect) && !(ics instanceof ImageResize) && !(ics instanceof ImageConvert))
				return -1;
		}
		return 2;
	}

	@Override
	public void convert(ImageConverterHelper helper, List<IImageConversionSpecifier> convs) throws Exception {
		String targetMime = null;
		int sourcePage = 0;
		ImageResize resize = null;

		//-- Extract all operations I can do something with.
		while(convs.size() > 0) {
			IImageConversionSpecifier ics = convs.get(0);
			if(ics instanceof ImagePageSelect) {
				convs.remove(0);
				ImagePageSelect ips = (ImagePageSelect) ics;
				sourcePage = ips.getPageNumber();
			} else if(ics instanceof ImageConvert) {
				convs.remove(0);
				ImageConvert c = (ImageConvert) ics;
				targetMime = c.getTargetMime();
			} else if(ics instanceof ImageResize) {
				convs.remove(0);
				resize = (ImageResize) ics;
				if(resize.getTargetMime() != null)
					targetMime = resize.getTargetMime();
			} else
				break;
		}
		//		if(resize == null)
		//			throw new IllegalStateException("Not acceptable (not a resize) after accept() accepted the work??");

		//-- Calculate the proper width and height, respecting the aspect ratio of the source
		boolean	multipage = helper.getSource().getInfo().getPageCount() > 1;
		OriginalImagePage ip = helper.getSource().getInfo().getPage(sourcePage);

		if(targetMime == null) {
			if(helper.getSource().getMime().equals("image/jpeg") || helper.getSource().getMime().equals("image/jpg"))
				targetMime = "image/jpeg";
			else
				targetMime = "image/png";
		}
		ImageHandler ih = ImageManipulator.getImageHandler();
		ImageSpec tis;

		//-- If we resize: do all at once in a single operation
		if(null != resize) {
			Dimension d = ImaTool.resizeWithAspect(resize.getWidth(), resize.getHeight(), ip.getWidth(), ip.getHeight());
			if(resize instanceof ImageThumbnail)
				tis = ih.thumbnail(helper, helper.getSource(), sourcePage, d.width, d.height, targetMime);
			else
				tis = ih.scale(helper, helper.getSource(), sourcePage, d.width, d.height, targetMime);
		} else if(multipage || !targetMime.equals(helper.getSource().getMime())) {
			tis = ih.convert(helper, helper.getSource(), sourcePage, targetMime);
		} else
			tis = helper.getSource();

		helper.setTarget(tis);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Identify an image's characteristics					*/
	/*--------------------------------------------------------------*/
	/**
	 *
	 * @see to.etc.domui.util.images.converters.IImageIdentifier#identifyImage(java.io.File, java.lang.String)
	 */
	@Override
	public ImageInfo identifyImage(File src, String mime) {
		//-- Ask ImageMagick...
		ImageHandler ih = ImageManipulator.getImageHandler();
		try {
			return ih.identify(src); // Try to identify
		} catch(Exception x) {
			return null;
		}
	}
}
