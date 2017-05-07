/*
 * DomUI Java User Interface library
 * Copyright (c) 2010 by Frits Jalvingh, Itris B.V.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * See the "sponsors" file for a list of supporters.
 *
 * The latest version of DomUI and related code, support and documentation
 * can be found at http://www.domui.org/
 * The contact for the project is Frits Jalvingh <jal@etc.to>.
 */
package to.etc.domui.component.dynaima;

import java.awt.image.*;
import java.io.*;

import to.etc.domui.dom.html.*;
import to.etc.domui.parts.*;
import to.etc.domui.state.*;
import to.etc.sjit.*;
import to.etc.util.*;

/**
 * UNSTABLE INTERFACE
 * Dynamic image component. This is an img tag containing server-generated
 * content.
 *
 * <h2>Internals</h2>
 * <p>This component uses the concept of an "image source" as the mechanism to send server-side generated
 * bitmap images to the client. We accept multiple source types as the source for an image. The most basic
 * kind is the IStreamingImageSource which represents a thing which can create the image when needed. To
 * prevent excessive (re) generation of the image we cache the resulting stream locally, in this component.
 * </p>
 *
 * <h2>Change management</h2>
 * <p>This component does not use a model and as such it is unable to determine that the image has changed.
 * To force an update on the image as shown in the browser you MUST call one of the source setters AGAIN with
 * the source to use. This may be the same source as last time; the calling again will cause the image to be
 * reloaded and regenerated from the server.</p>
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 2, 2008
 */
public class DynaIma extends Img {
	private IStreamingImageSource m_streamingSource;

	private IBufferedImageSource m_bufferedSource;

	private BufferedImage m_bufferedImage;

	private String m_cachedMime;

	private int m_cachedSize;

	private byte[][] m_cachedData;

	/** The image tag; part of the URL used to retrieve the image. Every time the image changes we need to increment this tag to force the browser to reload the image. */
	private long m_changeTag = System.nanoTime();

	public synchronized IStreamingImageSource getStreamingSource() {
		return m_streamingSource;
	}

	public synchronized void setStreamingSource(IStreamingImageSource streamingSource) {
		m_streamingSource = streamingSource;
		m_bufferedSource = null;
		m_bufferedImage = null;
		updateURL();
	}

	public synchronized IBufferedImageSource getBufferedSource() {
		return m_bufferedSource;
	}

	public synchronized void setBufferedSource(IBufferedImageSource bufferedSource) {
		m_bufferedImage = null;
		m_bufferedSource = bufferedSource;
		m_streamingSource = null;
		updateURL();
	}

	public synchronized void setBufferedImage(BufferedImage bufferedImage, String outputMime) {
		m_bufferedImage = bufferedImage;
		m_bufferedSource = null;
		m_streamingSource = null;
		m_cachedMime = outputMime;
		updateURL();
	}

	/**
	 * Called whenever a source changes. This merely changes the tag, and sets the image's "src" property to a new string containing
	 * that tag. This forces the component to be CHANGED (being part of the delta) which in turn causes the browser to reload this
	 * image.
	 */
	private synchronized void updateURL() {
		m_cachedData = null; // Discard any old data
		StringBuilder sb = new StringBuilder(256);
		ComponentPartRenderer.appendComponentURL(sb, DynaImaPart.class, this, UIContext.getRequestContext());
		sb.append("?tag=");
		sb.append(++m_changeTag);
		setSrc(sb.toString());
	}

	synchronized String getCachedMime() {
		return m_cachedMime;
	}

	//	synchronized void setCachedMime(String cachedMime) {
	//		m_cachedMime = cachedMime;
	//	}

	int getCachedSize() {
		return m_cachedSize;
	}

	void setCachedSize(int cachedSize) {
		m_cachedSize = cachedSize;
	}

	synchronized byte[][] getCachedData() {
		return m_cachedData;
	}

	synchronized void setCachedData(byte[][] cachedData) {
		m_cachedData = cachedData;
	}

	/**
	 * Called when a normal dynamic image's datastream is to be generated somehow.
	 * @throws Exception
	 */
	synchronized void initializeCached() throws Exception {
		if(m_cachedData != null)
			return;

		//-- If we have a BufferedImage provider use it,
		if(m_bufferedSource != null) {
			m_bufferedImage = m_bufferedSource.getImage();
			m_cachedMime = m_bufferedSource.getMimeType();
		}
		if(m_bufferedImage != null) {
			//-- Encode the buffered image to the specified mime format.
			String mime = m_cachedMime;
			if(mime == null || mime.trim().length() == 0)
				mime = "image/jpeg";
			ByteBufferOutputStream bbos = new ByteBufferOutputStream(32768);
			ImaTool.saveImageByMime(bbos, m_bufferedImage, mime); // Copy to byte buffers
			bbos.close();
			m_cachedData = bbos.getBuffers();
			return;
		}

		//-- 1. If we have a streaming thingerydoo use that;
		if(m_streamingSource != null) {
			m_cachedMime = m_streamingSource.getMimeType();
			m_cachedSize = m_streamingSource.getSize();
			InputStream is = m_streamingSource.getInputStream();
			try {
				m_cachedData = FileTool.loadByteBuffers(is);
			} finally {
				try {
					if(is != null)
						is.close();
				} catch(Exception x) {}
			}
			return;
		}

		//-- DUNNO HOW TO MAKE IMAGE
		m_cachedData = null;
	}
}
