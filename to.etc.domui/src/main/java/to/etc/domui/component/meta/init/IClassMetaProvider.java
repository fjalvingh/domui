package to.etc.domui.component.meta.init;

import org.eclipse.jdt.annotation.NonNull;
import to.etc.domui.component.meta.ClassMetaModel;
import to.etc.domui.component.meta.impl.DefaultClassMetaModel;

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
	void 	provide(@NonNull MetaInitContext context, @NonNull T model) throws Exception;
}
