package to.etc.template;

/**
 * A context for a template.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Nov 27, 2010
 */
public interface IJSTemplateContext {
	void write(String text) throws Exception;

	void writeValue(Object v) throws Exception;
}
