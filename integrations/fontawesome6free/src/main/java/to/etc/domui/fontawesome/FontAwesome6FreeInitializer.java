package to.etc.domui.fontawesome;

import to.etc.domui.dom.header.HeaderContributor;
import to.etc.domui.server.DomApplication;
import to.etc.domui.server.IApplicationInitializer;

/**
 */
final public class FontAwesome6FreeInitializer implements IApplicationInitializer {
	public void onStartInitialization(DomApplication da) {
	}

	public void onEndInitialization(DomApplication da) {
		System.out.println("domui: FontAwesome 6 FREE registered");
		//da.addHeaderContributor(HeaderContributor.loadStylesheet("https://use.fontawesome.com/releases/v6.1.1/css/all.css", "integrity", "sha384-fdfaead52457607fdb4b2a2efc7529fe189bed154dd536b04188daeb9f0510ea1d47f15004a56a4fd3a5f8ecf73a5ab9", "crossorigin", "anonymous"), 10);
		da.addHeaderContributor(HeaderContributor.loadStylesheet("css/font-awesome.min.css"), 10);
		FaIcon.initializeIcons();

	}

	public void onAfterDestroy(DomApplication da) {
	}
}
