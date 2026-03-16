package to.etc.domuidemo.pages.binding.tbl;

import org.eclipse.jdt.annotation.NonNull;
import to.etc.domui.component.buttons.LinkButton;
import to.etc.domui.component.layout.CaptionedHeader;
import to.etc.domui.component.misc.Icon;
import to.etc.domui.component.misc.VerticalSpacer;
import to.etc.domui.component.tbl.DataPager;
import to.etc.domui.component.tbl.DataTable;
import to.etc.domui.component.tbl.RowRenderer;
import to.etc.domui.component.tbl.SimpleSearchModel;
import to.etc.domui.databinding.observables.IObservableList;
import to.etc.domui.derbydata.db.Album;
import to.etc.domui.derbydata.db.Artist;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.IClicked;
import to.etc.domui.dom.html.UrlPage;
import to.etc.util.StringTool;
import to.etc.webapp.query.QCriteria;

import java.util.List;
import java.util.Random;

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
		QCriteria<Artist> q = QCriteria.create(Artist.class);

		SimpleSearchModel<Artist> sm = new SimpleSearchModel<>(this, q);
		RowRenderer<Artist> rr = new RowRenderer<>(Artist.class);
		rr.column("name").label("Name");

		DataTable<Artist> dt = new DataTable<>(sm, rr);
		add(dt);
		dt.setPageSize(10);
		add(new DataPager(dt));

		rr.setRowClicked(rowval -> clickedOne(rowval));
		add(new VerticalSpacer(10));
		add(m_lower);

	}

	private void clickedOne(@NonNull final Artist a) {
		List<Album> res = a.getAlbumList();
		System.out.println("Type is: " + res.getClass());

		m_lower.removeAllChildren();
		m_lower.add(new CaptionedHeader("Artist: " + a.getName()));

		final IObservableList<Album> ol = (IObservableList<Album>) res;

		RowRenderer<Album> rr = new RowRenderer<>(Album.class);
		rr.column("title").label("Title");

		DataTable<Album> dt = new DataTable<>(rr);
		m_lower.add(dt);
		dt.setList(ol);

		LinkButton lb = new LinkButton("Add album", Icon.of("THEME/btnAdd.png"), clickednode -> addAlbum(a, ol));
		m_lower.add(lb);

		lb = new LinkButton("Delete album", Icon.of("THEME/btnDelete.png"), clickednode -> deleteAlbum(ol));
		m_lower.add(lb);


	}

	private void deleteAlbum(@NonNull IObservableList<Album> ol) {
		if(ol.isEmpty())
			return;
		int ix = random(ol.size());
		ol.remove(ix);
	}

	@SuppressWarnings("squid:S2245")
	private final Random m_random = new Random();

	private int random(int max) {
		if(max <= 0)
			return 0;
		return m_random.nextInt(max);
	}

	private void addAlbum(@NonNull Artist a, @NonNull IObservableList<Album> ol) {
		Album al = new Album();
		al.setArtist(a);
		al.setTitle(StringTool.getRandomStringWithPrefix(10, "NewAl-"));

		int index = random(ol.size());
		ol.add(index, al);
	}

}
