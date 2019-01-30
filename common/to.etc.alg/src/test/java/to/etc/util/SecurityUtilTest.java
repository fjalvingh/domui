package to.etc.util;

import org.junit.Assert;
import org.junit.Test;

import java.security.PublicKey;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 30-1-19.
 */
public class SecurityUtilTest {
	static private final String PUB_SSH = "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAAAgQClOhbxnMhwQkmLFzFaZ2ZCK3ar4PkKhldMiBvKMMBy8Sw+pOZ8Z9XMIaq+AzBO+Dja0Jt1wzyfdmCRDHdHAjHfyT8YSYKXyF/0FyLc8zeMTOYAGPdZvli+l9wlfG9Ne5QwZrcU/uXFMAE8z9EqRwMneah8hZXmh08i/c+RCzYMxw== jal@pigalle";


	@Test
	public void testReadSshPk() throws Exception {
		PublicKey publicKey = SecurityUtils.decodeSshPublicKey(PUB_SSH);
		String s = SecurityUtils.encodeSshPublicKey(publicKey, "jal@pigalle");
		Assert.assertEquals(PUB_SSH, s);
	}
}
