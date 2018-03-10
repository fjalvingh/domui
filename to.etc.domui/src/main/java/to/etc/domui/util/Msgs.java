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
	BundleRef BUNDLE = BundleRef.create(Msgs.class, "messages");

	String UNEXPECTED_EXCEPTION = "ui.unexpected_exception";

	String MANDATORY = "ui.mandatory";

	String ONE_OF_IS_MANDATORY = "ui.one.of.mandatory";

	String NOT_VALID = "ui.invalid";

	String UI_BOOL_TRUE = "ui.boolean.true";

	String UI_BOOL_FALSE = "ui.boolean.false";

	String UI_DATA_INCONSISTENT = "ui.data.inconsistent";

	/** The database query timed out and was cancelled. */
	String UI_QUERY_TIMEOUT = "ui.query.timeout";

	String UI_VALIDATION_FAILED = "ui.validation.failed";

	String UI_VALIDATION_INTERRUPTED = "ui.validation.wait";

	//# Control texts
	/** Page x of y */
	String UI_PAGER_TEXT = "ui.pagertext";

	/** The result is truncated to {0} rows */
	String UI_PAGER_OVER = "ui.pagerover";

	/** No results */
	String UI_PAGER_EMPTY = "ui.pagerempty";

	/** No results (data table) */
	String UI_DATATABLE_EMPTY = "ui.dt.empty";

	String UI_LOOKUP_DATE_TILL = "ui.date.till";

	/** Error component panel header text: Fouten */
	String UI_ERROR_HEADER = "ui.error.header";

	/** Error component panel header text: Warnings */
	String UI_WARNING_HEADER = "ui.warning.header";

	/** Error component panel header text: Info */
	String UI_INFO_HEADER = "ui.info.header";

	/** (empty field) - the text shown when a lookup-popup-thingy is empty. */
	String UI_LOOKUP_EMPTY = "ui.lookup.empty";

	String UI_KEYWORD_SEARCH_COUNT = "ui.keyword.search.count";

	String UI_KEYWORD_SEARCH_NO_MATCH = "ui.keyword.search.no.match";

	String UI_KEYWORD_SEARCH_LARGE_MATCH = "ui.keyword.search.large.match";

	String UI_KEYWORD_SEARCH_HINT = "ui.keyword.search.hint";

	/** Lookup value invalid */
	String UI_LOOKUP_INVALID = "ui.lookup.invalid";

	String UI_LOOKUP_BAD_OPERATOR_COMBI = "ui.lookup.op.combi";

	String UI_LUI_TTL = "ui.lui.ttl";

	String UI_LUI_TTL_MULTI = "ui.lui.ttl.multi";

	/** Lookup input title if entityName is available. */
	String UI_LUI_TTL_WEN = "ui.lui.ttl.wen";

	String UI_MLUI_COL_TTL = "ui.mlui.col.ttl";

	//# Control: message box
	String UI_MBX_WARNING = "ui.mbx.warning";

	String UI_MBX_ERROR = "ui.mbx.error";

	String UI_MBX_INFO = "ui.mbx.info";

	String UI_MBX_DIALOG = "ui.mbx.dialog";

	String UI_MBX_INPUT = "ui.mbx.input";

	//-- ExpandingEditTable
	String UI_XDT_DELETE = "ui.xdt.delete";

	String UI_XDT_DELSURE = "ui.xdt.delsure";

	String UI_XDT_ADD = "ui.xdt.add";

	String UI_XDT_CONFIRM = "ui.xdt.confirm";

	String UI_XDT_CANCEL = "ui.xdt.cancel";

	String UI_XDT_SURE = "ui.xdt.sure";

	//# Exceptions.

	/** The required page parameter {0} is missing. */
	String X_MISSING_PARAMETER = "x.missing.parameter";

	/** Multiple values for parameter x not expected */
	String X_MULTIPLE_PARAMETER = "x.multiple.parameter";

	/** The page parameter {0} is invalid . */
	String X_INVALID_PARAMETER = "x.invalid.parameter";

	/**  . */
	String X_INVALID_PARAMTYPE = "x.badtype.parameter";

	/** The page {0} is in too many conversations, and a conversation was not specified. This usually indicates that a page was linked to incorrectly. */
	String X_TOO_MANY_CONVERSATIONS = "x.conver.toomany";

	//# Validators

	/** Invalid date */
	String V_INVALID_DATE = "v.invalid.date";

	/** Alle zoekvelden zijn leeg; vul minimaal een veld in om een zoekopdracht te doen. */
	String V_MISSING_SEARCH = "v.missing.search";

	String V_BAD_DURATION = "v.bad.duration";

	/** Invalid monetary amount */
	String V_BAD_AMOUNT = "v.bad.amount";

	/** Invalid percentage value */
	String V_BAD_PERCENTAGE = "v.bad.percentage";

	/** Invalid double */
	String V_INVALID_DOUBLE = "v.invalid.double";

	/** Invalid operator */
	String V_INVALID_OPERATOR = "v.invalid.operator";

	/** Invalid not unique */
	String V_INVALID_NOT_UNIQUE = "v.invalid.not.unique";

	/** Value out of range */
	String V_OUT_OF_RANGE = "v.out.of.range";

	/** Invalid value */
	String V_INVALID = "v.invalid";

	/** The input is not in the format {0} */
	String V_NO_RE_MATCH = "v.no.re.match";

	String V_TOOSMALL = "v.toosmall";

	String V_TOOLARGE = "v.toolarge";

	String V_LOGIC_ERROR = "v.logic.error";


	//# Server errors.
	/** Your session with the server has expired. The page will be reloaded with original data from the server. */
	String S_EXPIRED = "s.session.expired";

	/** Your browser is out-of-sync with the server, most probably because your session with the server has expired. The page will be reloaded with original data from the server.  */
	String S_BADNODE = "s.session.badnode";

	/** The page has been reset and loaded again; the error message is passed on. */
	String S_PAGE_CLEARED = "s.page.cleared";

	/*---------- Metamodel ----------*/
	String MM_COMPOUND_PROPERTY_NOT_FOUND = "mm.compound.prop";

	String MM_UNKNOWN_COLLECTION_TYPE = "mm.unknown.collection.type";

	String MM_BAD_REGEXP = "mm.bad.re";

	//# Login and access denied code
	String LOGIN_ACCESS_TITLE = "login.access.title";

	String LOGIN_ACCESS_DENIED = "login.access.denied";

	String LOGIN_REQUIRED_RIGHTS = "login.required.rights";

	String LOGIN_REFUSAL_REASON = "login.refusal.reason";

	String LOGIN_TO_INDEX = "login.toindex";

	String RIGHTS_NOT_ALLOWED = "rights.disallowed";

	//# Data not found code
	String DATA_EXPIRED_TITLE = "expired.data.title";

	String DATA_EXPIRED_REFRESH = "expired.data.refresh";

	// AsyncDialogTask
	String ASYD_COMPLETED = "async.dialog.completed";

	//# Access denied
	String DATA_ACCESS_VIOLATION_TITLE = "data.access.violation.title";

	String ACCESS_DENIED_MSG = "access.denied.msg";

	String INSUFFICIENT_RIGHTS = "insufficient.rights";

	//# Session expired
	String SESSION_EXPIRED_TITLE = "session.expired.title";

	String SESSION_EXPIRED_MESSAGE = "session.expired.msg";

	//# SaveSearchFilterDialog
	/** savesearchfilter.name.exists key */
	String SAVE_SEARCH_DIALOG_NAME_EXISTS = "savesearchfilter.name.exists";

	/** savesearchfilter.name.too.long key */
	String SAVE_SEARCH_DIALOG_NAME_TOO_LONG = "savesearchfilter.name.too.long";

	/** savesearchfilter.name.saved */
	String SAVE_SEARCH_DIALOG_NAME_SAVED = "savesearchfilter.name.saved";

	//# LookupForm
	/** lookupform.search key */
	String LOOKUP_FORM_SEARCH = "lookupform.search";

	/** lookupform.clear key */
	String LOOKUP_FORM_CLEAR = "lookupform.clear";

	/** lookupform.cancel key */
	String LOOKUP_FORM_CANCEL = "lookupform.cancel";

	/** lookupform.savesearch key */
	String LOOKUP_FORM_SAVE_SEARCH = "lookupform.savesearch";

	/** lookupform.save.filters label */
	String LOOKUP_FORM_SAVED_FILTERS = "lookupform.saved.filters.label";

	String LOOKUP_FORM_DELETE_FILTER = "lookupform.delete.filter";

	/** lookupform.confirm key */
	String LOOKUP_FORM_CONFIRM = "lookupform.confirm";

	/** lookupform.new key */
	String LOOKUP_FORM_NEW = "lookupform.new";

	/** lookupform.collapse key */
	String LOOKUP_FORM_COLLAPSE = "lookupform.collapse";

	/** lookupform.restore key */
	String LOOKUP_FORM_RESTORE = "lookupform.restore";

	/** lookupform.collapse title */
	String LOOKUP_FORM_COLLAPSE_TITLE = "lookupform.btn.collapse.title";

	/** lookupform.clear title */
	String LOOKUP_FORM_CLEAR_TITLE = "lookupform.btn.clear.title";

	/** lookupform.new title */
	String LOOKUP_FORM_NEW_TITLE = "lookupform.btn.new.title";

	/** lookupform.search title */
	String LOOKUP_FORM_SEARCH_TITLE = "lookupform.btn.search.title";

	/** lookupform.cancel title */
	String LOOKUP_FORM_CANCEL_TITLE = "lookupform.btn.cancel.title";

	String VERBATIM = "verbatim";

	//# AsyncContainer
	/** asynccontainer.cancel key */
	String ASYNC_CONTAINER_CANCEL = "asynccontainer.cancel";

	/** asynccontainer.complete.indicator key */
	String ASYNC_CONTAINER_COMPLETE_INDICATOR = "asynccontainer.complete.indicator";

	/** asynccontainer.cancelled key */
	String ASYNC_CONTAINER_CANCELLED = "asynccontainer.cancelled";

	/** asynccontainer.cancelled.msg key */
	String ASYNC_CONTAINER_CANCELLED_MSG = "asynccontainer.cancelled.msg";

	/** asynccontainer.no.results.msg key */
	String ASYNC_CONTAINER_NO_RESULTS_MSG = "asynccontainer.no.results.msg";

	String ASYNC_ERROR = "asynccontainer.error";

	/** EditDialog */
	String EDLG_OKAY = "edlg.okay";

	String EDLG_CANCEL = "edlg.cancel";

	/** General purpose */
	String BTN_CLOSE = "btn.close";

	String BTN_PRINT = "btn.print";

	/*** BulkUpload ***/
	String BULKUPLD_DISABLED = "bupl.disabled";

	String BULKUPLD_SELECT_FILES = "bupl.select.files";

	String BULKUPLD_UPLOAD_QUEUE = "bupl.upload.queue";

	/*** ImageSelectControl ***/
	String ISCT_EMPTY_TITLE = "isct.empty.title";

	/*--------------------------------------------------------------*/
	/*	CODING:	Available icons within the framework theme set.		*/
	/*--------------------------------------------------------------*/
	String ICON_ADD_TO_SELECTION = "THEME/addToSelection.png";

	String ICON_BIG_INFO = "THEME/big-info.png";

	String BTN_SHOW_CALENDAR = "THEME/btn-datein.png";

	String BTN_LOOKUP = Theme.BTN_POPUPLOOKUP;

	String BTN_CANCEL = Theme.BTN_CANCEL;

	String ICON_CHECKMARK = Theme.BTN_CHECKMARK;

	String BTN_CLEAR = Theme.BTN_CLEAR;

	String BTN_CLEAR_LOOKUP = Theme.BTN_CLEARLOOKUP;

	String BTN_CLOCK = "THEME/btnClock.png";

	String BTN_CONFIRM = "THEME/btnConfirm.png";

	String BTN_DELETE = "THEME/btnDelete.png";

	String BTN_EDIT = "THEME/btnEdit.png";

	String BTN_FIND = "THEME/btnFind.png";

	String BTN_HIDE_DETAILS = "THEME/btnHideDetails.png";

	String BTN_HIDE_LOOKUP = "THEME/btnHideLookup.png";

	String BTN_NEW = "THEME/btnNew.png";

	String ICON_RED_CROSS = "THEME/btnRedCross.png";

	String BTN_SAVE = "THEME/btnSave.png";

	String BTN_SHOW_DETAILS = "THEME/btnShowDetails.png";

	String BTN_SPECIAL_CHARS = "THEME/btnSpecialChar.png";

	String BTN_TODAY = "THEME/btnToday.png";

	String ICON_CLOSE = "THEME/close.png";

	String ICON_BIG_WARNING = "THEME/mbx-warning.png";

	String ICON_BIG_ERROR = Theme.ICON_MBX_ERROR;

	String ICON_BIG_QUESTION = "THEME/mbx-question.png";

	String ICON_MINI_ERROR = "THEME/mini-error.png";

	String ICON_MINI_WARNING = "THEME/mini-warning.png";

	String ICON_MINI_INFO = "THEME/mini-info.png";

	String ICON_PROGRESSBAR = "THEME/progressbar.gif";

	String ICON_RESIZE = "THEME/resize.png";

	String UPLOAD_TOO_LARGE = "upload.too.large";

	String UPLOAD_DATA_ERROR = "upload.data.error";

	String UPLOAD_INTERRUPTED = "upload.interrupted";

	String CONVERSATION_DESTROYED = "conversation.destroyed";

	String E_BINDING_FAILED = "e.binding.failed";

	String E_BINDING_DEFINITION = "e.binding.definition";

	String UI_UPLOAD_TEXT = "ui.upload.button";
	String UI_UPLOADMULTI_TEXT = "ui.uploadmulti.button";
	static public final String EXPORT_BUTTON = "ui.btn.export";


}
