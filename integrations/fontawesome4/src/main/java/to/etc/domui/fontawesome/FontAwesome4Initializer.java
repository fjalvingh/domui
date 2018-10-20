package to.etc.domui.fontawesome;

import to.etc.domui.dom.header.HeaderContributor;
import to.etc.domui.server.DomApplication;
import to.etc.domui.server.IApplicationInitializer;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 20-10-18.
 */
final public class FontAwesome4Initializer implements IApplicationInitializer {
	public void onStartInitialization(DomApplication da) {

	}

	public void onEndInitialization(DomApplication da) {
		da.addHeaderContributor(HeaderContributor.loadStylesheet("fonts/font-awesome.min.css"), 10);
	}

	public void onAfterDestroy(DomApplication da) {
	}
}
