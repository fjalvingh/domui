package to.etc.dbpool;

import java.util.*;

/**
 * Helper class which maintains track of hanging connections during
 * an expired connection scan.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Nov 3, 2010
 */
final class HangCheckState {
	private long m_now;

	private long m_expiryTS;

	private StringBuilder m_sb = new StringBuilder(8192);

	private ScanMode m_mode;

	/** When T the pool is out of connections. This forces connections closed regardless of scan mode. */
	private boolean m_forced;

	/** The #of hanging (pooled) connections. */
	private int m_hangCount;

	private int m_unpooledHangCount;

	/** The #of destroyed (pooled) connections. */
	private int m_destroyCount;

	/** List of all connections deemed to be hanging. Will contained destroyed connections too. */
	private List<ConnectionProxy> m_hangingList = new ArrayList<ConnectionProxy>();

	/** List of all connections that were released. */
	private List<ConnectionProxy> m_releasedList = new ArrayList<ConnectionProxy>();

	HangCheckState(ScanMode mode, long ts, long ets, boolean forced) {
		m_now = ts;
		m_expiryTS = ets;
		m_mode = mode;
		m_forced = forced;
	}

	public ScanMode getMode() {
		return m_mode;
	}

	public long getExpiryTS() {
		return m_expiryTS;
	}

	public long getNow() {
		return m_now;
	}

	public boolean isForced() {
		return m_forced;
	}

	public int getHangCount() {
		return m_hangCount;
	}

	public int getDestroyCount() {
		return m_destroyCount;
	}

	public int getUnpooledHangCount() {
		return m_unpooledHangCount;
	}

	public void incUnpooledHangCount() {
		m_unpooledHangCount++;
	}

	public void incHang() {
		m_hangCount++;
	}

	public void incDestroyed() {
		m_destroyCount++;
	}

	public List<ConnectionProxy> getHangingList() {
		return m_hangingList;
	}

	public List<ConnectionProxy> getReleasedList() {
		return m_releasedList;
	}

	public void addReleased(ConnectionProxy p) {
		m_releasedList.add(p);
	}

	public void addHanging(ConnectionProxy p) {
		m_hangingList.add(p);
	}

	public HangCheckState append(String s) {
		m_sb.append(s);
		return this;
	}

	public HangCheckState append(char c) {
		m_sb.append(c);
		return this;
	}

	public HangCheckState append(int i) {
		m_sb.append(i);
		return this;
	}

	public void appendTracepoint(Tracepoint allocationPoint) {
		DbPoolUtil.strStacktraceFiltered(m_sb, allocationPoint.getElements());
	}

	public String getReport() {
		return m_sb.toString();
	}
}
