package to.etc.syntaxer;

import java.io.Serializable;
import javax.swing.text.Segment;
import gnu.regexp.*;

/**
 * Lets <code>gnu.regexp</code> search within <code>Segment</code> objects.
 */
public class CharIndexedSegment implements CharIndexed, Serializable
{
	/**
	 * Creates a new <code>CharIndexedSegment</code>.
	 * @since jEdit 4.1pre3
	 */
	public CharIndexedSegment(Segment seg, int index)
	{
		this.seg = seg;
		m_index = index;
	}

	/**
	 * Creates a new <code>CharIndexedSegment</code>.
	 * @since jEdit 4.1pre1
	 */
	public CharIndexedSegment(Segment seg, boolean reverse)
	{
		this.seg = seg;
		m_index = (reverse ? seg.count - 1 : 0);
		this.reverse = reverse;
	}

	public char charAt(int index)
	{
		if(reverse)
			index = -index;

		return ((m_index + index) < seg.count && m_index + index >= 0)
			? seg.array[seg.offset + m_index + index]
			: CharIndexed.OUT_OF_BOUNDS;
	}

	public boolean isValid()
	{
		return (m_index >=0 && m_index < seg.count);
	}

	public void reset()
	{
		m_index = (reverse ? seg.count - 1 : 0);
	}

	public boolean move(int index)
	{
		if(reverse)
			index = -index;

		return ((m_index += index) < seg.count);
	}

	private Segment seg;
	private int m_index;
	private boolean reverse;
}
