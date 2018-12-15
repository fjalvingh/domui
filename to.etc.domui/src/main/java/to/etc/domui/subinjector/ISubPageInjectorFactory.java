package to.etc.domui.subinjector;

import org.eclipse.jdt.annotation.NonNullByDefault;
import to.etc.domui.dom.html.SubPage;

import java.util.List;

/**
 * A factory for injectors of a SubPage. This calculates whatever injectors it
 * thinks the page needs.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 4-12-18.
 */
@NonNullByDefault
public interface ISubPageInjectorFactory {
	List<ISubPageInjector> calculateInjectors(Class<? extends SubPage> clzz);
}
