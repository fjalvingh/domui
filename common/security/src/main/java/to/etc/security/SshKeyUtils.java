package to.etc.security;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.pkcs.RSAPrivateKey;
import org.bouncycastle.openssl.PEMDecryptorProvider;
import org.bouncycastle.openssl.PEMEncryptedKeyPair;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.PKCS8Generator;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.openssl.jcajce.JcaPKCS8Generator;
import org.bouncycastle.openssl.jcajce.JceOpenSSLPKCS8DecryptorProviderBuilder;
import org.bouncycastle.openssl.jcajce.JceOpenSSLPKCS8EncryptorBuilder;
import org.bouncycastle.openssl.jcajce.JcePEMDecryptorProviderBuilder;
import org.bouncycastle.operator.InputDecryptorProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.OutputEncryptor;
import org.bouncycastle.pkcs.PKCS8EncryptedPrivateKeyInfo;
import org.bouncycastle.pkcs.PKCSException;
import org.bouncycastle.util.encoders.Base64;
import org.bouncycastle.util.io.pem.PemObject;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.util.ByteArrayUtil;
import to.etc.util.ByteStream;
import to.etc.util.FileTool;
import to.etc.util.LineIterator;
import to.etc.util.StringTool;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 10-2-19.
 */
public class SshKeyUtils {
	public static final String SSH_RSA = "ssh-rsa";

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
	 * <p>
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
					byte[] buf = readIntLenBytes(dis);            // ssh-rsa signature
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

