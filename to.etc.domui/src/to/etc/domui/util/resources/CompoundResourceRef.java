package to.etc.domui.util.resources;

import java.io.*;

import javax.annotation.*;

import to.etc.domui.server.*;
import to.etc.domui.trouble.*;
import to.etc.util.*;

public class CompoundResourceRef implements IResourceRef, IModifyableResource {
	private byte[][] m_buffers;

	private CompoundResourceRef(@Nonnull byte[][] buffers) {
		super();
		m_buffers = buffers;
	}

	@Override
	public long getLastModified() {
		return 0;
	}

	@Override
	public boolean exists() {
		return true;
	}

	@Override
	@Nullable
	public InputStream getInputStream() throws Exception {
		return new ByteBufferInputStream(m_buffers);
	}

	/**
	 * Load the spec, and create the ref from it.
	 * @param da
	 * @param inclset
	 * @return
	 */
	public static IResourceRef loadBySpec(@Nonnull DomApplication da, @Nonnull String baseDir, @Nonnull String inclset, @Nonnull String origname) throws Exception {
		ByteBufferOutputStream	bos = new ByteBufferOutputStream();

		for(String line : new LineIterator(inclset)) {
			line = line.trim();
			if(line.length() == 0 || line.startsWith("#"))
				continue;
			if(line.startsWith("include")) {
				load(da, bos, baseDir, line.substring(7).trim(), origname);
			}
		}

		//-- Create the result
		bos.close();
		return new CompoundResourceRef(bos.getBuffers());
	}

	private static void load(@Nonnull DomApplication da, @Nonnull ByteBufferOutputStream bos, @Nonnull String baseDir, @Nonnull String filename, String origname) throws Exception {
		if(!filename.startsWith("/"))
			filename = baseDir + filename;
		IResourceRef resource = da.getAppFileOrResource(filename);
		if(! resource.exists())
			throw new ThingyNotFoundException("Cannot find include file " + filename + " referenced in " + origname);
		InputStream is = null;
		try {
			is = resource.getInputStream();
			FileTool.copyFile(bos, is);
		} finally {
			FileTool.closeAll(is);
		}

	}


}
