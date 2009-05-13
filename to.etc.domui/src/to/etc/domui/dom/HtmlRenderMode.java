package to.etc.domui.dom;

public enum HtmlRenderMode {
	/** Rendering FULLY, i.e. not an optimal-delta-rendering. */
	FULL,

	/** Rendering node attributes only */
	ATTR,

	/** Rendering node(s) that were ADDED to the model */
	ADDS,

	/** Rendering a full replacement for nodes */
	REPL,
	
}
