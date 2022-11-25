package to.etc.domui.dom;

import org.eclipse.jdt.annotation.NonNull;
import to.etc.domui.dom.html.Page;
import to.etc.domui.server.IRequestContext;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 29-3-18.
 */
public interface IContributorRenderer {
	@NonNull IBrowserOutput o();

	@NonNull IRequestContext ctx();

	@NonNull Page getPage();

	boolean isXml();

	void renderLoadCSS(@NonNull String path,String... options) throws Exception;

	void renderLoadJavascript(@NonNull String path, boolean async, boolean defer) throws Exception;
}
