package to.etc.domui.util;

import to.etc.webapp.nls.BundleRef;
import to.etc.webapp.nls.IBundleCode;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 16-12-18.
 */
public enum Msgs implements IBundleCode {
	unexpectedException,
	mandatory,
	oneIsMandatory,
	notValid,
	uiBoolTrue,
	uiBoolFalse,
	uiInconsistentData,
	uiConcurrentUpdate,
	/** The database query timed out and was cancelled. */
	uiQueryTimeout,
	uiValidationFailed,
	uiValidationInterrupted,

	/** Page x of y */
	uiPagerText,
	/** The result is truncated to {0} rows */
	uiPagerOverflow,
	/** No results */
	uiPagerEmpty,
	/** No results (data table) */
	uiDatatableEmpty,
	uiLookupDateTill,
	uiErrorHeader,

	/** (empty field) - the text shown when a lookup-popup-thingy is empty. */
	uiLookupEmpty, // = "ui.lookup.empty";

	/** Lookup value invalid */
	uiLookupInvalid, // = "ui.lookup.invalid";

	uiLookupOpCombi, // = "ui.lookup.op.combi";


	/** CheckBoxButton **/
	uiChkbbOn,
	uiChkbbOff,

	pageWithoutUrlContext,
	noUrlContextValueFor,
	dataValueAccessDenied,
	uiPagerPrev,
	uiPagerNext,
	uiPagerRecordCount,
	uiPagerOverflow2,
	uiWarningHeader,

	btnDelete,
	btnDeleteYesNo,
	btnAdd,
	btnEdit,
	btnSave,
	btnCancel,

	vInvalidRegex,
	vInvalidEmail,

/** Leave page dialog */
	leavePageQuestion,
	changesYouMadeMayNotBeSaved,
	leave,

/** Confirmation in blood */
	incorrectInputCantDeleteData,
	reason,

	//-- Validation
	/** Invalid value */
	vInvalid,
	vInvalidNoParam,

	/** Invalid date */
	vInvalidDate, // = "v.invalid.date";

	/** Alle zoekvelden zijn leeg; vul minimaal een veld in om een zoekopdracht te doen. */
	vMissingSearch, // = "v.missing.search";

	vBadDuration, // = "v.bad.duration";

	/** Invalid monetary amount */
	vBadAmount, // = "v.bad.amount";

	/** Monetary sign used in non-monetary value */
	vNotMonetary,

	/** Confused input with dots and commas */
	vBadDots,

	vBadThousandsSeparator,

	vBadCharInNumber,

	vExpectStrictScale,

	vFractionWithoutLeadingDigit,

	vTooManyFractionDigits,

	/** Invalid percentage value */
	vBadPercentage, // = "v.bad.percentage";

	/** Invalid double */
	vInvalidDouble, // = "v.invalid.double";

	/** Invalid operator */
	vInvalidOperator, // = "v.invalid.operator";

	/** Invalid not unique */
	vInvalidNotUnique, // = "v.invalid.not.unique";

	/** Value out of range */
	vOutOfRange, // = "v.out.of.range";

	/** The input is not in the format {0} */
	vNoReMatch, // = "v.no.re.match";

	vTooSmall, // = "v.toosmall";

	vTooLarge, // = "v.toolarge";

	vLogicError, // = "v.logic.error";

	//# Server errors.
	/** Your session with the server has expired. The page will be reloaded with original data from the server. */
	sSessionExpired, // = "s.session.expired";

	/** Your browser is out-of-sync with the server, most probably because your session with the server has expired. The page will be reloaded with original data from the server.  */
	sSessionBadnode, // = "s.session.badnode";

	/** The page has been reset and loaded again; the error message is passed on. */
	sPageCleared, // = "s.page.cleared";

	sConversationDestroyed, // = "conversation.destroyed";


	eBindingFailed, // = "e.binding.failed";

	eBindingDefinition, // = "e.binding.definition";

	//-- Crud
	verbatim, // = "verbatim";


	//# Exceptions.

	/** The required page parameter {0} is missing. */
	xMissingParameter, // = "x.missing.parameter";

