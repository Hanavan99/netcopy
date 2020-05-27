package com.github.hanavan99.netcopy;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

@Deprecated
public class DebugOutputStream extends OutputStream {

	private final DataOutputStream out;

	public DebugOutputStream(DataOutputStream out) {
		this.out = out;
	}

	public void writeUTF(String s) throws IOException {
		System.out.println("Writing UTF: " + s);
		out.writeUTF(s);
	}

	public void writeInt(int i) throws IOException {
		System.out.println("Writing int: " + i);
		out.writeInt(i);
	}

	public void writeLong(long l) throws IOException {
		System.out.println("Writing long: " + l);
		out.writeLong(l);
	}

	public void writeBoolean(boolean b) throws IOException {
		System.out.println("Writing boolean: " + b);
		out.writeBoolean(b);
	}

	@Override
	public void write(int b) throws IOException {
		System.out.println("Writing byte: " + b);
		out.write(b);
	}

}
