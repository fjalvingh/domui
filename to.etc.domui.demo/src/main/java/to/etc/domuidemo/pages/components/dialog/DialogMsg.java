package to.etc.domuidemo.pages.components.dialog;

import to.etc.webapp.nls.IBundleCode;

/**
 * The message codes used by the pages of the "windows, dialogs and messages"
 * component group. Every constant is a key in DialogMsg.properties, next to this
 * enum.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public enum DialogMsg implements IBundleCode {
	/** The album was saved: {0} = the album title. */
	albumSaved,

	/** The shop is running out: {0} = album title, {1} = copies left. */
	albumStockLow,

	/** The album cannot be sold at all: {0} = album title. */
	albumSoldOut
}
