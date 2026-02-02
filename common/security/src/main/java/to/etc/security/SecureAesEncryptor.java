package to.etc.security;

import org.eclipse.jdt.annotation.NonNull;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Secure way to encrypt data, ruthlessly stolen from https://github.com/luke-park/SecureCompatibleEncryptionExamples
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 25-08-22.
 */
final public class SecureAesEncryptor {
	private final static String ALGORITHM_NAME = "AES/GCM/NoPadding";
	private final static int ALGORITHM_NONCE_SIZE = 12;
	private final static int ALGORITHM_TAG_SIZE = 128;
	private final static int ALGORITHM_KEY_SIZE = 128;
	private final static String PBKDF2_NAME = "PBKDF2WithHmacSHA256";
	private final static int PBKDF2_SALT_SIZE = 16;
	private final static int PBKDF2_ITERATIONS = 32767;

	private SecureAesEncryptor() {
		// unused
	}

	public static String encryptStringWithPassword(String plaintext, String password) throws Exception {
		// Generate a 128-bit salt using a CSPRNG.
		byte[] salt = createRandom(PBKDF2_SALT_SIZE);

		// Create an instance of PBKDF2 and derive a key.
		PBEKeySpec pwSpec = new PBEKeySpec(password.toCharArray(), salt, PBKDF2_ITERATIONS, ALGORITHM_KEY_SIZE);
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(PBKDF2_NAME);
		byte[] key = keyFactory.generateSecret(pwSpec).getEncoded();

		// Encrypt and prepend salt.
		byte[] ciphertextAndNonce = encrypt(plaintext.getBytes(StandardCharsets.UTF_8), key);
		byte[] ciphertextAndNonceAndSalt = new byte[salt.length + ciphertextAndNonce.length];
		System.arraycopy(salt, 0, ciphertextAndNonceAndSalt, 0, salt.length);
		System.arraycopy(ciphertextAndNonce, 0, ciphertextAndNonceAndSalt, salt.length, ciphertextAndNonce.length);

		// Return as base64 string.
		return Base64.getEncoder().encodeToString(ciphertextAndNonceAndSalt);
	}

	public static String encryptStringWithKey(String plainText, byte[] passphrase) throws Exception {
		byte[] cipherAndNonce = encrypt(plainText.getBytes(StandardCharsets.UTF_8), passphrase);
		return Base64.getEncoder().encodeToString(cipherAndNonce);
	}

	public static String decryptStringWithPassword(String base64CiphertextAndNonceAndSalt, String password) throws Exception {
		// Decode the base64.
		byte[] ciphertextAndNonceAndSalt = Base64.getDecoder().decode(base64CiphertextAndNonceAndSalt);

		// Retrieve the salt and ciphertextAndNonce.
		byte[] salt = new byte[PBKDF2_SALT_SIZE];
		byte[] ciphertextAndNonce = new byte[ciphertextAndNonceAndSalt.length - PBKDF2_SALT_SIZE];
		System.arraycopy(ciphertextAndNonceAndSalt, 0, salt, 0, salt.length);
		System.arraycopy(ciphertextAndNonceAndSalt, salt.length, ciphertextAndNonce, 0, ciphertextAndNonce.length);

		// Create an instance of PBKDF2 and derive the key.
		PBEKeySpec pwSpec = new PBEKeySpec(password.toCharArray(), salt, PBKDF2_ITERATIONS, ALGORITHM_KEY_SIZE);
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(PBKDF2_NAME);
		byte[] key = keyFactory.generateSecret(pwSpec).getEncoded();

		// Decrypt and return result.
		return new String(decrypt(ciphertextAndNonce, key), StandardCharsets.UTF_8);
	}

	public static String decryptStringWithKey(String base64, byte[] passphrase) throws Exception {
		byte[] encrypted = Base64.getDecoder().decode(base64);
		return new String(decrypt(encrypted, passphrase), StandardCharsets.UTF_8);
	}

	public static byte[] encrypt(byte[] plaintext, byte[] key) throws InvalidKeyException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
		// Generate a 96-bit nonce using a CSPRNG.
		byte[] nonce = createRandom(ALGORITHM_NONCE_SIZE);

		// Create the cipher instance and initialize.
		Cipher cipher = Cipher.getInstance(ALGORITHM_NAME);
		cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"), new GCMParameterSpec(ALGORITHM_TAG_SIZE, nonce));

		// Encrypt and prepend nonce.
		byte[] ciphertext = cipher.doFinal(plaintext);
		byte[] ciphertextAndNonce = new byte[nonce.length + ciphertext.length];
		System.arraycopy(nonce, 0, ciphertextAndNonce, 0, nonce.length);
		System.arraycopy(ciphertext, 0, ciphertextAndNonce, nonce.length, ciphertext.length);
		return ciphertextAndNonce;
	}

	public static byte[] decrypt(byte[] ciphertextAndNonce, byte[] key) throws InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException {
		// Retrieve the nonce and ciphertext.
		byte[] nonce = new byte[ALGORITHM_NONCE_SIZE];
		byte[] ciphertext = new byte[ciphertextAndNonce.length - ALGORITHM_NONCE_SIZE];
		System.arraycopy(ciphertextAndNonce, 0, nonce, 0, nonce.length);
		System.arraycopy(ciphertextAndNonce, nonce.length, ciphertext, 0, ciphertext.length);

		// Create the cipher instance and initialize.
		Cipher cipher = Cipher.getInstance(ALGORITHM_NAME);
		cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"), new GCMParameterSpec(ALGORITHM_TAG_SIZE, nonce));

		// Decrypt and return result.
		return cipher.doFinal(ciphertext);
	}

	@NonNull
	private static byte[] createRandom(int size) {
		byte[] data = new byte[size];
		SecureRandom rand = new SecureRandom();
		rand.nextBytes(data);
		return data;
	}

}
