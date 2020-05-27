package com.github.hanavan99.netcopy;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

@Deprecated
public class DebugInputStream extends InputStream {

	private final DataInputStream in;

	public DebugInputStream(DataInputStream in) {
		this.in = in;
	}

	public String readUTF() throws IOException {
		System.out.print("Reading UTF: ");
		String s = in.readUTF();
		System.out.println(s);
		return s;
	}

	public int readInt() throws IOException {
		System.out.print("Reading int: ");
		int i = in.readInt();
		System.out.println(i);
		return i;
	}

	public long readLong() throws IOException {
		System.out.print("Reading long: ");
		long l = in.readLong();
		System.out.println(l);
		return l;
	}

	public boolean readBoolean() throws IOException {
		System.out.print("Reading boolean: ");
		boolean b = in.readBoolean();
		System.out.println(b);
		return b;
	}

	@Override
	public int read() throws IOException {
		System.out.print("Reading byte: ");
		int b = in.read();
		System.out.println(b);
		return b;
	}

}
