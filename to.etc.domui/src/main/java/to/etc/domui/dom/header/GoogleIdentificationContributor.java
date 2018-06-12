package to.etc.domui.dom.header;

import to.etc.domui.dom.IContributorRenderer;

import java.util.Objects;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 11-3-18.
 */
public class GoogleIdentificationContributor extends HeaderContributor {
	private final String m_key;

	public GoogleIdentificationContributor(String key) {
		m_key = key;
	}

	@Override public boolean isOfflineCapable() {
		return false;
	}

	@Override public void contribute(IContributorRenderer r) throws Exception {
		r.renderLoadJavascript("https://apis.google.com/js/platform.js", true, true);
		r.renderLoadJavascript(r.ctx().getRelativePath("$js/domui.login.js"), true, true);
		r.o().tag("meta");
		r.o().attr("name", "google-signin-client_id");
		r.o().attr("content", m_key);
		r.o().endtag();
	}

	@Override public boolean equals(Object o) {
		if(this == o) return true;
		if(o == null || getClass() != o.getClass()) return false;
		GoogleIdentificationContributor that = (GoogleIdentificationContributor) o;
		return Objects.equals(m_key, that.m_key);
	}

	@Override public int hashCode() {
		return Objects.hash(m_key);
	}
}
