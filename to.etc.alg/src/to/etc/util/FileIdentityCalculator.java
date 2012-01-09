package to.etc.util;

import java.io.*;
import java.security.*;


/**
 *	<p>When a file is delivered and it's name cannot be used as a way to
 *  establish the identity of the picture, i.e. you cannot use file
 *  names to check whether files are the same, we need something else..
 *	What we could use is the following mechanism.</p>
 *
 * 	<p>Before an file gets read you can request a unique "checksum"
 *  of it's contents called a digest. This is a fixed-size hash value
 *  calculated by traversing the contents of the file. The combination of
 *  (file size, hash value) will be used to identify a file. This
 *  function takes a filename and sets the global fields for the size and
 *  the hash value.
 *  </p>
 *  <p>The same instance of this class can be used over and over again to
 *  calculate digests and sizes.
 *  </p>
 *
 * <p>Title: Mumble Global Libraries - Non-database tools</p>
 * <p>Description: Small tools for Java programs</p>
 * <p>Copyright: Copyright (c) 2002 Frits Jalvingh; released under the LGPL licence.</p>
 * <p>Website <a href="http://www.mumble.to/">Mumble</a></p>
 * @author <a href="mailto:jal@mumble.to">Frits Jalvingh</a>
 * @version 1.0
 */
public class FileIdentityCalculator {
	/// The last calculated MD5 hash for an image file.
	protected byte[]	m_pict_identityhash;

	/// The last file's size.
	protected long		m_pict_filesize;

	public FileIdentityCalculator() {
	}

	/**
	 *	<p>When an image file is delivered it's name cannot be used as a way to
	 *  establish the identity of the picture, i.e. you cannot use image file
	 *  names to check whether images are the same. What we use is the following
	 *  mechanism.</p>
	 *
	 * 	<p>Before an image file gets read you can request a unique "checksum"
	 *  of it's contents called a digest. This is a fixed-size hash value
	 *  calculated by traversing the contents of the file. The combination of
	 *  (file size, hash value) will be used to identify an image. This
	 *  function takes a filename and sets the global fields for the size and
	 *  the hash value.
	 *  </p>
	 */
	public void getFileIdentity(File f) throws IOException {
		InputStream is = null;
		try {
			//-- Open the file, then start reading
			is = new FileInputStream(f);
			getFileIdentity(is);
		} finally {
			try {
				if(is != null)
					is.close();
			} catch(Exception x) {}
		}
	}


	/**
	 *	See the description of getFileIdentity(File f) for a description,
	 *  this uses an inputstream to calculate.
	 */
	public void getFileIdentity(InputStream is) throws IOException {
		m_pict_filesize = 0;
		m_pict_identityhash = null;
		MessageDigest md;

		try {
			md = MessageDigest.getInstance("MD5");
		} catch(NoSuchAlgorithmException x) {
			throw new Error("MISSING MANDATORY SECURITY DIGEST PROVIDER MD5: " + x.getMessage());
		}
		byte[] buf = new byte[8192];

		for(;;) {
			int szrd = is.read(buf); // Read as much as fits
			if(szrd <= 0)
				break; // At end of file-> bedone!
			m_pict_filesize += szrd;

			if(szrd == buf.length) // Update the hash value,
				md.update(buf);
			else
				md.update(buf, 0, szrd);
		}

		//-- The hash is known now!
		m_pict_identityhash = md.digest();
		//		System.out.println("Hash value has "+digest.length+" byters.");
		//		System.out.println("File size is "+m_pict_filesize+" byters.");
	}


	/**
	 *	Returns the digest as a byte array (16 bytes).
	 */
	public byte[] getDigestBinary() {
		if(m_pict_identityhash == null)
			throw new IllegalStateException("No file hashed yet");
		return m_pict_identityhash;
	}

	/**
	 *	Returns the size of the file.
	 */
	public long getFileSize() {
		if(m_pict_identityhash == null)
			throw new IllegalStateException("No file hashed yet");
		return m_pict_filesize;
	}


	/**
	 *	Returns the complete identity as a string, using hex encoding of
	 *  the bytes. The size is also added as a hex string.
	 */
	public String getFullDigest() {
		if(m_pict_identityhash == null)
			throw new IllegalStateException("No file hashed yet");
		StringBuffer sb = new StringBuffer(41); // max is 32 + 8 + 1

		sb.append(Long.toHexString(m_pict_filesize));
		sb.append(":");
		for(int i = 0; i < m_pict_identityhash.length; i++) {
			int val = m_pict_identityhash[i] & 0xff;
			sb.append(Integer.toHexString(val));
		}
		return sb.toString();
	}


	public static void main(String[] args) {
		//-- Callek a diggest.
		FileIdentityCalculator ph = new FileIdentityCalculator();
		try {
			ph.getFileIdentity(new File("d:/java/vfo/t.jpg"));
		} catch(Exception x) {
			System.out.println("EXCEPTION: " + x.toString());
			x.printStackTrace();
		}


	}

}
