package to.etc.webapp.query;

/**
 * All default and generic selection functions.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 14, 2009
 */
public enum QSelectionFunction {
	/** min(x) */
	MIN,

	/** max(x) */
	MAX,

	/** agv(x) */
	AVG,

	/** sum(x) */
	SUM,

	/** count() the #of rows */
	COUNT,

	/** Hibernate has this, but probably has no idea where to get this from */
	ROWCOUNT,

	/** A selector for the primary key */
	ID,

	/** A selector which represents a property of the object */
	PROPERTY,

	/** An user-defined (database specific) function. */
	USER
}
