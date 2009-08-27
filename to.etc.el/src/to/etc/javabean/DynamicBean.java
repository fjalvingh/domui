package to.etc.javabean;

public interface DynamicBean {
	public Object getDynamicProperty(String name);

	public void setDynamicProperty(String name, Object val);
}
