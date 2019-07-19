package to.etc.domui.dom.html;

import to.etc.domui.util.DomUtil;

public class Source extends NodeBase {
    private String m_src;

    private String m_type;


    public Source(String src, String type) {
        super("source");
        m_src = src;
        m_type = type;
    }

    @Override public void visit(INodeVisitor v) throws Exception {
        v.visitSource(this);

    }

    /**
     * Get the current source as an absolute web app path.
     */
    public String getSrc() {
        return m_src;
    }

    /**
     * Set the source as an absolute web app path. If the name is prefixed
     * with THEME/ it specifies an image from the current THEME's directory.
     */
    public void setSrc(String src) {
        if(!DomUtil.isEqual(src, m_src))
            changed();
        m_src = src;
    }

    /**
     * Set the source as a Java resource based off the given class.
     */
    public void setSrc(Class<?> base, String resurl) {
        String s = DomUtil.getJavaResourceRURL(base, resurl);
        setSrc(s);
    }


    public String getType() {
        return m_type;
    }

    public void setType(String type) {
        if(!DomUtil.isEqual(type, m_type))
            changed();

        m_type = type;
    }
}
