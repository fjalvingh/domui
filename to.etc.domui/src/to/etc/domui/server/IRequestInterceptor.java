package to.etc.domui.server;

/**
 * Interceptor for filter requests.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Sep 24, 2008
 */
public interface IRequestInterceptor {
	public void before(RequestContext rc) throws Exception;

	public void after(RequestContext rc, Exception x) throws Exception;
}
