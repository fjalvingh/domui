package to.etc.domui.util.images.converters;

import java.io.*;
import java.util.*;

public class ImageSpec {
	private File			m_source;
	private ImageData		m_data;

	public ImageSpec() {
	}
	public ImageSpec(File source, ImageData id) {
		m_source = source;
		m_data	= id;
	}
	public ImageSpec(File source, String mime, int w, int h) {
		m_source = source;
		List<ImagePage>	l = new ArrayList<ImagePage>(1);
		l.add(new ImagePage(0, w, h, false));
		m_data = new ImageData(mime, l);
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
	public ImageData getData() {
		return m_data;
	}
}
