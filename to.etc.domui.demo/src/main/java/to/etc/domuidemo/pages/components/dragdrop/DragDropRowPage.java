package to.etc.domuidemo.pages.components.dragdrop;

import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.DropMode;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.dom.html.Para;
import to.etc.domui.dom.html.TBody;
import to.etc.domui.dom.html.TD;
import to.etc.domui.dom.html.TR;
import to.etc.domui.dom.html.Table;
import to.etc.domui.dom.html.UrlPage;
import to.etc.domui.util.DropEvent;
import to.etc.domui.util.IDragArea;
import to.etc.domui.util.IDragHandler;
import to.etc.domui.util.IDropHandler;

/**
 * Drag and drop in ROW mode: things are dropped into a table, at the position they
 * are dropped at.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class DragDropRowPage extends UrlPage {
	private static final String[] TRACKS = {"Black Dog", "Kashmir", "Immigrant Song", "Rock and Roll", "Ramble On", "Going to California"};

	/** A track waiting to be put on the list, and a track that is on it. */
	private static final String TRACK = "track";

	private static final String ON_LIST = "listed-track";

	@Override
	public void createContent() throws Exception {
		setPageTitle("Drag and drop: into a table");

		ContentPanel cp = new ContentPanel();
		add(cp);
		cp.add(new HTag(1, "Drag and drop into a table"));

		cp.add(new Para().add("Drag a track into the playlist, at the place it should be played; "
			+ "drag a row of the playlist to move it, or back to the left to take it off again."));

		Div columns = new Div("dm-dnd-cols");
		cp.add(columns);

		//-- Left: the tracks not on the list yet.
		Div tracks = new Div("dm-dnd-zone");
		columns.add(tracks);

		//-- Right: the playlist. The DIV is the drop zone; the rows land in its TBody.
		Div listZone = new Div("dm-dnd-zone");
		columns.add(listZone);
		Table table = new Table("dm-dnd-list");
		listZone.add(table);
		table.setTableWidth("100%");
		TBody body = table.getBody();

		IDragHandler trackDrag = removingDragHandler(TRACK);
		IDragHandler rowDrag = removingDragHandler(ON_LIST);

		//-- Dropping a track on the playlist inserts a row at the position it was dropped at.
		listZone.setDropHandler(new IDropHandler() {
			@Override
			public String[] getAcceptableTypes() {
				return new String[]{TRACK, ON_LIST};
			}

			@Override
			public void onDropped(DropEvent event) throws Exception {
				NodeBase dropped = event.getDraggedNode();
				String track = dropped instanceof TR tr ? (String) tr.getRowData() : ((Div) dropped).getTitle();

				TR row = new TR();
				row.setRowData(track);
				row.setDragHandler(rowDrag);
				TD cell = row.addCell();
				cell.add(track);

				int index = Math.min(event.getIndex(), body.getChildCount());
				body.add(index, row);
			}
		});
		listZone.setDropBody(body, DropMode.ROW);

		//-- Dropping a row back on the left takes the track off the list.
		tracks.setDropHandler(new IDropHandler() {
			@Override
			public String[] getAcceptableTypes() {
				return new String[]{ON_LIST};
			}

			@Override
			public void onDropped(DropEvent event) throws Exception {
				TR row = (TR) event.getDraggedNode();
				tracks.add(track((String) row.getRowData(), trackDrag));
			}
		});

		for(String name : TRACKS) {
			tracks.add(track(name, trackDrag));
		}

		cp.add(new Para().add("A drop zone is in ROW mode when it is given a TBody to drop into: "
			+ "setDropBody(body, DropMode.ROW) on the DIV around the table. From then on the browser "
			+ "shows an insert marker between the rows while something hovers over the table, and "
			+ "the drop handler is told where it landed - DropEvent.getIndex() is the row number to "
			+ "insert at."));

		cp.add(new Para().add("The drag handler of a row removes it when it is dropped, so a row "
			+ "dragged inside the playlist is taken out before the drop handler puts it back at its "
			+ "new position. That is what makes a drop both a move and a re-order, and it is why "
			+ "the index is clamped to the number of rows that are left."));

		cp.add(new Para().add("A TR is draggable; a TBody is what a DIV drops rows into. Neither "
			+ "needs a component of its own: the plain html nodes carry the handlers."));
	}

	private static Div track(String name, IDragHandler dh) {
		Div d = new Div("dm-dnd-item");
		d.add(name);
		d.setTitle(name);
		d.setDragHandler(dh);
		return d;
	}

	/**
	 * A drag handler that takes the dragged node out of the tree when it is dropped; the
	 * drop handler makes the new node.
	 */
	private static IDragHandler removingDragHandler(String type) {
		return new IDragHandler() {
			@Override
			public String getTypeName(NodeBase source) {
				return type;
			}

			@Override
			public IDragArea getDragArea() {
				return null;
			}

			@Override
			public void onDropped(DropEvent event) throws Exception {
				event.getDraggedNode().remove();
			}
		};
	}
}
