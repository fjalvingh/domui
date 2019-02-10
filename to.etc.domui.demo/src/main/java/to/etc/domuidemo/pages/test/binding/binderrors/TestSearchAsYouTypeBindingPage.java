package to.etc.domuidemo.pages.test.binding.binderrors;

import to.etc.domui.component.buttons.DefaultButton;
import to.etc.domui.component.input.SearchAsYouType;
import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component.layout.MessageLine;
import to.etc.domui.component.misc.VerticalSpacer;
import to.etc.domui.dom.errors.MsgType;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.UrlPage;
import to.etc.util.DateUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 18-2-18.
 */
public class TestSearchAsYouTypeBindingPage extends UrlPage {
	private Date m_date;

	@Override public void createContent() throws Exception {
		add(new MessageLine(MsgType.INFO, "Type a year-month pair, like 02-2018"));

		ContentPanel cp = new ContentPanel();
		add(cp);

		List<Date> list = new ArrayList<>();
		Calendar cal = Calendar.getInstance();
		DateUtil.clearTime(cal);
		cal.set(Calendar.DAY_OF_MONTH, 1);
		for(int i = 0; i < 24; i++) {
			cal.add(Calendar.MONTH, -1);
			list.add(cal.getTime());
		}
		SearchAsYouType<Date> st = new SearchAsYouType<>(Date.class)
			.setData(list)
			.setSearchProperty("name")
			.setConverter((a, v) -> {
				SimpleDateFormat f = new SimpleDateFormat("MM-yyyy");
				return f.format(v);
			})
			;
		st.setMandatory(true);
		cp.add(st);
		st.bind().to(this, "date");

		Div d = new Div();
		cp.add(d);

		cp.add(new VerticalSpacer(10));
		DefaultButton b = new DefaultButton("validate", a -> {
			Div res = new Div();
			add(res);
			res.add("Result is " + st.getValue() + " and with binding " + getDate());
		});
		cp.add(b);
	}

	public Date getDate() {
		return m_date;
	}

	public void setDate(Date date) {
		m_date = date;
	}
}
