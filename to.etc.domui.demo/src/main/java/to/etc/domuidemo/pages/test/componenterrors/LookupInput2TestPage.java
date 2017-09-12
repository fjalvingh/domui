package to.etc.domuidemo.pages.test.componenterrors;

import to.etc.domui.component.buttons.DefaultButton;
import to.etc.domui.component.meta.MetaProperty;
import to.etc.domui.component.meta.YesNoType;
import to.etc.domui.component2.form4.FormBuilder;
import to.etc.domui.component2.lookupinput.LookupInput2;
import to.etc.domui.derbydata.db.Album;
import to.etc.domui.derbydata.db.Artist;
import to.etc.domui.dom.html.UrlPage;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 29-8-17.
 */
public class LookupInput2TestPage extends UrlPage {
	private Artist m_artist;

	private Album m_album;

	@Override public void createContent() throws Exception {
		/*
		 * This input uses Artist which allows no quick lookup, only search form lookup.
		 */
		LookupInput2<Artist> li = new LookupInput2<>(Artist.class);
		FormBuilder fb = new FormBuilder(this);
		li.setTestID("one");
		li.setMandatory(true);
		fb.property(this, "artist").control(li);

		/*
		 * This input uses Album which allows no quick lookup, only search form lookup.
		 */
		LookupInput2<Album> li2 = new LookupInput2<>(Album.class);
		li2.setTestID("two");
		li2.setMandatory(true);
		fb.property(this, "album").control(li2);

		DefaultButton validate = new DefaultButton("validate", a -> validate());
		add(validate);
	}

	private void validate() throws Exception {
		bindErrors();
	}

	@MetaProperty(required = YesNoType.YES)
	public Artist getArtist() {
		return m_artist;
	}

	public void setArtist(Artist artist) {
		m_artist = artist;
	}

	@MetaProperty(required = YesNoType.YES)
	public Album getAlbum() {
		return m_album;
	}

	public void setAlbum(Album album) {
		m_album = album;
	}
}
