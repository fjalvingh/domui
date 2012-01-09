package to.etc.sjit;

/**
 * Helper for the decoder; this contains data pertaining to the GIF image header.
 *
 * <p>Title: Mumble Global Libraries - Non-database tools</p>
 * <p>Description: Small tools for Java programs</p>
 * <p>Copyright: Copyright (c) 2002 Frits Jalvingh; released under the LGPL licence.</p>
 * <p>Website <a href="http://www.mumble.to/">Mumble</a></p>
 * @author <a href="mailto:jal@mumble.to">Frits Jalvingh</a>
 * @version 1.0
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
