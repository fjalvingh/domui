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
package to.etc.domui.util.bugs;

import java.util.*;

import javax.annotation.*;

import to.etc.domui.component.layout.*;
import to.etc.domui.dom.css.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.server.*;
import to.etc.domui.state.*;
import to.etc.domui.util.*;
import to.etc.util.*;

/**
 * This is a default DomUI bug listener. It collects all bugs in the ConversationContext, and
 * has code to show those bugs in the UI.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 18, 2010
 */
public class DefaultBugListener implements IBugListener {
	static private final int MAX_BUGS = 20;

	static private final String KEY = "DefaultBugList";

	static private final DefaultBugListener INSTANCE = new DefaultBugListener();

	static private class BugRef {
		private List<BugItem> m_bugList = new ArrayList<BugItem>();

		private NodeBase m_indicator;

		private Span m_countNode;

		private Img m_image;

		private int m_lastCount;

		private FloatingWindow m_window;

		public BugRef() {}

		public NodeBase getIndicator() {
			return m_indicator;
		}

		public void setIndicator(NodeBase indicator, Span countNode, Img image) {
			m_indicator = indicator;
			m_countNode = countNode;
			m_image = image;
		}

		public Span getCountNode() {
			return m_countNode;
		}

		public List<BugItem> getBugList() {
			return m_bugList;
		}

		public Img getImage() {
			return m_image;
		}

		public int getLastCount() {
			return m_lastCount;
		}

		public void setLastCount(int lastCount) {
			m_lastCount = lastCount;
		}

		public FloatingWindow getWindow() {
			return m_window;
		}

		public void setWindow(FloatingWindow window) {
			m_window = window;
		}
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Accepting BUG messages.								*/
	/*--------------------------------------------------------------*/
	/**
	 *
	 * @see to.etc.domui.util.bugs.IBugListener#bugSignaled(to.etc.domui.util.bugs.BugItem)
	 */
	@Override
	public void bugSignaled(@Nonnull BugItem item) {
		ConversationContext cc;
		try {
			cc = UIContext.getCurrentConversation();
		} catch(Exception x) {
			System.out.println("Bug signalled, outside UI: " + item);
			return;
		}

		//-- Is a bug list present in conversation?
		BugRef ref = (BugRef) cc.getAttribute(KEY);
		if(ref == null) {
			ref = new BugRef();
			cc.setAttribute(KEY, ref);
		}
		ref.getBugList().add(item);
		item.setNumber(ref.getBugList().size());

		if(ref.getWindow() != null) {
			ref.getWindow().remove();
			ref.setWindow(null);
		}
	}

	/**
	 * Should be called from a request interceptor. It clears the bug listener for this
	 * thread. Since that remains in the conversation it's contents is not lost, except
	 * when the conversation itself is gone.
	 * @param rc
	 */
	public static void onRequestAfter(IRequestContext rc) {
		Bug.setListener(null);
	}

