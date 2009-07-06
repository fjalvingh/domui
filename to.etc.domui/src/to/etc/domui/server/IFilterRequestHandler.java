package to.etc.domui.server;

public interface IFilterRequestHandler {
	public void handleRequest(RequestContextImpl ctx) throws Exception;
}
