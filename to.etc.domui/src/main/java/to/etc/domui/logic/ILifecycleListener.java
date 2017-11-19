package to.etc.domui.logic;

public interface ILifecycleListener {
	void containerInitialize() throws Exception;

	void containerTerminate() throws Exception;

	void containerDetached() throws Exception;

	void containerAttached() throws Exception;
}
