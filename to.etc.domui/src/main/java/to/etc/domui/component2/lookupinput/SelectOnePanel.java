package to.etc.domui.component2.lookupinput;

import to.etc.domui.component.combobox.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.util.*;

import javax.annotation.*;
import java.util.*;

/**
 * A panel that is meant as a dropdown for a {@link SearchInput2} or {@link ComboBoxBase}. It
 * shows a list of selectable items that floats connected to a parent; the items can be selected
 * with either keyboard or mouse.
 *
 * <h2>Selecting elements</h2>
 *
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 13, 2014
 */
public class SelectOnePanel<T> extends Div implements IHasChangeListener {
	@Nonnull
	final private List<T> m_itemList;

	final private IRenderInto<T> m_renderer;

	/** This is the currently selected value in the javascript component. It just shows which row is highlighted. A selection is only made on onClick */
	private int m_currentSelection;

	/** This is the current "value" which is set by a real selection (click) */
	private int m_currentValue;

	private IValueChanged< ? > m_valueChanged;

	public SelectOnePanel(@Nonnull List<T> itemList, @Nonnull IRenderInto<T> renderer) {
		m_itemList = itemList;
		m_renderer = renderer;
	}

	@Override
	public void createContent() throws Exception {
		setCssClass("ui-ssop");
		Table tbl = new Table();
		add(tbl);
		TBody body = new TBody();
		tbl.add(body);
		tbl.setCellPadding("0");
		tbl.setCellSpacing("0");
		for(T item : m_itemList) {
			renderItem(body, item);
		}
		appendCreateJS("new WebUI.SelectOnePanel('" + getActualID() + "');");
		m_currentSelection = -1;
		m_currentValue = -1;
	}

	private void renderItem(@Nonnull TBody body, @Nonnull T item) throws Exception {
		TR row = body.addRow();
		row.setCssClass("ui-ssop-row");
		TD cell = row.addCell();
		m_renderer.render(cell, item);
	}

	@Override
	public boolean acceptRequestParameter(String[] values) throws Exception {
		int value = -1;
		//System.out.println(".... acceptParam " + Arrays.toString(values) + " index=" + m_currentSelection);
		if(values.length == 1) {
			String s = values[0];
			if(s != null && s.trim().length() > 0) {
				try {
					value = Integer.parseInt(s);
				} catch(Exception x) {}
			}
		}

		/*
		 * 20160512 jal This only sets the current selection, it is not allowed to fire a
		 * value change event! Value changes happen all the time; only when a click event
		 * is received has the real value of the component changed!
		 */
		m_currentSelection = value;
		//System.out.println("selectOnePanel: value=" + value + ", changed=" + changed);
		return false;
	}

	@Override
	public void internalOnClicked(@Nonnull ClickInfo cli) throws Exception {
		//-- We have a click. Has the value changed?
		if(m_currentValue != m_currentSelection) {
			m_currentValue = m_currentSelection;					// The selected one becomes the value
			internalOnValueChanged();
		}

		super.internalOnClicked(cli);
	}

	public int getIndex() {
		return m_currentValue;
	}

	@Nullable
	public T getValue() {
		int index = getIndex();
		if(index < 0 || index >= m_itemList.size())
			return null;
		return m_itemList.get(index);
	}

	@Override
	public IValueChanged< ? > getOnValueChanged() {
		return m_valueChanged;
	}

	@Override
	public void setOnValueChanged(IValueChanged< ? > valueChanged) {
		m_valueChanged = valueChanged;
	}
}
