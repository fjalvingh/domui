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

/**
 * Helper for the decoder; this contains data pertaining to the GIF image header.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class GifImaMeta {
	/// Coordinates of the subimage into the logical GIF screen.
	public int		m_bx, m_by;

	/// The width and height of this subimage
	public int		m_w, m_h;

	/// If T the image was stored as an interlaced image
	public boolean	m_interlaced;

	//-- Color table information (copy of global color table OR local one)

	/// T if this is a local color table
	public boolean	m_haslocalcolortable;

	/// T if the entries in this table are sorted
	public boolean	m_sorted;

	/// #of bits in this color table
	public int		m_bits_colortable;

	/// The #of entries in this color table
	public int		m_sz_colortable;

	/// The local color table, as an array of bytes,
	public byte[]	m_reds, m_grns, m_blus;

	/// Graphics Control Extension..
	public int		m_disposalmethod;

	public boolean	m_userinputflag;

	public boolean	m_transparant;

	public int		m_transparant_ix;

	public int		m_delaytime;
}
