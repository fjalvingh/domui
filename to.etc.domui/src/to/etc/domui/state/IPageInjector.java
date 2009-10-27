package to.etc.domui.state;

import to.etc.domui.dom.html.*;
import to.etc.domui.server.*;

public interface IPageInjector {
	void injectPageValues(final UrlPage page, final RequestContextImpl ctx, final PageParameters papa) throws Exception;
}
