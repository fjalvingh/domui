package to.etc.domuidemo.pages.tutorial.database;

import to.etc.domui.component.buttons.DefaultButton;
import to.etc.domui.component.input.Text2;
import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component2.form4.FormBuilder;
import to.etc.domui.derbydata.db.Track;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.UrlPage;
import to.etc.webapp.query.QCriteria;
import to.etc.webapp.query.QRestrictorImpl;

import java.util.List;

/**
 * Tutorial, "using databases", step 2: restrictions are combined with "and", and
 * or() returns a restrictor that combines with "or".
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class QueryRestrictionsPage extends UrlPage {
	@Override
	public void createContent() throws Exception {
		setPageTitle("Restrictions and combinators");

		ContentPanel cp = new ContentPanel();
		add(cp);
		cp.add(new HTag(1, "Restrictions and combinators"));

		Text2<String> word = new Text2<>(String.class);
		word.setValue("brown");
		Text2<Integer> minutes = new Text2<>(Integer.class);
		minutes.setValue(4);

		Div query = new Div("dm-tut-q");
		Div result = new Div("dm-tut");

		FormBuilder fb = new FormBuilder(cp);
		fb.label("Track name or composer contains").control(word);
		fb.label("Lasting at least (minutes)").control(minutes);

		cp.add(new DefaultButton("Search", a -> search(word, minutes, query, result)));
		cp.add(query);
		cp.add(result);
		search(word, minutes, query, result);
	}

	private void search(Text2<String> word, Text2<Integer> minutes, Div query, Div result) throws Exception {
		QCriteria<Track> q = QCriteria.create(Track.class);

		String wordValue = word.getValueSafe();
		if(wordValue != null) {
			//-- Everything added to this restrictor is combined with "or".
			QRestrictorImpl<Track> or = q.or();
			or.ilike("name", "%" + wordValue + "%");
			or.ilike("composer", "%" + wordValue + "%");
		}

		Integer minutesValue = minutes.getValueSafe();
		if(minutesValue != null) {
			//-- Added to the query itself, so combined with the above using "and".
			q.ge("milliseconds", minutesValue.longValue() * 60000L);
		}
		q.ascending("name");
		q.limit(20);

		query.removeAllChildren();
		query.add(q.toString());

		List<Track> trackList = getSharedContext().query(q);
		result.removeAllChildren();
		result.add(new HTag(2, trackList.size() == 1 ? "1 track" : trackList.size() + " tracks"));
		for(Track track : trackList) {
			Div line = new Div();
			result.add(line);
			line.add(track.getName() + " (" + track.getMilliseconds() / 60000 + " min) - " + track.getComposer());
		}
	}
}
