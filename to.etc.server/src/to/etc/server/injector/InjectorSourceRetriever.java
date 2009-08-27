package to.etc.server.injector;

/**
 * Callback to retrieve an instance of an injector source class.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Nov 17, 2006
 */
public interface InjectorSourceRetriever {
	public Object getInjectorSource(Class< ? > sourcecl) throws Exception;
}
