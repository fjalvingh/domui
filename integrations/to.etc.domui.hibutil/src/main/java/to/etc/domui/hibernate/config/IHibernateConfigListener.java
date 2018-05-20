package to.etc.domui.hibernate.config;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.event.service.spi.EventListenerRegistry;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 20-5-18.
 */
@NonNullByDefault
public interface IHibernateConfigListener {
	/**
	 * Allow altering service settings using builder.applySetting().
	 */
	void onSettings(StandardServiceRegistryBuilder builder);

	void onAddSources(MetadataSources sources);

	void onAddListeners(EventListenerRegistry listenerRegistry);
}
