package to.etc.domuidemo.pages.components.input;

import to.etc.domui.component.input.DateInput2;
import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component2.form4.FormBuilder;
import to.etc.domui.converter.ConverterRegistry;
import to.etc.domui.converter.DateConverter;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.Para;
import to.etc.domui.dom.html.UrlPage;
import to.etc.webapp.nls.NlsContext;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * DateInput2: a date, a date and time, and the calendar and today buttons.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class DateInput2Page extends UrlPage {
	@Override
	public void createContent() throws Exception {
		setPageTitle("DateInput2: entering a date");

		ContentPanel cp = new ContentPanel();
		add(cp);
		cp.add(new HTag(1, "DateInput2: entering a date"));

		Div shown = new Div("dm-tut");
		shown.add("Change one of the fields to see the Date it produced.");

		DateInput2 date = new DateInput2();
		date.setValue(new Date());
		date.setOnValueChanged(a -> show(shown, "Date only", date.getValue()));

		DateInput2 dateTime = new DateInput2(true);
		dateTime.setOnValueChanged(a -> show(shown, "Date and time", dateTime.getValue()));

		DateInput2 seconds = new DateInput2(true);
		seconds.setWithSeconds(true);
		seconds.setOnValueChanged(a -> show(shown, "With seconds", seconds.getValue()));

		DateInput2 noToday = new DateInput2();
		noToday.setHideTodayButton(true);

		DateInput2 readOnly = new DateInput2();
		readOnly.setValue(new Date());
		readOnly.setReadOnly(true);

		DateInput2 disabled = new DateInput2(true);
		disabled.setValue(new Date());
		disabled.setDisabled(true);

		FormBuilder fb = new FormBuilder(cp);
		fb.label("Released").control(date);
		fb.label("Ordered at (with time)").control(dateTime);
		fb.label("Logged at (with seconds)").control(seconds);
		fb.label("setHideTodayButton(true)").control(noToday);
		fb.label("setReadOnly(true)").control(readOnly);
		fb.label("setDisabled(true)").control(disabled);
		cp.add(shown);

		cp.add(new Para().add("The first button opens a calendar, the second fills in today. "
			+ "Both disappear when the control is read only and grey out when it is disabled."));

		//-- Which format this control accepts depends on the locale of the request.
		Calendar cal = Calendar.getInstance();
		cal.set(2013, Calendar.FEBRUARY, 5, 14, 30, 0);
		String sample = ConverterRegistry.getConverterInstance(DateConverter.class)
			.convertObjectToString(NlsContext.getLocale(), cal.getTime());

		cp.add(new HTag(2, "The format follows the locale"));
		Div locale = new Div("dm-tut-q");
		locale.add("Locale of this request: " + NlsContext.getLocale()
			+ "\n5 February 2013 is written as: " + sample);
		cp.add(locale);

		cp.add(new Para().add("Dutch is the one locale with a parser that also takes the "
			+ "short forms: 13/3/2012, 13-3-12, 13/3 (this year) and 13032012 all arrive "
			+ "as the same date. In an English locale the format is yyyy-MM-dd and it is "
			+ "read strictly; every other locale uses the JDK's short format for it."));
	}

	private static void show(Div into, String what, Date value) {
		into.removeAllChildren();
		into.add(what + ": " + (value == null
			? "null"
			: new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(value)));
	}
}
