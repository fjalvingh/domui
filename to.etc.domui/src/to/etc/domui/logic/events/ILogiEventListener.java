package to.etc.domui.logic.events;

import javax.annotation.*;

/**
 * A listener for {@link LogiEvent} events as created by business logic.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Feb 14, 2013
 */
public interface ILogiEventListener {
	public void logicEvent(@Nonnull LogiEvent event) throws Exception;
}
