package to.etc.domuidemo.pages.lookupform2;

import to.etc.domuidemo.pages.MenuPage;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 3-2-18.
 */
public class Lookup2FormMenuPage extends MenuPage {
	public Lookup2FormMenuPage() {
		super("LookupForm2 demo's");
	}

	@Override public void createContent() throws Exception {
		addLink(Lookup2Manual.class, "Manual configuration");
		addLink(Lookup2Metadata1.class, "Using metadata");
	}
}
