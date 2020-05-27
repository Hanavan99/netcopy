package com.github.hanavan99.netcopy.ui;

import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import com.github.hanavan99.netcopy.net.FilePreferenceMode;
import com.github.hanavan99.netcopy.net.NetworkTransfer;

public abstract class TransferWindow extends Window {

	public static final String MESSAGE_SKIPPED = "skipped.";
	public static final String MESSAGE_DONE = "done.";
	public static final String MESSAGE_STARTED = "transferring %d files...";
	public static final String MESSAGE_FAILED = "failed: %s";
	public static final String PROGRESS_TOTAL = "Total progress: (%d/%d)";
	public static final String PROGRESS_FILE = "File progress: (%s/%s)";
	public static final String DIALOG_CLIENT_CONNECTED = "Client connected.";
	public static final String DIALOG_TRANSFER_FINISHED = "Transfer finished.";
	public static final String DIALOG_CLIENT_DISCONNECTED = "Lost connection to client: %s";
	public static final String DIALOG_NO_CLIENT = "No client is connected.";

	protected JTextField pathText;
	protected JButton browseButton;
	protected JCheckBox createMissingFolders;
	protected JCheckBox purgeDirectory;
	protected JCheckBox transferNonexistingFiles;
	protected JCheckBox transferExistingFiles;
	protected JComboBox<FilePreferenceMode> filePreferenceMode;
	protected JTextArea statusText;
	protected JProgressBar progressBar;
	protected JProgressBar fileProgressBar;
	protected JLabel progressLabel;
	protected JLabel fileProgressLabel;

	public TransferWindow(String name, int x, int y, int width, int height) {
		super(name, x, y, width, height);
	}

	protected String addSizeIndicator(long value) {
		if (value < 1_000L) {
			return value + "B";
		} else if (value < 1_000_000L) {
			return round(value / 1e3, 1) + "KB";
		} else if (value < 1_000_000_000L) {
			return round(value / 1e6, 1) + "MB";
		} else {
			return round(value / 1e9, 1) + "GB";
		}
	}

	private String round(double value, int digits) {
		return String.format("%." + digits + "f", value);
	}

	protected void sendSettings(NetworkTransfer transfer, boolean allowClientChanges) {
		if (transfer != null) {
			try {
				transfer.writeSettingsPacket(allowClientChanges, createMissingFolders.isSelected(), purgeDirectory.isSelected(), transferNonexistingFiles.isSelected(), transferExistingFiles.isSelected(), filePreferenceMode.getSelectedIndex());
			} catch (IOException e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(frame, "Failed to send settings: " + e.getMessage());
			}
		}
	}

	protected void updateProgress(int cur, int max) {
		progressBar.setMaximum(max);
		progressBar.setValue(cur);
		progressLabel.setText("Total progress: (" + cur + "/" + max + ")");
	}

	protected void updateFileProgress(long cur, long max) {
		int value = (int) ((cur * 100L) / max);
		fileProgressBar.setMaximum(100);
		fileProgressBar.setValue(value);
		fileProgressLabel.setText("File progress: (" + addSizeIndicator(cur) + "/" + addSizeIndicator(max) + ")");
	}

	protected void setUIState(boolean enabled) {
		pathText.setEnabled(enabled);
		browseButton.setEnabled(enabled);
		createMissingFolders.setEnabled(enabled);
		purgeDirectory.setEnabled(enabled);
		transferNonexistingFiles.setEnabled(enabled);
		transferExistingFiles.setEnabled(enabled);
		filePreferenceMode.setEnabled(enabled);
	}

	protected void updateSettings(boolean allowClientChanges, boolean createMissingFolders, boolean purgeDirectory, boolean transferNonexistingFiles, boolean transferExistingFiles, int filePreferenceMode) {
		this.createMissingFolders.setSelected(createMissingFolders);
		this.purgeDirectory.setSelected(purgeDirectory);
		this.transferNonexistingFiles.setSelected(transferNonexistingFiles);
		this.transferExistingFiles.setSelected(transferExistingFiles);
		this.filePreferenceMode.setSelectedItem(FilePreferenceMode.values()[filePreferenceMode]);

		this.createMissingFolders.setEnabled(allowClientChanges);
		this.purgeDirectory.setEnabled(allowClientChanges);
		this.transferNonexistingFiles.setEnabled(allowClientChanges);
		this.transferExistingFiles.setEnabled(allowClientChanges);
		this.filePreferenceMode.setEnabled(allowClientChanges);
	}

}
