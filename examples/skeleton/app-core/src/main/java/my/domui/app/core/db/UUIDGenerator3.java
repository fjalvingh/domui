package my.domui.app.core.db;

import org.hibernate.HibernateException;
import org.hibernate.engine.SessionImplementor;
import org.hibernate.id.IdentifierGenerator;
import to.etc.util.StringTool;

import java.io.Serializable;
import java.util.UUID;

/**
 * Create a String identifier that is based on an UUID, but which does
 * not use the very verbose string format of those things. Instead the
 * 128 bits are rendered as a base64 string (with the last 2 == signs
 * stripped), forming a 23 char identifier.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 12-2-18.
 */
final public class UUIDGenerator3 implements IdentifierGenerator {
	@Override public Serializable generate(SessionImplementor sessionImplementor, Object o) throws HibernateException {
		UUID uuid = UUID.randomUUID();
		byte[] data = new byte[16];

		moveBytes(data, 0, uuid.getMostSignificantBits());
		moveBytes(data, 8, uuid.getLeastSignificantBits());
		String str = StringTool.encodeBase64ToString(data);

		return str.substring(0, 23);				// Strip the ==
	}

	private void moveBytes(byte[] bytes, int offset, long bits) {
		for(int i = 8; --i >= 0;) {
			bytes[i + offset] = (byte) (bits & 0xff);
			bits = bits >> 8;
		}
	}

}
