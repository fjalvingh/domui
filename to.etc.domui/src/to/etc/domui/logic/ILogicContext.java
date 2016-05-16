package to.etc.domui.logic;

import javax.annotation.*;

import to.etc.domui.logic.errors.*;
import to.etc.webapp.query.*;

/**
 * Created by jal on 11/8/14.
 */
public interface ILogicContext {
	@Nonnull
	QDataContext dc();

	@Nonnull
	<L> L get(@Nonnull Class<L> classClass) throws Exception;

	@Nonnull
	<L extends ILogic, K, T extends IIdentifyable<K>> L get(@Nonnull Class<L> clz, @Nonnull T instance) throws Exception;

	<T extends ILogic, K, V extends IIdentifyable<K>> void register(Class<?> registrationType, T logicClass, V dataClass);

	<L extends ILogic> L get(@Nonnull Class<L> clz, Object reference) throws Exception;

	@Nonnull
	ProblemModel getErrorModel();
}
