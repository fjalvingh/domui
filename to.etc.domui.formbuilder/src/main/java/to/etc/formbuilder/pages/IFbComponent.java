package to.etc.formbuilder.pages;

import org.eclipse.jdt.annotation.NonNull;
import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.dom.html.NodeContainer;

import java.util.Set;

public interface IFbComponent {
	@NonNull String getTypeID();

	void drawSelector(@NonNull NodeContainer container) throws Exception;

	@NonNull String getShortName();

	@NonNull String getLongName();

	@NonNull String getCategoryName();

	@NonNull NodeBase createNodeInstance() throws Exception;

	/**
	 * Return all properties for this component.
	 * @return
	 */
	@NonNull Set<PropertyDefinition> getProperties();
}
