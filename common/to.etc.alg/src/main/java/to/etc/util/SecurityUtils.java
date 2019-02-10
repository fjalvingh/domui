/*
 * DomUI Java User Interface - shared code
 * Copyright (c) 2010 by Frits Jalvingh, Itris B.V.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * See the "sponsors" file for a list of supporters.
 *
 * The latest version of DomUI and related code, support and documentation
 * can be found at http://www.domui.org/
 * The contact for the project is Frits Jalvingh <jal@etc.to>.
 */
package to.etc.util;

import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.openssl.jcajce.JcaPKCS8Generator;
import org.bouncycastle.util.io.pem.PemObject;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.EncodedKeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Apr 6, 2004
 */
public class SecurityUtils {

	static private final SecureRandom RANDOM = new SecureRandom();

	public static final String SSH_RSA = "ssh-rsa";

	static public String encodeToHex(PrivateKey privk) {
		byte[] enc = privk.getEncoded();
		return StringTool.toHex(enc);
	}

	static public String encodeToHex(PublicKey pubk) {
		byte[] enc = pubk.getEncoded();
		return StringTool.toHex(enc);
	}

	static public String encodeToBase64(PrivateKey privk) {
		byte[] enc = privk.getEncoded();
		return StringTool.encodeBase64ToString(enc);
	}

	static public String encodeToBase64(PublicKey pubk) {
		byte[] enc = pubk.getEncoded();
		return StringTool.encodeBase64ToString(enc);
	}


	/**
	 * Decodes a public key from an encoded value.
	 * @param enc	the hex string.
	 * @return		a public key.
	 */
	static public PublicKey decodePublicKeyFromHex(String enc, String algo) throws Exception {
		//-- 1. Decode the hex string into a byte array,
		byte[] ba = StringTool.fromHex(enc);
		if(!StringTool.toHex(ba).equalsIgnoreCase(enc))
			throw new Exception("ASSERT: Problem with hex/binary translation");

		//-- Array defined.
		EncodedKeySpec pks = new X509EncodedKeySpec(ba); // Public keys are X509 encoded, of course: as clear as fog..
		KeyFactory kf = KeyFactory.getInstance(algo);
		return kf.generatePublic(pks);
	}

	/**
	 * Decodes a private key from an encoded value.
	 * @param enc	the hex string.
	 * @return		a public key.
	 */
	static public PrivateKey decodePrivateKeyFromHex(String enc, String algo) throws Exception {
		//-- 1. Decode the hex string into a byte array,
		byte[] ba = StringTool.fromHex(enc);
		if(!StringTool.toHex(ba).equalsIgnoreCase(enc))
			throw new Exception("ASSERT: Problem with hex/binary translation");

		//-- Array defined.
		EncodedKeySpec pks = new PKCS8EncodedKeySpec(ba); // Private keys are PKCS8 encoded.
		KeyFactory kf = KeyFactory.getInstance(algo);
		return kf.generatePrivate(pks);
	}

	/**
	 * Decodes a public key from an encoded value.
	 * @param enc	the hex string.
	 * @return		a public key.
	 */
	static public PublicKey decodePublicKeyFromHex(String enc) throws Exception {
		return decodePublicKeyFromHex(enc, "DSA");
	}

	/**
	 * Decodes a private key from an encoded value.
	 * @param enc	the hex string.
	 * @return		a public key.
	 */
	static public PrivateKey decodePrivateKeyFromHex(String enc) throws Exception {
		return decodePrivateKeyFromHex(enc, "DSA");
	}

	/**
	 * Decodes a public key from an encoded value.
	 * @param enc	the hex string.
	 * @return		a public key.
	 */
	static public PublicKey decodePublicKeyFromBase64(String enc) throws Exception {
		return decodePublicKeyFromBase64(enc, "DSA");
	}

	/**
	 * Decodes a private key from an encoded value.
	 * @param enc	the hex string.
	 * @return		a public key.
	 */
	static public PrivateKey decodePrivateKeyFromBase64(String enc) throws Exception {
		return decodePrivateKeyFromBase64(enc, "DSA");
	}

	/**
	 * Decodes a public key from an encoded value.
	 * @param enc	the hex string.
	 * @return		a public key.
	 */
	static public PublicKey decodePublicKeyFromBase64(String enc, String algo) throws Exception {
		//-- 1. Decode the hex string into a byte array,
		byte[] ba = StringTool.decodeBase64(enc);
		EncodedKeySpec pks = new X509EncodedKeySpec(ba); // Public keys are X509 encoded, of course: as clear as fog..
		KeyFactory kf = KeyFactory.getInstance(algo);
		return kf.generatePublic(pks);
	}

	/**
	 * Decodes a private key from an encoded value.
	 * @param enc	the hex string.
	 * @return		a public key.
	 */
	static public PrivateKey decodePrivateKeyFromBase64(String enc, String algo) throws Exception {
		//-- 1. Decode the hex string into a byte array,
		byte[] ba = StringTool.decodeBase64(enc);
		EncodedKeySpec pks = new PKCS8EncodedKeySpec(ba); // Private keys are PKCS8 encoded.
		KeyFactory kf = KeyFactory.getInstance(algo);
		return kf.generatePrivate(pks);
	}



