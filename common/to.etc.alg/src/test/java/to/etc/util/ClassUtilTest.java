package to.etc.util;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 29-04-20.
 */
public class ClassUtilTest {
	/**
	 * Three level deep generics resolution.
	 */
	@Test
	public void testGeneric1() throws Exception {
		var res = ClassUtil.findGenericGetterType(ClassUtilTestC1.class, ClassUtilTestC1.class.getMethod("getValue"));
		Assert.assertEquals(String.class, res);
	}

	/**
	 * Non-generic resolution in root class.
	 */
	@Test
	public void testGeneric2() throws Exception {
		var res = ClassUtil.findGenericGetterType(ClassUtilTestA2.class, ClassUtilTestA2.class.getMethod("getValue"));
		Assert.assertEquals(String.class, res);
	}



}
