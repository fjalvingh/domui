package to.etc.domuidemo.pages.overview.dnd;

import to.etc.domui.component.misc.*;
import to.etc.domui.dom.css.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.util.*;

/**
 * Demo/test page for a Row-mode table drag and drop thing. This defines a table as a drop zone,
 * and inserts draggables as a new row in the drop table.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Dec 11, 2008
 */
public class DemoTableInDrag extends UrlPage {
	private TD				m_acell;
	private TD				m_bcell;
	TBody					m_dropBody;

	@Override
	public void createContent() throws Exception {
		add(new Explanation("Dit voorbeeld showt drag-and-drop in <i>row</i> mode. In deze mode is de dropzone een DIV die gekoppeld is aan een TBODY. De " +
				" gesleepte objecten worden als 'row' toegevoegd in de TBODY, en dat wordt ook visueel weergegeven door het insert point te markeren. De locatie" +
				" waar het object terechtkomt wordt bepaald door het slepen."));

		Table	root = new Table();							// Horizontal divider.
		add(root);
		root.setTableWidth("100%");
		TBody	b = new TBody();
		root.add(b);
		m_acell = b.addRowAndCell();
		m_acell.setWidth("50%");
		m_bcell	= b.addCell();
		m_bcell.setWidth("50%");
		m_bcell.setCellHeight("300px");

		//-- Create the source thingy in cell A
		for(int i = 1; i < 11; i++) {
			Img img = new Img("img/dragndrop/dragt" + i + ".gif");
			Div	d = new Div();
			d.setFloat(FloatType.LEFT);
			d.add(img);
			m_acell.add(d);
			d.setDragHandler(DRAG_HANDLER);
		}

		//-- The drop DIV which contains the table we'll put our droppings in
		Div	dropdiv = new Div();
		m_bcell.add(dropdiv);
		dropdiv.setDropHandler(DROP_HANDLER);
		dropdiv.setHeight("100%");
		dropdiv.setWidth("100%");
		dropdiv.setOverflow(Overflow.SCROLL);

		//-- Create the drop table
		dropdiv.setBorder(1, "green", "solid");
		dropdiv.add(new MsgDiv("Drop your crud here."));
		Table	droptbl = new Table();
		dropdiv.add(droptbl);
		droptbl.setTableWidth("100%");
		m_dropBody = new TBody();
		droptbl.add(m_dropBody);

		//-- Mark the TBody as the target for droppings
		dropdiv.setDropBody(m_dropBody, DropMode.ROW);

//		//-- Fake a single thingy in the table now
//		TD cell = m_dropBody.addRowAndCell();
//		Div	id = new Div();
//		cell.add(id);
//		id.add(new Img("img/dragt0.gif"));
	}

	IDragHandler		DRAG_HANDLER = new IDragHandler() {
		@Override
		public String getTypeName(NodeBase source) {
			return "demo";
		}

		@Override
		public IDragArea getDragArea() {
			return null;
		}

		@Override
		public void onDropped(DropEvent context) throws Exception {
			context.getDraggedNode().remove();
		}
	};

	IDropHandler	DROP_HANDLER = new IDropHandler() {
		@Override
		public void onDropped(DropEvent context) throws Exception {
			System.out.println("Drop accepted: index="+context.getIndex());
			TR	row	= new TR();
			TD	cell = row.addCell();
			cell.add(context.getDraggedNode());
			m_dropBody.add(context.getIndex(), row);
		}

		@Override
		public DropMode getDragMode() {
			return DropMode.ROW;
		}

		@Override
		public String[] getAcceptableTypes() {
			return new String[] {"demo" };
		}
	};

}
