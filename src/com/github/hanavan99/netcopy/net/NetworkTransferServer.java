package com.github.hanavan99.netcopy.net;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;

public class NetworkTransferServer extends NetworkTransfer {

	private final Thread receiveThread;

	public NetworkTransferServer(Socket socket, File directory, int bufferSize, INetworkCallback callback) {
		super(socket, directory, callback, bufferSize);

		receiveThread = new Thread(() -> {
			try {
				// create streams
				DataInputStream dis = new DataInputStream(socket.getInputStream());

				// continue running
				while (true) {
					switch (dis.readInt()) {
					case PACKET_SETTINGS:
						boolean allowClientChanges = dis.readBoolean();
						boolean createMissingFolders = dis.readBoolean();
						boolean purgeDirectory = dis.readBoolean();
						boolean transferNonexistingFiles = dis.readBoolean();
						boolean transferExistingFiles = dis.readBoolean();
						int filePreferenceMode = dis.readInt();
						callback.settingsChanged(allowClientChanges, createMissingFolders, purgeDirectory, transferNonexistingFiles, transferExistingFiles, filePreferenceMode);
						break;
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
		receiveThread.start();
	}

	public void transfer() throws IOException {
		// create buffer and streams
		byte[] buffer = new byte[getBufferSize()];
		DataInputStream dis = new DataInputStream(getSocket().getInputStream());
		DataOutputStream dos = new DataOutputStream(getSocket().getOutputStream());
		INetworkCallback callback = getCallback();

		// count number of files
		int maxFile = getFileCount(getDirectory());

		// alert client we are starting
		callback.transferStarted();
		dos.writeInt(PACKET_START);

		// transfer files
		callback.messageLogged("transferring " + maxFile + " files...\n");
		transfer(getDirectory(), buffer, dis, dos, new AtomicInteger(0), maxFile);
		callback.messageLogged("done.");

		// alert client we are done
		dos.writeInt(PACKET_END);
		callback.transferEnded();
	}

	private int getFileCount(File directory) {
		int count = 0;
		for (File f : directory.listFiles()) {
			if (f.isDirectory()) {
				count += getFileCount(f);
			} else if (f.isFile()) {
				count++;
			}
		}
		return count;
	}

	private void transfer(File directory, byte[] buffer, DataInputStream dis, DataOutputStream dos, AtomicInteger curFile, int maxFile) throws IOException {
		// get callback
		INetworkCallback callback = getCallback();

		// iterate through files list
		for (File f : directory.listFiles()) {
			// determine file type
			if (f.isDirectory()) {
				// go through files in this folder
				transfer(f, buffer, dis, dos, curFile, maxFile);
			} else if (f.isFile()) {
				// tell client about file
				dos.writeInt(PACKET_FILE);
				String relativePath = getRelativePath(getDirectory(), f);
				long lastModified = f.lastModified();
				long length = f.length();
				dos.writeUTF(relativePath);
				dos.writeLong(lastModified);
				dos.writeLong(length);
				callback.messageLogged("sending file \"" + f.getAbsolutePath() + "\"... ");

				// see if client wants file
				if (dis.readBoolean()) {
					// attempt to send file
					try {
						FileInputStream fis = new FileInputStream(f);
						dos.writeBoolean(true);
						transfer(fis, dos, buffer, length);
						fis.close();
						callback.messageLogged("done.\n");
					} catch (IOException e) {
						e.printStackTrace();
						dos.writeBoolean(false);
						callback.messageLogged("failed: " + e.getMessage() + "\n");
					}
				} else {
					callback.messageLogged("skipped.\n");
				}
				// update progress
				curFile.incrementAndGet();
				callback.progressUpdated(curFile.get(), maxFile);
				dos.writeInt(PACKET_PROGRESS);
				dos.writeInt(curFile.get());
				dos.writeInt(maxFile);
			} else {
				throw new IllegalStateException("hit something not a file or directory");
			}
		}
	}

}
