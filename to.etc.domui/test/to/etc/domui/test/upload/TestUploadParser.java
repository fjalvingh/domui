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
package to.etc.domui.test.upload;

import java.io.*;
import java.util.*;

import org.junit.*;

import to.etc.domui.util.upload.MultipartStream.MalformedStreamException;
import to.etc.domui.util.upload.*;

public class TestUploadParser {
	@Test
	public void testGood() throws Exception {
		InputStream is = getClass().getResourceAsStream("good.bin");
		try {
			UploadParser up = new UploadParser();
			List<UploadItem> res = up.parseRequest(is, "utf-8", "multipart/form-data; boundary=---------------------------761455922829130801673802772", 999);
			for(UploadItem it : res) {
				System.out.println("item: " + it.getName());
			}
			Assert.assertEquals(2, res.size());
		} finally {
			try {
				is.close();
			} catch(Exception x) {}
		}
	}

	@Test(expected = MalformedStreamException.class)
	public void testBad() throws Exception {
		InputStream is = getClass().getResourceAsStream("bad.bin");
		try {
			UploadParser up = new UploadParser();
			up.parseRequest(is, "utf-8", "multipart/form-data; boundary=--boun-da-ry-0xababaeaGfHdNarcolethe-mumble-to-content-eNCoDer-gxixmar-rennes-le-chateau124a098aa8eetc", 999);
		} finally {
			try {
				is.close();
			} catch(Exception x) {}
		}
	}
}
