package to.etc.util;

/**
 * Handles embedded progress actions.
 * 
 * 
 * @author jal
 * Created on May 9, 2004
 */
public class ProgressHandler {
	/** The object to notify when progress is made */
	private iProgressSink	m_sink;

	/** The parent of this progress - */
	private ProgressHandler	m_parent;

	/** The input range for the entire progress */
	private int				m_range_start;

	private int				m_range_end;

	private int				m_curval;

	private int				m_local_start, m_local_end;

	public ProgressHandler(iProgressSink sink) {
		m_sink = sink;
		m_range_end = 10000;
		m_range_start = 0;
	}

	public ProgressHandler(ProgressHandler dad) {
		m_parent = dad;
		m_sink = dad.m_sink;
	}

	public ProgressHandler getParent() {
		return m_parent;
	}

	/**
	 * Get the range we'll traverse from start to end for
	 * this progress level.
	 * @param low
	 * @param high
	 */
	public void setRange(int low, int high) {
		if(low > high)
			throw new IllegalStateException("Range bad " + low + "," + high);
		m_local_start = low;
		m_local_end = high;
		m_curval = low;
	}

	/**
	 * return a progress handler for a subrange of THIS range. The number
	 * of steps in THIS range can be set.
	 * 
	 * @param spread
	 * @return
	 */
	public ProgressHandler getSubrange(int spread) {
		//-- 1. Calculate the size of the spread in REAL steps
		if(spread < 0)
			spread = 1;
		if(spread + m_curval > m_local_end)
			spread = m_local_end - m_curval;

		int rsz = m_range_end - m_range_start; // Real spread available by this thingy,
		int lsz = m_local_end - m_local_start; // This-item's steps
		int nsz = (spread * rsz) / lsz; // #of REAL steps for the new subrange

		//-- Get REAL range start position for the new subrange
		int rs = (m_curval * rsz) / lsz + m_range_start;

		ProgressHandler ph = new ProgressHandler(this);// Get handler depending on this
		ph.m_range_start = rs;
		ph.m_range_end = rs + nsz; // Set new item's range end
		return ph;
	}

	/**
	 * Called when progress is made with the current progress 
	 * state for this progress range.
	 * @param v
	 */
	public void setValue(int v) {
		if(v >= m_local_end)
			v = m_local_end;
		if(v < m_curval || v < m_local_start)
			return;

		//-- Calculate a new percentage value.
		int off = v - m_local_start; // Get 0-based offset,
		int sz = (m_local_end - m_local_start); // And the size,

		//-- Get a factor for the real size and such
		int rdif = (m_range_end - m_range_start); // Get actual (0.10000) range to map to
		int pct = (off * rdif) / sz + m_range_start; // Get actual position

		m_sink.progressed(pct / 100); // And send real percentage
	}
}
