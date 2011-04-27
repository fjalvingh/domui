package to.etc.domui.util.js;

public interface IScriptScope {
	/**
	 * Return the value for the specified variable in this scope.
	 * @param name
	 * @return
	 */
	Object getValue(String name);

	/**
	 * Put a value inside the scope.
	 * @param name
	 * @param instance
	 */
	void put(String name, Object instance);

	/**
	 * Create a new writable scope that has this scope as the "delegate". This new scope
	 * is writable.
	 * @return
	 */
	IScriptScope newScope();

	<T> T getAdapter(Class<T> clz);
}
