package to.etc.domui.logic;

import org.eclipse.jdt.annotation.NonNull;
import to.etc.domui.logic.errors.ProblemModel;
import to.etc.webapp.query.IIdentifyable;
import to.etc.webapp.query.QDataContext;

/**
 * Created by jal on 11/8/14.
 */
public interface ILogicContext {
	@NonNull
	QDataContext dc();

	@NonNull
	<L> L get(@NonNull Class<L> classClass) throws Exception;

	@NonNull
	<L extends ILogic, K, T extends IIdentifyable<K>> L get(@NonNull Class<L> clz, @NonNull T instance) throws Exception;

	<T extends ILogic, K, V extends IIdentifyable<K>> void register(Class<?> registrationType, T logicClass, V dataClass);

	@NonNull
	<L extends ILogic> L get(@NonNull Class<L> clz, @NonNull Object reference) throws Exception;

	@NonNull
	ProblemModel getErrorModel();
}
