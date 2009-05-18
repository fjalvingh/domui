package to.etc.iocular.def;

public enum ComponentPropertyMode {
	/** Do nothing with properties that are not explicitly named */
	NONE,

	/** Force values on all properties and abort if a property cannot be set because no source is available */
	ALL,

	/** Set values on all properties for which a value can be found; ignore all else. */
	KNOWN
}
