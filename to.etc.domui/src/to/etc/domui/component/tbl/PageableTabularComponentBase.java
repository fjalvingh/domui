/*
 * DomUI Java User Interface library
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
package to.etc.domui.component.tbl;

import javax.annotation.*;
import java.util.*;

abstract public class PageableTabularComponentBase<T> extends SelectableTabularComponent<T> implements ITableModelListener<T> {
	/** The current page #, starting at 0 */
	private int m_currentPage;

	protected int m_six, m_eix;

	abstract protected int getPageSize();

	public PageableTabularComponentBase(ITableModel<T> model) {
		super(model);
	}

	public PageableTabularComponentBase() {}

	@Override
	protected void fireModelChanged(@Nullable ITableModel<T> old, @Nullable ITableModel<T> model) {
		//m_currentPage = 0;									// jal See bugzilla 7383; ask me when you have issues with sorting/paging!
		super.fireModelChanged(old, model);
	}

	@Override
	protected void resetState() {
		m_currentPage = 0;
	}

	protected void calcIndices() throws Exception {
		int size = getModel().getRows();
		int pageSize = getPageSize();
		if(pageSize <= 0) {
			m_six = 0;
			m_eix = size;
		} else {
			m_six = m_currentPage * pageSize; // Start index,
			if(m_six >= size) {
				//-- Move to the last page instead
				int lp = (size / pageSize); // Page# of last element,
				m_six = lp * pageSize; // Start of that page
			}
			m_eix = m_six + pageSize;
			if(m_eix > size)
				m_eix = size;
		}
	}

	@Nonnull
	protected List<T> getPageItems() throws Exception {
		return getModel().getItems(m_six, m_eix); // Data to show
	}

	public int getCurrentPage() {
		return m_currentPage;
	}

	public void setCurrentPage(int currentPage) {
		if(m_currentPage < 0 || (currentPage != 0 && getPageSize() <= 0))
			throw new IllegalStateException("Cannot set current page to " + currentPage);
		if(m_currentPage == currentPage)
			return;
		m_currentPage = currentPage;
		forceRebuild();
		firePageChanged();
	}

	public int getPageCount() throws Exception {
		int pageSize = getPageSize();
		if(pageSize <= 0)
			return 1;
		return (getModel().getRows() + pageSize - 1) / pageSize;
	}

	/**
	 * FIXME jal 20160125 Remove and replace with isTruncated property.
	 * @return
	 * @throws Exception
	 */
	public int getTruncatedCount() throws Exception {
		ITableModel<T> tm = getModel();
		if(tm == null || !(tm instanceof ITruncateableDataModel))
			return 0;
		ITruncateableDataModel t = (ITruncateableDataModel) tm;
		if(t.isTruncated())
			return getModel().getRows();
		return 0;
	}
}
