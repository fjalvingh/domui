package to.etc.domui.server;

import to.etc.domui.server.parts.*;

public class ResourceRequestHandler implements IFilterRequestHandler {
	//	private DomApplication		m_app;

	private PartRequestHandler m_prh;

	private InternalResourcePart m_rp = new InternalResourcePart();

	public ResourceRequestHandler(DomApplication app, PartRequestHandler prh) {
		//		m_app = app;
		m_prh = prh;
	}

	/**
	 * Handles requests for $ resources. It just delegates to a special part.
	 *
	 * @see to.etc.domui.server.IFilterRequestHandler#handleRequest(to.etc.domui.server.RequestContextImpl)
	 */
	public void handleRequest(RequestContextImpl ctx) throws Exception {
		//		String	url = ctx.getInputPath().substring(1);
		//		m_prh.generate(m_rp, ctx, url);
		m_prh.generate(m_rp, ctx, ctx.getInputPath());
	}
}
