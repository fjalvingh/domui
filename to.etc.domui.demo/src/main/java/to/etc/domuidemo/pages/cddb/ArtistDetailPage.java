package to.etc.domuidemo.pages.cddb;

import to.etc.domui.annotations.UIUrlParameter;
import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component.masterchild.ChildFragment;
import to.etc.domui.component.misc.ExceptionDialog;
import to.etc.domui.component.misc.VerticalSpacer;
import to.etc.domui.component2.buttons.ButtonBar2;
import to.etc.domui.component2.form4.FormBuilder;
import to.etc.domui.derbydata.db.Album;
import to.etc.domui.derbydata.db.Album_;
import to.etc.domui.derbydata.db.Artist;
import to.etc.domui.derbydata.db.Artist_;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.UrlPage;
import to.etc.domui.state.UIGoto;
import to.etc.domui.themes.Theme;

/**
 * An artist, with the albums we have of that artist as the detail part: the
 * master/detail screen a shop would actually use to look after its catalogue.
 * Both the artist's fields and the album list are bound to the record, so
 * editing a field and pressing Save is all that is needed.
 */
public class ArtistDetailPage extends UrlPage {
	private Artist m_artist;

	@Override
	public String getPageTitle() {
		return m_artist.getId() == null ? "New artist" : m_artist.getName();
	}

	@Override
	public void createContent() throws Exception {
		ContentPanel cp = new ContentPanel();
		add(cp);
		cp.add(new HTag(1, "Artist"));

		FormBuilder fb = new FormBuilder(cp);
		fb.property(m_artist, Artist_.name()).mandatory().control();

		cp.add(new VerticalSpacer(10));
		cp.add(new HTag(2, "Albums by this artist"));

		//-- The detail part: bound to the artist's album list, so it follows the record.
		ChildFragment<Artist, Album> albums = new ChildFragment<>(m_artist, Artist_.albumList());
		cp.add(albums);
		albums.column(Album_.title()).ascending().sortdefault();
		albums.column().label("Tracks").renderer((node, album) -> node.add(Integer.toString(album.getTrackList().size())));
		albums.onClick(album -> UIGoto.moveSub(AlbumEditPage.class, "id", album.getId()));

		ButtonBar2 bb = new ButtonBar2();
		cp.add(bb);
		bb.addBackButton();
		bb.addButton("Save", Theme.BTN_SAVE, a -> save());
	}

	private void save() throws Exception {
		if(bindErrors())
			return;
		try {
			getSharedContext().save(m_artist);
			getSharedContext().commit();
			UIGoto.back();
		} catch(Exception x) {
			ExceptionDialog.create(this, "Save failed", x);
		}
	}

	@UIUrlParameter(name = "id")
	public Artist getArtist() {
		return m_artist;
	}

	public void setArtist(Artist artist) {
		m_artist = artist;
	}
}
