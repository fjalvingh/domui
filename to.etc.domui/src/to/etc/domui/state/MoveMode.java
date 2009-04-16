package to.etc.domui.state;

public enum MoveMode {
	/** Create a NEW shelve: clear the current shelve and this page becomes the one on top. */
	NEW,

	/** Shelve the current page and add the new page above it. */
	SUB,

	/** Move back to the page above me. */
	BACK,

	/** Replace the "current" page with this page, but leave the shelve stack in order. */
	REPLACE,

	REDIRECT
}
