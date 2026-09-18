package to.etc.domui.util;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for {@link HtmlUtil#removeUnsafe(String)}: what it must keep, and - more
 * importantly - what it must take out.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class HtmlUtilTest {
	private static void assertContains(String html, String what) {
		Assert.assertTrue("Expected [" + what + "] in [" + html + "]", html.contains(what));
	}

	private static void assertLacks(String html, String what) {
		Assert.assertFalse("Did not expect [" + what + "] in [" + html + "]", html.contains(what));
	}

	/*----------------------------------------------------------------------*/
	/*	CODING:	What must survive										*/
	/*----------------------------------------------------------------------*/

	@Test
	public void testFormattingIsKept() {
		String out = HtmlUtil.removeUnsafe("<b>bold</b> and <i>italic</i><br>and a<ul><li>list</li></ul>");
		assertContains(out, "<b>bold</b>");
		assertContains(out, "<i>italic</i>");
		assertContains(out, "<li>list</li>");
	}

	@Test
	public void testOrdinaryLinkIsKept() {
		String out = HtmlUtil.removeUnsafe("<a href=\"https://domui.org/\">DomUI</a>");
		assertContains(out, "href=\"https://domui.org/\"");
		assertContains(out, ">DomUI</a>");
	}

	@Test
	public void testRelativeLinkIsKept() {
		assertContains(HtmlUtil.removeUnsafe("<a href=\"/some/page.ui\">x</a>"), "href=\"/some/page.ui\"");
		assertContains(HtmlUtil.removeUnsafe("<a href=\"page.ui?a=1\">x</a>"), "href=\"page.ui?a=1\"");
		assertContains(HtmlUtil.removeUnsafe("<a href=\"#anchor\">x</a>"), "href=\"#anchor\"");
		assertContains(HtmlUtil.removeUnsafe("<a href=\"mailto:jal@etc.to\">x</a>"), "href=\"mailto:jal@etc.to\"");
	}

	@Test
	public void testOrdinaryStyleIsKept() {
		String out = HtmlUtil.removeUnsafe("<div style=\"color: red; font-weight: bold\">x</div>");
		assertContains(out, "style=\"color: red; font-weight: bold\"");
	}

	/*----------------------------------------------------------------------*/
	/*	CODING:	What must not survive									*/
	/*----------------------------------------------------------------------*/

	@Test
	public void testScriptElementAndItsContentAreRemoved() {
		String out = HtmlUtil.removeUnsafe("before<script>alert('x')</script>after");
		assertLacks(out, "script");
		assertLacks(out, "alert");							// The *content* must go too, not just the tags
		assertContains(out, "before");
		assertContains(out, "after");
	}

	@Test
	public void testUnclosedScriptRemovesTheRestOfTheInput() {
		String out = HtmlUtil.removeUnsafe("before<script>alert('x')");
		assertLacks(out, "alert");
		assertContains(out, "before");
	}

	@Test
	public void testOtherContentKillingElementsAreRemoved() {
		assertLacks(HtmlUtil.removeUnsafe("a<style>body{color:red}</style>b"), "color");
		assertLacks(HtmlUtil.removeUnsafe("a<iframe src=\"https://x/\">frame</iframe>b"), "iframe");
		assertLacks(HtmlUtil.removeUnsafe("a<object data=\"x\">obj</object>b"), "object");
		assertLacks(HtmlUtil.removeUnsafe("a<svg><script>alert(1)</script></svg>b"), "alert");
	}

	@Test
	public void testRejectedElementKeepsItsText() {
		//-- A table is not allowed, but the text in its cells is text: only the tags go.
		String out = HtmlUtil.removeUnsafe("<table><tr><td>a cell</td></tr></table>");
		assertLacks(out, "<table");
		assertContains(out, "a cell");
	}

	@Test
	public void testScriptSchemeInHrefIsRemoved() {
		String out = HtmlUtil.removeUnsafe("<a href=\"javascript:alert(1)\">press</a>");
		assertLacks(out, "javascript");
		assertContains(out, ">press</a>");					// The link text stays, the href does not
	}

	@Test
	public void testScriptSchemeVariationsAreRemoved() {
		//-- Case, leading whitespace, and characters a browser ignores inside the scheme.
		assertLacks(HtmlUtil.removeUnsafe("<a href=\"JaVaScRiPt:alert(1)\">x</a>"), "alert");
		assertLacks(HtmlUtil.removeUnsafe("<a href=\"  javascript:alert(1)\">x</a>"), "alert");
		assertLacks(HtmlUtil.removeUnsafe("<a href=\"java\tscript:alert(1)\">x</a>"), "alert");
		assertLacks(HtmlUtil.removeUnsafe("<a href=\"java\nscript:alert(1)\">x</a>"), "alert");
		assertLacks(HtmlUtil.removeUnsafe("<a href=\"vbscript:msgbox(1)\">x</a>"), "vbscript");
		assertLacks(HtmlUtil.removeUnsafe("<a href=\"data:text/html;base64,PHNjcmlwdD4=\">x</a>"), "data:");
	}

	@Test
	public void testEntityEncodedScriptSchemeIsRemoved() {
		//-- Entities are decoded before the check, so this must not slip through either.
		String out = HtmlUtil.removeUnsafe("<a href=\"&#106;avascript:alert(1)\">x</a>");
		assertLacks(out, "alert");
	}

	@Test
	public void testUnsafeStyleIsRemoved() {
		assertLacks(HtmlUtil.removeUnsafe("<div style=\"background:url(javascript:alert(1))\">x</div>"), "javascript");
		assertLacks(HtmlUtil.removeUnsafe("<div style=\"width:expression(alert(1))\">x</div>"), "expression");
		assertLacks(HtmlUtil.removeUnsafe("<div style=\"behavior:url(#x)\">x</div>"), "behavior");
		assertLacks(HtmlUtil.removeUnsafe("<div style=\"width:expr/**/ession(alert(1))\">x</div>"), "ession");
	}

	@Test
	public void testEventHandlerAttributesAreRemoved() {
		String out = HtmlUtil.removeUnsafe("<div onclick=\"alert(1)\" onmouseover=\"alert(2)\">x</div>");
		assertLacks(out, "onclick");
		assertLacks(out, "alert");
	}

	@Test
	public void testTargetGetsNoopener() {
		String out = HtmlUtil.removeUnsafe("<a href=\"https://domui.org/\" target=\"_blank\">x</a>");
		assertContains(out, "rel=\"noopener noreferrer\"");
	}

	@Test
	public void testDomUiIdsCannotBeHijacked() {
		//-- An id starting with _ could collide with a DomUI node id in the browser.
		assertLacks(HtmlUtil.removeUnsafe("<div id=\"_A1\">x</div>"), "_A1");
		assertContains(HtmlUtil.removeUnsafe("<div id=\"mine\">x</div>"), "id=\"mine\"");
	}

	@Test
	public void testNullIsNull() {
		Assert.assertNull(HtmlUtil.removeUnsafe(null));
	}
}
