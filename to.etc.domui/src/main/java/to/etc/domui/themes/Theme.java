package to.etc.domui.themes;

import org.eclipse.jdt.annotation.NonNullByDefault;
import to.etc.domui.component.misc.IIconRef;
import to.etc.domui.component.misc.Icon;
import to.etc.domui.component.misc.WrappedIconRef;
import to.etc.domui.dom.html.NodeBase;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Icons and images that are common to components in the framework's components.
 *
 * Please keep in alphabetical order.
 *
 * FIXME: for btadic -> fill in all resources (search for '"THEME' in code, and replace with constant that is placed in here).
 * We would schedule that once when we agree upon this with jal in order to minimize conflicts. Currently there are 222 hits for searching on '"THEME/'
 *
 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
 * Created on 9 Nov 2011
 */
@NonNullByDefault
public enum Theme implements IIconRef {
	ICON_ADD_TO_SELECTION
	, ICON_BIG_INFO
	, BTN_SHOW_CALENDAR
	, BTN_CLOCK
	, BTN_CONFIRM
	, BTN_DELETE
	, BTN_EDIT
	, BTN_FIND
	, BTN_HIDE_DETAILS
	, BTN_HIDE_LOOKUP
	, BTN_NEW
	, ICON_RED_CROSS
	, BTN_SAVE
	, BTN_SHOW_DETAILS
	, BTN_SHOW_LOOKUP
	, BTN_SPECIAL_CHARS
	, BTN_TODAY
	, ICON_CLOSE
	, ICON_BIG_WARNING
	, ICON_BIG_QUESTION
	, ICON_MINI_ERROR
	, ICON_MINI_WARNING
	, ICON_MINI_INFO
	, ICON_PROGRESSBAR
	, ICON_RESIZE
	, ACCESS_DENIED
	, BIG_ACCESS_DENIED
	, BTN_CANCEL
	, BTN_CHECKMARK
	, ICON_CHECKMARK
	, BTN_CLEARLOOKUP
	, BTN_HOVERCLEARLOOKUP
	, BTN_CLEAR
	, BTN_POPUPLOOKUP
	, BTN_LOOKUP
	, BTN_HOVERPOPUPLOOKUP
	, BTN_PLUS
	, DATA_EXPIRED
	, ICON_WARNING
	, ICON_ERROR
	, ICON_MBX_ERROR
	, ICON_BIG_ERROR
	, ICON_MBX_INFO
	, ICON_MBX_WARNING
	, ICON_MBX_DIALOG
	, BTN_CLOSE
	, ICON_PAW
	, ICON_SECURED
	, ICON_SUCCESS
	, ICON_DSPCB_ON
	, ISCT_EMPTY
	, ISCT_ERASE
	;

	static private Map<Theme, IIconRef> m_iconMap = new ConcurrentHashMap<>();

	public IIconRef getRef() {
		IIconRef ref = m_iconMap.get(this);
		if(ref == null)
			throw new IllegalStateException("No icon defined for " + this);
		return ref;
	}

	@Override public NodeBase createNode(String cssClasses) {
		return getRef().createNode(cssClasses);
	}

	@Override public String getClasses() {
		return getRef().getClasses();
	}

	@Override public IIconRef css(String... classes) {
		return new WrappedIconRef(this, classes);
	}

	/**
	 * Set an (alternative) icon for a theme based icon.
	 */
	static public void update(Theme key, IIconRef icon) {
		m_iconMap.put(key, icon);
	}

