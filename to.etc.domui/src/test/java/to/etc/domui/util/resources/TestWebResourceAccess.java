package to.etc.domui.util.resources;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 9-9-26.
 */
public class TestWebResourceAccess {
	@Test
	public void testNormalResourcesAreAllowed() {
		Assert.assertFalse(WebResourceAccess.isForbidden("js/domui.js"));
		Assert.assertFalse(WebResourceAccess.isForbidden("$themes/domui/style.scss"));
		Assert.assertFalse(WebResourceAccess.isForbidden("img/web-info.png"));
		Assert.assertFalse(WebResourceAccess.isForbidden("resources/webinf/thingy.css"));
	}

	@Test
	public void testProtectedDirectoriesAreRefused() {
		Assert.assertTrue(WebResourceAccess.isForbidden("WEB-INF/web.xml"));
		Assert.assertTrue(WebResourceAccess.isForbidden("/WEB-INF/app.properties"));
		Assert.assertTrue(WebResourceAccess.isForbidden("META-INF/MANIFEST.MF"));
		Assert.assertTrue(WebResourceAccess.isForbidden("js/../WEB-INF/web.xml"));
		Assert.assertTrue(WebResourceAccess.isForbidden("WEB-INF/classes/log4j2.xml"));
	}

	/**
	 * The name of a protected directory must be recognized whatever case, separator or Windows style
	 * padding with dots and spaces is used to write it.
	 */
	@Test
	public void testProtectedDirectoriesAreRefusedWhenObfuscated() {
		Assert.assertTrue(WebResourceAccess.isForbidden("web-inf/web.xml"));
		Assert.assertTrue(WebResourceAccess.isForbidden("Web-Inf/web.xml"));
		Assert.assertTrue(WebResourceAccess.isForbidden("WEB-INF./web.xml"));
		Assert.assertTrue(WebResourceAccess.isForbidden("WEB-INF /web.xml"));
		Assert.assertTrue(WebResourceAccess.isForbidden("WEB-INF\\web.xml"));
		Assert.assertTrue(WebResourceAccess.isForbidden("js\\..\\WEB-INF\\web.xml"));
	}

	@Test
	public void testEscapingTheWebappRootIsRefused() {
		Assert.assertTrue(WebResourceAccess.isForbidden("../../etc/passwd"));
		Assert.assertTrue(WebResourceAccess.isForbidden("js/../../etc/passwd"));
	}

	@Test(expected = SecurityException.class)
	public void testCheckAllowedThrowsOnProtectedPath() {
		WebResourceAccess.checkAllowed("WEB-INF/web.xml");
	}

	@Test
	public void testCheckAllowedAcceptsNormalPath() {
		WebResourceAccess.checkAllowed("js/domui.js");
	}
}
