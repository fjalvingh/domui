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

import to.etc.domui.dom.css.DisplayType;
import to.etc.domui.dom.css.Overflow;
import to.etc.domui.dom.css.PositionType;
import to.etc.domui.dom.html.BR;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.Img;
import to.etc.domui.dom.html.NodeContainer;
import to.etc.domui.dom.html.Span;
import to.etc.domui.dom.html.TBody;
import to.etc.domui.dom.html.TD;
import to.etc.domui.dom.html.TR;
import to.etc.domui.dom.html.Table;
import to.etc.domui.dom.html.TableVAlign;
import to.etc.domui.server.RequestContextImpl;
import to.etc.domui.util.DomUtil;
import to.etc.util.DateUtil;
import to.etc.webapp.nls.NlsContext;

import javax.annotation.Nonnull;
import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WeekAgendaComponent<T extends ScheduleItem> extends Div implements ScheduleModelChangedListener<T> {
	/** The model to use, */
	private ScheduleModel<T> m_model;

	/** The schedule mode to use */
	private ScheduleMode m_mode = ScheduleMode.WORKWEEK;

	/** The corrected date for the schedule mode */
	private Date m_date;

	/** The adjusted end date of the period to show (excl) */
	private Date m_end;

	private List<ScheduleHoliday> m_holidays;

	private List<ScheduleWorkHour> m_workhours;

	private List<T> m_items;

	private int m_startHour;

	private int m_endHour;

	//	private int						m_maxMinutes;

	private NodeContainer m_itemBase;

	public interface IItemRenderer<T extends ScheduleItem> {
		void render(WeekAgendaComponent<T> component, NodeContainer target, T object) throws Exception;
	}

	private IItemRenderer<T> m_itemRenderer;

	//	private StringBuilder			m_sb = new StringBuilder();

	private int m_days;

	private Map<String, Div> m_renderMap = new HashMap<String, Div>();

	private IItemRenderer<T> m_actualItemRenderer;

	private INewAppointment m_newAppointmentListener;

	public WeekAgendaComponent() {}

	@Override
	public void onForceRebuild() {
		super.onForceRebuild();
		m_renderMap.clear();
	}

	/**
	 * Recalculate all of the calendar's date-based boundaries.
	 */
	private void initDateBounds() {
		//-- Depending on the mode fix the date on an appropriate start
		Calendar cal = Calendar.getInstance();
		cal.setTime(m_date);
		DateUtil.clearTime(cal);
		switch(m_mode){
			default:
				throw new IllegalStateException(m_mode + ": unhandled mode");
			case DAY:
				m_date = cal.getTime();
				m_end = DateUtil.tomorrow(m_date);
				m_days = 1;
				break;
			case WEEK:
				//-- Move back to SUNDAY
				while(cal.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY)
					cal.add(Calendar.DAY_OF_MONTH, -1);
				m_date = cal.getTime();
				cal.add(Calendar.DAY_OF_MONTH, 7);
				m_end = cal.getTime();
				m_days = 7;
				break;

			case WORKWEEK:
				//-- Move back to MONDAY
				while(cal.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY)
					cal.add(Calendar.DAY_OF_MONTH, -1);
				m_date = cal.getTime();
				cal.add(Calendar.DAY_OF_MONTH, 5);
				m_end = cal.getTime();
				m_days = 5;
				break;

			case MONTH:
				cal.set(Calendar.DAY_OF_MONTH, 1);
				m_date = cal.getTime();
				cal.add(Calendar.MONTH, 1);
				m_end = cal.getTime();
				m_days = 31;
				break;
		}
	}


	/**
	 * Initialize the renderer. This gets the model, decodes and gets the workhours
	 * and saves the calculated stuff for later use.
	 */
	private void initModel() throws Exception {
		//-- Get model data once.
		if(m_model == null)
			throw new IllegalStateException("Missing 'model' on WeekAgendaComponent");

		if(m_mode == null)
			m_mode = ScheduleMode.DAY; // Default to 'day' mode
		if(m_date == null) {
			setDate(new Date()); // If empty default to "today"
		}

		//-- Render the raster table.
		m_holidays = m_model.getScheduleHolidays(m_date, m_end);

		//-- Handle working hours to get a start- and endtime for the calendar.
		//-- Calculate raster start and end times.
		m_workhours = m_model.getScheduleWorkHours(m_date, m_end);
		m_startHour = -1;
		m_endHour = -1;
		Calendar cal = Calendar.getInstance();
		for(ScheduleWorkHour h : m_workhours) {
			cal.setTime(h.getStart());
			int sh = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE);
			if(m_startHour == -1 || sh < m_startHour)
				m_startHour = sh;
			cal.setTime(h.getEnd());
			sh = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE);
			if(m_endHour == -1 || sh > m_endHour)
				m_endHour = sh;
		}
		if(m_startHour == -1) {
			m_startHour = 8 * 60;
			m_endHour = 18 * 60;
		} else {
			//-- Round off to whole hour
			if(m_startHour % 60 != 0) {
				m_startHour = m_startHour - (m_startHour % 60);
			} else
				m_startHour -= 60;
			if(m_endHour % 60 != 0) {
				m_endHour = m_endHour + (60 - (m_endHour % 60));
			} else
				m_endHour += 60;
		}
		m_startHour /= 60;
		m_endHour /= 60 + 1;
		//		m_maxMinutes	= (m_endHour - m_startHour) * 60;

		m_items = m_model.getScheduleItems(m_date, m_end);
	}

	private void initBase() {
		Div d = new Div(); // This is the div that is "over" the layout and which contains all rendering items.
		m_itemBase = d;
		add(d);
		d.setPosition(PositionType.ABSOLUTE);
		d.setTop("0px");
		d.setLeft(0);
		d.setOverflow(Overflow.HIDDEN);
		d.setWidth("100%");
		d.setHeight("500px");
		d.setCssClass("ui-wa-f");
	}

	@Override
	public void createContent() throws Exception {
		initModel();
		setPosition(PositionType.RELATIVE);
		setTop("0px");
		setLeft(0);
		setOverflow(Overflow.HIDDEN);
		if(getWidth() == null)
			setWidth("100%");
		if(getHeight() == null)
			setHeight("500px");
		setCssClass("ui-wa ui-wa-" + m_mode.name());
		initBase();

		//-- Render the appropriate mode
		switch(m_mode){
			default:
				throw new IllegalStateException(m_mode + ": mode not implemented yet");
			case DAY:
				renderWeek(1);
				break;
			case WORKWEEK:
				renderWeek(5);
				break;
			case WEEK:
				renderWeek(7);
				break;
		}
		renderItems();
	}

	static public String renderDate(Date in) {
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTime(in);
		StringBuilder sb = new StringBuilder(32);
		sb.append(cal.get(Calendar.YEAR));
		sb.append(',');
		sb.append(cal.get(Calendar.MONTH) + 1); // The idiot that defined this standard should be shot.
		sb.append(',');
		sb.append(cal.get(Calendar.DAY_OF_MONTH));
		sb.append(',');
		sb.append(cal.get(Calendar.HOUR_OF_DAY));
		sb.append(',');
		sb.append(cal.get(Calendar.MINUTE));
		return sb.toString();
	}

	/**
	 * Renders the week backdrop: the actual thing that items are rendered on.
	 * @param days
	 * @throws Exception
	 */
	private void renderWeek(int days) throws Exception {
		Div div = new Div(); // Backdrop div
		add(div);

		setSpecialAttribute("startDate", renderDate(m_date));
		setSpecialAttribute("endDate", renderDate(m_end));
		//		setSpecialAttribute("startDate", Long.toString(m_date.getTime()));
		//		setSpecialAttribute("endDate", Long.toString(m_end.getTime()));
		setSpecialAttribute("days", Integer.toString(m_days));
		setSpecialAttribute("hourstart", Integer.toString(m_startHour));
		setSpecialAttribute("hourend", Integer.toString(m_endHour));

		div.setCssClass("ui-wa-bg"); // Hard style in style.css with div
		Table t = new Table();
		div.add(t);
		t.setCssClass("ui-wa-bgtbl");
		t.setBorder(0);
		t.setCellPadding("0");
		t.setCellSpacing("1");
		t.setTableWidth("100%");
		t.setHeight("100%"); // Should be TableHeight?

		//-- Start the day header
		TBody b = new TBody();
		t.add(b);
		TR tr = new TR();
		b.add(tr);
		tr.setCssClass("ui-wa-hd"); // wsheader

		TD td = new TD(); // Empty space above "times" column (gutter)
		tr.add(td);
		td.setCssClass("ui-wa-gt ui-wa-empty");

		//-- Render a cell for each day
		Calendar cal = Calendar.getInstance();
		cal.setTime(m_date);
		DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM, NlsContext.getLocale());
		for(int day = 0; day < days; day++) {
			td = new TD();
			tr.add(td);
			td.setValign(TableVAlign.TOP);
			td.setWidth((100 / days) + "%");
			Date d = cal.getTime();
			ScheduleHoliday h = findHoliday(d.getTime());
			if(h != null && h.getImageURL() != null) {
				Img i = new Img();
				td.add(i);
				i.setBorder(0);
				i.setCssClass("ui-wa-img");
				i.setSrc(h.getImageURL());
			}
			td.add(df.format(d)); // Add cell text
			if(h != null) {
				//-- This is a holiday.
				td.add(new BR());
				td.add(h.getHolidayName(NlsContext.getLocale()));
			} else {
				String[] shd = new DateFormatSymbols(NlsContext.getLocale()).getShortWeekdays();
				td.add(new BR());
				td.add(shd[cal.get(Calendar.DAY_OF_WEEK)]);
			}
			cal.add(Calendar.DAY_OF_MONTH, 1);
		}

		//-- Render the raster. We render lines per half hour.
		for(int hr = m_startHour; hr < m_endHour; hr++) {
			tr = new TR();
			b.add(tr);
			tr.setCssClass("ui-wa-row");

			//-- Render the hour thing
			td = new TD();
			tr.add(td);
			td.setCssClass("ui-wa-gt");
			td.setRowspan(2);
			Span sp = new Span();
			td.add(sp);
			sp.setCssClass("ui-wa-hour");
			sp.add(Integer.toString(hr));

			sp = new Span();
			td.add(sp);
			sp.setCssClass("ui-wa-min");
			sp.setText("00");

			//-- Render day empy cells
			for(int i = 0; i < days; i++) {
				td = new TD();
				tr.add(td);
				td.setCssClass("ui-wa-cell");
				td.setText("\u00a0");
			}

			//-- Write 2nd half hours, skip 1st col
			tr = new TR();
			b.add(tr);
			tr.setCssClass("ui-wa-row");
			for(int i = 0; i < days; i++) {
				td = new TD();
				tr.add(td);
				td.setCssClass("ui-wa-cell");
				td.setText("\u00a0");
			}
		}
	}

	private ScheduleHoliday findHoliday(long ts) {
		for(ScheduleHoliday h : m_holidays) {
			if(h.getDate().getTime() >= ts && h.getDate().getTime() < ts + (86400 * 1000))
				return h;
		}
		return null;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Rendering items.									*/
	/*--------------------------------------------------------------*/
	/**
	 * Queries the current model, and renders all items from there.
	 */
	private void renderItems() throws Exception {
		m_renderMap.clear();
		m_itemBase.forceRebuild();
		m_timef = DateFormat.getTimeInstance(DateFormat.SHORT, NlsContext.getLocale());
		for(T si : m_items) {
			renderItem(si);
		}
		//		m_timef = null;
	}

	private void renderItem(T si) throws Exception {
		Div sidiv = new Div(); // The movable container.
		m_itemBase.add(sidiv);
		renderItem(sidiv, si);
	}

	private void renderItem(Div sidiv, T si) throws Exception {
		sidiv.setCssClass("ui-wa-it");
		if(m_actualItemRenderer == null) {
			m_actualItemRenderer = getItemRenderer(); // Try with user-supplied value
			if(m_actualItemRenderer == null)
				m_actualItemRenderer = new DefaultScheduleItemRenderer<T>(); // Use default renderer
		}

		m_actualItemRenderer.render(this, sidiv, si);
		sidiv.setSpecialAttribute("startDate", renderDate(si.getStart()));
		sidiv.setSpecialAttribute("endDate", renderDate(si.getEnd()));
		//		sidiv.setSpecialAttribute("startDate", Long.toString(si.getStart().getTime()));
		//		sidiv.setSpecialAttribute("endDate", Long.toString(si.getEnd().getTime()));
		//		adjustPosition(sidiv, si);
		sidiv.setDisplay(DisplayType.NONE); // Hide; let Javascript behaviour enable it,

		if(null != m_renderMap.put(si.getID(), sidiv))
			throw new IllegalStateException("Duplicate ID in ScheduleItem: " + si.getID());
	}

	//	private int		m_pxPerHour = 45;
	//	private int		m_gutterWidth = 45;
	//	private int		m_cellWidth	= 250;
	//
	//	private void	adjustPosition(Div d, ScheduleItem si) {
	//		int[]	res = new int[2];
	//		if(! calculateMinuteOffset(res, si.getStart(), 1))
	//			return;
	//		int	sday	= res[0];
	//		int	smin	= res[1];
	//		if(! calculateMinuteOffset(res, si.getEnd(), -1))
	//			return;
	//		int	eday	= res[0];
	//		int	emin	= res[1];
	//
	//		int ys = Math.round(smin * m_pxPerHour / 60);
	//		int ye = Math.round(emin * m_pxPerHour / 60);
	//
	//		int	xo = m_gutterWidth + (sday*m_cellWidth);
	//
	//		d.setPosition(PositionType.ABSOLUTE);
	//		d.setTop(ys);
	//		d.setLeft(xo);
	//		d.setWidth((m_cellWidth-2)+"px");
	//		d.setHeight((ye-ys)+"px");
	//	}
	//
	//	private boolean	calculateMinuteOffset(int[] res, Date d, int grav) {
	//		long 	ts = d.getTime();		// Get ts in millis
	//		if(ts <= m_date.getTime())
	//			return false;
	//		if(ts >= m_end.getTime())
	//			return false;
	//
	//		//-- Is in range. Get a day offset,
	//		long dayoff = (long) Math.floor( (ts - m_date.getTime()) / (86400000) );
	//
	//		//-- Get a minute offset, skipping the invisible hours
	//		int mins = 0;
	//		int h = d.getHours();
	//		if(h < m_startHour) {
	//			if(grav > 0)
	//				mins = 0;
	//			else {
	//				//-- Round off to end of previous day,
	//				if(dayoff == 0)
	//					mins = 0;
	//				else {
	//					dayoff--;
	//					mins = (m_endHour - m_startHour) * 60;
	//				}
	//			}
	//		}
	//		else if(h >= m_endHour) {
	//			if(grav > 0) {
	//				//-- Round to next day,
	//				if(dayoff+1 >= m_days) {
	//					mins = m_maxMinutes;
	//				} else {
	//					dayoff++;
	//					mins = 0;
	//				}
	//			} else {
	//				mins = m_maxMinutes;
	//			}
	//		} else {
	//			h -= m_startHour;
	//			mins = h * 60 + d.getMinutes();
	//		}
	//		res[0] = (int)dayoff;
	//		res[1] = mins;
	//		return true;
	//	}


	/*--------------------------------------------------------------*/
	/*	CODING:	ScheduleModelChangeListener impl.					*/
	/*--------------------------------------------------------------*/

	protected boolean inWindow(T si) {
		return si.getStart().getTime() < m_end.getTime() && si.getEnd().getTime() > m_date.getTime();
	}

	@Override
	public void scheduleItemAdded(T si) throws Exception {
		if(!inWindow(si))
			return;
		renderItem(si);
	}

	@Override
	public void scheduleItemChanged(T si) throws Exception {
		Div d = m_renderMap.remove(si.getID());
		if(d == null)
			return;
		d.removeAllChildren();
		renderItem(d, si);
	}

	@Override
	public void scheduleItemDeleted(T si) throws Exception {
		Div d = m_renderMap.remove(si.getID());
		if(d == null)
			return;
		d.remove();
	}

	@Override
	public void scheduleModelChanged() throws Exception {
		forceRebuild();
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Helper stuff for rendering.							*/
	/*--------------------------------------------------------------*/
	static private final long MINS = 1000l * 60;

	static private final long HOURS = MINS * 60;

	static private final long DAYS = HOURS * 24;

	private DateFormat m_timef;

	public DateFormat getDateFormat() {
		if(m_timef == null)
			m_timef = DateFormat.getTimeInstance(DateFormat.SHORT, NlsContext.getLocale());
		return m_timef;
	}

	public void appendDuration(StringBuilder sb, long duration) {
		if(duration < 0)
			duration = -duration;
		sb.append(' ');
		String sep = "";
		if(duration >= DAYS) {
			long d = duration / DAYS;
			sb.append(d);
			sb.append('d');
			sep = " ";
			duration %= DAYS;
		}
		if(duration >= HOURS) {
			long d = duration / HOURS;
			sb.append(sep);
			sb.append(d);
			sb.append('h');
			sep = " ";
			duration %= HOURS;
		}
		if(duration >= MINS) {
			long d = duration / MINS;
			sb.append(sep);
			sb.append(d);
			sb.append('m');
		}
	}

	@Override
	public void componentHandleWebAction(@Nonnull RequestContextImpl ctx, @Nonnull String action) throws Exception {
		if("newappt".equals(action)) {
			String s = ctx.getParameter("date");
			if(s == null)
				throw new IllegalStateException("WeekAgendaComponent: missing date");
			long val = Long.parseLong(s);
			s = ctx.getParameter("duration");
			if(s == null)
				throw new IllegalStateException("WeekAgendaComponent: missing duration");
			long dur = Long.parseLong(s);

			if(getNewAppointmentListener() != null) {
				Calendar cal = Calendar.getInstance();
				cal.setTimeInMillis(val);
				getNewAppointmentListener().newAppointment(cal.getTime(), dur);
			}
		} else
			super.componentHandleWebAction(ctx, action);
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Simple setters and getters.							*/
	/*--------------------------------------------------------------*/

	public ScheduleModel<T> getModel() {
		return m_model;
	}

	public void setModel(ScheduleModel<T> model) {
		if(DomUtil.isEqual(model, m_model))
			return;
		if(m_model != null)
			m_model.removeScheduleListener(this);
		m_model = model;
		if(m_model != null)
			m_model.addScheduleListener(this);
		forceRebuild();
	}

	public ScheduleMode getMode() {
		return m_mode;
	}

	public void setMode(ScheduleMode mode) {
		if(m_mode == mode)
			return;
		m_mode = mode;
		initDateBounds();
		forceRebuild();
	}

	public Date getDate() {
		return m_date;
	}

	public void setDate(Date date) {
		if(DomUtil.isEqual(date, m_date))
			return;
		m_date = date;
		initDateBounds();
		forceRebuild();
	}

	public int getDisplayDays() {
		return m_days;
	}

	public Date getFirstDate() {
		return m_date;
	}

	public Date getLastDate() {
		return m_end;
	}

	public IItemRenderer<T> getItemRenderer() {
		return m_itemRenderer;
	}

	public void setItemRenderer(IItemRenderer<T> itemRenderer) {
		m_itemRenderer = itemRenderer;
	}

	public INewAppointment getNewAppointmentListener() {
		return m_newAppointmentListener;
	}

	public void setNewAppointmentListener(INewAppointment newAppointmentListener) {
		m_newAppointmentListener = newAppointmentListener;
	}
}
