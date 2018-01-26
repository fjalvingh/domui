package to.etc.domui.login;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 10-11-17.
 */
public enum LoginResult {
	SUCCESS
	, FAILED

	/** Ignored means: logins will be ignored for the time specified in DefaultLoginHandler. */
	, IGNORED
}
