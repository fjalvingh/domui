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

import java.text.*;
import java.util.*;

import javax.annotation.*;

import to.etc.domui.dom.html.*;
import to.etc.util.*;
import to.etc.webapp.nls.*;

/**
 * This is a small panel which contains all days in a given month as a calendar. It can be
 * used to navigate quickly between dates.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 5, 2008
 */
public class MonthPanel extends Div {
	private Date m_date;

	private int m_firstDay = Calendar.MONDAY;

	private int m_month;

	private IDayClicked m_dayClicked;

	private Date m_firstDayDate;

	private IClicked<TD> m_clickHandler;

	private TBody m_body;

	@Override
	public void createContent() throws Exception {
		setCssClass("ui-mp");
		getDate(); // Prime date
		Calendar cal = Calendar.getInstance(NlsContext.getLocale());
		cal.setTime(m_firstDayDate);

		//-- We're now on the 1st "firstday" and can move from there.
		Table t = new Table();
		add(t);
		t.setCssClass("ui-mp");
		t.setCellPadding("0");
		t.setCellSpacing("0");
		TBody b = t.getBody();
		m_body = b;

		//-- Create the top row (day labels)
		createTopRow(b);

		//-- If we need to act on clicks add a clickhandler for dayclicks.
		if(getDayClicked() != null && m_clickHandler == null) {
			m_clickHandler = new IClicked<TD>() {
				@Override
				public void clicked(@Nonnull TD bx) throws Exception {
					handleClick(bx);
				}
			};
		}

		//-- Create rows of weeks. End when the current week ends on a new month.

		for(;;) {
			createWeekRow(b, cal);

			if(cal.get(Calendar.MONTH) != m_month)
				break;
		}
	}

	void handleClick(TD cell) throws Exception {
		if(null == getDayClicked())
			return;

		//-- Calculate the day from the cell #
		TR row = cell.getParent(TR.class); // Find the parent row
		int cellix = row.findChildIndex(cell); // Find the index in the row. Index 0 = weeknr, rest is a day;
		if(cellix <= 0)
			return;
		cellix--;

		Date start = (Date) row.getRowData();
		Calendar cal = Calendar.getInstance();
		cal.setTime(start);
		cal.add(Calendar.DATE, cellix);
		start = cal.getTime();

		//-- Call user code.
		getDayClicked().dayClicked(this, start);
	}

	/**
	 * Create a row of cells for a single week. Starts with the week number, followed by
	 * the dates in that week.
	 * @param b
	 * @param cal
	 */
	private void createWeekRow(TBody b, Calendar cal) {
		int wd = cal.get(Calendar.DAY_OF_WEEK);
		if(wd != m_firstDay) // MUST always start on specified day.
			throw new IllegalStateException("Internal logic error");
		TR row = b.addRow();
		row.setRowData(cal.getTime());

		//-- Add the week number
		TD td = b.addCell();
		td.setCssClass("ui-mp-w");
		int wn = cal.get(Calendar.WEEK_OF_YEAR);
		td.setText(Integer.toString(wn));

		//-- Now render the rest of the days, passing into the next month where needed.
		String cssinm = getDayClicked() == null ? "ui-mp-d" : "ui-mp-d ui-mp-ch"; // CSS style for inside month
		String cssexm = getDayClicked() == null ? "ui-mp-xd" : "ui-mp-xd ui-mp-ch"; // CSS styl for outside month

		for(int i = 0; i < 7; i++) {
			td = b.addCell();
			int dn = cal.get(Calendar.DATE);
			int mn = cal.get(Calendar.MONTH);
			td.setText(Integer.toString(dn));
			td.setCssClass(mn == m_month ? cssinm : cssexm);
			if(getDayClicked() != null)
				td.setClicked(m_clickHandler);
			cal.add(Calendar.DATE, 1);
		}
	}

