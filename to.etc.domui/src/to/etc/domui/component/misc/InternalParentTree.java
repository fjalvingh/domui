/*
 * DomUI Java User Interface library
 * Copyright (c) 2010 by Frits Jalvingh, Itris B.V.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * See the "sponsors" file for a list of supporters.
 *
 * The latest version of DomUI and related code, support and documentation
 * can be found at http://www.domui.org/
 * The contact for the project is Frits Jalvingh <jal@etc.to>.
 */
package to.etc.domui.component.misc;

import java.io.*;
import java.net.*;

import javax.annotation.*;

import to.etc.domui.component.buttons.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.util.*;
import to.etc.util.*;

/**
 * This popup floater shows all parent nodes from a given node up, and selects one. It is part
 * of the development mode double-tilde keypress.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Dec 28, 2010
 */
public class InternalParentTree extends Div {
	private NodeBase m_touched;

	private Div m_structure;

	public InternalParentTree(NodeBase touched) {
		m_touched = touched;
	}

	@Override
	public void createContent() throws Exception {
		setCssClass("ui-ipt");
		Div ttl = new Div();
		add(ttl);
		ttl.setCssClass("ui-ipt-ttl");
		ttl.add("Development: Parent Structure");
		Img img = new Img("THEME/close.png");
		img.setAlign(ImgAlign.RIGHT);
		ttl.add(img);
		img.setClicked(new IClicked<Img>() {
			@Override
			public void clicked(@Nonnull Img clickednode) throws Exception {
				//-- Remove this.
				InternalParentTree.this.remove();
			}
		});
		Div list = new Div();
		m_structure = list;
		add(list);
		list.setCssClass("ui-ipt-list");
		renderStructure(list);
		appendCreateJS("$('#" + getActualID() + "').draggable({" + "ghosting: false, zIndex:" + 200 + ", handle: '#" + ttl.getActualID() + "'});");
	}

	protected void renderStructure(Div list) {
		//-- Run all parents.
		TBody b = list.addTable();

		for(NodeBase nb = m_touched; nb != null;) {
			final NodeBase clicked = nb;
			TR row = b.addRow();
			row.setCssClass("ui-ipt-item");

			//-- Type icon
			TD td = b.addCell();
			td.setCellWidth("1%");
			String icon = "";
			String nn = nb.getClass().getName();
			if(nn.startsWith("to.etc.domui.dom.")) {
				icon = "iptHtml.png";
				td.setTitle("HTML Node");
			} else if(nb instanceof UrlPage) {
				icon = "iptPage.png";
				td.setTitle("DomUI Page");
			} else {
				td.setTitle("DomUI Component");
				icon = "iptComponent.png";
			}
			td.add(new Img("THEME/" + icon));

			//-- Show component source code button.
			td = b.addCell();
			td.setCssClass("ui-ipt-btn");
			td.setCellWidth("1%");
			td.setClicked(new IClicked<NodeBase>() {
				@Override
				public void clicked(@Nonnull NodeBase clickednode) throws Exception {
					openSource(clicked);
				}
			});
			td.setTitle("Open the component's source code");
			td.add(new Img("THEME/iptSourceCode.png"));

			//-- If applicable: component creation location
			if(null != nb.getAllocationTracepoint()) {
				td = b.addCell();
				td.setCssClass("ui-ipt-btn");
				td.setCellWidth("1%");
				td.setClicked(new IClicked<NodeBase>() {
					@Override
					public void clicked(@Nonnull NodeBase clickednode) throws Exception {
						showCreationTrace(clicked, clicked.getAllocationTracepoint());
					}
				});
				td.setTitle("Open the location where the component was created");
				td.add(new Img("THEME/iptLocation.png"));
			}

			//-- The name
			td = b.addCell();
			td.setCellWidth("97%");
			td.add(nn);

			if(!nb.hasParent())
				break;
			nb = nb.getParent();
		}
	}

