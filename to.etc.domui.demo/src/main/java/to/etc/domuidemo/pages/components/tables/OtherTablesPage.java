package to.etc.domuidemo.pages.components.tables;

import to.etc.domui.component.buttons.DefaultButton;
import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component.tbl.DataCellTable;
import to.etc.domui.component.tbl.ITableModel;
import to.etc.domui.component.tbl.IMovableShuttleModel;
import to.etc.domui.component.tbl.ListShuttle;
import to.etc.domui.component.tbl.SimpleListModel;
import to.etc.domui.derbydata.db.Album;
import to.etc.domui.derbydata.db.Album_;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.Para;
import to.etc.domui.dom.html.UrlPage;
import to.etc.webapp.query.QCriteria;

import java.util.ArrayList;
import java.util.List;

/**
 * The two other table-shaped components: a grid of cells, and two lists with
 * values moving between them.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class OtherTablesPage extends UrlPage {
	@Override
	public void createContent() throws Exception {
		setPageTitle("A grid and a shuttle");

		ContentPanel cp = new ContentPanel();
		add(cp);

		//-- 1. A grid: the same model, but laid out as cells rather than rows.
		cp.add(new HTag(1, "DataCellTable: a grid of cells"));

		List<Album> albums = getSharedContext().query(
			QCriteria.create(Album.class).ascending(Album_.title()).limit(12));

		DataCellTable<Album> grid = new DataCellTable<>(new SimpleListModel<>(albums));
		grid.setColumns(4);
		grid.setContentRenderer((node, album) -> {
			Div title = new Div("dm-tut-hi");
			node.add(title);
			title.add(album.getTitle());
			node.add(album.getArtist().getName());
		});
		cp.add(grid);

		cp.add(new Para().add("A DataCellTable puts one item in each cell of a grid instead of "
			+ "one item on each row: an index of covers, a wall of photographs. It takes the "
			+ "same ITableModel a DataTable takes, and a renderer that draws one item."));

		//-- 2. A shuttle: pick some of a list, and put them in order.
		cp.add(new HTag(2, "ListShuttle: choosing from a list"));

		Div shown = new Div("dm-tut-q");
		shown.add("Move some albums over and press the button.");

		AlbumShuttleModel shuttleModel = new AlbumShuttleModel(new ArrayList<>(albums));

		ListShuttle shuttle = new ListShuttle();
		cp.add(shuttle);
		shuttle.setModel(shuttleModel);
		shuttle.setSourceRenderer((node, album) -> node.add(((Album) album).getTitle()));
		shuttle.setTargetRenderer((node, album) -> node.add(((Album) album).getTitle()));

		cp.add(new DefaultButton("What did I choose?", a -> {
			StringBuilder sb = new StringBuilder();
			for(Album album : shuttleModel.getChosen()) {
				sb.append(album.getTitle()).append("\n");
			}
			shown.removeAllChildren();
			shown.add(sb.length() == 0 ? "Nothing yet" : sb.toString());
		}));
		cp.add(shown);

		cp.add(new Para().add("The shuttle is two models side by side with buttons that move "
			+ "values from one to the other. It is for choosing a handful out of a list *and* "
			+ "putting them in order - the columns of a report, the steps of a workflow."));
		cp.add(new Para().add("What it needs is one IShuttleModel: the two table models, and "
			+ "the three moves. The one below moves albums between two lists and keeps both "
			+ "models informed, which is what makes the screen follow."));
	}

	/**
	 * What a ListShuttle needs: two models and the moves between them. This one holds
	 * the two lists itself and tells each model what changed, so both sides redraw.
	 */
	private static final class AlbumShuttleModel implements IMovableShuttleModel<Album, Album> {
		private final List<Album> m_available;

		private final List<Album> m_chosen = new ArrayList<>();

		private final SimpleListModel<Album> m_sourceModel;

		private final SimpleListModel<Album> m_targetModel;

		AlbumShuttleModel(List<Album> available) {
			m_available = available;
			m_sourceModel = new SimpleListModel<>(m_available);
			m_targetModel = new SimpleListModel<>(m_chosen);
		}

		List<Album> getChosen() {
			return m_chosen;
		}

		@Override
		public ITableModel<Album> getSourceModel() {
			return m_sourceModel;
		}

		@Override
		public ITableModel<Album> getTargetModel() {
			return m_targetModel;
		}

		@Override
		public void moveSourceToTarget(int six, int tix) throws Exception {
			Album album = m_sourceModel.getItem(six);
			m_sourceModel.delete(six);
			//-- The shuttle asks for index 9999 when it means "at the end", so clamp.
			int at = tix < 0 || tix > m_chosen.size() ? m_chosen.size() : tix;
			m_targetModel.add(at, album);
		}

		@Override
		public void moveTargetToSource(int tix) throws Exception {
			Album album = m_targetModel.getItem(tix);
			m_targetModel.delete(tix);
			m_sourceModel.add(album);
		}

		@Override
		public void moveTargetItem(int from, int to) throws Exception {
			Album album = m_targetModel.getItem(from);
			m_targetModel.delete(from);
			int at = to < 0 || to > m_chosen.size() ? m_chosen.size() : to;
			m_targetModel.add(at, album);
		}
	}
}
