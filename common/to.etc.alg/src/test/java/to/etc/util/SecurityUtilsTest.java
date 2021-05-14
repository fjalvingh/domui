package to.etc.util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(Parameterized.class)
public class SecurityUtilsTest {

	private static int MAX = 10;

	@Parameters()
	public static Collection getData() {
		return IntStream.rangeClosed(1, MAX).boxed().collect(Collectors.toList());
	}

	public SecurityUtilsTest(Integer index) { }

	@Test
	public void testEncryptDecryptString() throws Exception {
		String initial = "this is some text";
		String password = StringTool.toHex(SecurityUtils.createSalt(50)).substring(0, 32);
		String encrypted = SecurityUtils.encryptString(initial, password);
		//System.out.println(password);
		//System.out.println(encrypted);
		String decrypted = SecurityUtils.decryptString(encrypted, password);
		assertThat(decrypted, is(initial));
	}

	@Test
	public void testEncryptDecryptStringAes() throws Exception {
		String initial = "this is some text";
		String password = StringTool.toHex(SecurityUtils.createSalt(50)).substring(0, 32);
		String encrypted = SecurityUtils.encryptStringAes(initial, password);
		//System.out.println(password);
		//System.out.println(encrypted);
		String decrypted = SecurityUtils.decryptStringAes(encrypted, password);
		assertThat(decrypted, is(initial));
	}

	@Test
	public void testEncryptDecryptStringAesHex() throws Exception {
		String initial = "this is some text";
		String password = StringTool.toHex(SecurityUtils.createSalt(50)).substring(0, 32);
		String encrypted = SecurityUtils.encryptStringAesHex(initial, password);
		//System.out.println(password);
		//System.out.println(encrypted);
		String decrypted = SecurityUtils.decryptStringAesHex(encrypted, password);
		assertThat(decrypted, is(initial));
	}
}
