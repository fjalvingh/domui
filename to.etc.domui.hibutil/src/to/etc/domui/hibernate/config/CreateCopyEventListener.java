package to.etc.domui.hibernate.config;

import org.hibernate.event.*;

/**
 * Used to create "before" copies of all instances loaded from the database. This listener
 * must be registered for before copy support to work on Hibernate.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jan 12, 2014
 */
public class CreateCopyEventListener implements PostLoadEventListener {
	@Override
	public void onPostLoad(PostLoadEvent event) {
		Object entity = event.getEntity();
		if(null == entity)
			throw new IllegalStateException("Entity passed to listener is null?");

		EventSource session = event.getSession();

		//-- FIXME We need to get hold of the QDataContext - how!?

	}
}
