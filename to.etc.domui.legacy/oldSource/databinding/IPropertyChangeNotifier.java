package to.etc.domui.databinding;

import org.eclipse.jdt.annotation.*;

public interface IPropertyChangeNotifier {
	public <V> void notifyIfChanged(@Nullable V old, @Nullable V value);
}