	/** Multiple values for parameter x not expected */
	xMultipleParameter, // = "x.multiple.parameter";

	/** The page parameter {0} is invalid . */
	xInvalidParameter, // = "x.invalid.parameter";

	/**  . */
	xInvalidParameterType, // = "x.badtype.parameter";

	/** The page {0} is in too many conversations, and a conversation was not specified. This usually indicates that a page was linked to incorrectly. */
	xTooManyConversations, // = "x.conver.toomany";

	rightsNotAllowed, // = "rights.disallowed";

	uploadTooLarge, // = "upload.too.large";

	uploadDataError, // = "upload.data.error";

	uploadInterrupted, // = "upload.interrupted";

	/*---------- Metamodel ----------*/
	mmCompoundPropertyNotFound, // = "mm.compound.prop";

	mmUnknownCollectionType, // = "mm.unknown.collection.type";

	mmBadRegexp, // = "mm.bad.re";

	//-- Controls
	ctlImageInvalid,

	//-- SQL Errors
	sqlErrNotUnique,
	;

	/** Error component panel header text: Info */
	public static String UI_INFO_HEADER = "ui.info.header";

	public static String UI_KEYWORD_SEARCH_COUNT = "ui.keyword.search.count";

	public static String UI_KEYWORD_SEARCH_NO_MATCH = "ui.keyword.search.no.match";

	public static String UI_KEYWORD_SEARCH_LARGE_MATCH = "ui.keyword.search.large.match";

	public static String UI_KEYWORD_SEARCH_HINT = "ui.keyword.search.hint";

	public static String UI_LUI_TTL = "ui.lui.ttl";

	public static String UI_LUI_TTL_MULTI = "ui.lui.ttl.multi";

	/** Lookup input title if entityName is available. */
	public static String UI_LUI_TTL_WEN = "ui.lui.ttl.wen";

	public static String UI_MLUI_COL_TTL = "ui.mlui.col.ttl";

	//# Control: message box
	public static String UI_MBX_WARNING = "ui.mbx.warning";

	public static String UI_MBX_ERROR = "ui.mbx.error";

	public static String UI_MBX_INFO = "ui.mbx.info";

	public static String UI_MBX_DIALOG = "ui.mbx.dialog";

	public static String UI_MBX_INPUT = "ui.mbx.input";

	//-- ExpandingEditTable
	public static String UI_XDT_DELETE = "ui.xdt.delete";

	public static String UI_XDT_DELSURE = "ui.xdt.delsure";

	public static String UI_XDT_ADD = "ui.xdt.add";

	public static String UI_XDT_CONFIRM = "ui.xdt.confirm";

	public static String UI_XDT_CANCEL = "ui.xdt.cancel";

	public static String UI_XDT_SURE = "ui.xdt.sure";


	//# Login and access denied code
	public static String LOGIN_ACCESS_TITLE = "login.access.title";

	public static String LOGIN_ACCESS_DENIED = "login.access.denied";

	public static String LOGIN_REQUIRED_RIGHTS = "login.required.rights";

	public static String LOGIN_REFUSAL_REASON = "login.refusal.reason";

	public static String LOGIN_TO_INDEX = "login.toindex";

	//# Data not found code
	public static String DATA_EXPIRED_TITLE = "expired.data.title";

	public static String DATA_EXPIRED_REFRESH = "expired.data.refresh";

	// AsyncDialogTask
	public static String ASYD_COMPLETED = "async.dialog.completed";

	//# Access denied
	public static String DATA_ACCESS_VIOLATION_TITLE = "data.access.violation.title";

	public static String ACCESS_DENIED_MSG = "access.denied.msg";

	public static String INSUFFICIENT_RIGHTS = "insufficient.rights";

	//# Session expired
	public static String SESSION_EXPIRED_TITLE = "session.expired.title";

	public static String SESSION_EXPIRED_MESSAGE = "session.expired.msg";

	//# SaveSearchFilterDialog
	/** savesearchfilter.name.exists key */
	public static String SAVE_SEARCH_DIALOG_NAME_EXISTS = "savesearchfilter.name.exists";

