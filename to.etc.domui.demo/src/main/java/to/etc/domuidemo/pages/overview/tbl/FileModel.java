package to.etc.domuidemo.pages.overview.tbl;

import java.io.*;
import java.util.*;

import to.etc.domui.component.tbl.*;

/**
 * Test model, listing file data.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 1, 2008
 */
class FileModel extends DefaultTableModel<File> {
	public FileModel(List<File> in) {
		super(in);
	}
}