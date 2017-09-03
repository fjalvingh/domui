package to.etc.domui.test.ui.imagehelper;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 31-8-17.
 */
public class ByteImage implements Image {
	final private ByteImage m_parentImage;

	private int m_parentStartOffset;

	private int m_rootWidth;

	private int m_width;

	private int m_height;

	private byte[] m_data;

	public ByteImage(int width, int height) {
		m_width = width;
		m_height = height;
		m_data = new byte[width * height];
		m_rootWidth = width;
		m_parentStartOffset = 0;
		m_parentImage = null;
	}

	public ByteImage(int width, int height, byte[] data) {
		m_width = width;
		m_height = height;
		m_data = data;
		m_rootWidth = width;
		m_parentStartOffset = 0;
		m_parentImage = null;
	}

	private ByteImage(ByteImage parentImage, int parentStartOffset, int rootWidth, int width, int height, byte[] data) {
		m_parentStartOffset = parentStartOffset;
		m_rootWidth = rootWidth;
		m_width = width;
		m_height = height;
		m_data = data;
		m_parentImage = parentImage;
	}

	/**
	 * Get the (x, y) location of this image in the topmost parent
	 * @return
	 */
	public Point getRootLocation() {
		int y = m_parentStartOffset / m_rootWidth;
		int x = m_parentStartOffset % m_rootWidth;
		return new Point(x, y);
	}

	static public ByteImage create(BufferedImage si) {
		ByteImage ti = new ByteImage(si.getWidth(), si.getHeight());

		int offset = si.getHeight() * si.getWidth();
		for(int y = si.getHeight(); --y >= 0;) {
			for(int x = si.getWidth(); --x >= 0;) {
				int p = si.getRGB(x, y);

				int b = p & 0xff;
				p >>= 8;
				int g = p & 0xff;
				p >>= 8;
				int r = p & 0xff;
				p >>= 8;
				int a = p & 0xff;
				if(a == 0) {
					r = g = b = 255;
				}
				int av = (r + g + b) / 3;
				ti.m_data[--offset] = (byte) av;
			}
		}
		return ti;
	}

	public void save(BufferedImage to) {
		int offset = calculateXYOffset(0, m_height);
		int stride = m_rootWidth - m_width;
		for(int y = m_height; --y >= 0;) {
			offset -= stride;
			for(int x = m_width; --x >= 0;) {
				int p = m_data[--offset] & 0xff;

				int rgb = (p << 16) | (p << 8) | p;
				to.setRGB(x, y, rgb | 0xff000000);
			}
		}
	}

	public BufferedImage save() {
		BufferedImage bi = new BufferedImage(m_width, m_height, BufferedImage.TYPE_INT_ARGB);
		save(bi);
		return bi;
	}

	private int calculateXYOffset(int x, int y) {
		return m_parentStartOffset + (y * m_rootWidth) + x;
	}

	@Override public int width() {
		return m_width;
	}

	@Override public int height() {
		return m_height;
	}

	@Override public int get(int x, int y) {
		return m_data[y * m_width + x];
	}

	public ByteImage sub(int x, int y, int ex, int ey) {
		if(x > ex)
			throw new IllegalStateException("Bad range: x = " + x + " and ex = " + ex);
		if(y > ey)
			throw new IllegalStateException("Bad range: y = " + y + " and ey = " + ey);

		int offset = calculateXYOffset(x, y);
		return new ByteImage(this, offset, m_rootWidth, ex - x, ey - y, m_data);
	}

	public ByteImage stripBorder() {
		//-- detect top border
		int y = 0;
		int offset = calculateXYOffset(0, 0);
		int linePxCount = m_width - (m_width / 4);
		while(y < m_height) {
			int count = countLinePixels(offset, m_width, 0, 220);
			if(count < linePxCount)
				break;
			offset += m_rootWidth;
			y++;
		}

		int startY = y;

		//-- Detect bottom border
		offset = calculateXYOffset(0, m_height - 1);
		y = m_height;
		while(y > startY) {
			int count = countLinePixels(offset, m_width, 0, 220);
			if(count < linePxCount)
				break;
			y--;
			offset -= m_rootWidth;
		}

		int endY = y;

		int colPxCount = m_height - (m_height / 4);
		offset = calculateXYOffset(0, 0);
		int x = 0;
		while(x < m_width) {
			int count = countColumnPixels(offset, m_height, 0, 220);
			if(count < colPxCount)
				break;
			offset++;
			x++;
		}

		int startX = x;

		offset = calculateXYOffset(m_width -1, 0);
		x = m_width;
		while(x > startX) {
			int count = countColumnPixels(offset, m_height, 0, 220);
			if(count < colPxCount)
				break;
			x--;
			offset--;
		}

		return sub(startX, startY, x, endY);
	}

	/**
	 * Count the #of pixels on a line that fall between min and max.
	 * @return
	 */
	private int countLinePixels(int offset, int numPixels, int minValue, int maxValue) {
		int count = 0;

		if(numPixels > m_width)
			throw new IllegalStateException("Bad #of pixels: " + numPixels + ", it is > width " + m_width);

		int end = offset + numPixels;
		while(offset < end) {
			int p = m_data[offset++] & 0xff;
			if(p >= minValue && p <= maxValue)
				count++;
		}
		return count;
	}

	private int countColumnPixels(int offset, int numPixels, int minValue, int maxValue) {
		int count = 0;

		if(numPixels > m_height)
			throw new IllegalStateException("Bad #of pixels: " + numPixels + ", it is > height " + m_height);

		int end = offset + numPixels * m_rootWidth;
		while(offset < end) {
			int p = m_data[offset] & 0xff;
			if(p >= minValue && p <= maxValue)
				count++;
			offset += m_rootWidth;
		}
		return count;
	}

	public List<int[]> findVerticalRectangles() {
		int x = 0;

		int offset = m_parentStartOffset;
		boolean inwhite = false;

		List<int[]> res = new ArrayList<>();

		int startDarkX = -1;
 		while(x < m_width) {
			int pixels = countColumnPixels(offset, m_height, 200, 255);		// Count whitish pixels
			if(pixels >= m_height) {
				//-- This is a WHITE line
				if(startDarkX != -1) {
					//-- We now have a boundary.
					res.add(new int[] {startDarkX, x - 1});
					startDarkX = -1;
				}
			} else {
				//-- this is a CHARACTER line
				if(startDarkX == -1) {
					startDarkX = x;
				}
			}

			x++;
			offset++;
		}

		if(startDarkX != -1) {
			res.add(new int[] {startDarkX, x - 1});
		}

		return res;
	}


	public int[] findFontBaselines() {
		List<int[]> vr = findVerticalRectangles();

		//-- detect top border
		int y = 0;
		int offset = calculateXYOffset(0, 0);
		int linePxCount = vr.size() * 2;
		while(y < m_height) {
			int count = countLinePixels(offset, m_width, 0, 80);
			if(count > linePxCount)
				break;
			offset += m_rootWidth;
			y++;
		}
		int startY = y;

		//-- Detect bottom border
		offset = calculateXYOffset(0, m_height - 1);
		y = m_height - 1;
		while(y >= startY) {
			int count = countLinePixels(offset, m_width, 0, 80);
			if(count > linePxCount)
				break;
			y--;
			offset -= m_rootWidth;
		}

		int endY = y;
		return new int[] {startY, endY};
	}


}
