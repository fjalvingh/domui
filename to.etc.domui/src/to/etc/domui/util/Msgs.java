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

	public static final String ONE_OF_IS_MANDATORY = "ui.one.of.mandatory";

	public static final String NOT_VALID = "ui.invalid";

	public static final String UI_BOOL_TRUE = "ui.boolean.true";

	public static final String UI_BOOL_FALSE = "ui.boolean.false";

	public static final String UI_DATA_INCONSISTENT = "ui.data.inconsistent";

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

	public static final String UI_KEYWORD_SEARCH_COUNT = "ui.keyword.search.count";

	public static final String UI_KEYWORD_SEARCH_NO_MATCH = "ui.keyword.search.no.match";

	public static final String UI_KEYWORD_SEARCH_LARGE_MATCH = "ui.keyword.search.large.match";

	public static final String UI_KEYWORD_SEARCH_HINT = "ui.keyword.search.hint";

	/** Lookup value invalid */
	public static final String UI_LOOKUP_INVALID = "ui.lookup.invalid";

	public static final String UI_LOOKUP_BAD_OPERATOR_COMBI = "ui.lookup.op.combi";

	public static final String UI_LUI_TTL = "ui.lui.ttl";

	public static final String UI_MLUI_COL_TTL = "ui.mlui.col.ttl";

	//# Control: message box
	public static final String UI_MBX_WARNING = "ui.mbx.warning";

	public static final String UI_MBX_ERROR = "ui.mbx.error";

	public static final String UI_MBX_INFO = "ui.mbx.info";

	public static final String UI_MBX_DIALOG = "ui.mbx.dialog";

	//-- ExpandingEditTable
	public static final String UI_XDT_DELETE = "ui.xdt.delete";

	public static final String UI_XDT_DELSURE = "ui.xdt.delsure";

	public static final String UI_XDT_ADD = "ui.xdt.add";

	public static final String UI_XDT_CANCEL = "ui.xdt.cancel";

	public static final String UI_XDT_SURE = "ui.xdt.sure";

	//# Exceptions.

	/** The required page parameter {0} is missing. */
	public static final String X_MISSING_PARAMETER = "x.missing.parameter";

	/** Multiple values for parameter x not expected */
	public static final String X_MULTIPLE_PARAMETER = "x.multiple.parameter";

	/** The page parameter {0} is invalid . */
	public static final String X_INVALID_PARAMETER = "x.invalid.parameter";

	/**  . */
	public static final String X_INVALID_PARAMTYPE = "x.badtype.parameter";

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

	/** Invalid operator */
	public static final String V_INVALID_OPERATOR = "v.invalid.operator";

	/** Invalid not unique */
	public static final String V_INVALID_NOT_UNIQUE = "v.invalid.not.unique";

	/** Value out of range */
	public static final String V_OUT_OF_RANGE = "v.out.of.range";

	/** Invalid value */
	public static final String V_INVALID = "v.invalid";

	/** The input is not in the format {0} */
	public static final String V_NO_RE_MATCH = "v.no.re.match";

	public static final String V_TOOSMALL = "v.toosmall";

	public static final String V_TOOLARGE = "v.toolarge";

	//# Server errors.
	/** Your session with the server has expired. The page will be reloaded with original data from the server. */
	public static final String S_EXPIRED = "s.session.expired";

	/** Your browser is out-of-sync with the server, most probably because your session with the server has expired. The page will be reloaded with original data from the server.  */
	public static final String S_BADNODE = "s.session.badnode";

	/** The page has been reset and loaded again; the error message is passed on. */
	public static final String S_PAGE_CLEARED = "s.page.cleared";

	/*---------- Metamodel ----------*/
	public static final String MM_COMPOUND_PROPERTY_NOT_FOUND = "mm.compound.prop";

	public static final String MM_UNKNOWN_COLLECTION_TYPE = "mm.unknown.collection.type";

	public static final String MM_BAD_REGEXP = "mm.bad.re";

	//# Login and access denied code
	public static final String LOGIN_ACCESS_TITLE = "login.access.title";

	public static final String LOGIN_ACCESS_DENIED = "login.access.denied";

	public static final String LOGIN_REQUIRED_RIGHTS = "login.required.rights";

	public static final String LOGIN_TO_INDEX = " login.toindex";

	//# Data not found code
	public static final String DATA_EXPIRED_TITLE = "expired.data.title";

	//# LookupForm
	/** lookupform.search key */
	public static final String LOOKUP_FORM_SEARCH = "lookupform.search";

	/** lookupform.clear key */
	public static final String LOOKUP_FORM_CLEAR = "lookupform.clear";

	/** lookupform.cancel key */
	public static final String LOOKUP_FORM_CANCEL = "lookupform.cancel";

	/** lookupform.confirm key */
	public static final String LOOKUP_FORM_CONFIRM = "lookupform.confirm";

	/** lookupform.new key */
	public static final String LOOKUP_FORM_NEW = "lookupform.new";

	/** lookupform.collapse key */
	public static final String LOOKUP_FORM_COLLAPSE = "lookupform.collapse";

	/** lookupform.restore key */
	public static final String LOOKUP_FORM_RESTORE = "lookupform.restore";

	public static final String VERBATIM = "verbatim";

	//# AsyncContainer
	/** asynccontainer.cancel key */
	public static final String ASYNC_CONTAINER_CANCEL = "asynccontainer.cancel";

	/** asynccontainer.complete.indicator key */
	public static final String ASYNC_CONTAINER_COMPLETE_INDICATOR = "asynccontainer.complete.indicator";

	/** asynccontainer.cancelled key */
	public static final String ASYNC_CONTAINER_CANCELLED = "asynccontainer.cancelled";

	/** asynccontainer.cancelled.msg key */
	public static final String ASYNC_CONTAINER_CANCELLED_MSG = "asynccontainer.cancelled.msg";

	/** asynccontainer.no.results.msg key */
	public static final String ASYNC_CONTAINER_NO_RESULTS_MSG = "asynccontainer.no.results.msg";

}
