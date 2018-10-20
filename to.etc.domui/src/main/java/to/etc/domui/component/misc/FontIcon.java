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
	private IFontIcon m_iconName;

	public FontIcon(@Nullable IFontIcon name) {
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
		IFontIcon iconName = m_iconName;
		if(null != iconName)
			addCssClass(iconName.getCssClassName());
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

	public void setIconName(IFontIcon iconName) {
		if(Objects.equals(iconName, m_iconName))
			return;
		IFontIcon oldName = m_iconName;
		if(null != oldName)
			removeCssClass(oldName.getCssClassName());
		if(null != iconName)
			addCssClass(iconName.getCssClassName());
		m_iconName = iconName;
	}
}
