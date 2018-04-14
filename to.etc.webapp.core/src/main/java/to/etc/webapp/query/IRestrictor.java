package to.etc.webapp.query;

import org.eclipse.jdt.annotation.NonNull;


public interface IRestrictor<T> {

	@NonNull
	QOperatorNode restrict(@NonNull T value);


}
