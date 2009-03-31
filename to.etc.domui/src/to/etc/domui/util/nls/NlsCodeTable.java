package to.etc.domui.util.nls;

/**
 * Represents a code table with localized translations for several
 * languages. This gets created for every single code table, and
 * is an accessor to the cached translated strings.
 * 
 * FIXME Implement, please.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 12, 2006
 */
public class NlsCodeTable {
    private String      m_name;

    NlsCodeTable(String name) {
        m_name = name;
    }
    public String getName() {
        return m_name;
    }

    public String   getLabel(String code, String def) {
        return def;
    }
    public String   getDescription(String code, String def) {
        return def;
    }
}