	private void createTopRow(TBody b) {
		DateFormatSymbols dfs = new DateFormatSymbols(NlsContext.getLocale());
		TR tr = b.addRow();
		tr.setCssClass("ui-mp-mn");
		TD td = b.addCell(); // Empty cell above the week number.
		td.setColspan(8);
		td.setText(dfs.getMonths()[m_month]);

		tr = b.addRow();
		tr.setCssClass("ui-mp-top");
		td = b.addCell(); // Empty cell above the week number.
		td.setCssClass("ui-mp-e");

		int cday = m_firstDay; // Day# currently,
		for(int i = 0; i < 7; i++) {
			String day = dfs.getShortWeekdays()[cday]; // Short day name
			td = b.addCell();
			td.setCssClass("ui-mp-d");
			td.setText(day);

			cday++;
			if(cday > Calendar.SATURDAY)
				cday = Calendar.SUNDAY;
		}
	}

	public Date getDate() {
		if(m_date == null)
			setDate(new Date());
		return m_date;
	}

	/**
	 * Sets the date for this month panel. Any date in the month will do.
	 * @param date
	 */
	public void setDate(Date date) {
		//-- Move to the 1st visible date in the month;
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		DateUtil.clearTime(cal);
		cal.set(Calendar.DATE, 1); // Move to the 1st of the month, 0:00.
		Date then = cal.getTime();

		if(m_date != null && then.getTime() == m_date.getTime()) // Already the same?
			return; // No redraw needed
		m_date = then;

		//-- Calculate the 1st visible day
		m_month = cal.get(Calendar.MONTH);

		//-- Locate the first "firstDay" AT or BEFORE the 1st of this month.
		DateUtil.clearTime(cal);
		cal.set(Calendar.DATE, 1); // Move to 1st of the month, 0:00
		int cnt = 7;
		for(;;) {
			if(cnt-- < 0)
				throw new IllegalStateException("?? Cannot reach weekday=" + m_firstDay);
			int weekday = cal.get(Calendar.DAY_OF_WEEK);
			if(weekday == m_firstDay)
				break;
			cal.add(Calendar.DATE, -1);
		}
		m_firstDayDate = cal.getTime(); // Date for first visible cell in the month
		forceRebuild();
	}

	public int getFirstDay() {
		return m_firstDay;
	}

	public void setFirstDay(int firstDay) {
		if(m_firstDay == firstDay)
			return;

		forceRebuild();
		m_firstDay = firstDay;
		setDate(getDate());
	}

	public IDayClicked getDayClicked() {
		return m_dayClicked;
	}

	public void setDayClicked(IDayClicked dayClicked) {
		if((dayClicked == null && m_dayClicked != null) || (dayClicked != null && m_dayClicked == null))
			forceRebuild();
		m_dayClicked = dayClicked;
	}

	/**
	 * If the given day is present on the calendar mark it.
	 * @param dt
	 */
	public void setMarked(Date dt, String css) throws Exception {
		mark(dt, css, true);
	}

	public void setUnmarked(Date dt, String css) throws Exception {
		mark(dt, css, false);
	}

	public void unmarkAll(String css) {
		if(m_body == null)
			return;
		for(NodeBase row : m_body) {
			TR tr = (TR) row;
			for(NodeBase b2 : tr) {
				TD td = (TD) b2;
				td.removeCssClass(css);
			}
		}
	}

	private void mark(Date dt, String css, boolean on) throws Exception {
		if(dt == null)
			return;
		if(dt.getTime() < m_firstDayDate.getTime())
			return;

		if(css == null)
			css = "mp-ui-mrk";
		build();

		//-- Walk all month rows and check which one contains this date
		for(NodeBase row : m_body) {
			TR tr = (TR) row;
			Date rd = (Date) tr.getRowData();
			if(rd == null)
				continue;

			long ets = rd.getTime() + 1000 * 7 * 86400l;
			if(dt.getTime() >= rd.getTime() && dt.getTime() < ets) {
				//-- Date is on this row; calculate the cell;
				int cellix = (int) ((dt.getTime() - rd.getTime()) / 86400000l) + 1;
				TD cell = (TD) tr.getChild(cellix);
				if(on)
					cell.addCssClass(css);
				else
					cell.removeCssClass(css);
			}
		}
	}
}
