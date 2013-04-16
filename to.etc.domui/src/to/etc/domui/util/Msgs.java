/*
 * DomUI Java User Interface library
 * Copyright (c) 2010 by Frits Jalvingh, Itris B.V.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * See the "sponsors" file for a list of supporters.
 *
 * The latest version of DomUI and related code, support and documentation
 * can be found at http://www.domui.org/
 * The contact for the project is Frits Jalvingh <jal@etc.to>.
 */
package to.etc.domui.util;

import to.etc.domui.themes.*;
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

	/** The database query timed out and was cancelled. */
	public static final String UI_QUERY_TIMEOUT = "ui.query.timeout";

	public static final String UI_VALIDATION_FAILED = "ui.validation.failed";

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

	/** Error component panel header text: Warnings */
	public static final String UI_WARNING_HEADER = "ui.warning.header";

	/** Error component panel header text: Info */
	public static final String UI_INFO_HEADER = "ui.info.header";

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

	public static final String UI_LUI_TTL_MULTI = "ui.lui.ttl.multi";

	/** Lookup input title if entityName is available. */
	public static final String UI_LUI_TTL_WEN = "ui.lui.ttl.wen";

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

	public static final String LOGIN_REFUSAL_REASON = "login.refusal.reason";

	public static final String LOGIN_TO_INDEX = "login.toindex";

	//# Data not found code
	public static final String DATA_EXPIRED_TITLE = "expired.data.title";

	public static final String DATA_EXPIRED_REFRESH = "expired.data.refresh";

	//# Access denied
	public static final String DATA_ACCESS_VIOLATION_TITLE = "data.access.violation.title";

	public static final String ACCESS_DENIED_MSG = "access.denied.msg";

	public static final String INSUFFICIENT_RIGHTS = "insufficient.rights";

	//# Session expired
	public static final String SESSION_EXPIRED_TITLE = "session.expired.title";

	public static final String SESSION_EXPIRED_MESSAGE = "session.expired.msg";

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

	/** EditDialog */
	public static final String EDLG_OKAY = "edlg.okay";

	public static final String EDLG_CANCEL = "edlg.cancel";

	/** General purpose */
	public static final String BTN_CLOSE = "btn.close";

	/*** BulkUpload ***/
	public static final String BULKUPLD_DISABLED = "bupl.disabled";


	/*--------------------------------------------------------------*/
	/*	CODING:	Available icons within the framework theme set.		*/
	/*--------------------------------------------------------------*/
	static public final String ICON_ADD_TO_SELECTION = "THEME/addToSelection.png";

	static public final String ICON_BIG_INFO = "THEME/big-info.png";

	static public final String BTN_SHOW_CALENDAR = "THEME/btn-datein.png";

	static public final String BTN_LOOKUP = Theme.BTN_POPUPLOOKUP;

	static public final String BTN_CANCEL = Theme.BTN_CANCEL;

	static public final String ICON_CHECKMARK = Theme.BTN_CHECKMARK;

	static public final String BTN_CLEAR = "THEME/btnClear.png";

	static public final String BTN_CLEAR_LOOKUP = Theme.BTN_CLEARLOOKUP;

	static public final String BTN_CLOCK = "THEME/btnClock.png";

	static public final String BTN_CONFIRM = "THEME/btnConfirm.png";

	static public final String BTN_DELETE = "THEME/btnDelete.png";

	static public final String BTN_EDIT = "THEME/btnEdit.png";

	static public final String BTN_FIND = "THEME/btnFind.png";

	static public final String BTN_HIDE_DETAILS = "THEME/btnHideDetails.png";

	static public final String BTN_HIDE_LOOKUP = "THEME/btnHideLookup.png";

	static public final String BTN_NEW = "THEME/btnNew.png";

	static public final String ICON_RED_CROSS = "THEME/btnRedCross.png";

	static public final String BTN_SAVE = "THEME/btnSave.png";

	static public final String BTN_SHOW_DETAILS = "THEME/btnShowDetails.png";

	static public final String BTN_SPECIAL_CHARS = "THEME/btnSpecialChar.png";

	static public final String BTN_TODAY = "THEME/btnToday.png";

	static public final String ICON_CLOSE = "THEME/close.png";

	static public final String ICON_BIG_WARNING = "THEME/mbx-warning.png";

	static public final String ICON_BIG_ERROR = Theme.ICON_MBX_ERROR;

	static public final String ICON_BIG_QUESTION = "THEME/mbx-question.png";

	static public final String ICON_MINI_ERROR = "THEME/mini-error.png";

	static public final String ICON_MINI_WARNING = "THEME/mini-warning.png";

	static public final String ICON_MINI_INFO = "THEME/mini-info.png";

	static public final String ICON_PROGRESSBAR = "THEME/progressbar.gif";

	static public final String ICON_RESIZE = "THEME/resize.png";
}
