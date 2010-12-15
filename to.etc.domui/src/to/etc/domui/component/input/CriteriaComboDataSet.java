package to.etc.domui.component.input;

import java.util.*;

import javax.annotation.*;

import to.etc.domui.dom.html.*;
import to.etc.domui.util.*;
import to.etc.webapp.query.*;

/**
 * A Combobox dataset provider which creates a dataset by using a QCriteria passed to it.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Dec 16, 2010
 */
public class CriteriaComboDataSet<T> implements IComboDataSet<T> {
	@Nonnull
	final private QCriteria<T> m_query;

	/**
	 * Create with the specified immutable QCriteria.
	 * @param query
	 */
	public CriteriaComboDataSet(@Nonnull QCriteria<T> query) {
		m_query = query;
	}

	/**
	 * Execute the query and return the result.
	 * @see to.etc.domui.util.IComboDataSet#getComboDataSet(to.etc.domui.dom.html.UrlPage)
	 */
	@Override
	public List<T> getComboDataSet(UrlPage page) throws Exception {
		return page.getSharedContext().query(m_query);
	}
}
