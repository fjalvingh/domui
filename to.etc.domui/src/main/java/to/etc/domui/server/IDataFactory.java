package to.etc.domui.server;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 30-11-21.
 */
public interface IDataFactory {
	void renderOutput(RequestContextImpl ctx) throws Exception;
}
