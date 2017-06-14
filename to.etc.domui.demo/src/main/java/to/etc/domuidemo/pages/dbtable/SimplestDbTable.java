package to.etc.domuidemo.pages.dbtable;

import to.etc.domui.component.layout.*;
import to.etc.domui.component.tbl.*;
import to.etc.domui.derbydata.db.Artist;
import to.etc.domui.dom.html.*;
import to.etc.webapp.query.*;

/**
 * Simplest example of showing data from the database. Shows:
 * <ul>
 * 	<li>Using QCriteria to define a selection</li>
 *	<li>Using
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Apr 25, 2010
 */
public class SimplestDbTable extends UrlPage {
	@Override
	public void createContent() throws Exception {
		add(new CaptionedHeader("Artists in the database"));

		// Just select all artists. This does not query but /specifies/ the query to do
		QCriteria<Artist> q = QCriteria.create(Artist.class);

		SimpleSearchModel<Artist> model = new SimpleSearchModel<Artist>(this, q);
		model.sortOn("name", false);

		BasicRowRenderer<Artist> rr = new BasicRowRenderer<Artist>(Artist.class);
		DataTable<Artist> dt = new DataTable<Artist>(model, rr);
		add(dt);
		dt.setPageSize(25);

		DataPager dp = new DataPager(dt);
		add(dp);
	}
}
