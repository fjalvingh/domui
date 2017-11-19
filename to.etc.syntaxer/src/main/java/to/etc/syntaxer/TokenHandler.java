package to.etc.syntaxer;

import javax.swing.text.Segment;

/**
 * Token markers send tokens to implementations of this interface.
 *
 * @author Slava Pestov
 * @version $Id: TokenHandler.java,v 1.6 2003/10/26 19:43:58 spestov Exp $
 * @since jEdit 4.1pre1
 */
public interface TokenHandler
{
	/**
	 * Called by the token marker when a syntax token has been parsed.
	 * @param seg The segment containing the text
	 * @param id The token type (one of the constants in the
	 * {@link Token} class).
	 * @param offset The start offset of the token
	 * @param length The number of characters in the token
	 * @param context The line context
	 * @since jEdit 4.2pre3
	 */
	void handleToken(Segment seg, byte id, int offset, int length, TokenMarker.LineContext context);

	/**
	 * The token handler can compare this object with the object
	 * previously given for this line to see if the token type at the end
	 * of the line has changed (meaning subsequent lines might need to be
	 * retokenized).
	 * @since jEdit 4.2pre6
	 */
	void setLineContext(TokenMarker.LineContext lineContext);
}
