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
package to.etc.dbpool.info;

import java.util.*;

import javax.annotation.*;

/**
 * Definition for some performance counter. It defines the list key, it's presentation name,
 * it's ordering (ascending/descending) and a data item renderer which renders the info elements
 * inside it.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Nov 8, 2010
 */
final public class PerfList {
	final private String m_key;

	final private String m_description;

	final private boolean m_descending;

	final private int m_maxSize;

	/** The actual list of performance items. This is ordered by value in ascending/descending order as specified by m_descending */
	final private List<PerfItem> m_itemList;

	private Map<String, PerfItem> m_existingMap = new HashMap<String, PerfItem>();

	static private final Comparator<PerfItem> C_ASCENDING = new Comparator<PerfItem>() {
		@Override
		public int compare(PerfItem a, PerfItem b) {
			long v = a.getMetric() - b.getMetric();
			if(v == 0)
				return 0;
			return v > 0 ? 1 : -1;
		}
	};

	static private final Comparator<PerfItem> C_DESCENDING = new Comparator<PerfItem>() {
		@Override
		public int compare(PerfItem a, PerfItem b) {
			long v = a.getMetric() - b.getMetric();
			if(v == 0)
				return 0;
			return v > 0 ? -1 : 1;
		}
	};

	public PerfList(String key, String description, boolean descending, int maxSize) {
		m_key = key;
		m_description = description;
		m_descending = descending;
		m_maxSize = maxSize;
		m_itemList = new ArrayList<PerfItem>();
	}

	PerfList(PerfList other) {
		m_key = other.getKey();
		m_description = other.getDescription();
		m_descending = other.isDescending();
		m_maxSize = other.getMaxSize();
		m_itemList = new ArrayList<PerfItem>(other.m_itemList);
	}

	/**
	 * This-list's key, used to find it.
	 * @return
	 */
	public String getKey() {
		return m_key;
	}

	/**
	 * A description for this list to show in the performance pages.
	 * @return
	 */
	public String getDescription() {
		return m_description;
	}

	/**
	 * T if this lists's values are descending, meaning a high value is on top.
	 * @return
	 */
	public boolean isDescending() {
		return m_descending;
	}

	/**
	 * The #of items that should be maintained, i.e. the xxx in top-xxx.
	 * @return
	 */
	public int getMaxSize() {
		return m_maxSize;
	}

	/**
	 * Add an item to this list, if it should be part of it.
	 * @param itemKey
	 * @param value
	 * @param data
	 */
	void addItem(@Nonnull String itemKey, long value, @Nullable String request, @Nullable Object data) {
		//-- 1. Can this item ever be part of this list?
		if(m_itemList.size() >= m_maxSize) { // At max size?
			PerfItem pi = m_itemList.get(m_maxSize - 1);
			if(m_descending) {
				//-- If we are smaller than the last one we're not part of the list.
				if(value <= pi.getMetric())
					return;
			} else {
				//-- If we're higher than the last one we're not part of the list.
				if(value >= pi.getMetric())
					return;
			}
		}

		//-- 2. We must get in here. If an existing list item has the same itemKey (if present) compare this with that one to see if this metric is worse
		if(null != itemKey) {
			PerfItem pi = m_existingMap.get(itemKey);
			if(null != pi) {
				if(m_descending) {
					if(value <= pi.getMetric())
						return;
				} else {
					if(value >= pi.getMetric())
						return;
				}

				//-- We need to replace the existing metric, so remove that
				m_itemList.remove(pi); // Remove existing metric.
			}
		}

		//-- 3. We need to insert this in proper order inside the metric list, then we need to remove any element after maxSize.
		PerfItem npi = new PerfItem(itemKey, value, request, data);
		addItem(npi);
	}

	void addItem(@Nonnull PerfItem npi) {
		int ispot = Collections.binarySearch(m_itemList, npi, m_descending ? C_DESCENDING : C_ASCENDING);
		if(ispot < 0)
			ispot = -ispot - 1;
		m_itemList.add(ispot, npi);
		while(m_itemList.size() > m_maxSize)
			m_itemList.remove(m_itemList.size() - 1);
	}

	/**
	 * Merge the data from another list of the same category. This code uses
	 * a suboptimal algorithm. A better algorith would walk all items-to-insert from top to bottom.
	 * Then it would binarySearch the insert point, while limiting the insert area to the last insert
	 * point.
	 *
	 * @param otherpl
	 */
	void mergeList(PerfList otherpl) {
		for(int i = 0, len = otherpl.m_itemList.size(); i < len; i++) {
			PerfItem opi = otherpl.m_itemList.get(i);
			if(m_itemList.size() < m_maxSize) {
				addItem(opi);
			} else {
				PerfItem lpi = m_itemList.get(m_itemList.size() - 1); // Get current last item.
				if(m_descending) {
					if(opi.getMetric() > lpi.getMetric())
						addItem(opi);
					else
						return;
				} else {
					if(opi.getMetric() < lpi.getMetric())
						addItem(opi);
					else
						return;
				}
			}
		}
	}

	/**
	 * Return duplicate of the item list.
	 * @return
	 */
	List<PerfItem> getItemList() {
		return new ArrayList<PerfItem>(m_itemList);
	}

	public void clear() {
		m_itemList.clear();
		m_existingMap.clear();
	}
}
