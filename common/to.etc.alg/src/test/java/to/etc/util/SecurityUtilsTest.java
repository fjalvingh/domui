package to.etc.util;

import org.apache.commons.lang3.RandomUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import to.etc.util.SecurityUtils.Algorithm;

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
	public void testEncryptDecryptStringBlowfish() throws Exception {
		String initial = "this is some text";
		String password = randomPassword();
		String encrypted = SecurityUtils.encryptStringBase64(initial, password, Algorithm.Blowfish);
		//System.out.println(password);
		//System.out.println(encrypted);
		String decrypted = SecurityUtils.decryptStringBase64(encrypted, password, Algorithm.Blowfish);
		assertThat(decrypted, is(initial));
	}

	@Test
	public void testEncryptDecryptStringBlowfishHex() throws Exception {
		String initial = "this is some text";
		String password = randomPassword();
		String encrypted = SecurityUtils.encryptStringHex(initial, password, Algorithm.Blowfish);
		//System.out.println(password);
		//System.out.println(encrypted);
		String decrypted = SecurityUtils.decryptStringHex(encrypted, password, Algorithm.Blowfish);
		assertThat(decrypted, is(initial));
	}

	@Test
	public void testEncryptDecryptStringAes() throws Exception {
		String initial = "this is some text";
		String password =  randomPassword();
		String encrypted = SecurityUtils.encryptStringBase64(initial, password, Algorithm.AES);
		//System.out.println(password);
		//System.out.println(encrypted);
		String decrypted = SecurityUtils.decryptStringBase64(encrypted, password, Algorithm.AES);
		assertThat(decrypted, is(initial));
	}

	@Test
	public void testEncryptDecryptStringAesHex() throws Exception {
		String initial = "this is some text";
		String password = randomPassword();
		String encrypted = SecurityUtils.encryptStringHex(initial, password, Algorithm.AES);
		//System.out.println(password);
		//System.out.println(encrypted);
		String decrypted = SecurityUtils.decryptStringHex(encrypted, password, Algorithm.AES);
		assertThat(decrypted, is(initial));
	}

	private String randomPassword() {
		byte[] bytes = RandomUtils.nextBytes(16);
		return StringTool.toHex(bytes);
	}
}
