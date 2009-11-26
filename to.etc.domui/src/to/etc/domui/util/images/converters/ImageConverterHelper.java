package to.etc.domui.util.images.converters;

import java.io.*;
import java.util.*;

/**
 * This handles a converter chain. For every operation in the chain we lookup the appropriate
 * factory, then we execute the conversion. For each next conversion we try to reuse the
 * factory if possible with the output of the previous conversion as base. If this does not
 * work we get another factory.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 7, 2008
 */
public class ImageConverterHelper {
	private List<File> m_workFiles = new ArrayList<File>();

	private ImageSpec m_source, m_target;

	public ImageSpec getSource() {
		return m_source;
	}

	public ImageSpec getTarget() {
		return m_target;
	}

	public void setTarget(ImageSpec target) {
		m_target = target;
	}

	/**
	 * Creates a new tempfile. Files creates by this call will be discarded when the image
	 * has been generated fully (when this helper is destroyed).
	 * @return
	 */
	public File createWorkFile(String ext) throws IOException {
		File tmp = File.createTempFile("imc", "." + ext);
		m_workFiles.add(tmp);
		return tmp;
	}

	public void destroy() {
		for(File wf : m_workFiles) {
			try {
				wf.delete();
			} catch(Exception x) {}
		}
	}

	/**
	 * Loop thru all conversions and convert until the image has been translated proper.
	 * @param src
	 * @param speclist
	 * @throws Exception
	 */
	public void executeConversionChain(ImageSpec src, List<IImageConversionSpecifier> speclist) throws Exception {
		if(speclist == null || speclist.size() == 0) {
			m_target = m_source = src;
			return;
		}

		m_source = src;
		while(speclist.size() > 0) {
			//-- Find the best converter for the first operation(s) in the list,
			IImageConverter ic = ImageConverterRegistry.findBestConverter(src.getMime(), speclist);
			if(ic == null)
				throw new IllegalStateException("Cannot find a converter to convert mime=" + src.getMime() + " using " + speclist);
			int prec = speclist.size();
			m_target = null;
			ic.convert(this, speclist);
			if(speclist.size() == prec)
				throw new IllegalStateException("Converter " + ic + " does not reduce the list-of-conversions after accepting mime=" + src.getMime() + " using " + speclist);
			if(m_target == null)
				throw new IllegalStateException("Converter " + ic + " did not set a conversion result after converting mime=" + src.getMime() + " using " + speclist);

			//-- Make source the new target.
			m_source = m_target;
		}
	}

	public static void main(String[] args) {
		try {
			//-- Execute a single conversion.
			File src = new File("/home/jal/img_5589.jpg");
			ImageData id = ImageConverterRegistry.identify("image/jpeg", src);
			ImageSpec sis = new ImageSpec(src, id);

			List<IImageConversionSpecifier> l = new ArrayList<IImageConversionSpecifier>();
			l.add(new ImagePageSelect(0));
			l.add(new ImageThumbnail(200, 200, "image/png"));

			ImageConverterHelper h = new ImageConverterHelper();
			h.executeConversionChain(sis, l);
			System.out.println("Result: " + h.getTarget().getSource() + ", mime=" + h.getTarget().getMime() + ", pages=" + h.getTarget().getData().getPageCount() + "; p0.size="
				+ h.getTarget().getData().getPage(0).getWidth() + "x" + h.getTarget().getData().getPage(0).getHeight());
		} catch(Exception x) {
			x.printStackTrace();
		}
	}
}
