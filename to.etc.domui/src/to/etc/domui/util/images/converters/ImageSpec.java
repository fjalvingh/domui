package to.etc.domui.util.images.converters;

import java.io.*;
import java.util.*;

import to.etc.domui.util.images.machines.*;

public class ImageSpec {
	private File m_source;

	private ImageInfo m_data;

	public ImageSpec() {}

	public ImageSpec(File source, ImageInfo id) {
		m_source = source;
		m_data = id;
	}

	public ImageSpec(File source, String mime, int w, int h) {
		m_source = source;
		List<OriginalImagePage> l = new ArrayList<OriginalImagePage>(1);
		l.add(new OriginalImagePage(0, w, h, mime, null, false));
		m_data = new ImageInfo(mime, null, true, l);
	}

	public String getMime() {
		return m_data.getMime();
	}

	public File getSource() {
		return m_source;
	}

	public void setSource(File source) {
		m_source = source;
	}

	public ImageInfo getInfo() {
		return m_data;
	}
}
