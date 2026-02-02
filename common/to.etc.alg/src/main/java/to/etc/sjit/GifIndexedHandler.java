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

import java.awt.image.*;

/**
 * Part of the Animated Gif Decoder, this class known about the 8-bit Java
 * BufferedImage formats that fit the GIF format best - the TYPE_BYTE_INDEXED
 * format.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class GifIndexedHandler extends GifHandlerBase {
	/// The databuffer's data buffer!
	private byte[]			m_data;

	public GifIndexedHandler(AnimGifDecoder de, GifImaMeta im, int type) {
		super(de, im, type);
	}

	/**
	 *	Prepare decoding the current image. Create a bufferedimage and create
	 *  the color table belonging to it from the global or local color table.
	 */
	@Override
	protected BufferedImage prepare() throws java.io.IOException {
		//-- Prepare the color model,
		IndexColorModel icm;
		if(m_im.m_transparant)
			icm = new IndexColorModel(m_im.m_bits_colortable, m_im.m_sz_colortable, m_im.m_reds, m_im.m_grns, m_im.m_blus, m_im.m_transparant_ix);
		else
			icm = new IndexColorModel(m_im.m_bits_colortable, m_im.m_sz_colortable, m_im.m_reds, m_im.m_grns, m_im.m_blus);


		//-- Create the BufferedImage,
		m_bi = new BufferedImage(m_im.m_w, m_im.m_h, m_type, icm);

		//-- Get all writer data & check,
		Raster ras = m_bi.getRaster();
		SampleModel tsm = ras.getSampleModel();
		if(!(tsm instanceof PixelInterleavedSampleModel))
			return null;
		//		PixelInterleavedSampleModel	sm	= (PixelInterleavedSampleModel) tsm;

		DataBuffer dbt = ras.getDataBuffer();
		if(dbt.getDataType() != DataBuffer.TYPE_BYTE)
			return null;
		if(dbt.getNumBanks() != 1)
			return null;
		DataBufferByte dbb = (DataBufferByte) dbt;

		m_data = dbb.getData();

		return m_bi;
	}


	@Override
	protected void pixels(byte[] pix, int len) throws java.io.IOException {
		int poff = 0;
		while(len > 0) {
			int m = getRunMax(); // Get max #pixels allowed
			if(m > len)
				m = len; // Truncate to availability,
			int off = getOffset();
			System.arraycopy(pix, poff, m_data, off, m);
			len -= m;
			incrementPos(m);
			poff += m;
		}
	}
}
