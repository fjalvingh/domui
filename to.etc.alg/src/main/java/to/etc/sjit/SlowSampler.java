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
 *	Subsamples an image to create a smaller (or larger(!)) image. This can use
 *  several sampling filters.
 *
 *
 *	Credits:
 *	The algorithms and methods used in this library are based on the article
 *  "General Filtered Image Rescaling" by Dale Schumacher which appeared in the
 *  book Graphics Gems III, published by Academic Press, Inc.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 *
 * @deprecated Use ImageSubsampler class instead.
 */
@Deprecated
public class SlowSampler {
	private SlowSampler() {
	}


	static private final int	IMAT	= BufferedImage.TYPE_INT_RGB;


	/**
	 *	The actual sampler code which converts a source image to a destination
	 *  image, reducing or enlarging the image. The size reduction does NOT
	 *  obey aspect ratio rules.
	 */
	static public BufferedImage resample(BufferedImage srci, ResamplerFilter f, int ow, int oh) {
		//-- 1. Create the output image.
		int sw = srci.getWidth(null);
		int sh = srci.getHeight(null);
		float fwidth = f.getWidth();
		if(sw < 2 || sh < 2)
			throw new IllegalArgumentException("Source is too small, you silly m/v");

		//-- Create intermediate image to hold horizontal zoom,
		BufferedImage worki = new BufferedImage(ow, sh, IMAT);
		float xscale = (float) (ow - 1) / (float) (sw - 1);
		float yscale = (float) (oh - 1) / (float) (sh - 1);

		//-- Pre-calculate filter contributions for a row.
		SContribList[] contrib = new SContribList[ow]; // Contributors for EACH pixel in the line

		if(xscale < 1.0f) {
			//-- Horizontal sub-sampling: scaling from bigger to smaller width
			float width = fwidth / xscale;
			float fscale = 1.0f / xscale;

			for(int i = 0; i < ow; i++) {
				SContribList cl = new SContribList((int) (width * 2.0f + 1));
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
				SContribList cl = new SContribList((int) (fwidth * 2.0f + 1f));
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

		//-- Now: apply filter to sample horizontally from SRC to WORK
		for(int k = 0; k < sh; k++) // SOURCE height (!)
		{
			for(int i = 0; i < ow; i++) // DEST width,
			{
				float r = 0.0f;
				float g = 0.0f;
				float b = 0.0f;
				SContribList cl = contrib[i];

				// Handle all contributing pixels for this destination pixel
				for(int j = 0; j < cl.m_n; j++) {
					int argb = srci.getRGB(cl.m_ar[j].m_pixel, k);
					float weight = cl.m_ar[j].m_weight;
					if(weight != 0.0f) {
						r += weight * ((argb >> 16) & 0xff);
						g += weight * ((argb >> 8) & 0xff);
						b += weight * (argb & 0xff);
					}
				}

				//-- Make a proper RGB pair,
				int br;
				int bg;
				int bb;

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
				worki.setRGB(i, k, (br << 16) | (bg << 8) | bb);
			}
			//			System.out.println("Line "+k);
		}

		//-- Precalculate filter contributions for a column
		contrib = new SContribList[oh];
		if(yscale < 1.0f) {
			//-- Vertical subsampling: from BIG to small picture
			float fscale = 1.0f / yscale;
			float width = fwidth / yscale;
			for(int i = 0; i < oh; i++) {
				SContribList cl = new SContribList((int) (width * 2.0 + 1));
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
				SContribList cl = new SContribList((int) (fwidth * 2.0 + 1));
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

		//-- Apply filter to sample vertically from work to destination image,
		BufferedImage obi = new BufferedImage(ow, oh, IMAT);

		for(int k = 0; k < ow; k++) // DEST width
		{
			for(int i = 0; i < oh; i++) // DEST height
			{
				float r = 0.0f;
				float g = 0.0f;
				float b = 0.0f;
				SContribList cl = contrib[i];

				// Handle all contributing pixels for this destination pixel
				for(int j = 0; j < cl.m_n; j++) {
					int argb = worki.getRGB(k, cl.m_ar[j].m_pixel);
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
				obi.setRGB(k, i, (br << 16) | (bg << 8) | bb);
			}
		}

		return obi;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Testing code.....									*/
	/*--------------------------------------------------------------*/

	static private ResamplerFilter loadFilter(String name) {
		try {
			Class< ? > cl = SlowSampler.class.getClassLoader().loadClass(name);
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
		ImaTool.saveJPEG(obi, new File("s-" + Integer.toString(p.x) + "x" + Integer.toString(p.y) + "-" + fn + ".jpg"), 0.7);
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
 *	SContributor for a destination pixel
 */
class SContributor {
	/// Source pixel
	int		m_pixel;

	/// Pixel's weight in the destination.
	float	m_weight;
}


/**
 * 	List of source pixels that contribute to a destination pixel
 */
class SContribList {
	int				m_n;

	SContributor[]	m_ar;

	public SContribList(int width) {
		m_n = 0;
		m_ar = new SContributor[width];
		for(int i = 0; i < width; i++)
			m_ar[i] = new SContributor();

	}
}
