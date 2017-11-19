package to.etc.domui.component.layout;

import javax.annotation.*;

/**
 * Default caption types for {@link Caption2}.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Nov 12, 2013
 */
public enum CaptionType {
	Default("ui-cptn2-alg"), Panel("ui-cptn2-pnl");

	@Nonnull
	final private String m_css;

	CaptionType(@Nonnull String css) {
		m_css = css;
	}

	@Nonnull
	public String getCssClass() {
		return m_css;
	}
}
