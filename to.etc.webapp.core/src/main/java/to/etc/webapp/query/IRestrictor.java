package to.etc.webapp.query;

import javax.annotation.*;


public interface IRestrictor<T> {

	@Nonnull
	QOperatorNode restrict(@Nonnull T value);


}
