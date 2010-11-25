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
 * Generic metrics storage class; base class both the per-thread metrics
 * storage plus the per-session and per-startup stores. This class allows
 * storage of multiple "top xxx" lists, where every list stores a descending
 * or ascending set of metrics - by some numeric value. This creates a top-nnn of
 * things, like:
 * <ul>
 *	<li>The top-10 slowest SQL statements</li>
 *	<li>The top-10 most-executed statements</li>
 *	<li>The top-10 requests with the slowest response time</li>
 * </ul>
 * Every specific top-xxx list has it's own max size, ascending/descending indication
 * and is identified by some string key value.
 *
 * <p>In every toplist, each "item" contains the following data:</p>
 * <ul>
 * 	<li>The numeric (long) value of the metric</li>
 * 	<li>An unique key representing 'the same source' object. For SQL statements this key <i>is</i> the
 * 		SQL statement; for requests this is the screen name <i>without parameters</i>. This key is used
 * 		to prevent multiple entries of the same SQL statement to obscure the list; only the "best scoring"
 * 		one is shown.</li>
 *	<li>A displayer handler which should be able to show title, overview and detail data for the item when shown on screen.</li>
 * </ul>
 * None of the methods of this base class are threadsafe; code using this must take care of that itself.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Nov 8, 2010
 */
public class PerformanceStore {
	final private Map<String, PerfList> m_listMap = new HashMap<String, PerfList>();

	public void define(String key, String desc, boolean descending, int maxsize) {
		if(m_listMap.get(key) != null)
			return;
		PerfList pl = new PerfList(key, desc, descending, maxsize);
		m_listMap.put(key, pl);
	}

	@Nonnull
	final public PerfList getList(String key) {
		PerfList pl = m_listMap.get(key);
		if(null == pl)
			throw new IllegalArgumentException("Unknown performance list '" + key + "'");
		return pl;
	}

	final public List<PerfList> getLists() {
		return new ArrayList<PerfList>(m_listMap.values());
	}

	final public List<PerfItem> getItems(String listKey) {
		PerfList pl = getList(listKey);
		return pl.getItemList();
	}

	/**
	 * Add an item to the specified list if it is is within that lists's bounds.
	 * @param listKey
	 * @param itemKey
	 * @param value
	 * @param data
	 */
	public void addItem(@Nonnull String listKey, @Nonnull String itemKey, long value, @Nullable String request, @Nullable Object data) {
		PerfList pl = getList(listKey);
		pl.addItem(itemKey, value, request, data);
	}

	public void addItem(@Nonnull String listKey, @Nonnull PerfItem pi) {
		PerfList pl = getList(listKey);
		pl.addItem(pi);
	}


	/**
	 * This merges-in the performance data from another store. For every list it merges the items of the
	 * other list inside this one. If a list in the other type is unknown here it's definition is copied,
	 * else the existing definition in here takes precedence. The developer should take care that merged
	 * lists should have the same data definition.
	 *
	 * @param other
	 */
	public void merge(PerformanceStore other) {
		if(null == other)
			return;
		for(PerfList otherpl : other.m_listMap.values()) {
			//-- 1. Create the list here if it does not yet exist.
			PerfList pl = m_listMap.get(otherpl.getKey());
			if(null == pl) {
				pl = new PerfList(otherpl); // Duplicate, and copy list.
				m_listMap.put(pl.getKey(), pl);
				continue;
			}

			//-- 2. Merge them.
			pl.mergeList(otherpl);
		}
	}

	public void clear() {
		for(PerfList pl : m_listMap.values()) {
			pl.clear();
		}
	}
}
