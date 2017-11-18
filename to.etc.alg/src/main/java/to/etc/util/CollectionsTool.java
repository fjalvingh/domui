package to.etc.util;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 15-11-17.
 */
final public class CollectionsTool {
	private CollectionsTool() {
	}


	static public <T> List<T> join(List<T>... parts) {
		if(parts.length == 1)
			return parts[0];
		List<T> res = new ArrayList<>();
		for(int i = 0; i < parts.length; i++) {
			res.addAll(parts[i]);
		}
		return res;
	}

}