	static public KeySpec decodeSshPublicKeySpec(String text) throws KeyFormatException {
		try {
			// Remove any newlines that can be the result of pasting.
			text = text.replace("\r", "").replace("\n", "");
			String[] split = text.split("\\s+");
			if(split.length < 2)
				throw new KeyFormatException("ssh key format not recognised");
			if(SSH_RSA.equals(split[0])) {
				byte[] data = StringTool.decodeBase64(split[1]);
				try(DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data))) {
					byte[] buf = readIntLenBytes(dis);            // ssh-rsa signature
					if(buf.length != 7 || !Arrays.equals(buf, SSH_RSA.getBytes()))
						throw new KeyFormatException("Expecting byte pattern ssh-rsa");
					BigInteger exp = new BigInteger(readIntLenBytes(dis));
					BigInteger mod = new BigInteger(readIntLenBytes(dis));
					return new RSAPublicKeySpec(mod, exp);
				}
			}
		} catch(Exception x) {
			throw new KeyFormatException(x, "Failed to decode public key");
		}
		throw new KeyFormatException("Key format not recognised");
	}

	private static byte[] readIntLenBytes(DataInputStream dis) throws Exception {
		int l = dis.readInt();                        // length of public exponent
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
			if(key.startsWith("-----BEGIN RSA PRIVATE KEY-----")) {        // PKCS#1
				//-- PEM like format. Strip pem lines
				byte[] pkcs1 = readPemFormat(key);
				byte[] pkcs8 = decodeRsaPKCS1PrivateKey(pkcs1);

				EncodedKeySpec pks = new PKCS8EncodedKeySpec(pkcs8);
				KeyFactory kf = KeyFactory.getInstance("RSA");
				return kf.generatePrivate(pks);
			} else if(key.startsWith("-----BEGIN ENCRYPTED PRIVATE KEY-----")) {

			} else if(key.startsWith("-----BEGIN PRIVATE KEY-----")) {
				//-- PEM like format. Strip pem lines
				byte[] encoded = readPemFormat(key);
				KeyFactory keyFactory = KeyFactory.getInstance("RSA");
				EncodedKeySpec pks = new PKCS8EncodedKeySpec(encoded);
				return keyFactory.generatePrivate(pks);

			}
		} catch(Exception x) {
			throw new KeyFormatException(x, "Bad or unknown format");
		}
		throw new KeyFormatException("Unknown format");
	}

	/*----------------------------------------------------------------------*/
	/*	CODING:	New OpenSSH key format										*/
	/*----------------------------------------------------------------------*/
	private static final String BEGIN = "-----BEGIN ";

	private static final String END = "-----END ";

	private static final byte[] AUTH_MAGIC = "openssh-key-v1\0".getBytes();

	public static final String OPENSSH_PRIVATE_KEY = "OPENSSH PRIVATE KEY-----";

	/**
	 * Decodes the key as an OpenSSH format key.
	 * This is not currently complete due to an extreme
	 * lack of documentation of this badly concocted nonstandard format.
	 * <p>
	 * See https://github.com/hierynomus/sshj/commit/d95b4db930df24bfb1e82882629f8ba811004325
	 */
	@Nullable
	static public PrivateKey decodeOpenSSHPrivateKey(String key) throws Exception {
		List<String> lines = new ArrayList<>();
		for(String s : new LineIterator(key)) {
			if(s.length() > 1)
				lines.add(s);
		}

		if(lines.size() > 2) {
			String header = lines.get(0);
			String trailer = lines.get(lines.size() - 1);
			if(header.equals(BEGIN + OPENSSH_PRIVATE_KEY) && trailer.equals(END + OPENSSH_PRIVATE_KEY)) {
				lines.remove(0);
				lines.remove(lines.size() - 1);
				String base64 = String.join("", lines);
				byte[] data = Base64.decode(base64);

				//-- Must have the openssh magic
				if(ByteArrayUtil.compare(data, 0, AUTH_MAGIC, 0, AUTH_MAGIC.length) != 0) {
					throw new KeyFormatException("OpenSSH key format error: not in openssh-key-v1 format");
				}

				ByteStream bs = new ByteStream(new ByteArrayInputStream(data, AUTH_MAGIC.length, data.length));

				StringBuilder sb = new StringBuilder();
				StringTool.dumpData(sb, data, AUTH_MAGIC.length, data.length);
				System.out.println(sb.toString());

				String cipherName = bs.readIntString(StandardCharsets.US_ASCII);
				String kdfName = bs.readIntString(StandardCharsets.US_ASCII);
				String kdfOptions = bs.readIntString(StandardCharsets.US_ASCII);
				int nrKeys = bs.readInt();
				if(nrKeys != 1)
					throw new KeyFormatException("Unsupported OpenSSH format: only one key per file supported (keys=" + nrKeys + ")");

				byte[] pubkeypart = bs.readIntBytes();
				System.out.println("--- Public part");
				dump(pubkeypart);

				byte[] privKeyPart = bs.readIntBytes();
				System.out.println("--- Private part");
				dump(privKeyPart);

				if("none".equals(cipherName)) {
					openSshReadUnencrypted(privKeyPart, pubkeypart);
				}

				return null;

			}
		}
		throw new KeyFormatException("OpenSSH key format error");
	}

	private static void openSshReadUnencrypted(byte[] privKeyPart, byte[] mainPubkeyPart) throws Exception {
		if(privKeyPart.length % 8 != 0)
			throw new KeyFormatException("OpenSSH format private key size incorrect");
		ByteStream pvs = new ByteStream(privKeyPart);
		int checkInt1 = pvs.readInt();
		int checkInt2 = pvs.readInt();
		if(checkInt1 != checkInt2)
			throw new KeyFormatException("OpenSSH format private key check values incorrect");
		String keyType = pvs.readIntString(StandardCharsets.US_ASCII);

		if("ssh-rsa".equals(keyType)) {

			byte[] pubKey = pvs.readIntBytes();
			int unused = pvs.readInt();                                // Skip a number
			int unusedbyte1 = pvs.read();
			int unusedbyte2 = pvs.read();
			int unusedbyte3 = pvs.read();
			byte[] string1 = pvs.readIntBytes();
			byte[] string2 = pvs.readIntBytes();
			byte[] string3 = pvs.readIntBytes();
			byte[] string4 = pvs.readIntBytes();

			String comment = pvs.readIntString(StandardCharsets.US_ASCII);

		} else {
			throw new KeyFormatException("OpenSSH Unrecognized key type: " + keyType);
		}
		byte[] padding = pvs.readBytes(pvs.available());

		for(int i = 0; i < padding.length; i++) {
			if((int) padding[i] != i + 1) {
				throw new IOException("Padding of key format contained wrong byte at position: " + i);
			}
		}
	}

	static private void dump(byte[] data) throws Exception {
		StringBuilder sb = new StringBuilder();
		StringTool.dumpData(sb, data, 0, data.length);
		System.out.println(sb.toString());
	}

	/**
	 * Change the unencrypted PKCS#1 RSA private key format to PKCS#8.
	 * <p>
	 * See
	 * https://stackoverflow.com/questions/45646808/convert-an-rsa-pkcs1-private-key-string-to-a-java-privatekey-object
	 * https://stackoverflow.com/questions/23709898/java-convert-dkim-private-key-from-rsa-to-der-for-javamail
	 */
	static private byte[] decodeRsaPKCS1PrivateKey(byte[] oldder) {
		final byte[] prefix = {0x30, (byte) 0x82, 0, 0, 2, 1, 0, // SEQUENCE(lenTBD) and version INTEGER
			0x30, 0x0d, 6, 9, 0x2a, (byte) 0x86, 0x48, (byte) 0x86, (byte) 0xf7, 0x0d, 1, 1, 1, 5, 0, // AlgID for rsaEncryption,NULL
			4, (byte) 0x82, 0, 0}; // OCTETSTRING(lenTBD)
		byte[] newder = new byte[prefix.length + oldder.length];
		System.arraycopy(prefix, 0, newder, 0, prefix.length);
		System.arraycopy(oldder, 0, newder, prefix.length, oldder.length);
		// and patch the (variable) lengths to be correct
		int len = oldder.length, loc = prefix.length - 2;
		newder[loc] = (byte) (len >> 8);
		newder[loc + 1] = (byte) len;
		len = newder.length - 4;
		loc = 2;
		newder[loc] = (byte) (len >> 8);
		newder[loc + 1] = (byte) len;

		return newder;
	}

	static private final byte[] readPemFormat(String in) {
		StringBuilder sb = new StringBuilder();
		for(String line : new LineIterator(in)) {
			if(!line.startsWith("--") && !line.isEmpty()) {
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
		return encodePemObject(pem);
	}

	private static String encodePemObject(PemObject pem) throws IOException {
		StringWriter sw = new StringWriter();
		try(JcaPEMWriter pw = new JcaPEMWriter(sw)) {
			pw.writeObject(pem);
		}
		return sw.toString();
	}

	static public String encodeSshPrivateKey(PrivateKey key, String passphrase) throws Exception {
		JceOpenSSLPKCS8EncryptorBuilder encryptorBuilder = new JceOpenSSLPKCS8EncryptorBuilder(PKCS8Generator.PBE_SHA1_3DES);
		encryptorBuilder.setRandom(new SecureRandom());
		encryptorBuilder.setPassword(passphrase.toCharArray());
		encryptorBuilder.setIterationCount(10000);
		OutputEncryptor oe = encryptorBuilder.build();
		JcaPKCS8Generator gen = new JcaPKCS8Generator(key, oe);
		PemObject pem = gen.generate();
		return encodePemObject(pem);
	}

	/**
	 * Read many forms of SSH private keys and return them as a PrivateKey.
	 */
	static public KeyPair decodeSshPrivateKeyPair(String key, @Nullable String passPhrase) throws Exception {
		try(PEMParser pp = new PEMParser(new StringReader(key))) {
			Object pem = pp.readObject();

			JcaPEMKeyConverter converter = new JcaPEMKeyConverter(); //.setProvider("BC");

			if(pem instanceof PEMEncryptedKeyPair) {
				if(null == passPhrase)
					throw new KeyFormatPasswordException("Missing password for private key");
				PEMEncryptedKeyPair pekp = (PEMEncryptedKeyPair) pem;
				PEMDecryptorProvider decryptor = new JcePEMDecryptorProviderBuilder().build(passPhrase.toCharArray());
				PEMKeyPair pair = pekp.decryptKeyPair(decryptor);
				return converter.getKeyPair(pair);
			}

			if(pem instanceof PKCS8EncryptedPrivateKeyInfo) {
				if(null == passPhrase)
					throw new KeyFormatPasswordException("Missing password for private key");
				try {
					/*
					 * Decode this trainwreck, and assume this is an RSA type key 8-(
					 */
					PKCS8EncryptedPrivateKeyInfo info = (PKCS8EncryptedPrivateKeyInfo) pem;
					InputDecryptorProvider decryptor = new JceOpenSSLPKCS8DecryptorProviderBuilder().build(passPhrase.toCharArray());
					PrivateKeyInfo pki = info.decryptPrivateKeyInfo(decryptor);

					PublicKey pubKey = calculateRsaPublicKeyFromPrivate(pki);
					return new KeyPair(pubKey, converter.getPrivateKey(pki));
				} catch(PKCSException | OperatorCreationException x) {
					//-- Password was incorrect
					throw new KeyFormatPasswordException("Incorrect password for PKCS#8 encrypted private key");
				}
			}

			if(pem instanceof PrivateKeyInfo) {
				PrivateKeyInfo info = (PrivateKeyInfo) pem;
				PublicKey pubKey = calculateRsaPublicKeyFromPrivate(info);
				return new KeyPair(pubKey, converter.getPrivateKey(info));
			}

			if(pem instanceof PEMKeyPair) {
				KeyPair keyPair = converter.getKeyPair((PEMKeyPair) pem);
				return keyPair;
			}
		}

		throw new KeyFormatException("Unsupported key format");
	}

	private static PublicKey calculateRsaPublicKeyFromPrivate(PrivateKeyInfo pki) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
		ASN1Encodable asn1private = pki.parsePrivateKey();
		RSAPrivateKey rsaKey = RSAPrivateKey.getInstance(asn1private);
		RSAPublicKeySpec spec = new RSAPublicKeySpec(rsaKey.getModulus(), rsaKey.getPublicExponent());
		KeyFactory fact = KeyFactory.getInstance("RSA");
		PublicKey pubKey = fact.generatePublic(spec);
		return pubKey;
	}

	static public void main(String[] args) throws Exception {
		String key = FileTool.readFileAsString(new File("/home/jal/test_id_rsa"));
		decodeSshPrivateKeyPair(key, "WXWtMbOH^ka2IKH*J0ipUiDLpUQkwotf   ");
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

		@Override
		public BigInteger getPublicExponent() {
			return m_publicExp;
		}

		@Override
		public String getAlgorithm() {
			return m_algo;
		}

		@Override
		public String getFormat() {
			return m_format;
		}

		@Override
		public byte[] getEncoded() {
			return m_encoded;
		}

		@Override
		public BigInteger getModulus() {
			return m_modulo;
		}
	}

	public static KeyPair generateKeyPair() throws Exception {
		KeyPairGenerator kg = KeyPairGenerator.getInstance("RSA");
		//SecureRandom random = SecureRandom.getInstanceStrong();
		SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
		kg.initialize(2048, random);
		KeyPair pair = kg.generateKeyPair();
		return pair;
	}

}
