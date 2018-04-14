package to.etc.domui.dom;

import org.eclipse.jdt.annotation.NonNull;
import to.etc.domui.server.IRequestContext;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 29-3-18.
 */
public interface IContributorRenderer {
	@NonNull IBrowserOutput o();

	@NonNull IRequestContext ctx();

	void renderLoadCSS(@NonNull String path) throws Exception;

	void renderLoadJavascript(@NonNull String path, boolean async, boolean defer) throws Exception;
}
