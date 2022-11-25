package to.etc.domui.component.searchpanel.lookupcontrols;

import org.eclipse.jdt.annotation.NonNull;
import to.etc.domui.component.meta.SearchPropertyMetaModel;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 3-12-17.
 */
public interface ILookupFactory<Q, D> {
	@NonNull FactoryPair<Q, D> createControl(@NonNull SearchPropertyMetaModel spm);
}
