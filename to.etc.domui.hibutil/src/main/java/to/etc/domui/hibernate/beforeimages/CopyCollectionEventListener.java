package to.etc.domui.hibernate.beforeimages;

import org.hibernate.*;
import org.hibernate.event.*;

/**
 * This listener must be added to Hibernate's listeners to support before-images of loaded collections. The
 * listener gets called after a lazy collection is loaded, and will update the "mirror" of that collection
 * inside the before cache.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jan 19, 2014
 */
public class CopyCollectionEventListener implements InitializeCollectionEventListener {
	@Override
	public void onInitializeCollection(InitializeCollectionEvent event) throws HibernateException {
		EventSource session = event.getSession();

		//-- The only way to get hold of the QDataContext is through the Interceptor which must be of a type we support.
		Interceptor ic = session.getInterceptor();
		if(!(ic instanceof BeforeImageInterceptor))
			throw new IllegalStateException("Interceptor must be of type 'CreateCopyInterceptor' to allow before-images");

		BeforeImageInterceptor ccic = (BeforeImageInterceptor) ic;
		ccic.collectionLoaded(event.getCollection());
	}
}
