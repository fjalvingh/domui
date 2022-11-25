package to.etc.domui.component.searchpanel.lookupcontrols;

import to.etc.domui.dom.html.IControl;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 3-12-17.
 */
public class FactoryPair<Q, D> {
	private final ILookupQueryBuilder<Q, D>	m_queryBuilder;

	private final IControl<D> m_control;

	public FactoryPair(ILookupQueryBuilder<Q, D> queryBuilder, IControl<D> control) {
		m_queryBuilder = queryBuilder;
		m_control = control;
	}

	public ILookupQueryBuilder<Q, D> getQueryBuilder() {
		return m_queryBuilder;
	}

	public IControl<D> getControl() {
		return m_control;
	}
}
