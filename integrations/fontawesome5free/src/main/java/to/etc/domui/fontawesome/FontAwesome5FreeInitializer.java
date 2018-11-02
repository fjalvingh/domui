package to.etc.domui.fontawesome;

import to.etc.domui.dom.header.HeaderContributor;
import to.etc.domui.server.DomApplication;
import to.etc.domui.server.IApplicationInitializer;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 20-10-18.
 */
final public class FontAwesome5FreeInitializer implements IApplicationInitializer {
	public void onStartInitialization(DomApplication da) {
	}

	public void onEndInitialization(DomApplication da) {
		System.out.println("domui: FontAwesome 5 FREE registered");
		da.addHeaderContributor(HeaderContributor.loadStylesheet("https://use.fontawesome.com/releases/v5.4.1/css/all.css", "integrity", "sha384-5sAR7xN1Nv6T6+dT2mhtzEpVJvfS3NScPQTrOxhwjIuvcA67KV2R5Jz6kr4abQsz", "crossorigin", "anonymous"), 10);
		FaIcon.initializeIcons();
	}

	public void onAfterDestroy(DomApplication da) {
	}
}
