package to.etc.javabean;

public class BeanUtil {
	static public Object getSimpleProperty(Object bean, String prop) throws Exception {
		if(bean == null)
			throw new IllegalStateException("Bean cannot be null");
		BeanPropertyDescriptor bpd = BeanEvaluator.findProperty(bean.getClass(), prop);
		if(bpd == null)
			throw new IllegalStateException("The property '" + prop + "' was not found in bean class=" + bean.getClass().getName());
		return bpd.callGetter(bean);
	}
}
