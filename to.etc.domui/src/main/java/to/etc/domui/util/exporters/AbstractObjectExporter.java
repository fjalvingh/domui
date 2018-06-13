package to.etc.domui.util.exporters;

import to.etc.domui.component.meta.impl.ExpandedDisplayProperty;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 28-5-18.
 */
public class AbstractObjectExporter<T> {
	protected List<IExportColumn<?>> convertExpandedToColumn(List<ExpandedDisplayProperty<?>> xProps) {
		return xProps.stream().map(a -> new ExpandedDisplayPropertyColumnWrapper<>(a)).collect(Collectors.toList());
	}
}
