package to.etc.domui.component.input;

import javax.annotation.*;

import to.etc.domui.dom.html.*;

/**
 * Marks select provider control.
 * 
 *
 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
 * Created on Nov 7, 2012
 */
public interface ISelectProvider {
	@Nonnull
	Select getSelectControl() throws Exception;
}
