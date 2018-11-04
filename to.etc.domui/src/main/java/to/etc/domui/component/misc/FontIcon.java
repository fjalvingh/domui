package to.etc.domui.component.misc;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.dom.html.Span;

import java.util.Objects;

/**
 * Generic set of font-based icons which must be implemented by at least one of the
 * font sets used. This is merely a span with one or more css classes provided by
 * the font library.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 4-8-17.
 */
@NonNullByDefault
public class FontIcon extends Span {
	@Nullable
	private String m_iconCssClass;

	public FontIcon(@Nullable IFontIconRef name) {
		m_iconCssClass = name == null ? null : name.getCssClassName();
	}

	public FontIcon(@Nullable String name) {
		m_iconCssClass = name;
	}

	@Override
	public FontIcon css(String... classNames) {
		super.css(classNames);
		return this;
	}

	@Override public void createContent() throws Exception {
		addCssClass("ui-fnti");
		String iconName = m_iconCssClass;
		if(null != iconName)
			addCssClass(iconName);
	}

	public void setIconName(@Nullable String name) {
		if(Objects.equals(name, m_iconCssClass))
			return;
		String oldName = m_iconCssClass;
		if(null != oldName)
			removeCssClass(oldName);
		if(null != name)
			addCssClass(name);
		m_iconCssClass = name;
	}

	public void setIconName(@Nullable IFontIconRef iconName) {
		setIconName(iconName == null ? null : iconName.getCssClassName());
	}
}