	/** savesearchfilter.name.too.long key */
	public static String SAVE_SEARCH_DIALOG_NAME_TOO_LONG = "savesearchfilter.name.too.long";

	/** savesearchfilter.name.saved */
	public static String SAVE_SEARCH_DIALOG_NAME_SAVED = "savesearchfilter.name.saved";

	//# LookupForm
	/** lookupform.search key */
	public static String LOOKUP_FORM_SEARCH = "lookupform.search";

	/** lookupform.clear key */
	public static String LOOKUP_FORM_CLEAR = "lookupform.clear";

	/** lookupform.cancel key */
	public static String LOOKUP_FORM_CANCEL = "lookupform.cancel";

	/** lookupform.savesearch key */
	public static String LOOKUP_FORM_SAVE_SEARCH = "lookupform.savesearch";

	/** lookupform.save.filters label */
	public static String LOOKUP_FORM_SAVED_FILTERS = "lookupform.saved.filters.label";

	public static String LOOKUP_FORM_DELETE_FILTER = "lookupform.delete.filter";

	/** lookupform.confirm key */
	public static String LOOKUP_FORM_CONFIRM = "lookupform.confirm";

	/** lookupform.new key */
	public static String LOOKUP_FORM_NEW = "lookupform.new";

	/** lookupform.collapse key */
	public static String LOOKUP_FORM_COLLAPSE = "lookupform.collapse";

	/** lookupform.restore key */
	public static String LOOKUP_FORM_RESTORE = "lookupform.restore";

	/** lookupform.collapse title */
	public static String LOOKUP_FORM_COLLAPSE_TITLE = "lookupform.btn.collapse.title";

	/** lookupform.clear title */
	public static String LOOKUP_FORM_CLEAR_TITLE = "lookupform.btn.clear.title";

	/** lookupform.new title */
	public static String LOOKUP_FORM_NEW_TITLE = "lookupform.btn.new.title";

	/** lookupform.search title */
	public static String LOOKUP_FORM_SEARCH_TITLE = "lookupform.btn.search.title";

	/** lookupform.cancel title */
	public static String LOOKUP_FORM_CANCEL_TITLE = "lookupform.btn.cancel.title";


	//# AsyncContainer
	/** asynccontainer.cancel key */
	public static String ASYNC_CONTAINER_CANCEL = "asynccontainer.cancel";

	/** asynccontainer.complete.indicator key */
	public static String ASYNC_CONTAINER_COMPLETE_INDICATOR = "asynccontainer.complete.indicator";

	/** asynccontainer.cancelled key */
	public static String ASYNC_CONTAINER_CANCELLED = "asynccontainer.cancelled";

	/** asynccontainer.cancelled.msg key */
	public static String ASYNC_CONTAINER_CANCELLED_MSG = "asynccontainer.cancelled.msg";

	/** asynccontainer.no.results.msg key */
	public static String ASYNC_CONTAINER_NO_RESULTS_MSG = "asynccontainer.no.results.msg";

	public static String ASYNC_ERROR = "asynccontainer.error";

	/** EditDialog */
	public static String EDLG_OKAY = "edlg.okay";

	public static String EDLG_CANCEL = "edlg.cancel";

	/** General purpose */
	public static String BTN_CLOSE = "btn.close";

	public static String BTN_PRINT = "btn.print";

	/*** BulkUpload ***/
	public static String BULKUPLD_DISABLED = "bupl.disabled";

	public static String BULKUPLD_SELECT_FILES = "bupl.select.files";

	public static String BULKUPLD_UPLOAD_QUEUE = "bupl.upload.queue";

	/*** ImageSelectControl ***/
	public static String ISCT_EMPTY_TITLE = "isct.empty.title";

	public static String UI_UPLOAD_TEXT = "ui.upload.button";

	public static String UI_UPLOADMULTI_TEXT = "ui.uploadmulti.button";

	static public final String EXPORT_BUTTON = "ui.btn.export";

	@Override public BundleRef getBundle() {
		return BUNDLE;
	}

	/**
	 * A reference to the global shared message bundle for DomUI messages.
	 */
	public static BundleRef BUNDLE = BundleRef.create(Msgs.class, "messages");

}
