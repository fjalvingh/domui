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

import java.awt.*;
import java.awt.image.*;
import java.io.*;


/**
 *	<p>Subsamples an image to create a smaller (or larger(!)) image. This can use
 *  several sampling filters.</p>
 *
 *	<h2>Credits:</h2>
 *	<p>The algorithms and methods used in this library are based on the article
 *  "General Filtered Image Rescaling" by Dale Schumacher which appeared in the
 *  book Graphics Gems III, published by Academic Press, Inc.</p>
 *
 * 	<p>Performance enhancements: (all in milliseconds)
 * 	<table>
 *  	<tr><td>One</td><td>Ten</td><td>What was done?</td></tr>
 *      <tr><td>2047</td><td> 19,812 </td><td>Initial, slow version sans opt</td></tr>
 *      <tr><td>765</td><td> 6,719</td><td>Using direct-access to BufferedImage's INT RBG buffer for the row sampler only</td></tr>
 *      <tr><td>406</td><td>3,531</td><td>Using direct-access for the COLUMN resampler..</td></tr>
 *      <tr><td>  </td><td>  </td><td>  </td></tr>
 *      <tr><td>  </td><td>  </td><td>  </td></tr>
 * 	</table>
 *	</p>
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class ImageSubsampler {
	private ImageSubsampler() {
	}

	static private final int	IMAT	= BufferedImage.TYPE_INT_RGB;


	/*--------------------------------------------------------------*/
	/*	CODING:	BufferedImage support routines...					*/
	/*--------------------------------------------------------------*/
	//	static private int getBiOffset(Raster ras, PixelInterleavedSampleModel sm, int x, int y)
	//	{
	//		return (y-ras.getSampleModelTranslateY()) * sm.getScanlineStride() + x-ras.getSampleModelTranslateX();
	//	}

	static private int getBiOffset(Raster ras, SinglePixelPackedSampleModel sm, int x, int y) {
		return (y - ras.getSampleModelTranslateY()) * sm.getScanlineStride() + x - ras.getSampleModelTranslateX();
	}


	/**
	 *	precalculates the filter contributions table for a row.
	 */
	static private ContribList[] getRowContrib(ResamplerFilter f, int sw, int ow) {
		float fwidth = f.getWidth();
		float xscale = (float) (ow - 1) / (float) (sw - 1);

		ContribList[] contrib = new ContribList[ow]; // Contributors for EACH pixel in the line

		if(xscale < 1.0f) {
			//-- Horizontal sub-sampling: scaling from bigger to smaller width
			float width = fwidth / xscale;
			float fscale = 1.0f / xscale;

			for(int i = 0; i < ow; i++) {
				ContribList cl = new ContribList((int) (width * 2.0f + 1));
				contrib[i] = cl;

				float center = i / xscale;
				int left = (int) Math.floor(center - width);
				int right = (int) Math.ceil(center + width);
				int n;
				for(int j = left; j <= right; j++) {
					float weight = f.filter((center - j) / fscale) / fscale;
					if(weight != 0.0f) {
						if(j < 0)
							n = -j;
						else if(j >= sw)
							n = sw - j + sw - 1; // ??
						else
							n = j;

						int k = cl.m_n++;
						cl.m_ar[k].m_pixel = n;
						cl.m_ar[k].m_weight = weight;
					}
				}
			}
		} else {
			//-- Horizontal super-sampling: scaling from smaller to larger width
			for(int i = 0; i < ow; i++) {
				ContribList cl = new ContribList((int) (fwidth * 2.0f + 1f));
				contrib[i] = cl;
				float center = i / xscale;
				int left = (int) Math.floor(center - fwidth);
				int right = (int) Math.ceil(center + fwidth);
				int n;
				for(int j = left; j <= right; j++) {
					float weight = f.filter(center - j);
					if(weight != 0.0f) {
						if(j < 0)
							n = -j;
						else if(j >= sw)
							n = sw - j + sw - 1; // ??
						else
							n = j;

						int k = cl.m_n++;
						cl.m_ar[k].m_pixel = n;
						cl.m_ar[k].m_weight = weight;
					}
				}
			}
		}

		return contrib;
	}


	/**
	 *	Gets the filter contributions table for a column.
	 */
	static private ContribList[] getColContrib(ResamplerFilter f, int sh, int oh) {
		float fwidth = f.getWidth();
		float yscale = (float) (oh - 1) / (float) (sh - 1);

		ContribList[] contrib = new ContribList[oh];
		if(yscale < 1.0f) {
			//-- Vertical subsampling: from BIG to small picture
			float fscale = 1.0f / yscale;
			float width = fwidth / yscale;
			for(int i = 0; i < oh; i++) {
				ContribList cl = new ContribList((int) (width * 2.0 + 1));
				contrib[i] = cl;

				float center = i / yscale;
				int loy = (int) Math.floor(center - width);
				int hiy = (int) Math.ceil(center + width);
				int n;
				for(int j = loy; j <= hiy; j++) {
					float weight = f.filter((center - j) / fscale) / fscale;
					if(weight != 0.0f) {
						if(j < 0)
							n = -j;
						else if(j >= sh)
							n = sh - j + sh - 1;
						else
							n = j;

						int k = cl.m_n++; // Pixel count,
						cl.m_ar[k].m_pixel = n; // Source pixel
						cl.m_ar[k].m_weight = weight;
					}
				}
			}
		} else {
			//-- Vertical supersampling: from small to BIGGER picture
			for(int i = 0; i < oh; i++) {
				ContribList cl = new ContribList((int) (fwidth * 2.0 + 1));
				contrib[i] = cl;
				float center = i / yscale;
				int loy = (int) Math.floor(center - fwidth);
				int hiy = (int) Math.ceil(center + fwidth);
				int n;
				for(int j = loy; j <= hiy; j++) {
					float weight = f.filter(center - j);
					if(weight != 0.0f) {
						if(j < 0)
							n = -j;
						else if(j >= sh)
							n = sh - j + sh - 1;
						else
							n = j;
						int k = cl.m_n++;
						cl.m_ar[k].m_pixel = n;
						cl.m_ar[k].m_weight = weight;
					}
				}
			}
		}
		return contrib;
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Row sampling for different models...				*/
	/*--------------------------------------------------------------*/
	/**
	 *	Resamples ROWS to a WORK image of type RGB, where the source is also
	 *  an RGB type. This uses the BufferedImage's DataBuffer directly to
	 *  gain speed. Lots of speed.
	 */
	static private BufferedImage resampleRowRGB(BufferedImage srci, ContribList[] contrib, int sw, int sh, int ow, int oh) {
		//-- Get the source's colormodel, the raster, the databuffer and the samplemodel
		//		ColorModel	cm	= srci.getColorModel();
		Raster sras = srci.getRaster();
		SampleModel stsm = sras.getSampleModel();
		if(!(stsm instanceof SinglePixelPackedSampleModel))
			throw new IllegalStateException("?? TYPE_INT_RGB doesn't have SinglePixelPackedSampleModel??");
		SinglePixelPackedSampleModel ssm = (SinglePixelPackedSampleModel) stsm;

		DataBuffer sdbt = sras.getDataBuffer();
		if(sdbt.getDataType() != DataBuffer.TYPE_INT)
			throw new IllegalStateException("?? TYPE_INT_RGB doesn't have TYPE_INT databuffer!?");
		if(sdbt.getNumBanks() != 1)
			throw new IllegalStateException("?? TYPE_INT_RGB having != 1 banks!?");
		DataBufferInt sdb = (DataBufferInt) sdbt;

		//-- Create destination image and get IT's databuffer and sjtuffh
		BufferedImage worki = new BufferedImage(ow, sh, BufferedImage.TYPE_INT_RGB);
		Raster dras = worki.getRaster();
		SampleModel dtsm = dras.getSampleModel();
		if(!(dtsm instanceof SinglePixelPackedSampleModel))
			throw new IllegalStateException("?? TYPE_INT_RGB doesn't have SinglePixelPackedSampleModel??");
		SinglePixelPackedSampleModel dsm = (SinglePixelPackedSampleModel) dtsm;

		DataBuffer ddbt = dras.getDataBuffer();
		if(ddbt.getDataType() != DataBuffer.TYPE_INT)
			throw new IllegalStateException("?? TYPE_INT_RGB doesn't have TYPE_INT databuffer!?");
		if(ddbt.getNumBanks() != 1)
			throw new IllegalStateException("?? TYPE_INT_RGB having != 1 banks!?");
		DataBufferInt ddb = (DataBufferInt) ddbt;

		//-- Create intermediate image to hold horizontal zoom,
		//		System.out.println("DBG: Using resampleRowRGB");

		//-- Prepare source variables to traverse the integer array
		int soff = getBiOffset(sras, ssm, 0, 0);
		int[] spx = sdb.getData(0); // Get the pixelset,
		int sofe = getBiOffset(sras, ssm, sw - 1, sh - 1); // calc end offset,
		int siw = ssm.getScanlineStride(); // Increment width = databuf's width

		//-- And destination variables...
		int doff = getBiOffset(dras, dsm, 0, 0); // Output table offset
		int[] dpx = ddb.getData(0); // Get the pixelset,
		int diw = dsm.getScanlineStride(); // Increment width = databuf's width

		//-- For each SOURCE input line,
		while(soff <= sofe) {
			//-- Now, soff is the start offset for source pixels on this line..
			for(int i = 0; i < ow; i++) // DEST width,
			{
				float r = 0.0f;
				float g = 0.0f;
				float b = 0.0f;
				ContribList cl = contrib[i];

				// Handle all contributing pixels for this destination pixel
				for(int j = 0; j < cl.m_n; j++) {
					int argb = spx[soff + cl.m_ar[j].m_pixel];
					float weight = cl.m_ar[j].m_weight;
					if(weight != 0.0f) {
						r += weight * ((argb >> 16) & 0xff);
						g += weight * ((argb >> 8) & 0xff);
						b += weight * (argb & 0xff);
					}
				}

				//-- Make a proper RGB pair,
				int br, bg, bb;

				if(r > 255f)
					br = 255;
				else if(r < 0f)
					br = 0;
				else
					br = (int) r;

				if(g > 255f)
					bg = 255;
				else if(g < 0f)
					bg = 0;
				else
					bg = (int) g;

				if(b > 255f)
					bb = 255;
				else if(b < 0f)
					bb = 0;
				else
					bb = (int) b;

				//-- Now set a new pixel in working image,
				dpx[doff++] = (br << 16) | (bg << 8) | bb;
			}

			//-- Proceed to the next line.
			soff += siw; // Source increment width
			doff += diw - ow;
		}

		return worki;
	}


	/**
	 *	Resamples ROWS by using the default getRGB() method. This is SLOW but
	 *  certainly works...
	 */
	static private BufferedImage resampleRowGeneric(BufferedImage srci, ContribList[] contrib, int sw, int sh, int ow, int oh) {
		//-- Create intermediate image to hold horizontal zoom,
		BufferedImage worki = new BufferedImage(ow, sh, BufferedImage.TYPE_INT_ARGB);

		//-- Now: apply filter to sample horizontally from SRC to WORK
		for(int k = 0; k < sh; k++) // SOURCE height (!)
		{
			for(int i = 0; i < ow; i++) // DEST width,
			{
				float a = 0.0f;
				float r = 0.0f;
				float g = 0.0f;
				float b = 0.0f;
				ContribList cl = contrib[i];

				// Handle all contributing pixels for this destination pixel
				for(int j = 0; j < cl.m_n; j++) {
					int argb = srci.getRGB(cl.m_ar[j].m_pixel, k);
					float weight = cl.m_ar[j].m_weight;
					if(weight != 0.0f) {
						r += weight * ((argb >> 16) & 0xff);
						g += weight * ((argb >> 8) & 0xff);
						b += weight * (argb & 0xff);
						a += weight * ((argb >> 24) & 0xff);
					}
				}

				//-- Make a proper RGB pair,
				int br, bg, bb, ba;

				if(a > 255f)
					ba = 255;
				else if(a < 0f)
					ba = 0;
				else
					ba = (int) a;

				if(r > 255f)
					br = 255;
				else if(r < 0f)
					br = 0;
				else
					br = (int) r;

				if(g > 255f)
					bg = 255;
				else if(g < 0f)
					bg = 0;
				else
					bg = (int) g;

				if(b > 255f)
					bb = 255;
				else if(b < 0f)
					bb = 0;
				else
					bb = (int) b;

				//-- Now set a new pixel in working image,
				//				if(k >= 100)
				//					System.out.println("At line 100!");
				worki.setRGB(i, k, (ba << 24) | (br << 16) | (bg << 8) | bb);
			}
			//			System.out.println("Line "+k);
		}

		return worki;
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Column resampling routines....						*/
	/*--------------------------------------------------------------*/
	/**
	 *	Resamples COLUMNS by using the default getRGB() method. This is SLOW but
	 *  certainly works...
	 */
	//	static private BufferedImage resampleColGeneric(BufferedImage worki, ContribList[] contrib, int sw, int sh, int ow, int oh)
	//	{
	//		BufferedImage	obi	= new BufferedImage(ow, oh, BufferedImage.TYPE_INT_RGB);
	//
	//		for(int k = 0; k < ow; k++)					// DEST width
	//		{
	//			for(int i = 0; i < oh; i++)				// DEST height
	//			{
	//				float	r	= 0.0f;
	//				float	g	= 0.0f;
	//				float	b	= 0.0f;
	//				ContribList	cl	= contrib[i];
	//
	//				// Handle all contributing pixels for this destination pixel
	//				for(int j = 0; j < cl.m_n; j++)
	//				{
	//					int	argb	= worki.getRGB(k, cl.m_ar[j].m_pixel);
	//					float	weight = cl.m_ar[j].m_weight;
	//					if(weight != 0.0f)
	//					{
	//						r	+= weight * ((argb >> 16) & 0xff);
	//						g	+= weight * ((argb >> 8) & 0xff);
	//						b	+= weight * (argb & 0xff);
	//					}
	//				}
	//
	//				//-- Make a proper RGB pair,
	//				int	br, bg, bb;
	//
	//				if(r > 255f)
	//					br	= 255;
	//				else if(r < 0f)
	//					br	= 0;
	//				else
	//					br	= (int)r;
	//
	//				if(g > 255f)
	//					bg	= 255;
	//				else if(g < 0f)
	//					bg	= 0;
	//				else
	//					bg	= (int) g;
	//
	//				if(b > 255f)
	//					bb	= 255;
	//				else if(b < 0f)
	//					bb	= 0;
	//				else
	//					bb	= (int)b;
	//
	//				//-- Now set a new pixel in working image,
	//				obi.setRGB(k, i, (br << 16) | (bg << 8) | bb);
	//			}
	//		}
	//		return obi;
	//	}


	/**
	 *	Resamples COLUMNS by using the default getRGB() method. This is SLOW but
	 *  certainly works...
	 */
	static private BufferedImage resampleColRGB(BufferedImage srci, ContribList[] contrib, int sw, int sh, int ow, int oh) {
		//-- Get the source's colormodel, the raster, the databuffer and the samplemodel
		//		ColorModel	cm	= srci.getColorModel();
		Raster sras = srci.getRaster();
		SampleModel stsm = sras.getSampleModel();
		if(!(stsm instanceof SinglePixelPackedSampleModel))
			throw new IllegalStateException("?? TYPE_INT_RGB doesn't have SinglePixelPackedSampleModel??");
		SinglePixelPackedSampleModel ssm = (SinglePixelPackedSampleModel) stsm;

		DataBuffer sdbt = sras.getDataBuffer();
		if(sdbt.getDataType() != DataBuffer.TYPE_INT)
			throw new IllegalStateException("?? TYPE_INT_RGB doesn't have TYPE_INT databuffer!?");
		if(sdbt.getNumBanks() != 1)
			throw new IllegalStateException("?? TYPE_INT_RGB having != 1 banks!?");
		DataBufferInt sdb = (DataBufferInt) sdbt;

		//-- Create destination image and get IT's databuffer and sjtuffh
		BufferedImage worki = new BufferedImage(ow, oh, BufferedImage.TYPE_INT_RGB);
		Raster dras = worki.getRaster();
		SampleModel dtsm = dras.getSampleModel();
		if(!(dtsm instanceof SinglePixelPackedSampleModel))
			throw new IllegalStateException("?? TYPE_INT_RGB doesn't have SinglePixelPackedSampleModel??");
		SinglePixelPackedSampleModel dsm = (SinglePixelPackedSampleModel) dtsm;

		DataBuffer ddbt = dras.getDataBuffer();
		if(ddbt.getDataType() != DataBuffer.TYPE_INT)
			throw new IllegalStateException("?? TYPE_INT_RGB doesn't have TYPE_INT databuffer!?");
		if(ddbt.getNumBanks() != 1)
			throw new IllegalStateException("?? TYPE_INT_RGB having != 1 banks!?");
		DataBufferInt ddb = (DataBufferInt) ddbt;

		//-- Create intermediate image to hold horizontal zoom,
		//		System.out.println("DBG: Using resampleRowRGB");

		//-- Prepare source variables to traverse the integer array
		int soff = getBiOffset(sras, ssm, 0, 0);
		int[] spx = sdb.getData(0); // Get the pixelset,
		//		int		sofe	= getBiOffset(sras, ssm, sw-1, sh-1);	// calc end offset,
		int siw = ssm.getScanlineStride(); // Increment width = databuf's width

		//-- And destination variables...
		int doff = getBiOffset(dras, dsm, 0, 0); // Output table offset
		int[] dpx = ddb.getData(0); // Get the pixelset,
		int diw = dsm.getScanlineStride(); // Increment width = databuf's width

		//-- For each destination column....
		for(int k = 0; k < ow; k++) // DEST width
		{
			int dpo = doff; // Dest pixel offset,

			for(int i = 0; i < oh; i++) // DEST height
			{
				float r = 0.0f;
				float g = 0.0f;
				float b = 0.0f;
				ContribList cl = contrib[i];

				// Handle all contributing pixels for this destination pixel
				for(int j = 0; j < cl.m_n; j++) {
					int argb = spx[soff + cl.m_ar[j].m_pixel * siw];

					float weight = cl.m_ar[j].m_weight;
					if(weight != 0.0f) {
						r += weight * ((argb >> 16) & 0xff);
						g += weight * ((argb >> 8) & 0xff);
						b += weight * (argb & 0xff);
					}
				}

				//-- Make a proper RGB pair,
				int br, bg, bb;

				if(r > 255f)
					br = 255;
				else if(r < 0f)
					br = 0;
				else
					br = (int) r;

				if(g > 255f)
					bg = 255;
				else if(g < 0f)
					bg = 0;
				else
					bg = (int) g;

				if(b > 255f)
					bb = 255;
				else if(b < 0f)
					bb = 0;
				else
					bb = (int) b;

				//-- Now set a new pixel in working image,
				dpx[dpo] = (br << 16) | (bg << 8) | bb;
				dpo += diw;
			}

			//-- Next column please,
			soff++;
			doff++;
		}
		return worki;
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	The actual resampler...								*/
	/*--------------------------------------------------------------*/
	/**
	 *	The actual sampler code which converts a source image to a destination
	 *  image, reducing or enlarging the image. The size reduction does NOT
	 *  obey aspect ratio rules.
	 */
	static public BufferedImage resample(BufferedImage srci, ResamplerFilter f, int ow, int oh) {
		//-- Get some constants and stuff, and check for reasonable input
		int sw = srci.getWidth(null);
		int sh = srci.getHeight(null);
		if(sw < 2 || sh < 2)
			throw new IllegalArgumentException("Source is too small, you silly m/v");

		//-- Pre-calculate filter contributions for a row.
		ContribList[] contrib = getRowContrib(f, sw, ow);
		BufferedImage worki;
		if(srci.getType() == BufferedImage.TYPE_INT_RGB)
			worki = resampleRowRGB(srci, contrib, sw, sh, ow, oh);
		else
			worki = resampleRowGeneric(srci, contrib, sw, sh, ow, oh);

		//-- Precalculate filter contributions for a column
		contrib = getColContrib(f, sh, oh);
		//		return resampleColGeneric(worki, contrib, sw, sh, ow, oh);
		return resampleColRGB(worki, contrib, sw, sh, ow, oh);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Testing code.....									*/
	/*--------------------------------------------------------------*/

	static private void saveImage(BufferedImage bi, String name) throws Exception {
		ImaTool.saveJPEG(bi, new File(name), 0.5);
		//
		//		FileOutputStream fos = new FileOutputStream(name);
		//		JPEGImageEncoder je = JPEGCodec.createJPEGEncoder(fos);
		//		//		JPEGEncodeParam		ep	= je.getDefaultJPEGEncodeParam(ima);
		//		//		ep.setQuality((float) 0.5, false);
		//		je.encode(bi);
		//		fos.close();
	}


	static private ResamplerFilter loadFilter(String name) {
		try {
			Class< ? > cl = ImageSubsampler.class.getClassLoader().loadClass(name);
			if(!ResamplerFilter.class.isAssignableFrom(cl))
				throw new IllegalArgumentException(name + ": not a resampler filter class!");
			return (ResamplerFilter) cl.newInstance();
		} catch(Exception x) {
			//			System.out.println("X: Loading "+name+": "+x.toString());
		}
		return null;
	}

	static private BufferedImage loadImage(File srcf) throws Exception {
		Toolkit t = Toolkit.getDefaultToolkit();
		Canvas f = new Canvas();

		MediaTracker mt = new MediaTracker(f);
		Image ima = t.getImage(srcf.toString());
		mt.addImage(ima, 0);
		mt.waitForAll();
		mt.removeImage(ima, 0);

		//-- Create a writable image,
		BufferedImage bi = new BufferedImage(ima.getWidth(f), ima.getHeight(f), IMAT);
		Graphics2D g = (Graphics2D) bi.getGraphics();
		g.drawImage(ima, 0, 0, f);
		return bi;
	}

	/**
	 *	Does a resize of an object with size (iw, ih) to fit within a rectangle
	 *  of (w, h) while keeping the same aspect ratio
	 */
	static public Point resizeWithAspect(int w, int h, int iw, int ih) {
		//-- Calculate Idx / Odx as fx
		double fx = (double) iw / (double) w;
		double fy = (double) ih / (double) h;

		double f = fx / fy;
		if(f >= 1.0) {
			//-- Adjust Y
			h = (int) (ih / fx);
		} else {
			w = (int) (iw / fy);
		}
		return new Point(w, h);
	}

	static private ResamplerFilter getFilterByName(String fname) throws Exception {
		//-- Try to load the filter.
		ResamplerFilter filter = loadFilter(fname);
		if(filter == null)
			filter = loadFilter(fname + "Filter");
		if(filter == null)
			filter = loadFilter("to.etc.sjit." + fname);
		if(filter == null)
			filter = loadFilter("to.etc.sjit." + fname + "Filter");
		if(filter == null) {
			throw new Exception("Unknown filter. Make sure the name starts with an uppercase letter...");
		}
		return filter;
	}


	private static void saveWithFilter(BufferedImage bi, String fname, Point p) throws Exception {
		ResamplerFilter filter = getFilterByName(fname);

		//-- Get filter base name.
		String fn = filter.getClass().getName();
		int dot = fn.lastIndexOf('.');
		if(dot != -1)
			fn = fn.substring(dot + 1); // Get last name
		if(fn.endsWith("Filter"))
			fn = fn.substring(0, fn.length() - 6);

		//		ImageSubsampler	s	= new ImageSubsampler();

		System.out.print("Resampling with " + fn + " filter, ");
		long t = System.currentTimeMillis();
		BufferedImage obi = resample(bi, filter, p.x, p.y);
		t = System.currentTimeMillis() - t;
		System.out.print("save, ");
		saveImage(obi, "s-" + Integer.toString(p.x) + "x" + Integer.toString(p.y) + "-" + fn + ".jpg");
		System.out.println("Done in " + t + " millis");
	}


	private static void perfFilter(BufferedImage bi, String fname, Point p) throws Exception {
		ResamplerFilter filter = getFilterByName(fname);

		//-- Get filter base name.
		String fn = filter.getClass().getName();
		int dot = fn.lastIndexOf('.');
		if(dot != -1)
			fn = fn.substring(dot + 1); // Get last name
		if(fn.endsWith("Filter"))
			fn = fn.substring(0, fn.length() - 6);

		//		ImageSubsampler	s	= new ImageSubsampler();

		System.out.print("Performance of " + fn + "-preload,");
		resample(bi, filter, p.x, p.y);
		resample(bi, filter, p.x, p.y);
		System.gc();

		System.out.print("measure,");
		long t = System.currentTimeMillis();
		for(int i = 0; i < 10; i++)
			resample(bi, filter, p.x, p.y);
		t = System.currentTimeMillis() - t;
		System.out.println(t + " millis");
	}


	static private void pl(PrintWriter pw, String f, Point p) {
		pw.println("<br><br>");
		pw.println("<table><tr><td><img src=\"s-" + Integer.toString(p.x) + "x" + Integer.toString(p.y) + "-" + f + ".jpg\"></td></tr>");
		pw.println("<tr><td>The " + f + " filter.</td></tr></table>");
	}


	public static void main(String[] args) {
		if(args.length != 4 && args.length != 2) {
			System.out.println("Usage: ImageSubsampler <filename> <FilterClass> <maxwid> <maxhig>");
			System.out.println("Filters I know of are: Bell, Box, Hermite, Lanczos3, Mitchell,");
			System.out.println("Spline and Triangle. You can also specify ALL to get an output image for EACH filter..");
			System.exit(10);
		}

		int w, h;
		if(args.length == 2) {
			w = 100;
			h = 100;
		} else {
			w = Integer.parseInt(args[2]);
			h = Integer.parseInt(args[3]);
		}


		File f = new File(args[0]);
		try {
			//-- Load the image,
			BufferedImage bi = loadImage(f); // Get source image,
			Point p = resizeWithAspect(w, h, bi.getWidth(), bi.getHeight());

			if(args[1].equalsIgnoreCase("all")) {
				//-- For each filter.
				saveWithFilter(bi, "Bell", p);
				//				saveWithFilter(bi, "Box", p);
				saveWithFilter(bi, "Hermite", p);
				saveWithFilter(bi, "Lanczos3", p);
				saveWithFilter(bi, "Mitchell", p);
				saveWithFilter(bi, "Spline", p);
				saveWithFilter(bi, "Triangle", p);

				//-- Save an HTML outputfile..
				PrintWriter pw = new PrintWriter(new FileWriter("s-" + Integer.toString(p.x) + "x" + Integer.toString(p.y) + ".html"));
				pw.println("<html><head><title>Output of rescaling filter set</title></head>");
				pw.println("<body><h1>Generated for output size " + Integer.toString(p.x) + "x" + Integer.toString(p.y) + "</h1>");

				pl(pw, "Bell", p);
				//				pl(pw, "Box", p);
				pl(pw, "Hermite", p);
				pl(pw, "Lanczos3", p);
				pl(pw, "Mitchell", p);
				pl(pw, "Spline", p);
				pl(pw, "Triangle", p);
				pw.close();
			} else if(args[1].equalsIgnoreCase("perf")) {
				perfFilter(bi, "Bell", p);
			} else {
				saveWithFilter(bi, args[1], p);
			}
			System.out.println("Done");
		} catch(Throwable t) {
			System.out.println("FATAL: " + t.toString());
			t.printStackTrace();
		}
		System.exit(10);
	}


}

/**
 *	Contributor for a destination pixel
 */
class Contributor {
	/// Source pixel
	int		m_pixel;

	/// Pixel's weight in the destination.
	float	m_weight;
}


/**
 * 	List of source pixels that contribute to a destination pixel
 */
class ContribList {
	int				m_n;

	Contributor[]	m_ar;

	public ContribList(int width) {
		m_n = 0;
		m_ar = new Contributor[width];
		for(int i = 0; i < width; i++)
			m_ar[i] = new Contributor();

	}
}
