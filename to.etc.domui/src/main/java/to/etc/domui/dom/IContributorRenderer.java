package to.etc.domui.dom;

import to.etc.domui.server.IRequestContext;

import javax.annotation.Nonnull;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 29-3-18.
 */
public interface IContributorRenderer {
	@Nonnull IBrowserOutput o();

	@Nonnull IRequestContext ctx();

	void renderLoadCSS(@Nonnull String path) throws Exception;

	void renderLoadJavascript(@Nonnull String path) throws Exception;
}
