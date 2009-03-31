package to.etc.domui.server;

public interface FilterRequestHandler {
	public void handleRequest(RequestContextImpl ctx) throws Exception;
}
