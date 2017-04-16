package to.etc.event.local;

import javax.annotation.*;


/**
 * Interface to be used for events that can be handled on te same server.
 *
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jan 3, 2013
 */
public interface ILocalEventListener<T> {

	void eventFired(@Nonnull T event) throws Exception;

}
