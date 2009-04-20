package to.etc.domui.components.menu;

import java.util.*;

import to.etc.domui.dom.html.*;
import to.etc.domui.state.*;


public interface IMenuItem {
	String getId();

	String getParentID();

	Class< ? extends UrlPage> getPageClass();

	PageParameters getPageParameters();

	String getIconPath();

	boolean isDisabled();

	List<IMenuItem> getChildren();

	String getSearchString();

	String[] getRequiredRights();

	String getLabel();

	String getDescription();

	int	getOrder();

	boolean	isSubMenu();

	String	getTarget();

	String	getRURL();
}