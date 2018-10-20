package to.etc.domui.component.misc;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.dom.html.Span;

import java.util.Objects;

/**
 * Generic set of font-based icons which must be implemented by at least one of the
 * font sets used.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 4-8-17.
 */
@NonNullByDefault
final public class FontIcon extends Span {
	@Nullable
	private String m_iconName;

	public FontIcon(@Nullable String name) {
		m_iconName = name;
	}

	@Override
	public FontIcon css(String... classNames) {
		super.css(classNames);
		return this;
	}

	@Override public void createContent() throws Exception {
		//removeFaClasses();
		addCssClass("fa");
		if(null != m_iconName)
			addCssClass(m_iconName);
	}

	private void removeFaClasses() {
		String cssClass = getCssClass();
		if(null == cssClass)
			return;

		String[] split = cssClass.split("\\s+");
		for(String s : split) {
			if(s.equals("fa") || s.startsWith("fa-")) {
				removeCssClass(s);
			}
		}
	}

	public void setIconName(String iconName) {
		if(Objects.equals(iconName, m_iconName))
			return;
		String oldName = m_iconName;
		if(null != oldName)
			removeCssClass(oldName);
		if(null != iconName)
			addCssClass(iconName);
		m_iconName = iconName;
	}
}
