package to.etc.launcher.misc;

import javax.annotation.*;

import org.w3c.dom.*;

public class XmlHelper {
	public static @Nullable
	Node locateDirectChild(@Nonnull Element element, @Nonnull String tagName) {
		NodeList codes = element.getChildNodes();
		for(int index = 0; index < codes.getLength(); index++) {
			if(tagName.equals(codes.item(index).getNodeName())) {
				return codes.item(index);
			}
		}
		return null;
	}

}
