package my.domui.app.core.db;

import org.hibernate.engine.SessionImplementor;
import org.hibernate.id.UUIDGenerationStrategy;

import java.util.UUID;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 12-2-18.
 */
public class UuidGenerator implements UUIDGenerationStrategy {
	@Override public UUID generateUUID(SessionImplementor sessionImplementor) {
		return UUID.randomUUID();
	}

	@Override public int getGeneratedVersion() {
		return 4;
	}
}
