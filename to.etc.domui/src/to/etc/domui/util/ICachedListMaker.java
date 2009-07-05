package to.etc.domui.util;

public interface ICachedListMaker<T> extends IListMaker<T> {
	String getCacheKey();
}
