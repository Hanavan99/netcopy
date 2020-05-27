package com.github.hanavan99.netcopy.net;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;

public class NetworkTransferClient extends NetworkTransfer {

	private final Thread receiveThread;
	private boolean allowClientChanges;
	private boolean createMissingFolders;
	private boolean purgeDirectory;
	private boolean transferNonexistingFiles;
	private boolean transferExistingFiles;
	private int filePreferenceMode;
	private int fileCount;
	private int curFile;

	public NetworkTransferClient(Socket socket, File directory, int bufferSize, INetworkCallback callback) throws IOException {
		super(socket, directory, callback, bufferSize);

		receiveThread = new Thread(() -> {
			try {
				// create buffer and streams
				byte[] buffer = new byte[getBufferSize()];
				DataInputStream dis = new DataInputStream(socket.getInputStream());
				DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

				// continue running
				while (true) {
					switch (dis.readInt()) {
					case PACKET_START:
						fileCount = in.readInt();
						curFile = 0;
						callback.transferStarted();
						callback.progressUpdated(curFile, fileCount);
						callback.messageLogged("beginning transfer of " + fileCount + " files...\n");
						if (purgeDirectory) {
							callback.messageLogged("deleting parent directory...\n");
							deleteDirectory(directory);
							callback.messageLogged("done.\n");
						}
						break;
					case PACKET_END:
						callback.messageLogged("done.");
						callback.transferEnded();
						break;
					case PACKET_FILE_PROPOSE:
						String relativePath = dis.readUTF();
						long lastModified = dis.readLong();
						long length = dis.readLong();
						File file = new File(getDirectory(), relativePath);
						callback.messageLogged("receiving file \"" + file.getAbsolutePath() + "\"... ");

						if (createMissingFolders) {
							file.getParentFile().mkdirs();
						}
						try {
							// check if we need to skip the file first
							if (!file.exists() && transferNonexistingFiles) {
								transferFile(file, dos, dis, buffer, length, lastModified, callback);
							} else if (file.exists() && transferExistingFiles) {
								switch (FilePreferenceMode.values()[filePreferenceMode]) {
								case TRANSFER_ALL:
									transferFile(file, dos, dis, buffer, length, lastModified, callback);
									break;
								case TRANSFER_EQUAL:
									if (length == file.length()) {
										transferFile(file, dos, dis, buffer, length, lastModified, callback);
									} else {
										skipFile(dos, callback);
									}
									break;
								case TRANSFER_LARGER:
									if (length > file.length()) {
										transferFile(file, dos, dis, buffer, length, lastModified, callback);
									} else {
										skipFile(dos, callback);
									}
									break;
								case TRANSFER_NEWER:
									if (lastModified > file.lastModified()) {
										transferFile(file, dos, dis, buffer, length, lastModified, callback);
									} else {
										skipFile(dos, callback);
									}
									break;
								case TRANSFER_OLDER:
									if (lastModified < file.lastModified()) {
										transferFile(file, dos, dis, buffer, length, lastModified, callback);
									} else {
										skipFile(dos, callback);
									}
									break;
								case TRANSFER_SAME:
									if (lastModified == file.lastModified()) {
										transferFile(file, dos, dis, buffer, length, lastModified, callback);
									} else {
										skipFile(dos, callback);
									}
									break;
								case TRANSFER_SAME_AND_NEWER:
									if (lastModified >= file.lastModified()) {
										transferFile(file, dos, dis, buffer, length, lastModified, callback);
									} else {
										skipFile(dos, callback);
									}
									break;
								case TRANSFER_SAME_AND_OLDER:
									if (lastModified <= file.lastModified()) {
										transferFile(file, dos, dis, buffer, length, lastModified, callback);
									} else {
										skipFile(dos, callback);
									}
									break;
								case TRANSFER_SMALLER:
									if (length < file.length()) {
										transferFile(file, dos, dis, buffer, length, lastModified, callback);
									} else {
										skipFile(dos, callback);
									}
									break;
								default:
									break;

								}
							} else {
								skipFile(dos, callback);
							}
						} catch (IOException e) {
							e.printStackTrace();
							callback.messageLogged("failed: " + e.getMessage() + "\n");
							dos.writeBoolean(false); // don't send file
						}

						// update file count
						curFile++;
						callback.progressUpdated(curFile, fileCount);
						break;
					case PACKET_SETTINGS:
						allowClientChanges = dis.readBoolean();
						createMissingFolders = dis.readBoolean();
						purgeDirectory = dis.readBoolean();
						transferNonexistingFiles = dis.readBoolean();
						transferExistingFiles = dis.readBoolean();
						filePreferenceMode = dis.readInt();
						if (!ignoreSettingsUpdate) {
							callback.settingsChanged(allowClientChanges, createMissingFolders, purgeDirectory, transferNonexistingFiles, transferExistingFiles, filePreferenceMode);
						}
						ignoreSettingsUpdate = false;
						break;
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
		receiveThread.start();
	}

	private void skipFile(DataOutputStream dos, INetworkCallback callback) throws IOException {
		writeFileRejectPacket();
		callback.messageLogged("skipped.\n");
	}

	private void transferFile(File file, DataOutputStream dos, DataInputStream dis, byte[] buffer, long length, long lastModified, INetworkCallback callback) throws IOException {
		// try to open file
		FileOutputStream fos = new FileOutputStream(file);

		// tell server we want the file
		writeFileAcceptPacket();
		if (dis.readBoolean()) { // check if server can send file
			transfer(dis, fos, buffer, length);
			fos.close();
			file.setLastModified(lastModified);
			callback.messageLogged("done.\n");
		} else {
			callback.messageLogged("skipped.\n");
		}
	}

	private void deleteDirectory(File directory) {
		// delete all files first
		for (File f : directory.listFiles()) {
			if (f.isFile()) {
				f.delete();
			}
		}

		// delete directories
		for (File f : directory.listFiles()) {
			if (f.isDirectory()) {
				deleteDirectory(f);
			}
		}

		// delete ourselves
		directory.delete();
	}

}
