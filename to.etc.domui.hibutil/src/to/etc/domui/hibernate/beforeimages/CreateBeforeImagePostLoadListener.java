package to.etc.domui.hibernate.beforeimages;

import org.hibernate.*;
import org.hibernate.event.*;

/**
 * Used to create "before" copies of all instances loaded from the database. This listener
 * must be registered for before copy support to work on Hibernate.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jan 12, 2014
 */
public class CreateBeforeImagePostLoadListener implements PostLoadEventListener {
	@Override
	public void onPostLoad(PostLoadEvent event) {
		Object entity = event.getEntity();
		if(null == entity)
			throw new IllegalStateException("Entity passed to listener is null?");

		EventSource session = event.getSession();

		//-- The only way to get hold of the QDataContext is through the Interceptor which must be of a type we support.
		Interceptor ic = session.getInterceptor();
		if(!(ic instanceof BeforeImageInterceptor))
			throw new IllegalStateException("Interceptor must be of type '" + BeforeImageInterceptor.class.getName() + "' to allow before-images");
		BeforeImageInterceptor ccic = (BeforeImageInterceptor) ic;
		ccic.onAfterLoad(event);
	}
}
