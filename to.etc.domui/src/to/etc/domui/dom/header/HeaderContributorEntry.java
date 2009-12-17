package to.etc.domui.dom.header;

final public class HeaderContributorEntry {
	private HeaderContributor m_contributor;

	private int m_order;

	public HeaderContributorEntry(HeaderContributor contributor, int order) {
		m_contributor = contributor;
		m_order = order;
	}

	public HeaderContributor getContributor() {
		return m_contributor;
	}

	public int getOrder() {
		return m_order;
	}
}
