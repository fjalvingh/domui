package to.etc.csv;

/**
 *
 * Created on Oct 13, 2003
 * @author jal
 */
public interface iInputField {
	public void setName(String s);

	public boolean isEmpty();

	public String getValue();

	public String getName();
}
