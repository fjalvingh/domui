package to.etc.domuidemo.pages.overview.tbl;

import to.etc.domui.component.meta.*;
import to.etc.domui.component.tbl.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.util.*;
import to.etc.domuidemo.db.*;
import to.etc.webapp.query.*;

public class DemoRowRenderer2 extends UrlPage {
	@Override
	public void createContent() throws Exception {
		// Set up a data table
		QCriteria<Album> q = QCriteria.create(Album.class);
		SimpleSearchModel<Album> ssm = new SimpleSearchModel<Album>(this, q);

		INodeContentRenderer<String>	ncr = new INodeContentRenderer<String>() {
			@Override
			public void renderNodeContent(NodeBase component, NodeContainer node, String value, Object parameters) throws Exception {
				if(null == value)
					return;
				String inside = value.toLowerCase();
				if(inside.contains("import")) {
					node.add(new Img("img/import.png"));
				} else if(inside.contains("live")) {
					node.add(new Img("img/live.png"));
				}
				node.add("\u00a0" + value);
			}
		};

		BasicRowRenderer<Album> brr = new BasicRowRenderer<Album>(Album.class
			, "artist.name", "^The Artist", "%50"
			, "title", "^Album title", "%50", SortableType.SORTABLE_ASC, ncr
		);
		DataTable<Album> dt = new DataTable<Album>(ssm, brr);
		add(dt);
		dt.setPageSize(25);

		//-- This is the actual demo ;-)
		add(new DataPager(dt));
	}
}
