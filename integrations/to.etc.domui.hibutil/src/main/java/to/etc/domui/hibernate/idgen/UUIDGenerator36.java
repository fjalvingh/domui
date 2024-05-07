package to.etc.domui.hibernate.idgen;

import org.eclipse.jdt.annotation.NonNull;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;

import java.io.Serializable;
import java.util.UUID;

/**
 * Create a String identifier that is based on an UUID, but which does
 * not use the very verbose string format of those things. Instead the
 * 128 bits are rendered as a base64 like string where the two non-ascii
 * characters are _ and -, to make them URL safe.
 *
 * To use the generator annotate your class(es) ID property as follows:
 * <pre>
 *  &#064;GeneratedValue(generator = "uuid3")
 * 	&#064;GenericGenerator(name = "uuid3", strategy = "to.etc.domui.hibernate.idgen.UUIDGenerator36")
 * 	&#064;Id
 * 	&#064;Column(name = "id", length = 23, nullable = false)
 * 	&#064;Nullable @Override public String getId() {
 * 		return m_id;
 *  }
 * </pre>
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 12-2-18.
 */
final public class UUIDGenerator36 implements IdentifierGenerator {
	static private final char[]	BASE64MAP	= "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz-_".toCharArray();


	@Override
	public Serializable generate(SharedSessionContractImplementor sharedSessionContractImplementor, Object o) throws HibernateException {
		return generate();
	}

	static public String generate() {
		UUID uuid = UUID.randomUUID();
		byte[] data = new byte[16];

		moveBytes(data, 0, uuid.getMostSignificantBits());
		moveBytes(data, 8, uuid.getLeastSignificantBits());
		String str = encodeBase64(data);
		return str;
	}

	private static void moveBytes(byte[] bytes, int offset, long bits) {
		for(int i = 8; --i >= 0; ) {
			bytes[i + offset] = (byte) (bits & 0xff);
			bits = bits >> 8;
		}
	}

	private static String encodeBase64(@NonNull byte[] data) {
		int sidx;
		StringBuilder sb = new StringBuilder(24);

		// 3-byte to 4-byte conversion + 0-63 to ascii printable conversion
		for(sidx = 0; sidx < data.length - 2; sidx += 3) {
			sb.append(BASE64MAP[(data[sidx] >>> 2) & 077]);
			sb.append(BASE64MAP[(data[sidx + 1] >>> 4) & 017 | (data[sidx] << 4) & 077]);
			sb.append(BASE64MAP[(data[sidx + 2] >>> 6) & 003 | (data[sidx + 1] << 2) & 077]);
			sb.append(BASE64MAP[data[sidx + 2] & 077]);
		}
		if(sidx < data.length) {
			sb.append(BASE64MAP[(data[sidx] >>> 2) & 077]);
			if(sidx < data.length - 1) {
				sb.append(BASE64MAP[(data[sidx + 1] >>> 4) & 017 | (data[sidx] << 4) & 077]);
				sb.append(BASE64MAP[(data[sidx + 1] << 2) & 077]);
			} else
				sb.append(BASE64MAP[(data[sidx] << 4) & 077]);
		}
		return sb.toString();
	}


}
