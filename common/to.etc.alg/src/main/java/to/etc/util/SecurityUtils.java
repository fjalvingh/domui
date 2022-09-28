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

import org.eclipse.jdt.annotation.NonNull;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.EncodedKeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Apr 6, 2004
 */
public class SecurityUtils {

	static private final SecureRandom RANDOM = new SecureRandom();

	public enum Algorithm {
		Blowfish,
		AES
	}

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
	 *
	 * @param enc the hex string.
	 * @return a public key.
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
	 *
	 * @param enc the hex string.
	 * @return a public key.
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
	 *
	 * @param enc the hex string.
	 * @return a public key.
	 */
	static public PublicKey decodePublicKeyFromHex(String enc) throws Exception {
		return decodePublicKeyFromHex(enc, "DSA");
	}

	/**
	 * Decodes a private key from an encoded value.
	 *
	 * @param enc the hex string.
	 * @return a public key.
	 */
	static public PrivateKey decodePrivateKeyFromHex(String enc) throws Exception {
		return decodePrivateKeyFromHex(enc, "DSA");
	}

	/**
	 * Decodes a public key from an encoded value.
	 *
	 * @param enc the hex string.
	 * @return a public key.
	 */
	static public PublicKey decodePublicKeyFromBase64(String enc) throws Exception {
		return decodePublicKeyFromBase64(enc, "DSA");
	}

	/**
	 * Decodes a private key from an encoded value.
	 *
	 * @param enc the hex string.
	 * @return a public key.
	 */
	static public PrivateKey decodePrivateKeyFromBase64(String enc) throws Exception {
		return decodePrivateKeyFromBase64(enc, "DSA");
	}

	/**
	 * Decodes a public key from an encoded value.
	 *
	 * @param enc the hex string.
	 * @return a public key.
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
	 *
	 * @param enc the hex string.
	 * @return a public key.
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

	/**
	 * Returns the hash string generated based on md5 encoded as bas 36 string -> produces hash of 25 length, lowercase.
	 */
	static public String getMd5HashBase36(@NonNull String in) {
		try {
			byte[] md5Hash = md5Hash(in.getBytes("UTF-8"));
			String hexStr = StringTool.toHex(md5Hash);
			return new BigInteger(hexStr, 16).toString(36).toLowerCase();
		} catch(Exception x) {
			throw WrappedException.wrap(x);
		}
	}

	static public String convertHexToBase36(@NonNull String hexStr) {
		try {
			return new BigInteger(hexStr, 16).toString(36).toLowerCase();
		} catch(Exception x) {
			throw WrappedException.wrap(x);
		}
	}

	@NonNull
	static public String getSha256Hex(@NonNull String in, @NonNull Charset encoding) {
		try {
			MessageDigest sha = MessageDigest.getInstance("SHA-256");
			byte[] hash = sha.digest(in.getBytes(encoding));
			return StringTool.toHex(hash);
		} catch(Exception ex) {
			throw WrappedException.wrap(ex);
		}
	}

	static public byte[] createSalt(int bytes) {
		byte[] salt = new byte[bytes];
		RANDOM.nextBytes(salt);
		return salt;
	}

	/**
	 * Generate a secure password hash by randomly generating a salt, then
	 * hashing the password using PBKDF2.
	 */
	static public String encryptPassword(String password) throws Exception {
		byte[] salt = createSalt(16);

		PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 1000, 20 * 8);
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
			PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 1000, 20 * 8);
			SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
			byte[] hash = skf.generateSecret(spec).getEncoded();
			String result = StringTool.toHex(hash);
			return result.equals(split[1]);
		} catch(Exception x) {
			return false;
		}
	}

	static private byte[] encryptStringToBytes(String input, String password, Algorithm algorithm) throws Exception {
		SecretKeySpec skeyspec = new SecretKeySpec(password.getBytes(), algorithm.name());
		Cipher cipher = Cipher.getInstance(algorithm.name());
		cipher.init(Cipher.ENCRYPT_MODE, skeyspec);
		return cipher.doFinal(input.getBytes(StandardCharsets.UTF_8));
	}

	static private String decryptStringFromBytes(byte[] data, String password, Algorithm algorithm) throws Exception {
		SecretKeySpec skeyspec = new SecretKeySpec(password.getBytes(), algorithm.name());
		Cipher cipher = Cipher.getInstance(algorithm.name());
		cipher.init(Cipher.DECRYPT_MODE, skeyspec);
		byte[] decrypted = cipher.doFinal(data);
		return new String(decrypted, StandardCharsets.UTF_8);
	}

	static public String encryptString(String input, String password) throws Exception {
		return encryptStringBase64(input, password, Algorithm.Blowfish);
	}

	static public String decryptString(String input, String password) throws Exception {
		return decryptStringBase64(input, password, Algorithm.Blowfish);
	}

	static public String encryptStringBase64(String input, String password, Algorithm alg) throws Exception {
		byte[] bytes = encryptStringToBytes(input, password, alg);
		return StringTool.encodeBase64ToString(bytes);
	}

	static public String decryptStringBase64(String input, String password, Algorithm alg) throws Exception {
		byte[] bytes = StringTool.decodeBase64(input);
		return decryptStringFromBytes(bytes, password, alg);
	}

	static public String encryptStringHex(String input, String password, Algorithm alg) throws Exception {
		byte[] bytes = encryptStringToBytes(input, password, alg);
		return StringTool.toHex(bytes);
	}

	static public String decryptStringHex(String input, String password, Algorithm alg) throws Exception {
		byte[] bytes = StringTool.fromHex(input);
		return decryptStringFromBytes(bytes, password, alg);
	}

}
