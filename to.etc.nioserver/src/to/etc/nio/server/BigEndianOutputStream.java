package to.etc.nio.server;

import java.io.*;
import java.nio.*;
import java.util.*;

public class BigEndianOutputStream extends NioOutputStream {
	BigEndianOutputStream(ConnectionHandler ch) {
		super(ch);
	}

	BigEndianOutputStream(ConnectionHandler ch, List<ByteBuffer> l) {
		super(ch, l);
	}

	@Override
	public void writeInt(int val) throws IOException {
		write((val >> 24) & 0xff);
		write((val >> 16) & 0xff);
		write((val >> 8) & 0xff);
		write(val & 0xff);
	}

	@Override
	public void writeShort(short val) throws IOException {
		write((val >> 8) & 0xff);
		write(val & 0xff);
	}
}