	/*--------------------------------------------------------------*/
	/*	CODING:	Hashing functions.									*/
	/*--------------------------------------------------------------*/
	/**
	 * Returns the MD5 hash for the data passed.
	 */
	static public byte[] md5Hash(byte[] data) {
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("MD5");
		} catch(NoSuchAlgorithmException x) {
			throw new RuntimeException("MISSING MANDATORY SECURITY DIGEST PROVIDER MD5: " + x.getMessage());
		}
		md.update(data);
		return md.digest();
	}

	/**
	 * Returns the MD5 hash for the data passed.
	 */
	static public byte[] md5Hash(byte[][] data) {
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("MD5");
		} catch(NoSuchAlgorithmException x) {
			throw new RuntimeException("MISSING MANDATORY SECURITY DIGEST PROVIDER MD5: " + x.getMessage());
		}
		for(byte[] buf : data)
			md.update(buf);
		return md.digest();
	}

	@NonNull
	static public String getMD5Hash(@NonNull String in, @NonNull String encoding) {
		try {
			byte[] hash = md5Hash(in.getBytes(encoding));
			return StringTool.toHex(hash);
		} catch(Exception x) {
			throw WrappedException.wrap(x);
		}
	}
	static public String getMD5Hash(@NonNull String in, @NonNull Charset encoding) {
		try {
			byte[] hash = md5Hash(in.getBytes(encoding));
			return StringTool.toHex(hash);
		} catch(Exception x) {
			throw WrappedException.wrap(x);
		}
	}


	static public byte[] createSalt(int bytes) {
		byte[] salt = new byte[bytes];
		RANDOM.nextBytes(salt);
		return salt;
	}

	/*----------------------------------------------------------------------*/
	/*	CODING:	SSH format keys												*/
	/*----------------------------------------------------------------------*/
	/*
	 * https://www.cryptosys.net/pki/rsakeyformats.html
	 */

	/**
	 * Decodes a id_xxx.pub format key, like:
	 * <pre>
	 *     ssh-rsa AAAAB3N....== jal@etc.to
	 * </pre>
	 *
	 * See https://stackoverflow.com/questions/3706177/how-to-generate-ssh-compatible-id-rsa-pub-from-java
	 */
	static public PublicKey decodeSshPublicKey(String text) throws KeyFormatException {
		try {
			// Remove any newlines that can be the result of pasting.
			text = text.replace("\r", "").replace("\n", "");
			String[] split = text.split("\\s+");
			if(split.length < 2)
				throw new KeyFormatException("ssh key format not recognised");
			if(SSH_RSA.equals(split[0])) {
				byte[] data = StringTool.decodeBase64(split[1]);
				try(DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data))) {
					byte[] buf = readIntLenBytes(dis);			// ssh-rsa signature
					if(buf.length != 7 || !Arrays.equals(buf, SSH_RSA.getBytes()))
						throw new KeyFormatException("Expecting byte pattern ssh-rsa");
					BigInteger exp = new BigInteger(readIntLenBytes(dis));
					BigInteger mod = new BigInteger(readIntLenBytes(dis));
					return new RSAPublicKeyImpl(exp, mod, "RSA", "RFC4251", data);
				}
			}
		} catch(Exception x) {
			throw new KeyFormatException(x, "Failed to decode public key");
		}
		throw new KeyFormatException("Key format not recognised");
	}

	private static byte[] readIntLenBytes(DataInputStream dis) throws Exception {
		int l = dis.readInt();						// length of public exponent
		if(l < 0 || l > 8192)
			throw new KeyFormatException("Bad length");
		byte[] buf = new byte[l];
		if(dis.read(buf) != l)
			throw new KeyFormatException("Bad length");
		return buf;
	}

	static public String encodeSshPublicKey(PublicKey key, @Nullable String userId) throws KeyFormatException {
		String algo = key.getAlgorithm();
		if("RSA".equalsIgnoreCase(algo)) {
			try(ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
				try(DataOutputStream dos = new DataOutputStream(bos)) {
					byte[] bytes = SSH_RSA.getBytes();
					dos.writeInt(bytes.length);
					dos.write(bytes);

					RSAPublicKey p = (RSAPublicKey) key;
					BigInteger exp = p.getPublicExponent();
					bytes = exp.toByteArray();
					dos.writeInt(bytes.length);
					dos.write(bytes);

					BigInteger mod = p.getModulus();
					bytes = mod.toByteArray();
					dos.writeInt(bytes.length);
					dos.write(bytes);

					if(userId == null)
						userId = "unknown";
					return "ssh-rsa"
						+ " " + StringTool.encodeBase64ToString(bos.toByteArray())
						+ " " + userId;
				}
			} catch(IOException x) {
				throw new KeyFormatException(x, "Nonsense");
			}
		}

		throw new KeyFormatException("Unsupported key algorithm: " + algo);
	}

	/**
	 *
	 */
	static public PrivateKey decodeSshPrivateKey(String key) throws KeyFormatException {
		try {
			if(key.startsWith("-----BEGIN RSA PRIVATE KEY-----")) {		// PKCS#1
				//-- PEM like format. Strip pem lines
				byte[] pkcs1 = readPemFormat(key);
				byte[] pkcs8 = decodeRsaPKCS1PrivateKey(pkcs1);

				EncodedKeySpec pks = new PKCS8EncodedKeySpec(pkcs8);
				KeyFactory kf = KeyFactory.getInstance("RSA");
				return kf.generatePrivate(pks);
			} else if(key.startsWith("-----BEGIN ENCRYPTED PRIVATE KEY-----")) {

			}
		} catch(Exception x) {
			throw new KeyFormatException(x, "Bad or unknown format");
		}
		throw new KeyFormatException("Unknown format");
	}

	/**
	 * Change the unencrypted PKCS#1 RSA private key format to PKCS#8.
	 *
	 * See
	 * https://stackoverflow.com/questions/45646808/convert-an-rsa-pkcs1-private-key-string-to-a-java-privatekey-object
	 * https://stackoverflow.com/questions/23709898/java-convert-dkim-private-key-from-rsa-to-der-for-javamail
	 */
	static private byte[] decodeRsaPKCS1PrivateKey(byte[] oldder) {
		final byte[] prefix = {0x30,(byte)0x82,0,0, 2,1,0, // SEQUENCE(lenTBD) and version INTEGER
			0x30,0x0d, 6,9,0x2a,(byte)0x86,0x48,(byte)0x86,(byte)0xf7,0x0d,1,1,1, 5,0, // AlgID for rsaEncryption,NULL
			4,(byte)0x82,0,0 }; // OCTETSTRING(lenTBD)
		byte[] newder = new byte [prefix.length + oldder.length];
		System.arraycopy (prefix,0, newder,0, prefix.length);
		System.arraycopy (oldder,0, newder,prefix.length, oldder.length);
		// and patch the (variable) lengths to be correct
		int len = oldder.length, loc = prefix.length-2;
		newder[loc] = (byte)(len>>8); newder[loc+1] = (byte)len;
		len = newder.length-4; loc = 2;
		newder[loc] = (byte)(len>>8); newder[loc+1] = (byte)len;

		return newder;
	}

	static private final byte[] readPemFormat(String in) {
		StringBuilder sb = new StringBuilder();
		for(String line: new LineIterator(in)) {
			if(! line.startsWith("--") && line.length() > 0) {
				sb.append(line);

			}
		}
		return StringTool.decodeBase64(sb.toString());
	}

	/**
	 * See
	 * http://techxperiment.blogspot.com/2016/10/create-and-read-pkcs-8-format-private.html
	 */
	static public String encodeSshPkcs8PrivateKey(PrivateKey key) throws Exception {
		JcaPKCS8Generator g = new JcaPKCS8Generator(key, null);
		PemObject pem = g.generate();
		StringWriter sw = new StringWriter();
		try(JcaPEMWriter pw = new JcaPEMWriter(sw)) {
			pw.writeObject(pem);
		}
		return sw.toString();
	}

	public final static class RSAPublicKeyImpl implements RSAPublicKey {
		private final BigInteger m_publicExp;

		private final BigInteger m_modulo;

		private final String m_algo;
		private final String m_format;
		private final byte[] m_encoded;

		public RSAPublicKeyImpl(BigInteger publicExp, BigInteger modulo, String algo, String format, byte[] encoded) {
			m_publicExp = publicExp;
			m_modulo = modulo;
			m_algo = algo;
			m_format = format;
			m_encoded = encoded;
		}

		@Override public BigInteger getPublicExponent() {
			return m_publicExp;
		}

		@Override public String getAlgorithm() {
			return m_algo;
		}

		@Override public String getFormat() {
			return m_format;
		}

		@Override public byte[] getEncoded() {
			return m_encoded;
		}

		@Override public BigInteger getModulus() {
			return m_modulo;
		}
	}

	/**
	 * Generate a secure password hash by randomly generating a salt, then
	 * hashing the password using PBKDF2.
	 */
	static public String encryptPassword(String password) throws Exception {
		byte[] salt = createSalt(16);

		PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 1000, 20*8);
		SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
		byte[] hash = skf.generateSecret(spec).getEncoded();
		String result = StringTool.toHex(hash);

		return StringTool.toHex(salt) + ";" + result;
	}

	static public boolean checkPassword(@NonNull String encodedPassword, @NonNull String password) {
		String[] split = encodedPassword.split(";");
		if(split.length != 2)
			return false;
		try {
			byte[] salt = StringTool.fromHex(split[0]);
			PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 1000, 20*8);
			SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
			byte[] hash = skf.generateSecret(spec).getEncoded();
			String result = StringTool.toHex(hash);
			return result.equals(split[1]);
		} catch(Exception x) {
			return false;
		}
	}
}
