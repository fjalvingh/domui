package to.etc.domui.logic;

import javax.annotation.*;

import to.etc.domui.logic.errors.*;
import to.etc.webapp.query.*;

/**
 * Created by jal on 11/8/14.
 */
public interface ILogicContext {
	@Nonnull
	public QDataContext dc();

	@Nonnull
	public <L> L get(@Nonnull Class<L> classClass) throws Exception;

	@Nonnull
	public <L extends ILogic, K, T extends IIdentifyable<K>> L get(@Nonnull Class<L> clz, @Nonnull T instance) throws Exception;

	@Nonnull
	public ProblemModel getErrorModel();
}
