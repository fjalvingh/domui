package to.etc.syntaxer;


import java.awt.Font;
import java.awt.Color;

/**
 * A simple text style class. It can specify the color, italic flag,
 * and bold flag of a run of text.
 * @author Slava Pestov
 * @version $Id: SyntaxStyle.java,v 1.5 2003/03/14 02:51:25 spestov Exp $
 */
public class SyntaxStyle
{
	//{{{ SyntaxStyle constructor
	/**
	 * Creates a new SyntaxStyle.
	 * @param fgColor The text color
	 * @param bgColor The background color
	 * @param font The text font
	 */
	public SyntaxStyle(Color fgColor, Color bgColor, Font font)
	{
		this.fgColor = fgColor;
		this.bgColor = bgColor;
		this.font = font;
	} //}}}

	//{{{ getForegroundColor() method
	/**
	 * Returns the text color.
	 */
	public Color getForegroundColor()
	{
		return fgColor;
	} //}}}

	//{{{ getBackgroundColor() method
	/**
	 * Returns the background color.
	 */
	public Color getBackgroundColor()
	{
		return bgColor;
	} //}}}

	//{{{ getFont() method
	/**
	 * Returns the style font.
	 */
	public Font getFont()
	{
		return font;
	} //}}}

	//{{{ getCharWidth() method
	/**
	 * Returns the character width of the monospaced font.
	 * @since jEdit 4.2pre1
	 */
	public int getCharWidth()
	{
		return charWidth;
	} //}}}

	//{{{ setCharWidth() method
	/**
	 * Sets the character width of the monospaced font.
	 * @param charWidth The character width
	 * @since jEdit 4.2pre1
	 */
	public void setCharWidth(int charWidth)
	{
		this.charWidth = charWidth;
	} //}}}

	//{{{ Private members
	private Color fgColor;

	private Color bgColor;

	private Font font;

	private int charWidth;
	//}}}
}
