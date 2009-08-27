package to.etc.server.syslogger;

/**
 * 
 *
 * @author jal
 * Created on Jan 21, 2005
 */
public interface LogDataConverter {
	public boolean accepts(Object o);

	public void convert(StringBuffer sb, Object o);
}
