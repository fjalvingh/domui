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
package to.etc.domui.test.delta;

import org.junit.*;
import to.etc.domui.dom.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.server.*;
import to.etc.domui.state.UIContext;
import to.etc.domui.test.*;
import to.etc.domui.testsupport.*;

import java.io.*;

import static org.junit.Assert.*;

public class TestDelta {
	private Page createPage() throws Exception {
		Page p = TUtilDomUI.createPage(UrlPage.class);
		for(int i = 0; i < 10; i++) {
			Div d2 = new Div();
			d2.setText("This is line " + i);
			p.getBody().add(d2);
		}
		return p;
	}

	private Page createRenderedPage() throws Exception {
		Page p = createPage();
		getFullRenderText(p);
		return p;
	}

	@Test
	public void testSingleDelete() throws Exception {
		Page p = TUtilDomUI.createPage(UrlPage.class);
		for(int i = 0; i < 10; i++) {
			Div d2 = new Div();
			d2.setText("This is line " + i);
			p.getBody().add(d2);
		}

		//-- Base page known; render it initially
		String full = getFullRenderText(p);

		//-- At this time NO node may be dirty, as all is rendered
		p.getBody().visit(new DirtyNodeChecker());
		//System.out.println("Rendered: " + full);

		//-- Now delete a single node
		p.getBody().getChild(5).remove();

		String render = getDeltaRenderText(p);
		//System.out.println("\n\n---- Render after remove node 5 in child list ----\n" + render);
		p.getBody().visit(new DirtyNodeChecker());

		assertEquals(render, "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" + "<delta>\n" + "  <remove select=\"#_C\"/>\n" + "  <eval>WebUI.cancelPolling();</eval>\n" + "</delta>\n");

		//-- Remove another node
		p.getBody().getChild(1).remove();
		render = getDeltaRenderText(p);
		//System.out.println("\n\n---- Render after remove node 1 in child list ----\n" + render);
		p.getBody().visit(new DirtyNodeChecker());
	}

	public BrowserVersion getBrowserVersion() {
		return BrowserVersion.parseUserAgent("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; .NET CLR 2.0.50727)");
	}

	public String getFullRenderText(Page pg) throws Exception {
		return getFullRenderText(getBrowserVersion(), pg);
	}

	public HtmlFullRenderer getFullRenderer(IBrowserOutput o) throws Exception {
		BrowserVersion	bv = getBrowserVersion();
		return TUtilDomUI.getApplication().findRendererFor(bv, o);
	}

	public String getFullRenderText(BrowserVersion bv, Page pg) throws Exception {
		StringWriter sw = new StringWriter();
		IBrowserOutput ro = new PrettyXmlOutputWriter(sw);
		HtmlFullRenderer hr = getFullRenderer(ro);

		pg.internalFullBuild();
		IRequestContext ctx = new TestRequestContext();
		UIContext.internalSet(ctx);
		hr.render(ctx, pg);
		pg.internalClearDeltaFully();
		return sw.getBuffer().toString();
	}

	public String getDeltaRenderText(Page pg) throws Exception {
		return getDeltaRenderText(getBrowserVersion(), pg);
	}
	public String getDeltaRenderText(BrowserVersion bv, Page pg) throws Exception {
		StringWriter sw = new StringWriter();
		IBrowserOutput ro = new PrettyXmlOutputWriter(sw);
		IRequestContext ctx = new TestRequestContext();
		HtmlFullRenderer hr = getFullRenderer(ro);
		pg.internalDeltaBuild();
		OptimalDeltaRenderer odr = new OptimalDeltaRenderer(hr, ctx, pg);
		odr.render();
		return sw.getBuffer().toString();
	}

	static private Div getIntDiv(Page p) {
		return p.getBody();
	}

	@Test
	public void testSingleAdd() throws Exception {
		Page p = createRenderedPage();

		Div d = new Div();
		d.setText("new@5");
		getIntDiv(p).add(5, d);

		String render = getDeltaRenderText(p);
		//System.out.println("\n\n---- Render after adding node@5 in child list ----\n" + render);
	}

	/**
	 * Testcase for Jo's problem with "Hell Freezeth over" exception, pass 1.
	 */
	@Test
	public void testHellFreeze1() throws Exception {
		//-- 1st render: create nested structure fully contained and render it.
		Page p = TUtilDomUI.createPage(UrlPage.class);
		UrlPage up = p.getBody();
		Div root = new Div();
		up.add(root);
		Div popin = new Div();
		root.add(popin);
		popin.add(new MsgDiv("Fixed content"));
		Div content = new MsgDiv("Blabla");
		popin.add(content);
		//System.out.println("root=" + root.getActualID() + ", popin=" + popin.getActualID() + ", content=" + content.getActualID());

		//-- Render 1: full render.
		getFullRenderText(p);
		//for(String s : p.internalNodeMap().keySet())
		//	System.out.println("INITIAL key=" + s);

		//-- 2nd phase: remove the inner content (but keep its instances), then delta; this fills the old state.
		//System.out.println("------- step 2: remove popin ---------");
		popin.remove();
		//		for(String s: p.internalNodeMap().keySet())
		//			System.out.println("AFTER popin REMOVE key="+s);
		//		for(String s: p.getBeforeMap().keySet())
		//			System.out.println("AFTER popin OLDMAP key="+s);

		getDeltaRenderText(p);

		//-- 3rd phase: add the popin, then remove it's contents;
		//System.out.println("------- step 3: add popin, then remove contents ---------");
		root.add(popin); // Add back,
		content.remove(); // and AFTER THAT remove it's contents - this fucks up the state,
		getDeltaRenderText(p);

		//-- At this point we would have stale pointers in content. Now do zhe loop again,
		//System.out.println("------- step 4: add back contents and die ---------");
		popin.add(content);
		getDeltaRenderText(p);
	}

}
