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
	//private HashMap<Integer, FileInputStream> fileMap = new HashMap<Integer, FileInputStream>();
	private final Object fileLock = new Object();
	private boolean fileAccepted;

	public NetworkTransferServer(Socket socket, File directory, int bufferSize, INetworkCallback callback) throws IOException {
		super(socket, directory, callback, bufferSize);

		receiveThread = new Thread(() -> {
			try {
				// continue running
				while (true) {
					switch (in.readInt()) {
					case PACKET_SETTINGS:
						boolean allowClientChanges = in.readBoolean();
						boolean createMissingFolders = in.readBoolean();
						boolean purgeDirectory = in.readBoolean();
						boolean transferNonexistingFiles = in.readBoolean();
						boolean transferExistingFiles = in.readBoolean();
						int filePreferenceMode = in.readInt();
						if (!ignoreSettingsUpdate) {
							callback.settingsChanged(allowClientChanges, createMissingFolders, purgeDirectory, transferNonexistingFiles, transferExistingFiles, filePreferenceMode);
						}
						ignoreSettingsUpdate = false;
						break;
					case PACKET_FILE_ACCEPT:
						fileAccepted = true;
						synchronized (fileLock) {
							fileLock.notify();
						}
						break;
					case PACKET_FILE_REJECT:
						fileAccepted = false;
						synchronized (fileLock) {
							fileLock.notify();
						}
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
		INetworkCallback callback = getCallback();

		// count number of files
		int fileCount = getFileCount(getDirectory());

		// alert client we are starting
		callback.transferStarted();
		callback.progressUpdated(0, fileCount);
		writeStartPacket(fileCount);

		// transfer files
		try {
			callback.messageLogged("transferring " + fileCount + " files...\n");
			transfer(getDirectory(), buffer, in, out, new AtomicInteger(0), fileCount);
			callback.messageLogged("done.");
		} catch (InterruptedException e) {
			e.printStackTrace();
			callback.messageLogged("\nProcess was interrupted!\n");
		}

		// alert client we are done
		writeEndPacket();
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

	private void transfer(File directory, byte[] buffer, DataInputStream dis, DataOutputStream dos, AtomicInteger curFile, int maxFile) throws IOException, InterruptedException {
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
				String relativePath = getRelativePath(getDirectory(), f);
				long lastModified = f.lastModified();
				long length = f.length();
				writeFileProposePacket(relativePath, lastModified, length);
				callback.messageLogged("sending file \"" + f.getAbsolutePath() + "\"... ");

				// wait for response
				synchronized (fileLock) {
					fileLock.wait();
				}

				// see if client wants file
				if (fileAccepted) {
					
					// attempt to send file
					try {
						FileInputStream fis = new FileInputStream(f);
						dos.writeBoolean(true);
						dos.flush();
						transfer(fis, dos, buffer, length, callback);
						fis.close();
						callback.messageLogged("done.\n");
					} catch (IOException e) {
						e.printStackTrace();
						dos.writeBoolean(false);
						dos.flush();
						callback.messageLogged("failed: " + e.getMessage() + "\n");
					}
				} else {
					callback.messageLogged("skipped.\n");
				}
				// update progress
				curFile.incrementAndGet();
				callback.progressUpdated(curFile.get(), maxFile);
			} else {
				throw new IllegalStateException("hit something not a file or directory");
			}
		}
	}

}
