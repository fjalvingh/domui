package to.etc.domuidemo.pages.test.uitest.test;

import to.etc.domui.webdriver.core.WebDriverConnector;
import to.etc.domui.webdriver.poproxies.CpNodeAsText;

/**
 * The page object for OrderEntryTestPage. Everything the generator recognized
 * sits in {@link POOrderEntryTestPageBase}, which is regenerated whenever the
 * page changes; this class holds what the generator cannot know:
 *
 * <ul>
 *	<li>the answer is a plain Div, and there is no proxy generator for a Div,
 *		so its accessor is written here;</li>
 *	<li>"order this album" is a step in the vocabulary of the tests, not a
 *		component of the page.</li>
 * </ul>
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class POOrderEntryTestPage extends POOrderEntryTestPageBase {
	private CpNodeAsText m_answer;

	public POOrderEntryTestPage(WebDriverConnector connector) {
		super(connector);
	}

	public CpNodeAsText answer() {
		CpNodeAsText answer = m_answer;
		if(null == answer) {
			answer = new CpNodeAsText(wd(), () -> WebDriverConnector.getTestIDSelector("answer"));
			m_answer = answer;
		}
		return answer;
	}

	/**
	 * Order the album with this title, whichever row of the basket it is in.
	 */
	public void order(String album) throws Exception {
		for(int i = 0; i < basket().getVisibleRowCount(); i++) {
			if(album.equals(basket().row(i).album().getText())) {
				basket().row(i).order().click();
				return;
			}
		}
		throw new IllegalStateException("The basket does not contain " + album);
	}
}
