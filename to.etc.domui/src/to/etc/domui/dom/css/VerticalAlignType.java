package to.etc.domui.dom.css;

/**
 * Mapping for <I>vertical-align</I> CSS Style attribute.	 
 *
 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
 * Created on Apr 7, 2010
 */
public enum VerticalAlignType {
	/** Align the baseline of the element with the baseline of the parent element. This is default. */
	BASELINE("baseline"),
	/** Aligns the element as it was subscript. */
	SUB("sub"),
	/** Aligns the element as it was superscript. */
	SUPER("super"),
	/** The top of the element is aligned with the top of the tallest element on the line. */
	TOP("top"),
	/** The top of the element is aligned with the top of the parent element's font. */
	TEXT_TOP("text-top"),
	/** The element is placed in the middle of the parent element. */
	MIDDLE("middle"),
	/** The bottom of the element is aligned with the lowest element on the line. */
	BOTTOM("bottom"),
	/** The bottom of the element is aligned with the bottom of the parent element's font. */
	TEXT_BOTTOM("text-bottom"),
	/** Specifies that the value of the vertical-align property should be inherited from the parent element. */
	INHERIT("inherit");

	private String m_txt;

	VerticalAlignType(String s) {
		m_txt = s;
	}

	@Override
	public String toString() {
		return m_txt;
	}
}
