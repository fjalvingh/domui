package to.etc.domui.logic;

import to.etc.webapp.query.IIdentifyable;
import to.etc.webapp.query.QDataContext;

import javax.annotation.Nonnull;

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

	@Nonnull
	LogiErrors getErrorModel();
}
