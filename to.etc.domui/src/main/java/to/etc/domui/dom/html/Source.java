package to.etc.domui.dom.html;

import to.etc.domui.util.DomUtil;

public class Source extends NodeBase {



    public Source(String src, String type) {
        super("source");
        m_src = src;
        mType = type;
    }

    @Override public void visit(INodeVisitor v) throws Exception {

        v.visitSource(this);

    }
    private String m_src;

    private String mType;

    /**
     * Get the current source as an absolute web app path.
     * @return
     */
    public String getSrc() {
        return m_src;
    }

    /**
     * Set the source as an absolute web app path. If the name is prefixed
     * with THEME/ it specifies an image from the current THEME's directory.
     * @param src
     */
    public void setSrc(String src) {
        if(!DomUtil.isEqual(src, m_src))
            changed();
        m_src = src;
    }

    /**
     * Set the source as a Java resource based off the given class.
     * @param base
     * @param resurl
     */
    public void setSrc(Class<?> base, String resurl) {
        String s = DomUtil.getJavaResourceRURL(base, resurl);
        setSrc(s);
    }


    public String getType() {
        return mType;
    }

    public void setType(String type) {
        if(!DomUtil.isEqual(type, mType))
            changed();

        mType = type;
    }
}
