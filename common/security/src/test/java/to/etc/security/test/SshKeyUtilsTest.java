package to.etc.security.test;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import to.etc.security.KeyFormatPasswordException;
import to.etc.security.SshKeyUtils;
import to.etc.util.FileTool;

import java.security.KeyPair;
import java.security.PrivateKey;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 11-01-23.
 */
public class SshKeyUtilsTest {
	@Test
	public void readPKCS8EncryptedCorrectly() throws Exception {
		String key = FileTool.readResourceAsString(getClass(), "/sshkeys/pkcs8_encrypted_id", "utf-8");
		KeyPair privateKey = SshKeyUtils.decodeSshPrivateKeyPair(key, "WXWtMbOH^ka2IKH*J0ipUiDLpUQkwotf");
		Assert.assertNotNull("Must have a private key", privateKey);
	}

	@Test(expected = KeyFormatPasswordException.class)
	public void readPKCS8EncryptedBadPass() throws Exception {
		String key = FileTool.readResourceAsString(getClass(), "/sshkeys/pkcs8_encrypted_id", "utf-8");
		KeyPair privateKey = SshKeyUtils.decodeSshPrivateKeyPair(key, "WXWtMbOH^ka2IKH*J0ipUiDLpUQkwotf   ");
	}

	@Test
	public void readRSAUnencryptedCorrectly() throws Exception {
		String key = FileTool.readResourceAsString(getClass(), "/sshkeys/unencrypted_id_rsa", "utf-8");
		KeyPair privateKey = SshKeyUtils.decodeSshPrivateKeyPair(key, null);
		Assert.assertNotNull("Must have a private key", privateKey);
	}

	@Ignore("The implementation is currently incomplete")
	@Test
	public void readOpensshRSAUnencryptedCorrectly() throws Exception {
		String key = FileTool.readResourceAsString(getClass(), "/sshkeys/openssh_unencryped_id_rsa", "utf-8");
		KeyPair privateKey = SshKeyUtils.decodeSshPrivateKeyPair(key, null);
		Assert.assertNotNull("Must have a private key", privateKey);
	}

	@Ignore("The implementation is currently incomplete")
	@Test
	public void readOpensshRSAUnencryptedCorrectly2() throws Exception {
		String key = FileTool.readResourceAsString(getClass(), "/sshkeys/openssh_unencryped_id_rsa", "utf-8");
		PrivateKey privateKey = SshKeyUtils.decodeOpenSSHPrivateKey(key);
		Assert.assertNotNull("Must have a private key", privateKey);
	}



}
