package to.etc.domui.util.resources;

import javax.annotation.DefaultNonNull;
import javax.annotation.Nullable;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Contains the inventory for a classpath directory.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 26-10-17.
 */
@DefaultNonNull
public class DirectoryFileContainer implements IFileContainer {
	final private File m_directory;

	final private Map<String, ClasspathFileRef> m_map = new HashMap<>();

	private DirectoryFileContainer(File directory) {
		m_directory = directory;
	}

	@Nullable @Override public ClasspathFileRef findFile(String name) {
		ClasspathFileRef ref = m_map.get(name);
		if(ref != null) {
			return ref;
		}

		File file = new File(m_directory, name);
		if(! file.exists())
			return null;

		ref = new ClasspathFileRef(file);
		m_map.put(name, ref);
		return ref;
	}

	public static DirectoryFileContainer create(File f) {
		return new DirectoryFileContainer(f);
	}

	@Override public List<String> getInventory() {
		List<String> res = new ArrayList<>();
		StringBuilder sb = new StringBuilder();
		inventory(res, m_directory, sb);
		return res;
	}

	private void inventory(List<String> res, File directory, StringBuilder sb) {
		if(! directory.exists() || ! directory.isDirectory())
			return;
		int clen = sb.length();
		for(File file : directory.listFiles()) {
			sb.setLength(clen);
			if(sb.length() > 0)
				sb.append('/');
			sb.append(file.getName());
			if(file.isDirectory()) {
				inventory(res, file, sb);
			} else {
				res.add(sb.toString());
			}
		}

		sb.setLength(clen);
	}
}
