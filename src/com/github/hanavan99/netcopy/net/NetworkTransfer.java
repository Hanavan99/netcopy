package com.github.hanavan99.netcopy.net;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public abstract class NetworkTransfer {

	public static final int DEFAULT_BUFFER_SIZE = 0xFFFF;
	public static final int PACKET_START = 0;
	public static final int PACKET_FILE = 1;
	public static final int PACKET_PROGRESS = 2;
	public static final int PACKET_END = 3;
	public static final int PACKET_SETTINGS = 4;

	private final Socket socket;
	private File directory;
	private INetworkCallback callback;
	private int bufferSize = 0xFFFF;
	protected boolean ignoreSettingsUpdate = false;

	public NetworkTransfer(Socket socket, File directory, INetworkCallback callback, int bufferSize) {
		this.socket = socket;
		this.directory = directory;
		this.bufferSize = bufferSize;
		this.callback = callback;
	}

	public Socket getSocket() {
		return socket;
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

	protected void transfer(InputStream in, OutputStream out, byte[] buffer, long length) throws IOException {
		while (length > 0) {
			int cnt = in.read(buffer, 0, (int) Math.min(buffer.length, length));
			if (cnt > 0) {
				out.write(buffer, 0, cnt);
				length -= cnt;
			} else {
				continue;
			}
		}
	}

	protected String getRelativePath(File base, File f) {
		return f.getAbsolutePath().substring(base.getAbsolutePath().length() + 1);
	}

	public void sendSettingsChanged(boolean allowClientChanges, boolean createMissingFolders, boolean purgeDirectory, boolean transferNonexistingFiles, boolean transferExistingFiles, int filePreferenceMode) {
		try {
			DataOutputStream dos = new DataOutputStream(getSocket().getOutputStream());
			dos.writeInt(PACKET_SETTINGS);
			dos.writeBoolean(allowClientChanges);
			dos.writeBoolean(createMissingFolders);
			dos.writeBoolean(purgeDirectory);
			dos.writeBoolean(transferNonexistingFiles);
			dos.writeBoolean(transferExistingFiles);
			dos.writeInt(filePreferenceMode);
			ignoreSettingsUpdate = true;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
