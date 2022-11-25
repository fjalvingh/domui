package to.etc.domui.dom.header;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.dom.IBrowserOutput;
import to.etc.domui.dom.IContributorRenderer;
import to.etc.domui.dom.html.OptimalDeltaRenderer;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 10-10-19.
 */
public class LinkHeaderContributor extends HeaderContributor {
	@NonNull
	private final String m_rel;

	@NonNull
	private final String m_href;

	@Nullable
	private final String m_type;

	public LinkHeaderContributor(@NonNull String rel, @NonNull String href, @Nullable String type) {
		m_rel = rel;
		m_href = href;
		m_type = type;
	}

	@Override
	public void contribute(IContributorRenderer r) throws Exception {
		if(r instanceof OptimalDeltaRenderer)
			return;

		IBrowserOutput o = r.o();

		o.tag("link");
		o.attr("rel", m_rel);
		o.attr("href", r.ctx().getRelativePath(m_href));
		if(null != m_type)
			o.attr("type", m_type);
		if(r.isXml())
			o.endAndCloseXmltag();
		else
			o.endtag();
		o.dec();

	}

	@Override
	public boolean isOfflineCapable() {
		return true;
	}

	@Override
	public boolean equals(Object o) {
		if(this == o)
			return true;
		if(o == null || getClass() != o.getClass())
			return false;

		LinkHeaderContributor that = (LinkHeaderContributor) o;

		if(!m_rel.equals(that.m_rel))
			return false;
		if(!m_href.equals(that.m_href))
			return false;
		return m_type != null ? m_type.equals(that.m_type) : that.m_type == null;
	}

	@Override
	public int hashCode() {
		int result = m_rel.hashCode();
		result = 31 * result + m_href.hashCode();
		result = 31 * result + (m_type != null ? m_type.hashCode() : 0);
		return result;
	}
}
