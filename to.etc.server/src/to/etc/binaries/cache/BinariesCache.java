package to.etc.binaries.cache;

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.List;

import javax.media.jai.*;
import javax.sql.*;

import to.etc.binaries.images.*;
import to.etc.dbutil.*;
import to.etc.server.*;
import to.etc.server.cache.*;
import to.etc.server.vfs.*;
import to.etc.sjit.*;
import to.etc.util.*;

/**
 * Implements the Binary cache. This provides the DAO methods to store images/data in
 * the cache, and the code to retrieve the images and image metadata. The image metadata
 * and the image data itself gets cached for better performance. The metadata is small
 * and gets cached in a simple LRU structure. The image data can be big and gets cached
 * in a two-tier model: small images (smaller than MEMFENCE bytes) are cached in memory
 * while bigger ones are cached on the file system. The latter is needed to prevent a
 * a database connection from being needed while transferring data to a browser over a
 * potentially slow connection.
 * This cache does not cache NON-EXISTENCE, i.e. every reference to a nonexisting or
 * unbuildable binary will cause database IO.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Nov 9, 2006
 */
public class BinariesCache {
	static private BinariesCache				m_instance		= new BinariesCache();

	static private Dimension					MAXSIZE			= new Dimension(1024, 1024);

	static private String						PNG				= "image/png";

	/** Everything above this will be cached in a FILE, not in memory. */
	static int									MEMFENCE		= 500 * 1024;

	/** The thingy that does all database IO. */
	BinariesCacheDAO							m_dao;

	DataSource									m_ds;

	/** The map containing all cached info structures. */
	private LRUHashMap<Long, List<BinaryInfo>>	m_infoCache		= new LRUHashMap<Long, List<BinaryInfo>>(1024);

	private List<BinaryConverter>				m_converterList	= new ArrayList<BinaryConverter>();

	private ResourceCache						m_cache			= new ResourceCache(10 * 1024 * 1024, 0, 100 * 1024 * 1024);

	static public void initialize(DataSource ds, BinariesCacheDAO dao) {
		m_instance.init(ds, dao);
	}

	static public BinariesCache getInstance() {
		return m_instance;
	}

	private void init(DataSource ds, BinariesCacheDAO dao) {
		m_ds = ds;
		m_dao = dao;
	}

	public BinariesCache() {
		register(new BinaryImageResizeConverter());
	}

	static int calcSize(byte[][] data) {
		int size = 0;
		for(int i = data.length; --i >= 0;)
			size += data[i].length;
		return size;
	}

	/**
	 * FIXME Needs proper impl.
	 * @return
	 */
	static public File makeTempFile(String ext) throws IOException {
		return File.createTempFile("bincache", ext);
	}

	public synchronized void register(BinaryConverter bc) {
		List<BinaryConverter> list = new ArrayList<BinaryConverter>(m_converterList); // Dup the list initially
		list.add(bc);
		m_converterList = list; // Replace the list.
	}

	/**
	 * Primitive to store a record in the binaries table. Do not use!!!!!
	 *
	 * @param dbc       The connection.
	 * @param mime      The mime type of the stored data. Required.
	 * @param type      The type of the stored data. Required.
	 * @param original  The ID of the original record linked to the new record. Use -1 for the original itself.
	 * @param hash      The hash of the stored data, if already known. If you pass null the hash gets calculated by reading the file.
	 * @param pw        The width of the thing, in pixels; if not applicable pass -1.
	 * @param ph        The height of the thing, in pixels; if not applicable pass -1.
	 * @param data      The file to store. Required.
	 * @return
	 * @throws Exception
	 */
	protected long insertBinary(Connection dbc, String type, long original, String hash, ImageDataSource bds) throws Exception {
		if(hash == null && original == -1) {
			InputStream is = bds.getInputStream();
			try {
				if(is != null) {
					hash = FileTool.hashFileHex(is);
				}
			} finally {
				try {
					if(is != null)
						is.close();
				} catch(Exception x) {}
			}
		}
		return m_dao.insertBinary(dbc, type, original, hash, bds);
	}

	/*--------------------------------------------------------------*/
	/* CODING: Image storage routines.                              */
	/*--------------------------------------------------------------*/

