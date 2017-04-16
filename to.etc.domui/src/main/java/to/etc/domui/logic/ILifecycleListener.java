package to.etc.domui.logic;

public interface ILifecycleListener {
	public void containerInitialize() throws Exception;

	public void containerTerminate() throws Exception;

	public void containerDetached() throws Exception;

	public void containerAttached() throws Exception;
}
