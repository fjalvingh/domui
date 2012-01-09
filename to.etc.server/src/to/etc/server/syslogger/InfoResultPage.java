package to.etc.server.syslogger;

import java.util.*;


/**
 *	This handles a result page in some way. It holds some info about the page
 *  and contains a callback to get each entry's data...
 */
public abstract class InfoResultPage {
	/// The total #of results in the originating query (NOT the # on the page)!
	protected int	m_count;

	/// The index of the first one on this page
	protected int	m_ix_first;

	/// The actual #of results on this page. Will be below n_perpage at end of set!
	protected int	m_n_onpage;

	/// The requested #of results on a page. When zero the set is not sliced.
	protected int	m_n_perpage;

	public InfoResultPage() {
	}

	protected void setPageData(int count, int npp, int pnr) {
		m_count = count;
		m_n_perpage = npp;
		if(npp == 0) // Means a single big page
		{
			m_ix_first = 0; // Start here
			m_n_onpage = m_count;
			return;
		}
		m_ix_first = npp * pnr; // First item on this page,
		m_n_onpage = m_count - m_ix_first; // Get #items left
		if(m_n_onpage > npp)
			m_n_onpage = npp; // Do not allow more than #pp
		m_n_perpage = npp;
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Basic information calls...							*/
	/*--------------------------------------------------------------*/
	public boolean isEmpty() {
		return m_count == 0;
	}

	/**
	 *	Returns the total number of results in this query. It is the record count
	 *	returned by scanning the result set. It is NOT the page size!
	 */
	public int getCount() {
		return m_count;
	}

	/**
	 *	Returns this-page's number.
	 */
	public int getPagenr() {
		//-- Calc the page number;
		if(m_n_perpage == 0)
			return m_ix_first; // Unpaged: return the item number.
		return ((m_ix_first + m_n_perpage - 1) / m_n_perpage); // Else use this;
	}

	public int getNextPage() {
		return isLastPage() ? 0 : getPagenr() + 1;
	}

	public int getPrevPage() {
		return isFirstPage() ? 0 : getPagenr() - 1;
	}


	/**
	 *	Returns the #of pages in the current result set, determined by the page
	 *	size.
	 */
	public int getNumPages() {
		if(m_n_perpage == 0)
			return 1; // No pagesize = 1 page,
		return (m_count + m_n_perpage - 1) / m_n_perpage;
	}


	/**
	 *	Returns T if the current page is the "last" page.
	 */
	public boolean isLastPage() {
		if(m_n_perpage == 0)
			return true;

		return m_ix_first + m_n_perpage >= m_count;
	}

	/**
	 *	Returns T if the current page is the "first" page (page 0).
	 */
	public boolean isFirstPage() {
		return m_ix_first <= 0;
	}


	/**
	 *	Returns the row number for the first record on the current page.
	 */
	public int getFirstOnPage() {
		return m_ix_first;
	}

	/**
	 *	Returns the row number for the LAST record on the current page.
	 */
	public int getLastOnPage() {
		return m_ix_first + m_n_onpage;
	}

	public Iterator iterator() {
		return new IrEnumerator(this);
	}


	public abstract Object getItem(int ix);
}


class IrEnumerator implements Iterator {
	private InfoResultPage	m_ip;

	private int				m_ix;

	protected IrEnumerator(InfoResultPage ip) {
		m_ip = ip;
	}


	public boolean hasNext() {
		return (m_ix < m_ip.m_n_onpage);
	}

	public Object next() {
		return m_ip.getItem(m_ix++);
	}

	public void remove() {
		throw new java.lang.UnsupportedOperationException("Method remove() not yet implemented.");
	}


}
