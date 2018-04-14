package to.etc.domui.component.layout;

import org.eclipse.jdt.annotation.NonNull;

/**
 * Default caption types for {@link Caption2}.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Nov 12, 2013
 */
public enum CaptionType {
	Default("ui-cptn2-alg"), Panel("ui-cptn2-pnl");

	@NonNull
	final private String m_css;

	CaptionType(@NonNull String css) {
		m_css = css;
	}

	@NonNull
	public String getCssClass() {
		return m_css;
	}
}
