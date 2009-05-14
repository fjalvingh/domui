package to.etc.iocular;

public enum BindingScope {
	/**
	 * Singleton scope: the object gets created only once within the container, and is
	 * reused when the same object is required again.
	 */
	SINGLETON,

	/**
	 * This type gets initialized every time an object is needed. It gets destroyed only
	 * when it's end-of-life (garbage collected).
	 */
	PROTOTYPE,

}
