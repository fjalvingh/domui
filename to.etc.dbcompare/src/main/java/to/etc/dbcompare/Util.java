package to.etc.dbcompare;

public class Util {
	static public boolean equalObjects(Object a, Object b) {
		if(a == b)
			return true;
		if(a == null || b == null)
			return false;
		return a.equals(b);
	}
}
