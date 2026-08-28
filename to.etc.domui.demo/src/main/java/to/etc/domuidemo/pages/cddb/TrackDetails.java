package to.etc.domuidemo.pages.cddb;

import to.etc.domui.annotations.UIUrlParameter;
import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component.misc.ExceptionDialog;
import to.etc.domui.component.misc.VerticalSpacer;
import to.etc.domui.component2.buttons.ButtonBar2;
import to.etc.domui.component2.combo.ComboLookup2;
import to.etc.domui.component2.form4.FormBuilder;
import to.etc.domui.converter.MsDurationConverter;
import to.etc.domui.derbydata.db.Genre;
import to.etc.domui.derbydata.db.MediaType;
import to.etc.domui.derbydata.db.Track;
import to.etc.domui.derbydata.db.Track_;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.Span;
import to.etc.domui.dom.html.UrlPage;
import to.etc.domui.state.UIGoto;
import to.etc.domui.themes.Theme;
import to.etc.webapp.nls.NlsContext;
import to.etc.webapp.query.QCriteria;

/**
 * A single track as it is sold: what it is, which album it came from and what it
 * costs. This is where the track search (see {@link CdCollection}) ends up.
 *
 * The album and the artist are shown but not editable here: a track moves to
 * another album by editing the album, not the track.
 */
public class TrackDetails extends UrlPage {
	private Track m_track;

	@Override
	public String getPageTitle() {
		return m_track.getName();
	}

	@Override
	public void createContent() throws Exception {
		ContentPanel cp = new ContentPanel();
		add(cp);
		cp.add(new HTag(1, m_track.getName()));

		FormBuilder fb = new FormBuilder(cp);
		fb.property(m_track, Track_.name()).mandatory().control();
		fb.property(m_track, Track_.composer()).control();

		//-- Where the track came from; maintained on the album itself.
		fb.property(m_track, Track_.album()).readOnly().control();
		fb.label("Artist").item(new Span(m_track.getAlbum().getArtist().getName()));

		fb.property(m_track, Track_.genre()).control(new ComboLookup2<>(QCriteria.create(Genre.class).ascending("name")));
		fb.property(m_track, Track_.mediaType()).control(new ComboLookup2<>(QCriteria.create(MediaType.class).ascending("name")));

		fb.label("Duration").item(new Span(new MsDurationConverter().convertObjectToString(NlsContext.getLocale(), m_track.getMilliseconds())));
		fb.property(m_track, Track_.unitPrice()).mandatory().control();

		cp.add(new VerticalSpacer(10));

		ButtonBar2 bb = new ButtonBar2();
		cp.add(bb);
		bb.addBackButton();
		bb.addLinkButton("Show the album", Theme.BTN_FIND, a -> UIGoto.moveSub(AlbumEditPage.class, "id", m_track.getAlbum().getId()));
		bb.addButton("Save", Theme.BTN_SAVE, a -> save());
	}

	private void save() throws Exception {
		if(bindErrors())
			return;
		try {
			getSharedContext().save(m_track);
			getSharedContext().commit();
			UIGoto.back();
		} catch(Exception x) {
			ExceptionDialog.create(this, "Save failed", x);
		}
	}

	@UIUrlParameter(name = "id")
	public Track getTrack() {
		return m_track;
	}

	public void setTrack(Track track) {
		m_track = track;
	}
}
