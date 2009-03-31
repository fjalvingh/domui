package to.etc.domui.util;

/**
 * Constants for errors within the framework.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 11, 2008
 */
public interface Msgs {
	public static final String	UNEXPECTED_EXCEPTION = "ui.unexpected_exception";
	public static final	String	MANDATORY = "ui.mandatory";
	public static final String	NOT_VALID = "ui.invalid";
	
	public static final String	UI_BOOL_TRUE	= "ui.boolean.true";
	public static final String	UI_BOOL_FALSE	= "ui.boolean.false";

	/** Page x of y */
	public static final String	UI_PAGER_TEXT = "ui.pagertext";
	/** No results */
	public static final String	UI_PAGER_EMPTY	= "ui.pagerempty";

	/** No results (data table) */
	public static final String	UI_DATATABLE_EMPTY	= "ui.dt.empty";
	
	/** The result is truncated to {0} rows */
	public static final String	UI_PAGER_OVER = "ui.pagerover";

	public static final String	UI_LOOKUP_DATE_TILL	= "ui.date.till";
	
	/** Error component panel header text: Fouten */
	public static final String	UI_ERROR_HEADER	= "ui.error.header";

	/* Exceptions. */
	
	/** The required page parameter {0} is missing. */
	public static final String	X_MISSING_PARAMETER	= "x.missing.parameter";

	/** The page {0} is in too many conversations, and a conversation was not specified. This usually indicates that a page was linked to incorrectly. */
	public static final String	X_TOO_MANY_CONVERSATIONS = "x.conver.toomany";

	
	/** Invalid date */
	public static final String	V_INVALID_DATE	= "v.invalid.date";

	/** (empty field) - the text shown when a lookup-popup-thingy is empty. */
	public static final String	UI_LOOKUP_EMPTY	= "ui.lookup.empty";

	/** Alle zoekvelden zijn leeg; vul minimaal een veld in om een zoekopdracht te doen. */
	public static final String	V_MISSING_SEARCH	= "v.missing.search";

	/** Your session with the server has expired. The page will be reloaded with original data from the server. */
	public static final String	S_EXPIRED	= "s.session.expired";
	
	/** Your browser is out-of-sync with the server, most probably because your session with the server has expired. The page will be reloaded with original data from the server.  */
	public static final String	S_BADNODE	= "s.session.badnode";

}
