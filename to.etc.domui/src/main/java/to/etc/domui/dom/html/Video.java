package to.etc.domui.dom.html;

public class Video extends NodeContainer {


    private final Format mFormat;
    public enum Format{
        MP4
    }

    public Video(String src,Format format) {
        super("video");
        mFormat = format;
        add(  new Source(src + "." + format.name().toLowerCase(),"video/" + format.name().toLowerCase()) );
    }

    @Override
    public void visit(INodeVisitor v) throws Exception {
        v.visitVideo(this);
    }


}
