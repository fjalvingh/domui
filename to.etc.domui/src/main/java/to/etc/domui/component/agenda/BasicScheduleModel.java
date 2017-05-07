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
package to.etc.domui.component.agenda;

import java.util.*;

public class BasicScheduleModel<T extends ScheduleItem> implements ScheduleModel<T> {
	private List<ScheduleHoliday> m_holidays = new ArrayList<ScheduleHoliday>();

	private List<T> m_items = new ArrayList<T>();

	private List<ScheduleWorkHour> m_workHours = new ArrayList<ScheduleWorkHour>();

	private List<ScheduleModelChangedListener<T>> m_listeners = Collections.EMPTY_LIST;

	@Override
	public List<ScheduleHoliday> getScheduleHolidays(Date start, Date end) throws Exception {
		List<ScheduleHoliday> l = new ArrayList<ScheduleHoliday>();
		for(ScheduleHoliday h : m_holidays) {
			if(h.getDate().getTime() >= start.getTime() && h.getDate().getTime() < end.getTime())
				l.add(h);
		}
		return l;
	}

	@Override
	public List<T> getScheduleItems(Date start, Date end) throws Exception {
		List<ScheduleItem> l = new ArrayList<ScheduleItem>();
		for(ScheduleItem h : m_items) {
			if(h.getEnd().getTime() >= start.getTime() && h.getStart().getTime() < end.getTime())
				l.add(h);
		}
		return m_items;
	}

	@Override
	public List<ScheduleWorkHour> getScheduleWorkHours(Date start, Date end) throws Exception {
		return m_workHours;
	}

	public void addHoliday(ScheduleHoliday h) throws Exception {
		m_holidays.add(h);
		fireModelChanged();
	}

	public void addItem(T i) throws Exception {
		m_items.add(i);
		fireItemAdded(i);
	}

	public void deleteItem(T i) throws Exception {
		if(m_items.remove(i))
			fireItemDeleted(i);
	}

	public void changeItem(T i) throws Exception {
		fireItemChanged(i);
	}

	public void addWorkHour(ScheduleWorkHour h) throws Exception {
		m_workHours.add(h);
		fireModelChanged();
	}

	public void addWorkHour(Date start, Date end) throws Exception {
		m_workHours.add(new BasicScheduleWorkHour(start, end));
		fireModelChanged();
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Event handling.										*/
	/*--------------------------------------------------------------*/
	@Override
	public synchronized void addScheduleListener(ScheduleModelChangedListener<T> chl) {
		if(m_listeners.contains(chl))
			return;
		m_listeners = new ArrayList<ScheduleModelChangedListener<T>>(m_listeners);
		m_listeners.add(chl);
	}

	@Override
	public synchronized void removeScheduleListener(ScheduleModelChangedListener<T> chl) {
		m_listeners = new ArrayList<ScheduleModelChangedListener<T>>(m_listeners);
		m_listeners.remove(chl);
	}

	protected synchronized List<ScheduleModelChangedListener<T>> getListeners() {
		return m_listeners;
	}

	protected void fireModelChanged() throws Exception {
		List<ScheduleModelChangedListener<T>> list = getListeners();
		for(int i = list.size(); --i >= 0;) {
			list.get(i).scheduleModelChanged();
		}
	}

	protected void fireItemAdded(T si) throws Exception {
		List<ScheduleModelChangedListener<T>> list = getListeners();
		for(int i = list.size(); --i >= 0;) {
			list.get(i).scheduleItemAdded(si);
		}
	}

	protected void fireItemDeleted(T si) throws Exception {
		List<ScheduleModelChangedListener<T>> list = getListeners();
		for(int i = list.size(); --i >= 0;) {
			list.get(i).scheduleItemDeleted(si);
		}
	}

	protected void fireItemChanged(T si) throws Exception {
		List<ScheduleModelChangedListener<T>> list = getListeners();
		for(int i = list.size(); --i >= 0;) {
			list.get(i).scheduleItemChanged(si);
		}
	}
}