	protected static void onRequestBefore(IRequestContext rc) {
		Bug.setListener(INSTANCE);
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Registration and initialization.					*/
	/*--------------------------------------------------------------*/
	/**
	 * Call in Application.initialize() to register stuff to use the default bug handler.
	 * @param da
	 */
	public static void registerSessionListener(DomApplication da) {
		//-- Add interceptor to make sure Bug listerer becomes null after request.
		da.addInterceptor(new IRequestInterceptor() {
			@Override
			public void before(IRequestContext rc) throws Exception {
				onRequestBefore(rc);
			}

			@Override
			public void after(IRequestContext rc, Exception x) throws Exception {
				onRequestAfter(rc);
			}
		});

		da.addUIStateListener(new IDomUIStateListener() {
			@Override
			public void windowSessionDestroyed(WindowSession ws) throws Exception {
			}

			@Override
			public void windowSessionCreated(WindowSession ws) throws Exception {
			}

			@Override
			public void onBeforePageAction(RequestContextImpl ctx, Page pg) {
			}

			@Override
			public void onBeforeFullRender(RequestContextImpl ctx, Page pg) {
			}

			@Override
			public void onAfterPage(IRequestContext ctx, Page pg) {
				checkForBugs(ctx, pg);
			}

			@Override
			public void conversationDestroyed(ConversationContext cc) throws Exception {
			}

			@Override
			public void conversationCreated(ConversationContext cc) throws Exception {
			}
		});
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	User interface when page has bugs.					*/
	/*--------------------------------------------------------------*/

	static final String[] PRESET = {"to.etc.dbpool.", "oracle.", "to.etc.domui.util.bugs."};

	static final String[] ENDSET = {"to.etc.dbpool.", "org.apache.tomcat.", "org.apache.coyote.", "org.apache.catalina."};

	/**
	 * Check if the page has bugs and if so show a bug icon in it's top. When
	 * the bug icon is clicked show the bugs in a floating popup thingerydoo.
	 * @param ctx
	 * @param pg
	 */
	protected static void checkForBugs(IRequestContext ctx, final Page pg) {
		ConversationContext cc = pg.getConversation();
		BugRef ref = (BugRef) cc.getAttribute(KEY);
		if(null == ref || ref.getBugList().size() == 0)
			return;

		System.out.println("BugListener: need to add UI for " + ref.getBugList().size() + " bugs are present");

		//-- We need to create/add to the Bug UI
		if(ref.getIndicator() == null) {
			//-- Create the bug indicator.
			Div ind = new Div();
			ind.setCssClass("ui-bug-ind");
			ind.setTitle("Houston, we have a problem... Bugs have been found.");

			Span count = new Span();
			count.setCssClass("ui-bug-count");
			ind.add(count);

			Img img = new Img("THEME/ui-bug-ind.png");
			ind.add(img);
			img.setImgBorder(0);
			img.setAlign(ImgAlign.RIGHT);

			ref.setIndicator(ind, count, img);

			final BugRef info = ref; // Sigh
			ind.setClicked(new IClicked<Div>() {
				@Override
				public void clicked(@Nonnull Div clickednode) throws Exception {
					toggleBugDisplay(pg, info);
				}
			});

			pg.getBody().add(ind);
		}
		if(ref.getBugList().size() != ref.getLastCount()) {
			int ct = ref.getBugList().size();
			if(ct >= MAX_BUGS && ref.getLastCount() < MAX_BUGS) {
				ref.getCountNode().setText("\u221e"); // Infinity
				ref.getIndicator().setTitle("Too many bugs..");
				ref.getImage().setSrc("THEME/ui-bug-ovf.png");
			} else if(ct < MAX_BUGS) {
				ref.getCountNode().setText(Integer.toString(ct));
				ref.setLastCount(ref.getBugList().size());
			}
		}
	}

	protected static void clearMessages(BugRef ref) {
		if(ref.getWindow() != null) {
			ref.getWindow().remove();
			ref.setWindow(null);
		}
		if(ref.getIndicator() != null) {
			ref.getIndicator().remove();
			ref.setIndicator(null, null, null);
		}
		ref.getBugList().clear();
		ref.setLastCount(0);
	}

	/**
	 * Toggles the bug display floater.
	 * @param pg
	 * @param info
	 */
	protected static void toggleBugDisplay(Page pg, final BugRef ref) {
		if(ref.getWindow() != null) {
			//-- Discard the window and be done.
			ref.getWindow().remove();
			ref.setWindow(null);
			return;
		}

		//-- Create the floatert and link it.
		FloatingWindow fw = FloatingWindow.create(pg.getBody(), "Problem report", false);
		ref.setWindow(fw);

		//-- When the window is closed - clear messages
		fw.setOnClose(new IWindowClosed() {
			@Override
			public void closed(@Nonnull String closeReason) throws Exception {
				ref.setWindow(null);
				clearMessages(ref);
			}
		});

		//-- Show all bugs. Stop at MAX_BUGS.
		List<BugItem> list = new ArrayList<BugItem>(ref.getBugList());

		// REVERSE ORDER (descending date)
		Collections.sort(list, (a, b) -> DomUtil.compareNullableOnFunctions(b, a, Long::compareTo, (item -> item.getTimestamp().getTime()), (item -> Long.valueOf(item.getNumber()))));

		Table tbl = new Table();
		fw.add(tbl);
		TBody b = new TBody();
		tbl.add(b);

		for(int i = 0; i < list.size() && i < MAX_BUGS; i++) {
			BugItem bi = list.get(i);
			ItemPnl p = new ItemPnl(bi);
			b.add(p);
		}
	}

	static private class ItemPnl extends TR {
		private BugItem m_bi;

		private TD m_maintd;

		private Div m_detail;

		private Img m_clickimg;

		public ItemPnl(BugItem bi) {
			m_bi = bi;
		}

		@Override
		public void createContent() throws Exception {
			TD td = addCell();
			td.setVerticalAlign(VerticalAlignType.TOP);
			m_clickimg = new Img("THEME/xdt-collapsed.png");
			td.add(m_clickimg);
			m_clickimg.setAlign(ImgAlign.LEFT);
			m_clickimg.setClicked(new IClicked<Img>() {
				@Override
				public void clicked(@Nonnull Img clickednode) throws Exception {
					toggle();
				}
			});

			td = addCell();
			Div ttl = new Div();
			td.add(ttl);
			List<NodeBase> formattedMsg = m_bi.getFormattedMsg();
			if(formattedMsg == null) {
				DomUtil.renderHtmlString(ttl, m_bi.getMessage() + " [" + m_bi.getNumber() + "]");
			} else {
				for(NodeBase nb : formattedMsg)
					ttl.add(nb);
			}
			ttl.setCssClass("ui-bug-msg");
			m_maintd = td;
		}

		protected void toggle() {
			if(null != m_detail) {
				m_detail.remove();
				m_detail = null;
				m_clickimg.setSrc("THEME/xdt-collapsed.png");
				return;
			}

			//-- Expand
			m_clickimg.setSrc("THEME/xdt-expanded.png");
			m_detail = new Div();
			m_maintd.add(m_detail);
			m_detail.setCssClass("ui-bug-stk");

			//-- Show location stacktrace
			StringBuilder sb = new StringBuilder();
			StringTool.strStacktraceFiltered(sb, m_bi.getLocation(), PRESET, ENDSET, 40);
			m_detail.setText(sb.toString());
		}
	}

}
