package to.etc.domui.databinding;

import javax.annotation.*;

public interface IPropertyChangeNotifier {
	public <V> void notifyIfChanged(@Nullable V old, @Nullable V value);
}
