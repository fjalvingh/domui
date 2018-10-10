package to.etc.domuidemo.pages.cddb;

import to.etc.domui.annotations.UIUrlParameter;
import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component.misc.ExceptionDialog;
import to.etc.domui.component2.buttons.ButtonBar2;
import to.etc.domui.component2.form4.FormBuilder;
import to.etc.domui.derbydata.db.Album;
import to.etc.domui.derbydata.db.Album_;
import to.etc.domui.dom.html.UrlPage;
import to.etc.domui.state.UIGoto;
import to.etc.domui.themes.Theme;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 10-10-18.
 */
public class AlbumEditPage extends UrlPage {
	private Album m_album;

	@Override public String getPageTitle() {
		return m_album.getId() == null ? "New Album" : m_album.getTitle();
	}

	@Override public void createContent() throws Exception {
		ContentPanel cp = new ContentPanel();
		add(cp);

		FormBuilder fb = new FormBuilder(cp);
		fb.property(m_album, Album_.artist()).control();
		fb.property(m_album, Album_.title()).control();

		ButtonBar2 bb = new ButtonBar2();
		add(bb);
		bb.addButton("Save", Theme.BTN_SAVE, a -> save());
	}

	@UIUrlParameter(name = "id")
	public Album getAlbum() {
		return m_album;
	}

	public void setAlbum(Album album) {
		m_album = album;
	}

	private void save() throws Exception {
		if(bindErrors())
			return;

		try {
			getSharedContext().save(m_album);
			getSharedContext().commit();
			UIGoto.back();
		} catch(Exception x) {
			ExceptionDialog.create(this, "Save failed", x);
		}
	}
}
