package to.etc.domui.dom.css;

/**
 * CSS2 display property values
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Aug 17, 2007
 */
public enum DisplayType {
	NONE("none"), INLINE("inline"), BLOCK("block"), LIST_ITEM("list-item"), RUN_IN("run-in"), COMPACT("compact"), MARKER("marker"), TABLE("table"), INLINE_TABLE("inline-table"), TABLE_ROW_GROUP(
		"table-row-group"), TABLE_HEADER_GROUP("table-header-group"), TABLE_FOOTER_GROUP("table-footer-group"), TABLE_ROW("table-row"), TABLE_COLUMN_GROUP("table-column-group"), TABLE_COLUMN(
		"table-column"), TABLE_CELL("table-cell"), TABLE_CAPTION("table-caption");

	private String m_txt;

	DisplayType(String txt) {
		m_txt = txt;
	}

	@Override
	public String toString() {
		return m_txt;
	}

}
