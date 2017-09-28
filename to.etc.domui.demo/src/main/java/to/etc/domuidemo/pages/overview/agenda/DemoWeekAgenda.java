package to.etc.domuidemo.pages.overview.agenda;

import to.etc.domui.component.agenda.BasicScheduleHoliday;
import to.etc.domui.component.agenda.BasicScheduleItem;
import to.etc.domui.component.agenda.BasicScheduleModel;
import to.etc.domui.component.agenda.BasicScheduleWorkHour;
import to.etc.domui.component.agenda.DefaultScheduleItemRenderer;
import to.etc.domui.component.agenda.INewAppointment;
import to.etc.domui.component.agenda.MonthPanel;
import to.etc.domui.component.agenda.ScheduleItem;
import to.etc.domui.component.agenda.ScheduleWorkHour;
import to.etc.domui.component.agenda.WeekAgendaComponent;
import to.etc.domui.component.agenda.WeekAgendaComponent.IItemRenderer;
import to.etc.domui.component.buttons.DefaultButton;
import to.etc.domui.component.form.TabularFormBuilder;
import to.etc.domui.component.input.DateInput;
import to.etc.domui.component.input.Text;
import to.etc.domui.component.layout.FloatingWindow;
import to.etc.domui.component.layout.SplitPanel;
import to.etc.domui.converter.SecondDurationConverter;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.IClicked;
import to.etc.domui.dom.html.NodeContainer;
import to.etc.domui.dom.html.UrlPage;
import to.etc.domui.util.DomUtil;
import to.etc.util.DateUtil;
import to.etc.util.StringTool;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class DemoWeekAgenda extends UrlPage {
	BasicScheduleModel<ScheduleItem>	m_model;
	WeekAgendaComponent<ScheduleItem>	m_agenda;
	private int							m_idgen;

	@Override
	public void createContent() throws Exception {
		Div	left	= new Div();
		Calendar	cal	= Calendar.getInstance();
		Date		sel	= cal.getTime();

		MonthPanel	mp	= new MonthPanel();
		left.add(mp);
		mp.setDate(sel);

		Div spcr = new Div();
		spcr.setHeight("10px");
		left.add(spcr);

		mp	= new MonthPanel();
		left.add(mp);
		cal.add(Calendar.MONTH, 1);
		mp.setDate(cal.getTime());

		WeekAgendaComponent<ScheduleItem>	wac = new WeekAgendaComponent<ScheduleItem>();
		m_agenda = wac;

		SplitPanel	sp	= new SplitPanel(left, "220px", wac, "");
		add(sp);
		sp.setWidth("100%");

		BasicScheduleModel<ScheduleItem>	m = getScheduleModel();
		wac.setModel(m);
		m_model = m;
		wac.setDate(sel);				// For now.

		DefaultButton b = new DefaultButton("Add Random", new IClicked<DefaultButton>() {
			@Override
			public void clicked(DefaultButton xb) throws Exception {
				addFake();
			}
		});
		add(b);

		IItemRenderer<ScheduleItem> care = new DefaultScheduleItemRenderer<ScheduleItem>() {
			@Override public void render(WeekAgendaComponent<ScheduleItem> age, NodeContainer root, ScheduleItem si) throws Exception {
				super.render(age, root, si);
				root.setBackgroundColor(DomUtil.createRandomColor());
			}
		};
		m_agenda.setItemRenderer(care);

		m_agenda.setNewAppointmentListener(new INewAppointment() {
			@Override
			public void newAppointment(Date dt, long duration) throws Exception {
				System.out.println("new appt "+dt+", dur="+StringTool.strDurationMillis(duration));
				newAppt(dt, duration);
			}
		});
	}

	void	addFake() throws Exception {
		//-- 5 days, from 10:00 to 16:00 every half hour = 6 hours == 12 half hours
		int	max = 5 * 12;
		int	sel = (int) (Math.random() * max);

		int	day	= (sel / 12);
		sel	%= 12;
		int	hour	= 10+(sel /2);
		Calendar	cal	= Calendar.getInstance();
		cal.setTime(m_agenda.getDate());
		System.out.println("Calendar start day is "+m_agenda.getDate());
		cal.add(Calendar.DAY_OF_MONTH, day);
		cal.set(Calendar.HOUR_OF_DAY, hour);
		cal.set(Calendar.MINUTE, (sel & 0x1) == 0 ? 0 : 30);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		Date	start = cal.getTime();
		int dur	= 1+(int)(6*Math.random());
		cal.add(Calendar.MINUTE, 15*dur);
		Date	end	= cal.getTime();

		BasicScheduleItem	si = new BasicScheduleItem((++m_idgen)+"", start, end, null, "Random#"+m_idgen, null, null);
		m_model.addItem(si);
		System.out.println("Add fake "+start+" to "+end);
	}

	String	newID() {
		return Integer.toString(++m_idgen);
	}
	private BasicScheduleModel<ScheduleItem>	getScheduleModel() throws Exception {
		BasicScheduleModel<ScheduleItem>	m = new BasicScheduleModel<ScheduleItem>() {
			@Override
			public List<ScheduleWorkHour> getScheduleWorkHours(Date start, Date end) {
				Calendar	cal	= Calendar.getInstance();
				cal.setTime(start);
				DateUtil.clearTime(cal);
				List<ScheduleWorkHour>	l = new ArrayList<ScheduleWorkHour>();
				while(cal.getTimeInMillis() < end.getTime()) {
					DateUtil.clearTime(cal);
					cal.set(Calendar.HOUR_OF_DAY, 8);
					cal.set(Calendar.MINUTE, 30);
					Date	d1 = cal.getTime();			// 9:30

					cal.set(Calendar.HOUR_OF_DAY, 12);
					cal.set(Calendar.MINUTE, 10);
					l.add(new BasicScheduleWorkHour(d1, cal.getTime()));

					cal.set(Calendar.HOUR_OF_DAY, 13);
					cal.set(Calendar.MINUTE, 00);
					d1 = cal.getTime();			// 13:00 - 18:00

					cal.set(Calendar.HOUR_OF_DAY, 18);
					cal.set(Calendar.MINUTE, 00);
					l.add(new BasicScheduleWorkHour(d1, cal.getTime()));
					cal.add(Calendar.DAY_OF_MONTH, 1);
				}
				return l;
			}
		};

		//-- Add some appointments. First adjust to 1st day of the week
		Calendar	c = Calendar.getInstance();
		DateUtil.clearTime(c);
		int day = c.get(Calendar.DAY_OF_WEEK);
		int delta = 0;
		switch(day) {
			default:
				break;
			case Calendar.TUESDAY:	delta = -1;	break;
			case Calendar.WEDNESDAY:delta = -2;	break;
			case Calendar.THURSDAY:	delta = -3;	break;
			case Calendar.FRIDAY:	delta = -4;	break;
			case Calendar.SATURDAY:	delta = -5;	break;
			case Calendar.SUNDAY:	delta = +1;	break;
		}
		c.add(Calendar.DAY_OF_MONTH, delta);

		// Start adding dates
		DateUtil.setTime(c, 8, 0, 0, 0);
		Date s = c.getTime();
		DateUtil.setTime(c, 10, 0, 0, 0);
		Date e = c.getTime();
		m.addItem(new BasicScheduleItem(newID(), s, e, "Network Problem", "KPN will fix the problem with the leased line, hopefully", null, null));

		c.add(Calendar.DAY_OF_MONTH, 1);
		DateUtil.setTime(c, 11, 30, 0, 0);
		s = c.getTime();
		DateUtil.setTime(c, 12, 10, 0, 0);
		e = c.getTime();
		m.addItem(new BasicScheduleItem(newID(), s, e, "Fysiotherapy", "Practice, practice", null, "img/cal/exercise.png"));

		c.add(Calendar.DAY_OF_MONTH, 1);
		DateUtil.setTime(c, 9, 45, 0, 0);
		s = c.getTime();
		DateUtil.setTime(c, 12, 0, 0, 0);
		e = c.getTime();
		m.addItem(new BasicScheduleItem(newID(), s, e, "Washing Car", "Clean out the car and make it proper", null, null));

		DateUtil.setTime(c, 15, 15, 0, 0);
		s = c.getTime();
		DateUtil.setTime(c, 17, 0, 0, 0);
		e = c.getTime();
		m.addItem(new BasicScheduleItem(newID(), s, e, "DomUI Presentation", "Present the current inner workings of DomUI", null, null));

		//-- Add a holiday
		c.add(Calendar.DAY_OF_MONTH, 1);
		m.addHoliday(new BasicScheduleHoliday(c.getTime(), "Frunnikday", "img/cal/christmas-bell.png"));

		return m;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Nieuwe afspraak dinges.								*/
	/*--------------------------------------------------------------*/

	public void	newAppt(Date dt, long dur) {
		final FloatingWindow floater = FloatingWindow.create(this, "New Appointment");
		floater.setHeight("200px");
		floater.setIcon("THEME/btnFind.png");

		TabularFormBuilder	f = new TabularFormBuilder();
		final DateInput	di = new DateInput();
		di.setValue(dt);
		f.addLabelAndControl("Afspraaktijd", di, true);
		final Text<Long>	dc	= new Text<Long>(Long.class);
		dc.setConverter(new SecondDurationConverter());
		dc.setValue(Long.valueOf(dur/1000));
		f.addLabelAndControl("Duur", dc, true);

		final Text<String>	sub = new Text<String>(String.class);
		f.addLabelAndControl("Omschrijving", sub, true);

		floater.add(f.finish());

		//-- Buttons
		DefaultButton b = new DefaultButton("Opslaan", "img/btnSave.png", new IClicked<DefaultButton>() {
			@Override
			public void clicked(DefaultButton bx) throws Exception {
				Calendar cal = Calendar.getInstance();
				cal.setTime(di.getValue());
				Long value = dc.getValue();
				if(null != value)
					cal.add(Calendar.SECOND, value.intValue());
				m_model.addItem(new BasicScheduleItem(newID(), di.getValue(), cal.getTime(), "Prive", sub.getValue(), null, "img/cal/exercise.png"));
				floater.close();
			}
		});
		floater.add(b);
	}

}
