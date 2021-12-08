package to.etc.domui.uitest.pogenerator;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 08-12-21.
 */
public interface IPoProxyGenerator {
	/**
	 * This allows the generator to see if there is content inside it that
	 * it wants to have generated.
	 */
	void acceptChildren() throws Exception;

	void generate(PoGeneratorContext context) throws Exception;

	void prepare(PoGeneratorContext context) throws Exception;

	void generateCode(PoGeneratorContext context) throws Exception;
}
