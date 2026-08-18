package to.etc.domui.test.render;

import org.junit.Test;
import to.etc.domui.dom.HtmlFullRenderer;
import to.etc.domui.dom.IBrowserOutput;
import to.etc.domui.dom.PrettyXmlOutputWriter;
import to.etc.domui.dom.html.Input;
import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.dom.html.Page;
import to.etc.domui.dom.html.TextArea;
import to.etc.domui.dom.html.UrlPage;
import to.etc.domui.server.BrowserVersion;
import to.etc.domui.server.IRequestContext;
import to.etc.domui.state.UIContext;
import to.etc.domui.testsupport.TUtilDomUI;
import to.etc.domui.testsupport.TestRequestContext;

import java.io.StringWriter;

import static org.junit.Assert.assertTrue;

/**
 * Checks that inputs are rendered with the hints that keep browser password managers away
 * from fields that are not credential fields.
 */
public class TestInputRenderer {
	@Test
	public void testNormalInputBlocksPasswordManagers() throws Exception {
		String html = renderInput(new Input());
		assertTrue(html, html.contains("autocomplete=\"off\""));
		assertTrue(html, html.contains("data-1p-ignore=\"true\""));
		assertTrue(html, html.contains("data-lpignore=\"true\""));
		assertTrue(html, html.contains("data-bwignore=\"true\""));
		assertTrue(html, html.contains("data-form-type=\"other\""));
	}

	@Test
	public void testCredentialInputAllowsPasswordManagers() throws Exception {
		Input in = new Input();
		in.setInputType("password");
		in.setPasswordManagerAllowed(true);
		String html = renderInput(in);
		assertTrue(html, html.contains("autocomplete=\"on\""));
		assertTrue(html, !html.contains("data-1p-ignore"));
		assertTrue(html, !html.contains("data-lpignore"));
		assertTrue(html, !html.contains("data-bwignore"));
		assertTrue(html, !html.contains("data-form-type"));
	}

	@Test
	public void testExplicitAutocompleteIsNotOverwritten() throws Exception {
		Input in = new Input();
		in.setSpecialAttribute("autocomplete", "new-password");
		String html = renderInput(in);
		assertTrue(html, html.contains("autocomplete=\"new-password\""));
		assertTrue(html, !html.contains("autocomplete=\"off\""));
		assertTrue(html, html.contains("data-1p-ignore=\"true\""));
	}

	@Test
	public void testTextAreaBlocksPasswordManagers() throws Exception {
		String html = renderInput(new TextArea(40, 5));
		assertTrue(html, html.contains("autocomplete=\"off\""));
		assertTrue(html, html.contains("data-1p-ignore=\"true\""));
		assertTrue(html, html.contains("data-lpignore=\"true\""));
		assertTrue(html, html.contains("data-bwignore=\"true\""));
		assertTrue(html, html.contains("data-form-type=\"other\""));
	}

	@Test
	public void testTextAreaCanAllowPasswordManagers() throws Exception {
		TextArea ta = new TextArea(40, 5);
		ta.setPasswordManagerAllowed(true);
		String html = renderInput(ta);
		assertTrue(html, html.contains("autocomplete=\"on\""));
		assertTrue(html, !html.contains("data-1p-ignore"));
	}

	private String renderInput(NodeBase in) throws Exception {
		Page pg = TUtilDomUI.createPage(UrlPage.class);
		pg.getBody().add(in);

		StringWriter sw = new StringWriter();
		IBrowserOutput ro = new PrettyXmlOutputWriter(sw);
		BrowserVersion bv = BrowserVersion.parseUserAgent("Mozilla/5.0 (X11; Linux x86_64) Chrome/130.0.0.0");
		HtmlFullRenderer hr = TUtilDomUI.getApplication().findRendererFor(bv, ro);

		pg.internalFullBuild();
		IRequestContext ctx = new TestRequestContext();
		UIContext.internalSet(ctx);
		hr.render(ctx, pg);
		return sw.getBuffer().toString();
	}
}
