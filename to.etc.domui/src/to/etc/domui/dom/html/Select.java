package to.etc.domui.dom.html;

/**
 * INCOMPLETE A full-coded select box: this is unsuitable for large amount of options.
 * 
 * Handling the selected item is incomplete.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 11, 2008
 */
public class Select extends InputNodeContainer {
	private boolean m_multiple;

	private boolean m_disabled;

	private int m_size;

	private int m_selectedIndex;

	public Select() {
		super("select");
	}

	public Select(String... txt) {
		this();
		for(String s : txt) {
			add(new SelectOption(s));
		}
	}

	@Override
	public void visit(NodeVisitor v) throws Exception {
		v.visitSelect(this);
	}

	@Override
	protected boolean canContain(NodeBase node) {
		return node instanceof SelectOption;
	}

	public boolean isMultiple() {
		return m_multiple;
	}

	public void setMultiple(boolean multiple) {
		m_multiple = multiple;
	}

	public boolean isDisabled() {
		return m_disabled;
	}

	public void setDisabled(boolean disabled) {
		m_disabled = disabled;
	}

	public int getSize() {
		return m_size;
	}

	public void setSize(int size) {
		m_size = size;
	}

	@Override
	public void setReadOnly(boolean readOnly) {
		if(isReadOnly() == readOnly)
			return;
		changed();
		super.setReadOnly(readOnly);
	}

	public SelectOption getOption(int ix) {
		if(ix < 0 || ix >= getChildCount())
			throw new ArrayIndexOutOfBoundsException("The option index " + ix + " is invalid, the #options is " + getChildCount());
		return (SelectOption) getChild(ix);
	}

	@Override
	public void acceptRequestParameter(String[] values) throws Exception {
		String in = values[0];
		SelectOption selo = (SelectOption) getPage().findNodeByID(in);
		if(selo == null) {
			m_selectedIndex = -1;
		} else {
			m_selectedIndex = findChildIndex(selo); // Must be found
		}
		for(int i = getChildCount(); --i >= 0;) {
			getOption(i).setSelected(i == m_selectedIndex);
		}
	}

	public void clearSelected() {
		m_selectedIndex = -1;
		for(int i = getChildCount(); --i >= 0;) {
			getOption(i).setSelected(false);
		}
	}

	public int getSelectedIndex() {
		return m_selectedIndex;
	}

	public void setSelectedIndex(int ix) {
		m_selectedIndex = ix;
		for(int i = getChildCount(); --i >= 0;) {
			getOption(i).setSelected(i == m_selectedIndex);
		}
	}
}