	public long insertImage(Connection dbc, File srcf, String type, String mime, boolean reduce) throws Exception {
		if(mime == null)
			mime = ServerTools.getExtMimeType(FileTool.getFileExtension(srcf.getName()));
		String hash = FileTool.hashFileHex(srcf);
		long id = m_dao.checkExisting(dbc, srcf.length(), type, hash);
		if(id != -1)
			return id;

		//-- We need to load this image. Get image headers for size, then insert;
		long ts = System.nanoTime();
		RenderedImage ri = JAI.create("fileload", srcf.toString());
		int pw = ri.getWidth();
		int ph = ri.getHeight();
		ImageDataSource bds = new FileBinaryDataSource(srcf, mime, pw, ph);
		id = insertBinary(dbc, type, -1, hash, bds);

		//-- If the image is big and reduce is requested then reduce the image to fit in MAXSIZE pixels
		if(reduce && (pw > MAXSIZE.width || ph > MAXSIZE.height)) {
			try {
				Dimension d = ImaTool.resizeWithAspect(MAXSIZE.width, MAXSIZE.height, pw, ph);
				bds = ImageManipulator.scale(srcf, 0, pw, ph, d.width, d.height, PNG);
				ts = System.nanoTime() - ts;
				System.out.println("Scale took " + StringTool.strNanoTime(ts));

				//-- And insert this as a reduce of the original
				insertBinary(dbc, type, id, null, bds);
			} finally {
				bds.discard();
			}
		}
		return id;
	}


	/**
	 * Returns the metrics of an image that would obey the specified requirements. This usually does
	 * not cause the image to be created.
	 *
	 * @param dbcin
	 * @param originalid
	 * @param type
	 * @param mime
	 * @param width
	 * @param height
	 * @return
	 */
	public ImageInfo getImageInfo(Connection dbcin, long originalid, String type, String mime, int width, int height) throws Exception {
		BinaryInfo bi = findBinary(dbcin, Long.valueOf(originalid), type, mime, width, height, true);
		return bi;
	}

	/*--------------------------------------------------------------*/
	/* CODING: Binary descriptor cache                              */
	/*--------------------------------------------------------------*/

	//    private BinaryInfo  findOriginalInfo(Connection dbc, long id) throws Exception {
	//        return findBinary(dbc, id, null,null, -1, -1, false);
	//    }

	/**
	 * Try to find a binary with the specified characteristics. If the thingy does not exist
	 * but there is an original an attempt is made to create the required version. Currently
	 * the only transformation is resizing-to-smaller.
	 *
	 * @param originalid
	 * @param width
	 * @param height
	 * @param mime
	 * @return
	 */
	private BinaryInfo findBinary(Connection dbcin, Long originalid, String type, String mime, int width, int height, boolean create) throws Exception {
		List<BinaryInfo> list;
		synchronized(m_infoCache) {
			list = m_infoCache.get(originalid); // Find by original's key,
			if(list == null) {
				list = new ArrayList<BinaryInfo>(4); // Make a small list
				m_infoCache.put(originalid, list); // And save,
			}
		}

		//-- Second phase: transfer locking to list; then try to locate a match.
		Connection dbc = dbcin;
		try {
			synchronized(list) {
				BinaryInfo bi = findInfo(list, width, height, mime);
				if(bi != null)
					return bi;

				//-- We need to reload the list to make sure nothing was added...
				if(dbc == null)
					dbc = m_ds.getConnection();
				reloadInfo(dbc, list, originalid.longValue());
				bi = findInfo(list, width, height, mime); // Has the load provided us with a match?
				if(bi != null)
					return bi;
				if(list.size() == 0)
					throw new IllegalStateException("The binary with id=" + originalid + " is not found.");
				if(!create)
					return null;

				//-- No match. Can we generate a copy with the requested specs?
				bi = createDerived(dbc, list, type, mime, originalid, width, height);
				dbc.commit();
				return bi;
			}
		} finally {
			try {
				if(dbcin == null && dbc != null)
					dbc.close();
			} catch(Exception x) {}
		}
	}

	/**
	 * Selects all binaries with the same original from the database, and adds all
	 * missing ones to the list. This code ensures that the <i>original</i> is always
	 * list element 0 if it exists.
	 * @param dbc
	 * @param list
	 * @throws Exception
	 */
	private void reloadInfo(Connection dbc, List<BinaryInfo> list, long originalid) throws Exception {
		List<BinaryInfo> nlist = m_dao.reloadInfo(dbc, originalid);
		boolean[] seen = new boolean[list.size()];// What original items have been seen
		for(BinaryInfo bi : nlist) {
			int ix = findIndexByID(list, bi.getId()); // Is this already in the source list?
			if(ix != -1) {
				seen[ix] = true; // Yep-> this entry was seen
			} else {
				//-- New entry. add...
				if(bi.getId() == originalid)
					list.add(0, bi);
				else
					list.add(bi);
			}
		}

		//-- Now remove all records that were no longer seen.
		for(int i = seen.length; --i >= 0;) {
			if(!seen[i])
				list.remove(i);
		}
	}