	static {
		update(ICON_ADD_TO_SELECTION, Icon.of("THEME/addToSelection.png"));
		update(BTN_CANCEL, Icon.of("THEME/btnCancel.png"));
		update(BTN_CHECKMARK, Icon.of("THEME/btnCheckmark.png"));
		update(ICON_CHECKMARK, BTN_CHECKMARK);
		update(BTN_CLEARLOOKUP, Icon.of("THEME/btnClearLookup.png"));
		//update(BTN_CLEAR_LOOKUP, BTN_CLEARLOOKUP);
		update(BTN_HOVERCLEARLOOKUP, Icon.of("THEME/btn-hover-ClearLookup.png"));
		update(BTN_CLEAR, Icon.of("THEME/btnClear.png"));
		update(BTN_POPUPLOOKUP, Icon.of("THEME/btn-popuplookup.png"));
		update(BTN_LOOKUP, BTN_POPUPLOOKUP);
		update(BTN_HOVERPOPUPLOOKUP, Icon.of("THEME/btn-hover-popuplookup.png"));
		update(BTN_PLUS, Icon.of("THEME/btnPlus.png"));
		update(DATA_EXPIRED, Icon.of("THEME/dataExpired.png"));
		update(ICON_WARNING, Icon.of("THEME/warning.png"));
		update(ICON_ERROR, Icon.of("THEME/error.png"));
		update(ICON_MBX_ERROR, Icon.of("THEME/mbx-error.png"));
		update(ICON_BIG_ERROR, ICON_MBX_ERROR);
		update(ICON_MBX_WARNING, Icon.of("THEME/mbx-warning.png"));
		update(ICON_MBX_INFO, Icon.of("THEME/mbx-info.png"));
		update(ICON_MBX_DIALOG, Icon.of("THEME/mbx-question.png"));
		update(BTN_CLOSE, Icon.of("THEME/btnClose.png"));
		update(ICON_PAW, Icon.of("THEME/paw.png"));
		update(ICON_SECURED, Icon.of("THEME/secured.png"));
		update(ICON_SUCCESS, Icon.of("THEME/success.png"));
		update(ICON_DSPCB_ON, Icon.of("THEME/dspcb-on.png"));
		update(ISCT_EMPTY, Icon.of("THEME/isct_empty.png"));
		update(ISCT_ERASE, Icon.of("THEME/48x16_isct_erase.png"));
		update(ACCESS_DENIED, Icon.of("THEME/accessDenied.png"));
		update(BIG_ACCESS_DENIED, Icon.of("THEME/big-accessDenied.png"));
		update(ICON_BIG_INFO, Icon.of("THEME/big-info.png"));
		update(BTN_SHOW_CALENDAR, Icon.of("THEME/btn-datein.png"));
		update(BTN_CLOCK, Icon.of("THEME/btnClock.png"));
		update(BTN_CONFIRM, Icon.of("THEME/btnConfirm.png"));
		update(BTN_DELETE, Icon.of("THEME/btnDelete.png"));
		update(BTN_EDIT, Icon.of("THEME/btnEdit.png"));
		update(BTN_FIND, Icon.of("THEME/btnFind.png"));
		update(BTN_HIDE_DETAILS, Icon.of("THEME/btnHideDetails.png"));
		update(BTN_HIDE_LOOKUP, Icon.of("THEME/btnHideLookup.png"));
		update(BTN_NEW, Icon.of("THEME/btnNew.png"));
		update(ICON_RED_CROSS, Icon.of("THEME/btnRedCross.png"));
		update(BTN_SAVE, Icon.of("THEME/btnSave.png"));
		update(BTN_SHOW_DETAILS, Icon.of("THEME/btnShowDetails.png"));
		update(BTN_SHOW_LOOKUP, Icon.of("THEME/btnShowLookup.png"));
		update(BTN_SPECIAL_CHARS, Icon.of("THEME/btnSpecialChar.png"));
		update(BTN_TODAY, Icon.of("THEME/btnToday.png"));
		update(ICON_CLOSE, Icon.of("THEME/close.png"));
		update(ICON_BIG_WARNING, Icon.of("THEME/mbx-warning.png"));
		update(ICON_BIG_QUESTION, Icon.of("THEME/mbx-question.png"));
		update(ICON_MINI_ERROR, Icon.of("THEME/mini-error.png"));
		update(ICON_MINI_WARNING, Icon.of("THEME/mini-warning.png"));
		update(ICON_MINI_INFO, Icon.of("THEME/mini-info.png"));
		update(ICON_PROGRESSBAR, Icon.of("THEME/progressbar.gif"));
		update(ICON_RESIZE, Icon.of("THEME/resize.png"));
	}
}
