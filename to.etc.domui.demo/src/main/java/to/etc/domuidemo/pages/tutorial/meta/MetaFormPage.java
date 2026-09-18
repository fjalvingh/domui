package to.etc.domuidemo.pages.tutorial.meta;

import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component.meta.ClassMetaModel;
import to.etc.domui.component.meta.MetaManager;
import to.etc.domui.component.meta.NumericPresentation;
import to.etc.domui.component.meta.PropertyMetaModel;
import to.etc.domui.component.meta.PropertyRelationType;
import to.etc.domui.component.tbl.DataPager;
import to.etc.domui.component.tbl.DataTable;
import to.etc.domui.component.tbl.RowRenderer;
import to.etc.domui.component.tbl.SimpleSearchModel;
import to.etc.domui.component2.form4.FormBuilder;
import to.etc.domui.converter.IConverter;
import to.etc.domui.derbydata.db.Track;
import to.etc.domui.derbydata.db.Track_;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.UrlPage;
import to.etc.webapp.query.QCriteria;

import java.util.stream.Collectors;

/**
 * Tutorial, "metadata", step 1: a form and a table that say nothing about the
 * fields they show. The labels, the widths, the money and duration formatting,
 * the mandatory markers, the columns of the table and the order it is sorted in
 * all come from the Track class itself.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class MetaFormPage extends UrlPage {
	@Override
	public void createContent() throws Exception {
		setPageTitle("A form that repeats nothing");

		ContentPanel cp = new ContentPanel();
		add(cp);
		cp.add(new HTag(1, "A form that repeats nothing"));

		Track track = getSharedContext().get(Track.class, 1L);

		//-- Not one label, size, converter or mandatory marker here.
		FormBuilder fb = new FormBuilder(cp);
		fb.property(track, Track_.name()).control();
		fb.property(track, Track_.composer()).control();
		fb.property(track, Track_.milliseconds()).control();
		fb.property(track, Track_.unitPrice()).control();
		fb.property(track, Track_.album()).readOnly().control();
		fb.property(track, Track_.mediaType()).readOnly().control();

		cp.add(new HTag(2, "The same class, as a table"));

		//-- A RowRenderer without a single column() call: the columns come from @MetaObject.
		SimpleSearchModel<Track> model = new SimpleSearchModel<>(this, QCriteria.create(Track.class));
		DataTable<Track> dt = new DataTable<>(model, new RowRenderer<>(Track.class));
		cp.add(dt);
		dt.setPageSize(5);
		cp.add(new DataPager(dt));

		cp.add(new HTag(2, "What the metadata actually says"));
		Div box = new Div("dm-tut-q");
		cp.add(box);

		ClassMetaModel cmm = MetaManager.findClassMeta(Track.class);
		box.add("default sort: " + cmm.getDefaultSortProperty() + " " + cmm.getDefaultSortDirection() + "\n");
		box.add("table columns: " + cmm.getTableDisplayProperties().stream()
			.map(d -> d.getProperty().getName()).collect(Collectors.joining(", ")) + "\n");
		box.add("search fields: " + cmm.getSearchProperties().stream()
			.map(d -> d.getProperty().getName()).collect(Collectors.joining(", ")) + "\n\n");
		for(String name : new String[]{"name", "composer", "milliseconds", "unitPrice", "album", "mediaType"}) {
			box.add(describe(cmm.getProperty(name)) + "\n");
		}
	}

	/**
	 * Print the part of a property's metadata that this page's form and table are using.
	 */
	private static String describe(PropertyMetaModel<?> pmm) {
		StringBuilder sb = new StringBuilder();
		sb.append(pmm.getName()).append(": label=\"").append(pmm.getDefaultLabel()).append("\"");
		sb.append(", type=").append(pmm.getActualType().getSimpleName());
		if(pmm.getLength() > 0)
			sb.append(", length=").append(pmm.getLength());
		if(pmm.getPrecision() > 0)
			sb.append(", precision=").append(pmm.getPrecision()).append("/").append(pmm.getScale());
		if(pmm.isRequired())
			sb.append(", required");
		IConverter<?> converter = pmm.getConverter();
		if(converter != null)
			sb.append(", converter=").append(converter.getClass().getSimpleName());
		if(pmm.getNumericPresentation() != NumericPresentation.UNKNOWN)
			sb.append(", numeric=").append(pmm.getNumericPresentation());
		if(pmm.getRelationType() != PropertyRelationType.NONE)
			sb.append(", relation=").append(pmm.getRelationType());
		return sb.toString();
	}
}
