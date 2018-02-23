package my.domui.app.core.authentication;

import my.domui.app.core.db.DbUser;
import org.apache.commons.codec.binary.Hex;
import org.bouncycastle.util.Arrays;
import to.etc.domui.login.ILoginAuthenticator;
import to.etc.domui.login.IUser;
import to.etc.domui.server.IRequestContext;
import to.etc.util.FileTool;
import to.etc.util.StringTool;
import to.etc.webapp.query.QContextManager;
import to.etc.webapp.query.QCriteria;
import to.etc.webapp.query.QDataContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.SecureRandom;

/**
 * Authenticates users to the system.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Dec 5, 2009
 */
public class LoginAuthenticator implements ILoginAuthenticator {
	static private final SecureRandom RANDOM = new SecureRandom();

	private static final String HMAC = "HmacSHA256";

	private static final String CYPHER = "AES/CBC/PKCS5Padding";

	private static final String CYPHERKEY = "AES";

	public LoginAuthenticator() throws Exception {
	}

	@Nullable
	@Override
	public IUser authenticateUser(@Nullable String uid, @Nullable String pw) throws Exception {
		//-- Locate the user, if present
		if(uid == null || uid.length() == 0)
			return null;
		QDataContext dc = QContextManager.createUnmanagedContext();
		try {
			QCriteria<DbUser> q = QCriteria.create(DbUser.class).eq("email", uid);
			DbUser p = dc.queryOne(q);
			if(p == null)
				return null;
			if(pw != null) {
				if(!isEncryptedPasswordCorrect(p.getPassword(), pw)) {
					return null;
				}
			}
			LoginUser loginUser = new LoginUser(dc, p);
			return loginUser;
		} finally {
			try {
				dc.close();
			} catch(Exception x) {
			}
		}
	}

	@Nullable
	@Override
	public String calcCookieHash(@Nullable String userid, long ts) throws Exception {
		if(userid == null || userid.length() == 0)
			return null;

		QDataContext dc = QContextManager.createUnmanagedContext();
		try {
			QCriteria<DbUser> q = QCriteria.create(DbUser.class).eq("email", userid);
			DbUser p = dc.queryOne(q);
			if(p == null)
				return null;

			//-- Calculate a hash from password:userid
			String s = p.getPassword() + ":" + p.getEmail();
			byte[] data = s.getBytes("utf-8");
			data = FileTool.hashBuffers(new byte[][]{data});
			return StringTool.toHex(data);
		} finally {
			try {
				dc.close();
			} catch(Exception x) {
			}
		}
	}

	@Nullable
	@Override
	public IUser authenticateByCookie(@Nullable String userid, long ts, String hashcode) throws Exception {
		if(userid == null || userid.length() == 0)
			return null;

		QDataContext dc = QContextManager.createUnmanagedContext();
		try {
			QCriteria<DbUser> q = QCriteria.create(DbUser.class).eq("email", userid);
			DbUser p = dc.queryOne(q);
			if(p == null)
				return null;

			//-- Calculate a hash from password:userid
			String s = p.getPassword() + ":" + p.getEmail();
			byte[] data = s.getBytes("utf-8");
			data = FileTool.hashBuffers(new byte[][]{data});
			s = StringTool.toHex(data);
			if(!s.equals(hashcode))
				return null;
			return new LoginUser(dc, p);
		} finally {
			try {
				dc.close();
			} catch(Exception x) {
			}
		}
	}

	private static final int HASH_BYTE_SIZE = 64; // 512 bits

	private static final int PBKDF2_ITERATIONS = 1000;

	private static final int SALT_BYTE_SIZE = 20;

	/**
	 * Stored password in the format
	 */
	static private final boolean isEncryptedPasswordCorrect(String db, String password) throws Exception {
		String[] passfrag = db.split(";");
		if(passfrag.length != 2)
			return false;							// Bad format -> exit

		// JAL : THIS IS NOT FUNNY 8-(
		byte[] salt = Hex.decodeHex(passfrag[0].toCharArray());
		if(salt.length != SALT_BYTE_SIZE)
			return false;
		byte[] dbhash = Hex.decodeHex(passfrag[1].toCharArray());

		PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, PBKDF2_ITERATIONS, SALT_BYTE_SIZE * 8);
		SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
		byte[] hash = skf.generateSecret(spec).getEncoded();
		return Arrays.areEqual(hash, dbhash);
	}

	static public String getEncyptedPassword(String password) throws Exception {
		byte[] salt = new byte[SALT_BYTE_SIZE];
		RANDOM.nextBytes(salt);

		PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, PBKDF2_ITERATIONS, SALT_BYTE_SIZE * 8);
		SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
		byte[] hash = skf.generateSecret(spec).getEncoded();
		return Hex.encodeHexString(salt) + ";" + Hex.encodeHexString(hash);
	}

	/**
	 * Accepts requests from others, as long as they have the correct _signature request.
	 */
	@Override public IUser authenticateByRequest(@Nonnull IRequestContext rx) throws Exception {
		return null;
	}

	static public void main(String[] args) throws Exception {
		String pw = "admin";

		String pwd = getEncyptedPassword(pw);
		System.out.println(pwd);

		if(! isEncryptedPasswordCorrect(pwd, pw))
			System.out.println("Failed");

	}
}
