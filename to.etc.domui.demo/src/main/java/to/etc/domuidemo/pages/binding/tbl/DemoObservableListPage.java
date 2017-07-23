package to.etc.domuidemo.pages.binding.tbl;

import java.util.*;

import javax.annotation.*;

import to.etc.domui.component.buttons.*;
import to.etc.domui.component.layout.*;
import to.etc.domui.component.misc.*;
import to.etc.domui.component.tbl.*;
import to.etc.domui.databinding.observables.*;
import to.etc.domui.derbydata.db.Album;
import to.etc.domui.derbydata.db.Artist;
import to.etc.domui.dom.html.*;
import to.etc.util.*;
import to.etc.webapp.query.*;

/**
 * Demo/test for {@link IObservableList} support in Hibernate relations.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Sep 4, 2013
 */
public class DemoObservableListPage extends UrlPage {
	final private Div m_lower = new Div();

	@Override
	public void createContent() throws Exception {
		QDataContext dc = getSharedContext();
		QCriteria<Artist> q = QCriteria.create(Artist.class);

		SimpleSearchModel<Artist> sm = new SimpleSearchModel<Artist>(this, q);
		RowRenderer<Artist> rr = new RowRenderer<Artist>(Artist.class);
		rr.column("name").label("Name");

		DataTable<Artist> dt = new DataTable<Artist>(sm, rr);
		add(dt);
		dt.setPageSize(10);
		add(new DataPager(dt));

		rr.setRowClicked(new ICellClicked<Artist>() {
			@Override
			public void cellClicked(Artist rowval) throws Exception {
				clickedOne(rowval);
			}
		});
		add(new VerticalSpacer(10));
		add(m_lower);

	}

	private void clickedOne(@Nonnull final Artist a) {
		List<Album> res = a.getAlbumList();
		System.out.println("Type is: " + res.getClass());

		m_lower.removeAllChildren();
		m_lower.add(new CaptionedHeader("Artist: " + a.getName()));

		final IObservableList<Album> ol = (IObservableList<Album>) res;

		RowRenderer<Album> rr = new RowRenderer<Album>(Album.class);
		rr.column("title").label("Title");

		DataTable<Album> dt = new DataTable<Album>(rr);
		m_lower.add(dt);
		dt.setList(ol);

		LinkButton lb = new LinkButton("Add album", "THEME/btnAdd.png", new IClicked<LinkButton>() {
			@Override
			public void clicked(LinkButton clickednode) throws Exception {
				addAlbum(a, ol);
			}
		});
		m_lower.add(lb);

		lb = new LinkButton("Delete album", "THEME/btnDelete.png", new IClicked<LinkButton>() {
			@Override
			public void clicked(LinkButton clickednode) throws Exception {
				deleteAlbum(a, ol);
			}
		});
		m_lower.add(lb);


	}

	private void deleteAlbum(@Nonnull Artist a, @Nonnull IObservableList<Album> ol) {
		if(ol.size() == 0)
			return;
		int ix = random(ol.size());
		ol.remove(ix);
	}

	private int random(int max) {
		if(max <= 0)
			return 0;
		Random r = new Random();
		return r.nextInt(max);
	}

	private void addAlbum(@Nonnull Artist a, @Nonnull IObservableList<Album> ol) {
		Album al = new Album();
		al.setArtist(a);
		al.setTitle(StringTool.getRandomStringWithPrefix(10, "NewAl-"));

		int index = random(ol.size());
		ol.add(index, al);
	}

}
