package to.etc.domuidemo.pages.components.agenda;

import to.etc.domui.component.agenda.MonthPanel;
import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.Para;
import to.etc.domui.dom.html.UrlPage;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * MonthPanel: a month as a small calendar, to pick a day from.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class MonthPanelPage extends UrlPage {
	/** The day that was picked, if any. State, so the rebuild after a click can show it again. */
	private Date m_selected;

	@Override
	public void createContent() throws Exception {
		setPageTitle("MonthPanel");

		ContentPanel cp = new ContentPanel();
		add(cp);
		cp.add(new HTag(1, "MonthPanel"));

		Div frame = new Div("dm-tut");
		cp.add(frame);

		Calendar cal = Calendar.getInstance();
		for(int i = 0; i < 2; i++) {
			MonthPanel mp = new MonthPanel();
			frame.add(mp);
			mp.setDate(cal.getTime());
			mp.setDayClicked((panel, date) -> {
				m_selected = date;
				forceRebuild();
			});
			if(m_selected != null)
				mp.setMarked(m_selected, null);              // Only the panel holding that day marks it
			cal.add(Calendar.MONTH, 1);
		}

		Div picked = new Div("dm-tut-q");
		cp.add(picked);
		if(m_selected == null) {
			picked.add("Click a day.");
		} else {
			picked.add(DateFormat.getDateInstance(DateFormat.FULL).format(m_selected));
		}

		cp.add(new Para().add("Two panels, each showing a month around today. A panel is a table of "
			+ "week rows: the week number, then the seven days of that week, with the days of the "
			+ "month before and after the one shown greyed out."));

		cp.add(new Para().add("Days are only clickable when a day click handler is set - without "
			+ "one the panel is a calendar to look at, and the cells get neither the pointer nor the "
			+ "hover. The handler here keeps the clicked date in a field of the page and rebuilds, "
			+ "and the new panels mark that date with setMarked()."));

		cp.add(new Para().add("setMarked(date, css) adds a css class to the cell of a day, "
			+ "unmarkAll(css) takes it off every cell again; passing no class uses MonthPanel.MARKED. "
			+ "That is how an application shows which days have something on them."));
	}
}
