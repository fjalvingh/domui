package to.etc.domui.dom;

import java.io.*;

public class FastXmlOutputWriter extends XmlOutputWriterBase implements IBrowserOutput {
	public FastXmlOutputWriter(Writer w) {
		super(w);
	}

	public void setIndentEnabled(boolean ind) {}
}
