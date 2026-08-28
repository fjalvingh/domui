package to.etc.domuidemo.pages.cddb;

import to.etc.domui.component.input.Text2;
import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component.layout.TabPanel;
import to.etc.domui.component.misc.ExceptionDialog;
import to.etc.domui.component.misc.Icon;
import to.etc.domui.component.tbl.DataTable;
import to.etc.domui.component.tbl.IRowControlFactory;
import to.etc.domui.component.tbl.RowRenderer;
import to.etc.domui.component.tbl.SimpleListModel;
import to.etc.domui.component2.buttons.ButtonBar2;
import to.etc.domui.derbydata.db.Genre;
import to.etc.domui.derbydata.db.Genre_;
import to.etc.domui.derbydata.db.MediaType;
import to.etc.domui.derbydata.db.MediaType_;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.UrlPage;
import to.etc.domui.themes.Theme;
import to.etc.webapp.query.QCriteria;
import to.etc.webapp.query.QField;

import java.util.List;
import java.util.function.Supplier;

/**
 * The two lookup tables behind the catalogue: the genres and the media types a
 * track can have. Both are short lists that are edited in place rather than
 * through a separate screen per record - each row's control is bound straight to
 * the record it shows, so editing a cell changes the record.
 */
public class ReferenceDataPage extends UrlPage {
	private SimpleListModel<Genre> m_genreModel;

	private SimpleListModel<MediaType> m_mediaTypeModel;

	@Override
	public String getPageTitle() {
		return "Genres and media types";
	}

	@Override
	public void createContent() throws Exception {
		ContentPanel cp = new ContentPanel();
		add(cp);
		cp.add(new HTag(1, "Genres and media types"));

		List<Genre> genreList = getSharedContext().query(QCriteria.create(Genre.class).ascending(Genre_.name()));
		SimpleListModel<Genre> genreModel = m_genreModel = new SimpleListModel<>(genreList);

		List<MediaType> mediaList = getSharedContext().query(QCriteria.create(MediaType.class).ascending(MediaType_.name()));
		SimpleListModel<MediaType> mediaModel = m_mediaTypeModel = new SimpleListModel<>(mediaList);

		TabPanel tp = new TabPanel();
		cp.add(tp);
		tp.add(createTab(Genre.class, Genre_.name(), genreModel, Genre::new), "Genres");
		tp.add(createTab(MediaType.class, MediaType_.name(), mediaModel, MediaType::new), "Media types");

		ButtonBar2 bb = new ButtonBar2();
		cp.add(bb);
		bb.addBackButton();
		bb.addButton("Save", Theme.BTN_SAVE, a -> save());
	}

	/**
	 * Both lookup tables have the same shape - a name and nothing else - so one
	 * method builds the maintenance table for either of them.
	 */
	private <T> Div createTab(Class<T> dataClass, QField<T, String> nameProperty, SimpleListModel<T> model, Supplier<T> factory) throws Exception {
		Div d = new Div();

		RowRenderer<T> rr = new RowRenderer<>(dataClass);
		rr.column(nameProperty).editable().factory(createNameFactory(nameProperty));
		DataTable<T> dt = new DataTable<>(model, rr);
		dt.setPreventRowHighlight(true);
		dt.setPageSize(25);
		d.add(dt);

		ButtonBar2 bb = new ButtonBar2();
		d.add(bb);
		bb.addLinkButton("Add", Icon.faPlus, a -> model.add(factory.get()));
		return d;
	}

	private <T> IRowControlFactory<T> createNameFactory(QField<T, String> nameProperty) {
		return row -> {
			Text2<String> ctrl = new Text2<>(String.class);
			ctrl.setMandatory(true);
			ctrl.bind().to(row, nameProperty.getName());		// The row's control edits the row's record
			return ctrl;
		};
	}

	private void save() throws Exception {
		if(bindErrors())
			return;
		try {
			for(Genre genre : m_genreModel.getList()) {
				getSharedContext().save(genre);
			}
			for(MediaType mediaType : m_mediaTypeModel.getList()) {
				getSharedContext().save(mediaType);
			}
			getSharedContext().commit();
		} catch(Exception x) {
			ExceptionDialog.create(this, "Save failed", x);
		}
	}
}
