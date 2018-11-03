package to.etc.domui.fontawesome;

import to.etc.domui.component.misc.Icon;
import to.etc.domui.dom.header.HeaderContributor;
import to.etc.domui.server.DomApplication;
import to.etc.domui.server.IApplicationInitializer;

import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 20-10-18.
 */
final public class FontAwesome4Initializer implements IApplicationInitializer {
	public void onStartInitialization(DomApplication da) {

	}

	public void onEndInitialization(DomApplication da) {
		System.out.println("domui: FontAwesome 4.7.0 registered");
		da.addHeaderContributor(HeaderContributor.loadStylesheet("fonts/font-awesome.min.css"), 10);

		//-- Initialize the Icon map
		Map<String, FaIcon> map = new HashMap<String, FaIcon>();
		for(FaIcon icon : FaIcon.values()) {
			map.put(icon.name(), icon);
		}

		for(Icon icon : Icon.values()) {
			FaIcon alt = map.get(icon.name());
			if(null == alt)
				throw new IllegalStateException("No implementation known for Icon." + icon.name());
			Icon.setIcon(icon, alt);
		}
	}

	public void onAfterDestroy(DomApplication da) {
	}
}
