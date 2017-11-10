package to.etc.domui.login;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 10-11-17.
 */
public class UILogin {
	static private volatile ILoginHandler	m_loginHandler = new DefaultLoginHandler();

	public static ILoginHandler getLoginHandler() {
		return m_loginHandler;
	}
}
