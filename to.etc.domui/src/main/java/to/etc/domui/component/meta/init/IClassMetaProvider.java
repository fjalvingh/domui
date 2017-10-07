package to.etc.domui.component.meta.init;

import to.etc.domui.component.meta.ClassMetaModel;
import to.etc.domui.component.meta.impl.DefaultClassMetaModel;

import javax.annotation.Nonnull;

/**
 * Instances of this add metadata to an accepted metamodel.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 2-10-17.
 */
public interface IClassMetaProvider<T extends ClassMetaModel> {
	default Class<T> getModelClass() {
		return (Class<T>) DefaultClassMetaModel.class;
	}

	/**
	 * Scan whatever data is needed and add the extra data to the meta model - if you recognize it.
	 *
	 * @param model
	 * @throws Exception
	 */
	void 	provide(@Nonnull MetaInitContext context, @Nonnull T model) throws Exception;
}
