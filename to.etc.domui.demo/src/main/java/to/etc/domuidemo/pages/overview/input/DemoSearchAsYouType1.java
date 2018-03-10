package to.etc.domuidemo.pages.overview.input;

import to.etc.domui.component.buttons.DefaultButton;
import to.etc.domui.component.input.SearchAsYouType;
import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component.layout.MessageLine;
import to.etc.domui.component.misc.VerticalSpacer;
import to.etc.domui.derbydata.db.Genre;
import to.etc.domui.dom.errors.MsgType;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.UrlPage;
import to.etc.webapp.query.QCriteria;

import java.util.List;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 18-2-18.
 */
public class DemoSearchAsYouType1 extends UrlPage {
	@Override public void createContent() throws Exception {
		ContentPanel cp = new ContentPanel();
		add(cp);

		cp.add(new MessageLine(MsgType.INFO, "Type a music genre like rock"));

		//-- Make a set of genres
		List<Genre> genreList = getSharedContext().query(QCriteria.create(Genre.class));
		SearchAsYouType<Genre> st = new SearchAsYouType<>(Genre.class)
			.setData(genreList)
			.setSearchProperty("name")
			;
		st.setMandatory(true);
		cp.add(st);

		Div d = new Div();
		cp.add(d);

		cp.add(new VerticalSpacer(10));
		DefaultButton b = new DefaultButton("validate", a -> {
			Div res = new Div();
			add(res);
			res.add("Result is " + st.getValue());
		});
		cp.add(b);
	}
}
