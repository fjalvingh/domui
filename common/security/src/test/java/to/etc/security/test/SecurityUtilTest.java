package to.etc.security.test;

import org.junit.Assert;
import org.junit.Test;
import to.etc.security.SshKeyUtils;
import to.etc.util.SecurityUtils;

import java.security.PrivateKey;
import java.security.PublicKey;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 30-1-19.
 */
public class SecurityUtilTest {
	static private final String PUB_SSH = "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAAAgQClOhbxnMhwQkmLFzFaZ2ZCK3ar4PkKhldMiBvKMMBy8Sw+pOZ8Z9XMIaq+AzBO+Dja0Jt1wzyfdmCRDHdHAjHfyT8YSYKXyF/0FyLc8zeMTOYAGPdZvli+l9wlfG9Ne5QwZrcU/uXFMAE8z9EqRwMneah8hZXmh08i/c+RCzYMxw== jal@pigalle";

	static private final String PRIV_SSH = "-----BEGIN RSA PRIVATE KEY-----\n"
		+ "MIICWwIBAAKBgQClOhbxnMhwQkmLFzFaZ2ZCK3ar4PkKhldMiBvKMMBy8Sw+pOZ8\n"
		+ "Z9XMIaq+AzBO+Dja0Jt1wzyfdmCRDHdHAjHfyT8YSYKXyF/0FyLc8zeMTOYAGPdZ\n"
		+ "vli+l9wlfG9Ne5QwZrcU/uXFMAE8z9EqRwMneah8hZXmh08i/c+RCzYMxwIDAQAB\n"
		+ "AoGBAJNTscOs/hkDMlqAyrQGwOq9oKpwBwB4e301XDo0sFWNcNtG5HIHkF7dokad\n"
		+ "x0STFvcdzZD7DqJNxptvlyfM8DRhkpw8tTqA/utBxvgFxoFQwcb25Ia6L7ktrjNQ\n"
		+ "BjxIf8kXZaiX0904wPEWTAF+u043uniROBP1x6hX1Eu9p3+RAkEAzjhgJ0Skkp6C\n"
		+ "/+sy3Yv7A/hfgYF0fqKNYr78I6ooHrBRhZQQTEdNJ1DXdnBpezOSKJc7gqX6Lzy8\n"
		+ "Gi6Q9rkcWQJBAM0ce92q4bmsIewmIQUNMoOhX0eqr4YwE8VtsYtewuEq64WG4f7S\n"
		+ "H103MYGAS7HKNVDjn1TGtl5180qOm3Zczh8CQBM4Nd3zC9OOam8noIn9bduk3mHX\n"
		+ "it/yjnLRkfZQ+YRCspZcglhZnNs5MZucRnhxCgI2dhlrFrIoWu+lv2T/Q6ECP0W0\n"
		+ "aGPsrslqthyK2K3ezkvti3PPjdcMf6uYm73BdnkPHPtD24m93urO1wQrlY3WKkhT\n"
		+ "EtK8tT1k2El+LVcMMwJAdSFRm4eYUgFs4Zm0FxS6HbEx7g0LEbC72LdQsR1LEssf\n"
		+ "BYQchNI8bDMBdGscgqds9mt7Bpa3at0KuyMG2Cjd0g==\n"
		+ "-----END RSA PRIVATE KEY-----\n";

	@Test
	public void testReadSshPk() throws Exception {
		PublicKey publicKey = SshKeyUtils.decodeSshPublicKey(PUB_SSH);
		String s = SshKeyUtils.encodeSshPublicKey(publicKey, "jal@pigalle");
		Assert.assertEquals(PUB_SSH, s);
	}

	@Test
	public void testReadSshPriv() throws Exception {
		PrivateKey privKey = SshKeyUtils.decodeSshPrivateKey(PRIV_SSH);
		//String s = SecurityUtils.encodeSshPublicKey(publicKey, "jal@pigalle");
		//Assert.assertEquals(PUB_SSH, s);
	}

	@Test
	public void testGetMd5HashBase36() {
		assertThat(SecurityUtils.getMd5HashBase36("bla bla bla"), is("ad75l5xoxr9samzdjjg5l4may"));
	}

	@Test
	public void testConvertHexToBase36() {
		assertThat(SecurityUtils.convertHexToBase36("67799e3c8745c1f89678a99b2f6f04be"), is("64j9gzm117qpdopa4x12iflj2"));
	}
}
