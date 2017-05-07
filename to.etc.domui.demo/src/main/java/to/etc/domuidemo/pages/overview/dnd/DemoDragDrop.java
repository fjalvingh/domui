package to.etc.domuidemo.pages.overview.dnd;

import to.etc.domui.component.misc.*;
import to.etc.domui.dom.css.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.util.*;

/**
 * Test/demo code for handling drag and drop from div1 to div2.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Dec 9, 2008
 */
public class DemoDragDrop extends UrlPage {
	Div			m_diva, m_divb;

	@Override
	public void createContent() throws Exception {
		m_diva	= new Div();
		add(m_diva);
		m_diva.setHeight("400px");
		m_diva.setBorder(1, "red", "dotted");
		m_diva.setDropHandler(DROP_HANDLER);

		add(new VerticalSpacer(10));
		add(new MsgDiv("Select your pet and drag it to the window above. Deselect by dragging it down again."));
		m_divb = new Div();
		add(m_divb);
		m_divb.setHeight("200px");
		m_divb.setBorder(1, "green", "solid");
		m_divb.setDropHandler(DROP_HANDLER2);

//		for(String s: DATA) {
//			Img	img = new Img(s);
//			Div	d = new Div();
//			d.setFloat(FloatType.LEFT);
//			d.add(img);
//			m_divb.add(d);
//
//			d.setDragHandler(DRAG_HANDLER);
//		}
		for(int i = 1; i < 16; i++) {
			Img img = new Img("img/dragndrop/drag" + i + ".gif");
			Div	d = new Div();
			d.setFloat(FloatType.LEFT);
			d.add(img);
			m_divb.add(d);

			d.setDragHandler(DRAG_HANDLER);
		}
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

	IDragHandler		DRAG_HANDLER2 = new IDragHandler() {
		@Override
		public String getTypeName(NodeBase source) {
			return "demo2";
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

	static final String[]	ACCEPTABLE = {"demo"};

	IDropHandler		DROP_HANDLER = new IDropHandler() {
		@Override
		public String[] getAcceptableTypes() {
			return ACCEPTABLE;
		}
		@Override
		public void onDropped(DropEvent context) throws Exception {
			((IDraggable) context.getDraggedNode()).setDragHandler(DRAG_HANDLER2);
			m_diva.add(context.getDraggedNode());
		}

		@Override
		public DropMode getDragMode() {
			return DropMode.DIV;
		}
	};
	IDropHandler		DROP_HANDLER2 = new IDropHandler() {
		@Override
		public String[] getAcceptableTypes() {
			return new String[] {"demo2"};
		}
		@Override
		public void onDropped(DropEvent context) throws Exception {
			m_divb.add(context.getDraggedNode());
			((IDraggable) context.getDraggedNode()).setDragHandler(DRAG_HANDLER);
		}
		@Override
		public DropMode getDragMode() {
			return DropMode.ROW;
		}
	};
}
