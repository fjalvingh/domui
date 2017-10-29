package to.etc.domuidemo.pages.test.componenterrors;

import to.etc.domui.component.buttons.DefaultButton;
import to.etc.domui.component.input.LookupInput;
import to.etc.domui.component.meta.MetaProperty;
import to.etc.domui.component.meta.YesNoType;
import to.etc.domui.component2.form4.FormBuilder;
import to.etc.domui.derbydata.db.Album;
import to.etc.domui.derbydata.db.Artist;
import to.etc.domui.dom.html.Div;
import to.etc.webapp.query.QCriteria;

import java.util.Date;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 29-10-17.
 */
public class LookupInput1Fragment extends Div {
	private Artist m_artist;

	private Album m_album;

	private Album m_album3;

	private Album m_album4;

	private Artist m_artist2;

	private Album m_album2;

	private Album m_album22;

	private Album m_album23;

	private String m_text = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaargh";

	private String m_memo = "bbbbbbbbbbbbbbbbbbbbbbbbrgh";

	private Date m_date = new Date();

	@Override public void createContent() throws Exception {
		QCriteria<Album> q = QCriteria.create(Album.class).eq("title", "Angel Dust");
		m_album4 = m_album3 = m_album22 = m_album23 = getSharedContext().queryOne(q);

		FormBuilder fb = new FormBuilder(this);

		//-- LookupInput
		LookupInput<Artist> li = new LookupInput<>(Artist.class);
		li.setTestID("one");
		li.setMandatory(true);
		fb.property(this, "artist").control(li);

		// This input uses Album which allows no quick lookup, only search form lookup.
		LookupInput<Album> li2 = new LookupInput<>(Album.class);
		li2.setTestID("two");
		li2.setMandatory(true);
		fb.property(this, "album").control(li2);

		LookupInput<Album> li7 = new LookupInput<>(Album.class);
		li7.setTestID("seven");
		li7.setMandatory(true);
		fb.property(this, "album3").control(li7);

		LookupInput<Album> li8 = new LookupInput<>(Album.class);
		li8.setValueColumns("title", "artist.name");
		li8.setTestID("eight");
		li8.setMandatory(true);
		fb.property(this, "album4").control(li8);

		LookupInput<Album> li4 = new LookupInput<>(Album.class);
		li4.setTestID("four");
		li4.setMandatory(true);
		fb.property(this, "album2").control(li4);

		LookupInput<Album> li22 = new LookupInput<>(Album.class);
		li22.setTestID("l22");
		li22.setMandatory(true);
		fb.property(this, "album22").control(li22);

		LookupInput<Album> li23 = new LookupInput<>(Album.class);
		li23.setValueColumns("title", "artist.name");
		li23.setTestID("l23");
		li23.setMandatory(true);
		fb.property(this, "album23").control(li23);

		DefaultButton validate = new DefaultButton("validate", a -> bindErrors());
		add(validate);
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

	@MetaProperty(required = YesNoType.YES)
	public Artist getArtist2() {
		return m_artist2;
	}

	public void setArtist2(Artist artist2) {
		m_artist2 = artist2;
	}

	@MetaProperty(required = YesNoType.YES)
	public Album getAlbum2() {
		return m_album2;
	}

	public void setAlbum2(Album album2) {
		m_album2 = album2;
	}

	@MetaProperty(required = YesNoType.YES, length = 70)
	public String getText() {
		return m_text;
	}

	@Override public void setText(String text) {
		m_text = text;
	}

	@MetaProperty(required = YesNoType.YES)
	public Date getDate() {
		return m_date;
	}

	public void setDate(Date date) {
		m_date = date;
	}

	@MetaProperty(required = YesNoType.YES)
	public String getMemo() {
		return m_memo;
	}

	public void setMemo(String memo) {
		m_memo = memo;
	}

	@MetaProperty(required = YesNoType.YES)
	public Album getAlbum3() {
		return m_album3;
	}

	public void setAlbum3(Album album3) {
		m_album3 = album3;
	}

	@MetaProperty(required = YesNoType.YES)
	public Album getAlbum4() {
		return m_album4;
	}

	public void setAlbum4(Album album4) {
		m_album4 = album4;
	}

	@MetaProperty(required = YesNoType.YES)
	public Album getAlbum22() {
		return m_album22;
	}

	public void setAlbum22(Album album22) {
		m_album22 = album22;
	}

	@MetaProperty(required = YesNoType.YES)
	public Album getAlbum23() {
		return m_album23;
	}

	public void setAlbum23(Album album23) {
		m_album23 = album23;
	}
}
