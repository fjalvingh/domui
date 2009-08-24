package to.etc.domui.util;

import to.etc.webapp.nls.*;

/**
 * Constants for errors within the framework.
 * Please keep in same order as in resource bundle.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 11, 2008
 */
public interface Msgs {
	/**
	 * A reference to the global shared message bundle for DomUI messages.
	 */
	static public final BundleRef BUNDLE = BundleRef.create(Msgs.class, "messages");

	public static final String UNEXPECTED_EXCEPTION = "ui.unexpected_exception";

	public static final String MANDATORY = "ui.mandatory";

	public static final String NOT_VALID = "ui.invalid";

	public static final String UI_BOOL_TRUE = "ui.boolean.true";

	public static final String UI_BOOL_FALSE = "ui.boolean.false";

	//# Control texts
	/** Page x of y */
	public static final String UI_PAGER_TEXT = "ui.pagertext";

	/** The result is truncated to {0} rows */
	public static final String UI_PAGER_OVER = "ui.pagerover";

	/** No results */
	public static final String UI_PAGER_EMPTY = "ui.pagerempty";

	/** No results (data table) */
	public static final String UI_DATATABLE_EMPTY = "ui.dt.empty";

	public static final String UI_LOOKUP_DATE_TILL = "ui.date.till";

	/** Error component panel header text: Fouten */
	public static final String UI_ERROR_HEADER = "ui.error.header";

	/** (empty field) - the text shown when a lookup-popup-thingy is empty. */
	public static final String UI_LOOKUP_EMPTY = "ui.lookup.empty";

	public static final String UI_LUI_TTL = "ui.lui.ttl";

	//# Control: message box
	public static final String UI_MBX_WARNING = "ui.mbx.warning";

	public static final String UI_MBX_ERROR = "ui.mbx.error";

	public static final String UI_MBX_INFO = "ui.mbx.info";

	public static final String UI_MBX_DIALOG = "ui.mbx.dialog";

	//# Exceptions. 

	/** The required page parameter {0} is missing. */
	public static final String X_MISSING_PARAMETER = "x.missing.parameter";

	/** The page {0} is in too many conversations, and a conversation was not specified. This usually indicates that a page was linked to incorrectly. */
	public static final String X_TOO_MANY_CONVERSATIONS = "x.conver.toomany";

	//# Validators

	/** Invalid date */
	public static final String V_INVALID_DATE = "v.invalid.date";

	/** Alle zoekvelden zijn leeg; vul minimaal een veld in om een zoekopdracht te doen. */
	public static final String V_MISSING_SEARCH = "v.missing.search";

	/** Invalid monetary amount */
	public static final String V_BAD_AMOUNT = "v.bad.amount";

	/** Invalid percentage value */
	public static final String V_BAD_PERCENTAGE = "v.bad.percentage";

	/** Invalid double */
	public static final String V_INVALID_DOUBLE = "v.invalid.double";

	//# Server errors.
	/** Your session with the server has expired. The page will be reloaded with original data from the server. */
	public static final String S_EXPIRED = "s.session.expired";

	/** Your browser is out-of-sync with the server, most probably because your session with the server has expired. The page will be reloaded with original data from the server.  */
	public static final String S_BADNODE = "s.session.badnode";


	//# Login and access denied code
	public static final String LOGIN_ACCESS_TITLE = "login.access.title";

	public static final String LOGIN_ACCESS_DENIED = "login.access.denied";

	public static final String LOGIN_REQUIRED_RIGHTS = "login.required.rights";

	public static final String LOGIN_TO_INDEX = " login.toindex";

	//# LookupForm 
	/** lookupform.search key */
	public static final String LOOKUP_FORM_SEARCH = "lookupform.search";

	/** lookupform.clear key */
	public static final String LOOKUP_FORM_CLEAR = "lookupform.clear";

	/** lookupform.new key */
	public static final String LOOKUP_FORM_NEW = "lookupform.new";

	/** lookupform.collapse key */
	public static final String LOOKUP_FORM_COLLAPSE = "lookupform.collapse";

	/** lookupform.restore key */
	public static final String LOOKUP_FORM_RESTORE = "lookupform.restore";

}
