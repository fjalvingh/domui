package to.etc.domui.component.misc;

import org.eclipse.jdt.annotation.NonNull;

/**
 * Present on font icon enums, this defines the things the enum
 * needs to have to allow it to be used as a font icon.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 20-10-18.
 */
public interface IFontIcon {
	@NonNull String getCssClassName();
}
