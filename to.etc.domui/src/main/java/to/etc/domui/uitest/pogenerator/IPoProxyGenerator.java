package to.etc.domui.uitest.pogenerator;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 08-12-21.
 */
@NonNullByDefault
public interface IPoProxyGenerator {
	static public final String PROXYPACKAGE = "to.etc.domui.webdriver.poproxies";

	/**
	 * This allows the generator to see if there is content inside it that
	 * it wants to have generated.
	 */
	boolean acceptChildren(PoGeneratorContext ctx) throws Exception;

	void prepare(PoGeneratorContext context) throws Exception;

	void generateCode(PoGeneratorContext context, PoClass intoClass, String baseName) throws Exception;

	void updateSelector(IPoSelector selector);

	String identifier();
}
