package com.github.hanavan99.netcopy.net;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public abstract class NetworkTransfer {

	/**
	 * Default buffer size for file transfers.
	 */
	public static final int DEFAULT_BUFFER_SIZE = 0x100000;

	/**
	 * Packet ID for packet sent when transfer has started.
	 */
	public static final int PACKET_START = 0;

	/**
	 * Packet ID for packet sent when settings have changed on the client or server.
	 */
	public static final int PACKET_SETTINGS = 1;

	/**
	 * Packet ID for packet sent for proposing a file for the client to receive.
	 */
	public static final int PACKET_FILE_PROPOSE = 2;

	/**
	 * Packet ID for packet sent when the client accepts a file.
	 */
	public static final int PACKET_FILE_ACCEPT = 3;

	/**
	 * Packet ID for packet sent when the client rejects a file.
	 */
	public static final int PACKET_FILE_REJECT = 4;

	/**
	 * Packet ID for packet sent when the transfer has finished.
	 */
	public static final int PACKET_END = 5;

	protected final Socket socket;
	protected final DataInputStream in;
	protected final DataOutputStream out;
	private File directory;
	private INetworkCallback callback;
	private int bufferSize = 0xFFFF;
	protected boolean ignoreSettingsUpdate = false;

	public NetworkTransfer(Socket socket, File directory, INetworkCallback callback, int bufferSize) throws IOException {
		this.socket = socket;
		this.in = new DataInputStream(socket.getInputStream());
		this.out = new DataOutputStream(socket.getOutputStream());
		this.directory = directory;
		this.bufferSize = bufferSize;
		this.callback = callback;
	}

	public File getDirectory() {
		return directory;
	}

	public INetworkCallback getCallback() {
		return callback;
	}

	public void setDirectory(File directory) {
		this.directory = directory;
	}

	public int getBufferSize() {
		return bufferSize;
	}

	public void setBufferSize(int bufferSize) {
		this.bufferSize = bufferSize;
	}

	public boolean isIgnoreSettingsUpdate() {
		return ignoreSettingsUpdate;
	}

	protected void transfer(InputStream in, OutputStream out, byte[] buffer, long length, INetworkCallback callback) throws IOException {
		final long fileLength = length;
		while (length > 0) {
			int cnt = in.read(buffer, 0, (int) Math.min(buffer.length, length));
			if (cnt > 0) {
				out.write(buffer, 0, cnt);
				length -= cnt;
				if (callback != null) {
					callback.fileProgressUpdated(fileLength - length, fileLength);
				}
			} else {
				continue;
			}
		}
	}

	protected String getRelativePath(File base, File f) {
		return f.getAbsolutePath().substring(base.getAbsolutePath().length() + 1);
	}

	public void writeStartPacket(int fileCount) throws IOException {
		out.writeInt(PACKET_START);
		out.writeInt(fileCount);
	}

	public void writeFileAcceptPacket() throws IOException {
		out.writeInt(PACKET_FILE_ACCEPT);
	}

	public void writeFileRejectPacket() throws IOException {
		out.writeInt(PACKET_FILE_REJECT);
	}

	public void writeFileProposePacket(String relativePath, long lastModified, long length) throws IOException {
		out.writeInt(PACKET_FILE_PROPOSE);
		out.writeUTF(relativePath);
		out.writeLong(lastModified);
		out.writeLong(length);
	}

	public void writeEndPacket() throws IOException {
		out.writeInt(PACKET_END);
	}

	public void writeSettingsPacket(boolean allowClientChanges, boolean createMissingFolders, boolean purgeDirectory, boolean transferNonexistingFiles, boolean transferExistingFiles, int filePreferenceMode) throws IOException {
		out.writeInt(PACKET_SETTINGS);
		out.writeBoolean(allowClientChanges);
		out.writeBoolean(createMissingFolders);
		out.writeBoolean(purgeDirectory);
		out.writeBoolean(transferNonexistingFiles);
		out.writeBoolean(transferExistingFiles);
		out.writeInt(filePreferenceMode);
		ignoreSettingsUpdate = true;
	}

	public void close() throws IOException {
		socket.close();
	}

}
