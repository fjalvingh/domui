package to.etc.formbuilder.pages;

import java.util.*;

import javax.annotation.*;

import to.etc.domui.dom.html.*;

public interface IFbComponent {
	@Nonnull String getTypeID();

	void drawSelector(@Nonnull NodeContainer container) throws Exception;

	@Nonnull String getShortName();

	@Nonnull String getLongName();

	@Nonnull String getCategoryName();

	@Nonnull NodeBase createNodeInstance() throws Exception;

	/**
	 * Return all properties for this component.
	 * @return
	 */
	@Nonnull Set<PropertyDefinition> getProperties();
}
