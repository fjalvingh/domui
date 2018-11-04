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

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.component.buttons.LinkButton;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.IClicked;
import to.etc.domui.dom.html.Img;
import to.etc.domui.dom.html.ImgAlign;
import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.dom.html.NodeContainer;
import to.etc.domui.dom.html.TBody;
import to.etc.domui.dom.html.TD;
import to.etc.domui.dom.html.TR;
import to.etc.domui.dom.html.UrlPage;
import to.etc.domui.server.DomApplication;
import to.etc.domui.util.DomUtil;
import to.etc.util.FileTool;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * This popup floater shows all parent nodes from a given node up, and selects one. It is part
 * of the development mode double-tilde keypress.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Dec 28, 2010
 */
public class InternalParentTree extends Div {
	private static final String PMS = ". Please make sure you use the latest plugin version always.";

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
		img.setClicked((IClicked<Img>) clickednode -> {
			InternalParentTree.this.remove();
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
			renderComponentLine(b, nb);

			if(!nb.hasParent())
				break;
			nb = nb.getParent();
		}
	}

	private void renderComponentLine(TBody body, NodeBase node) {
		final NodeBase clicked = node;
		TR row = body.addRow();
		row.setCssClass("ui-ipt-item");

		//-- Type icon
		TD td = body.addCell();
		td.setCellWidth("1%");
		td.css("ui-ipt-icon");
		String icon = "";
		String nn = node.getClass().getName();
		if(nn.startsWith("to.etc.domui.dom.")) {
			icon = "iptHtml.png";
			td.setTitle("HTML Node");
		} else if(node instanceof UrlPage) {
			icon = "iptPage.png";
			td.setTitle("DomUI Page");
		} else {
			td.setTitle("DomUI Component");
			icon = "iptComponent.png";
		}
		td.add(new Img("THEME/" + icon));


		//-- If applicable: component creation location
		StackTraceElement[] allocSt = node.getAllocationTracepoint();
		StackTraceElement created = null;

		if(null != allocSt && node.hasParent()) {
			List<StackTraceElement> stack = findStack(allocSt);
			if(stack.size() > 0) {
				created = stack.get(0);
			}
		}

		td = body.addCell();
		td.setCellWidth("97%");

		td.add(nn);
		if(null != created) {
			StackTraceElement morons = created;			// Pathetic.
			td.addCssClass("ui-ipt-link");
			td.setTitle("Open the location where the component is created");
			td.setClicked(clickednode -> openSource(morons));
		}

		//-- Show component source code button.
		td = body.addCell();
		td.setCssClass("ui-ipt-btn");
		td.setCellWidth("1%");
		td.setClicked(clickednode -> openSource(clicked));
		td.setTitle("Open the component's source code");
		//td.add(new Img("THEME/iptSourceCode.png"));
		td.add(Icon.faCode.css("is-size-small is-info").createNode());

		//-- Button to open the component source code.
		td = body.addCell();
		td.setCellWidth("1%");

		if(null != allocSt && node.hasParent()) {
			List<StackTraceElement> stack = findStack(allocSt);
			if(stack.size() > 0) {
				td.setCssClass("ui-ipt-btn");
				td.setClicked(clickednode -> showCreationTrace(clicked, stack));
				td.setTitle("Show the stacktrace where the component was created");
				td.add(Icon.faBars.css("is-danger is-size-small").createNode());
				//td.add(new Img("THEME/iptLocation.png"));
			}
		}
	}

	private List<StackTraceElement> findStack(StackTraceElement[] list) {
		boolean first = true;
		boolean gotctor = false;

		List<StackTraceElement> res = new ArrayList<>();
		for(StackTraceElement ste : list) {
			String nn1 = ste.getClassName();
			if(nn1.startsWith("org.apache.tomcat."))
				return res;

			//-- Skip code when it is inside internal code.
			if(first) {
				if(ste.getMethodName().equals("<init>")) {
					gotctor = true;
				}

				if(nn1.equals(DomUtil.class.getName()) || nn1.equals(NodeBase.class.getName()) || nn1.equals(NodeContainer.class.getName()))
					continue;
				first = false;
				if(!gotctor)
					continue;
				if(ste.getMethodName().equals("<init>"))
					continue;
			}
			if(ste.getMethodName().equals("<init>")){
				continue;
			}
			res.add(ste);
		}
		return res;
	}