	static private int findIndexByID(List<BinaryInfo> list, long id) {
		for(int i = list.size(); --i >= 0;) {
			BinaryInfo bi = list.get(i);
			if(bi.getId() == id)
				return i;
		}
		return -1;
	}

	/**
	 * Helper which walks the list to find a matching thingy. Returns null if not found.
	 * @param list
	 * @param w
	 * @param h
	 * @param mime
	 * @return
	 */
	static private BinaryInfo findInfo(List<BinaryInfo> list, int w, int h, String mime) {
		if(list.size() == 0)
			return null;

		//-- If all match criteria are null then we want the original
		if(w == -1 && h == -1 && mime == null) {
			return list.get(0); // Item 0 is original by convention
		}

		//-- Is this a size change?
		if(w != -1 && h != -1) {
			for(BinaryInfo bi : list) {
				if(((bi.getWidth() == w && bi.getHeight() <= h) || (bi.getHeight() == h && bi.getWidth() <= w)) && (mime == null || mime.equalsIgnoreCase(bi.getMime())))
					return bi;
			}
			return null;
		}

		//-- Not a size change; lookup a mime change only.
		if(mime != null) {
			for(BinaryInfo bi : list) {
				if(mime.equalsIgnoreCase(bi.getMime()))
					return bi;
			}
			return null;
		}
		throw new IllegalStateException("Unknown conversion.");
	}


	/*--------------------------------------------------------------*/
	/* CODING: Binary transformations.                              */
	/*--------------------------------------------------------------*/
	/**
	 * Called when a given binary does not yet exist, this checks if there is a factory
	 * that will create a binary with the given specs. If so the factory is asked to
	 * provide the new info structure of the generated copy.
	 *
	 * @param dbc
	 * @param list
	 * @param originalid
	 * @param width
	 * @param height
	 * @param mime
	 * @return
	 */
	private BinaryInfo createDerived(Connection dbc, List<BinaryInfo> list, String type, String mime, Long originalid, int width, int height) throws Exception {
		//-- 1. Find a converter willing to convert the input to the output,
		BinaryInfo bi = findInfo(list, MAXSIZE.width, MAXSIZE.height, "image/png"); // Is a base rescaled image available?
		if(bi == null)
			bi = list.get(0); // No rescaled: use original as base
		BinaryConverter bc = findConverter(bi, type, mime, width, height);
		if(bc == null)
			throw new IllegalStateException("I don't know how to derive type=" + type + ", mime=" + mime + " for source=" + originalid);

		if(bc instanceof TwoStepBinaryConverter) {
			//-- A two-step converter calculates metrics only and leaves the actual generation of the thing to a data retrieval call
			TwoStepBinaryConverter c = (TwoStepBinaryConverter) bc;
			ConverterResult cr = c.calculate(bi, type, mime, width, height);
			bi = new BinaryInfo(-1, originalid.longValue(), cr.getWidth(), cr.getHeight(), -1, mime, type);
			list.add(bi);
			return bi;
		} else {
			//-- Get a source COPY to resize,
			BinaryRef source = getConversionSource(dbc, originalid);
			ImageDataSource cr = bc.generate(source, type, mime, width, height);

			//-- Store the thingy,
			long newkey = insertBinary(dbc, type, originalid.longValue(), null, cr);
			bi = new BinaryInfo(newkey, originalid.longValue(), cr.getWidth(), cr.getHeight(), cr.getSize(), cr.getMime(), type);
			list.add(bi);
			return bi;
		}
	}

	/**
	 * Walks the converter list and finds the first converter that can convert the source in
	 * the specified target.
	 *
	 * @param oribi
	 * @param type
	 * @param mime
	 * @param w
	 * @param h
	 * @return
	 */
	BinaryConverter findConverter(BinaryInfo oribi, String type, String mime, int w, int h) {
		List<BinaryConverter> list;
		synchronized(this) {
			list = m_converterList; // Atomic get. The whole list is replaced when items are added.
		}
		for(BinaryConverter bc : list) {
			if(bc.accepts(oribi, type, mime, w, h))
				return bc;
		}
		return null;
	}

