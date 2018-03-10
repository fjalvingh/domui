package to.etc.util;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 3-8-16.
 */
final public class NoUtf8Exception extends RuntimeException {
    public NoUtf8Exception(int offset, int linenr) {
        super("Not UTF=8 format at byte offset " + offset + " (line " + linenr + ")");
    }
}
