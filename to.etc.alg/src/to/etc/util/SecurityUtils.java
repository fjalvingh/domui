package to.etc.util;

import java.security.*;
import java.security.spec.*;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Apr 6, 2004
 */
public class SecurityUtils {
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
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("MD5");
		} catch(NoSuchAlgorithmException x) {
			throw new RuntimeException("MISSING MANDATORY SECURITY DIGEST PROVIDER MD5: " + x.getMessage());
		}
		md.update(data);
		return md.digest();
	}


}
