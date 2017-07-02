package to.etc.domuidemo.pages.cddb;

import to.etc.domui.component.event.INotify;
import to.etc.domui.component.lookup.LookupForm;
import to.etc.domui.component.tbl.DataPager;
import to.etc.domui.component.tbl.DataTable;
import to.etc.domui.component.tbl.RowRenderer;
import to.etc.domui.component.tbl.SimpleSearchModel;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.IClicked;
import to.etc.domui.dom.html.NodeBase;
import to.etc.webapp.query.QCriteria;

import javax.annotation.Nonnull;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 2-7-17.
 */
public class ResultFragment<T> extends Div {
	private LookupForm<T> m_lookup;

	private DataTable<T> m_table;

	private INotify<T> m_onClick;

	public ResultFragment(LookupForm<T> lookup) {
		m_lookup = lookup;
	}

	@Override public void createContent() throws Exception {
		m_lookup.setClicked(new IClicked<NodeBase>() {
			@Override public void clicked(@Nonnull NodeBase clickednode) throws Exception {
				search(m_lookup.getEnteredCriteria());
			}
		});

	}

	private void search(QCriteria<T> enteredCriteria) {
		if(null == enteredCriteria)
			return;
		SimpleSearchModel<T> model = new SimpleSearchModel<T>(this, enteredCriteria);
		DataTable<T> table = m_table;
		if(null == table) {
			RowRenderer<T> rowRenderer = getRowRenderer();
			INotify<T> onClick = m_onClick;
			if(null != onClick) {
				rowRenderer.setRowClicked(ck -> {
					onClick.onNotify(ck);
				});
			}
			table = m_table = new DataTable<>(model, rowRenderer);
			add(table);
			table.setPageSize(20);
			add(new DataPager(table));
		} else {
			table.setModel(model);
		}
	}

	private RowRenderer<T> getRowRenderer() {
		return new RowRenderer<T>(m_lookup.getLookupClass());
	}

	public INotify<T> getOnClick() {
		return m_onClick;
	}

	public void setOnClick(INotify<T> onClick) {
		m_onClick = onClick;
	}
}
