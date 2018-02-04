package to.etc.domui.component.searchpanel.lookupcontrols;

import to.etc.domui.component.meta.SearchPropertyMetaModel;

import javax.annotation.Nonnull;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 3-12-17.
 */
public interface ILookupFactory<D> {
	@Nonnull FactoryPair<D> createControl(@Nonnull SearchPropertyMetaModel spm);
}
