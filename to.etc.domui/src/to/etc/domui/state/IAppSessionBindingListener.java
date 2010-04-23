package to.etc.domui.state;

public interface IAppSessionBindingListener {
	void boundToSession(AppSession ses, String name);

	void unboundFromSession(AppSession ses, String name);
}
