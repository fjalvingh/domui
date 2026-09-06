package to.etc.domuidemo.pages.test.binding.binderrors;

import to.etc.domui.component.buttons.DefaultButton;
import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component.meta.MetaProperty;
import to.etc.domui.component.meta.YesNoType;
import to.etc.domui.component2.lookupinput.LookupInput2;
import to.etc.domui.derbydata.db.Artist;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.NodeContainer;
import to.etc.domui.dom.html.UrlPage;
import to.etc.webapp.query.QCriteria;

import java.util.List;

/**
 * This tests the following:
 * - add a mandatory LookupInput2 control and bind it to a field with an initial value
 * - clear the LookupInput2
 * - call the binding errors
 *
 * Expected result:
 * - the mandatory error is shown
 * - the bound field is set to null
 *
 * Original bug:
 * - the mandatory error was shown
 * - but the bound field retained its old value.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 3-4-18.
 */
public class BindError2Page extends UrlPage {
	private Artist m_artist;

	@Override public void createContent() throws Exception {
		ContentPanel cp = new ContentPanel();
		add(cp);

		LookupInput2<Artist> li = new LookupInput2<>(QCriteria.create(Artist.class));
		cp.add(li);

		List<Artist> artists = getSharedContext().query(QCriteria.create(Artist.class).limit(1));
		m_artist = artists.get(0);

		li.setMandatory(true);
		li.setTestID("edit");
		li.bind().to(this, "artist");

		cp.add(new DefaultButton("click", a-> save(cp)));
	}

	private void save(NodeContainer target) throws Exception {
		if(bindErrors()) {                            // Make sure bind errors are calculated
			//-- If we have bind errors we expect the control to have set the binding to null
			Artist artist = getArtist();
			if(null != artist)
				target.add(new Div("failed", "The binding has still the previous value: " + artist));
			else
				target.add(new Div("success", "Binding is null as it should be"));
		}
	}

	@MetaProperty(required = YesNoType.YES)
	public Artist getArtist() {
		return m_artist;
	}

	public void setArtist(Artist artist) {
		m_artist = artist;
	}
}
