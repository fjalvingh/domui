package to.etc.domuidemo.pages.components.agenda;

import to.etc.domui.component.agenda.BasicScheduleHoliday;
import to.etc.domui.component.agenda.BasicScheduleItem;
import to.etc.domui.component.agenda.BasicScheduleModel;
import to.etc.domui.component.agenda.DefaultScheduleItemRenderer;
import to.etc.domui.component.agenda.ScheduleItem;
import to.etc.domui.component.agenda.ScheduleMode;
import to.etc.domui.component.agenda.WeekAgendaComponent;
import to.etc.domui.component.buttons.DefaultButton;
import to.etc.domui.component.input.Text2;
import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component.layout.InputDialog;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.NodeContainer;
import to.etc.domui.dom.html.Para;
import to.etc.domui.dom.html.UrlPage;
import to.etc.util.DateUtil;

import java.util.Calendar;
import java.util.Date;

/**
 * WeekAgendaComponent: a week of appointments, on a raster of half hours.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class WeekAgendaPage extends UrlPage {
	/** The appointments shown. The model is state, so it is a field; the components are not. */
	private BasicScheduleModel<ScheduleItem> m_model;

	/** The day the agenda is showing; the component moves it to the start of its period. */
	private Date m_date = new Date();

	private ScheduleMode m_mode = ScheduleMode.WORKWEEK;

	private int m_idgen;

	@Override
	public void createContent() throws Exception {
		setPageTitle("WeekAgendaComponent");

		ContentPanel cp = new ContentPanel();
		add(cp);
		cp.add(new HTag(1, "WeekAgendaComponent"));

		if(m_model == null)
			m_model = createModel();

		//-- The period buttons: they only change the date field and rebuild.
		Div buttons = new Div("dm-tut");
		cp.add(buttons);
		buttons.add(new DefaultButton("Previous", a -> moveDays(-7)));
		buttons.add(new DefaultButton("Today", a -> {
			m_date = new Date();
			forceRebuild();
		}));
		buttons.add(new DefaultButton("Next", a -> moveDays(7)));
		buttons.add(new DefaultButton("Day", a -> setMode(ScheduleMode.DAY)));
		buttons.add(new DefaultButton("Work week", a -> setMode(ScheduleMode.WORKWEEK)));
		buttons.add(new DefaultButton("Week", a -> setMode(ScheduleMode.WEEK)));

		WeekAgendaComponent<ScheduleItem> agenda = new WeekAgendaComponent<>();
		cp.add(agenda);
		agenda.setMode(m_mode);
		agenda.setDate(m_date);
		agenda.setModel(m_model);

		//-- Give every appointment the colour of its type.
		agenda.setItemRenderer(new DefaultScheduleItemRenderer<>() {
			@Override
			public void render(WeekAgendaComponent<ScheduleItem> component, NodeContainer target, ScheduleItem item) throws Exception {
				super.render(component, target, item);
				if(item.getType() != null)
					target.addCssClass("dm-agenda-" + item.getType());
			}
		});

		//-- Dragging over an empty part of the raster asks for a new appointment.
		agenda.setNewAppointmentListener((start, duration) -> askNewAppointment(start, duration));

		cp.add(new Para().add("The agenda shows the appointments a ScheduleModel hands it, on a "
			+ "raster of half hours running from an hour before the first work hour of the model to "
			+ "an hour after the last. The buttons above move the period and switch between a day, "
			+ "the work week and the full week."));

		cp.add(new Para().add("Drag over an empty piece of the raster to make an appointment there: "
			+ "the component asks its INewAppointment listener for one, and this page asks for a "
			+ "description and adds it to the model. Adding to the model is enough - the component "
			+ "listens to it, so the new appointment appears without the page being rebuilt."));

		cp.add(new Para().add("The item renderer decides what an appointment looks like. This one "
			+ "calls the default renderer and then adds a css class taken from the appointment's "
			+ "type, which is where the colours come from."));
	}

	private void setMode(ScheduleMode mode) {
		m_mode = mode;
		forceRebuild();
	}

	private void moveDays(int days) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(m_date);
		cal.add(Calendar.DAY_OF_MONTH, days);
		m_date = cal.getTime();
		forceRebuild();
	}

	/**
	 * Ask for the description of the appointment that was dragged on the raster, then add it.
	 */
	private void askNewAppointment(Date start, long duration) throws Exception {
		Text2<String> what = new Text2<>(String.class);
		what.setMandatory(true);

		add(new InputDialog<>(what, "New appointment", "What is it?") {
			@Override
			protected boolean onSaveData(String value) throws Exception {
				Date end = new Date(start.getTime() + duration);
				m_model.addItem(new BasicScheduleItem(nextId(), start, end, value, null, "meeting", null));
				return true;
			}
		});
	}

	private String nextId() {
		return Integer.toString(++m_idgen);
	}

	/**
	 * The schedule: work hours for every day shown, a holiday and a few appointments.
	 */
	private BasicScheduleModel<ScheduleItem> createModel() throws Exception {
		BasicScheduleModel<ScheduleItem> model = new BasicScheduleModel<>();

		//-- Work hours: 08:30-12:30 and 13:00-17:30, for the four weeks around today.
		Calendar cal = Calendar.getInstance();
		DateUtil.clearTime(cal);
		cal.add(Calendar.DAY_OF_MONTH, -14);
		for(int i = 0; i < 28; i++) {
			model.addWorkHour(at(cal, 8, 30), at(cal, 12, 30));
			model.addWorkHour(at(cal, 13, 0), at(cal, 17, 30));
			cal.add(Calendar.DAY_OF_MONTH, 1);
		}

		//-- The appointments are placed relative to the monday of this week.
		Calendar monday = Calendar.getInstance();
		DateUtil.clearTime(monday);
		while(monday.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY)
			monday.add(Calendar.DAY_OF_MONTH, -1);

		addItem(model, monday, 0, 9, 0, 10, 30, "Leased line repair", "KPN said they would be there in the morning", "problem");
		addItem(model, monday, 1, 11, 30, 12, 10, "Physiotherapy", "Practice, practice", "private");
		addItem(model, monday, 2, 9, 45, 12, 0, "Washing the car", "It has not been this colour in a while", "private");
		addItem(model, monday, 2, 15, 15, 17, 0, "DomUI presentation", "The inner workings, for whoever will listen", "meeting");
		addItem(model, monday, 3, 10, 0, 11, 0, "Release meeting", null, "meeting");

		//-- And a holiday, on the thursday.
		Calendar holiday = (Calendar) monday.clone();
		holiday.add(Calendar.DAY_OF_MONTH, 3);
		model.addHoliday(new BasicScheduleHoliday(holiday.getTime(), "Ascension day", "img/cal/christmas-bell.png"));
		return model;
	}

	private void addItem(BasicScheduleModel<ScheduleItem> model, Calendar monday, int dayOffset, int sh, int sm, int eh, int em,
		String name, String details, String type) throws Exception {
		Calendar cal = (Calendar) monday.clone();
		cal.add(Calendar.DAY_OF_MONTH, dayOffset);
		model.addItem(new BasicScheduleItem(nextId(), at(cal, sh, sm), at(cal, eh, em), name, details, type, null));
	}

	private static Date at(Calendar cal, int hour, int minute) {
		Calendar c = (Calendar) cal.clone();
		DateUtil.setTime(c, hour, minute, 0, 0);
		return c.getTime();
	}
}
