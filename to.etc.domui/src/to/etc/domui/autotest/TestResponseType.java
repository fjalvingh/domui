package to.etc.domui.autotest;

public enum TestResponseType {
	/** The page did not respond at all 8-/ */
	NOTHING,

	/** The page responded with an HTTP error. */
	ERROR,

	/** The page responded by sending a HTTP redirect */
	REDIRECT,

	/** The page responded with an output document. */
	DOCUMENT
}