	/**
	 * Show a stacktrace window with the ability to open the source for that element.
	 */
	protected void showCreationTrace(NodeBase clicked, List<StackTraceElement> list) {
		m_structure.removeAllChildren();

		Div alt = new Div();
		m_structure.add(alt);
		LinkButton lb = new LinkButton("Back to structure", Icon.of("THEME/btnBack.png"), (IClicked<LinkButton>) clickednode -> {
			m_structure.removeAllChildren();
			renderStructure(m_structure);
		});
		alt.add(lb);

		Div stk = new Div();
		m_structure.add(stk);

		/*
		 * We need to find the 1st constructor in the stack trace, because that w
		 */
		TBody b = stk.addTable();
		for(StackTraceElement ste : list) {
			String nn = ste.getClassName();
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
				public void clicked(@NonNull NodeBase clickednode) throws Exception {
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

	@NonNull
	private String openableClassName(@NonNull String str) {
		return str.replace('.', '/').replaceAll("\\$.*", "");
	}

	protected void openSource(NodeBase clicked) {
		NodeBase body = getPage().getBody();
		remove();

		//-- Get name for the thingy,
		String name = openableClassName(clicked.getClass().getName()) + ".java";
		openSourceWithWarning(body, name);
	}

	private void openSourceWithWarning(@NonNull NodeBase body, @NonNull String name) {
		CommandResponse cr = openEclipseSource(name);
		if(cr.getType() == AnswerType.SUCCESS)
			return;

		String message = getResponseMessage(cr);
		MsgBox.message(body, MsgBox.Type.ERROR, message);
	}

	@NonNull
	static public String getResponseMessage(@NonNull CommandResponse cr) {
		switch(cr.getType()){
			default:
				throw new IllegalStateException("Missing case: " + cr.getType());
			case ERROR:
				return "Eclipse responded with an error to the 'open file' command: " + cr.getMessage() + PMS;

			case NOCONNECTION:
				//-- No one responded.
				if(isOldPortInUse()) {
					return "It looks like you are using an old version of the DomUI Plugin. Please update using Help -> Check for updates in Eclipse";
				} else {
					return "It looks like you do not have the DomUI Eclipse plugin installed. Please see " + URL + " for installation details";
				}

			case SUCCESS:
				return "";

			case REFUSED:
				if(isOldPortInUse()) {
					return "None of your running Eclipse instances wanted to open the page. It looks like " +	//
						"you have at least one Eclipse installation that uses the old version of the plugin, which is not compatible. " + //
						"Please update the DomUI plugin to the latest version using Help -> Check for updates in all your running Eclipses"; //
				} else {
					return "An unexpected error has occurred: none of the running Eclipse installations " +	//
						"recognized this web application. Please report this as a bug.";
				}
		}
	}


	protected void openSource(StackTraceElement ste) {
		NodeBase body = getPage().getBody();
		remove();

		//-- Get name for the thingy,
		String name = openableClassName(ste.getClassName()) + ".java";
		if(ste.getLineNumber() <= 0)
			name += "@" + ste.getMethodName();
		else
			name += "#" + ste.getLineNumber();
		openSourceWithWarning(body, name);
	}

	static private final String URL = "http://www.domui.org/wiki/bin/view/Documentation/EclipsePlugin";

	@NonNull
	static public CommandResponse openEclipseSource(@NonNull String name) {
		File root = DomApplication.get().getAppFile("");
		return openEclipseSource(root.toString(), name);
	}

	/**
	 * Try to reach Eclipse on localhost and make it open the source for the specified class.
	 * @param name
	 * @return
	 */
	@NonNull
	static public CommandResponse openEclipseSource(@NonNull String webappRoot, @NonNull String name) {
		int nconnects = 0;
		for(int port = 5051; port < 5060; port++) {
			CommandResponse cr = tryPortCommand(port, webappRoot, name);

			switch(cr.getType()){
				case NOCONNECTION:
					break;

				case SUCCESS:
					//-- It worked ;-)
					return cr;

				case ERROR:
					//-- An eclipse did react but had a specific error. This means we FOUND the right handler but it was unable to execute the command.
					return cr;

				case REFUSED:
					//-- That plugin did not want to handle our webapp.
					nconnects++;
					break;
			}
		}

		//-- Nothing worked. Distill some meaning of why not.
		System.out.println("DomUI: cannot connect to Eclipse on localhost ports 5051..5060. See " + URL);
		if(nconnects > 0) {
			return new CommandResponse(AnswerType.REFUSED, null);					// We had connects but all refused.
		}
		return new CommandResponse(AnswerType.NOCONNECTION, null);
	}

	static private boolean isOldPortInUse() {
		Socket s = null;
		//		boolean connected = false;
		try {
			s = new Socket("127.0.0.1", 5050);
			return true;
		} catch(Exception x) {
			return false;
		} finally {
			try {
				if(null != s)
					s.close();
			} catch(Exception x) {
				//-- willfully ignore.
			}
		}
	}

	public enum AnswerType {
		NOCONNECTION, REFUSED, ERROR, SUCCESS
	}

	static public class CommandResponse {
		@NonNull
		final private AnswerType m_type;

		@Nullable
		final private String m_message;

		public CommandResponse(@NonNull AnswerType type, @Nullable String message) {
			m_type = type;
			m_message = message;
		}

		@Nullable
		public String getMessage() {
			String message = m_message;
			if(null == message)
				throw new IllegalStateException("Message not defined");
			return message;
		}

		@NonNull
		public AnswerType getType() {
			return m_type;
		}
	}

	/**
	 * New-style command sending: send a SELECT [webapp] COMMAND url and wait for Eclipse to answer.
	 * @param port
	 * @param webappRoot
	 * @param name
	 * @return
	 */
	@NonNull
	static private CommandResponse tryPortCommand(int port, @NonNull String webappRoot, @NonNull String name) {
		Socket s = null;
		//		boolean connected = false;
		try {
			s = new Socket("127.0.0.1", port);
		} catch(Exception x) {
			System.out.println("DomUI: connect to Eclipse on socket "+port+" failed: "+x);
			return new CommandResponse(AnswerType.NOCONNECTION, null);
		}

		//-- Send a command
		OutputStream outputStream = null;
		InputStream is = null;
		try {
			//			connected = true;
			outputStream = s.getOutputStream();

			StringBuilder sb = new StringBuilder();
			sb.append("SELECT `");
			sb.append(webappRoot);
			sb.append("` OPENFILE `");
			sb.append(name);
			sb.append('`');
			outputStream.write(sb.toString().getBytes("UTF-8"));
			outputStream.write(0);
			outputStream.flush();

			//-- Read the response till EOF or error.
			is = s.getInputStream();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			byte[] buffer = new byte[512];
			int szrd;
			while(0 < (szrd = is.read(buffer))) {
				//				System.out.println("data: " + szrd);
				baos.write(buffer, 0, szrd);
			}
			//			System.out.println("data end = " + szrd);
			baos.close();

			String response = new String(baos.toByteArray(), "utf-8");
			System.out.println("DomUI Eclipse: response=" + response);

			//-- If response ends in lf strip it
			while(response.length() > 0 && response.charAt(response.length() - 1) == '\n')
				response = response.substring(0, response.length() - 1);

			//-- Get 1st token in the response.
			int pos = response.indexOf(' ');
			String code, rest;
			if(pos == -1) {
				code = response;
				rest = "";
			} else {
				code = response.substring(0, pos).trim();
				rest = response.substring(pos + 1).trim();
			}

			if("SELECT-FAILED".equals(code)) {
				return new CommandResponse(AnswerType.REFUSED, rest);
			} else if("OK".equals(code)) {
				return new CommandResponse(AnswerType.SUCCESS, rest);
			} else if("ERROR".equals(code)) {
				return new CommandResponse(AnswerType.ERROR, rest);
			} else {
				//-- Unknown response, but we have one -> treat as refused with a message.
				return new CommandResponse(AnswerType.REFUSED, rest);
			}
		} catch(Exception x) {
			System.out.println("DomUI: eclipse data exchange failed with " + x);
			x.printStackTrace();

			//-- We return refused, because this might not be the right eclipse anyway.
			return new CommandResponse(AnswerType.REFUSED, x.toString());
		} finally {
			FileTool.closeAll(outputStream, is);
			try {
				if(s != null)
					s.close();
			} catch(Exception x) {}
		}
	}


}
