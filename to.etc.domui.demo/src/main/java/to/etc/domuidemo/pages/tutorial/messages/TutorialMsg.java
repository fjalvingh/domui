package to.etc.domuidemo.pages.tutorial.messages;

import to.etc.webapp.nls.IBundleCode;

/**
 * Tutorial, "telling something to a user": the message codes used by the pages of
 * this chapter. Every constant is a key in TutorialMsg.properties, next to this
 * enum, so a message is named by something the compiler checks and translated
 * without the code that posts it knowing a language.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public enum TutorialMsg implements IBundleCode {
	/** The order was accepted: {0} = the number of copies. */
	orderSaved,

	/** Too many copies ordered: {0} = the maximum that does not need approval. */
	orderTooLarge,

	/** The shop is running out: {0} = album title, {1} = copies left. */
	orderStockLow,

	/** Nothing was ordered at all. */
	orderEmpty,

	/** Ordering failed because the album is sold out: {0} = album title. */
	orderOutOfStock
}
