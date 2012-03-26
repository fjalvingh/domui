package to.etc.domui.server;

import javax.annotation.*;

import to.etc.domui.server.*;
import to.etc.domui.server.parts.*;

/**
 * This is a request handler for <i>DomUI Parts</i> that need to react
 * to an URL pattern.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Mar 26, 2012
 */
final class UrlPartRequestHandler implements IFilterRequestHandler {
	@Nonnull
	final private IUrlPart m_factory;

	@Nonnull
	final private PartRequestHandler m_partHandler;

	public UrlPartRequestHandler(PartRequestHandler partRequestHandler, @Nonnull IUrlPart factory) {
		m_partHandler = partRequestHandler;
		m_factory = factory;
	}

	/**
	 * Accept if the factory accepts this path.
	 * @see to.etc.domui.server.IFilterRequestHandler#accepts(to.etc.domui.server.IRequestContext)
	 */
	@Override
	public boolean accepts(@Nonnull IRequestContext ri) throws Exception {
		return m_factory.accepts(ri.getInputPath());
	}

	@Override
	public void handleRequest(@Nonnull RequestContextImpl ctx) throws Exception {
		m_partHandler.renderUrlPart(m_factory, ctx);
	}
}
