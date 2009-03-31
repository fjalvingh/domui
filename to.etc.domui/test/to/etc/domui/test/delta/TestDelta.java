package to.etc.domui.test.delta;

import java.io.*;

import org.junit.*;
import static org.junit.Assert.*;

import to.etc.domui.dom.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.server.*;
import to.etc.domui.test.*;
import to.etc.domui.test.util.*;

public class TestDelta {
	private Page	createPage() throws Exception {
		Page	p = TestUtil.createPage(UrlPage.class);
		for(int i = 0; i < 10; i++) {
			Div d2 = new Div();
			d2.setText("This is line "+i);
			p.getBody().add(d2);
		}
		return p;
	}

	private Page	createRenderedPage() throws Exception {
		Page p  = createPage();
		getFullRenderText(p);
		return p;
	}

	@Test
	public void	testSingleDelete() throws Exception {
		Page	p = TestUtil.createPage(UrlPage.class);
		for(int i = 0; i < 10; i++) {
			Div d2 = new Div();
			d2.setText("This is line "+i);
			p.getBody().add(d2);
		}

		//-- Base page known; render it initially
		String full = getFullRenderText(p);

		//-- At this time NO node may be dirty, as all is rendered
		p.getBody().visit(new DirtyNodeChecker());
		System.out.println("Rendered: "+full);

		//-- Now delete a single node
		p.getBody().getChild(5).remove();

		String render = getDeltaRenderText(p);
		System.out.println("\n\n---- Render after remove node 5 in child list ----\n"+render);
		p.getBody().visit(new DirtyNodeChecker());

		assertEquals(render, "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
			+	"<delta>\n"
			+	"  <remove select=\"#_C\"/>\n"
			+	"  <eval>WebUI.cancelPolling();</eval>\n"
			+	"</delta>\n"
		);

		//-- Remove another node
		p.getBody().getChild(1).remove();
		render = getDeltaRenderText(p);
		System.out.println("\n\n---- Render after remove node 1 in child list ----\n"+render);
		p.getBody().visit(new DirtyNodeChecker());
		
		
		
	}

	
	public String	getFullRenderText(Page pg) throws Exception {
		StringWriter	sw	= new StringWriter();
		BrowserOutput	ro	= new PrettyXmlOutputWriter(sw);
		HtmlRenderer	bhr = new HtmlRenderer(ro);
		FullHtmlRenderer	hr = new FullHtmlRenderer(bhr, ro);

		RequestContext	ctx = new TestRequestContext();
		hr.render(ctx, pg);
		pg.clearDeltaFully();
		return sw.getBuffer().toString();
	}

	public String	getDeltaRenderText(Page pg) throws Exception {
		StringWriter	sw	= new StringWriter();
		BrowserOutput	ro	= new PrettyXmlOutputWriter(sw);
		HtmlRenderer	bhr = new HtmlRenderer(ro);
		OptimalDeltaRenderer	hr = new OptimalDeltaRenderer(bhr, ro);

		RequestContext	ctx = new TestRequestContext();
		hr.render(ctx, pg);
		return sw.getBuffer().toString();
	}

	static private Div	getIntDiv(Page p) {
		return p.getBody();
	}

	@Test
	public void	testSingleAdd() throws Exception {
		Page	p = createRenderedPage();

		Div	d = new Div();
		d.setText("new@5");
		getIntDiv(p).add(5, d);

		String render = getDeltaRenderText(p);
		System.out.println("\n\n---- Render after adding node@5 in child list ----\n"+render);
	}

	/**
	 * Testcase for Jo's problem with "Hell Freezeth over" exception, pass 1.
	 */
	@Test
	public void	testHellFreeze1() throws Exception {
		//-- 1st render: create nested structure fully contained and render it.
		Page	p = TestUtil.createPage(UrlPage.class);
		UrlPage	up = p.getBody();
		Div	root	= new Div();
		up.add(root);
		Div		popin	= new Div();
		root.add(popin);
		popin.add(new Div("Fixed content"));
		Div	content = new Div("Blabla");
		popin.add(content);
		System.out.println("root="+root.getActualID()+", popin="+popin.getActualID()+", content="+content.getActualID());

		//-- Render 1: full render.
		getFullRenderText(p);
		for(String s: p.internalNodeMap().keySet())
			System.out.println("INITIAL key="+s);

		//-- 2nd phase: remove the inner content (but keep its instances), then delta; this fills the old state.
		System.out.println("------- step 2: remove popin ---------");
		popin.remove();
//		for(String s: p.internalNodeMap().keySet())
//			System.out.println("AFTER popin REMOVE key="+s);
//		for(String s: p.getBeforeMap().keySet())
//			System.out.println("AFTER popin OLDMAP key="+s);
		
		getDeltaRenderText(p);

		//-- 3rd phase: add the popin, then remove it's contents;
		System.out.println("------- step 3: add popin, then remove contents ---------");
		root.add(popin);					// Add back,
		content.remove();					// and AFTER THAT remove it's contents - this fucks up the state,
		getDeltaRenderText(p);

		//-- At this point we would have stale pointers in content. Now do zhe loop again,
		System.out.println("------- step 4: add back contents and die ---------");
		popin.add(content);
		getDeltaRenderText(p);
	}
	
}