	private CacheObjectFactory	ORIGINAL_FACTORY	= new CacheObjectFactory() {
														public CacheStats makeStatistics() {
															return new CacheStats();
														}

														public Object makeObject(ResourceRef ref, VfsPathResolver vr, Object pk, DependencySet depset, Object p1, Object p2, Object p3)
															throws Exception {
															BinaryInfo bi = (BinaryInfo) pk;
															Connection dbc = m_ds.getConnection();
															PreparedStatement ps = null;
															ResultSet rs = null;
															try {
																if(bi.getId() != -1) {
																	//-- The thing is known to be in the database. Load it.
																	ps = m_dao.findRecord(dbc, bi.getId());
																	rs = ps.executeQuery();
																	if(!rs.next())
																		throw new IllegalStateException("The binary with pk=" + bi.getId() + " is not found in the database.");

																	int size = rs.getInt(1);
																	if(size > MEMFENCE) {
																		//-- Load the data into a tempfile, then return;
																		File temp = makeTempFile("tmp"); // Ask for a cached file.
																		GenericDB.saveBlob(rs, 2, temp); // Save to the file system
																		ref.registerFileSize(size); // Save the #bytes allocated in the FS
																		return new CachedBinary(bi, temp); // And return.
																	}

																	//-- Not too big: load as Buffers.
																	InputStream is = null;
																	try {
																		is = GenericDB.getLobStream(dbc, rs, 2);
																		byte[][] data = FileTool.loadByteBuffers(is);
																		ref.registerMemorySize(size);
																		return new CachedBinary(bi, data);
																	} finally {
																		try {
																			if(is != null)
																				is.close();
																		} catch(Exception x) {}
																	}
																}

																//-- We need to generate the copy.
																//-- First get an uncached copy of the original, then generate the copy;
																BinaryRef br = getConversionSource(dbc, Long.valueOf(bi.getOriginal())); // Get an uncached original

																BinaryConverter bc = findConverter(br.getInfo(), br.getInfo().getType(), bi.getMime(), bi.getWidth(), bi.getHeight());
																ImageDataSource bds = bc.generate(br, br.getInfo().getType(), bi.getMime(), bi.getWidth(), bi.getHeight());
																long newkey = m_dao.insertBinary(dbc, br.getInfo().getType(), bi.getOriginal(), null, bds);
																int size = bds.getSize();
																bi.update(newkey, size);
																if(bds instanceof FileBinaryDataSource) {
																	ref.registerFileSize(size);
																} else {
																	ref.registerMemorySize(size);
																}
																return new CachedBinary(bi, bds.getFile());
															} finally {
																try {
																	if(rs != null)
																		rs.close();
																} catch(Exception x) {}
																try {
																	if(ps != null)
																		ps.close();
																} catch(Exception x) {}
																try {
																	if(dbc != null)
																		dbc.close();
																} catch(Exception x) {}
															}
														}
													};

	public BinaryRef getObject(Long original) throws Exception {
		return getObject(original, null, null, -1, -1);
	}

	public BinaryRef getObject(Long original, String type, String mime, int w, int h) throws Exception {
		BinaryInfo bi = findBinary(null, original, type, mime, w, h, true); // Try to find/create
		if(bi == null)
			throw new IllegalStateException("No such binary with original=" + original);
		ResourceRef ref = m_cache.findResource(null, ORIGINAL_FACTORY, bi, null, null, null);
		return new BinaryRef(ref);
	}

	BinaryRef getConversionSource(Connection dbc, Long id) throws Exception {
		//-- 1. If we have an intermediate source use that, else use the original
		BinaryInfo bi = findBinary(dbc, id, "raster", PNG, MAXSIZE.width, MAXSIZE.height, false);
		if(bi == null || bi.getId() == -1) {
			return getObject(id);
		}
		ResourceRef ref = m_cache.findResource(null, ORIGINAL_FACTORY, bi, null, null, null);
		return new BinaryRef(ref);
	}

	/*--------------------------------------------------------------*/
	/* CODING: Binary transformations.                              */
	/*--------------------------------------------------------------*/

	//    static public void main(String[] s) {
	//        try {
	//            ConnectionPool  pool = PoolManager.getInstance().definePool("vpdemo");
	//            Connection  dbc = pool.getPooledDataSource().getConnection();
	//
	//            File    src = new File("/home/jal/20060623_0591.JPG");
	//
	////            testJAI3(tgt, src, new Dimension(1024, 768));
	////            long pk = insertBinary(dbc, "image/jpeg", "TEST", -1, null, -1, -1, src);
	//
	//            long pk = insertImage(dbc, src, "IMA", "image/jpeg", true);
	//            System.out.println("Inserted pk="+pk);
	//        } catch(Exception x) {
	//            x.printStackTrace();
	//        }
	//    }
}
