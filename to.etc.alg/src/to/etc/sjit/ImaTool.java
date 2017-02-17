/*
 * DomUI Java User Interface - shared code
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
package to.etc.sjit;

import to.etc.util.*;

import javax.annotation.*;
import javax.imageio.*;
import javax.imageio.stream.*;
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;

/**
 * This static utility class has a lot of image-related helper functions.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class ImaTool {
	private ImaTool() {
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Small helper functions..							*/
	/*--------------------------------------------------------------*/
	/**
	 *	Does a resize of an object with size (sw, sh) to fit within a rectangle
	 *  of (dw, dh) while keeping the same aspect ratio
	 */
	static public Dimension resizeWithAspect(int dw, int dh, int sw, int sh) {
		//-- Exit if already at/below the ar


		//-- Calculate Idx / Odx as fx
		double fx = (double) sw / (double) dw;
		double fy = (double) sh / (double) dh;

		double f = fx / fy;
		if(f >= 1.0) {
			//-- Adjust Y
			dh = (int) (sh / fx + 0.5);
		} else {
			dw = (int) (sw / fy + 0.5);
		}
		return new Dimension(dw, dh);
	}

	static public Dimension resizeWithAspect(Dimension dest, Dimension src) {
		//-- Calculate Idx / Odx as fx
		double fx = (double) src.width / (double) dest.width;
		double fy = (double) src.height / (double) dest.height;

		double f = fx / fy;
		if(f >= 1.0) {
			//-- Adjust Y
			return new Dimension(dest.width, (int) (src.height / fx + 0.5));
			//			dh	= (int)(sh / fx);
		} else {
			return new Dimension((int) (src.width / fy + 0.5), dest.height);
			//			dw	= (int)(sw / fy);
		}
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Conversion...										*/
	/*--------------------------------------------------------------*/
	/**
	 *	Converts an Image into a same-size buffered image if it's not already a
	 *  BufferedImage.
	 */
	static public BufferedImage makeBuffered(Image i, int bit) {
		if(i instanceof BufferedImage)
			return (BufferedImage) i;

		//-- Generate a buffered thing.
		int iw = i.getWidth(null);
		int ih = i.getHeight(null);
		BufferedImage bi = new BufferedImage(iw, ih, bit);
		Graphics g = bi.getGraphics();
		g.drawImage(i, 0, 0, null);
		g.dispose();
		return bi;
	}


	/**
	 *	Converts an Image into a same-size buffered image if it's not already a
	 *  BufferedImage.
	 */
	static public BufferedImage makeBuffered(Image i) {
		return makeBuffered(i, BufferedImage.TYPE_INT_RGB);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	PNG loader (temp).									*/
	/*--------------------------------------------------------------*/
	static public BufferedImage loadPNG(InputStream is) throws IOException {
		return ImageIO.read(is);
		//
		//		PngImage	p = new PngImage();
		//		return p.read(is, false);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	JPEG save and load stuff...							*/
	/*--------------------------------------------------------------*/
	/**
	 *	Loads a JPEG image from a stream. The image is returned as a BufferedImage.
	 */
	static public BufferedImage loadJPEG(InputStream is) throws IOException {
		return ImageIO.read(is);
		//
		//		JPEGImageDecoder jd = JPEGCodec.createJPEGDecoder(is);
		//		//		JPEGDecodeParam		jdp	= jd.getJPEGDecodeParam();
		//		BufferedImage bi = jd.decodeAsBufferedImage();
		//		return bi;
	}


	/**
	 *	Loads a JPEG image from a file. The image is returned as a BufferedImage.
	 */
	static public BufferedImage loadJPEG(File f) throws IOException {
		FileInputStream fis = new FileInputStream(f);
		try {
			return loadJPEG(fis);
		} finally {
			try {
				if(fis != null)
					fis.close();
			} catch(Exception x) {}
		}
	}


	/**
	 *	Saves an image as a JPEG to the stream specified, with the quality
	 *  spec'd. If q is zero the default quality will be used.
	 */
	static public void saveJPEG(BufferedImage bi, OutputStream os, double qf) throws IOException {
		//-- Write a JPEG
		ImageIO.write(bi, "JPG", os);
		//
		//		ImageIO.
		//		JPEGImageEncoder je = JPEGCodec.createJPEGEncoder(os);
		//		if(qf != 0.0) {
		//			JPEGEncodeParam ep = je.getDefaultJPEGEncodeParam(bi);
		//			ep.setQuality((float) qf, false);
		//			je.encode(bi, ep);
		//		} else
		//			je.encode(bi);
	}


	/**
	 *	Saves an image as a JPEG to the stream specified, with the quality
	 *  spec'd. If q is zero the default quality will be used.
	 */
	static public void saveJPEG(BufferedImage bi, File f, double qf) throws IOException {
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(f);
			saveJPEG(bi, fos, qf);

		} finally {
			try {
				if(fos != null)
					fos.close();
			} catch(Exception x) {}

		}
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Loading/saving GIF format files (no animation).		*/
	/*--------------------------------------------------------------*/
	/**
	 *	Load a GIF format image.
	 */
	static public BufferedImage loadGIF(File f, int buffertype) throws IOException {
		InputStream is = new FileInputStream(f);
		try {
			return loadGIF(is, buffertype);

		} finally {
			try {
				is.close();
			} catch(Exception x) {}
		}
	}


	/**
	 *	Load a GIF format image.
	 */
	static public BufferedImage loadGIF(InputStream is, int buffertype) throws IOException {
		AnimGifDecoder ade = new AnimGifDecoder(is);
		BufferedImage bi = ade.read(buffertype);
		return bi;
	}


	/**
	 *	Load a GIF format image using the BYTE_TYPE_INDEXED format (fastest)
	 */
	static public BufferedImage loadGIF(InputStream is) throws IOException {
		return loadGIF(is, BufferedImage.TYPE_BYTE_INDEXED);
	}

	/**
	 *	Load a GIF format image using the BYTE_TYPE_INDEXED format (fastest)
	 */
	static public BufferedImage loadGIF(File f) throws IOException {
		return loadGIF(f, BufferedImage.TYPE_BYTE_INDEXED);
	}

	/**
	 *	Saves an image as a GIF file to a stream.
	 */
	static public void saveGIF(BufferedImage bi, OutputStream os) throws Exception {
		AnimGifEncoder age = new AnimGifEncoder(os);
		age.add(bi);
		age.encode();
		age.flush();
	}

	/**
	 *	Saves an image as a GIF file to a stream.
	 */
	static public void saveGIF(BufferedImage bi, File f) throws Exception {
		FileOutputStream fos = new FileOutputStream(f);
		try {
			saveGIF(bi, fos);
		} finally {
			try {
				fos.close();
			} catch(Exception x) {}
		}
	}

	static public void savePNG(BufferedImage bi, File f) throws Exception {
		ImageIO.write(bi, "PNG", f);
	}

	static public void savePNG(BufferedImage bi, OutputStream f) throws Exception {
		ImageIO.write(bi, "PNG", f);
	}

	/*--------------------------------------------------------------*/
	/*      CODING: Loading several formats from stream/file...                     */
	/*--------------------------------------------------------------*/
	/**
	 *	Loads an image from a file. If the extension of the file is known (either
	 *  gif, jpg or jpeg) then the file is loaded using one of the optimized
	 *  loaders; if not the routine returns null.
	 */
	static public BufferedImage loadFile(File f) throws IOException {
		String ext = FileTool.getFileExtension(f.toString());
		if(ext.length() < 3)
			return null;

		//-- try using extension
		try {
			if(ext.equalsIgnoreCase("gif"))
				return loadGIF(f);
			else if(ext.equalsIgnoreCase("jpg") || ext.equalsIgnoreCase("jpeg"))
				return loadJPEG(f);
		} catch(Exception x) {}

		//-- Try to load as JPEG
		try {
			return loadJPEG(f);
		} catch(Exception x) {}

		try {
			return loadGIF(f);
		} catch(Exception x) {}

		//-- Still no go! Load using default code...
		return awtLoadBufferedImage(f);
	}

	/**
	 *	Loads an image from a file. If the extension of the file is known (either
	 *  gif, jpg or jpeg) then the file is loaded using one of the optimized
	 *  loaders; if not the routine returns null.
	 */
	static public Image loadFile_image(File f) throws IOException {
		String ext = FileTool.getFileExtension(f.toString());
		if(ext.length() < 3)
			return null;

		//-- try using extension
		try {
			if(ext.equalsIgnoreCase("gif"))
				return loadGIF(f);
			else if(ext.equalsIgnoreCase("jpg") || ext.equalsIgnoreCase("jpeg"))
				return loadJPEG(f);
		} catch(Exception x) {}

		//-- Try to load as JPEG
		try {
			return loadJPEG(f);
		} catch(Exception x) {}

		try {
			return loadGIF(f);
		} catch(Exception x) {}

		//-- Still no go! Load using default code...
		return awtLoadImage(f);
	}


	/**
	 *	Loads an image from a stream. The image type must contain jpg or jpeg
	 *  for a JPEG file, or gif for a GIF file, or png for a PNG file.
	 */
	static public BufferedImage loadStream(InputStream is, String type) throws IOException {
		type = type.toLowerCase();
		if(type.indexOf("jpeg") != -1 || type.indexOf("jpg") != -1)
			return loadJPEG(is);
		else if(type.indexOf("gif") != -1)
			return loadGIF(is);
		else if(type.indexOf("png") != -1)
			return loadPNG(is);
		return null;
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	AWT Stuff...										*/
	/*--------------------------------------------------------------*/
	/// The current frame which appears to be necessary,,, Bah.
	static private Frame	m_frame;

	/**
	 *	Returns the frame. It gets allocated if it didn't exist.
	 */
	static private Frame getFrame() {
		if(m_frame == null)
			m_frame = new Frame();
		return m_frame;
	}


	/**
	 *	Loads the image from the file into an image. This loads exactly according
	 *  to the AWT, without using any of the extensions. It will return a
	 *  completely loaded image however.
	 */
	static public Image awtLoadImage(File f) throws IOException {
		try {
			MediaTracker mt = new MediaTracker(getFrame());
			Image ima = Toolkit.getDefaultToolkit().createImage(f.toString());
			//			Image	ima	= m_t.getImage(f.toString());
			mt.addImage(ima, 0);
			mt.waitForAll();
			Image r = ima;
			if(mt.isErrorAny())
				r = null;
			mt.removeImage(ima, 0);
			return r;
		} catch(InterruptedException x) {
			throw new IOException("Interrupted load");
		}
	}


	/**
	 *	Loads the image from the file into an image. This loads exactly according
	 *  to the AWT, without using any of the extensions. After the load the
	 *  image is copied into a BufferedImage and returned. This is VERY expensive
	 *  so try to use another function instead!
	 */
	static public BufferedImage awtLoadBufferedImage(File f) throws IOException {
		return makeBuffered(awtLoadImage(f));
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Resizing an image in all the myriad ways..			*/
	/*--------------------------------------------------------------*/
	static public BufferedImage resizeFiltered_RAW(BufferedImage bi, int w, int h, ResamplerFilter fil) {
		//-- Now reraster using an optimized method...
		BufferedImage obi = ImageSubsampler.resample(bi, fil, w, h);
		return obi;
	}


	static public BufferedImage resizeFiltered_RAW(BufferedImage i, int w, int h) {
		return resizeFiltered(i, w, h, new Lanczos3Filter());
	}


	/**
	 *	Resizes a BufferedImage while keeping it's aspect ratio, using the
	 *  optimal filtered stuff... This uses the default filter..
	 */
	static public BufferedImage resizeFiltered(BufferedImage bi, int w, int h, ResamplerFilter fil) {
		//-- Make this a BufferedImage if it is not one already
		int iw = bi.getWidth();
		int ih = bi.getHeight();
		java.awt.Dimension p = ImaTool.resizeWithAspect(w, h, iw, ih); // Get new size, obeying aspect ratio of source

		//-- Now reraster using an optimized method...
		BufferedImage obi = ImageSubsampler.resample(bi, fil, p.width, p.height);
		return obi;
	}


	/**
	 *	Resizes a BufferedImage while keeping it's aspect ratio, using the
	 *  optimal filtered stuff... This uses the default filter..
	 */
	static public BufferedImage resizeFiltered(BufferedImage i, int w, int h) {
		return resizeFiltered(i, w, h, new Lanczos3Filter());
	}


	/**
	 *	Resizes a BufferedImage while keeping it's aspect ratio, using the
	 *  AWT mechanism... This returns a lower quality image; use resizeFiltered
	 *  to get a filtered resampled image.
	 */
	static public BufferedImage awtResize(BufferedImage bi, int w, int h) {
		int iw = bi.getWidth();
		int ih = bi.getHeight();
		java.awt.Dimension p = ImaTool.resizeWithAspect(w, h, iw, ih); // Get new size, obeying aspect ratio of source
		BufferedImage obi = new BufferedImage(p.width, p.height, bi.getType());
		Graphics2D g = (Graphics2D) obi.getGraphics();
		g.drawImage(bi, 0, 0, null);
		g.dispose();
		return obi;
	}


	/**
	 *	Taking a BufferedImage, this function will try to find a JPEG
	 *  quality factor that will save the image with a size around the size
	 *  specified. This is done by looping a number of times and adjusting
	 *  the quality until something reasonable is obtained.
	 */
	static public double findQualityBySize(BufferedImage bi, int sizenear) {
		double lowq = 0.2; // Lowest qual
		double hiq = 1.0; // Highest qual
		double q = 1.0; // First quality try
		int iter = 0; // Size-increasing iterations
		int aiter = 0; // Actual iterations.
		int esz = sizenear + (sizenear / 10);
		int bsz = sizenear - (sizenear / 10);
		int sz = 0;

		NullOutputStream nos = new NullOutputStream();

		/*
		 *	Loop: encode an image until the size is reasonable...
		 */
		while(iter < 5 && aiter < 10) {
			aiter++;
			nos.reset(); // Reset current output size,
			try {
				ImaTool.saveJPEG(bi, nos, q); // Save to dummy stream,
			} catch(Exception x) {
				x.printStackTrace();
			}
			sz = (int) nos.getSzWritten(); // And get size generated;

			//			System.out.println("findQualityBySize(): try with Q="+q+" result="+sz+" bytes");

			if(sz > esz) {
				//-- Output is WAY TOO BIG: another iteration needed ALWAYS.
				hiq = q;
				q = (lowq + hiq) / 2.0; // Get new quality,
				if(q == hiq)
					break;

				if(q >= hiq)
					q = q / 2; // ..safety..
			} else if(sz < bsz) // Too small...
			{
				//-- Too small. Increment iteration thingy.
				iter++;
				if(q >= 0.90)
					break; // Already HIGH quality so use the small size,
				lowq = q;

				q = (hiq + lowq) / 2.0;
			} else
				break;
		}

		//		System.out.println("findQualityBySize(): "+sz+" bytes in "+aiter+" iterations");
		return q;
	}

	static public void saveImageByMime(OutputStream os, BufferedImage bi, String mime) throws Exception {
		if("image/jpeg".equals(mime) || "image/jpg".equals(mime))
			saveJPEG(bi, os, 0.6);
		else if("image/gif".equals(mime))
			saveGIF(bi, os);
		else if("image/png".equals(mime))
			savePNG(bi, os);
		else
			throw new IllegalStateException("Unsupported mime type for save: " + mime);
	}

	static public Dimension getImageDimension(File resourceFile) throws IOException {
		ImageInputStream in = ImageIO.createImageInputStream(resourceFile);
		return getDimension(in);
	}

	@Nullable
	static public Dimension getImageDimension(@Nonnull InputStream is) throws IOException {
		try(ImageInputStream in = ImageIO.createImageInputStream(is)) {
			return getDimension(in);
		}
	}

	@Nullable
	private static Dimension getDimension(ImageInputStream in) throws IOException {
		final Iterator<ImageReader> readers = ImageIO.getImageReaders(in);
		if(readers.hasNext()) {
			ImageReader reader = readers.next();
			try {
				reader.setInput(in);
				return new Dimension(reader.getWidth(0), reader.getHeight(0));
			} finally {
				reader.dispose();
			}
		}
		return null;
	}

	public static void main(String[] args) {
		System.out.println("resize=" + resizeWithAspect(100, 100, 500, 391));
		System.out.println("resize2=" + resizeWithAspect(100, 78, 500, 391));
	}
}
