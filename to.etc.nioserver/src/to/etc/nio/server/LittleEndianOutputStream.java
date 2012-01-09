package to.etc.nio.server;

import java.io.*;
import java.nio.*;
import java.util.*;

public class LittleEndianOutputStream extends NioOutputStream {
	LittleEndianOutputStream(ConnectionHandler ch) {
		super(ch);
	}

	LittleEndianOutputStream(ConnectionHandler ch, List<ByteBuffer> l) {
		super(ch, l);
	}

	@Override
	public void writeInt(int val) throws IOException {
		write(val & 0xff);
		write((val >> 8) & 0xff);
		write((val >> 16) & 0xff);
		write((val >> 24) & 0xff);
	}

	@Override
	public void writeShort(short val) throws IOException {
		write(val & 0xff);
		write((val >> 8) & 0xff);
	}
}
