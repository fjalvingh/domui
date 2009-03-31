package to.etc.domui.util;

import java.util.*;

import to.etc.domui.server.*;

public interface IListMaker<T> {
	public List<T>		createList(DomApplication a) throws Exception;
}
