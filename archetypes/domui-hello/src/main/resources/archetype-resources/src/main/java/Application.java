package ${package};

import to.etc.domui.dom.html.*;
import ${package}.pages.*;
import to.etc.domui.server.*;

import javax.annotation.*;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 14-6-17.
 */
public class Application extends DomApplication {
	@Nullable @Override public Class<? extends UrlPage> getRootPage() {
		return HomePage.class;
	}
}