	/**
	 * Show a stacktrace window with the ability to open the source for that element.
	 * @param clicked
	 * @param allocationTracepoint
	 */
	protected void showCreationTrace(NodeBase clicked, StackTraceElement[] allocationTracepoint) {
		m_structure.removeAllChildren();

		Div alt = new Div();
		m_structure.add(alt);
		LinkButton lb = new LinkButton("Back to structure", "THEME/btnBack.png", new IClicked<LinkButton>() {
			@Override
			public void clicked(@Nonnull LinkButton clickednode) throws Exception {
				m_structure.removeAllChildren();
				renderStructure(m_structure);
			}
		});
		alt.add(lb);

		Div stk = new Div();
		m_structure.add(stk);

		/*
		 * We need to find the 1st constructor in the stack trace, because that w
		 */
		boolean first = true;
		boolean gotctor = false;
		TBody b = stk.addTable();
		for(StackTraceElement ste : allocationTracepoint) {
			String nn = ste.getClassName();
			if(nn.startsWith("org.apache.tomcat."))
				return;

			//-- Skip code when it is inside internal code.
			if(first) {
				if(ste.getMethodName().equals("<init>")) {
					gotctor = true;
				}

				if(nn.equals(DomUtil.class.getName()) || nn.equals(NodeBase.class.getName()) || nn.equals(NodeContainer.class.getName()))
					continue;
				first = false;
				if(!gotctor)
					continue;
				if(ste.getMethodName().equals("<init>"))
					continue;
			}

			first = false;
			TR row = b.addRow();
			row.setCssClass("ui-ipt-item");

			//-- Type icon
			TD td = b.addCell();
			td.setCellWidth("1%");
			String icon = "";
			if(nn.startsWith("to.etc.domui.dom.")) {
				icon = "iptHtml.png";
			} else if(nn.startsWith("to.etc.domui.")) {
				icon = "iptComponent.png";
			} else {
				icon = "iptPage.png";
			}
			td.add(new Img("THEME/" + icon));

			//-- Show component source code button.
			td = b.addCell();
			td.setCssClass("ui-ipt-btn");
			td.setCellWidth("1%");
			final StackTraceElement cste = ste;
			td.setClicked(new IClicked<NodeBase>() {
				@Override
				public void clicked(@Nonnull NodeBase clickednode) throws Exception {
					openSource(cste);
				}
			});
			td.setTitle("Open the source code at this location");
			td.add(new Img("THEME/iptSourceCode.png"));

			//-- Source link.
			td = b.addCell();
			td.setCellWidth("97%");
			td.add(ste.getClassName() + "#" + ste.getMethodName() + " (" + ste.getLineNumber() + ")");
		}
	}

	protected void openSource(NodeBase clicked) {
		NodeBase body = getPage().getBody();
		remove();

		//-- Get name for the thingy,
		String name = clicked.getClass().getName().replace('.', '/') + ".java";
		if(! openEclipseSource(name)) {
			MsgBox.message(body, MsgBox.Type.WARNING, "I was not able to send an OPEN FILE command to Eclipse.. You need to have the Eclipse plugin running. Please see " + URL + " for details");
		}
	}

	protected void openSource(StackTraceElement ste) {
		NodeBase body = getPage().getBody();
		remove();

		//-- Get name for the thingy,
		String name;
		if(ste.getLineNumber() <= 0)
			name = ste.getClassName().replace('.', '/') + ".java@" + ste.getMethodName();
		else
			name = ste.getClassName().replace('.', '/') + ".java#" + ste.getLineNumber();
		if(!openEclipseSource(name)) {
			MsgBox.message(body, MsgBox.Type.WARNING, "I was not able to send an OPEN FILE command to Eclipse.. You need to have the Eclipse plugin running. Please see " + URL + " for details");
		}
	}

	static private final String URL = "http://www.domui.org/wiki/bin/view/Documentation/EclipsePlugin";

	/**
	 * Try to reach Eclipse on localhost and make it open the source for the specified class.
	 * @param name
	 * @return
	 */
	static public boolean openEclipseSource(String name) {
		int port = DeveloperOptions.getInt("domui.eclipse", 5050); // Default Eclipse port is 5050.
		Socket s = null;
		OutputStream outputStream = null;
		//		boolean connected = false;
		try {
			s = new Socket("127.0.0.1", port);
			//			connected = true;
			outputStream = s.getOutputStream();
			String msg = "OPENFILE " + name;
			outputStream.write(msg.getBytes("UTF-8"));
			outputStream.close();
			s.close();
			return true;
		} catch(Exception x) {
			System.out.println("DomUI: cannot connect to Eclipse on localhost:" + port + ". Is the DomUI plugin running in Eclipse? See "+URL);
			System.out.println("DomUI: the connect failed with " + x);
			return false;
		} finally {
			try {
				if(outputStream != null)
					outputStream.close();
			} catch(Exception x) {}
			try {
				if(s != null)
					s.close();
			} catch(Exception x) {}
		}
	}
}
