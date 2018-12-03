package to.etc.dbcompare.generator;

import org.eclipse.jdt.annotation.NonNull;
import to.etc.dbutil.schema.*;

public interface TypeMapping {
	void renderType(@NonNull StringBuilder sb, @NonNull DbColumn c);
}
